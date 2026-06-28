package neoproxy.neolinkmc;

import neoproxy.neolinkmc.service.ConnectionService;
import neoproxy.neolinkmc.service.MinecraftMessageHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.server.IntegratedServer;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Fabric entrypoint for 26.1; loader-specific code is intentionally limited to
 * metadata, config path, and lifecycle event bridging.
 */
public final class NeoLinkMC implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = NeoLinkCore.MOD_ID;
    public static final Logger LOGGER = NeoLinkCore.LOGGER;
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
    public static final String VERSION = FabricLoader.getInstance()
            .getModContainer(MOD_ID)
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");

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

    @Override
    public void onInitialize() {
        NeoLinkCore.initialize(FabricLoader.getInstance().getConfigDir(), VERSION, new MinecraftMessageHandler());
        ServerLifecycleEvents.SERVER_STARTED.register(server -> NeoLinkCore.onServerStarted(server.getPort()));
        ServerLifecycleEvents.SERVER_STOPPING.register(server ->
                NeoLinkCore.onServerStopping(server instanceof IntegratedServer));
    }

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> NeoLinkCore.onClientStarted());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> NeoLinkCore.onClientStopping());

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (client.hasSingleplayerServer() || (client.getCurrentServer() != null && client.isLocalServer())) {
                NeoLinkCore.onLocalPlayDisconnect();
            }
        });

        LOGGER.info("NeoLinkMC Fabric 26.1 客户端初始化完成");
        LOGGER.debug("配置目录路径: {}", CONFIG_DIR);
    }
}
