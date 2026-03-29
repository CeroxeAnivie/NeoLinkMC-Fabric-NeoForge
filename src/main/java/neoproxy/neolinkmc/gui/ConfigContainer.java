package neoproxy.neolinkmc.gui;

import neoproxy.neolinkmc.config.ConfigManager;
import neoproxy.neolinkmc.util.UUIDFixer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.GameType;

/**
 * 配置容器类 - 简化版本
 * <p>
 * 直接管理所有配置字段，去除复杂的抽象类和子类结构
 * 通过数据驱动而非继承来实现灵活性
 *
 * @author NeoProxy Team
 * @version 2.0.0
 */
public final class ConfigContainer {

    // 服务端连接配置
    public String key = "";
    public String remoteServer = "p.ceroxe.fun";
    public int localPort = 25565;
    public int hookPort = 44801;
    public int hostConnectPort = 44802;

    // 游戏设置
    public boolean pvpAllowed = true;
    public OnlineMode onlineMode = OnlineMode.OFFLINE_TRY_ONLINE_UUID_FIRST;
    public boolean allowCheats = true;
    public int maxPlayers = 8;
    public GameType gameType = GameType.SURVIVAL;

    /**
     * 从当前服务器和配置文件加载配置
     *
     * @param server 当前单人游戏服务器实例，可为 null
     */
    public void loadFromCurrentServer(IntegratedServer server) {
        // 从服务器读取游戏设置
        if (server != null) {
            this.gameType = server.getForcedGameType();
            this.allowCheats = server.getPlayerList().isAllowCommandsForAllPlayers();
            this.localPort = server.getPort();

            boolean serverOnlineMode = server.usesAuthentication();
            this.onlineMode = OnlineMode.of(serverOnlineMode, !serverOnlineMode);
            this.pvpAllowed = server.isPvpAllowed();
            this.maxPlayers = server.getMaxPlayers();
        }

        // 从配置文件读取内网穿透设置
        ConfigManager.loadConfig();
        this.remoteServer = ConfigManager.getString("remote_domain", "p.ceroxe.fun");
        this.hookPort = Integer.parseInt(ConfigManager.getString("host_hook_port", "44801"));
        this.hostConnectPort = Integer.parseInt(ConfigManager.getString("host_connect_port", "44802"));

        // 密钥永远不读取，GUI 上显示为空
        this.key = "";
    }

    /**
     * 应用配置到当前服务器
     *
     * @param server 当前单人游戏服务器实例
     */
    public void applyToCurrentServer(IntegratedServer server) {
        server.setDefaultGameType(this.gameType);
        server.getPlayerList().setAllowCommandsForAllPlayers(this.allowCheats);
        server.setUsesAuthentication(this.onlineMode.onlineModeEnabled);
        server.setPvpAllowed(this.pvpAllowed);
        UUIDFixer.tryOnlineFirst = this.onlineMode.tryOnlineUUIDFirst;
    }

    /**
     * 保存配置到 config.json
     * 注意：key 不会被保存
     */
    public void saveConfig() {
        ConfigManager.setString("remote_domain", this.remoteServer);
        ConfigManager.setString("host_hook_port", String.valueOf(this.hookPort));
        ConfigManager.setString("host_connect_port", String.valueOf(this.hostConnectPort));
        ConfigManager.setString("local_port", String.valueOf(this.localPort));
        ConfigManager.setString("local_domain", "localhost");
        ConfigManager.setString("gamemode", this.gameType.name());
        ConfigManager.setString("onlinemode", this.onlineMode.name());
        ConfigManager.setBoolean("pvp_allowed", this.pvpAllowed);
        ConfigManager.setBoolean("allow_cheats", this.allowCheats);
        ConfigManager.setInt("max_players", this.maxPlayers);
        ConfigManager.saveConfig();
    }
}
