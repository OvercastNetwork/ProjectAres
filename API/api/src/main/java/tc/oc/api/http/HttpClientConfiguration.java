package tc.oc.api.http;

import java.net.URL;

public interface HttpClientConfiguration {

    int DEFAULT_THREADS = 0;
    int DEFAULT_CONNECT_TIMEOUT = 20000;
    int DEFAULT_READ_TIMEOUT = 20000;
    int DEFAULT_RETRIES = 10;

    /**
     * Base URL of the API. End points will be appended to this address.
     */
    URL getBaseUrl();

    /**
     * Number of threads to execute requests. 0 indicates an unbounded number
     * of threads.
     */
    int getThreads();

    /**
     * Timeout in milliseconds to establish a connection or 0 for an infinite
     * timeout.
     */
    int getConnectTimeout();

    /**
     * Timeout in milliseconds to read data from an established connection or 0
     * for an infinite timeout.
     */
    int getReadTimeout();

    /**
     * Number of retries to execute a request until giving up. 0 indicates no
     * retrying.
     */
    int getRetries();
}
