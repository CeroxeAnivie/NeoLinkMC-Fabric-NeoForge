package neoproxy.neolinkmc.service.thread;

import fun.ceroxe.api.net.SecureSocket;
import neoproxy.neolinkmc.NeoLinkMC;

import java.net.Socket;

import static neoproxy.neolinkmc.network.InternetOperator.*;

/**
 * TCP 数据转发器
 * <p>
 * 核心职责：
 * 1. 在本地服务和 Neo 服务器之间双向转发 TCP 数据
 * 2. 支持 Proxy Protocol v2 的剥离或透传
 * 3. 通过复用实例缓冲区减少 GC 压力
 * <p>
 * 设计特点：
 * - 双向转发：支持 Neo 到本地、本地到 Neo 两种模式
 * - 缓冲区复用：每个实例使用独立缓冲区，避免频繁分配内存
 * - Proxy Protocol v2 支持：可选剥离或透传真实客户端 IP
 * - 优雅关闭：支持中断信号，确保资源正确释放
 *
 * @author NeoProxy Team
 * @version 0.0.1
 */
public class TCPTransformer implements Runnable {

    public static final int MODE_NEO_TO_LOCAL = 0;
    public static final int MODE_LOCAL_TO_NEO = 1;
    public static final int BUFFER_LENGTH = 65535;
    // Proxy Protocol v2 的 12 字节固定签名
    private static final byte[] PPV2_SIG = new byte[]{
            (byte) 0x0D, (byte) 0x0A, (byte) 0x0D, (byte) 0x0A,
            (byte) 0x00, (byte) 0x0D, (byte) 0x0A, (byte) 0x51,
            (byte) 0x55, (byte) 0x49, (byte) 0x54, (byte) 0x0A
    };
    private final Socket plainSocket;
    private final SecureSocket secureSocket;
    private final int mode;
    private final boolean enableProxyProtocol;
    private final byte[] buffer = new byte[BUFFER_LENGTH];

    /**
     * 构造函数：用于从 Neo 服务器接收数据并转发到本地服务
     */
    public TCPTransformer(SecureSocket secureSender, Socket localReceiver, boolean enableProxyProtocol) {
        this.secureSocket = secureSender;
        this.plainSocket = localReceiver;
        this.mode = MODE_NEO_TO_LOCAL;
        this.enableProxyProtocol = enableProxyProtocol;
    }

    /**
     * 构造函数：用于从本地服务接收数据并转发到 Neo 服务器
     */
    public TCPTransformer(Socket localSender, SecureSocket secureReceiver, boolean enableProxyProtocol) {
        this.plainSocket = localSender;
        this.secureSocket = secureReceiver;
        this.mode = MODE_LOCAL_TO_NEO;
        this.enableProxyProtocol = enableProxyProtocol;
    }

    /**
     * 将本地数据转发到 Neo 服务器 (Local -> Neo)
     */
    private void transferDataToNeoServer() {
        NeoLinkMC.LOGGER.debug("[DEBUG] transferDataToNeoServer() 方法开始执行，模式: Local -> Neo");
        long totalBytes = 0;
        int packetCount = 0;

        try (var inputFromLocal = plainSocket.getInputStream()) {
            NeoLinkMC.LOGGER.debug("[DEBUG] 开始从本地Socket读取数据...");
            int bytesRead;
            while ((bytesRead = inputFromLocal.read(buffer)) != -1) {
                packetCount++;
                totalBytes += bytesRead;
                NeoLinkMC.LOGGER.debug("[DEBUG] 从本地读取数据包 #{}，大小: {} 字节，累计: {} 字节",
                        packetCount, bytesRead, totalBytes);
                secureSocket.sendByte(buffer, 0, bytesRead);
                NeoLinkMC.LOGGER.debug("[DEBUG] 数据包 #{} 已发送到Neo服务器", packetCount);
            }
            NeoLinkMC.LOGGER.debug("[DEBUG] 本地Socket输入流结束，共发送 {} 个数据包，{} 字节", packetCount, totalBytes);
            secureSocket.sendByte(null);
            NeoLinkMC.LOGGER.debug("[DEBUG] 发送结束信号到Neo服务器");
            shutdownInput(plainSocket);
        } catch (Exception e) {
            NeoLinkMC.LOGGER.debug("[DEBUG] transferDataToNeoServer() 异常: {} - {}",
                    e.getClass().getName(), e.getMessage());
            shutdownOutput(secureSocket);
            shutdownInput(plainSocket);
        }
        NeoLinkMC.LOGGER.debug("[DEBUG] transferDataToNeoServer() 方法执行完毕");
    }

