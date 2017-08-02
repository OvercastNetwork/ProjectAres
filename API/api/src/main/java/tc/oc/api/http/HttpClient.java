package tc.oc.api.http;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.api.client.http.AbstractHttpContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpBackOffIOExceptionHandler;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.ExponentialBackOff;
import com.google.common.base.Charsets;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import tc.oc.api.connectable.Connectable;
import tc.oc.api.config.ApiConstants;
import tc.oc.api.exceptions.ApiException;
import tc.oc.api.exceptions.Conflict;
import tc.oc.api.exceptions.Forbidden;
import tc.oc.api.exceptions.NotFound;
import tc.oc.api.exceptions.UnprocessableEntity;
import tc.oc.api.message.types.Reply;
import tc.oc.api.serialization.JsonUtils;
import tc.oc.commons.core.concurrent.ExecutorUtils;
import tc.oc.commons.core.logging.Loggers;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class HttpClient implements Connectable {

    private static final Duration SHUTDOWN_TIMEOUT = Duration.ofSeconds(10);

    protected final Logger logger;

    private final HttpClientConfiguration config;
    private final ListeningExecutorService executorService;
    private final JsonUtils jsonUtils;
    private final Gson gson;
    private final HttpRequestFactory requestFactory;

    @Inject HttpClient(Loggers loggers, HttpClientConfiguration config, JsonUtils jsonUtils, Gson gson) {
        this.logger = loggers.get(getClass());
        this.config = checkNotNull(config, "config");
        this.jsonUtils = jsonUtils;
        this.gson = gson;
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("API HTTP Executor").build();
        if (config.getThreads() > 0) {
            this.executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(config.getThreads()));
        } else {
            this.executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool(threadFactory));
        }

        this.requestFactory = this.createRequestFactory();

        // By default the google http client is very noisy on http errors,
        // spitting out a long stack trace every time one try of a request
        // fails. Disabling the parent handlers (removes default console
        // handling) and adding our own handler that will only show the short
        // message makes the error output much more manageable.
        // See HttpRequest#986 for the offending line and HttpTransport#81 for
        // the logger definition (it's package private so we can't access it
        // directly).
        final Logger httpLogger = Logger.getLogger(HttpTransport.class.getName());
        httpLogger.setUseParentHandlers(false);
        httpLogger.addHandler(new ConsoleHandler() {
            @Override
            public void publish(LogRecord record) {
                String message = record.getMessage();
                if (record.getThrown() != null) message += ": " + record.getThrown().toString();
                HttpClient.this.logger.log(record.getLevel(), message);
            }
        });
    }

    private HttpRequestFactory createRequestFactory() {
        return new NetHttpTransport().createRequestFactory(request -> {
            request.setConnectTimeout(HttpClient.this.config.getConnectTimeout());
            request.setReadTimeout(HttpClient.this.config.getReadTimeout());
            request.setNumberOfRetries(HttpClient.this.config.getRetries());
            request.setIOExceptionHandler(new HttpBackOffIOExceptionHandler(new ExponentialBackOff.Builder().build()));
        });
    }

    public ListenableFuture<?> get(String path, HttpOption... options) {
        return get(path, (TypeToken) null, options);
    }

    public <T> ListenableFuture<T> get(String path, @Nullable Class<T> returnType, HttpOption...options) {
        return request(HttpMethods.GET, path, null, returnType, options);
    }

    public ListenableFuture<?> get(String path, @Nullable Type returnType, HttpOption...options) {
        return request(HttpMethods.GET, path, null, returnType, options);
    }

    public <T> ListenableFuture<T> get(String path, @Nullable TypeToken<T> returnType, HttpOption...options) {
        return request(HttpMethods.GET, path, null, returnType, options);
    }

    public ListenableFuture<?> post(String path, @Nullable Object content, HttpOption...options) {
        return post(path, content, (TypeToken) null, options);
    }

    public <T> ListenableFuture<T> post(String path, @Nullable Object content, @Nullable Class<T> returnType, HttpOption...options) {
        return request(HttpMethods.POST, path, content, returnType, options);
    }

    public ListenableFuture<?> post(String path, @Nullable Object content, @Nullable Type returnType, HttpOption...options) {
        return request(HttpMethods.POST, path, content, returnType, options);
    }

    public <T> ListenableFuture<T> post(String path, @Nullable Object content, @Nullable TypeToken<T> returnType, HttpOption...options) {
        return request(HttpMethods.POST, path, content, returnType, options);
    }

    public ListenableFuture<?> put(String path, @Nullable Object content, HttpOption...options) {
        return put(path, content, (TypeToken) null, options);
    }

    public <T> ListenableFuture<T> put(String path, @Nullable Object content, @Nullable Class<T> returnType, HttpOption...options) {
        return request(HttpMethods.PUT, path, content, returnType, options);
    }

    public ListenableFuture<?> put(String path, @Nullable Object content, @Nullable Type returnType, HttpOption...options) {
        return request(HttpMethods.PUT, path, content, returnType, options);
    }

    public <T> ListenableFuture<T> put(String path, @Nullable Object content, @Nullable TypeToken<T> returnType, HttpOption...options) {
        return request(HttpMethods.PUT, path, content, returnType, options);
    }

    protected <T> ListenableFuture<T> request(String method, String path, @Nullable Object content, @Nullable Class<T> returnType, HttpOption...options) {
        return request(method, path, content, returnType == null ? null : TypeToken.of(returnType), options);
    }

    protected ListenableFuture<?> request(String method, String path, @Nullable Object content, @Nullable Type returnType, HttpOption...options) {
        return request(method, path, content, returnType == null ? null : TypeToken.of(returnType), options);
    }

    protected <T> ListenableFuture<T> request(String method, String path, @Nullable Object content, @Nullable TypeToken<T> returnType, HttpOption...options) {
        final GenericUrl url;
        try {
            url = new GenericUrl(new URL(config.getBaseUrl(), path));
        } catch(MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        // NOTE: Serialization must happen synchronously, because getter methods may not be thread-safe
        final HttpContent httpContent = content == null ? null : new Content(gson.toJson(content));

        final HttpRequest request;
        try {
            request = requestFactory.buildRequest(method, url, httpContent).setThrowExceptionOnExecuteError(false);
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Failed to build request to " + url.toString(), e);
            return Futures.immediateFailedFuture(e);
        }

        request.getHeaders().set("X-OCN-Version", String.valueOf(ApiConstants.PROTOCOL_VERSION));
        request.getHeaders().setAccept("application/json");

        for(HttpOption option : options) {
            switch(option) {
                case INFINITE_RETRY:
                    request.setNumberOfRetries(Integer.MAX_VALUE);
                    break;

                case NO_RETRY:
                    request.setNumberOfRetries(0);
                    break;
            }
        }

        return this.executorService.submit(new RequestCallable<T>(request, returnType, options));
    }

    @Override
    public void connect() throws IOException {
        // Nothing to do
    }

    @Override
    public void disconnect() throws IOException {
        ExecutorUtils.shutdownImpatiently(executorService, logger, SHUTDOWN_TIMEOUT);
    }

    protected Logger getLogger() {
        return logger;
    }

    private class Content extends AbstractHttpContent {
        final String json;

        protected Content(String json) {
            super("application/json");
            this.json = json;
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            out.write(json.getBytes(Charsets.UTF_8));
        }
    }

    private class RequestCallable<T> implements Callable<T> {
        private final HttpRequest request;
        private final @Nullable Content content;
        private final TypeToken<T> returnType;
        private StackTraceElement[] callSite;

        public RequestCallable(HttpRequest request, @Nullable TypeToken<T> returnType, HttpOption...options) {
            this.request = checkNotNull(request, "http request");
            this.content = (Content) request.getContent();
            this.returnType = returnType;
            this.callSite = new Exception().getStackTrace();
        }

        @Override
        public T call() throws Exception {
            try {
                if(logger.isLoggable(Level.FINE)) {
                    logger.fine("Request " + request.getRequestMethod() + " " + request.getUrl() +
                                (content == null ? " (no content)" : "\n" + content.json));
                }

                final HttpResponse response = this.request.execute();
                final String json = response.parseAsString();

                if(logger.isLoggable(Level.FINE)) {
                    logger.fine("Response " + response.getStatusCode() +
                                " " + response.getStatusMessage() +
                                "\n" + jsonUtils.prettify(json));
                }

                if(response.getStatusCode() < 300) {
                    // We need a 200 in order to return a document
                    if(returnType == null) return null;
                    try {
                        return gson.fromJson(json, returnType.getType());
                    } catch (JsonParseException e) {
                        throw new ApiException("Parsing of " + returnType + " failed at [" + jsonUtils.errorContext(json, returnType.getType()) + "]", e, callSite);
                    }
                } else if(response.getStatusCode() < 400 && returnType == null) {
                    // 3XX is a successful response only if no response document is expected
                    return null;
                } else {
                    Reply reply;
                    // Error response might be a Reply, even if that is not the return type
                    try {
                        reply = gson.fromJson(json, Reply.class);
                    } catch(JsonParseException ex) {
                        reply = null;
                    }

                    final String message;
                    if(reply != null && reply.error() != null) {
                        // If we have a Reply somehow, use the included error message
                        message = reply.error();
                    } else {
                        // Otherwise, make a generic message
                        message = "HTTP " + response.getStatusCode() + " " + response.getStatusMessage();
                    }

                    final ApiException apiException;
                    switch(response.getStatusCode()) {
                        case 403: apiException = new Forbidden(message, reply); break;
                        case 404: apiException = new NotFound(message, reply); break;
                        case 409: apiException = new Conflict(message, reply); break;
                        case 422: apiException = new UnprocessableEntity(message, reply); break;
                        default:  apiException = new ApiException(message, reply); break;
                    }

                    throw apiException;
                }
            } catch(ApiException e) {
                e.setCallSite(callSite);
                throw e;
            } catch (Throwable e) {
                final String message = "Unhandled exception submitting request to " + this.request.getUrl();
                logger.log(Level.SEVERE, message, e);
                throw new ApiException(message, e, callSite);
            }
        }
    }
}
