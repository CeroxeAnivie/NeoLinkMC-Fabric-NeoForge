package neoproxy.neolinkmc.gui;

import neoproxy.neolinkmc.NeoLinkMC;
import neoproxy.neolinkmc.config.ConnectionConfig;
import neoproxy.neolinkmc.config.NeoLinkConfig;
import neoproxy.neolinkmc.gui.LanScreenLayout.Positions;
import neoproxy.neolinkmc.service.ConnectionService;
import neoproxy.neolinkmc.service.MinecraftMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

import java.awt.Desktop;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * NeoLink 配置界面 - 26.1 版本
 * <p>
 * 界面布局：
 * - 顶部：标题
 * - 中部：游戏设置按钮网格 + 输入框
 * - 底部：操作按钮
 */
public class NeoLinkConfigScreen extends Screen {

    // ==================== 界面文本常量 ====================

    private static final Component TITLE = Component.translatable("neolink.gui.title");
    private static final Component ADVANCED_SETTINGS = Component.translatable("neolink.gui.advanced_settings");
    private static final Component OPEN_CONFIG_FOLDER = Component.translatable("neolink.gui.open_config_folder");
    private static final Component TUNNEL_SETTINGS = Component.translatable("neolink.gui.tunnel_settings");
    private static final Component PLAYER_SETTINGS = Component.translatable("neolink.gui.player_settings");
    private static final Component START_TUNNEL = Component.translatable("neolink.gui.start_tunnel");
    private static final Component CANCEL = Component.translatable("neolink.gui.cancel");
    private static final Component PORT_LABEL = Component.translatable("neolink.gui.port");
    private static final Component MAX_PLAYERS_LABEL = Component.translatable("neolink.gui.max_players");

    // ==================== 配置状态 ====================

    private final Screen parentScreen;
    private final ConfigContainer config;

    // ==================== GUI 组件 ====================

    private Button gameModeButton;
    private Button allowCheatsButton;
    private Button onlineModeButton;
    private Button allowPvpButton;
    private EditBox portEditBox;
    private EditBox maxPlayersEditBox;

    public NeoLinkConfigScreen(Screen parentScreen) {
        super(TITLE);
        this.parentScreen = parentScreen;
        this.config = new ConfigContainer();
        loadConfig();
    }

    private void loadConfig() {
        this.config.loadFromConfig();
    }

