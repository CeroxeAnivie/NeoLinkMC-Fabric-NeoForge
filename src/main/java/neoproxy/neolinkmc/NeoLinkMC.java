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
 * @version 0.0.1
 */
public class NeoLinkMC implements ModInitializer, ClientModInitializer {

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
     * <p>
     * 流程说明：
     * 1. 此方法在密钥验证通过后、LAN开启后被调用
     * 2. 传入的端口是实际LAN端口，不是建议端口
     * 3. 服务启动后会使用此端口连接本地Minecraft服务器
     *
     * @param key  密钥（已验证通过，空字符串会被替换为"Free"）
     * @param port 实际LAN端口，-1表示使用配置文件中的端口
     */
    public static void startService(String key, int port) {
        LOGGER.info("[DEBUG] startService(key, port) 方法开始执行");
        LOGGER.info("[DEBUG] 传入的密钥: {}", key != null ? "[已设置]" : "[null/使用默认]");
        LOGGER.info("[DEBUG] 传入的端口: {}", port);
        try {
            if (connectionService == null) {
                LOGGER.info("[DEBUG] ConnectionService 实例为null，准备创建新实例");
                connectionService = new ConnectionService();
                LOGGER.info("[DEBUG] ConnectionService 实例创建成功，准备启动服务...");

                // 如果传入了有效端口，先更新配置
                if (port > 0) {
                    connectionService.localPort = port;
                    LOGGER.info("[DEBUG] 使用实际LAN端口: {}", port);
                }

                connectionService.start(key);
                LOGGER.info("NeoLink 核心服务启动成功");
                LOGGER.info("[DEBUG] ConnectionService 启动成功，服务运行状态: {}", connectionService.isRunning());
            } else {
                LOGGER.info("[DEBUG] ConnectionService 实例已存在，跳过创建");
            }
        } catch (Exception e) {
            LOGGER.error("NeoLink 核心服务启动失败", e);
            LOGGER.info("[DEBUG] 启动服务异常: {} - {}", e.getClass().getName(), e.getMessage());
        }
        LOGGER.info("[DEBUG] startService(key, port) 方法执行完毕");
    }

    public static void stopService() {
        LOGGER.info("[DEBUG] stopService() 方法开始执行");
        try {
            if (connectionService != null) {
                LOGGER.info("[DEBUG] ConnectionService 实例存在，准备停止服务...");
                connectionService.stop();
                LOGGER.info("[DEBUG] ConnectionService 已停止，清理实例引用");
                connectionService = null;
                LOGGER.info("NeoLink 核心服务已停止");
            } else {
                LOGGER.info("[DEBUG] ConnectionService 实例为null，无需停止");
            }
        } catch (Exception e) {
            LOGGER.error("NeoLink 核心服务停止时出错", e);
            LOGGER.info("[DEBUG] 停止服务异常: {} - {}", e.getClass().getName(), e.getMessage());
        }
        LOGGER.info("[DEBUG] stopService() 方法执行完毕");
    }

    public static ConnectionService getConnectionService() {
        LOGGER.info("[DEBUG] getConnectionService() 被调用，当前实例: {}", connectionService);
        return connectionService;
    }

    public static boolean isRunning() {
        return connectionService != null && connectionService.isRunning();
    }

    public static void updateLocalPort(int port) {
        LOGGER.info("[DEBUG] updateLocalPort() 被调用，新端口: {}", port);
        if (connectionService != null) {
            connectionService.localPort = port;
            LOGGER.info("NeoLinkMC 本地端口已更新为: {}", port);
            LOGGER.info("[DEBUG] ConnectionService.localPort 已更新为: {}", port);
        } else {
            LOGGER.warn("[DEBUG] ConnectionService 为null，无法更新端口");
        }
    }

