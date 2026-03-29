package neoproxy.neolinkmc;

import neoproxy.neolinkmc.config.ConfigManager;
import neoproxy.neolinkmc.service.ConnectionService;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NeoLinkMC Mod 主入口类
 * <p>
 * Fabric Mod 的标准入口点，负责：
 * 1. Mod 初始化和生命周期管理
 * 2. 配置目录初始化
 * 3. Minecraft 事件监听注册
 * 4. 核心服务管理
 *
 * @author NeoProxy Team
 * @version 1.0.0
 */
public final class NeoLinkMC implements ModInitializer, ClientModInitializer {

    public static final String MOD_ID = "neolinkmc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);

    private static ConnectionService connectionService;

    public static void startService() {
        startService(null, -1);
    }

    public static void startService(String key) {
        startService(key, -1);
    }

    /**
     * 启动 NeoLinkMC 服务
     *
     * @param key  密钥（空值会使用默认密钥"Free"）
     * @param port 实际LAN端口，-1表示使用配置文件中的端口
     */
    public static void startService(String key, int port) {
        LOGGER.debug("startService() called with port: {}", port);
        try {
            if (connectionService == null) {
                connectionService = new ConnectionService();
                if (port > 0) {
                    connectionService.setLocalPort(port);
                }
                connectionService.start(key);
                LOGGER.info("NeoLink 核心服务启动成功");
            }
        } catch (Exception e) {
            LOGGER.error("NeoLink 核心服务启动失败", e);
        }
    }

    public static void stopService() {
        LOGGER.debug("stopService() called");
        try {
            if (connectionService != null) {
                connectionService.stop();
                connectionService = null;
                LOGGER.info("NeoLink 核心服务已停止");
            }
        } catch (Exception e) {
            LOGGER.error("NeoLink 核心服务停止时出错", e);
        }
    }

    public static ConnectionService getConnectionService() {
        return connectionService;
    }

    public static boolean isRunning() {
        return connectionService != null && connectionService.isRunning();
    }

    public static void updateLocalPort(int port) {
        LOGGER.debug("updateLocalPort() called with port: {}", port);
        if (connectionService != null) {
            connectionService.setLocalPort(port);
            LOGGER.info("NeoLinkMC 本地端口已更新为: {}", port);
        }
    }

    /**
     * 版本号，从 Fabric Mod 元数据读取
     */
    public static final String VERSION = FabricLoader.getInstance()
            .getModContainer(MOD_ID)
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");

    /**
     * 获取版本号（兼容旧代码）
     * @deprecated 直接使用 VERSION 常量
     */
    @Deprecated
    public static String getVersion() {
        return VERSION;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("╔════════════════════════════════════════════════════════╗");
        LOGGER.info("║         NeoLinkMC Mod 正在初始化...                    ║");
        LOGGER.info("╚════════════════════════════════════════════════════════╝");

        FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(container -> {
            String version = container.getMetadata().getVersion().getFriendlyString();
            LOGGER.info("Mod 版本: {}", version);
        });

        initializeConfig();
        registerLifecycleEvents();

        LOGGER.info("NeoLinkMC Mod 初始化完成");
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("NeoLinkMC 客户端初始化完成");
        LOGGER.debug("配置目录路径: {}", CONFIG_DIR);
    }

    private void initializeConfig() {
        try {
            if (!CONFIG_DIR.toFile().exists()) {
                CONFIG_DIR.toFile().mkdirs();
                LOGGER.info("创建配置目录: {}", CONFIG_DIR);
            }
            String minecraftConfigDir = FabricLoader.getInstance().getConfigDir().toString();
            ConfigManager.init(minecraftConfigDir);
            LOGGER.info("配置系统初始化完成");
        } catch (Exception e) {
            LOGGER.error("配置系统初始化失败", e);
        }
    }

    private void registerLifecycleEvents() {
        ClientLifecycleEvents.CLIENT_STARTED.register(this::onClientStarted);
        ClientLifecycleEvents.CLIENT_STOPPING.register(this::onClientStopping);
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (client.hasSingleplayerServer() || (client.getCurrentServer() != null && client.isLocalServer())) {
                LOGGER.info("[NeoLinkMC] 房主断开连接，停止服务...");
                stopService();
            }
        });

        final AtomicInteger tickCounter = new AtomicInteger(0);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.screen != null) {
                boolean isTitleScreen = client.screen instanceof TitleScreen;
                boolean serviceRunning = isRunning();

                if (tickCounter.incrementAndGet() % 40 == 0) {
                    LOGGER.debug("Tick检测 - 屏幕: {}, 服务运行: {}",
                            client.screen.getClass().getSimpleName(), serviceRunning);
                }

                if (isTitleScreen && serviceRunning) {
                    LOGGER.info("[NeoLinkMC] 检测到回到主界面，停止服务...");
                    stopService();
                }
            }
        });

        LOGGER.info("生命周期事件监听器注册完成");
    }

    private void onClientStarted(Minecraft client) {
        LOGGER.info("Minecraft 客户端启动完成");
    }

    private void onClientStopping(Minecraft client) {
        LOGGER.info("Minecraft 客户端正在停止，关闭 NeoLink 服务");
        stopService();
    }

    private void onServerStarted(MinecraftServer server) {
        LOGGER.info("Minecraft 服务器启动完成，端口: {}", server.getPort());
    }

    private void onServerStopping(MinecraftServer server) {
        LOGGER.info("Minecraft 服务器正在停止");
        if (server instanceof net.minecraft.client.server.IntegratedServer) {
            LOGGER.info("[NeoLinkMC] 单人游戏服务器停止，停止服务...");
            stopService();
        }
    }
}
