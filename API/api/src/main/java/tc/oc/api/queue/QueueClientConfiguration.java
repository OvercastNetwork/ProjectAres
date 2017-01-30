package tc.oc.api.queue;

import java.util.List;

import com.rabbitmq.client.Address;

public interface QueueClientConfiguration {

    /**
     * Addresses to connect to in order. If connection to one fails try the
     * next one in the list.
     */
    List<Address> getAddresses();

    /**
     * Name of user to connect as.
     */
    String getUsername();

    /**
     * Password of user to connect as.
     */
    String getPassword();

    /**
     * Virtual host to connect to.
     */
    String getVirtualHost();

    /**
     * The connection timeout; zero means to wait indefinitely.
     */
    int getConnectionTimeout();

    /**
     * How long will automatic recovery wait before attempting to reconnect in
     * milliseconds.
     */
    int getNetworkRecoveryInterval();

    int getThreads();
}
