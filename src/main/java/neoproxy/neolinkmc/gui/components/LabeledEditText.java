package neoproxy.neolinkmc.gui.components;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.GradientDrawable;
import icyllis.modernui.text.Editable;
import icyllis.modernui.text.TextWatcher;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.EditText;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import neoproxy.neolinkmc.gui.core.ThemeManager;

import java.util.function.Consumer;

/**
 * 带标签的输入框组件 - Material Design 3 风格
 * 将标签和输入框封装为一个整体组件，支持聚焦效果和错误提示
 *
 * @author NeoProxy Team
 * @version 5.11.2
 */
public class LabeledEditText extends LinearLayout {

    private final TextView labelView;
    private final EditText editText;
    private final View focusIndicator;
    private final GradientDrawable inputBackground;
    private String originalLabel;
    private String errorText;
    private boolean hasError = false;

    /**
     * 创建带标签的输入框
     *
     * @param context      上下文
     * @param label        标签文本
     * @param hint         输入提示
     * @param text         初始文本
     * @param onTextChange 文本变化回调
     */
    public LabeledEditText(Context context, String label, String hint,
                           String text, Consumer<String> onTextChange) {
        super(context);
        this.originalLabel = label;

        // 设置布局方向为垂直
        setOrientation(LinearLayout.VERTICAL);
        setPadding(0, dp(ThemeManager.SPACING_SMALL), 0, dp(ThemeManager.SPACING_SMALL));

        // 创建标签
        labelView = new TextView(context);
        labelView.setText(label);
        labelView.setTextColor(ThemeManager.TEXT_SECONDARY_COLOR);
        labelView.setTextSize(ThemeManager.TEXT_SIZE_SMALL);
        labelView.setPadding(dp(ThemeManager.PADDING_SMALL), 0, 0, dp(4));
        addView(labelView);

        // 创建输入框容器
        LinearLayout inputContainer = new LinearLayout(context);
        inputContainer.setOrientation(LinearLayout.HORIZONTAL);

        // 创建输入框背景
        inputBackground = new GradientDrawable();
        inputBackground.setColor(ThemeManager.SURFACE_CONTAINER_COLOR);
        inputBackground.setCornerRadius(dp(ThemeManager.INPUT_CORNER_RADIUS));
        inputBackground.setStroke(dp(1), ThemeManager.withAlpha(ThemeManager.PRIMARY_COLOR, 50));
        inputContainer.setBackground(inputBackground);

        // 创建输入框
        editText = new EditText(context);
        editText.setHint(hint);
        editText.setText(text);
        editText.setTextColor(ThemeManager.TEXT_PRIMARY_COLOR);
        editText.setHintTextColor(ThemeManager.TEXT_HINT_COLOR);
        editText.setBackground(null); // 移除默认背景
        editText.setPadding(dp(ThemeManager.PADDING_SMALL), dp(ThemeManager.PADDING_SMALL),
                dp(ThemeManager.PADDING_SMALL), dp(ThemeManager.PADDING_SMALL));

        // 设置文本变化监听
        if (onTextChange != null) {
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // 清除错误状态当用户开始输入
                    if (hasError && count > 0) {
                        clearError();
                    }
                    onTextChange.accept(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        // 设置焦点监听
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            updateFocusState(hasFocus);
        });

        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        inputContainer.addView(editText, editParams);
        addView(inputContainer);

        // 添加聚焦指示器（底部线条）
        focusIndicator = new View(context);
        GradientDrawable indicatorBg = new GradientDrawable();
        indicatorBg.setColor(ThemeManager.PRIMARY_COLOR);
        focusIndicator.setBackground(indicatorBg);
        LinearLayout.LayoutParams indicatorParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(2));
        indicatorParams.topMargin = dp(2);
        focusIndicator.setLayoutParams(indicatorParams);
        focusIndicator.setAlpha(0.3f);
        addView(focusIndicator);
    }

    /**
     * 创建文本输入框（简化版）
     *
     * @param context      上下文
     * @param label        标签文本
     * @param hint         输入提示
     * @param onTextChange 文本变化回调
     */
    public LabeledEditText(Context context, String label, String hint,
                           Consumer<String> onTextChange) {
        this(context, label, hint, "", onTextChange);
    }

    /**
     * 创建数字输入框
     *
     * @param context       上下文
     * @param label         标签文本
     * @param hint          输入提示
     * @param value         初始数值
     * @param onValueChange 数值变化回调
     */
    public LabeledEditText(Context context, String label, String hint,
                           int value, Consumer<Integer> onValueChange) {
        this(context, label, hint, String.valueOf(value),
                text -> {
                    try {
                        onValueChange.accept(Integer.parseInt(text));
                    } catch (NumberFormatException e) {
                        onValueChange.accept(0);
                    }
                });
    }

    /**
     * 更新聚焦状态
     */
    private void updateFocusState(boolean focused) {
        if (hasError) {
            // 错误状态下不改变
            return;
        }

        if (focused) {
            focusIndicator.setAlpha(1.0f);
            inputBackground.setStroke(dp(2), ThemeManager.PRIMARY_COLOR);
        } else {
            focusIndicator.setAlpha(0.3f);
            inputBackground.setStroke(dp(1), ThemeManager.withAlpha(ThemeManager.PRIMARY_COLOR, 50));
        }
    }

    /**
     * 获取当前文本
     *
     * @return 文本内容
     */
    public String getText() {
        return editText.getText().toString();
    }

    /**
     * 设置文本
     *
     * @param text 文本内容
     */
    public void setText(String text) {
        editText.setText(text);
    }

    /**
     * 获取当前数值
     *
     * @return 数值，解析失败返回0
     */
    public int getIntValue() {
        try {
            return Integer.parseInt(getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 设置标签文本
     *
     * @param label 标签文本
     */
    public void setLabel(String label) {
        this.originalLabel = label;
        updateLabel();
    }

    /**
     * 更新标签显示
     */
    private void updateLabel() {
        if (hasError && errorText != null && !errorText.isEmpty()) {
            labelView.setText(originalLabel + " - " + errorText);
            labelView.setTextColor(ThemeManager.ERROR_COLOR);
        } else {
            labelView.setText(originalLabel);
            labelView.setTextColor(ThemeManager.TEXT_SECONDARY_COLOR);
        }
    }

    /**
     * 设置输入框提示
     *
     * @param hint 提示文本
     */
    public void setHint(String hint) {
        editText.setHint(hint);
    }

    /**
     * 设置错误提示
     *
     * @param error 错误文本，null表示清除错误
     */
    public void setError(String error) {
        this.errorText = error;
        this.hasError = error != null && !error.isEmpty();

        if (hasError) {
            // 错误状态
            labelView.setTextColor(ThemeManager.ERROR_COLOR);
            inputBackground.setStroke(dp(2), ThemeManager.ERROR_COLOR);
            focusIndicator.setAlpha(1.0f);
            GradientDrawable indicatorBg = (GradientDrawable) focusIndicator.getBackground();
            indicatorBg.setColor(ThemeManager.ERROR_COLOR);
        } else {
            // 清除错误状态
            labelView.setTextColor(ThemeManager.TEXT_SECONDARY_COLOR);
            inputBackground.setStroke(dp(1), ThemeManager.withAlpha(ThemeManager.PRIMARY_COLOR, 50));
            focusIndicator.setAlpha(0.3f);
            GradientDrawable indicatorBg = (GradientDrawable) focusIndicator.getBackground();
            indicatorBg.setColor(ThemeManager.PRIMARY_COLOR);
        }

        updateLabel();
    }

    /**
     * 清除错误状态
     */
    public void clearError() {
        setError(null);
    }

    /**
     * 获取内部 EditText 实例
     *
     * @return EditText 实例
     */
    public EditText getEditText() {
        return editText;
    }

    /**
     * 设置是否启用
     *
     * @param enabled 是否启用
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        editText.setEnabled(enabled);
        labelView.setTextColor(enabled ? ThemeManager.TEXT_SECONDARY_COLOR : ThemeManager.TEXT_DISABLED_COLOR);
        if (!enabled) {
            inputBackground.setColor(ThemeManager.withAlpha(ThemeManager.SURFACE_CONTAINER_COLOR, 50));
        } else {
            inputBackground.setColor(ThemeManager.SURFACE_CONTAINER_COLOR);
        }
    }
}
