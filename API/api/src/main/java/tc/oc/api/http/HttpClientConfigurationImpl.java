package tc.oc.api.http;

import java.net.URL;
import javax.inject.Inject;

import tc.oc.commons.core.configuration.ConfigUtils;
import tc.oc.minecraft.api.configuration.Configuration;
import tc.oc.minecraft.api.configuration.ConfigurationSection;

import static com.google.common.base.Preconditions.checkNotNull;

public class HttpClientConfigurationImpl implements HttpClientConfiguration {

    public static final String SECTION = "api.http";

    public static final String RETRIES_PATH = "retries";
    public static final String READ_TIMEOUT_PATH = "read-timeout";
    public static final String CONNECT_TIMEOUT_PATH = "connect-timeout";
    public static final String THREADS_PATH = "threads";
    public static final String BASE_URL_PATH = "base-url";

    private final ConfigurationSection config;

    @Inject public HttpClientConfigurationImpl(Configuration config) {
        this.config = checkNotNull(config.getSection(SECTION));
    }

    @Override
    public URL getBaseUrl() {
        return ConfigUtils.needUrl(config, BASE_URL_PATH);
    }

    @Override
    public int getThreads() {
        return config.getInt(THREADS_PATH);
    }

    @Override
    public int getConnectTimeout() {
        return config.getInt(CONNECT_TIMEOUT_PATH);
    }

    @Override
    public int getReadTimeout() {
        return config.getInt(READ_TIMEOUT_PATH);
    }

    @Override
    public int getRetries() {
        return config.getInt(RETRIES_PATH);
    }
}
