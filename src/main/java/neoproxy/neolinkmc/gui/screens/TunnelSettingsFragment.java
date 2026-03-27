package neoproxy.neolinkmc.gui.screens;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Color;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.EditText;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import neoproxy.neolinkmc.config.ConfigManager;
import neoproxy.neolinkmc.gui.core.MinecraftThemeManager;

import java.util.function.Consumer;

public class TunnelSettingsFragment {

    private final Context context;
    private final Consumer<String> onValidationError;
    private final Runnable onStartTunnel;
    private final Runnable onStopTunnel;

    private EditText keyEdit, serverAddressEdit, localPortEdit, hookPortEdit, hostConnectPortEdit;
    private TextView statusText;
    private Button startButton, stopButton;

    private String key = "", serverAddress = "p.ceroxe.fun";
    private int localPort = 25565, hookPort = 44801, hostConnectPort = 44802;
    private boolean isConnected = false;

    public TunnelSettingsFragment(Context context, Consumer<String> onValidationError, Runnable onStartTunnel, Runnable onStopTunnel) {
        this.context = context;
        this.onValidationError = onValidationError;
        this.onStartTunnel = onStartTunnel;
        this.onStopTunnel = onStopTunnel;
        loadFromConfig();
    }

    private void loadFromConfig() {
        this.key = ConfigManager.getString("tunnel_key", "");
        this.serverAddress = ConfigManager.getString("tunnel_server", "p.ceroxe.fun");
        this.localPort = ConfigManager.getInt("tunnel_local_port", 25565);
        this.hookPort = ConfigManager.getInt("tunnel_hook_port", 44801);
        this.hostConnectPort = ConfigManager.getInt("tunnel_host_connect_port", 44802);
    }

    public View createView() {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(16));

        root.addView(createLabeledEditRow("密钥", key, v -> key = v));
        root.addView(createLabeledEditRow("服务器地址", serverAddress, v -> serverAddress = v));
        root.addView(createLabeledEditRow("本地端口", String.valueOf(localPort), v -> {
            try {
                localPort = Integer.parseInt(v);
            } catch (Exception ignored) {
            }
        }));
        root.addView(createLabeledEditRow("Hook端口", String.valueOf(hookPort), v -> {
            try {
                hookPort = Integer.parseInt(v);
            } catch (Exception ignored) {
            }
        }));
        root.addView(createLabeledEditRow("主机连接端口", String.valueOf(hostConnectPort), v -> {
            try {
                hostConnectPort = Integer.parseInt(v);
            } catch (Exception ignored) {
            }
        }));

        View divider = new View(context);
        divider.setBackground(new ColorDrawable(MinecraftThemeManager.DIVIDER_COLOR));
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(2));
        dlp.setMargins(0, dp(20), 0, dp(20));
        root.addView(divider, dlp);

        LinearLayout statusRow = new LinearLayout(context);
        statusRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView sl = new TextView(context);
        sl.setText("连接状态: ");
        sl.setTextColor(Color.WHITE);
        sl.setTextSize(16f);
        statusText = new TextView(context);
        statusText.setTextSize(16f);
        statusRow.addView(sl);
        statusRow.addView(statusText);
        root.addView(statusRow);

        LinearLayout buttonRow = new LinearLayout(context);
        buttonRow.setPadding(0, dp(20), 0, 0);
        buttonRow.setGravity(Gravity.CENTER);

        startButton = createBtn("启动", onStartTunnel);
        stopButton = createBtn("停止", onStopTunnel);

        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        blp.rightMargin = dp(10);
        buttonRow.addView(startButton, blp);
        buttonRow.addView(stopButton, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        root.addView(buttonRow);
        refreshStatus();
        return root;
    }

    private LinearLayout createLabeledEditRow(String label, String value, Consumer<String> onChange) {
        LinearLayout row = new LinearLayout(context);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(6), 0, dp(6));

        TextView tv = new TextView(context);
        tv.setText(label);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(14f);
        EditText et = new EditText(context);
        et.setText(value);
        et.setTextColor(Color.WHITE);
        et.setBackground(MinecraftThemeManager.createMinecraftInputBackground(et, false));
        et.setPadding(dp(10), dp(10), dp(10), dp(10));
        et.setOnFocusChangeListener((v, f) -> et.setBackground(MinecraftThemeManager.createMinecraftInputBackground(et, f)));
        et.addTextChangedListener(new icyllis.modernui.text.TextWatcher() {
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

        row.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
        row.addView(et, new LinearLayout.LayoutParams(dp(200), ViewGroup.LayoutParams.WRAP_CONTENT));
        return row;
    }

    private Button createBtn(String text, Runnable r) {
        Button b = new Button(context);
        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setTextSize(16f);
        b.setPadding(dp(10), dp(12), dp(10), dp(12));
        b.setBackground(MinecraftThemeManager.createMinecraftButtonBackground(b));
        b.setOnClickListener(v -> {
            if (r != null) r.run();
        });
        return b;
    }

    public void refreshStatus() {
        boolean r = neoproxy.neolinkmc.NeoLinkMC.isRunning();
        if (statusText != null) {
            statusText.setText(r ? "已连接" : "未连接");
            statusText.setTextColor(r ? MinecraftThemeManager.SUCCESS_COLOR : Color.GRAY);
        }
        if (startButton != null) startButton.setEnabled(!r);
        if (stopButton != null) stopButton.setEnabled(r);
    }

    private int dp(int v) {
        return (int) (v * context.getResources().getDisplayMetrics().density);
    }
}