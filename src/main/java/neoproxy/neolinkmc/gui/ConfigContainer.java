package neoproxy.neolinkmc.gui;

import neoproxy.neolinkmc.config.ConfigManager;
import neoproxy.neolinkmc.util.UUIDFixer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.GameType;

public abstract class ConfigContainer {

    public String key; // 仅驻留内存，绝对不保存到本地
    public String remoteServer;
    public int localPort;
    public int hookPort;
    public int hostConnectPort;

    public boolean pvpAllowed;
    public OnlineMode onlineMode;
    public boolean allowCheats;
    public int maxPlayers;
    public GameType gameType;

    protected abstract void setGameType(GameType gameType);

    protected abstract void setCommandEnabled(boolean commandEnabled);

    protected abstract GameType getGuiGameType();

    protected abstract boolean getGuiCommandEnabled();

    protected abstract int getGuiPort();

    protected abstract void setGuiPort(int port);

    /**
     * 从当前服务器和配置文件加载配置
     * <p>
     * 【重要】调用时机：玩家点击"对局域网开放"按钮时
     * 此方法在NeoLinkMainScreen构造方法中被调用
     * <p>
     * 加载逻辑：
     * 1. 游戏设置（游戏模式、作弊、PVP等）从当前服务器读取
     * 2. 内网穿透设置（服务器地址、端口等）从config.json读取
     * 3. 密钥(key)永远不读取，保持为空字符串
     *
     * @param server 当前单人游戏服务器实例
     */
    public void loadFromCurrentServer(IntegratedServer server) {
        // 从服务器读取游戏设置
        this.setGameType(server.getForcedGameType());
        this.setCommandEnabled(server.getPlayerList().isAllowCommandsForAllPlayers());
        this.setGuiPort(server.getPort());

        boolean serverOnlineMode = server.usesAuthentication();
        this.onlineMode = OnlineMode.of(serverOnlineMode, !serverOnlineMode);
        this.pvpAllowed = server.isPvpAllowed();
        this.maxPlayers = server.getMaxPlayers();

        // 从config.json读取内网穿透设置
        // 【重要】此时读取配置文件，在GUI上显示供玩家查看和修改
        ConfigManager.loadConfig();
        this.remoteServer = ConfigManager.getString("remote_domain", "p.ceroxe.fun");
        this.localPort = server.getPort(); // 强制对齐当前服务器端口
        this.hookPort = Integer.parseInt(ConfigManager.getString("host_hook_port", "44801"));
        this.hostConnectPort = Integer.parseInt(ConfigManager.getString("host_connect_port", "44802"));
        this.allowCheats = ConfigManager.getBoolean("allow_cheats", true);

        // 【重要】密钥永远不读取，GUI上显示为空
        // 玩家需要每次手动输入，或保持为空使用默认"Free"
        this.key = "";
    }

    public void applyToCurrentServer(IntegratedServer server) {
        server.setDefaultGameType(this.getGuiGameType());
        server.getPlayerList().setAllowCommandsForAllPlayers(this.getGuiCommandEnabled());
        server.setUsesAuthentication(this.onlineMode.onlineModeEnabled);
        server.setPvpAllowed(this.pvpAllowed);
        UUIDFixer.tryOnlineFirst = this.onlineMode.tryOnlineUUIDFirst;
    }

    // 保存配置到唯一的 config.json
    public void saveConfig() {
        ConfigManager.setString("remote_domain", this.remoteServer);
        ConfigManager.setString("host_hook_port", String.valueOf(this.hookPort));
        ConfigManager.setString("host_connect_port", String.valueOf(this.hostConnectPort));
        ConfigManager.setString("local_port", String.valueOf(this.localPort));
        ConfigManager.setString("local_domain", "localhost");
        ConfigManager.setString("gamemode", this.getGuiGameType().name());
        ConfigManager.setString("onlinemode", this.onlineMode.name());
        ConfigManager.setBoolean("pvp_allowed", this.pvpAllowed);
        ConfigManager.setBoolean("allow_cheats", this.allowCheats);
        ConfigManager.setInt("max_players", this.maxPlayers);
        // 绝对不要保存 Key！
        ConfigManager.saveConfig();
    }

    public static class Modded extends ConfigContainer {
        private GameType gameMode;
        private boolean commands;
        private int listeningPort;

        @Override
        protected void setGameType(GameType gameType) {
            this.gameMode = gameType;
        }

        @Override
        protected void setCommandEnabled(boolean commandEnabled) {
            this.commands = commandEnabled;
        }

        @Override
        protected GameType getGuiGameType() {
            return this.gameMode;
        }

        @Override
        protected boolean getGuiCommandEnabled() {
            return this.commands;
        }

        @Override
        protected int getGuiPort() {
            return this.listeningPort;
        }

        @Override
        protected void setGuiPort(int port) {
            this.listeningPort = port;
        }
    }

    /**
     * 独立模式 - 用于 Cloth Config 完全接管界面
     * 不依赖原版 ShareToLanScreen
     */
    public static class Standalone extends ConfigContainer {
        private GameType gameMode = GameType.SURVIVAL;
        private boolean commands = true;
        private int listeningPort = 12222;

        @Override
        protected void setGameType(GameType gameType) {
            this.gameMode = gameType;
        }

        @Override
        protected void setCommandEnabled(boolean commandEnabled) {
            this.commands = commandEnabled;
        }

        @Override
        protected GameType getGuiGameType() {
            return this.gameMode;
        }

        @Override
        protected boolean getGuiCommandEnabled() {
            return this.commands;
        }

        @Override
        protected int getGuiPort() {
            return this.listeningPort;
        }

        @Override
        protected void setGuiPort(int port) {
            this.listeningPort = port;
        }
    }
}