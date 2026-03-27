package neoproxy.neolinkmc.network;

import fun.ceroxe.api.net.SecureSocket;
import neoproxy.neolinkmc.NeoLinkMC;

import java.io.Closeable;
import java.net.Socket;

/**
 * 网络操作工具类
 * <p>
 * 核心职责：
 * 1. 提供安全的资源关闭方法
 * 2. 处理网络异常和调试信息
 * 3. 提供 Socket 流控制方法
 * <p>
 * 设计特点：
 * - 纯工具类，无状态
 * - 统一的异常处理
 * - 安全的资源释放
 *
 * @author NeoProxy Team
 * @version 0.0.1
 */
public class InternetOperator {

    /**
     * 安全关闭多个资源
     */
    public static void close(Closeable... closeables) {
        NeoLinkMC.LOGGER.debug("[DEBUG] InternetOperator.close() 被调用，资源数量: {}", closeables.length);
        int closedCount = 0;
        for (Closeable a : closeables) {
            try {
                if (a != null) {
                    NeoLinkMC.LOGGER.debug("[DEBUG] 关闭资源: {}", a.getClass().getSimpleName());
                    a.close();
                    closedCount++;
                    NeoLinkMC.LOGGER.debug("[DEBUG] 资源关闭成功: {}", a.getClass().getSimpleName());
                }
            } catch (Exception e) {
                NeoLinkMC.LOGGER.debug("[DEBUG] 关闭资源异常: {} - {}", a != null ? a.getClass().getSimpleName() : "null", e.getMessage());
            }
        }
        NeoLinkMC.LOGGER.debug("[DEBUG] InternetOperator.close() 执行完毕，成功关闭 {} 个资源", closedCount);
    }

    /**
     * 关闭 SecureSocket 输入流
     */
    public static void shutdownInput(SecureSocket socket) {
        try {
            if (socket != null) {
                NeoLinkMC.LOGGER.debug("[DEBUG] 关闭 SecureSocket 输入流");
                socket.shutdownInput();
                NeoLinkMC.LOGGER.debug("[DEBUG] SecureSocket 输入流关闭成功");
            }
        } catch (Exception e) {
            NeoLinkMC.LOGGER.debug("[DEBUG] 关闭 SecureSocket 输入流异常（已忽略）: {}", e.getMessage());
        }
    }

    /**
     * 关闭 Socket 输入流
     */
    public static void shutdownInput(Socket socket) {
        try {
            if (socket != null) {
                NeoLinkMC.LOGGER.debug("[DEBUG] 关闭 Socket 输入流");
                socket.shutdownInput();
                NeoLinkMC.LOGGER.debug("[DEBUG] Socket 输入流关闭成功");
            }
        } catch (Exception e) {
            NeoLinkMC.LOGGER.debug("[DEBUG] 关闭 Socket 输入流异常（已忽略）: {}", e.getMessage());
        }
    }

    /**
     * 关闭 SecureSocket 输出流
     */
    public static void shutdownOutput(SecureSocket socket) {
        try {
            if (socket != null) {
                NeoLinkMC.LOGGER.debug("[DEBUG] 关闭 SecureSocket 输出流");
                socket.shutdownOutput();
                NeoLinkMC.LOGGER.debug("[DEBUG] SecureSocket 输出流关闭成功");
            }
        } catch (Exception e) {
            NeoLinkMC.LOGGER.debug("[DEBUG] 关闭 SecureSocket 输出流异常（已忽略）: {}", e.getMessage());
        }
    }

    /**
     * 关闭 Socket 输出流
     */
    public static void shutdownOutput(Socket socket) {
        try {
            if (socket != null) {
                NeoLinkMC.LOGGER.debug("[DEBUG] 关闭 Socket 输出流");
                socket.shutdownOutput();
                NeoLinkMC.LOGGER.debug("[DEBUG] Socket 输出流关闭成功");
            }
        } catch (Exception e) {
            NeoLinkMC.LOGGER.debug("[DEBUG] 关闭 Socket 输出流异常（已忽略）: {}", e.getMessage());
        }
    }
}
