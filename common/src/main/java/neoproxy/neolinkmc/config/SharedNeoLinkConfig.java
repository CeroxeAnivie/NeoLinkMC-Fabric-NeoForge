package neoproxy.neolinkmc.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import neoproxy.neolinkmc.NeoLinkCore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Shared JSON configuration store for all loaders.
 *
 * <p>The file format uses primitive values so Fabric, Forge, NeoForge, and
 * version-specific modules can map their own Minecraft enum types at the edge.
 * This keeps defaults, validation, and persistence identical across every jar.</p>
 */
public final class SharedNeoLinkConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "neolinkmc.json";
    private static final String TEMPLATE_PATH = "/templates/config.json";

    public static final String DEFAULT_REMOTE_DOMAIN = "mc.p.ceroxe.fun";
    public static final String DEFAULT_LOCAL_DOMAIN = "localhost";
    public static final int DEFAULT_LOCAL_PORT = 25565;
    public static final int DEFAULT_HOOK_PORT = 9100;
    public static final int DEFAULT_HOST_CONNECT_PORT = 9101;
    public static final boolean DEFAULT_PVP_ALLOWED = true;
    public static final boolean DEFAULT_ALLOW_CHEATS = true;
    public static final int DEFAULT_MAX_PLAYERS = 8;
    public static final String DEFAULT_GAME_TYPE = "SURVIVAL";
    public static final String DEFAULT_ONLINE_MODE = "OFFLINE_TRY_ONLINE_UUID_FIRST";

    private static ConfigData configData = new ConfigData();
    private static Path configDir;
    private static Path configFile;

    private SharedNeoLinkConfig() {
    }

    public static void init(Path loaderConfigDir) {
        configDir = loaderConfigDir;
        configFile = loaderConfigDir.resolve(CONFIG_FILE_NAME);
        load();
    }

    public static void load() {
        ensureInitialized();

        if (Files.exists(configFile)) {
            try {
                String json = Files.readString(configFile);
                configData = GSON.fromJson(json, ConfigData.class);
                if (configData == null) {
                    configData = new ConfigData();
                }
            } catch (IOException e) {
                NeoLinkCore.LOGGER.error("加载配置文件失败", e);
                configData = new ConfigData();
            }
            return;
        }

        if (copyTemplateConfig()) {
            load();
            return;
        }

        configData = new ConfigData();
        save();
    }

    private static boolean copyTemplateConfig() {
        try (InputStream templateStream = SharedNeoLinkConfig.class.getResourceAsStream(TEMPLATE_PATH)) {
            if (templateStream == null) {
                NeoLinkCore.LOGGER.warn("找不到配置文件模板: {}", TEMPLATE_PATH);
                return false;
            }

            Files.createDirectories(configFile.getParent());
            Files.copy(templateStream, configFile, StandardCopyOption.REPLACE_EXISTING);
            NeoLinkCore.LOGGER.info("已从模板复制默认配置文件");
            return true;
        } catch (IOException e) {
            NeoLinkCore.LOGGER.error("复制配置文件模板失败", e);
            return false;
        }
    }

    public static void save() {
        ensureInitialized();

        try {
            Files.createDirectories(configFile.getParent());
            Files.writeString(configFile, GSON.toJson(configData));
        } catch (IOException e) {
            NeoLinkCore.LOGGER.error("保存配置文件失败", e);
        }
    }

    public static Path getConfigDir() {
        ensureInitialized();
        return configDir;
    }

    public static Path getModConfigDir() {
        ensureInitialized();
        Path dir = configDir.resolve("neolinkmc");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            NeoLinkCore.LOGGER.error("创建模组配置目录失败", e);
        }
        return dir;
    }

    public static String getRemoteDomain() {
        return textOrDefault(configData.remote_domain, DEFAULT_REMOTE_DOMAIN);
    }

    public static int getLocalPort() {
        return parseInt(configData.local_port, DEFAULT_LOCAL_PORT);
    }

    public static int getHookPort() {
        return parseInt(configData.host_hook_port, DEFAULT_HOOK_PORT);
    }

    public static int getHostConnectPort() {
        return parseInt(configData.host_connect_port, DEFAULT_HOST_CONNECT_PORT);
    }

    public static boolean isPvpAllowed() {
        return configData.pvp_allowed;
    }

    public static boolean isAllowCheats() {
        return configData.allow_cheats;
    }

    public static int getMaxPlayers() {
        return parseInt(configData.max_players, DEFAULT_MAX_PLAYERS);
    }

    public static String getGameTypeName() {
        return textOrDefault(configData.gamemode, DEFAULT_GAME_TYPE);
    }

    public static String getOnlineMode() {
        return textOrDefault(configData.onlinemode, DEFAULT_ONLINE_MODE);
    }

    public static void setRemoteDomain(String domain) {
        configData.remote_domain = domain;
    }

    public static void setLocalPort(int port) {
        configData.local_port = String.valueOf(port);
    }

    public static void setHookPort(int port) {
        configData.host_hook_port = String.valueOf(port);
    }

    public static void setHostConnectPort(int port) {
        configData.host_connect_port = String.valueOf(port);
    }

    public static void setPvpAllowed(boolean allowed) {
        configData.pvp_allowed = allowed;
    }

    public static void setAllowCheats(boolean allow) {
        configData.allow_cheats = allow;
    }

    public static void setMaxPlayers(int players) {
        configData.max_players = String.valueOf(players);
    }

    public static void setGameTypeName(String gameTypeName) {
        configData.gamemode = textOrDefault(gameTypeName, DEFAULT_GAME_TYPE);
    }

    public static void setOnlineMode(String mode) {
        configData.onlinemode = mode;
    }

    public static void setLocalDomain(String localDomain) {
        configData.local_domain = textOrDefault(localDomain, DEFAULT_LOCAL_DOMAIN);
    }

    private static void ensureInitialized() {
        if (configFile == null || configDir == null) {
            throw new IllegalStateException("SharedNeoLinkConfig.init(Path) must be called before using config values.");
        }
    }

    private static String textOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static int parseInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(String.valueOf(value).trim());
            return parsed >= 1 && parsed <= 65535 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static final class ConfigData {
        String remote_domain = DEFAULT_REMOTE_DOMAIN;
        String local_domain = DEFAULT_LOCAL_DOMAIN;
        String local_port = String.valueOf(DEFAULT_LOCAL_PORT);
        String host_hook_port = String.valueOf(DEFAULT_HOOK_PORT);
        String host_connect_port = String.valueOf(DEFAULT_HOST_CONNECT_PORT);
        String gamemode = DEFAULT_GAME_TYPE;
        String onlinemode = DEFAULT_ONLINE_MODE;
        boolean pvp_allowed = DEFAULT_PVP_ALLOWED;
        boolean allow_cheats = DEFAULT_ALLOW_CHEATS;
        String max_players = String.valueOf(DEFAULT_MAX_PLAYERS);
    }
}
