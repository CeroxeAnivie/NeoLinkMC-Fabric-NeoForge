package neoproxy.neolinkmc.config;

/**
 * 连接配置类
 * <p>
 * 存储连接相关的配置信息
 */
public final class ConnectionConfig {

    private String remoteDomain = NeoLinkConfig.DEFAULT_REMOTE_DOMAIN;
    private String localDomain = "localhost";
    private int localPort = NeoLinkConfig.DEFAULT_LOCAL_PORT;
    private int hookPort = NeoLinkConfig.DEFAULT_HOOK_PORT;
    private int hostConnectPort = NeoLinkConfig.DEFAULT_HOST_CONNECT_PORT;
    private int connectPort = NeoLinkConfig.DEFAULT_HOST_CONNECT_PORT;
    private String key = "Free";

    public ConnectionConfig() {
        // 从全局配置加载
        this.remoteDomain = NeoLinkConfig.getRemoteDomain();
        this.localPort = NeoLinkConfig.getLocalPort();
        this.hookPort = NeoLinkConfig.getHookPort();
        this.hostConnectPort = NeoLinkConfig.getHostConnectPort();
        this.connectPort = NeoLinkConfig.getHostConnectPort();
    }

    public String getRemoteDomain() {
        return remoteDomain;
    }

    public void setRemoteDomain(String remoteDomain) {
        this.remoteDomain = remoteDomain;
    }

    public String getLocalDomain() {
        return localDomain;
    }

    public void setLocalDomain(String localDomain) {
        this.localDomain = localDomain;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public int getHookPort() {
        return hookPort;
    }

    public void setHookPort(int hookPort) {
        this.hookPort = hookPort;
    }

    public int getHostConnectPort() {
        return hostConnectPort;
    }

    public void setHostConnectPort(int hostConnectPort) {
        this.hostConnectPort = hostConnectPort;
    }

    public int getConnectPort() {
        return connectPort;
    }

    public void setConnectPort(int connectPort) {
        this.connectPort = connectPort;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 创建 Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类
     */
    public static class Builder {
        private final ConnectionConfig config = new ConnectionConfig();

        public Builder remoteDomain(String remoteDomain) {
            config.remoteDomain = remoteDomain;
            return this;
        }

        public Builder localDomain(String localDomain) {
            config.localDomain = localDomain;
            return this;
        }

        public Builder localPort(int localPort) {
            config.localPort = localPort;
            return this;
        }

        public Builder hookPort(int hookPort) {
            config.hookPort = hookPort;
            return this;
        }

        public Builder hostConnectPort(int hostConnectPort) {
            config.hostConnectPort = hostConnectPort;
            return this;
        }

        public Builder connectPort(int connectPort) {
            config.connectPort = connectPort;
            return this;
        }

        public Builder key(String key) {
            config.key = key;
            return this;
        }

        public ConnectionConfig build() {
            return config;
        }
    }
}
