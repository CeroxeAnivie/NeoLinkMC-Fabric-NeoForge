package neoproxy.neolinkmc.config;

import java.util.Objects;

/**
 * Platform-neutral tunnel configuration passed from a Minecraft loader module
 * into the shared NeoLinkAPI adapter.
 *
 * <p>The common module deliberately owns only primitive tunnel fields. Fabric,
 * Forge, NeoForge, and future version-specific modules can each decide how to
 * load UI/config files without leaking loader APIs into the reusable tunnel
 * lifecycle.</p>
 */
public record ConnectionConfig(
        String remoteDomain,
        String localDomain,
        int hookPort,
        int hostConnectPort,
        String key,
        int localPort
) {
    public static final String DEFAULT_KEY = "Free";
    public static final String DEFAULT_LOCAL_DOMAIN = "localhost";

    public ConnectionConfig {
        remoteDomain = requireText(remoteDomain, "remoteDomain");
        localDomain = requireText(localDomain, "localDomain");
        hookPort = requirePort(hookPort, "hookPort");
        hostConnectPort = requirePort(hostConnectPort, "hostConnectPort");
        key = normalizeKey(key);
        localPort = requirePort(localPort, "localPort");
    }

    private static String requireText(String value, String fieldName) {
        String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        return normalized;
    }

    private static int requirePort(int value, String fieldName) {
        if (value < 1 || value > 65535) {
            throw new IllegalArgumentException(fieldName + " must be between 1 and 65535.");
        }
        return value;
    }

    public static String normalizeKey(String value) {
        return value == null || value.trim().isEmpty() ? DEFAULT_KEY : value.trim();
    }
}
