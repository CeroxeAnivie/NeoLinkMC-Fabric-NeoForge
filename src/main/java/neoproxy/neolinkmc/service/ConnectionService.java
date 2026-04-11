package neoproxy.neolinkmc.service;

import fun.ceroxe.api.net.SecureSocket;
import fun.ceroxe.api.thread.ThreadManager;
import neoproxy.neolinkmc.NeoLinkMC;
import neoproxy.neolinkmc.config.ConnectionConfig;
import neoproxy.neolinkmc.config.LanguageData;
import neoproxy.neolinkmc.service.thread.CheckAliveTask;
import neoproxy.neolinkmc.service.thread.TCPTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 连接服务核心类 - 重构版本
 * <p>
 * 设计原则：
 * 1. 单一职责 - 只负责连接管理和数据转发
 * 2. 依赖注入 - 通过构造函数注入所有依赖
 * 3. 封装性 - 所有字段私有，通过方法访问
 * 4. 线程安全 - 使用 volatile 和同步机制
 *
 * @author NeoProxy Team
 * @version 2.0.0
 */
public final class ConnectionService {

    private static final int INVALID_LOCAL_PORT = -1;
    private static final String DEFAULT_KEY = "Free";

    private volatile long lastReceivedTime = System.currentTimeMillis();
    private volatile SecureSocket hookSocket;
    private volatile Socket connectingSocket;
    private volatile boolean running = false;
    private volatile boolean initialized = false;
    private Thread connectionThread;

    private final MessageHandler messageHandler;
    private final LanguageData languageData;
    private final CheckAliveTask checkAliveTask;
    private ConnectionConfig config;
    private String key;
    private int localPort = INVALID_LOCAL_PORT;

    public ConnectionService() {
        this(new MinecraftMessageHandler(), new LanguageData());
    }

    public ConnectionService(@NotNull MessageHandler messageHandler, @NotNull LanguageData languageData) {
        this.messageHandler = messageHandler;
        this.languageData = languageData;
        this.checkAliveTask = new CheckAliveTask(this::getLastReceivedTime, this::sendHeartbeat, messageHandler);
    }

    public void start() {
        start(null);
    }

    public void start(@Nullable String externalKey) {
        if (running) return;

        try {
            this.key = validateAndNormalizeKey(externalKey);
            this.config = loadOrCreateConfig();

            messageHandler.log(languageData.VERSION + NeoLinkMC.VERSION, MessageHandler.LogLevel.INFO);
            messageHandler.send("NeoLinkMC 客户端启动中...", MessageHandler.MessageType.INFO);

            running = true;
            initialized = true;
            startConnectionThread();

        } catch (Exception e) {
            messageHandler.log("连接服务启动失败", MessageHandler.LogLevel.ERROR);
            messageHandler.send("服务启动失败：" + e.getMessage(), MessageHandler.MessageType.ERROR);
        }
    }

    /**
     * 同步验证密钥 - 用于 GUI 启动流程
     * 先验证密钥是否正确，如果正确再启动完整服务
     *
     * @param externalKey 外部传入的密钥
     * @return 验证结果，包含是否成功和错误信息
     */
    public KeyValidationResult validateKeySync(@Nullable String externalKey) {
        String normalizedKey = validateAndNormalizeKey(externalKey);
        ConnectionConfig tempConfig = loadOrCreateConfig();

        try {
            messageHandler.send("正在验证密钥...", MessageHandler.MessageType.INFO);

            // 创建临时连接验证密钥
            SecureSocket tempSocket = new SecureSocket(tempConfig.getRemoteDomain(), tempConfig.getHookPort());

            // 发送客户端信息
            String clientInfo = "zh;" + NeoLinkMC.VERSION + ";" + normalizedKey + ";T";
            tempSocket.sendStr(clientInfo);

            // 等待服务器响应（同步阻塞，带超时）
            String response = tempSocket.receiveStr();
            tempSocket.close();

            if (response == null) {
                return new KeyValidationResult(false, "服务器返回空响应");
            }

            if (isErrorResponse(response)) {
                return new KeyValidationResult(false, response);
            }

            // 密钥验证成功，保存密钥并返回成功
            this.key = normalizedKey;
            return new KeyValidationResult(true, null);

        } catch (Exception e) {
            return new KeyValidationResult(false, "连接失败: " + e.getMessage());
        }
    }

    /**
     * 在密钥验证成功后启动完整服务
     * 注意：必须先调用 validateKeySync 验证密钥
     */
    public void startAfterValidation() {
        if (running) return;
        if (this.key == null) {
            throw new IllegalStateException("密钥未验证，请先调用 validateKeySync");
        }

        this.config = loadOrCreateConfig();
        running = true;
        initialized = true;
        startConnectionThread();
    }

