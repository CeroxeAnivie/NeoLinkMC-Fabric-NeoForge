package neoproxy.neolinkmc.service;

import fun.ceroxe.api.net.SecureSocket;
import neoproxy.neolinkmc.NeoLinkMC;

import java.io.IOException;

/**
 * 密钥验证器
 * 用于在开启 Minecraft LAN 之前预验证密钥是否正确
 */
public class KeyValidator {

    private static final String DEFAULT_KEY = "Free";

    /**
     * 预验证密钥
     *
     * @param key          用户输入的密钥（可能为空）
     * @param remoteDomain 远程服务器域名
     * @param hookPort     Hook 端口
     * @return ValidationResult 验证结果
     */
    public static ValidationResult validateKey(String key, String remoteDomain, int hookPort) {
        NeoLinkMC.LOGGER.debug("[DEBUG] KeyValidator.validateKey() 开始验证密钥");

        // 高内聚防御性编程：如果密钥为空，降级使用默认密钥
        if (key == null || key.trim().isEmpty()) {
            key = DEFAULT_KEY;
            NeoLinkMC.LOGGER.debug("[DEBUG] 密钥为空，回退使用默认密钥: {}", key);
        }
        final String finalKey = key.trim();

        try {
            NeoLinkMC.LOGGER.debug("[DEBUG] 连接到验证服务器: {}:{}", remoteDomain, hookPort);
            SecureSocket hookSocket = new SecureSocket(remoteDomain, hookPort);

            // 直接硬编码 "zh" 语言标识发送给服务端
            String clientInfo = "zh;" + NeoLinkMC.VERSION + ";" + finalKey + ";T";

            NeoLinkMC.LOGGER.debug("[DEBUG] 发送鉴权信息...");
            hookSocket.sendStr(clientInfo);

            String response = hookSocket.receiveStr();
            NeoLinkMC.LOGGER.debug("[DEBUG] 收到服务器响应: {}", response);

            hookSocket.close();

            // 检查响应是否包含拒绝/错误特征词
            if (response.contains("exit") || response.contains("退") || response.contains("错误")
                    || response.contains("denied") || response.contains("already")
                    || response.contains("过期") || response.contains("占")
                    || response.contains("密钥错误")) {
                NeoLinkMC.LOGGER.warn("密钥验证失败: {}", response);
                return new ValidationResult(false, response, remoteDomain, hookPort);
            }

            NeoLinkMC.LOGGER.info("密钥验证成功");
            return new ValidationResult(true, response, remoteDomain, hookPort);

        } catch (IOException e) {
            NeoLinkMC.LOGGER.error("密钥验证时发生IO异常", e);
            return new ValidationResult(false, "连接穿透服务器失败: " + e.getMessage(), remoteDomain, hookPort);
        } catch (Exception e) {
            NeoLinkMC.LOGGER.error("密钥验证时发生未知异常", e);
            return new ValidationResult(false, "验证过程出错: " + e.getMessage(), remoteDomain, hookPort);
        }
    }

    /**
     * 获取系统默认密钥
     */
    public static String getDefaultKey() {
        return DEFAULT_KEY;
    }

    /**
     * 验证密钥格式是否有效
     * 简单的格式验证，不连接服务器
     *
     * @param key 密钥字符串
     * @return 是否格式有效
     */
    public static boolean isValidKeyFormat(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }
        // 密钥长度应在 1-64 之间
        String trimmed = key.trim();
        return trimmed.length() >= 1 && trimmed.length() <= 64;
    }

    /**
     * 验证密钥是否有效（别名方法）
     * 简单的格式验证，不连接服务器
     *
     * @param key 密钥字符串
     * @return 是否格式有效
     */
    public static boolean isValid(String key) {
        return isValidKeyFormat(key);
    }

    /**
     * 验证结果记录类
     */
    public record ValidationResult(boolean success, String message, String remoteDomain, int hookPort) {
    }
}