    /**
     * 获取 Mod 版本号
     *
     * @return 版本号字符串
     */
    public static String getVersion() {
        return FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    @Override
    public void onInitialize() {
        LOGGER.info("╔════════════════════════════════════════════════════════╗");
        LOGGER.info("║         NeoLinkMC Mod 正在初始化...                    ║");
        LOGGER.info("╚════════════════════════════════════════════════════════╝");
        LOGGER.info("[DEBUG] onInitialize() 方法开始执行");

        FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(container -> {
            String version = container.getMetadata().getVersion().getFriendlyString();
            LOGGER.info("Mod 版本: {}", version);
            LOGGER.info("[DEBUG] 从 Fabric Mod 容器读取版本: {}", version);
        });

        LOGGER.info("[DEBUG] 准备初始化配置系统...");
        initializeConfig();
        LOGGER.info("[DEBUG] 配置系统初始化完成，准备注册生命周期事件...");
        registerLifecycleEvents();

        LOGGER.info("NeoLinkMC Mod 初始化完成");
        LOGGER.info("[DEBUG] onInitialize() 方法执行完毕");
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("NeoLinkMC 客户端初始化");
        LOGGER.info("[DEBUG] onInitializeClient() 方法开始执行");
        LOGGER.info("[DEBUG] 配置目录路径: {}", CONFIG_DIR);
        LOGGER.info("[DEBUG] 客户端特定初始化逻辑执行完毕");
    }

    private void initializeConfig() {
        LOGGER.info("[DEBUG] initializeConfig() 方法开始执行");
        try {
            if (!CONFIG_DIR.toFile().exists()) {
                LOGGER.info("[DEBUG] 配置目录不存在，准备创建: {}", CONFIG_DIR);
                CONFIG_DIR.toFile().mkdirs();
                LOGGER.info("创建配置目录: {}", CONFIG_DIR);
                LOGGER.info("[DEBUG] 配置目录创建成功");
            } else {
                LOGGER.info("[DEBUG] 配置目录已存在: {}", CONFIG_DIR);
            }
            String minecraftConfigDir = FabricLoader.getInstance().getConfigDir().toString();
            LOGGER.info("[DEBUG] 调用 ConfigManager.init()，Minecraft配置目录: {}", minecraftConfigDir);
            ConfigManager.init(minecraftConfigDir);
            LOGGER.info("配置系统初始化完成");
            LOGGER.info("[DEBUG] ConfigManager 初始化成功");
        } catch (Exception e) {
            LOGGER.error("配置系统初始化失败", e);
            LOGGER.info("[DEBUG] 配置系统初始化异常: {}", e.getMessage());
        }
        LOGGER.info("[DEBUG] initializeConfig() 方法执行完毕");
    }

    private void registerLifecycleEvents() {
        LOGGER.info("[DEBUG] registerLifecycleEvents() 方法开始执行");
        LOGGER.info("[DEBUG] 注册 CLIENT_STARTED 事件监听器");
        ClientLifecycleEvents.CLIENT_STARTED.register(this::onClientStarted);
        LOGGER.info("[DEBUG] 注册 CLIENT_STOPPING 事件监听器");
        ClientLifecycleEvents.CLIENT_STOPPING.register(this::onClientStopping);
        LOGGER.info("[DEBUG] 注册 SERVER_STARTED 事件监听器");
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        LOGGER.info("[DEBUG] 注册 SERVER_STOPPING 事件监听器");
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
        LOGGER.info("[DEBUG] 注册 DISCONNECT 事件监听器（仅房主退出时停止服务）");
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (client.hasSingleplayerServer() || (client.getCurrentServer() != null && client.isLocalServer())) {
                LOGGER.info("[NeoLinkMC] 房主断开连接，准备停止服务...");
                stopService();
            } else {
                LOGGER.info("[DEBUG] 普通玩家断开连接，不停止服务（不是房主）");
            }
        });
        LOGGER.info("[DEBUG] 注册 CLIENT_TICK 事件监听器（检测回到主界面时停止服务）");
        final java.util.concurrent.atomic.AtomicInteger tickCounter = new java.util.concurrent.atomic.AtomicInteger(0);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.screen != null) {
                String screenClass = client.screen.getClass().getSimpleName();
                boolean isTitleScreen = client.screen instanceof TitleScreen;
                boolean serviceRunning = isRunning();

                // 每40 ticks（2秒）记录一次，避免日志过多
                if (tickCounter.incrementAndGet() % 40 == 0) {
                    LOGGER.info("[DEBUG] Tick检测 - 当前屏幕: {}, 是主界面: {}, 服务运行中: {}",
                            screenClass, isTitleScreen, serviceRunning);
                }

                if (isTitleScreen && serviceRunning) {
                    LOGGER.info("[NeoLinkMC] 检测到回到主界面，停止服务...");
                    stopService();
                }
            }
        });
        LOGGER.info("生命周期事件监听器注册完成");
        LOGGER.info("[DEBUG] registerLifecycleEvents() 方法执行完毕");
    }

    private void onClientStarted(Minecraft client) {
        LOGGER.info("Minecraft 客户端启动完成");
        LOGGER.info("[DEBUG] onClientStarted() 回调触发");
        LOGGER.info("[DEBUG] Minecraft 客户端实例: {}", client);
        // 注意：服务不再在客户端启动时自动开启
        // 服务将在用户点击"开启内网穿透"按钮后启动
        LOGGER.info("[DEBUG] 服务将在点击\"开启内网穿透\"后启动");
        LOGGER.info("[DEBUG] onClientStarted() 回调处理完毕");
    }

    private void onClientStopping(Minecraft client) {
        LOGGER.info("Minecraft 客户端正在停止，关闭 NeoLink 服务");
        LOGGER.info("[DEBUG] onClientStopping() 回调触发");
        LOGGER.info("[DEBUG] 正在停止客户端，准备关闭服务...");
        stopService();
        LOGGER.info("[DEBUG] onClientStopping() 回调处理完毕");
    }

    private void onServerStarted(MinecraftServer server) {
        LOGGER.info("Minecraft 服务器启动完成");
        LOGGER.info("[DEBUG] onServerStarted() 回调触发");
        LOGGER.info("[DEBUG] 服务器端口: {}", server.getPort());
        LOGGER.info("[DEBUG] 服务器是否开放: {}", server.isPublished());
    }

    private void onServerStopping(MinecraftServer server) {
        LOGGER.info("Minecraft 服务器正在停止");
        LOGGER.info("[DEBUG] onServerStopping() 回调触发");

        if (server instanceof net.minecraft.client.server.IntegratedServer) {
            LOGGER.info("[NeoLinkMC] 单人游戏服务器停止，停止 NeoLinkMC 服务...");
            stopService();
        } else {
            LOGGER.info("[DEBUG] 多人游戏服务器停止，不停止 NeoLinkMC 服务");
        }

        LOGGER.info("[DEBUG] onServerStopping() 回调处理完毕");
    }
}
