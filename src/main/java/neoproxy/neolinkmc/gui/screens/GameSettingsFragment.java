package neoproxy.neolinkmc.gui.screens;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Color;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.*;
import neoproxy.neolinkmc.config.ConfigManager;
import neoproxy.neolinkmc.gui.OnlineMode;
import neoproxy.neolinkmc.gui.core.MinecraftThemeManager;
import net.minecraft.world.level.GameType;

import java.util.Arrays;
import java.util.function.Consumer;

public class GameSettingsFragment {

    private final Context context;
    private final Consumer<String> onValidationError;

    private Spinner gameModeSpinner, onlineModeSpinner;
    private CheckBox pvpCheckBox, allowCheatsCheckBox;
    private EditText maxPlayersEdit;

    private GameType gameType = GameType.SURVIVAL;
    private OnlineMode onlineMode = OnlineMode.OFFLINE_TRY_ONLINE_UUID_FIRST;
    private boolean pvp = true, cheats = true;
    private int maxPlayers = 8;

    public GameSettingsFragment(Context context, Consumer<String> onValidationError) {
        this.context = context;
        this.onValidationError = onValidationError;
        loadFromConfig();
    }

    private void loadFromConfig() {
        this.gameType = ConfigManager.getGameType("gamemode", GameType.SURVIVAL);
        try {
            this.onlineMode = OnlineMode.valueOf(ConfigManager.getString("onlinemode", "OFFLINE_TRY_ONLINE_UUID_FIRST"));
        } catch (Exception e) {
            this.onlineMode = OnlineMode.OFFLINE_TRY_ONLINE_UUID_FIRST;
        }
        this.pvp = ConfigManager.getBoolean("pvp_allowed", true);
        this.cheats = ConfigManager.getBoolean("allow_cheats", true);
        this.maxPlayers = ConfigManager.getInt("max_players", 8);
    }

    public View createView() {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(16));

        root.addView(createRow("游戏模式", createGameModeSpinner()));
        root.addView(createRow("在线模式", createOnlineModeSpinner()));

        pvpCheckBox = new CheckBox(context);
        pvpCheckBox.setText("允许 PvP");
        pvpCheckBox.setChecked(pvp);
        pvpCheckBox.setTextColor(Color.WHITE);
        allowCheatsCheckBox = new CheckBox(context);
        allowCheatsCheckBox.setText("允许作弊");
        allowCheatsCheckBox.setChecked(cheats);
        allowCheatsCheckBox.setTextColor(Color.WHITE);

        root.addView(pvpCheckBox);
        root.addView(new View(context), new LinearLayout.LayoutParams(1, dp(10)));
        root.addView(allowCheatsCheckBox);
        root.addView(new View(context), new LinearLayout.LayoutParams(1, dp(15)));

        maxPlayersEdit = new EditText(context);
        maxPlayersEdit.setText(String.valueOf(maxPlayers));
        maxPlayersEdit.setTextColor(Color.WHITE);
        maxPlayersEdit.setBackground(MinecraftThemeManager.createMinecraftInputBackground(maxPlayersEdit, false));
        maxPlayersEdit.setPadding(dp(10), dp(10), dp(10), dp(10));
        root.addView(createRow("最大玩家数", maxPlayersEdit));

        return root;
    }

    private LinearLayout createRow(String label, View v) {
        LinearLayout row = new LinearLayout(context);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));
        TextView tv = new TextView(context);
        tv.setText(label);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(16f);
        row.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
        row.addView(v, new LinearLayout.LayoutParams(dp(200), ViewGroup.LayoutParams.WRAP_CONTENT));
        return row;
    }

    private Spinner createGameModeSpinner() {
        gameModeSpinner = new Spinner(context);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, Arrays.asList("生存模式", "创造模式", "冒险模式", "旁观模式"));
        gameModeSpinner.setAdapter(adapter);
        gameModeSpinner.setBackground(MinecraftThemeManager.createMinecraftButtonBackground(gameModeSpinner));
        gameModeSpinner.setPadding(dp(10), dp(10), dp(10), dp(10));
        return gameModeSpinner;
    }

    private Spinner createOnlineModeSpinner() {
        onlineModeSpinner = new Spinner(context);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, Arrays.asList("正版验证", "离线+UUID修复", "纯离线模式"));
        onlineModeSpinner.setAdapter(adapter);
        onlineModeSpinner.setBackground(MinecraftThemeManager.createMinecraftButtonBackground(onlineModeSpinner));
        onlineModeSpinner.setPadding(dp(10), dp(10), dp(10), dp(10));
        return onlineModeSpinner;
    }

    private int dp(int v) {
        return (int) (v * context.getResources().getDisplayMetrics().density);
    }
}