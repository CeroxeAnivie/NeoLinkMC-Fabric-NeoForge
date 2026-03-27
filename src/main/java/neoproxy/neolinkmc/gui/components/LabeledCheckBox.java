package neoproxy.neolinkmc.gui.components;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.GradientDrawable;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.MotionEvent;
import icyllis.modernui.widget.CheckBox;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import neoproxy.neolinkmc.gui.core.ThemeManager;

import java.util.function.Consumer;

/**
 * 带标签的复选框组件 - Material Design 3 风格
 * 将标签和复选框封装为一个整体组件，支持标签点击切换和悬停效果
 *
 * @author NeoProxy Team
 * @version 5.11.2
 */
public class LabeledCheckBox extends LinearLayout {

    private final CheckBox checkBox;
    private final TextView labelView;
    private final GradientDrawable backgroundDrawable;
    private Consumer<Boolean> onCheckedChange;

    /**
     * 创建带标签的复选框
     *
     * @param context  上下文
     * @param label    标签文本
     * @param checked  初始选中状态
     * @param onChange 状态变化回调
     */
    public LabeledCheckBox(Context context, String label,
                           boolean checked, Consumer<Boolean> onChange) {
        super(context);

        this.onCheckedChange = onChange;

        // 设置布局方向为水平
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(dp(ThemeManager.PADDING_SMALL), dp(ThemeManager.SPACING_SMALL),
                dp(ThemeManager.PADDING_SMALL), dp(ThemeManager.SPACING_SMALL));

        // 设置背景 - 带圆角的卡片效果
        backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setColor(ThemeManager.SURFACE_CONTAINER_COLOR);
        backgroundDrawable.setCornerRadius(dp(ThemeManager.INPUT_CORNER_RADIUS));
        setBackground(backgroundDrawable);

        // 创建复选框
        checkBox = new CheckBox(context);
        checkBox.setChecked(checked);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBackground();
            if (onCheckedChange != null) {
                onCheckedChange.accept(isChecked);
            }
        });
        addView(checkBox);

        // 创建标签
        labelView = new TextView(context);
        labelView.setText(label);
        labelView.setTextColor(ThemeManager.TEXT_PRIMARY_COLOR);
        labelView.setTextSize(ThemeManager.TEXT_SIZE_BODY);
        labelView.setPadding(dp(ThemeManager.PADDING_SMALL), 0, 0, 0);

        // 标签点击也切换复选框状态
        labelView.setOnClickListener(v -> {
            boolean newState = !checkBox.isChecked();
            checkBox.setChecked(newState);
        });

        addView(labelView);

        // 初始化背景状态
        updateBackground();

        // 添加悬停效果
        setupHoverEffect();
    }

    /**
     * 创建带标签的复选框（简化版）
     *
     * @param context 上下文
     * @param label   标签文本
     * @param checked 初始选中状态
     */
    public LabeledCheckBox(Context context, String label, boolean checked) {
        this(context, label, checked, null);
    }

    /**
     * 更新背景颜色
     */
    private void updateBackground() {
        if (checkBox.isChecked()) {
            backgroundDrawable.setColor(ThemeManager.withAlpha(ThemeManager.PRIMARY_COLOR, 20));
            backgroundDrawable.setStroke(dp(1), ThemeManager.withAlpha(ThemeManager.PRIMARY_COLOR, 80));
        } else {
            backgroundDrawable.setColor(ThemeManager.SURFACE_CONTAINER_COLOR);
            backgroundDrawable.setStroke(dp(1), ThemeManager.withAlpha(ThemeManager.PRIMARY_COLOR, 30));
        }
    }

    /**
     * 设置悬停效果
     */
    private void setupHoverEffect() {
        setOnHoverListener((v, event) -> {
            if (!checkBox.isChecked()) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        backgroundDrawable.setColor(ThemeManager.SURFACE_LIGHT_COLOR);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        backgroundDrawable.setColor(ThemeManager.SURFACE_CONTAINER_COLOR);
                        break;
                }
            }
            return false;
        });
    }

    /**
     * 获取当前选中状态
     *
     * @return 是否选中
     */
    public boolean isChecked() {
        return checkBox.isChecked();
    }

    /**
     * 设置选中状态
     *
     * @param checked 是否选中
     */
    public void setChecked(boolean checked) {
        checkBox.setChecked(checked);
    }

    /**
     * 切换选中状态
     */
    public void toggle() {
        checkBox.toggle();
    }

    /**
     * 设置标签文本
     *
     * @param label 标签文本
     */
    public void setLabel(String label) {
        labelView.setText(label);
    }

    /**
     * 设置标签颜色
     *
     * @param color 颜色
     */
    public void setLabelColor(int color) {
        labelView.setTextColor(color);
    }

    /**
     * 设置状态变化监听器
     *
     * @param listener 监听器
     */
    public void setOnCheckedChangeListener(Consumer<Boolean> listener) {
        this.onCheckedChange = listener;
    }

    /**
     * 设置是否启用
     *
     * @param enabled 是否启用
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        checkBox.setEnabled(enabled);
        labelView.setTextColor(enabled ? ThemeManager.TEXT_PRIMARY_COLOR : ThemeManager.TEXT_DISABLED_COLOR);
        if (!enabled) {
            backgroundDrawable.setColor(ThemeManager.withAlpha(ThemeManager.SURFACE_CONTAINER_COLOR, 50));
        } else {
            updateBackground();
        }
    }
}
