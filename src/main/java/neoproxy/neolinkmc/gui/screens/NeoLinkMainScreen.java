package neoproxy.neolinkmc.gui.screens;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Color;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.*;
import icyllis.modernui.widget.Button;
import neoproxy.neolinkmc.NeoLinkMC;
import neoproxy.neolinkmc.config.ConfigManager;
import neoproxy.neolinkmc.gui.ConfigContainer;
import neoproxy.neolinkmc.gui.OnlineMode;
import neoproxy.neolinkmc.gui.core.MinecraftThemeManager;
import neoproxy.neolinkmc.gui.core.ModernUIScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

public class NeoLinkMainScreen extends ModernUIScreen {

    // 统一网格间距
    private static final int GRID_GAP = 8;
    private final ConfigContainer configContainer;
    private View currentView;
    private GameType selectedGameType = GameType.SURVIVAL;
    private OnlineMode selectedOnlineMode = OnlineMode.OFFLINE_TRY_ONLINE_UUID_FIRST;
    private boolean allowCheats = true;
    private boolean pvpAllowed = true;
    private int localPort = 25565;
    private int maxPlayers = 8;
    private String serverAddress = "p.ceroxe.fun";
    private int hookPort = 44801;
    private int hostConnectPort = 44802;
    private String key = "";
    private Button gameModeButton;
    private Button cheatsButton;
    private Button onlineModeButton;
    private Button pvpButton;
    private EditText portEdit;
    private EditText maxPlayersEdit;

    public NeoLinkMainScreen(Screen lastScreen) {
        super(Component.literal("NeoLinkMC 配置"), lastScreen);
        this.configContainer = new ConfigContainer();
        // 注意：配置加载移至 onCreateView，确保每次打开界面都读取最新配置
    }

    /**
     * 从配置文件加载配置并应用到 GUI
     * 严格按照 config.json 定义的变量
     * 仅在点击「对局域网开放」时调用一次
     */
    private void loadConfigFromFile() {
        try {
            // 先加载配置文件
            ConfigManager.loadConfig();

            // 从当前服务器获取默认值（如果可能）
            IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
            if (server != null) {
                configContainer.loadFromCurrentServer(server);
                this.selectedGameType = configContainer.gameType;
                this.allowCheats = configContainer.allowCheats;
                this.pvpAllowed = configContainer.pvpAllowed;
                this.selectedOnlineMode = configContainer.onlineMode;
            }

            // 从配置文件读取，覆盖服务器默认值
            String gameModeStr = ConfigManager.getString("gamemode", "SURVIVAL");
            try {
                this.selectedGameType = GameType.valueOf(gameModeStr);
            } catch (Exception ignored) {
            }

            String onlineModeStr = ConfigManager.getString("onlinemode", "OFFLINE_TRY_ONLINE_UUID_FIRST");
            try {
                this.selectedOnlineMode = OnlineMode.valueOf(onlineModeStr);
            } catch (Exception ignored) {
            }

            this.pvpAllowed = ConfigManager.getBoolean("pvp_allowed", true);
            this.allowCheats = ConfigManager.getBoolean("allow_cheats", true);
            this.maxPlayers = ConfigManager.getInt("max_players", 8);

            // 服务端连接配置（注意：key 不存储到配置文件，保持为空）
            this.serverAddress = ConfigManager.getString("remote_domain", "p.ceroxe.fun");
            this.hookPort = Integer.parseInt(ConfigManager.getString("host_hook_port", "44801"));
            this.hostConnectPort = Integer.parseInt(ConfigManager.getString("host_connect_port", "44802"));
            this.localPort = Integer.parseInt(ConfigManager.getString("local_port", "25565"));
            this.key = ""; // key 不存储到配置文件

            NeoLinkMC.LOGGER.info("[NeoLinkMC] 已从配置文件加载配置");
        } catch (Exception e) {
            NeoLinkMC.LOGGER.error("[NeoLinkMC] 加载配置失败", e);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable DataSet savedInstanceState) {
        Context context = requireContext();
        // 点击「对局域网开放」时，从配置文件加载并应用到 GUI
        loadConfigFromFile();
        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        scrollView.setBackground(new ColorDrawable(MinecraftThemeManager.BACKGROUND_COLOR));
        showMainScreen(context, scrollView);
        return scrollView;
    }

    private void showMainScreen(Context context, ScrollView scrollView) {
        scrollView.removeAllViews();
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mainLayout.setPadding(0, dp(40), 0, dp(40));

        // 核心内容容器（限定最大宽度，让两边的按钮自动平分）
        LinearLayout contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        // 设置一个宽阔的基准宽度，确保按钮足够巨大！
        int maxWidth = dp(400);
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(Math.min(maxWidth, ViewGroup.LayoutParams.MATCH_PARENT), ViewGroup.LayoutParams.WRAP_CONTENT));

        // 1. 顶部两个长按钮
        contentLayout.addView(createTopButtonRow(context));
        contentLayout.addView(new View(context), new LinearLayout.LayoutParams(dp(1), dp(30)));

        // 2. 标题
        contentLayout.addView(createTitle(context, "内网穿透设置", 20f));
        contentLayout.addView(new View(context), new LinearLayout.LayoutParams(dp(1), dp(4)));

        // 3. 副标题
        contentLayout.addView(createTitle(context, "对其他玩家的设置", 16f));
        contentLayout.addView(new View(context), new LinearLayout.LayoutParams(dp(1), dp(20)));

        // 4. 中间 2x2 按钮网格
        contentLayout.addView(createSettingsGrid(context));
        contentLayout.addView(new View(context), new LinearLayout.LayoutParams(dp(1), dp(20)));

        // 5. 端口和最大玩家数
        contentLayout.addView(createPortAndPlayersRow(context));
        contentLayout.addView(new View(context), new LinearLayout.LayoutParams(dp(1), dp(20)));

        // 6. 底部确认行
        contentLayout.addView(createBottomButtonRow(context));

        mainLayout.addView(contentLayout);
        scrollView.addView(mainLayout);
        currentView = mainLayout;
    }