    /**
     * 密钥验证结果封装类
     */
    public record KeyValidationResult(boolean success, String errorMessage) {
    }

    public void stop() {
        if (!running) return;
        running = false;
        try {
            checkAliveTask.stop();
            closeQuietly(hookSocket);
            closeQuietly(connectingSocket);
            if (connectionThread != null && connectionThread.isAlive()) {
                connectionThread.interrupt();
            }
            messageHandler.send(languageData.SERVICE_STOPPED, MessageHandler.MessageType.INFO);
        } catch (Exception e) {
            messageHandler.log("停止连接服务出错", MessageHandler.LogLevel.ERROR);
        } finally {
            initialized = false;
        }
    }

    public boolean isRunning() {
        return running && initialized;
    }

    public void setLocalPort(int port) {
        if (port > 0 && port <= 65535) {
            this.localPort = port;
        }
    }

    public int getLocalPort() {
        if (localPort > 0) {
            return localPort;
        }
        return config != null ? config.getLocalPort() : 25565;
    }

    public long getLastReceivedTime() {
        return lastReceivedTime;
    }

    public boolean isHookSocketAvailable() {
        return hookSocket != null && hookSocket.isConnected();
    }

    public Object getHookSocketLock() {
        return this;
    }

    public void sendHeartbeat() throws IOException {
        if (hookSocket != null) {
            hookSocket.sendStr("PING");
        }
    }

