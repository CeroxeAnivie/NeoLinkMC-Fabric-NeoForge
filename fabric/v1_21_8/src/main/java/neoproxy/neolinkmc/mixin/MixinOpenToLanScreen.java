package neoproxy.neolinkmc.mixin;

import neoproxy.neolinkmc.NeoLinkMC;
import neoproxy.neolinkmc.gui.NeoLinkConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ShareToLanScreen 的 Mixin 注入
 * 用于劫持原版的"对局域网开放"界面，替换为 Architectury API 原生 GUI
 *
 * @author NeoProxy Team
 * @version 3.0.0
 */
@Mixin(ShareToLanScreen.class)
public abstract class MixinOpenToLanScreen extends Screen {

    @Shadow
    @Final
    private Screen lastScreen;

    protected MixinOpenToLanScreen(Component title) {
        super(title);
    }

    /**
     * init 方法注入 - 直接打开 NeoLink 配置界面替代原版界面
     * 在 init 方法开头注入，直接切换到 Architectury API 原生 GUI
     */
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void neolinkmc$onInit(CallbackInfo ci) {
        NeoLinkMC.LOGGER.info("[DEBUG] MixinOpenToLanScreen.neolinkmc$onInit() 被调用，准备切换到 NeoLink 配置界面");
        try {
            // 创建并打开 Architectury API 原生配置界面
            Screen configScreen = new NeoLinkConfigScreen(lastScreen);

            // 使用 Minecraft 原生方法打开屏幕
            // 显式 null 检查替代 assert，避免生产环境 assert 被禁用导致 NPE
            if (this.minecraft == null) {
                NeoLinkMC.LOGGER.error("[NeoLinkMC] Minecraft 实例为 null，无法打开配置界面");
                return;
            }
            this.minecraft.setScreen(configScreen);

            NeoLinkMC.LOGGER.info("[DEBUG] NeoLink 配置界面已成功打开并设置");

            // 取消原版的 init 方法执行
            ci.cancel();
            NeoLinkMC.LOGGER.info("[DEBUG] 原版 ShareToLanScreen init 已取消");
        } catch (Exception e) {
            NeoLinkMC.LOGGER.error("[NeoLinkMC] 打开配置界面失败，回退到原版界面", e);
            NeoLinkMC.LOGGER.error("[DEBUG] 配置界面异常详情: {}", e.toString());
            e.printStackTrace();
            // 不取消，让原版界面继续初始化
        }
    }
}
