package neoproxy.neolinkmc.service.thread;

import fun.ceroxe.api.utils.Sleeper;
import neoproxy.neolinkmc.service.MessageHandler;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

/**
 * 心跳检测任务 - 重构版本
 * <p>
 * 核心职责：
 * 1. 定期向服务器发送心跳包，保持连接活跃
 * 2. 检测连接状态，识别连接中断
 * 3. 连续失败达到阈值时自动关闭连接
 * <p>
 * 设计特点：
 * - 去除静态单例，改为实例化使用
 * - 通过构造函数注入依赖（时间获取器、心跳发送器、消息处理器）
 * - 使用原子变量保证线程安全
 * - 守护线程，不阻止 JVM 退出
 *
 * @author NeoProxy Team
 * @version 2.0.0
 */
public final class CheckAliveTask implements Runnable {

    private static final String HEARTBEAT_PACKET = "PING";
    private static final int MAX_CONSECUTIVE_FAILURES = 5;
    private static final int HEARTBEAT_INTERVAL_MS = 1000;
    private static final long HEARTBEAT_THRESHOLD_MS = 2000;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final LongSupplier lastReceivedTimeSupplier;
    private final HeartbeatSender heartbeatSender;
    private final MessageHandler messageHandler;
    private Thread heartbeatThread;

    /**
     * 心跳发送器函数式接口
     */
    @FunctionalInterface
    public interface HeartbeatSender {
        void send() throws IOException;
    }

    public CheckAliveTask(LongSupplier lastReceivedTimeSupplier,
                          HeartbeatSender heartbeatSender,
                          MessageHandler messageHandler) {
        this.lastReceivedTimeSupplier = lastReceivedTimeSupplier;
        this.heartbeatSender = heartbeatSender;
        this.messageHandler = messageHandler;
    }

    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            heartbeatThread = new Thread(this, "NeoLink-Heartbeat");
            heartbeatThread.setDaemon(true);
            heartbeatThread.start();
            messageHandler.log("心跳线程已启动", MessageHandler.LogLevel.DEBUG);
        }
    }

    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            if (heartbeatThread != null) {
                heartbeatThread.interrupt();
            }
            messageHandler.log("心跳线程已停止", MessageHandler.LogLevel.DEBUG);
        }
    }

    @Override
    public void run() {
        AtomicInteger failureCount = new AtomicInteger(0);

        while (isRunning.get() && !Thread.currentThread().isInterrupted()) {
            long timeSinceLastRecv = System.currentTimeMillis() - lastReceivedTimeSupplier.getAsLong();

            if (timeSinceLastRecv > HEARTBEAT_THRESHOLD_MS) {
                try {
                    heartbeatSender.send();
                    failureCount.set(0);
                } catch (IOException e) {
                    int failures = failureCount.incrementAndGet();
                    messageHandler.log("心跳发送失败 (" + failures + "/" + MAX_CONSECUTIVE_FAILURES + "): " + e.getMessage(),
                            MessageHandler.LogLevel.WARN);

                    if (failures >= MAX_CONSECUTIVE_FAILURES) {
                        messageHandler.send("连接已断开，心跳超时", MessageHandler.MessageType.ERROR);
                        stop();
                    }
                }
            }

            Sleeper.sleep(HEARTBEAT_INTERVAL_MS);
        }
    }
}
