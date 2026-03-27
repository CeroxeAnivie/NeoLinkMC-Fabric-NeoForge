package neoproxy.neolinkmc.gui.core;

import icyllis.modernui.graphics.Color;
import icyllis.modernui.graphics.drawable.*;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.util.StateSet;
import icyllis.modernui.view.View;

/**
 * ModernUI 主题管理器
 * 统一管理界面颜色、尺寸等主题配置
 * 采用 Material Design 3 设计规范
 *
 * @author NeoProxy Team
 * @version 5.11.2
 */
public final class ThemeManager {

    /**
     * 主色调 - 现代蓝色 (Material Blue 500)
     */
    public static final int PRIMARY_COLOR = Color.argb(255, 33, 150, 243);

    // ==================== Material Design 3 颜色配置 ====================
    /**
     * 主色调亮色 - 用于悬停状态 (Material Blue 400)
     */
    public static final int PRIMARY_LIGHT_COLOR = Color.argb(255, 66, 165, 245);
    /**
     * 主色调暗色 - 用于按下状态 (Material Blue 700)
     */
    public static final int PRIMARY_DARK_COLOR = Color.argb(255, 25, 118, 210);
    /**
     * 次色调 - 紫色 (Material Purple 500)
     */
    public static final int SECONDARY_COLOR = Color.argb(255, 156, 39, 176);
    /**
     * 强调色 - 青色 (Material Cyan 500)
     */
    public static final int ACCENT_COLOR = Color.argb(255, 0, 188, 212);
    /**
     * 背景色 - 深灰色半透明，带模糊效果
     */
    public static final int BACKGROUND_COLOR = Color.argb(235, 18, 18, 20);
    /**
     * 表面色 - 卡片背景 (Surface)
     */
    public static final int SURFACE_COLOR = Color.argb(255, 40, 40, 45);
    /**
     * 表面亮色 - 用于悬停卡片
     */
    public static final int SURFACE_LIGHT_COLOR = Color.argb(255, 50, 50, 56);
    /**
     * 表面容器色 - 次级容器背景
     */
    public static final int SURFACE_CONTAINER_COLOR = Color.argb(255, 35, 35, 40);
    /**
     * 卡片背景色 - 带渐变效果的卡片
     */
    public static final int CARD_BACKGROUND_COLOR = Color.argb(255, 42, 42, 48);
    /**
     * 卡片边框色 - 微妙的边框
     */
    public static final int CARD_BORDER_COLOR = Color.argb(80, 255, 255, 255);
    /**
     * 主文本颜色 - 高对比度白色
     */
    public static final int TEXT_PRIMARY_COLOR = Color.argb(255, 255, 255, 255);
    /**
     * 次要文本颜色 - 浅灰色
     */
    public static final int TEXT_SECONDARY_COLOR = Color.argb(255, 200, 200, 200);
    /**
     * 第三级文本颜色 - 中灰色
     */
    public static final int TEXT_TERTIARY_COLOR = Color.argb(255, 160, 160, 160);
    /**
     * 提示文本颜色 - 暗灰色
     */
    public static final int TEXT_HINT_COLOR = Color.argb(255, 120, 120, 120);
    /**
     * 禁用文本颜色
     */
    public static final int TEXT_DISABLED_COLOR = Color.argb(255, 100, 100, 100);
    /**
     * 分割线颜色 - 微妙的分隔线
     */
    public static final int DIVIDER_COLOR = Color.argb(60, 255, 255, 255);
    /**
     * 错误颜色 - Material Red 500
     */
    public static final int ERROR_COLOR = Color.argb(255, 244, 67, 54);
    /**
     * 错误浅色 - 用于背景
     */
    public static final int ERROR_LIGHT_COLOR = Color.argb(255, 239, 154, 154);
    /**
     * 成功颜色 - Material Green 500
     */
    public static final int SUCCESS_COLOR = Color.argb(255, 76, 175, 80);
    /**
     * 成功浅色 - 用于背景
     */
    public static final int SUCCESS_LIGHT_COLOR = Color.argb(255, 165, 214, 167);
    /**
     * 警告颜色 - Material Orange 500
     */
    public static final int WARNING_COLOR = Color.argb(255, 255, 152, 0);
    /**
     * 信息颜色 - Material Blue 300
     */
    public static final int INFO_COLOR = Color.argb(255, 100, 181, 246);
    /**
     * 运行中状态颜色 - 绿色渐变
     */
    public static final int STATUS_RUNNING_COLOR = Color.argb(255, 102, 187, 106);
    /**
     * 停止状态颜色 - 灰色
     */
    public static final int STATUS_STOPPED_COLOR = Color.argb(255, 158, 158, 158);
    /**
     * 卡片渐变起始色
     */
    public static final int CARD_GRADIENT_START = Color.argb(255, 48, 48, 55);

