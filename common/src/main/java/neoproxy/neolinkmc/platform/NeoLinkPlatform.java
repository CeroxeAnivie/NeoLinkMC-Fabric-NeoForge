package neoproxy.neolinkmc.platform;

import java.nio.file.Path;

/**
 * Loader-specific services required by the shared NeoLinkMC core.
 *
 * <p>The common module owns the tunnel lifecycle, but it must not import Fabric,
 * Forge, or NeoForge classes. Each loader module provides this narrow adapter so
 * all platform APIs stay at the edges of the codebase.</p>
 */
public interface NeoLinkPlatform {
    Path configDir();

    String version();
}