    private void showAdvancedSettingsScreen(Context context, ScrollView scrollView) {
        scrollView.removeAllViews();
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.setPadding(0, dp(40), 0, dp(40));

        LinearLayout contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(Math.min(dp(400), ViewGroup.LayoutParams.MATCH_PARENT), ViewGroup.LayoutParams.WRAP_CONTENT));

        contentLayout.addView(createTitle(context, "内网穿透高级设置", 20f));
        contentLayout.addView(new View(context), new LinearLayout.LayoutParams(dp(1), dp(20)));

        contentLayout.addView(createAdvancedInputRow(context, "服务器节点", serverAddress, value -> serverAddress = value));
        contentLayout.addView(createAdvancedInputRow(context, "Hook 端口", String.valueOf(hookPort), value -> {
            try {
                hookPort = Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
            }
        }));
        contentLayout.addView(createAdvancedInputRow(context, "主机连接端口", String.valueOf(hostConnectPort), value -> {
            try {
                hostConnectPort = Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
            }
        }));
        contentLayout.addView(createAdvancedInputRow(context, "密钥", key, value -> key = value));

        Button backButton = createMinecraftButton(context, "返回", () -> {
            // 注意：不在此处保存配置，只在点击「开启内网穿透」时保存
            // 此时 serverAddress, hookPort, hostConnectPort, key 已通过 lambda 更新到成员变量
            showMainScreen(context, scrollView);
        });
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        backParams.topMargin = dp(20);
        backButton.setLayoutParams(backParams);
        contentLayout.addView(backButton);

