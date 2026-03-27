package neoproxy.neolinkmc.service;

import org.jetbrains.annotations.NotNull;

/**
 * 消息处理器接口
 * <p>
 * 设计原则：
 * 1. 接口隔离 - 只定义消息处理相关方法
 * 2. 依赖倒置 - ConnectionService 依赖抽象而非具体实现
 * 3. 单一职责 - 只负责消息的展示和日志记录
 * 4. 开闭原则 - 可以轻松扩展新的消息处理器实现
 * <p>
 * 使用场景：
 * - ConnectionService 通过此接口发送消息
 * - MinecraftMessageHandler 实现类负责与 Minecraft 交互
 * - 可以轻松替换为其他实现（如测试用的 Mock 实现）
 *
 * @author NeoProxy Team
 * @version 1.0.0
 */
public interface MessageHandler {

    /**
     * 发送普通消息
     *
     * @param message 消息内容
     */
    void sendMessage(@NotNull String message);

    /**
     * 发送成功消息（绿色）
     *
     * @param message 消息内容
     */
    void sendSuccess(@NotNull String message);

    /**
     * 发送警告消息（黄色）
     *
     * @param message 消息内容
     */
    void sendWarning(@NotNull String message);

    /**
     * 发送错误消息（红色）
     *
     * @param message 消息内容
     */
    void sendError(@NotNull String message);

    /**
     * 记录调试日志
     *
     * @param message 日志内容
     */
    void debug(@NotNull String message);

    /**
     * 记录信息日志
     *
     * @param message 日志内容
     */
    void info(@NotNull String message);

    /**
     * 记录警告日志
     *
     * @param message 日志内容
     */
    void warn(@NotNull String message);

    /**
     * 记录错误日志
     *
     * @param message 日志内容
     */
    void error(@NotNull String message);

    /**
     * 记录错误日志（带异常）
     *
     * @param message 日志内容
     * @param throwable 异常对象
     */
    void error(@NotNull String message, @NotNull Throwable throwable);
}