    @Override
    protected void init() {
        super.init();
        Positions layout = LanScreenLayout.calculate(this.width, this.height, preferredButtonTextWidth());

        addCenteredStringWidget(TUNNEL_SETTINGS, layout.centerX(), layout.titleY());
        addCenteredStringWidget(PLAYER_SETTINGS, layout.centerX(), layout.playerSettingsY());
        addStringWidget(PORT_LABEL, layout.leftColumnX(), layout.inputLabelY());
        addStringWidget(MAX_PLAYERS_LABEL, layout.rightColumnX(), layout.inputLabelY());

        addRenderableWidget(createButton(layout.leftColumnX(), layout.topRowY(), ADVANCED_SETTINGS, this::onAdvancedSettingsClick));
        addRenderableWidget(createButton(layout.rightColumnX(), layout.topRowY(), OPEN_CONFIG_FOLDER, this::onOpenConfigFolderClick));

        this.gameModeButton = addRenderableWidget(createButton(
                layout.leftColumnX(), layout.firstOptionRowY(), getGameModeDisplayText(), this::onGameModeClick));
        this.onlineModeButton = addRenderableWidget(createButton(
                layout.rightColumnX(), layout.firstOptionRowY(), getOnlineModeDisplayText(), this::onOnlineModeClick));
        this.allowCheatsButton = addRenderableWidget(createButton(
                layout.leftColumnX(), layout.secondOptionRowY(), getAllowCheatsDisplayText(), this::onAllowCheatsClick));
        this.allowPvpButton = addRenderableWidget(createButton(
                layout.rightColumnX(), layout.secondOptionRowY(), getAllowPvpDisplayText(), this::onAllowPvpClick));

        this.portEditBox = addRenderableWidget(createFilteredEditBox(layout.leftColumnX(), layout.inputRowY(),
                String.valueOf(this.config.localPort), this::isValidPortInput, 5));
        this.maxPlayersEditBox = addRenderableWidget(createFilteredEditBox(layout.rightColumnX(), layout.inputRowY(),
                String.valueOf(this.config.maxPlayers), this::isValidMaxPlayersInput, 3));

        addRenderableWidget(createButton(layout.leftColumnX(), layout.bottomRowY(), START_TUNNEL, this::onStartTunnelClick));
        addRenderableWidget(createButton(layout.rightColumnX(), layout.bottomRowY(), CANCEL, this::onCancelClick));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    // ==================== 组件创建辅助方法 ====================

    private Button createButton(int x, int y, Component message, Consumer<Button> onPress) {
        return Button.builder(message, onPress::accept)
                .pos(x, y)
                .size(LanScreenLayout.calculate(this.width, this.height, preferredButtonTextWidth()).buttonWidth(),
                        LanScreenLayout.calculate(this.width, this.height, preferredButtonTextWidth()).buttonHeight())
                .build();
    }

    private void addCenteredStringWidget(Component message, int centerX, int y) {
        StringWidget widget = new StringWidget(message, this.font);
        widget.setPosition(centerX - this.font.width(message) / 2, y);
        addRenderableWidget(widget);
    }

    private void addStringWidget(Component message, int x, int y) {
        StringWidget widget = new StringWidget(message, this.font);
        widget.setPosition(x, y);
        addRenderableWidget(widget);
    }

    private EditBox createEditBox(int x, int y, String initial) {
        EditBox editBox = new EditBox(
                this.font,
                x, y,
                LanScreenLayout.calculate(this.width, this.height, preferredButtonTextWidth()).inputWidth(),
                LanScreenLayout.calculate(this.width, this.height, preferredButtonTextWidth()).buttonHeight(),
                Component.literal("")
        );
        editBox.setValue(initial);
        return editBox;
    }

    private EditBox createFilteredEditBox(int x, int y, String initial,
                                          Predicate<String> filter, int maxLength) {
        EditBox editBox = createEditBox(x, y, initial);
        editBox.setResponder((value) -> {
            if (!filter.test(value)) {
                String validValue = value.isEmpty() ? "" : value.replaceAll("[^0-9]", "");
                if (!validValue.equals(value)) {
                    editBox.setValue(validValue);
                }
            }
        });
        editBox.setMaxLength(maxLength);
        return editBox;
    }

    // ==================== 按钮文本生成 ====================

    private int preferredButtonTextWidth() {
        int maxWidth = 0;
        maxWidth = Math.max(maxWidth, this.font.width(ADVANCED_SETTINGS));
        maxWidth = Math.max(maxWidth, this.font.width(OPEN_CONFIG_FOLDER));
        maxWidth = Math.max(maxWidth, this.font.width(getGameModeDisplayText()));
        maxWidth = Math.max(maxWidth, this.font.width(getOnlineModeDisplayText()));
        maxWidth = Math.max(maxWidth, this.font.width(getAllowCheatsDisplayText()));
        maxWidth = Math.max(maxWidth, this.font.width(getAllowPvpDisplayText()));
        maxWidth = Math.max(maxWidth, this.font.width(START_TUNNEL));
        maxWidth = Math.max(maxWidth, this.font.width(CANCEL));
        return maxWidth;
    }

    private Component getGameModeDisplayText() {
        String modeName = switch (this.config.gameType) {
            case SURVIVAL -> Component.translatable("neolink.gui.gamemode.survival").getString();
            case CREATIVE -> Component.translatable("neolink.gui.gamemode.creative").getString();
            case ADVENTURE -> Component.translatable("neolink.gui.gamemode.adventure").getString();
            case SPECTATOR -> Component.translatable("neolink.gui.gamemode.spectator").getString();
        };
        return Component.literal("游戏模式：" + modeName);
    }

    private Component getAllowCheatsDisplayText() {
        String status = this.config.allowCheats
                ? Component.translatable("neolink.gui.status.on").getString()
                : Component.translatable("neolink.gui.status.off").getString();
        return Component.literal("允许作弊：" + status);
    }

    private Component getOnlineModeDisplayText() {
        String modeName = switch (this.config.onlineMode) {
            case ONLINE_ONLINE_UUID_ONLY -> Component.translatable("neolink.gui.online_mode.online").getString();
            case OFFLINE_TRY_ONLINE_UUID_FIRST -> Component.translatable("neolink.gui.online_mode.offline_fixed").getString();
            case OFFLINE_OFFLINE_UUID_ONLY -> Component.translatable("neolink.gui.online_mode.offline_vanilla").getString();
        };
        return Component.literal(modeName);
    }

    private Component getAllowPvpDisplayText() {
        String status = this.config.pvpAllowed
                ? Component.translatable("neolink.gui.status.on").getString()
                : Component.translatable("neolink.gui.status.off").getString();
        return Component.literal("允许 PVP：" + status);
    }

    // ==================== 输入验证 ====================

    private boolean isValidPortInput(String input) {
        if (input.isEmpty()) return true;
        try {
            int port = Integer.parseInt(input);
            return port >= 1 && port <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidMaxPlayersInput(String input) {
        if (input.isEmpty()) return true;
        try {
            int players = Integer.parseInt(input);
            return players >= 1 && players <= 1000;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ==================== 事件处理 ====================

    private void onAdvancedSettingsClick(Button button) {
        Minecraft.getInstance().setScreen(new NeoLinkAdvancedSettingsScreen(this, this.config, advancedConfig -> {
            this.config.remoteServer = advancedConfig.remoteServer();
            this.config.hookPort = advancedConfig.hookPort();
            this.config.hostConnectPort = advancedConfig.hostConnectPort();
            this.config.key = advancedConfig.key();
            NeoLinkMC.LOGGER.debug("从高级设置界面接收到配置: remote={}, hookPort={}, hostConnectPort={}, key={}",
                    advancedConfig.remoteServer(), advancedConfig.hookPort(), advancedConfig.hostConnectPort(),
                    advancedConfig.key().isEmpty() ? "(空)" : "(已设置)");
        }));
    }

    private void onOpenConfigFolderClick(Button button) {
        boolean success = openFolder(NeoLinkConfig.getModConfigDir());
        if (!success) {
            NeoLinkMC.LOGGER.error("打开配置文件夹失败");
        }
    }

    private void onGameModeClick(Button button) {
        this.config.gameType = switch (this.config.gameType) {
            case SURVIVAL -> GameType.CREATIVE;
            case CREATIVE -> GameType.ADVENTURE;
            case ADVENTURE -> GameType.SPECTATOR;
            case SPECTATOR -> GameType.SURVIVAL;
        };
        this.gameModeButton.setMessage(getGameModeDisplayText());
    }

    private void onAllowCheatsClick(Button button) {
        this.config.allowCheats = !this.config.allowCheats;
        this.allowCheatsButton.setMessage(getAllowCheatsDisplayText());
    }

    private void onOnlineModeClick(Button button) {
        this.config.onlineMode = switch (this.config.onlineMode) {
            case ONLINE_ONLINE_UUID_ONLY -> OnlineMode.OFFLINE_TRY_ONLINE_UUID_FIRST;
            case OFFLINE_TRY_ONLINE_UUID_FIRST -> OnlineMode.OFFLINE_OFFLINE_UUID_ONLY;
            case OFFLINE_OFFLINE_UUID_ONLY -> OnlineMode.ONLINE_ONLINE_UUID_ONLY;
        };
        this.onlineModeButton.setMessage(getOnlineModeDisplayText());
    }

    private void onAllowPvpClick(Button button) {
        this.config.pvpAllowed = !this.config.pvpAllowed;
        this.allowPvpButton.setMessage(getAllowPvpDisplayText());
    }

    private void onStartTunnelClick(Button button) {
        // 保存配置
        saveConfig();

        // 获取密钥
        String key = this.config.key;
        if (key == null || key.trim().isEmpty()) {
            key = "Free";
        }

        // 开启 LAN
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) {
            NeoLinkMC.LOGGER.error("无法获取单人游戏服务器实例");
            return;
        }

        this.config.applyToCurrentServer(server);
        int suggestedPort = parsePortInput();
        server.publishServer(this.config.gameType, this.config.allowCheats, suggestedPort);
        int actualPort = server.getPort();
        NeoLinkMC.LOGGER.info("LAN 已开启，建议端口: {}, 实际端口: {}", suggestedPort, actualPort);

        ConnectionService connectionService = new ConnectionService(new MinecraftMessageHandler());
        connectionService.start(new ConnectionConfig(
                this.config.remoteServer,
                ConnectionConfig.DEFAULT_LOCAL_DOMAIN,
                this.config.hookPort,
                this.config.hostConnectPort,
                key,
                actualPort
        ));
        NeoLinkMC.updateConnectionService(connectionService);

        goBack();
    }

    private void onCancelClick(Button button) {
        goBack();
    }

    // ==================== 辅助方法 ====================

    private int parsePortInput() {
        try {
            String value = this.portEditBox.getValue();
            if (value.isEmpty()) {
                return this.config.localPort;
            }
            int port = Integer.parseInt(value);
            return Math.max(1, Math.min(65535, port));
        } catch (NumberFormatException e) {
            return this.config.localPort;
        }
    }

    private int parseMaxPlayersInput() {
        try {
            String value = this.maxPlayersEditBox.getValue();
            if (value.isEmpty()) {
                return this.config.maxPlayers;
            }
            int players = Integer.parseInt(value);
            return Math.max(1, Math.min(1000, players));
        } catch (NumberFormatException e) {
            return this.config.maxPlayers;
        }
    }

    private void saveConfig() {
        this.config.localPort = parsePortInput();
        this.config.maxPlayers = parseMaxPlayersInput();
        this.config.saveConfig();
    }

    private void goBack() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    private boolean openFolder(Path path) {
        try {
            Desktop.getDesktop().open(path.toFile());
            return true;
        } catch (Exception e) {
            NeoLinkMC.LOGGER.error("打开文件夹失败: {}", path, e);
            return false;
        }
    }
}
