package tc.oc.api.http;

/**
 * Flags passed to HttpClient for individual requests.
 *
 * TODO nice-to-have: replace HttpClientConfig with this mechanism so
 * any setting can be overridden per request.
 */
public enum HttpOption {
    NO_RETRY,               // Do not retry the request
    INFINITE_RETRY          // Retry the request forever
}
