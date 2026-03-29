package neoproxy.neolinkmc.network;

import fun.ceroxe.api.net.SecureSocket;

import java.io.Closeable;
import java.net.Socket;

/**
 * 网络操作器
 * <p>
 * 核心职责：
 * 1. 封装与 Neo 服务器的通信操作
 * 2. 提供安全的资源关闭方法
 * 3. 处理网络异常
 * <p>
 * 与完整版 NeoLink 项目对齐，保持简洁
 *
 * @author NeoProxy Team
 * @since 5.0.0
 */
public final class InternetOperator {

    /**
     * 安全关闭多个资源
     */
    public static void close(Closeable... closeables) {
        for (Closeable a : closeables) {
            try {
                if (a != null) {
                    a.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 关闭 SecureSocket 输入流
     */
    public static void shutdownInput(SecureSocket socket) {
        try {
            if (socket != null) {
                socket.shutdownInput();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 关闭 Socket 输入流
     */
    public static void shutdownInput(Socket socket) {
        try {
            if (socket != null) {
                socket.shutdownInput();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 关闭 SecureSocket 输出流
     */
    public static void shutdownOutput(SecureSocket socket) {
        try {
            if (socket != null) {
                socket.shutdownOutput();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 关闭 Socket 输出流
     */
    public static void shutdownOutput(Socket socket) {
        try {
            if (socket != null) {
                socket.shutdownOutput();
            }
        } catch (Exception ignored) {
        }
    }
}
