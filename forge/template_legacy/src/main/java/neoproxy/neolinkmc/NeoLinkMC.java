package neoproxy.neolinkmc;

import neoproxy.neolinkmc.service.ConnectionService;
import neoproxy.neolinkmc.service.MinecraftMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Forge loader 桥接层。
 *
 * <p>Forge 专属代码只保留在这里：metadata 查询、配置目录发现以及 event bus 注册。
 * Tunnel 生命周期和校验逻辑由 {@link NeoLinkCore} 统一承载，避免 Forge、NeoForge
 * 与 Fabric 之间出现行为漂移。</p>
 */
@Mod(NeoLinkCore.MOD_ID)
@Mod.EventBusSubscriber(modid = NeoLinkCore.MOD_ID, value = Dist.CLIENT)
public final class NeoLinkMC {
    public static final String MOD_ID = NeoLinkCore.MOD_ID;
    public static final Logger LOGGER = NeoLinkCore.LOGGER;
    public static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve(MOD_ID);
    public static final String VERSION = ModList.get()
            .getModContainerById(MOD_ID)
            .map(container -> container.getModInfo().getVersion().toString())
            .orElse("unknown");

    public NeoLinkMC() {
        NeoLinkCore.initialize(FMLPaths.CONFIGDIR.get(), VERSION, new MinecraftMessageHandler());
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("NeoLinkMC Forge 客户端初始化完成");
        LOGGER.debug("配置目录路径: {}", CONFIG_DIR);
    }

    public static void startService() {
        NeoLinkCore.startService();
    }

    public static void startService(String key) {
        NeoLinkCore.startService(key);
    }

    public static void startService(String key, int port) {
        NeoLinkCore.startService(key, port);
    }

    public static void stopService() {
        NeoLinkCore.stopService();
    }

    public static ConnectionService getConnectionService() {
        return NeoLinkCore.getConnectionService();
    }

    public static boolean isRunning() {
        return NeoLinkCore.isRunning();
    }

    public static void updateLocalPort(int port) {
        NeoLinkCore.updateLocalPort(port);
    }

    public static void updateConnectionService(ConnectionService service) {
        NeoLinkCore.updateConnectionService(service);
    }

    @Deprecated
    public static String getVersion() {
        return VERSION;
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        NeoLinkCore.onServerStarted(event.getServer().getPort());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        NeoLinkCore.onServerStopping(event.getServer() instanceof IntegratedServer);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Minecraft client = Minecraft.getInstance();
        if (client.hasSingleplayerServer() || (client.getCurrentServer() != null && client.isLocalServer())) {
            NeoLinkCore.onLocalPlayDisconnect();
        }
    }
}