    /**
     * 将 Neo 服务器数据转发到本地 (Neo -> Local)
     */
    private void transferDataToLocalServer() {
        NeoLinkMC.LOGGER.debug("[DEBUG] transferDataToLocalServer() 方法开始执行，模式: Neo -> Local");
        NeoLinkMC.LOGGER.debug("[DEBUG] Proxy Protocol 启用状态: {}", enableProxyProtocol);
        long totalBytes = 0;
        int packetCount = 0;

        try (var outputToLocal = plainSocket.getOutputStream()) {
            byte[] data;
            boolean isFirstPacket = true;

            while ((data = secureSocket.receiveByte()) != null) {
                packetCount++;
                if (data.length == 0) {
                    NeoLinkMC.LOGGER.debug("[DEBUG] 收到空数据包 #{}，跳过", packetCount);
                    continue;
                }

                NeoLinkMC.LOGGER.debug("[DEBUG] 从Neo服务器接收数据包 #{}，大小: {} 字节", packetCount, data.length);

                if (isFirstPacket) {
                    isFirstPacket = false;
                    NeoLinkMC.LOGGER.debug("[DEBUG] 处理第一个数据包，检查Proxy Protocol签名...");
                    if (isProxyProtocolV2Signature(data)) {
                        NeoLinkMC.LOGGER.debug("[DEBUG] 检测到Proxy Protocol v2签名");
                        if (this.enableProxyProtocol) {
                            NeoLinkMC.LOGGER.debug("[DEBUG] Proxy Protocol已启用，转发数据包");
                            outputToLocal.write(data);
                            totalBytes += data.length;
                        } else {
                            NeoLinkMC.LOGGER.debug("[DEBUG] Proxy Protocol已禁用，跳过签名数据包");
                            continue;
                        }
                    } else {
                        NeoLinkMC.LOGGER.debug("[DEBUG] 未检测到Proxy Protocol签名，正常转发");
                        outputToLocal.write(data);
                        totalBytes += data.length;
                    }
                } else {
                    outputToLocal.write(data);
                    totalBytes += data.length;
                }
            }
            NeoLinkMC.LOGGER.debug("[DEBUG] Neo服务器输入流结束，共接收 {} 个数据包，{} 字节", packetCount, totalBytes);
            shutdownInput(secureSocket);
            shutdownOutput(plainSocket);
        } catch (Exception e) {
            NeoLinkMC.LOGGER.debug("[DEBUG] transferDataToLocalServer() 异常: {} - {}",
                    e.getClass().getName(), e.getMessage());
            shutdownInput(secureSocket);
            shutdownOutput(plainSocket);
        }
        NeoLinkMC.LOGGER.debug("[DEBUG] transferDataToLocalServer() 方法执行完毕");
    }

    /**
     * 检查数据包是否以 Proxy Protocol v2 签名开头
     */
    private boolean isProxyProtocolV2Signature(byte[] data) {
        NeoLinkMC.LOGGER.debug("[DEBUG] isProxyProtocolV2Signature() 被调用，数据长度: {}", data != null ? data.length : 0);
        if (data == null || data.length < 12) {
            NeoLinkMC.LOGGER.debug("[DEBUG] 数据长度不足12字节，不是Proxy Protocol签名");
            return false;
        }
        for (int i = 0; i < 12; i++) {
            if (data[i] != PPV2_SIG[i]) {
                NeoLinkMC.LOGGER.debug("[DEBUG] 第 {} 字节不匹配，不是Proxy Protocol签名", i);
                return false;
            }
        }
        NeoLinkMC.LOGGER.debug("[DEBUG] 检测到Proxy Protocol v2签名");
        return true;
    }

    @Override
    public void run() {
        NeoLinkMC.LOGGER.debug("[DEBUG] TcpTransformer.run() 方法开始执行，模式: {}",
                mode == MODE_NEO_TO_LOCAL ? "NEO_TO_LOCAL" : "LOCAL_TO_NEO");
        try {
            if (mode == MODE_NEO_TO_LOCAL) {
                NeoLinkMC.LOGGER.debug("[DEBUG] 执行 Neo -> Local 数据转发");
                transferDataToLocalServer();
            } else {
                NeoLinkMC.LOGGER.debug("[DEBUG] 执行 Local -> Neo 数据转发");
                transferDataToNeoServer();
            }
        } catch (Exception e) {
            NeoLinkMC.LOGGER.debug("[DEBUG] TcpTransformer.run() 异常: {} - {}",
                    e.getClass().getName(), e.getMessage());
        } finally {
            NeoLinkMC.LOGGER.debug("[DEBUG] TcpTransformer 正在关闭Socket连接...");
            close(plainSocket, secureSocket);
            NeoLinkMC.LOGGER.debug("[DEBUG] TcpTransformer.run() 方法执行完毕");
        }
    }
}