        mainLayout.addView(contentLayout);
        scrollView.addView(mainLayout);
        currentView = mainLayout;
    }

    private View createTopButtonRow(Context context) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);

        Button advancedButton = createMinecraftButton(context, "内网穿透高级设置", () -> {
            if (currentView != null && currentView.getParent() instanceof ScrollView) {
                showAdvancedSettingsScreen(context, (ScrollView) currentView.getParent());
            }
        });
        // 使用 weight=1.0f 自动撑满平分宽度
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        p1.rightMargin = dp(GRID_GAP);
        advancedButton.setLayoutParams(p1);
        row.addView(advancedButton);

        Button openConfigButton = createMinecraftButton(context, "打开配置文件", this::openConfigFolder);
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        openConfigButton.setLayoutParams(p2);
        row.addView(openConfigButton);

        return row;
    }

    private View createTitle(Context context, String text, float size) {
        TextView title = new TextView(context);
        title.setText(text);
        title.setTextSize(size); // 16f 或 20f 的正常字号
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        return title;
    }

    private View createSettingsGrid(Context context) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);

        // 第一行
        LinearLayout row1 = new LinearLayout(context);
        row1.setOrientation(LinearLayout.HORIZONTAL);

        gameModeButton = createMinecraftButton(context, getGameModeText(), this::toggleGameMode);
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        p1.rightMargin = dp(GRID_GAP);
        gameModeButton.setLayoutParams(p1);
        row1.addView(gameModeButton);

        cheatsButton = createMinecraftButton(context, getCheatsText(), this::toggleCheats);
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        cheatsButton.setLayoutParams(p2);
        row1.addView(cheatsButton);

        container.addView(row1);

        // 垂直间隙
        container.addView(new View(context), new LinearLayout.LayoutParams(dp(1), dp(GRID_GAP)));

        // 第二行
        LinearLayout row2 = new LinearLayout(context);
        row2.setOrientation(LinearLayout.HORIZONTAL);

        onlineModeButton = createMinecraftButton(context, getOnlineModeText(), this::toggleOnlineMode);
        LinearLayout.LayoutParams p3 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        p3.rightMargin = dp(GRID_GAP);
        onlineModeButton.setLayoutParams(p3);
        row2.addView(onlineModeButton);

        pvpButton = createMinecraftButton(context, getPvpText(), this::togglePvp);
        LinearLayout.LayoutParams p4 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        pvpButton.setLayoutParams(p4);
        row2.addView(pvpButton);

        container.addView(row2);
        return container;
    }

    private View createPortAndPlayersRow(Context context) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);

        // 左列（端口）
        LinearLayout leftColumn = new LinearLayout(context);
        leftColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        leftParams.rightMargin = dp(GRID_GAP);
        leftColumn.setLayoutParams(leftParams);

        TextView portLabel = new TextView(context);
        portLabel.setText("端口");
        portLabel.setTextSize(14f);
        portLabel.setTextColor(Color.WHITE);
        portLabel.setPadding(dp(2), 0, 0, dp(4));
        leftColumn.addView(portLabel);

        portEdit = new EditText(context);
        portEdit.setText(String.valueOf(localPort));
        portEdit.setTextSize(16f); // 恢复正常字号
        portEdit.setTextColor(Color.WHITE);
        portEdit.setBackground(MinecraftThemeManager.createMinecraftInputBackground(portEdit, false));
        // 给输入框内部充足的上下边距，撑起高度
        portEdit.setPadding(dp(8), dp(8), dp(8), dp(8));
        portEdit.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        portEdit.setOnFocusChangeListener((v, hasFocus) -> portEdit.setBackground(MinecraftThemeManager.createMinecraftInputBackground(portEdit, hasFocus)));
        portEdit.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        leftColumn.addView(portEdit);

        container.addView(leftColumn);

        // 右列（最大玩家）
        LinearLayout rightColumn = new LinearLayout(context);
        rightColumn.setOrientation(LinearLayout.VERTICAL);
        rightColumn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView playersLabel = new TextView(context);
        playersLabel.setText("最大玩家数量");
        playersLabel.setTextSize(14f);
        playersLabel.setTextColor(Color.WHITE);
        playersLabel.setPadding(dp(2), 0, 0, dp(4));
        rightColumn.addView(playersLabel);

        maxPlayersEdit = new EditText(context);
        maxPlayersEdit.setText(String.valueOf(maxPlayers));
        maxPlayersEdit.setTextSize(16f);
        maxPlayersEdit.setTextColor(Color.WHITE);
        maxPlayersEdit.setBackground(MinecraftThemeManager.createMinecraftInputBackground(maxPlayersEdit, false));
        maxPlayersEdit.setPadding(dp(8), dp(8), dp(8), dp(8));
        maxPlayersEdit.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        maxPlayersEdit.setOnFocusChangeListener((v, hasFocus) -> maxPlayersEdit.setBackground(MinecraftThemeManager.createMinecraftInputBackground(maxPlayersEdit, hasFocus)));
        maxPlayersEdit.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        rightColumn.addView(maxPlayersEdit);

        container.addView(rightColumn);
        return container;
    }

    private View createBottomButtonRow(Context context) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);

        Button startButton = createMinecraftButton(context, "开启内网穿透", this::onStartTunnel);
        LinearLayout.LayoutParams startParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        startParams.rightMargin = dp(GRID_GAP);
        startButton.setLayoutParams(startParams);
        row.addView(startButton);

        Button cancelButton = createMinecraftButton(context, "取消", this::onCancel);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        cancelButton.setLayoutParams(cancelParams);
        row.addView(cancelButton);

        return row;
    }

    private View createAdvancedInputRow(Context context, String label, String initialValue, Consumer<String> onChange) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(4), 0, dp(8));

        TextView labelView = new TextView(context);
        labelView.setText(label);
        labelView.setTextSize(14f);
        labelView.setTextColor(Color.WHITE);
        labelView.setPadding(dp(2), 0, 0, dp(4));
        row.addView(labelView);

        EditText editText = new EditText(context);
        editText.setText(initialValue);
        editText.setTextSize(16f);
        editText.setTextColor(Color.WHITE);
        editText.setBackground(MinecraftThemeManager.createMinecraftInputBackground(editText, false));
        editText.setPadding(dp(8), dp(8), dp(8), dp(8));
        editText.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        editText.setOnFocusChangeListener((v, hasFocus) -> editText.setBackground(MinecraftThemeManager.createMinecraftInputBackground(editText, hasFocus)));
        editText.addTextChangedListener(new icyllis.modernui.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(icyllis.modernui.text.Editable s) {
                onChange.accept(s.toString());
            }
        });
        row.addView(editText, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return row;
    }

    private Button createMinecraftButton(Context context, String text, Runnable onClick) {
        Button button = new Button(context);
        button.setText(text);

        // 使用正常、饱满的 16f 字体，不再微缩
        button.setTextSize(16f);
        button.setTextColor(Color.WHITE);
        button.setGravity(Gravity.CENTER);

        // 利用内边距（Padding）把按钮的物理高度自然撑开！
        button.setPadding(dp(8), dp(10), dp(8), dp(10));

        button.setBackground(MinecraftThemeManager.createMinecraftButtonBackground(button));
        button.setOnClickListener(v -> onClick.run());
        return button;
    }

    // ==================== 切换及业务逻辑保留完整 ====================

    private void toggleGameMode() {
        switch (selectedGameType) {
            case SURVIVAL:
                selectedGameType = GameType.CREATIVE;
                break;
            case CREATIVE:
                selectedGameType = GameType.ADVENTURE;
                break;
            case ADVENTURE:
                selectedGameType = GameType.SPECTATOR;
                break;
            case SPECTATOR:
                selectedGameType = GameType.SURVIVAL;
                break;
        }
        gameModeButton.setText(getGameModeText());
    }

    private void toggleCheats() {
        allowCheats = !allowCheats;
        cheatsButton.setText(getCheatsText());
    }

    private void toggleOnlineMode() {
        switch (selectedOnlineMode) {
            case ONLINE_ONLINE_UUID_ONLY:
                selectedOnlineMode = OnlineMode.OFFLINE_TRY_ONLINE_UUID_FIRST;
                break;
            case OFFLINE_TRY_ONLINE_UUID_FIRST:
                selectedOnlineMode = OnlineMode.OFFLINE_OFFLINE_UUID_ONLY;
                break;
            case OFFLINE_OFFLINE_UUID_ONLY:
                selectedOnlineMode = OnlineMode.ONLINE_ONLINE_UUID_ONLY;
                break;
        }
        onlineModeButton.setText(getOnlineModeText());
    }

    private void togglePvp() {
        pvpAllowed = !pvpAllowed;
        pvpButton.setText(getPvpText());
    }

    private String getGameModeText() {
        String modeName;
        switch (selectedGameType) {
            case SURVIVAL:
                modeName = "生存";
                break;
            case CREATIVE:
                modeName = "创造";
                break;
            case ADVENTURE:
                modeName = "冒险";
                break;
            case SPECTATOR:
                modeName = "旁观";
                break;
            default:
                modeName = "生存";
        }
        return "游戏模式：" + modeName;
    }

    private String getCheatsText() {
        return "允许作弊：" + (allowCheats ? "开" : "关");
    }

    private String getOnlineModeText() {
        switch (selectedOnlineMode) {
            case ONLINE_ONLINE_UUID_ONLY:
                return "正版验证";
            case OFFLINE_TRY_ONLINE_UUID_FIRST:
                return "离线模式 + UUID 修复";
            case OFFLINE_OFFLINE_UUID_ONLY:
                return "纯离线模式";
            default:
                return "离线模式 + UUID 修复";
        }
    }

    private String getPvpText() {
        return "允许 PVP：" + (pvpAllowed ? "开" : "关");
    }

    private void openConfigFolder() {
        try {
            File configDir = new File("config/NeoLinkMC");
            if (!configDir.exists()) configDir.mkdirs();
            Desktop.getDesktop().open(configDir);
        } catch (Exception e) {
            showError("无法打开配置文件夹: " + e.getMessage());
        }
    }

    private void onStartTunnel() {
        try {
            if (portEdit == null || maxPlayersEdit == null) {
                showError("界面未正确初始化");
                return;
            }
            if (portEdit.getText().toString().trim().isEmpty() || maxPlayersEdit.getText().toString().trim().isEmpty()) {
                showError("端口或最大玩家数不能为空");
                return;
            }
            try {
                localPort = Integer.parseInt(portEdit.getText().toString().trim());
                maxPlayers = Integer.parseInt(maxPlayersEdit.getText().toString().trim());
            } catch (NumberFormatException e) {
                showError("必须输入有效数字");
                return;
            }

            String useKey = (key == null || key.trim().isEmpty()) ? "Free" : key;
            saveAllConfig();

            IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
            if (server != null) {
                configContainer.gameType = selectedGameType;
                configContainer.onlineMode = selectedOnlineMode;
                configContainer.pvpAllowed = pvpAllowed;
                configContainer.allowCheats = allowCheats;
                configContainer.maxPlayers = maxPlayers;
                configContainer.applyToCurrentServer(server);
            }

            showInfo("正在启动内网穿透服务...");
            NeoLinkMC.startService(useKey, localPort);

            if (!NeoLinkMC.isRunning()) {
                showError("开启失败：密钥错误或无法连接服务器");
                return;
            }

            if (server != null) {
                server.publishServer(selectedGameType, allowCheats, localPort);
                int actualPort = server.getPort();
                if (actualPort != localPort) {
                    showInfo("端口已调整: " + localPort + " -> " + actualPort);
                    NeoLinkMC.updateLocalPort(actualPort);
                }
                showSuccess("内网穿透服务已启动");
                // 返回到 ESC 菜单界面
                goBack();
            } else {
                showError("无法获取服务器实例");
                NeoLinkMC.stopService();
            }
        } catch (Exception e) {
            showError("启动失败: " + e.getMessage());
            NeoLinkMC.stopService();
        }
    }

    private void onCancel() {
        goBack();
    }

    /**
     * 保存所有 GUI 配置到配置文件
     * 严格按照 config.json 定义的变量，禁止添加额外项
     * 仅在点击「开启内网穿透」时调用
     */
    private void saveAllConfig() {
        // 服务端连接配置
        ConfigManager.setString("remote_domain", serverAddress);
        ConfigManager.setString("host_hook_port", String.valueOf(hookPort));
        ConfigManager.setString("host_connect_port", String.valueOf(hostConnectPort));
        ConfigManager.setString("local_port", String.valueOf(localPort));
        ConfigManager.setString("local_domain", "localhost");
        // 游戏设置
        ConfigManager.setString("gamemode", selectedGameType.name());
        ConfigManager.setString("onlinemode", selectedOnlineMode.name());
        ConfigManager.setBoolean("pvp_allowed", pvpAllowed);
        ConfigManager.setBoolean("allow_cheats", allowCheats);
        ConfigManager.setInt("max_players", maxPlayers);
        // 写入文件
        ConfigManager.saveConfig();
    }

    private void showError(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
    }

    private void showSuccess(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void showInfo(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private int dp(int dp) {
        // 直接从 Context 获取密度进行计算，避免 View 类型不匹配问题
        float density = requireContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }
}