package neoproxy.neolinkmc.service;

import neoproxy.neolinkmc.config.ConnectionConfig;
import top.ceroxe.api.neolink.NeoLinkAPI;
import top.ceroxe.api.neolink.NeoLinkCfg;
import top.ceroxe.api.neolink.NeoLinkState;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Shared tunnel lifecycle adapter for all NeoLinkMC platform modules.
 *
 * <p>NeoLinkAPI is the single source of truth for the protocol, heartbeat,
 * transfer workers, TCP/UDP dispatch and resource cleanup. This adapter keeps
 * Minecraft-facing behavior stable while preventing each loader/version module
 * from re-implementing socket orchestration.</p>
 */
public final class ConnectionService implements AutoCloseable {
    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 5_000;

    private final MessageHandler messageHandler;
    private final AtomicBoolean startingOrRunning = new AtomicBoolean(false);

    private volatile NeoLinkAPI api;
    private volatile ConnectionConfig config;
    private volatile Thread workerThread;
    private volatile NeoLinkState state = NeoLinkState.STOPPED;

    public ConnectionService(MessageHandler messageHandler) {
        this.messageHandler = Objects.requireNonNull(messageHandler, "messageHandler");
    }

    public void start(ConnectionConfig nextConfig) {
        Objects.requireNonNull(nextConfig, "nextConfig");
        if (!startingOrRunning.compareAndSet(false, true)) {
            messageHandler.log("NeoLink tunnel is already starting or running.", MessageHandler.LogLevel.WARN);
            return;
        }

        this.config = nextConfig;
        NeoLinkAPI nextApi = createApi(nextConfig);
        this.api = nextApi;

        workerThread = new Thread(() -> runTunnel(nextApi), "NeoLinkMC-Tunnel");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    public void stop() {
        NeoLinkAPI activeApi = api;
        if (activeApi != null) {
            activeApi.close();
        }

        Thread activeWorker = workerThread;
        if (activeWorker != null) {
            activeWorker.interrupt();
        }
    }

    public boolean isRunning() {
        NeoLinkState currentState = state;
        return startingOrRunning.get()
                && (currentState == NeoLinkState.STARTING || currentState == NeoLinkState.RUNNING);
    }

    public NeoLinkState getState() {
        return state;
    }

    private NeoLinkAPI createApi(ConnectionConfig source) {
        NeoLinkCfg apiConfig = new NeoLinkCfg(
                source.remoteDomain(),
                source.hookPort(),
                source.hostConnectPort(),
                source.key(),
                source.localPort()
        )
                .setLocalDomainName(source.localDomain())
                .setLanguage(NeoLinkCfg.ZH_CH)
                .setTCPEnabled(true)
                .setUDPEnabled(false);

        return new NeoLinkAPI(apiConfig)
                .setOnStateChanged(this::onStateChanged)
                .setOnServerMessage(this::onServerMessage)
                .setOnError(this::onError)
                .setOnConnect((protocol, sourceAddress, targetAddress) ->
                        messageHandler.log(
                                protocol + " connection " + sourceAddress + " -> " + targetAddress + " established.",
                                MessageHandler.LogLevel.INFO
                        ))
                .setOnDisconnect((protocol, sourceAddress, targetAddress) ->
                        messageHandler.log(
                                protocol + " connection " + sourceAddress + " -> " + targetAddress + " closed.",
                                MessageHandler.LogLevel.INFO
                        ))
                .setDebugSink((message, cause) -> {
                    if (message != null) {
                        messageHandler.log(message, MessageHandler.LogLevel.DEBUG);
                    }
                    if (cause != null) {
                        messageHandler.log("NeoLinkAPI debug exception", MessageHandler.LogLevel.DEBUG, cause);
                    }
                });
    }

    private void runTunnel(NeoLinkAPI tunnel) {
        try {
            messageHandler.send("NeoLinkMC 客户端启动中...", MessageHandler.MessageType.INFO);
            tunnel.start(DEFAULT_CONNECT_TIMEOUT_MILLIS);
        } catch (Exception e) {
            if (startingOrRunning.get()) {
                onError("内网穿透服务启动或运行失败。", e);
            }
        } finally {
            state = NeoLinkState.STOPPED;
            startingOrRunning.set(false);
            api = null;
            workerThread = null;
            messageHandler.send("内网穿透服务已停止", MessageHandler.MessageType.INFO);
        }
    }

    private void onStateChanged(NeoLinkState nextState) {
        state = nextState;
        switch (nextState) {
            case STARTING -> {
                ConnectionConfig activeConfig = config;
                if (activeConfig != null) {
                    messageHandler.send(
                            "正在连接到 " + activeConfig.remoteDomain() + "...",
                            MessageHandler.MessageType.INFO
                    );
                }
            }
            case RUNNING -> messageHandler.send("内网穿透服务已启动", MessageHandler.MessageType.SUCCESS);
            case STOPPING -> messageHandler.send("正在停止内网穿透服务...", MessageHandler.MessageType.INFO);
            case FAILED -> messageHandler.send("内网穿透服务异常停止", MessageHandler.MessageType.ERROR);
            case STOPPED -> {
            }
        }
    }

    private void onServerMessage(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        MessageHandler.MessageType type = message.contains("消耗") || message.contains("流量")
                ? MessageHandler.MessageType.WARNING
                : MessageHandler.MessageType.INFO;
        messageHandler.send(message, type);
    }

    private void onError(String message, Throwable cause) {
        String detail = cause == null || cause.getMessage() == null ? message : message + " " + cause.getMessage();
        messageHandler.send(detail, MessageHandler.MessageType.ERROR);
        if (cause instanceof IOException) {
            messageHandler.log(detail, MessageHandler.LogLevel.ERROR);
        } else if (cause != null) {
            messageHandler.log(detail, MessageHandler.LogLevel.ERROR, cause);
        }
    }

    @Override
    public void close() {
        stop();
    }
}
