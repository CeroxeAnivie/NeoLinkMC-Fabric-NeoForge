package neoproxy.neolinkmc.gui;

import neoproxy.neolinkmc.NeoLinkMC;
import neoproxy.neolinkmc.config.NeoLinkConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

import java.awt.Desktop;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * NeoLink 配置界面
 * <p>
 * 界面布局：
 * - 顶部：高级设置按钮 + 打开配置文件夹按钮
 * - 中部：内网穿透设置标题 + 游戏设置按钮网格
 * - 底部：端口输入框 + 最大玩家数输入框 + 操作按钮
 *
 * @author NeoProxy Team
 * @version 3.0.0
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

    // ==================== 布局常量 ====================

    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int INPUT_WIDTH = 150;
    private static final int LABEL_WIDTH = 120;
    private static final int PADDING = 10;
    private static final int ROW_HEIGHT = 25;

    // ==================== 配置状态 ====================

    private final Screen parentScreen;
    private final ConfigContainer config;

    // ==================== GUI 组件 ====================

    private Button advancedSettingsButton;
    private Button openConfigFolderButton;
    private Button gameModeButton;
    private Button allowCheatsButton;
    private Button onlineModeButton;
    private Button allowPvpButton;
    private Button startTunnelButton;
    private Button cancelButton;
    private EditBox portEditBox;
    private EditBox maxPlayersEditBox;

    // ==================== 动态计算的Y坐标（供render使用）====================

    private int titleY;
    private int subtitleY;
    private int inputLabelY;

    public NeoLinkConfigScreen(Screen parentScreen) {
        super(TITLE);
        this.parentScreen = parentScreen;
        this.config = new ConfigContainer();
        loadConfig();
    }

    /**
     * 从配置文件加载配置 - 启动流程：打开GUI时读取config.json
     */
    private void loadConfig() {
        this.config.loadFromConfig();
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int leftButtonX = centerX - 155;
        int rightButtonX = centerX + 5;

        // ==================== 完全自适应布局（整体自适应）====================
        // 上面一大团（标题+6个按钮）作为一个整体，根据窗口大小向下移动
        // 计算这团内容的高度
        int topSectionHeight = 25 + BUTTON_HEIGHT + 30 + BUTTON_HEIGHT + 15 + BUTTON_HEIGHT + 4 + BUTTON_HEIGHT; // 间距+元素总高度

        // 从底部向上计算，确保底部元素位置正确
        int bottomMargin = this.height / 10;
        int bottomRowY = this.height - bottomMargin;
        int inputRowY = bottomRowY - BUTTON_HEIGHT - (this.height / 30);
        int labelHeight = 10;
        int inputLabelY = inputRowY - labelHeight - (this.height / 60);

        // 上面那团内容的起始位置：在输入框标签上方留出合理间距
        int topSectionStart = Math.max(20, inputLabelY - 40 - topSectionHeight); // 最小20像素，防止小窗口被切掉

        // 使用固定相对位置布局
        int titleY = topSectionStart; // 大标题
        int topRowY = titleY + 25; // 顶部按钮在大标题下方25像素
        int subtitleY = topRowY + BUTTON_HEIGHT + 30; // 副标题在顶部按钮下方30像素
        int firstRowY = subtitleY + 15; // 第一排按钮在副标题下方15像素
        int secondRowY = firstRowY + BUTTON_HEIGHT + 4; // 第二排按钮紧贴第一排下方（固定4像素间距）

        // 保存Y坐标供render使用
        this.titleY = titleY;
        this.subtitleY = subtitleY;
        this.inputLabelY = inputLabelY;

        // ==================== 第一行：顶部按钮 ====================
        this.advancedSettingsButton = addRenderableWidget(
                createButton(leftButtonX, topRowY, 150, BUTTON_HEIGHT,
                        ADVANCED_SETTINGS, this::onAdvancedSettingsClick)
        );
        this.openConfigFolderButton = addRenderableWidget(
                createButton(rightButtonX, topRowY, 150, BUTTON_HEIGHT,
                        OPEN_CONFIG_FOLDER, this::onOpenConfigFolderClick)
        );

        // ==================== 第二/三行：标题区域（在render中绘制）====================

        // ==================== 第四行：游戏设置按钮网格 ====================
        this.gameModeButton = addRenderableWidget(
                createButton(leftButtonX, firstRowY, 150, BUTTON_HEIGHT,
                        getGameModeDisplayText(), this::onGameModeClick)
        );
        this.onlineModeButton = addRenderableWidget(
                createButton(rightButtonX, firstRowY, 150, BUTTON_HEIGHT,
                        getOnlineModeDisplayText(), this::onOnlineModeClick)
        );

        this.allowCheatsButton = addRenderableWidget(
                createButton(leftButtonX, secondRowY, 150, BUTTON_HEIGHT,
                        getAllowCheatsDisplayText(), this::onAllowCheatsClick)
        );
        this.allowPvpButton = addRenderableWidget(
                createButton(rightButtonX, secondRowY, 150, BUTTON_HEIGHT,
                        getAllowPvpDisplayText(), this::onAllowPvpClick)
        );

        // ==================== 第五/六行：输入框标签和输入框 ====================

        this.portEditBox = addRenderableWidget(
                createFilteredEditBox(leftButtonX, inputRowY, 147, BUTTON_HEIGHT,
                        String.valueOf(this.config.localPort),
                        this::isValidPortInput, 5)
        );
        this.maxPlayersEditBox = addRenderableWidget(
                createFilteredEditBox(rightButtonX, inputRowY, 147, BUTTON_HEIGHT,
                        String.valueOf(this.config.maxPlayers),
                        this::isValidMaxPlayersInput, 3)
        );

        // ==================== 第七行：底部按钮 ====================
        this.startTunnelButton = addRenderableWidget(
                createButton(leftButtonX, bottomRowY, 150, BUTTON_HEIGHT,
                        START_TUNNEL, this::onStartTunnelClick)
        );
        this.cancelButton = addRenderableWidget(
                createButton(rightButtonX, bottomRowY, 150, BUTTON_HEIGHT,
                        CANCEL, this::onCancelClick)
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 先渲染背景和组件（super.render 会处理背景模糊和半透明遮罩）
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 然后在背景之上绘制自定义文本
        int centerX = this.width / 2;

        // 渲染标题 - 使用init中动态计算的Y坐标
        drawCenteredString(guiGraphics, TUNNEL_SETTINGS, centerX, this.titleY, 0xFFFFFFFF);

        // 渲染副标题 - 使用init中动态计算的Y坐标，颜色使用白色更明显
        drawCenteredString(guiGraphics, PLAYER_SETTINGS, centerX, this.subtitleY, 0xFFFFFFFF);

        // 渲染输入框标签 - 使用init中动态计算的Y坐标
        int leftLabelX = centerX - 155;
        int rightLabelX = centerX + 5;
        drawString(guiGraphics, PORT_LABEL, leftLabelX, this.inputLabelY, 0xFFAAAAAA);
        drawString(guiGraphics, MAX_PLAYERS_LABEL, rightLabelX, this.inputLabelY, 0xFFAAAAAA);
    }

    // ==================== 组件创建辅助方法 ====================

    private Button createButton(int x, int y, int width, int height, Component message, Consumer<Button> onPress) {
        return Button.builder(message, onPress::accept)
                .pos(x, y)
                .size(width, height)
                .build();
    }

    private EditBox createEditBox(int x, int y, int width, int height, String initial) {
        EditBox editBox = new EditBox(
                this.font,
                x, y,
                width, height,
                Component.literal("")
        );
        editBox.setValue(initial);
        return editBox;
    }

    private EditBox createFilteredEditBox(int x, int y, int width, int height, String initial,
                                          Predicate<String> filter, int maxLength) {
        EditBox editBox = createEditBox(x, y, width, height, initial);
        editBox.setFilter(filter);
        editBox.setMaxLength(maxLength);
        return editBox;
    }

    // ==================== 渲染辅助方法 ====================

    private void drawCenteredString(GuiGraphics guiGraphics, Component text, int x, int y, int color) {
        int textWidth = this.font.width(text);
        int drawX = x - textWidth / 2;
        guiGraphics.drawString(this.font, text, drawX, y, color, true);
    }

    private void drawString(GuiGraphics guiGraphics, Component text, int x, int y, int color) {
        guiGraphics.drawString(this.font, text, x, y, color, true);
    }

    // ==================== 按钮文本生成 ====================

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
        // 1. 保存配置到 config.json（key 不保存）- 启动流程：点击开启时写入config
        saveConfig();

        // 2. 获取密钥（从高级设置界面传入的密钥，空字符串则使用 "Free"）
        String key = this.config.key;
        if (key == null || key.trim().isEmpty()) {
            key = "Free";
        }

        // 3. 先验证密钥，如果失败则直接提示并返回，不开启 LAN
        var connectionService = new neoproxy.neolinkmc.service.ConnectionService();
        var validationResult = connectionService.validateKeySync(key);

        if (!validationResult.success()) {
            // 密钥验证失败，显示错误信息并返回，不开启LAN
            NeoLinkMC.LOGGER.error("密钥验证失败: {}", validationResult.errorMessage());
            Minecraft.getInstance().gui.getChat().addMessage(
                    Component.literal("§c[NeoLinkMC] 开启失败: " + validationResult.errorMessage())
            );
            return;
        }

        // 4. 密钥验证成功，根据配置的端口建议值开启 LAN
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) {
            NeoLinkMC.LOGGER.error("无法获取单人游戏服务器实例");
            return;
        }

        // 应用游戏设置到服务器
        this.config.applyToCurrentServer(server);

        // 获取建议端口（用户输入的）
        int suggestedPort = parsePortInput();

        // 开启 LAN，传入建议端口，Minecraft 会尝试使用，如果不可用会分配其他端口
        server.publishServer(this.config.gameType, this.config.allowCheats, suggestedPort);

        // 获取实际分配的 LAN 端口
        int actualPort = server.getPort();
        NeoLinkMC.LOGGER.info("LAN 已开启，建议端口: {}, 实际端口: {}", suggestedPort, actualPort);

        // 5. 启动 NeoLinkMC 服务（密钥已验证）
        connectionService.startAfterValidation();

        // 6. 动态修改 NeoLinkMC 的 localPort 为实际LAN端口
        connectionService.setLocalPort(actualPort);
        NeoLinkMC.LOGGER.info("已设置 NeoLinkMC 本地端口为实际LAN端口: {}", actualPort);

        // 保存到全局服务引用
        NeoLinkMC.updateConnectionService(connectionService);

        // 返回上级界面
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

    @Override
    public void onClose() {
        goBack();
    }

    /**
     * 打开文件夹
     *
     * @param path 文件夹路径
     * @return 是否成功打开
     */
    private static boolean openFolder(Path path) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(path.toFile());
                return true;
            }
        } catch (Exception e) {
            NeoLinkMC.LOGGER.error("打开文件夹失败: {}", path, e);
        }
        return false;
    }
}
