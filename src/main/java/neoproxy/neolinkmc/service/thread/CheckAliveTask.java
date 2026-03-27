package neoproxy.neolinkmc.service.thread;

import fun.ceroxe.api.utils.Sleeper;
import neoproxy.neolinkmc.NeoLinkMC;
import neoproxy.neolinkmc.service.ConnectionService;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 心跳检测任务
 * <p>
 * 核心职责：
 * 1. 定期向服务器发送心跳包，保持连接活跃
 * 2. 检测连接状态，识别连接中断
 * 3. 连续失败达到阈值时自动关闭连接
 * <p>
 * 设计特点：
 * - 与 ConnectionService 关联，不依赖静态字段
 * - 使用原子变量保证线程安全
 * - 守护线程，不阻止 JVM 退出
 * - 可安全启动和停止
 * - 通过 ConnectionService 的封装方法访问资源
 *
 * @author NeoProxy Team
 * @version 1.0.0
 */
public final class CheckAliveTask implements Runnable {

    private static final String HEARTBEAT_PACKET = "PING";
    private static final int MAX_CONSECUTIVE_FAILURES = 5;
    private static final int HEARTBEAT_PACKET_DELAY = 1000;
    private static final long HEARTBEAT_THRESHOLD_MS = 2000;

    private static volatile CheckAliveTask instance;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private Thread heartbeatThread;
    private ConnectionService service;

    private CheckAliveTask() {
    }

    private static CheckAliveTask getInstance() {
        if (instance == null) {
            synchronized (CheckAliveTask.class) {
                if (instance == null) instance = new CheckAliveTask();
            }
        }
        return instance;
    }

    public static void start(ConnectionService service) {
        NeoLinkMC.LOGGER.debug("[CheckAliveTask] start() 被调用");
        CheckAliveTask task = getInstance();
        task.service = service;
        task.startInternal();
    }

    public static void stop() {
        NeoLinkMC.LOGGER.debug("[CheckAliveTask] stop() 被调用");
        if (instance != null) {
            instance.stopInternal();
        }
    }

    private void startInternal() {
        if (isRunning.compareAndSet(false, true)) {
            heartbeatThread = new Thread(this, "NeoLink-Heartbeat");
            heartbeatThread.setDaemon(true);
            heartbeatThread.start();
            NeoLinkMC.LOGGER.debug("[CheckAliveTask] 心跳线程已启动");
        }
    }

    private void stopInternal() {
        if (isRunning.compareAndSet(true, false)) {
            if (heartbeatThread != null) {
                heartbeatThread.interrupt();
            }
        }
    }

    @Override
    public void run() {
        AtomicInteger failureCount = new AtomicInteger(0);

        while (isRunning.get() && !Thread.currentThread().isInterrupted()) {
            if (service == null || !service.isHookSocketAvailable()) {
                Sleeper.sleep(HEARTBEAT_PACKET_DELAY);
                continue;
            }

            long timeSinceLastRecv = System.currentTimeMillis() - service.getLastReceivedTime();

            if (timeSinceLastRecv > HEARTBEAT_THRESHOLD_MS) {
                try {
                    synchronized (service.getHookSocketLock()) {
                        service.sendHeartbeat();
                    }
                    failureCount.set(0);
                } catch (Exception e) {
                    int currentFailures = failureCount.incrementAndGet();
                    NeoLinkMC.LOGGER.debug("[CheckAliveTask] 心跳发送失败，次数: {}/{}", currentFailures, MAX_CONSECUTIVE_FAILURES);

                    if (currentFailures >= MAX_CONSECUTIVE_FAILURES) {
                        NeoLinkMC.LOGGER.warn("[CheckAliveTask] 达到最大失败次数，关闭连接");
                        service.closeHookSocket();
                        stopInternal();
                        break;
                    }
                }
            } else {
                failureCount.set(0);
            }

            Sleeper.sleep(HEARTBEAT_PACKET_DELAY);
        }
    }
}