    // ==================== 渐变配置 ====================
    /**
     * 卡片渐变结束色
     */
    public static final int CARD_GRADIENT_END = Color.argb(255, 38, 38, 43);
    /**
     * 按钮渐变起始色
     */
    public static final int BUTTON_GRADIENT_START = Color.argb(255, 33, 150, 243);
    /**
     * 按钮渐变结束色
     */
    public static final int BUTTON_GRADIENT_END = Color.argb(255, 25, 118, 210);
    /**
     * 激活标签渐变
     */
    public static final int TAB_ACTIVE_GRADIENT_START = Color.argb(255, 41, 160, 255);
    public static final int TAB_ACTIVE_GRADIENT_END = Color.argb(255, 30, 136, 229);
    /**
     * 屏幕默认宽度
     */
    public static final int SCREEN_DEFAULT_WIDTH = 420;

    // ==================== 尺寸配置 ====================
    /**
     * 屏幕默认高度
     */
    public static final int SCREEN_DEFAULT_HEIGHT = 340;
    /**
     * 标准内边距
     */
    public static final int PADDING_STANDARD = 16;
    /**
     * 小内边距
     */
    public static final int PADDING_SMALL = 8;
    /**
     * 大内边距
     */
    public static final int PADDING_LARGE = 24;
    /**
     * 超大内边距
     */
    public static final int PADDING_XLARGE = 32;
    /**
     * 标题文本大小
     */
    public static final float TEXT_SIZE_TITLE = 26f;
    /**
     * 副标题文本大小
     */
    public static final float TEXT_SIZE_SUBTITLE = 18f;
    /**
     * 卡片标题文本大小
     */
    public static final float TEXT_SIZE_CARD_TITLE = 16f;
    /**
     * 正文文本大小
     */
    public static final float TEXT_SIZE_BODY = 14f;
    /**
     * 小文本大小
     */
    public static final float TEXT_SIZE_SMALL = 12f;
    /**
     * 超小文本大小
     */
    public static final float TEXT_SIZE_XSMALL = 10f;
    /**
     * 按钮文本大小
     */
    public static final float TEXT_SIZE_BUTTON = 14f;
    /**
     * 元素间标准间距
     */
    public static final int SPACING_STANDARD = 12;

    // ==================== 间距配置 ====================
    /**
     * 元素间小间距
     */
    public static final int SPACING_SMALL = 8;
    /**
     * 元素间大间距
     */
    public static final int SPACING_LARGE = 20;
    /**
     * 卡片间距
     */
    public static final int CARD_SPACING = 16;
    /**
     * 卡片圆角半径 - Material 3 大圆角
     */
    public static final float CARD_CORNER_RADIUS = 16f;
    /**
     * 小卡片圆角半径
     */
    public static final float CARD_CORNER_RADIUS_SMALL = 12f;
    /**
     * 按钮圆角半径 - Material 3 全圆角
     */
    public static final float BUTTON_CORNER_RADIUS = 20f;
    /**
     * 小按钮圆角半径
     */
    public static final float BUTTON_CORNER_RADIUS_SMALL = 8f;
    /**
     * 输入框圆角半径
     */
    public static final float INPUT_CORNER_RADIUS = 8f;
    /**
     * 卡片阴影高度
     */
    public static final float CARD_ELEVATION = 4f;

