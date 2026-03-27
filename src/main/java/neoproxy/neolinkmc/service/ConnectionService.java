package neoproxy.neolinkmc.service;

import fun.ceroxe.api.net.SecureSocket;
import fun.ceroxe.api.thread.ThreadManager;
import neoproxy.neolinkmc.NeoLinkMC;
import neoproxy.neolinkmc.config.ConnectionConfig;
import neoproxy.neolinkmc.config.LanguageData;
import neoproxy.neolinkmc.service.thread.CheckAliveTask;
import neoproxy.neolinkmc.service.thread.TCPTransformer;
import neoproxy.neolinkmc.util.VersionInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 连接服务核心类
 * <p>
 * 设计原则：
 * 1. 单一职责 - 只负责连接管理和数据转发
 * 2. 依赖注入 - 通过构造函数或配置对象注入依赖
 * 3. 封装性 - 所有字段私有，通过方法访问
 * 4. 线程安全 - 使用 volatile 和同步机制
 *
 * @author NeoProxy Team
 * @version 1.0.0
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
    private ConnectionConfig config;
    private String key;
    private int localPort = INVALID_LOCAL_PORT;

    public ConnectionService() {
        this(new MinecraftMessageHandler());
    }

    public ConnectionService(@NotNull MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        this.languageData = new LanguageData();
    }

    public void start() {
        start(null);
    }

    public void start(@Nullable String externalKey) {
        if (running) return;

        try {
            this.key = validateAndNormalizeKey(externalKey);
            this.config = loadOrCreateConfig();

            messageHandler.info(languageData.VERSION + VersionInfo.VERSION);
            messageHandler.info("NeoLinkMC 客户端启动中...");

            running = true;
            initialized = true;
            startConnectionThread();

        } catch (Exception e) {
            messageHandler.error("连接服务启动失败", e);
            messageHandler.sendError("服务启动失败：" + e.getMessage());
        }
    }

    public void stop() {
        if (!running) return;
        running = false;
        try {
            CheckAliveTask.stop();
            closeQuietly(hookSocket);
            closeQuietly(connectingSocket);
            if (connectionThread != null && connectionThread.isAlive()) {
                connectionThread.interrupt();
            }
            messageHandler.info(languageData.SERVICE_STOPPED);
        } catch (Exception e) {
            messageHandler.error("停止连接服务出错", e);
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
        // 如果 config 为 null 或尚未初始化，返回默认值 25565
        return config != null ? config.getLocalPort() : 25565;
    }

    private void startConnectionThread() {
        connectionThread = new Thread(() -> {
            try {
                connectToNeoServer();
                exchangeClientInfoWithServer();
                CheckAliveTask.start(this);
                listenForServerCommands();
            } catch (Exception e) {
                if (running) handleConnectionFailure(e);
            }
        }, "NeoLink-Connection");
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    private void connectToNeoServer() throws IOException {
        // 安全检查：确保配置已初始化
        if (config == null) {
            throw new IOException("配置未初始化，无法连接到服务器");
        }
        messageHandler.info(languageData.CONNECT_TO + config.getRemoteDomain() + languageData.OMITTED);
        hookSocket = new SecureSocket(config.getRemoteDomain(), config.getHookPort());
    }

    private void exchangeClientInfoWithServer() throws IOException {
        String clientInfo = "zh;" + VersionInfo.VERSION + ";" + key + ";T";
        messageHandler.info("正在发送客户端信息到服务器...");
        messageHandler.debug("客户端信息格式: zh;版本;密钥;T");
        messageHandler.debug("实际发送内容: " + clientInfo.replace(key, "[密钥隐藏]"));
        sendStr(clientInfo);

        messageHandler.info("等待服务器响应...");
        String serverResponse = receiveStr();
        messageHandler.info("服务器响应: " + serverResponse);

        if (serverResponse == null) {
            messageHandler.error("服务器返回空响应，连接被拒绝");
            throw new IOException("服务器返回空响应");
        }

        if (isErrorResponse(serverResponse)) {
            messageHandler.error("服务器拒绝连接: " + serverResponse);
            messageHandler.sendError(serverResponse);
            exitAndFreeze(0);
        } else {
            lastReceivedTime = System.currentTimeMillis();
            messageHandler.info("[服务端] " + serverResponse);
            messageHandler.sendSuccess(serverResponse);
        }
    }

    private void listenForServerCommands() throws IOException {
        String message;
        while (running && (message = receiveStr()) != null) {
            lastReceivedTime = System.currentTimeMillis();
            try {
                if (Thread.interrupted()) throw new InterruptedException();
            } catch (InterruptedException e) {
                return;
            }

            if (message.startsWith(":>")) {
                handleServerCommand(message.substring(2));
            } else if (message.contains("消耗") || message.contains("流量")) {
                messageHandler.warn("[服务器通告] " + message);
                messageHandler.sendWarning(message);
            } else {
                messageHandler.info("[服务器消息] " + message);
                messageHandler.sendMessage(message);
            }
        }
        throw new IOException("Connection closed.");
    }

    private void handleServerCommand(String command) {
        String[] parts = command.split(";");
        switch (parts[0]) {
            case "sendSocketTCP" -> ThreadManager.runAsync(() -> createNewTCPConnection(parts[1], parts[2]));
            case "exitNoFlow" -> {
                messageHandler.info(languageData.NO_FLOW_LEFT);
                messageHandler.sendError(languageData.NO_FLOW_LEFT);
                exitAndFreeze(0);
            }
        }
    }

    private void handleConnectionFailure(Exception e) {
        messageHandler.error("连接失败，详细异常信息:", e);
        messageHandler.error("异常类型: " + e.getClass().getName());
        messageHandler.error("异常消息: " + e.getMessage());
        if (e.getCause() != null) {
            messageHandler.error("异常原因: " + e.getCause().getMessage());
        }

        CheckAliveTask.stop();
        // 安全获取远程域名，如果 config 为 null 则使用默认值
        String remoteDomain = config != null ? config.getRemoteDomain() : "p.ceroxe.fun";
        messageHandler.info(languageData.FAIL_TO_BUILD_A_CHANNEL_FROM + remoteDomain);
        messageHandler.sendError(languageData.FAIL_TO_BUILD_A_CHANNEL_FROM + remoteDomain);

        exitAndFreeze(-1, e);
    }

    public void createNewTCPConnection(String socketID, String remoteAddress) {
        // 安全检查：确保配置已初始化
        if (config == null) {
            messageHandler.error("配置未初始化，无法创建 TCP 连接");
            return;
        }

        Socket localServerSocket = null;
        SecureSocket neoTransferSocket = null;
        try {
            int effectiveLocalPort = getLocalPort();
            localServerSocket = connectToLocalRobustly(config.getLocalDomain(), effectiveLocalPort);
            neoTransferSocket = new SecureSocket(config.getRemoteDomain(), config.getConnectPort());
            neoTransferSocket.sendStr("TCP;" + socketID);

            // 输出连接建立信息到日志
            messageHandler.info(languageData.A_TCP_CONNECTION + remoteAddress + " -> " +
                    config.getLocalDomain() + ":" + effectiveLocalPort + languageData.BUILD_UP);

            // enableProxyProtocol 硬编码为 false，PPv2 头会被剥离不传给 Minecraft
            TCPTransformer serverToNeoTask = new TCPTransformer(neoTransferSocket, localServerSocket, false);
            TCPTransformer neoToServerTask = new TCPTransformer(localServerSocket, neoTransferSocket, false);
            ThreadManager manager = new ThreadManager(serverToNeoTask, neoToServerTask);

            manager.startAsyncWithCallback(result -> {
                // 输出连接断开信息到日志
                messageHandler.info(languageData.A_TCP_CONNECTION + remoteAddress + " -> " +
                        config.getLocalDomain() + ":" + effectiveLocalPort + languageData.DESTROY);
                manager.close();
            });
        } catch (Exception e) {
            messageHandler.info(languageData.FAIL_TO_CONNECT_LOCALHOST + config.getLocalDomain() + ":" + getLocalPort());
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
        exitAndFreeze(exitCode, null);
    }

    private void exitAndFreeze(int exitCode, @Nullable Exception cause) {
        if (cause != null) {
            messageHandler.error("致命错误，服务停止。异常详情:", cause);
        }
        stop();
        messageHandler.sendError("内网穿透服务已由于致命错误停止。");
    }

    private String validateAndNormalizeKey(@Nullable String externalKey) {
        if (externalKey != null && !externalKey.trim().isEmpty()) {
            messageHandler.debug("使用外部传入的密钥");
            return externalKey.trim();
        }
        messageHandler.sendWarning("未配置密钥，使用默认密钥");
        return DEFAULT_KEY;
    }

    private boolean isErrorResponse(String response) {
        return response.contains("exit") || response.contains("退") || response.contains("错误")
                || response.contains("denied") || response.contains("already")
                || response.contains("过期") || response.contains("错")
                || response.contains("密钥");
    }

    /**
     * 加载或创建配置
     * 与老代码 applyConfig() 逻辑一致：优先使用已设置的值，只有在未设置时才从配置读取
     */
    private ConnectionConfig loadOrCreateConfig() {
        // 先加载配置文件（只加载一次）
        neoproxy.neolinkmc.config.ConfigManager.loadConfig();

        // 与老代码 applyConfig() 逻辑一致：优先使用已设置的值，只有在未设置时才从配置读取
        String remoteDomain = getStringFromConfig("remote_domain", "p.ceroxe.fun");
        String localDomain = getStringFromConfig("local_domain", "localhost");
        int hookPort = getIntFromConfig("host_hook_port", 44801);
        int connectPort = getIntFromConfig("host_connect_port", 44802);
        int configLocalPort = getIntFromConfig("local_port", 25565);

        // 优先使用外部设置的端口（与老代码逻辑一致）
        int effectiveLocalPort = localPort > 0 ? localPort : configLocalPort;

        return ConnectionConfig.builder()
                .remoteDomain(remoteDomain)
                .localDomain(localDomain)
                .hookPort(hookPort)
                .connectPort(connectPort)
                .localPort(effectiveLocalPort)
                .build();
    }

    /**
     * 从已加载的配置中获取字符串值
     */
    private String getStringFromConfig(String key, String defaultValue) {
        try {
            return neoproxy.neolinkmc.config.ConfigManager.getString(key, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 从已加载的配置中获取整数值
     * 与老代码一致：配置文件中端口存储为字符串，需要解析
     */
    private int getIntFromConfig(String key, int defaultValue) {
        try {
            String value = neoproxy.neolinkmc.config.ConfigManager.getString(key, String.valueOf(defaultValue));
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 从已加载的配置中获取布尔值
     */
    private boolean getBooleanFromConfig(String key, boolean defaultValue) {
        try {
            return neoproxy.neolinkmc.config.ConfigManager.getBoolean(key, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public String getRemoteDomain() {
        return config != null ? config.getRemoteDomain() : "localhost";
    }

    public int getHookPort() {
        return config != null ? config.getHookPort() : 44801;
    }

    public String getKey() {
        return key;
    }

    public long getLastReceivedTime() {
        return lastReceivedTime;
    }

    public void sendHeartbeat() throws IOException {
        if (hookSocket != null) {
            hookSocket.sendStr("PING");
        }
    }

    public void closeHookSocket() {
        closeQuietly(hookSocket);
    }

    public boolean isHookSocketAvailable() {
        return hookSocket != null;
    }

    public Object getHookSocketLock() {
        return hookSocket != null ? hookSocket : new Object();
    }
}
