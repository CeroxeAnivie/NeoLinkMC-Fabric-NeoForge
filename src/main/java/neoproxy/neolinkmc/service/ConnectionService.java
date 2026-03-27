package neoproxy.neolinkmc.service;

import fun.ceroxe.api.net.SecureSocket;
import fun.ceroxe.api.thread.ThreadManager;
import neoproxy.neolinkmc.NeoLinkMC;
import neoproxy.neolinkmc.config.ConfigManager;
import neoproxy.neolinkmc.config.LanguageData;
import neoproxy.neolinkmc.service.thread.CheckAliveTask;
import neoproxy.neolinkmc.service.thread.TCPTransformer;
import neoproxy.neolinkmc.util.VersionInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 连接服务核心类
 * 负责维护与远端 NeoProxyServer 的控制信道及数据隧道下发
 */
public class ConnectionService {

    public static final int INVALID_LOCAL_PORT = -1;

    public volatile long lastReceivedTime = System.currentTimeMillis();
    public int remotePort;
    public String remoteDomainName = "localhost";
    public String localDomainName = "localhost";
    public int hostHookPort = 44801;
    public int hostConnectPort = 44802;
    public volatile SecureSocket hookSocket;
    public volatile Socket connectingSocket = null;
    public String key = null;
    public int localPort = INVALID_LOCAL_PORT;
    public LanguageData languageData;
    public boolean showConnection = true;
    public boolean enableProxyProtocol = false;

    private volatile boolean running = false;
    private volatile boolean initialized = false;
    private Thread connectionThread;

    public void start() {
        start(null);
    }

    public void start(String externalKey) {
        if (running) return;

        try {
            ConfigManager.loadConfig();
            applyConfig();

            // 【关键修复】如果外部传入了密钥，使用外部密钥（优先）
            if (externalKey != null && !externalKey.trim().isEmpty()) {
                this.key = externalKey.trim();
                NeoLinkMC.LOGGER.debug("[DEBUG] 使用外部传入的密钥");
            }

            // 实例化全新的单语言 LanguageData
            languageData = new LanguageData();

            // 容错：兜底空密钥处理
            if (!validateKey()) {
                throw new IllegalStateException("密钥验证不通过");
            }

            say(languageData.VERSION + VersionInfo.VERSION);
            NeoLinkMC.LOGGER.info("NeoLinkMC 客户端启动中...");

            running = true;
            initialized = true;
            startConnectionThread();

        } catch (Exception e) {
            NeoLinkMC.LOGGER.error("连接服务启动失败", e);
            sendToMinecraftChat("§c[NeoLinkMC] §f服务启动失败：" + e.getMessage());
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
            NeoLinkMC.LOGGER.info(languageData.SERVICE_STOPPED);
        } catch (Exception e) {
            NeoLinkMC.LOGGER.error("停止连接服务出错", e);
        } finally {
            initialized = false;
        }
    }

    private void applyConfig() {
        // 注意：优先使用已设置的值（从GUI传入），只有在未设置时才从配置读取
        if (this.remoteDomainName == null || this.remoteDomainName.equals("localhost")) {
            this.remoteDomainName = ConfigManager.getString("remote_domain", "p.ceroxe.fun");
        }
        if (this.localDomainName == null || this.localDomainName.equals("localhost")) {
            this.localDomainName = ConfigManager.getString("local_domain", "localhost");
        }
        if (this.hostHookPort == 44801) {
            this.hostHookPort = Integer.parseInt(ConfigManager.getString("host_hook_port", "44801"));
        }
        if (this.hostConnectPort == 44802) {
            this.hostConnectPort = Integer.parseInt(ConfigManager.getString("host_connect_port", "44802"));
        }
        // 只有在未设置密钥时才从配置读取
        if (this.key == null) {
            this.key = ConfigManager.getString("key", null);
        }
        // 只有在未设置本地端口时才从配置读取
        if (this.localPort == INVALID_LOCAL_PORT) {
            try {
                this.localPort = Integer.parseInt(ConfigManager.getString("local_port", "-1"));
            } catch (Exception ignored) {
            }
        }
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
        say(languageData.CONNECT_TO + remoteDomainName + languageData.OMITTED);
        hookSocket = new SecureSocket(remoteDomainName, hostHookPort);
    }

