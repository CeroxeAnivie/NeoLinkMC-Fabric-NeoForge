package neoproxy.neolinkmc.service.thread;

import fun.ceroxe.api.utils.Sleeper;
import neoproxy.neolinkmc.NeoLinkMC;
import neoproxy.neolinkmc.service.ConnectionService;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static neoproxy.neolinkmc.network.InternetOperator.close;

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
 *
 * @author NeoProxy Team
 * @version 0.0.1
 */
public final class CheckAliveTask implements Runnable {

    private static final String HEARTBEAT_PACKET = "PING";
    private static final int MAX_CONSECUTIVE_FAILURES = 5;
    private static final int HEARTBEAT_PACKET_DELAY = 1000;

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
        NeoLinkMC.LOGGER.debug("[DEBUG] CheckAliveTask.start() 被调用");
        CheckAliveTask task = getInstance();
        task.service = service;
        NeoLinkMC.LOGGER.debug("[DEBUG] CheckAliveTask 服务实例已设置");
        task.start();
    }

    public static void stop() {
        NeoLinkMC.LOGGER.debug("[DEBUG] CheckAliveTask.stop() 被调用");
        if (instance != null) {
            instance.stopInternal();
        } else {
            NeoLinkMC.LOGGER.debug("[DEBUG] CheckAliveTask 实例为 null，无需停止");
        }
    }

    private void start() {
        NeoLinkMC.LOGGER.debug("[DEBUG] CheckAliveTask.start() 内部方法开始执行");
        if (isRunning.compareAndSet(false, true)) {
            NeoLinkMC.LOGGER.debug("[DEBUG] 状态从 false 设置为 true，准备启动心跳线程...");
            heartbeatThread = new Thread(this, "NeoLink-Heartbeat");
            heartbeatThread.setDaemon(true);
            NeoLinkMC.LOGGER.debug("[DEBUG] 心跳线程创建完成，设置为守护线程");
            heartbeatThread.start();
            NeoLinkMC.LOGGER.debug("[DEBUG] 心跳线程已启动");
            NeoLinkMC.LOGGER.debug("[DEBUG] CheckAliveTask started.");
        } else {
            NeoLinkMC.LOGGER.debug("[DEBUG] CheckAliveTask 已在运行中，跳过启动");
        }
    }

    private void stopInternal() {
        NeoLinkMC.LOGGER.debug("[DEBUG] CheckAliveTask.stopInternal() 方法开始执行");
        if (isRunning.compareAndSet(true, false)) {
            NeoLinkMC.LOGGER.debug("[DEBUG] 状态从 true 设置为 false，准备停止心跳线程...");
            if (heartbeatThread != null) {
                heartbeatThread.interrupt();
                NeoLinkMC.LOGGER.debug("[DEBUG] 心跳线程已中断");
            } else {
                NeoLinkMC.LOGGER.debug("[DEBUG] 心跳线程为 null，无需中断");
            }
        } else {
            NeoLinkMC.LOGGER.debug("[DEBUG] CheckAliveTask 未在运行中，无需停止");
        }
        NeoLinkMC.LOGGER.debug("[DEBUG] Stopping CheckAliveTask...");
    }

    @Override
    public void run() {
        NeoLinkMC.LOGGER.debug("[DEBUG] CheckAliveTask.run() 方法开始执行");
        AtomicInteger failureCount = new AtomicInteger(0);
        int loopCount = 0;
        NeoLinkMC.LOGGER.debug("[DEBUG] CheckAliveTask loop started.");
        NeoLinkMC.LOGGER.debug("[DEBUG] 心跳检测循环开始，心跳包延迟: {} ms", HEARTBEAT_PACKET_DELAY);

        while (isRunning.get() && !Thread.currentThread().isInterrupted()) {
            loopCount++;
            NeoLinkMC.LOGGER.debug("[DEBUG] 心跳检测循环 #{} 开始", loopCount);

            if (service == null || service.hookSocket == null) {
                NeoLinkMC.LOGGER.debug("[DEBUG] 服务或 Socket 为 null，等待 {} ms 后重试", HEARTBEAT_PACKET_DELAY);
                Sleeper.sleep(HEARTBEAT_PACKET_DELAY);
                continue;
            }

            long timeSinceLastRecv = System.currentTimeMillis() - service.lastReceivedTime;
            NeoLinkMC.LOGGER.debug("[DEBUG] 距离上次接收数据时间: {} ms", timeSinceLastRecv);

            if (timeSinceLastRecv > 2000) {
                NeoLinkMC.LOGGER.debug("[DEBUG] 超过2秒未收到数据，发送心跳包...");
                try {
                    synchronized (service.hookSocket) {
                        NeoLinkMC.LOGGER.debug("[DEBUG] 获取到 hookSocket 锁，发送心跳包: {}", HEARTBEAT_PACKET);
                        service.hookSocket.sendStr(HEARTBEAT_PACKET);
                        NeoLinkMC.LOGGER.debug("[DEBUG] 心跳包发送成功");
                    }
                    int oldFailures = failureCount.getAndSet(0);
                    if (oldFailures > 0) {
                        NeoLinkMC.LOGGER.debug("[DEBUG] 心跳发送成功，重置失败计数（之前: {}）", oldFailures);
                    }
                } catch (Exception e) {
                    int currentFailures = failureCount.incrementAndGet();
                    NeoLinkMC.LOGGER.debug("[DEBUG] 心跳发送失败，当前失败次数: {}/{}", currentFailures, MAX_CONSECUTIVE_FAILURES);

                    if (currentFailures >= MAX_CONSECUTIVE_FAILURES) {
                        NeoLinkMC.LOGGER.debug("[DEBUG] 达到最大失败次数，关闭 Socket 并停止心跳任务");
                        NeoLinkMC.LOGGER.debug("[DEBUG] Max heartbeat failures reached. Closing socket.");
                        close(service.hookSocket);
                        stopInternal();
                        break;
                    }
                }
            } else {
                NeoLinkMC.LOGGER.debug("[DEBUG] 2秒内收到过数据，无需发送心跳包");
                int oldFailures = failureCount.getAndSet(0);
                if (oldFailures > 0) {
                    NeoLinkMC.LOGGER.debug("[DEBUG] 重置失败计数（之前: {}）", oldFailures);
                }
            }

            NeoLinkMC.LOGGER.debug("[DEBUG] 心跳检测循环 #{} 结束，休眠 {} ms", loopCount, HEARTBEAT_PACKET_DELAY);
            Sleeper.sleep(HEARTBEAT_PACKET_DELAY);
        }
        NeoLinkMC.LOGGER.debug("[DEBUG] 心跳检测循环结束，共执行 {} 次循环", loopCount);
        NeoLinkMC.LOGGER.debug("[DEBUG] CheckAliveTask finished.");
    }
}
