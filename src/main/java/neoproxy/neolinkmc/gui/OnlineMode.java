package neoproxy.neolinkmc.gui;

import net.minecraft.network.chat.Component;

/**
 * 在线模式枚举 - 26.1 版本
 * 合并正版验证和UUID修复为一个三态选项
 * 与 LanServerProperties 保持一致
 *
 * @author NeoProxy Team
 * @version 0.0.1
 */
public enum OnlineMode {
    /**
     * 正版验证开启，使用正版UUID
     */
    ONLINE_ONLINE_UUID_ONLY(true, false, "on"),

    /**
     * 正版验证关闭，但优先尝试获取正版UUID（UUID修复开启）
     */
    OFFLINE_TRY_ONLINE_UUID_FIRST(false, true, "off.fixed"),

    /**
     * 正版验证关闭，使用离线UUID（纯离线模式）
     */
    OFFLINE_OFFLINE_UUID_ONLY(false, false, "off.vanilla");

    private static final String TRANSLATION_KEY = "neolink.gui.online_mode";
    public static final Component TRANSLATION = Component.translatable(TRANSLATION_KEY);

    /**
     * 是否启用正版验证
     */
    public final boolean onlineModeEnabled;

    /**
     * 是否优先尝试获取正版UUID（UUID修复）
     */
    public final boolean tryOnlineUUIDFirst;

    private final Component displayName;
    private final Component toolTip;

    OnlineMode(boolean onlineModeEnabled, boolean tryOnlineUUIDFirst, String key) {
        this.onlineModeEnabled = onlineModeEnabled;
        this.tryOnlineUUIDFirst = tryOnlineUUIDFirst;
        this.displayName = Component.translatable(TRANSLATION_KEY + "." + key);
        this.toolTip = Component.translatable(TRANSLATION_KEY + "." + key + ".tooltip");
    }

    /**
     * 获取显示名称 - 兼容 26.1 API
     */
    public Component getDisplayName() {
        return this.displayName;
    }

    /**
     * 获取提示文本 - 兼容 26.1 API
     */
    public Component gettoolTip() {
        return this.toolTip;
    }

    /**
     * 根据布尔值创建 OnlineMode
     *
     * @param onlineModeEnabled  是否启用正版验证
     * @param tryOnlineUUIDFirst 是否优先尝试正版UUID
     * @return 对应的 OnlineMode 枚举值
     */
    public static OnlineMode of(boolean onlineModeEnabled, boolean tryOnlineUUIDFirst) {
        if (onlineModeEnabled) {
            return ONLINE_ONLINE_UUID_ONLY;
        } else {
            return tryOnlineUUIDFirst ? OFFLINE_TRY_ONLINE_UUID_FIRST : OFFLINE_OFFLINE_UUID_ONLY;
        }
    }
}