    // ==================== 阴影配置 ====================
    /**
     * 按钮阴影高度
     */
    public static final float BUTTON_ELEVATION = 2f;
    /**
     * 悬停阴影高度
     */
    public static final float HOVER_ELEVATION = 8f;

    // 私有构造函数，防止实例化
    private ThemeManager() {
    }

    // ==================== 工具方法 ====================

    /**
     * 根据状态获取颜色
     *
     * @param isError 是否为错误状态
     * @return 对应的颜色
     */
    public static int getStatusColor(boolean isError) {
        return isError ? ERROR_COLOR : SUCCESS_COLOR;
    }

    /**
     * 获取带透明度的颜色
     *
     * @param color 原始颜色
     * @param alpha 透明度 (0-255)
     * @return 调整后的颜色
     */
    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    /**
     * 创建渐变卡片背景
     *
     * @param context 上下文
     * @return 渐变背景
     */
    public static GradientDrawable createCardBackground(View view) {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{CARD_GRADIENT_START, CARD_GRADIENT_END}
        );
        drawable.setCornerRadius(dp(view, CARD_CORNER_RADIUS));
        drawable.setStroke(dp(view, 1), CARD_BORDER_COLOR);
        return drawable;
    }

    /**
     * 创建纯色卡片背景
     *
     * @param context 上下文
     * @return 纯色背景
     */
    public static GradientDrawable createSolidCardBackground(View view) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(CARD_BACKGROUND_COLOR);
        drawable.setCornerRadius(dp(view, CARD_CORNER_RADIUS));
        drawable.setStroke(dp(view, 1), CARD_BORDER_COLOR);
        return drawable;
    }

    /**
     * 创建主按钮背景（带渐变和水波纹）
     *
     * @param view 视图
     * @return 按钮背景
     */
    public static Drawable createPrimaryButtonBackground(View view) {
        // 正常状态 - 渐变
        GradientDrawable normalDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{BUTTON_GRADIENT_START, BUTTON_GRADIENT_END}
        );
        normalDrawable.setCornerRadius(dp(view, BUTTON_CORNER_RADIUS));

        // 按下状态 - 深色
        GradientDrawable pressedDrawable = new GradientDrawable();
        pressedDrawable.setColor(PRIMARY_DARK_COLOR);
        pressedDrawable.setCornerRadius(dp(view, BUTTON_CORNER_RADIUS));

        // 创建 StateListDrawable
        StateListDrawable stateList = new StateListDrawable();
        stateList.addState(StateSet.get(StateSet.VIEW_STATE_PRESSED), pressedDrawable);
        stateList.addState(StateSet.get(StateSet.VIEW_STATE_HOVERED), pressedDrawable);
        stateList.addState(StateSet.WILD_CARD, normalDrawable);

        return stateList;
    }

    /**
     * 创建次要按钮背景
     *
     * @param view 视图
     * @return 按钮背景
     */
    public static Drawable createSecondaryButtonBackground(View view) {
        // 正常状态
        GradientDrawable normalDrawable = new GradientDrawable();
        normalDrawable.setColor(SURFACE_COLOR);
        normalDrawable.setCornerRadius(dp(view, BUTTON_CORNER_RADIUS));
        normalDrawable.setStroke(dp(view, 1), PRIMARY_COLOR);

        // 按下状态
        GradientDrawable pressedDrawable = new GradientDrawable();
        pressedDrawable.setColor(withAlpha(PRIMARY_COLOR, 30));
        pressedDrawable.setCornerRadius(dp(view, BUTTON_CORNER_RADIUS));
        pressedDrawable.setStroke(dp(view, 1), PRIMARY_COLOR);

        StateListDrawable stateList = new StateListDrawable();
        stateList.addState(StateSet.get(StateSet.VIEW_STATE_PRESSED), pressedDrawable);
        stateList.addState(StateSet.get(StateSet.VIEW_STATE_HOVERED), pressedDrawable);
        stateList.addState(StateSet.WILD_CARD, normalDrawable);

        return stateList;
    }

    /**
     * 创建标签按钮背景
     *
     * @param view     视图
     * @param isActive 是否激活
     * @return 按钮背景
     */
    public static Drawable createTabButtonBackground(View view, boolean isActive) {
        GradientDrawable drawable = new GradientDrawable();
        if (isActive) {
            drawable.setColors(new int[]{TAB_ACTIVE_GRADIENT_START, TAB_ACTIVE_GRADIENT_END});
            drawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        } else {
            drawable.setColor(SURFACE_CONTAINER_COLOR);
        }
        drawable.setCornerRadius(dp(view, BUTTON_CORNER_RADIUS_SMALL));
        return drawable;
    }

    /**
     * 创建输入框背景
     *
     * @param view      视图
     * @param isFocused 是否聚焦
     * @return 输入框背景
     */
    public static Drawable createInputBackground(View view, boolean isFocused) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(SURFACE_CONTAINER_COLOR);
        drawable.setCornerRadius(dp(view, INPUT_CORNER_RADIUS));
        if (isFocused) {
            drawable.setStroke(dp(view, 2), PRIMARY_COLOR);
        } else {
            drawable.setStroke(dp(view, 1), withAlpha(PRIMARY_COLOR, 50));
        }
        return drawable;
    }

    /**
     * 创建状态指示器背景
     *
     * @param view    视图
     * @param isError 是否为错误状态
     * @return 状态背景
     */
    public static Drawable createStatusBackground(View view, boolean isError) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(isError ? withAlpha(ERROR_COLOR, 30) : withAlpha(SUCCESS_COLOR, 30));
        drawable.setCornerRadius(dp(view, 12));
        drawable.setStroke(dp(view, 1), isError ? ERROR_COLOR : SUCCESS_COLOR);
        return drawable;
    }

    /**
     * 创建带图标的按钮背景
     *
     * @param view 视图
     * @return 按钮背景
     */
    public static Drawable createIconButtonBackground(View view) {
        GradientDrawable normalDrawable = new GradientDrawable();
        normalDrawable.setColor(SURFACE_CONTAINER_COLOR);
        normalDrawable.setCornerRadius(dp(view, 12));

        GradientDrawable pressedDrawable = new GradientDrawable();
        pressedDrawable.setColor(SURFACE_LIGHT_COLOR);
        pressedDrawable.setCornerRadius(dp(view, 12));

        StateListDrawable stateList = new StateListDrawable();
        stateList.addState(StateSet.get(StateSet.VIEW_STATE_PRESSED), pressedDrawable);
        stateList.addState(StateSet.get(StateSet.VIEW_STATE_HOVERED), pressedDrawable);
        stateList.addState(StateSet.WILD_CARD, normalDrawable);

        return stateList;
    }

    /**
     * 创建分隔线
     *
     * @param view 视图
     * @return 分隔线
     */
    public static Drawable createDivider(View view) {
        ShapeDrawable drawable = new ShapeDrawable();
        drawable.setShape(ShapeDrawable.HLINE);
        drawable.setColor(DIVIDER_COLOR);
        drawable.setSize(dp(view, 1), dp(view, 1));
        return drawable;
    }

    /**
     * dp 转 px 工具方法
     *
     * @param view    视图
     * @param dpValue dp值
     * @return px值
     */
    public static int dp(View view, float dpValue) {
        return (int) (dpValue * view.getContext().getResources().getDisplayMetrics().density);
    }

    /**
     * 创建水波纹效果（如果支持）
     *
     * @param view  视图
     * @param color 水波纹颜色
     * @return 带水波纹的背景
     */
    public static Drawable createRippleBackground(View view, int color, Drawable content) {
        return new RippleDrawable(ColorStateList.valueOf(withAlpha(color, 50)), content, null);
    }
}
