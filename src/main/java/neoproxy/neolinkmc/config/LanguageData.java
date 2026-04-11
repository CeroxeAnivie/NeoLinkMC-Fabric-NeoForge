package neoproxy.neolinkmc.config;

import java.util.HashMap;
import java.util.Map;

/**
 * 语言数据类
 * <p>
 * 存储多语言消息
 */
public final class LanguageData {

    // 常量字段（供直接访问）
    public final String VERSION = "版本: ";
    public final String CONNECT_TO = "正在连接到 ";
    public final String OMITTED = "...";
    public final String FAIL_TO_BUILD_A_CHANNEL_FROM = "无法建立到服务器的通道: ";
    public final String NO_FLOW_LEFT = "流量已用完";
    public final String BUILD_UP = " 建立";
    public final String DESTROY = " 断开";
    public final String A_TCP_CONNECTION = "TCP连接 ";
    public final String FAIL_TO_CONNECT_LOCALHOST = "无法连接到本地服务器: ";
    public final String SERVICE_STOPPED = "服务已停止";

    private final Map<String, String> messages = new HashMap<>();

    public LanguageData() {
        // 初始化默认消息
        messages.put("connection.success", "连接成功");
        messages.put("connection.failed", "连接失败");
        messages.put("connection.closed", "连接已关闭");
        messages.put("heartbeat.timeout", "心跳超时");
        messages.put("error.invalid_key", "无效的密钥");
        messages.put("error.server_unavailable", "服务器不可用");
    }

    public String get(String key) {
        return messages.getOrDefault(key, key);
    }

    public String get(String key, Object... args) {
        String message = get(key);
        return String.format(message, args);
    }
}
