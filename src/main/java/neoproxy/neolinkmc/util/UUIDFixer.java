package neoproxy.neolinkmc.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import neoproxy.neolinkmc.NeoLinkMC;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * UUID修复工具类
 * 用于在离线模式下获取正版玩家的UUID
 * 与 LanServerProperties 保持一致
 *
 * @author NeoProxy Team
 * @version 0.0.1
 */
public class UUIDFixer {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    /**
     * 是否优先尝试获取正版UUID
     */
    public static boolean tryOnlineFirst = false;
    /**
     * 始终使用离线UUID的玩家列表
     */
    public static List<String> alwaysOfflinePlayers = Collections.emptyList();
    /**
     * UUID修复器是否启用
     */
    private static boolean enabled = false;

    public static void enableFixer() {
        NeoLinkMC.LOGGER.debug("[DEBUG] UUIDFixer.enableFixer() 被调用");
        enabled = true;
        NeoLinkMC.LOGGER.debug("[DEBUG] UUID修复器已启用");
    }

    public static void disableFixer() {
        NeoLinkMC.LOGGER.debug("[DEBUG] UUIDFixer.disableFixer() 被调用");
        enabled = false;
        NeoLinkMC.LOGGER.debug("[DEBUG] UUID修复器已禁用");
    }

    public static boolean isEnabled() {
        boolean result = enabled;
        NeoLinkMC.LOGGER.debug("[DEBUG] UUIDFixer.isEnabled() 被调用，结果: {}", result);
        return result;
    }

    public static void setEnabled(boolean enabled) {
        NeoLinkMC.LOGGER.debug("[DEBUG] UUIDFixer.setEnabled() 被调用，新状态: {}", enabled);
        UUIDFixer.enabled = enabled;
        NeoLinkMC.LOGGER.debug("[DEBUG] UUID修复器状态已设置为: {}", enabled);
    }

    /**
     * Mixin回调入口
     * 在创建离线玩家UUID时调用
     *
     * @param playerName 玩家名称
     * @return 正版UUID（如果需要修复），null（使用默认离线UUID）
     */
    public static UUID hookEntry(String playerName) {
        NeoLinkMC.LOGGER.debug("[DEBUG] UUIDFixer.hookEntry() 被调用，玩家: {}", playerName);

        if (alwaysOfflinePlayers.contains(playerName)) {
            NeoLinkMC.LOGGER.debug("[DEBUG] 玩家在 alwaysOfflinePlayers 列表中，跳过UUID修复: {}", playerName);
            return null;
        }

        if (tryOnlineFirst) {
            NeoLinkMC.LOGGER.debug("[DEBUG] tryOnlineFirst 为 true，尝试获取正版UUID...");
            UUID result = getOfficialUUID(playerName);
            if (result != null) {
                NeoLinkMC.LOGGER.debug("[DEBUG] 成功获取正版UUID: {} -> {}", playerName, result);
            } else {
                NeoLinkMC.LOGGER.debug("[DEBUG] 获取正版UUID失败，将使用离线UUID: {}", playerName);
            }
            return result;
        }

        NeoLinkMC.LOGGER.debug("[DEBUG] tryOnlineFirst 为 false，跳过UUID修复: {}", playerName);
        return null;
    }

    /**
     * 从Mojang API获取正版UUID
     *
     * @param playerName 玩家名称
     * @return 正版UUID，如果获取失败则返回null
     */
    public static UUID getOfficialUUID(String playerName) {
        NeoLinkMC.LOGGER.debug("[DEBUG] UUIDFixer.getOfficialUUID() 被调用，玩家: {}", playerName);
        String url = "https://api.mojang.com/users/profiles/minecraft/" + playerName;
        NeoLinkMC.LOGGER.debug("[DEBUG] 请求URL: {}", url);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            NeoLinkMC.LOGGER.debug("[DEBUG] HTTP请求已构建，超时: 5秒");

            NeoLinkMC.LOGGER.debug("[DEBUG] 发送HTTP请求...");
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            NeoLinkMC.LOGGER.debug("[DEBUG] HTTP响应状态码: {}", response.statusCode());

            if (response.statusCode() == 200) {
                String json = response.body();
                NeoLinkMC.LOGGER.debug("[DEBUG] 响应体长度: {} 字节", json.length());
                if (!json.isEmpty()) {
                    NeoLinkMC.LOGGER.debug("[DEBUG] 解析JSON响应...");
                    JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                    String playerName2 = root.getAsJsonPrimitive("name").getAsString();
                    String uuidString = root.getAsJsonPrimitive("id").getAsString();
                    NeoLinkMC.LOGGER.debug("[DEBUG] API返回玩家名: {}, UUID字符串: {}", playerName2, uuidString);

                    // 解析UUID字符串
                    UUID uuid = parseUUIDFromString(uuidString);
                    NeoLinkMC.LOGGER.debug("[DEBUG] UUID字符串解析完成: {}", uuid);

                    if (playerName2.equalsIgnoreCase(playerName)) {
                        NeoLinkMC.LOGGER.debug("[DEBUG] UUID修复成功: {} -> {}", playerName, uuid);
                        return uuid;
                    } else {
                        NeoLinkMC.LOGGER.debug("[DEBUG] 玩家名不匹配: 请求={}, 返回={}", playerName, playerName2);
                    }
                } else {
                    NeoLinkMC.LOGGER.debug("[DEBUG] 响应体为空");
                }
            } else {
                NeoLinkMC.LOGGER.debug("[DEBUG] HTTP请求失败，状态码: {}", response.statusCode());
            }
        } catch (IOException | InterruptedException | JsonSyntaxException e) {
            NeoLinkMC.LOGGER.debug("[DEBUG] 获取正版UUID异常: {} - {}", e.getClass().getName(), e.getMessage());
        }

        NeoLinkMC.LOGGER.debug("[DEBUG] 获取正版UUID失败，返回 null: {}", playerName);
        return null;
    }

    /**
     * 解析Mojang API返回的UUID字符串
     * 参考: com.mojang.util.UUIDTypeAdapter.fromString(String)
     *
     * @param uuidString 无连字符的UUID字符串
     * @return UUID对象
     */
    private static UUID parseUUIDFromString(String uuidString) {
        NeoLinkMC.LOGGER.debug("[DEBUG] UUIDFixer.parseUUIDFromString() 被调用，字符串长度: {}", uuidString.length());
        long uuidMSB = Long.parseLong(uuidString.substring(0, 8), 16);
        uuidMSB <<= 32;
        uuidMSB |= Long.parseLong(uuidString.substring(8, 16), 16);
        long uuidLSB = Long.parseLong(uuidString.substring(16, 24), 16);
        uuidLSB <<= 32;
        uuidLSB |= Long.parseLong(uuidString.substring(24, 32), 16);
        UUID result = new UUID(uuidMSB, uuidLSB);
        NeoLinkMC.LOGGER.debug("[DEBUG] UUID解析完成: MSB={}, LSB={}, UUID={}", uuidMSB, uuidLSB, result);
        return result;
    }
}
