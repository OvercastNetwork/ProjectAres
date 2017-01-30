package tc.oc.api.queue;

import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.rabbitmq.client.Address;
import tc.oc.minecraft.api.configuration.Configuration;
import tc.oc.minecraft.api.configuration.ConfigurationSection;

import static com.google.common.base.Preconditions.checkNotNull;

public class QueueClientConfigurationImpl implements QueueClientConfiguration {

    public static final String SECTION = "queue";
    
    public static final String NETWORK_RECOVERY_INTERVAL_PATH = "network-recovery-interval";
    public static final String CONNECTION_TIMEOUT_PATH = "connection-timeout";
    public static final String VIRTUAL_HOST_PATH = "virtual-host";
    public static final String THREADS_PATH = "threads";
    public static final String PASSWORD_PATH = "password";
    public static final String USERNAME_PATH = "username";
    public static final String ADDRESSES_PATH = "addresses";

    private final ConfigurationSection config;

    @Inject public QueueClientConfigurationImpl(Configuration config) {
        this.config = checkNotNull(config.getSection(SECTION));
    }

    @Override
    public List<Address> getAddresses() {
        return config.getStringList(ADDRESSES_PATH).stream().map(Address::new).collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return config.getString(USERNAME_PATH);
    }

    @Override
    public String getPassword() {
        return config.getString(PASSWORD_PATH);
    }

    @Override
    public String getVirtualHost() {
        return config.getString(VIRTUAL_HOST_PATH);
    }

    @Override
    public int getConnectionTimeout() {
        return config.getInt(CONNECTION_TIMEOUT_PATH);
    }

    @Override
    public int getNetworkRecoveryInterval() {
        return config.getInt(NETWORK_RECOVERY_INTERVAL_PATH);
    }

    @Override
    public int getThreads() {
        return config.getInt(THREADS_PATH);
    }
}