    private void startConnectionThread() {
        connectionThread = new Thread(() -> {
            try {
                connectToNeoServer();
                exchangeClientInfoWithServer();
                checkAliveTask.start();
                listenForServerCommands();
            } catch (Exception e) {
                if (running) handleConnectionFailure(e);
            }
        }, "NeoLink-Connection");
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    private void connectToNeoServer() throws IOException {
        if (config == null) {
            throw new IOException("配置未初始化，无法连接到服务器");
        }
        messageHandler.send(languageData.CONNECT_TO + config.getRemoteDomain() + languageData.OMITTED, MessageHandler.MessageType.INFO);
        hookSocket = new SecureSocket(config.getRemoteDomain(), config.getHookPort());
    }

    private void exchangeClientInfoWithServer() throws IOException {
        String clientInfo = "zh;" + NeoLinkMC.VERSION + ";" + key + ";T";
        messageHandler.send("正在发送客户端信息到服务器...", MessageHandler.MessageType.INFO);
        messageHandler.log("客户端信息: " + clientInfo.replace(key, "[密钥隐藏]"), MessageHandler.LogLevel.DEBUG);
        sendStr(clientInfo);

        messageHandler.send("等待服务器响应...", MessageHandler.MessageType.INFO);
        String serverResponse = receiveStr();
        messageHandler.send("服务器响应: " + serverResponse, MessageHandler.MessageType.INFO);

        if (serverResponse == null) {
            messageHandler.log("服务器返回空响应，连接被拒绝", MessageHandler.LogLevel.ERROR);
            throw new IOException("服务器返回空响应");
        }

        if (isErrorResponse(serverResponse)) {
            messageHandler.log("服务器拒绝连接: " + serverResponse, MessageHandler.LogLevel.ERROR);
            messageHandler.send(serverResponse, MessageHandler.MessageType.ERROR);
            exitAndFreeze(0);
        } else {
            lastReceivedTime = System.currentTimeMillis();
            messageHandler.send(serverResponse, MessageHandler.MessageType.SUCCESS);
        }
    }

    private void listenForServerCommands() throws IOException {
        String message;
        try {
            while (running && (message = receiveStr()) != null) {
                lastReceivedTime = System.currentTimeMillis();

                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                if (message.startsWith(":>")) {
                    handleServerCommand(message.substring(2));
                } else if (message.contains("消耗") || message.contains("流量")) {
                    messageHandler.send(message, MessageHandler.MessageType.WARNING);
                } else {
                    messageHandler.send(message, MessageHandler.MessageType.INFO);
                }
            }
        } catch (InterruptedException e) {
            // 线程被中断，优雅退出
            Thread.currentThread().interrupt();
            messageHandler.log("连接监听线程被中断", MessageHandler.LogLevel.DEBUG);
            return;
        }
        throw new IOException("Connection closed.");
    }

    private void handleServerCommand(String command) {
        String[] parts = command.split(";");

        // 防御性校验：确保命令不为空
        if (parts.length == 0 || parts[0].isEmpty()) {
            messageHandler.log("收到空命令，忽略", MessageHandler.LogLevel.WARN);
            return;
        }

        switch (parts[0]) {
            case "sendSocketTCP" -> {
                // 防御性校验：确保参数足够
                if (parts.length >= 3) {
                    ThreadManager.runAsync(() -> createNewTCPConnection(parts[1], parts[2]));
                } else {
                    messageHandler.log("sendSocketTCP 命令参数不足，期望 3 个，实际 " + parts.length, MessageHandler.LogLevel.WARN);
                }
            }
            case "exitNoFlow" -> {
                messageHandler.send(languageData.NO_FLOW_LEFT, MessageHandler.MessageType.ERROR);
                exitAndFreeze(0);
            }
            case "exitKey" -> {
                messageHandler.send("密钥验证失败，连接被拒绝", MessageHandler.MessageType.ERROR);
                exitAndFreeze(0);
            }
            default -> messageHandler.log("未知服务器命令: " + parts[0], MessageHandler.LogLevel.WARN);
        }
    }

    private void handleConnectionFailure(Exception e) {
        messageHandler.log("连接失败: " + e.getMessage(), MessageHandler.LogLevel.ERROR);

        checkAliveTask.stop();
        String remoteDomain = config != null ? config.getRemoteDomain() : "p.ceroxe.fun";
        messageHandler.send(languageData.FAIL_TO_BUILD_A_CHANNEL_FROM + remoteDomain, MessageHandler.MessageType.ERROR);

        exitAndFreeze(-1);
    }

    public void createNewTCPConnection(String socketID, String remoteAddress) {
        if (config == null) {
            messageHandler.log("配置未初始化，无法创建 TCP 连接", MessageHandler.LogLevel.ERROR);
            return;
        }

        Socket localServerSocket = null;
        SecureSocket neoTransferSocket = null;
        try {
            int effectiveLocalPort = getLocalPort();
            localServerSocket = connectToLocalRobustly(config.getLocalDomain(), effectiveLocalPort);
            neoTransferSocket = new SecureSocket(config.getRemoteDomain(), config.getConnectPort());
            neoTransferSocket.sendStr("TCP;" + socketID);

            messageHandler.log(languageData.A_TCP_CONNECTION + remoteAddress + " -> " +
                    config.getLocalDomain() + ":" + effectiveLocalPort + languageData.BUILD_UP, MessageHandler.LogLevel.INFO);

            TCPTransformer serverToNeoTask = new TCPTransformer(neoTransferSocket, localServerSocket, false);
            TCPTransformer neoToServerTask = new TCPTransformer(localServerSocket, neoTransferSocket, false);
            ThreadManager manager = new ThreadManager(serverToNeoTask, neoToServerTask);

            manager.startAsyncWithCallback(result -> {
                messageHandler.log(languageData.A_TCP_CONNECTION + remoteAddress + " -> " +
                        config.getLocalDomain() + ":" + effectiveLocalPort + languageData.DESTROY, MessageHandler.LogLevel.INFO);
                manager.close();
            });
        } catch (Exception e) {
            messageHandler.log(languageData.FAIL_TO_CONNECT_LOCALHOST + config.getLocalDomain() + ":" + getLocalPort(), MessageHandler.LogLevel.ERROR);
            closeQuietly(localServerSocket);
            closeQuietly(neoTransferSocket);
        }
    }

    private Socket connectToLocalRobustly(String host, int port) throws IOException {
        InetAddress[] addresses = InetAddress.getAllByName(host);
        IOException lastEx = null;
        for (InetAddress address : addresses) {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(address, port), 2000);
                return socket;
            } catch (IOException e) {
                lastEx = e;
            }
        }
        throw (lastEx != null) ? lastEx : new IOException("Resolve failed");
    }

    private void sendStr(String str) throws IOException {
        if (hookSocket != null) hookSocket.sendStr(str);
    }

    private String receiveStr() throws IOException {
        return hookSocket != null ? hookSocket.receiveStr() : null;
    }

    private void closeQuietly(AutoCloseable c) {
        try {
            if (c != null) c.close();
        } catch (Exception ignored) {
        }
    }

    private void exitAndFreeze(int exitCode) {
        stop();
        messageHandler.send("内网穿透服务已停止", MessageHandler.MessageType.ERROR);
    }

    private String validateAndNormalizeKey(String key) {
        return (key == null || key.trim().isEmpty()) ? DEFAULT_KEY : key.trim();
    }

    private ConnectionConfig loadOrCreateConfig() {
        return ConnectionConfig.builder().build();
    }

    private boolean isErrorResponse(String response) {
        String lower = response.toLowerCase();
        return lower.contains("exit") || lower.contains("退") || lower.contains("错误")
                || lower.contains("denied") || lower.contains("already")
                || lower.contains("过期") || lower.contains("占")
                || lower.contains("密钥错误");
    }
}