    private void exchangeClientInfoWithServer() throws IOException {
        // 硬编码 "zh" 给服务端，只有TCP协议(T)
        String clientInfo = "zh;" + VersionInfo.VERSION + ";" + key + ";T";
        NeoLinkMC.LOGGER.info("[NeoLinkMC] 正在发送客户端信息到服务器...");
        NeoLinkMC.LOGGER.debug("[DEBUG] 客户端信息格式: zh;版本;密钥;T");
        NeoLinkMC.LOGGER.debug("[DEBUG] 实际发送内容: {}", clientInfo.replace(key, "[密钥隐藏]"));
        sendStr(clientInfo);

        NeoLinkMC.LOGGER.info("[NeoLinkMC] 等待服务器响应...");
        String serverResponse = receiveStr();
        NeoLinkMC.LOGGER.info("[NeoLinkMC] 服务器响应: {}", serverResponse);

        if (serverResponse == null) {
            NeoLinkMC.LOGGER.error("[NeoLinkMC] 服务器返回空响应，连接被拒绝");
            throw new IOException("服务器返回空响应");
        }

        if (serverResponse.contains("exit") || serverResponse.contains("退") || serverResponse.contains("错误")
                || serverResponse.contains("denied") || serverResponse.contains("already")
                || serverResponse.contains("过期") || serverResponse.contains("错")
                || serverResponse.contains("密钥")) {
            NeoLinkMC.LOGGER.error("[NeoLinkMC] 服务器拒绝连接: {}", serverResponse);
            say(serverResponse);
            sendToMinecraftChat("§c[NeoLinkMC] " + serverResponse);
            exitAndFreeze(0);
        } else {
            lastReceivedTime = System.currentTimeMillis();
            say("[服务端] " + serverResponse);
            sendToMinecraftChat("§a[NeoLinkMC] " + serverResponse);
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
                NeoLinkMC.LOGGER.warn("[服务器通告] " + message);
                sendToMinecraftChat("§e[NeoLinkMC] " + message);
            } else {
                say("[服务器消息] " + message);
                sendToMinecraftChat("§b[NeoLinkMC] " + message);
            }
        }
        throw new IOException("Connection closed.");
    }

    private void handleServerCommand(String command) {
        String[] parts = command.split(";");
        switch (parts[0]) {
            case "sendSocketTCP" -> ThreadManager.runAsync(() -> createNewTCPConnection(parts[1], parts[2]));
            case "exitNoFlow" -> {
                say(languageData.NO_FLOW_LEFT);
                sendToMinecraftChat("§c[NeoLinkMC] " + languageData.NO_FLOW_LEFT);
                exitAndFreeze(0);
            }
        }
    }

    private void handleConnectionFailure(Exception e) {
        // 【添加详细异常日志】
        NeoLinkMC.LOGGER.error("[NeoLinkMC] 连接失败，详细异常信息:", e);
        NeoLinkMC.LOGGER.error("[NeoLinkMC] 异常类型: {}", e.getClass().getName());
        NeoLinkMC.LOGGER.error("[NeoLinkMC] 异常消息: {}", e.getMessage());
        if (e.getCause() != null) {
            NeoLinkMC.LOGGER.error("[NeoLinkMC] 异常原因: {}", e.getCause().getMessage());
        }

        CheckAliveTask.stop();
        say(languageData.FAIL_TO_BUILD_A_CHANNEL_FROM + remoteDomainName);
        sendToMinecraftChat("§c[NeoLinkMC] " + languageData.FAIL_TO_BUILD_A_CHANNEL_FROM + remoteDomainName);

        // 连接失败时直接停止服务，不进行重连
        exitAndFreeze(-1, e);
    }

    public void createNewTCPConnection(String socketID, String remoteAddress) {
        Socket localServerSocket = null;
        SecureSocket neoTransferSocket = null;
        try {
            localServerSocket = connectToLocalRobustly(localDomainName, localPort);
            neoTransferSocket = new SecureSocket(remoteDomainName, hostConnectPort);
            neoTransferSocket.sendStr("TCP;" + socketID);

            if (showConnection) {
                say(languageData.A_TCP_CONNECTION + remoteAddress + " -> " + localDomainName + ":" + localPort + languageData.BUILD_UP);
            }

            TCPTransformer serverToNeoTask = new TCPTransformer(neoTransferSocket, localServerSocket, enableProxyProtocol);
            TCPTransformer neoToServerTask = new TCPTransformer(localServerSocket, neoTransferSocket, false);
            ThreadManager manager = new ThreadManager(serverToNeoTask, neoToServerTask);

            manager.startAsyncWithCallback(result -> {
                if (showConnection) {
                    say(languageData.A_TCP_CONNECTION + remoteAddress + " -> " + localDomainName + ":" + localPort + languageData.DESTROY);
                }
                manager.close();
            });
        } catch (Exception e) {
            if (showConnection) {
                say(languageData.FAIL_TO_CONNECT_LOCALHOST + localDomainName + ":" + localPort);
            }
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

    private void exitAndFreeze(int exitCode, Exception cause) {
        // 【添加详细异常日志】打印完整的异常堆栈
        if (cause != null) {
            NeoLinkMC.LOGGER.error("[NeoLinkMC] 致命错误，服务停止。异常详情:", cause);
        }
        stop();
        sendToMinecraftChat("§c[NeoLinkMC] §f内网穿透服务已由于致命错误停止。");
    }

    private boolean validateKey() {
        if (key == null || key.trim().isEmpty()) {
            key = KeyValidator.getDefaultKey();
            sendToMinecraftChat("§e[NeoLinkMC] §f未配置密钥，使用默认密钥");
        }
        key = key.trim();
        return true;
    }

    /**
     * 代替原先复杂的 Loggist，直接输出到 Fabric Logger
     */
    public void say(String str) {
        NeoLinkMC.LOGGER.info("[NeoLink] " + str);
    }

    /**
     * 安全地将消息推送到游戏内聊天框
     */
    public void sendToMinecraftChat(String message) {
        try {
            net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
            if (client != null) {
                client.execute(() -> {
                    if (client.player != null) {
                        client.player.displayClientMessage(net.minecraft.network.chat.Component.literal(message), false);
                    }
                });
            }
        } catch (Exception ignored) {
        }
    }

    public boolean isRunning() {
        return running && initialized;
    }
}