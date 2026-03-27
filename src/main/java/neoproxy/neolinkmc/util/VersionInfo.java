package neoproxy.neolinkmc.util;

import neoproxy.neolinkmc.NeoLinkMC;
import net.fabricmc.loader.api.FabricLoader;

/**
 * 版本信息类
 * <p>
 * 核心职责：
 * 1. 管理应用程序版本号（从 Fabric Mod 元数据读取）
 * 2. 提供版本相关的元数据
 * <p>
 * 设计特点：
 * - 版本号从 Fabric Mod 容器读取
 * - 支持开发环境和生产环境
 *
 * @author NeoProxy Team
 * @version 0.0.1
 */
public class VersionInfo {

    /**
     * 版本号，从 Fabric Mod 元数据读取
     */
    public static final String VERSION = getModVersion();

    /**
     * 作者信息
     */
    public static final String AUTHOR = "Ceroxe";

    /**
     * 获取 Mod 版本号
     */
    private static String getModVersion() {
        try {
            return FabricLoader.getInstance()
                    .getModContainer(NeoLinkMC.MOD_ID)
                    .map(container -> container.getMetadata().getVersion().getFriendlyString())
                    .orElse("Unknown");
        } catch (Exception e) {
            return "Dev-Build";
        }
    }
}
