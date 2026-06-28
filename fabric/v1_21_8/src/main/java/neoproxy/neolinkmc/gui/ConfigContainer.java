package neoproxy.neolinkmc.gui;

import neoproxy.neolinkmc.config.NeoLinkConfig;
import neoproxy.neolinkmc.util.UUIDFixer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 配置容器类 - 管理GUI配置状态
 * <p>
 * 核心职责：
 * 1. 存储GUI中的所有配置字段
 * 2. 从配置文件加载配置（打开GUI时）
 * 3. 保存配置到文件（点击开启时，不含key）
 * 4. 应用配置到Minecraft服务器
 * <p>
 * 注意：key永远不会被持久化存储
 */
public final class ConfigContainer {

    // 服务端连接配置
    public String key = "";
    public String remoteServer = NeoLinkConfig.DEFAULT_REMOTE_DOMAIN;
    public int localPort = NeoLinkConfig.DEFAULT_LOCAL_PORT;
    public int hookPort = NeoLinkConfig.DEFAULT_HOOK_PORT;
    public int hostConnectPort = NeoLinkConfig.DEFAULT_HOST_CONNECT_PORT;

    // 游戏设置
    public boolean pvpAllowed = NeoLinkConfig.DEFAULT_PVP_ALLOWED;
    public OnlineMode onlineMode = OnlineMode.OFFLINE_TRY_ONLINE_UUID_FIRST;
    public boolean allowCheats = NeoLinkConfig.DEFAULT_ALLOW_CHEATS;
    public int maxPlayers = NeoLinkConfig.DEFAULT_MAX_PLAYERS;
    public GameType gameType = NeoLinkConfig.DEFAULT_GAME_TYPE;

    /**
     * 从配置文件加载配置 - 启动流程：打开GUI时读取config.json
     */
    public void loadFromConfig() {
        NeoLinkConfig.load();
        this.remoteServer = NeoLinkConfig.getRemoteDomain();
        this.hookPort = NeoLinkConfig.getHookPort();
        this.hostConnectPort = NeoLinkConfig.getHostConnectPort();
        this.localPort = NeoLinkConfig.getLocalPort();
        this.gameType = NeoLinkConfig.getGameType();
        this.onlineMode = OnlineMode.valueOf(NeoLinkConfig.getOnlineMode());
        this.pvpAllowed = NeoLinkConfig.isPvpAllowed();
        this.allowCheats = NeoLinkConfig.isAllowCheats();
        this.maxPlayers = NeoLinkConfig.getMaxPlayers();
        // 密钥永远不读取，GUI上显示为空
        this.key = "";
    }

    /**
     * 应用配置到当前服务器
     */
    public void applyToCurrentServer(IntegratedServer server) {
        server.setDefaultGameType(this.gameType);
        applyAllowCheats(server.getPlayerList(), this.allowCheats);
        server.setUsesAuthentication(this.onlineMode.onlineModeEnabled);
        applyPvpAllowed(server, this.pvpAllowed);
        UUIDFixer.tryOnlineFirst = this.onlineMode.tryOnlineUUIDFirst;
    }

    private static void applyAllowCheats(PlayerList playerList, boolean allowCheats) {
        try {
            Method method = PlayerList.class.getMethod("setAllowCommandsForAllPlayers", boolean.class);
            method.invoke(playerList, allowCheats);
        } catch (NoSuchMethodException ignored) {
            // Minecraft 1.20.x does not expose this runtime mutator. The value is
            // still persisted and used by versions whose server API supports it.
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to apply allow-cheats setting.", e);
        }
    }

    private static void applyPvpAllowed(IntegratedServer server, boolean pvpAllowed) {
        try {
            Method method = IntegratedServer.class.getMethod("setPvpAllowed", boolean.class);
            method.invoke(server, pvpAllowed);
            return;
        } catch (NoSuchMethodException ignored) {
            // Minecraft 1.21.9+ moved PvP to a game rule. Keep this path
            // reflective so the shared 1.20-1.21 template still compiles.
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to apply PvP setting.", e);
        }

        try {
            Class<?> gameRulesClass = Class.forName("net.minecraft.world.level.gamerules.GameRules");
            Field pvpField = gameRulesClass.getField("PVP");
            Object pvpRuleKey = pvpField.get(null);
            Object gameRules = invokeNoArgMethod(server, "getGameRules");
            for (Method method : gameRules.getClass().getMethods()) {
                if (isGameRuleSetMethod(method, pvpRuleKey, server)) {
                    method.invoke(gameRules, pvpRuleKey, pvpAllowed, server);
                    return;
                }
            }
            throw new NoSuchMethodException("No compatible GameRules#set method found.");
        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException ignored) {
            // Older versions do not expose the PvP game rule. The saved value
            // remains available and versions with a runtime mutator apply it.
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to apply PvP game rule.", e);
        }
    }

    private static boolean isGameRuleSetMethod(Method method, Object pvpRuleKey, IntegratedServer server) {
        if (!method.getName().equals("set")) {
            return false;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        return parameterTypes.length == 3
                && parameterTypes[0].isAssignableFrom(pvpRuleKey.getClass())
                && parameterTypes[1] == boolean.class
                && parameterTypes[2].isAssignableFrom(server.getClass());
    }

    private static Object invokeNoArgMethod(Object target, String methodName)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }

    /**
     * 保存配置到config.json
     * 注意：key不会被保存
     */
    public void saveConfig() {
        NeoLinkConfig.setRemoteDomain(this.remoteServer);
        NeoLinkConfig.setHookPort(this.hookPort);
        NeoLinkConfig.setHostConnectPort(this.hostConnectPort);
        NeoLinkConfig.setLocalPort(this.localPort);
        NeoLinkConfig.setLocalDomain("localhost");
        NeoLinkConfig.setGameType(this.gameType);
        NeoLinkConfig.setOnlineMode(this.onlineMode.name());
        NeoLinkConfig.setPvpAllowed(this.pvpAllowed);
        NeoLinkConfig.setAllowCheats(this.allowCheats);
        NeoLinkConfig.setMaxPlayers(this.maxPlayers);
        NeoLinkConfig.save();
    }
}
