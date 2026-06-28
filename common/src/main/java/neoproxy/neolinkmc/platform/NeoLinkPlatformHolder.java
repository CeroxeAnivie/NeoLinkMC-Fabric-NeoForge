package neoproxy.neolinkmc.platform;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Process-wide platform registry used by shared code.
 *
 * <p>Minecraft loaders initialize mods very early and with different entrypoint
 * types. A tiny explicit holder keeps common code deterministic: every platform
 * must register before config or lifecycle code runs, and failures are reported
 * at the real source instead of being hidden behind a null path later.</p>
 */
public final class NeoLinkPlatformHolder {
    private static volatile NeoLinkPlatform platform;

    private NeoLinkPlatformHolder() {
    }

    public static void register(NeoLinkPlatform nextPlatform) {
        platform = Objects.requireNonNull(nextPlatform, "nextPlatform");
    }

    public static NeoLinkPlatform get() {
        NeoLinkPlatform current = platform;
        if (current == null) {
            throw new IllegalStateException("NeoLinkMC platform has not been registered.");
        }
        return current;
    }

    public static Path configDir() {
        return get().configDir();
    }

    public static String version() {
        return get().version();
    }
}
