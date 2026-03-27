package neoproxy.neolinkmc.gui.components;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.GradientDrawable;
import icyllis.modernui.graphics.drawable.StateListDrawable;
import icyllis.modernui.util.StateSet;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import neoproxy.neolinkmc.gui.core.ThemeManager;

/**
 * 卡片式分区组件 - Material Design 3 风格
 * 将相关内容组织在带标题的卡片中，支持渐变背景和悬停效果
 *
 * @author NeoProxy Team
 * @version 5.11.2
 */
public class SectionCard extends LinearLayout {

    private final TextView titleView;
    private final LinearLayout contentLayout;
    private final Context mContext;
    private final GradientDrawable backgroundDrawable;

    /**
     * 创建分区卡片
     *
     * @param context 上下文
     * @param title   分区标题
     */
    public SectionCard(Context context, String title) {
        super(context);
        this.mContext = context;

        // 设置整体布局方向为垂直
        setOrientation(LinearLayout.VERTICAL);

        // 设置卡片背景 - 使用渐变效果
        backgroundDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{ThemeManager.CARD_GRADIENT_START, ThemeManager.CARD_GRADIENT_END}
        );
        backgroundDrawable.setCornerRadius(dp(ThemeManager.CARD_CORNER_RADIUS));
        backgroundDrawable.setStroke(dp(1), ThemeManager.CARD_BORDER_COLOR);
        setBackground(backgroundDrawable);

        // 设置内边距 - Material 3 规范
        int padding = dp(ThemeManager.PADDING_STANDARD);
        setPadding(padding, padding, padding, padding);

        // 创建标题区域
        LinearLayout titleArea = new LinearLayout(context);
        titleArea.setOrientation(LinearLayout.HORIZONTAL);
        titleArea.setGravity(Gravity.CENTER_VERTICAL);

        // 创建标题
        if (title != null && !title.isEmpty()) {
            titleView = new TextView(context);
            titleView.setText(title);
            titleView.setTextColor(ThemeManager.PRIMARY_COLOR);
            titleView.setTextSize(ThemeManager.TEXT_SIZE_CARD_TITLE);
            titleView.setTextStyle(icyllis.modernui.text.Typeface.BOLD);

            // 标题底部间距
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            titleParams.bottomMargin = dp(ThemeManager.SPACING_STANDARD);
            titleArea.addView(titleView, titleParams);

            // 添加装饰线
            View divider = new View(context);
            GradientDrawable dividerBg = new GradientDrawable();
            dividerBg.setColor(ThemeManager.PRIMARY_COLOR);
            dividerBg.setCornerRadius(dp(1));
            divider.setBackground(dividerBg);
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    dp(24), dp(3)
            );
            dividerParams.leftMargin = dp(8);
            titleArea.addView(divider, dividerParams);
        } else {
            titleView = null;
        }

        addView(titleArea);

        // 创建内容容器
        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        addView(contentLayout);

        // 设置布局参数 - 底部间距
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(ThemeManager.CARD_SPACING);
        setLayoutParams(params);

        // 添加悬停效果
        setupHoverEffect();
    }

    /**
     * 创建无标题的分区卡片
     *
     * @param context 上下文
     */
    public SectionCard(Context context) {
        this(context, null);
    }

    /**
     * 设置悬停效果
     */
    private void setupHoverEffect() {
        // 创建悬停状态的背景
        GradientDrawable hoverDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{ThemeManager.SURFACE_LIGHT_COLOR, ThemeManager.CARD_GRADIENT_END}
        );
        hoverDrawable.setCornerRadius(dp(ThemeManager.CARD_CORNER_RADIUS));
        hoverDrawable.setStroke(dp(1), ThemeManager.withAlpha(ThemeManager.PRIMARY_COLOR, 100));

        StateListDrawable stateList = new StateListDrawable();
        stateList.addState(StateSet.get(StateSet.VIEW_STATE_HOVERED), hoverDrawable);
        stateList.addState(StateSet.WILD_CARD, backgroundDrawable);

        setBackground(stateList);
    }

    /**
     * 向卡片内容区添加视图
     *
     * @param view 要添加的视图
     */
    public void addContentView(View view) {
        // 为内容添加顶部间距（除了第一个）
        if (contentLayout.getChildCount() > 0) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.topMargin = dp(ThemeManager.SPACING_SMALL);
            view.setLayoutParams(params);
        }
        contentLayout.addView(view);
    }

    /**
     * 向卡片内容区添加视图（带布局参数）
     *
     * @param view   要添加的视图
     * @param params 布局参数
     */
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        contentLayout.addView(view, params);
    }

    /**
     * 设置分区标题
     *
     * @param title 标题文本
     */
    public void setTitle(String title) {
        if (titleView != null) {
            titleView.setText(title);
            titleView.setVisibility(title.isEmpty() ? GONE : VISIBLE);
        }
    }

    /**
     * 获取标题视图
     *
     * @return 标题视图
     */
    public TextView getTitleView() {
        return titleView;
    }

    /**
     * 获取内容容器
     *
     * @return 内容布局
     */
    public LinearLayout getContentLayout() {
        return contentLayout;
    }

    /**
     * 设置卡片背景颜色
     *
     * @param color 颜色值
     */
    public void setCardBackgroundColor(int color) {
        backgroundDrawable.setColor(color);
    }

    /**
     * 设置卡片边框颜色
     *
     * @param color 边框颜色
     */
    public void setCardBorderColor(int color) {
        backgroundDrawable.setStroke(dp(1), color);
    }
}
