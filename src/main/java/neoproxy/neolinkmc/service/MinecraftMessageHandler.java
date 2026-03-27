package neoproxy.neolinkmc.service;

import neoproxy.neolinkmc.NeoLinkMC;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Minecraft 消息处理器实现
 * <p>
 * 实现 MessageHandler 接口，负责：
 * 1. 向 Minecraft 聊天栏发送消息
 * 2. 向 NeoLinkMC 日志系统记录日志
 * <p>
 * 设计特点：
 * - 线程安全：所有方法都可以从任何线程调用
 * - 消息格式化：自动添加 [NeoLinkMC] 前缀和颜色代码
 * <p>
 * 注意：消息格式必须与老代码保持一致，确保玩家体验一致
 *
 * @author NeoProxy Team
 * @version 1.0.0
 */
public final class MinecraftMessageHandler implements MessageHandler {

    // 使用与老代码完全一致的格式
    private static final String PREFIX = "[NeoLinkMC] ";

    @Override
    public void sendMessage(@NotNull String message) {
        sendToChat("§b" + PREFIX + message);
        info(message);
    }

    @Override
    public void sendSuccess(@NotNull String message) {
        // 老代码格式：§a[NeoLinkMC] + 消息
        sendToChat("§a" + PREFIX + message);
        info("[SUCCESS] " + message);
    }

    @Override
    public void sendWarning(@NotNull String message) {
        // 老代码格式：§e[NeoLinkMC] + 消息
        sendToChat("§e" + PREFIX + message);
        warn(message);
    }

    @Override
    public void sendError(@NotNull String message) {
        // 老代码格式：§c[NeoLinkMC] §f + 消息 或 §c[NeoLinkMC] + 消息
        sendToChat("§c" + PREFIX + "§f" + message);
        error(message);
    }

    @Override
    public void debug(@NotNull String message) {
        NeoLinkMC.LOGGER.debug("[DEBUG] {}", message);
    }

    @Override
    public void info(@NotNull String message) {
        // 与老代码 say() 方法保持一致，添加 [NeoLink] 前缀
        NeoLinkMC.LOGGER.info("[NeoLink] " + message);
    }

    @Override
    public void warn(@NotNull String message) {
        NeoLinkMC.LOGGER.warn(message);
    }

    @Override
    public void error(@NotNull String message) {
        NeoLinkMC.LOGGER.error(message);
    }

    @Override
    public void error(@NotNull String message, @NotNull Throwable throwable) {
        NeoLinkMC.LOGGER.error(message, throwable);
    }

    /**
     * 发送消息到 Minecraft 聊天栏
     * 与老代码 ConnectionService.sendToMinecraftChat() 逻辑完全一致
     */
    private void sendToChat(String message) {
        try {
            Minecraft client = Minecraft.getInstance();
            if (client != null) {
                client.execute(() -> {
                    if (client.player != null) {
                        client.player.displayClientMessage(Component.literal(message), false);
                    }
                });
            }
        } catch (Exception e) {
            NeoLinkMC.LOGGER.debug("Failed to send chat message: {}", e.getMessage());
        }
    }
}
