package neoproxy.neolinkmc.gui.components;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.GradientDrawable;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.*;
import neoproxy.neolinkmc.gui.core.ThemeManager;

import java.util.List;
import java.util.function.Consumer;

/**
 * 带标签的下拉选择框组件 - Material Design 3 风格
 * 将标签和下拉框封装为一个整体组件，支持聚焦效果和动画
 *
 * @author NeoProxy Team
 * @version 5.11.2
 */
public class LabeledSpinner<T> extends LinearLayout {

    private final TextView labelView;
    private final Spinner spinner;
    private final View focusIndicator;
    private List<T> items;
    private Consumer<T> onItemSelected;

    /**
     * 创建带标签的下拉选择框
     *
     * @param context        上下文
     * @param label          标签文本
     * @param items          选项列表
     * @param selectedIndex  默认选中索引
     * @param onItemSelected 选项变化回调
     */
    public LabeledSpinner(Context context, String label, List<T> items,
                          int selectedIndex, Consumer<T> onItemSelected) {
        super(context);

        this.items = items;
        this.onItemSelected = onItemSelected;

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

        // 创建下拉框容器
        LinearLayout spinnerContainer = new LinearLayout(context);
        spinnerContainer.setOrientation(LinearLayout.HORIZONTAL);
        spinnerContainer.setBackground(createSpinnerBackground());
        spinnerContainer.setPadding(dp(ThemeManager.PADDING_SMALL), dp(ThemeManager.PADDING_SMALL),
                dp(ThemeManager.PADDING_SMALL), dp(ThemeManager.PADDING_SMALL));

        // 创建下拉框 - ModernUI 的 Spinner
        spinner = new Spinner(context);
        ArrayAdapter<T> adapter = new ArrayAdapter<>(context, items);
        spinner.setAdapter(adapter);

        // 设置默认选中
        if (selectedIndex >= 0 && selectedIndex < items.size()) {
            spinner.setSelection(selectedIndex);
        }

        // 设置选择监听
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateFocusIndicator(true);
                if (onItemSelected != null && position >= 0 && position < items.size()) {
                    onItemSelected.accept(items.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateFocusIndicator(false);
            }
        });

        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        spinnerContainer.addView(spinner, spinnerParams);

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

        addView(spinnerContainer);
        addView(focusIndicator);
    }

    /**
     * 创建带标签的下拉选择框（简化版）
     *
     * @param context        上下文
     * @param label          标签文本
     * @param items          选项列表
     * @param onItemSelected 选项变化回调
     */
    public LabeledSpinner(Context context, String label, List<T> items,
                          Consumer<T> onItemSelected) {
        this(context, label, items, 0, onItemSelected);
    }

    /**
     * 创建下拉框背景
     */
    private GradientDrawable createSpinnerBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(ThemeManager.SURFACE_CONTAINER_COLOR);
        drawable.setCornerRadius(dp(ThemeManager.INPUT_CORNER_RADIUS));
        drawable.setStroke(dp(1), ThemeManager.withAlpha(ThemeManager.PRIMARY_COLOR, 50));
        return drawable;
    }

    /**
     * 更新聚焦指示器状态
     */
    private void updateFocusIndicator(boolean focused) {
        focusIndicator.setAlpha(focused ? 1.0f : 0.3f);
        if (focused) {
            GradientDrawable bg = (GradientDrawable) focusIndicator.getBackground();
            bg.setColor(ThemeManager.PRIMARY_COLOR);
        }
    }

    /**
     * 获取当前选中的项
     *
     * @return 当前选中的项，如果没有选中则返回 null
     */
    public T getSelectedItem() {
        int position = spinner.getSelectedItemPosition();
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    /**
     * 获取选中项的索引
     *
     * @return 选中索引，未选中返回 -1
     */
    public int getSelectedIndex() {
        return spinner.getSelectedItemPosition();
    }

    /**
     * 设置选中项
     *
     * @param index 索引
     */
    public void setSelection(int index) {
        if (index >= 0 && index < items.size()) {
            spinner.setSelection(index);
        }
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
     * 获取下拉框控件
     *
     * @return Spinner 控件
     */
    public Spinner getSpinner() {
        return spinner;
    }

    /**
     * 设置选项变化监听器
     *
     * @param listener 监听器
     */
    public void setOnItemSelectedListener(Consumer<T> listener) {
        this.onItemSelected = listener;
    }

    /**
     * 设置是否启用
     *
     * @param enabled 是否启用
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        spinner.setEnabled(enabled);
        labelView.setTextColor(enabled ? ThemeManager.TEXT_SECONDARY_COLOR : ThemeManager.TEXT_DISABLED_COLOR);
    }
}
