package neoproxy.neolinkmc.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import neoproxy.neolinkmc.NeoLinkMC;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.GameType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * NeoLink 配置管理类
 * <p>
 * 负责配置的加载、保存和管理。
 * 配置存储在 JSON 文件中，位于 Minecraft 配置目录下。
 * 首次加载时从 templates/config.json 模板复制默认配置。
 */
public final class NeoLinkConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "neolinkmc.json";
    private static final String TEMPLATE_PATH = "/templates/config.json";

    // 默认值（与模板一致）
    public static final String DEFAULT_REMOTE_DOMAIN = "mc.p.ceroxe.fun";
    public static final int DEFAULT_LOCAL_PORT = 25565;
    public static final int DEFAULT_HOOK_PORT = 9100;
    public static final int DEFAULT_HOST_CONNECT_PORT = 9101;
    public static final boolean DEFAULT_PVP_ALLOWED = true;
    public static final boolean DEFAULT_ALLOW_CHEATS = true;
    public static final int DEFAULT_MAX_PLAYERS = 8;
    public static final GameType DEFAULT_GAME_TYPE = GameType.SURVIVAL;
    public static final String DEFAULT_ONLINE_MODE = "OFFLINE_TRY_ONLINE_UUID_FIRST";

    private static ConfigData configData = new ConfigData();
    private static Path configFile;

    /**
     * 初始化配置系统
     */
    public static void init() {
        configFile = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
        load();
    }

    /**
     * 加载配置
     * 如果配置文件不存在，从模板复制默认配置
     */
    public static void load() {
        if (configFile == null) {
            configFile = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
        }

        if (Files.exists(configFile)) {
            // 配置文件存在，直接加载
            try {
                String json = Files.readString(configFile);
                configData = GSON.fromJson(json, ConfigData.class);
                if (configData == null) {
                    configData = new ConfigData();
                }
            } catch (IOException e) {
                NeoLinkMC.LOGGER.error("加载配置文件失败", e);
                configData = new ConfigData();
            }
        } else {
            // 配置文件不存在，从模板复制
            if (copyTemplateConfig()) {
                // 复制成功后重新加载
                try {
                    String json = Files.readString(configFile);
                    configData = GSON.fromJson(json, ConfigData.class);
                    if (configData == null) {
                        configData = new ConfigData();
                    }
                } catch (IOException e) {
                    NeoLinkMC.LOGGER.error("从模板加载配置文件失败", e);
                    configData = new ConfigData();
                }
            } else {
                // 模板复制失败，使用默认配置并保存
                configData = new ConfigData();
                save();
            }
        }
    }

    /**
     * 从模板复制默认配置文件
     *
     * @return 是否成功复制
     */
    private static boolean copyTemplateConfig() {
        try (InputStream templateStream = NeoLinkConfig.class.getResourceAsStream(TEMPLATE_PATH)) {
            if (templateStream == null) {
                NeoLinkMC.LOGGER.warn("找不到配置文件模板: {}", TEMPLATE_PATH);
                return false;
            }

            Files.createDirectories(configFile.getParent());
            Files.copy(templateStream, configFile, StandardCopyOption.REPLACE_EXISTING);
            NeoLinkMC.LOGGER.info("已从模板复制默认配置文件");
            return true;
        } catch (IOException e) {
            NeoLinkMC.LOGGER.error("复制配置文件模板失败", e);
            return false;
        }
    }

    /**
     * 保存配置
     */
    public static void save() {
        if (configFile == null) {
            configFile = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
        }

        try {
            Files.createDirectories(configFile.getParent());
            String json = GSON.toJson(configData);
            Files.writeString(configFile, json);
        } catch (IOException e) {
            NeoLinkMC.LOGGER.error("保存配置文件失败", e);
        }
    }

    /**
     * 获取配置目录
     */
    public static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    /**
     * 获取模组配置目录
     */
    public static Path getModConfigDir() {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve("neolinkmc");
        if (!dir.toFile().exists()) {
            dir.toFile().mkdirs();
        }
        return dir;
    }

    // ==================== Getter 方法 ====================

    public static String getRemoteDomain() {
        return configData.remote_domain != null ? configData.remote_domain : DEFAULT_REMOTE_DOMAIN;
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

    public static GameType getGameType() {
        if (configData.gamemode == null) {
            return DEFAULT_GAME_TYPE;
        }
        try {
            return GameType.valueOf(configData.gamemode);
        } catch (IllegalArgumentException e) {
            return DEFAULT_GAME_TYPE;
        }
    }

    public static String getOnlineMode() {
        return configData.onlinemode != null ? configData.onlinemode : DEFAULT_ONLINE_MODE;
    }

    // ==================== Setter 方法 ====================

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

    public static void setGameType(GameType gameType) {
        configData.gamemode = gameType.name();
    }

    public static void setOnlineMode(String mode) {
        configData.onlinemode = mode;
    }

    public static void setLocalDomain(String localDomain) {
        configData.local_domain = localDomain;
    }

    // ==================== 辅助方法 ====================

    private static int parseInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 配置数据类（与模板 JSON 结构对应）
     */
    private static class ConfigData {
        String remote_domain = DEFAULT_REMOTE_DOMAIN;
        String local_domain = "localhost";
        String local_port = String.valueOf(DEFAULT_LOCAL_PORT);
        String host_hook_port = String.valueOf(DEFAULT_HOOK_PORT);
        String host_connect_port = String.valueOf(DEFAULT_HOST_CONNECT_PORT);
        String gamemode = DEFAULT_GAME_TYPE.name();
        String onlinemode = DEFAULT_ONLINE_MODE;
        boolean pvp_allowed = DEFAULT_PVP_ALLOWED;
        boolean allow_cheats = DEFAULT_ALLOW_CHEATS;
        String max_players = String.valueOf(DEFAULT_MAX_PLAYERS);
    }
}
