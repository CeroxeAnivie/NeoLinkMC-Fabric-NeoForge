package neoproxy.neolinkmc.gui.core;

import icyllis.modernui.graphics.Color;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.graphics.drawable.LayerDrawable;
import icyllis.modernui.graphics.drawable.StateListDrawable;
import icyllis.modernui.util.StateSet;
import icyllis.modernui.view.View;

/**
 * Minecraft 原版风格主题管理器
 * 完美还原原版 3D 按钮边缘、输入框及材质颜色
 */
public final class MinecraftThemeManager {

    // ==================== Minecraft 原版颜色配置 ====================
    public static final int BUTTON_NORMAL_CENTER = Color.argb(255, 198, 198, 198);
    public static final int BUTTON_HOVER_CENTER = Color.argb(255, 160, 160, 220);
    public static final int BUTTON_PRESSED_CENTER = Color.argb(255, 128, 128, 128);
    public static final int BUTTON_BORDER_OUTER = Color.argb(255, 0, 0, 0);
    public static final int BUTTON_BORDER_LIGHT = Color.argb(255, 255, 255, 255);
    public static final int BUTTON_BORDER_DARK = Color.argb(255, 85, 85, 85);
    public static final int INPUT_BACKGROUND_COLOR = Color.argb(255, 0, 0, 0);
    public static final int INPUT_BORDER_COLOR = Color.argb(255, 160, 160, 160);
    public static final int INPUT_FOCUSED_BORDER_COLOR = Color.argb(255, 255, 255, 255);
    public static final int BACKGROUND_COLOR = Color.argb(200, 16, 16, 16);
    public static final int PANEL_BACKGROUND_COLOR = Color.argb(180, 0, 0, 0);
    public static final int DIVIDER_COLOR = Color.argb(100, 255, 255, 255);
    public static final int TEXT_PRIMARY_COLOR = Color.argb(255, 255, 255, 255);
    public static final int TEXT_SECONDARY_COLOR = Color.argb(255, 170, 170, 170);
    public static final int TEXT_HINT_COLOR = Color.argb(255, 128, 128, 128);
    public static final int TEXT_DISABLED_COLOR = Color.argb(255, 100, 100, 100);
    public static final int ERROR_COLOR = Color.argb(255, 255, 85, 85);
    public static final int SUCCESS_COLOR = Color.argb(255, 85, 255, 85);
    public static final int WARNING_COLOR = Color.argb(255, 255, 255, 85);
    // ==================== 尺寸与排版常量 ====================
    public static final int PADDING_STANDARD = 8;
    public static final int PADDING_SMALL = 4;
    public static final int PADDING_LARGE = 16;
    public static final float TEXT_SIZE_TITLE = 20f;
    public static final float TEXT_SIZE_SUBTITLE = 16f;
    public static final float TEXT_SIZE_BODY = 14f;
    public static final float TEXT_SIZE_SMALL = 12f;
    public static final float TEXT_SIZE_BUTTON = 14f;
    public static final int SPACING_STANDARD = 8;
    public static final int SPACING_LARGE = 16;
    public static final int BUTTON_HEIGHT = 20;
    public static final int INPUT_HEIGHT = 20;
    public static final int BORDER_WIDTH = 2;
    private MinecraftThemeManager() {
    }

    // ==================== 绘图工具方法 ====================

    public static Drawable createMinecraftButtonBackground(View view) {
        int bw = dp(view, BORDER_WIDTH);

        // 正常状态
        LayerDrawable normalDrawable = new LayerDrawable(new Drawable[]{
                new ColorDrawable(BUTTON_BORDER_OUTER),
                new ColorDrawable(BUTTON_BORDER_DARK),
                new ColorDrawable(BUTTON_BORDER_LIGHT),
                new ColorDrawable(BUTTON_NORMAL_CENTER)
        });
        normalDrawable.setLayerInset(1, bw, bw, bw, bw);
        normalDrawable.setLayerInset(2, bw, bw, bw * 2, bw * 2);
        normalDrawable.setLayerInset(3, bw * 2, bw * 2, bw * 2, bw * 2);

        // 悬停状态
        LayerDrawable hoverDrawable = new LayerDrawable(new Drawable[]{
                new ColorDrawable(BUTTON_BORDER_OUTER),
                new ColorDrawable(BUTTON_BORDER_DARK),
                new ColorDrawable(BUTTON_BORDER_LIGHT),
                new ColorDrawable(BUTTON_HOVER_CENTER)
        });
        hoverDrawable.setLayerInset(1, bw, bw, bw, bw);
        hoverDrawable.setLayerInset(2, bw, bw, bw * 2, bw * 2);
        hoverDrawable.setLayerInset(3, bw * 2, bw * 2, bw * 2, bw * 2);

        // 按下状态
        LayerDrawable pressedDrawable = new LayerDrawable(new Drawable[]{
                new ColorDrawable(BUTTON_BORDER_OUTER),
                new ColorDrawable(BUTTON_BORDER_LIGHT),
                new ColorDrawable(BUTTON_BORDER_DARK),
                new ColorDrawable(BUTTON_PRESSED_CENTER)
        });
        pressedDrawable.setLayerInset(1, bw, bw, bw, bw);
        pressedDrawable.setLayerInset(2, bw, bw, bw * 2, bw * 2);
        pressedDrawable.setLayerInset(3, bw * 2, bw * 2, bw * 2, bw * 2);

        StateListDrawable stateList = new StateListDrawable();
        stateList.addState(StateSet.get(StateSet.VIEW_STATE_PRESSED), pressedDrawable);
        stateList.addState(StateSet.get(StateSet.VIEW_STATE_HOVERED), hoverDrawable);
        stateList.addState(StateSet.WILD_CARD, normalDrawable);
        return stateList;
    }

    public static Drawable createMinecraftInputBackground(View view, boolean isFocused) {
        int bw = dp(view, BORDER_WIDTH);
        int borderColor = isFocused ? INPUT_FOCUSED_BORDER_COLOR : INPUT_BORDER_COLOR;
        LayerDrawable drawable = new LayerDrawable(new Drawable[]{
                new ColorDrawable(borderColor),
                new ColorDrawable(INPUT_BACKGROUND_COLOR)
        });
        drawable.setLayerInset(1, bw, bw, bw, bw);
        return drawable;
    }

    public static Drawable createMinecraftPrimaryButtonBackground(View view) {
        return createMinecraftButtonBackground(view);
    }

    public static int dp(View view, int dp) {
        return (int) (dp * view.getContext().getResources().getDisplayMetrics().density);
    }
}