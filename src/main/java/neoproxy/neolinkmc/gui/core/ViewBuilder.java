package neoproxy.neolinkmc.gui.core;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.text.Editable;
import icyllis.modernui.text.TextWatcher;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.*;

import java.util.function.Consumer;

/**
 * ModernUI 视图构建工具类
 * 提供流畅的 API 用于构建 ModernUI 界面
 *
 * @author NeoProxy Team
 * @version 5.11.2
 */
public class ViewBuilder {

    private final View view;

    private ViewBuilder(View view) {
        this.view = view;
    }

    /**
     * 创建线性布局构建器
     *
     * @param context 视图上下文
     * @return ViewBuilder 实例
     */
    public static ViewBuilder linearLayout(Context context) {
        LinearLayout layout = new LinearLayout(context);
        return new ViewBuilder(layout);
    }

    /**
     * 创建帧布局构建器
     *
     * @param context 视图上下文
     * @return ViewBuilder 实例
     */
    public static ViewBuilder frameLayout(Context context) {
        FrameLayout layout = new FrameLayout(context);
        return new ViewBuilder(layout);
    }

    /**
     * 创建滚动视图构建器
     *
     * @param context 视图上下文
     * @return ViewBuilder 实例
     */
    public static ViewBuilder scrollView(Context context) {
        ScrollView scrollView = new ScrollView(context);
        return new ViewBuilder(scrollView);
    }

    /**
     * 创建文本视图构建器
     *
     * @param context 视图上下文
     * @param text    文本内容
     * @return ViewBuilder 实例
     */
    public static ViewBuilder textView(Context context, String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        return new ViewBuilder(textView);
    }

    /**
     * 创建按钮构建器
     *
     * @param context 视图上下文
     * @param text    按钮文本
     * @param onClick 点击回调
     * @return ViewBuilder 实例
     */
    public static ViewBuilder button(Context context, String text, View.OnClickListener onClick) {
        Button button = new Button(context);
        button.setText(text);
        button.setOnClickListener(onClick);
        return new ViewBuilder(button);
    }

    /**
     * 创建输入框构建器
     *
     * @param context    视图上下文
     * @param hint       提示文本
     * @param textChange 文本变化回调
     * @return ViewBuilder 实例
     */
    public static ViewBuilder editText(Context context, String hint, Consumer<String> textChange) {
        EditText editText = new EditText(context);
        editText.setHint(hint);
        if (textChange != null) {
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    textChange.accept(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
        return new ViewBuilder(editText);
    }

    /**
     * 创建复选框构建器
     *
     * @param context  视图上下文
     * @param text     标签文本
     * @param checked  初始选中状态
     * @param onChange 状态变化回调
     * @return ViewBuilder 实例
     */
    public static ViewBuilder checkBox(Context context, String text, boolean checked,
                                       Consumer<Boolean> onChange) {
        CheckBox checkBox = new CheckBox(context);
        checkBox.setText(text);
        checkBox.setChecked(checked);
        if (onChange != null) {
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> onChange.accept(isChecked));
        }
        return new ViewBuilder(checkBox);
    }

    /**
     * 设置布局方向（仅 LinearLayout）
     *
     * @param orientation 方向（LinearLayout.VERTICAL 或 LinearLayout.HORIZONTAL）
     * @return this
     */
    public ViewBuilder orientation(int orientation) {
        if (view instanceof LinearLayout) {
            ((LinearLayout) view).setOrientation(orientation);
        }
        return this;
    }

    /**
     * 设置内边距
     *
     * @param left   左内边距
     * @param top    上内边距
     * @param right  右内边距
     * @param bottom 下内边距
     * @return this
     */
    public ViewBuilder padding(int left, int top, int right, int bottom) {
        view.setPadding(dp(left), dp(top), dp(right), dp(bottom));
        return this;
    }

    /**
     * 设置统一内边距
     *
     * @param padding 内边距值
     * @return this
     */
    public ViewBuilder padding(int padding) {
        int p = dp(padding);
        view.setPadding(p, p, p, p);
        return this;
    }

    /**
     * 设置布局参数
     *
     * @param width  宽度
     * @param height 高度
     * @return this
     */
    public ViewBuilder layoutParams(int width, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(width, height);
        } else {
            params.width = width;
            params.height = height;
        }
        view.setLayoutParams(params);
        return this;
    }

    /**
     * 设置线性布局权重（仅 LinearLayout 子视图）
     *
     * @param weight 权重值
     * @return this
     */
    public ViewBuilder weight(float weight) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) params).weight = weight;
            view.setLayoutParams(params);
        }
        return this;
    }

    /**
     * 设置背景颜色
     *
     * @param color 颜色值
     * @return this
     */
    public ViewBuilder backgroundColor(int color) {
        view.setBackground(new ColorDrawable(color));
        return this;
    }

    /**
     * 设置背景 Drawable
     *
     * @param drawable Drawable 对象
     * @return this
     */
    public ViewBuilder background(Drawable drawable) {
        view.setBackground(drawable);
        return this;
    }

    /**
     * 设置文本颜色（仅 TextView 及其子类）
     *
     * @param color 颜色值
     * @return this
     */
    public ViewBuilder textColor(int color) {
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(color);
        }
        return this;
    }

    /**
     * 设置文本大小（仅 TextView 及其子类）
     *
     * @param size 文本大小（sp）
     * @return this
     */
    public ViewBuilder textSize(float size) {
        if (view instanceof TextView) {
            ((TextView) view).setTextSize(size);
        }
        return this;
    }

    /**
     * 设置文本对齐方式（仅 TextView 及其子类）
     *
     * @param gravity 对齐方式
     * @return this
     */
    public ViewBuilder textGravity(int gravity) {
        if (view instanceof TextView) {
            ((TextView) view).setGravity(gravity);
        }
        return this;
    }

    /**
     * 添加子视图（仅 ViewGroup）
     *
     * @param child 子视图
     * @return this
     */
    public ViewBuilder addView(View child) {
        if (view instanceof ViewGroup) {
            ((ViewGroup) view).addView(child);
        }
        return this;
    }

    /**
     * 添加子视图构建器（仅 ViewGroup）
     *
     * @param builder 子视图构建器
     * @return this
     */
    public ViewBuilder addView(ViewBuilder builder) {
        if (view instanceof ViewGroup) {
            ((ViewGroup) view).addView(builder.build());
        }
        return this;
    }

    /**
     * 设置可见性
     *
     * @param visible 是否可见
     * @return this
     */
    public ViewBuilder visible(boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
        return this;
    }

    /**
     * 设置点击监听器
     *
     * @param listener 点击监听器
     * @return this
     */
    public ViewBuilder onClick(View.OnClickListener listener) {
        view.setOnClickListener(listener);
        return this;
    }

    /**
     * 构建并返回视图
     *
     * @return 构建好的视图
     */
    public View build() {
        return view;
    }

    /**
     * 获取视图（用于需要直接操作视图的场景）
     *
     * @param <T> 视图类型
     * @return 视图实例
     */
    @SuppressWarnings("unchecked")
    public <T extends View> T getView() {
        return (T) view;
    }

    /**
     * 将 dp 转换为像素
     *
     * @param dp dp 值
     * @return 像素值
     */
    private int dp(int dp) {
        return view.dp(dp);
    }
}
