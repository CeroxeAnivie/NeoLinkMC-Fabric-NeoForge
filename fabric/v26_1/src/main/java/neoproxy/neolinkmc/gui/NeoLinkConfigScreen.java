package neoproxy.neolinkmc.gui;

import neoproxy.neolinkmc.NeoLinkMC;
import neoproxy.neolinkmc.config.ConnectionConfig;
import neoproxy.neolinkmc.config.NeoLinkConfig;
import neoproxy.neolinkmc.service.ConnectionService;
import neoproxy.neolinkmc.service.MinecraftMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

import java.awt.Desktop;
import java.nio.file.Path;
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
    private static final Component START_TUNNEL = Component.translatable("neolink.gui.start_tunnel");
    private static final Component CANCEL = Component.translatable("neolink.gui.cancel");
    private static final Component PORT_LABEL = Component.translatable("neolink.gui.port");
    private static final Component MAX_PLAYERS_LABEL = Component.translatable("neolink.gui.max_players");

    // ==================== 布局常量 ====================

    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int INPUT_WIDTH = 150;
    private static final int PADDING = 10;

    // ==================== 配置状态 ====================

    private final Screen parentScreen;
    private final ConfigContainer config;

    // ==================== GUI 组件 ====================

    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
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

        // 添加标题到头部
        this.layout.addToHeader(new StringWidget(this.title, this.font));

        // 创建内容区域 - 使用网格布局
        GridLayout gridLayout = new GridLayout();
        gridLayout.columnSpacing(8).rowSpacing(4);
        GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);

        // 游戏模式选择
        rowHelper.addChild(CycleButton.builder(GameType::getShortDisplayName, this.config.gameType)
                .withValues(GameType.values())
                .create(Component.translatable("selectWorld.gameMode"), (cycleButton, gameType) -> {
                    this.config.gameType = gameType;
                }));

        // 在线模式选择
        rowHelper.addChild(CycleButton.builder(OnlineMode::getDisplayName, this.config.onlineMode)
                .withValues(OnlineMode.values())
                .withTooltip((mode) -> Tooltip.create(mode.gettoolTip()))
                .create(Component.translatable("neolink.gui.online_mode"), (cycleButton, onlineMode) -> {
                    this.config.onlineMode = onlineMode;
                }));

        // 允许作弊
        rowHelper.addChild(CycleButton.onOffBuilder(this.config.allowCheats)
                .create(Component.translatable("selectWorld.allowCommands"), (cycleButton, allowCheats) -> {
                    this.config.allowCheats = allowCheats;
                }));

        // 允许 PvP
        rowHelper.addChild(CycleButton.onOffBuilder(this.config.pvpAllowed)
                .create(Component.translatable("neolink.gui.pvp"), (cycleButton, pvp) -> {
                    this.config.pvpAllowed = pvp;
                }));

        // 端口输入
        rowHelper.addChild(new StringWidget(PORT_LABEL, this.font));
        this.portEditBox = createFilteredEditBox(INPUT_WIDTH, BUTTON_HEIGHT,
                String.valueOf(this.config.localPort), this::isValidPortInput, 5);
        rowHelper.addChild(this.portEditBox);

        // 最大玩家数输入
        rowHelper.addChild(new StringWidget(MAX_PLAYERS_LABEL, this.font));
        this.maxPlayersEditBox = createFilteredEditBox(INPUT_WIDTH, BUTTON_HEIGHT,
                String.valueOf(this.config.maxPlayers), this::isValidMaxPlayersInput, 3);
        rowHelper.addChild(this.maxPlayersEditBox);

        this.layout.addToContents(gridLayout);

        // 底部按钮
        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(ADVANCED_SETTINGS, this::onAdvancedSettingsClick).width(BUTTON_WIDTH).build());
        footer.addChild(Button.builder(OPEN_CONFIG_FOLDER, this::onOpenConfigFolderClick).width(BUTTON_WIDTH).build());
        footer.addChild(Button.builder(START_TUNNEL, this::onStartTunnelClick).width(BUTTON_WIDTH).build());
        footer.addChild(Button.builder(CANCEL, this::onCancelClick).width(BUTTON_WIDTH).build());

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    // ==================== 组件创建辅助方法 ====================

    private EditBox createEditBox(int width, int height, String initial) {
        EditBox editBox = new EditBox(
                this.font,
                0, 0,
                width, height,
                Component.literal("")
        );
        editBox.setValue(initial);
        return editBox;
    }

    private EditBox createFilteredEditBox(int width, int height, String initial,
                                          Predicate<String> filter, int maxLength) {
        EditBox editBox = createEditBox(width, height, initial);
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
