package neoproxy.neolinkmc;

import neoproxy.neolinkmc.service.ConnectionService;
import neoproxy.neolinkmc.service.MinecraftMessageHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Fabric 26.x entrypoint.
 *
 * <p>26.x currently has no usable intermediary mapping set for this build, so this entrypoint
 * intentionally depends only on Fabric Loader. Keeping Fabric API lifecycle hooks out of this
 * template prevents 26.x's no-intermediate mapping mode from remapping unrelated Fabric API mods.</p>
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
        LOGGER.info("NeoLinkMC Fabric 26.x 初始化完成");
        LOGGER.debug("配置目录路径: {}", CONFIG_DIR);
    }

    @Override
    public void onInitializeClient() {
        NeoLinkCore.onClientStarted();
    }
}
