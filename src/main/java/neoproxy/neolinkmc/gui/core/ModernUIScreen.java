package neoproxy.neolinkmc.gui.core;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.mc.ScreenCallback;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * ModernUI 屏幕 Fragment 基类
 * 提供统一的 ModernUI 屏幕实现基础
 *
 * @author NeoProxy Team
 * @version 5.11.2
 */
public abstract class ModernUIScreen extends Fragment implements ScreenCallback {

    protected Screen lastScreen;

    /**
     * 创建 ModernUI 屏幕
     *
     * @param title      屏幕标题
     * @param lastScreen 上一个屏幕（用于返回）
     */
    protected ModernUIScreen(Component title, Screen lastScreen) {
        this.lastScreen = lastScreen;
    }

    /**
     * 返回上一个屏幕
     */
    protected void goBack() {
        if (lastScreen != null) {
            net.minecraft.client.Minecraft.getInstance().setScreen(lastScreen);
        }
    }

    /**
     * 关闭所有界面，直接返回到游戏画面
     * 用于启动服务成功后直接回到游戏
     */
    protected void closeAllScreens() {
        net.minecraft.client.Minecraft.getInstance().setScreen(null);
    }

    @Override
    public abstract View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                      @Nullable DataSet savedInstanceState);

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldBlurBackground() {
        return true;
    }
}
