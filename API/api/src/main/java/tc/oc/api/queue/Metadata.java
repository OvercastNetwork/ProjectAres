package tc.oc.api.queue;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import java.time.Duration;
import java.time.Instant;
import tc.oc.api.config.ApiConstants;

public class Metadata extends AMQP.BasicProperties {

    // Custom headers
    public static final String PROTOCOL_VERSION = "protocol_version";
    public static final String MODEL_NAME = "model_name";

    public static Map<String, Object> nonNullHeaders(Map<String, Object> headers) {
        return headers != null ? headers : Collections.<String, Object>emptyMap();
    }

    public static Map<String, Object> getHeaders(AMQP.BasicProperties props) {
        return nonNullHeaders(props.getHeaders());
    }

    @Override
    public Map<String, Object> getHeaders() {
        return nonNullHeaders(super.getHeaders());
    }

    public static @Nullable String getHeaderString(AMQP.BasicProperties props, String name) {
        // Header values are some kind of spooky fake string object
        // called LongStringHelper.ByteArrayLongString
        Object o = getHeaders(props).get(name);
        return o == null ? null : o.toString();
    }

    public @Nullable String getHeaderString(String name) {
        return getHeaderString(this, name);
    }

    public static int getHeaderInt(AMQP.BasicProperties props, String name, int def) {
        final String text = getHeaderString(props, name);
        return text == null ? def : Integer.parseInt(text);
    }

    public int getHeaderInt(String name, int def) {
        return getHeaderInt(this, name, def);
    }

    public static int protocolVersion(AMQP.BasicProperties props) {
        return getHeaderInt(props, PROTOCOL_VERSION, ApiConstants.PROTOCOL_VERSION);
    }

    public int protocolVersion() {
        return protocolVersion(this);
    }

    public static Optional<String> modelName(AMQP.BasicProperties props) {
        return Optional.ofNullable(getHeaderString(props, MODEL_NAME));
    }

    public Optional<String> modelName() {
        return modelName(this);
    }

    public @Nullable Duration expiration() {
        final String expiration = getExpiration();
        return expiration == null ? null : Duration.ofMillis(Long.parseLong(expiration));
    }

    public @Nullable Instant timestamp() {
        final Date timestamp = getTimestamp();
        return timestamp == null ? null : timestamp.toInstant();
    }

    public @Nullable Instant expiresAt() {
        final Instant timestamp = timestamp();
        final Duration expiration = expiration();
        if(timestamp != null && expiration != null) {
            return timestamp.plus(expiration);
        }
        return null;
    }

    public Metadata(String contentType, String contentEncoding, Map<String, Object> headers, Integer deliveryMode, Integer priority, String correlationId, String replyTo, String expiration, String messageId, Date timestamp, String type, String userId, String appId) {
        super(contentType, contentEncoding, headers, deliveryMode, priority, correlationId, replyTo, expiration, messageId, timestamp, type, userId, appId, null);
    }

    public Metadata(BasicProperties p) {
        this(p.getContentType(), p.getContentEncoding(), p.getHeaders(), p.getDeliveryMode(), p.getPriority(), p.getCorrelationId(), p.getReplyTo(), p.getExpiration(), p.getMessageId(), p.getTimestamp(), p.getType(), p.getUserId(), p.getAppId());
    }

    public static class Builder {
        private String contentType;
        private String contentEncoding;
        private Map<String,Object> headers;
        private Integer deliveryMode;
        private Integer priority;
        private String correlationId;
        private String replyTo;
        private String expiration;
        private String messageId;
        private Date timestamp;
        private String type;
        private String userId;
        private String appId;

        public Builder() {};

        public Builder(@Nullable BasicProperties p) {
            if(p == null) return;
            this.contentType = p.getContentType();
            this.contentEncoding = p.getContentEncoding();
            this.headers = p.getHeaders();
            this.deliveryMode = p.getDeliveryMode();
            this.priority = p.getPriority();
            this.correlationId = p.getCorrelationId();
            this.replyTo = p.getReplyTo();
            this.expiration = p.getExpiration();
            this.timestamp = p.getTimestamp();
            this.type = p.getType();
            this.userId = p.getUserId();
            this.appId = p.getAppId();
        };

        public Builder expiration(Duration expiration) {
            return expiration(String.valueOf(expiration.toMillis()));
        }

        public Builder persistent(boolean persistent) {
            return deliveryMode(persistent ? 2 : 1);
        }

        public Builder timestamp(Instant timestamp) {
            return timestamp(Date.from(timestamp));
        }

        public Builder replyTo(Queue queue) {
            return replyTo(queue.name());
        }

        public Builder header(String key, String value) {
            if(headers == null) {
                headers = new HashMap<>();
            }
            headers.put(key, value);
            return this;
        }

        public Builder header(String key, int value) {
            return header(key, String.valueOf(value));
        }

        public Builder protocolVersion(int version) {
            return header("protocol_version", version);
        }

        // Commence copypasta
        public Builder contentType(String contentType)
        {   this.contentType = contentType; return this; }
        public Builder contentEncoding(String contentEncoding)
        {   this.contentEncoding = contentEncoding; return this; }
        public Builder headers(Map<String,Object> headers)
        {   this.headers = headers; return this; }
        public Builder deliveryMode(Integer deliveryMode)
        {   this.deliveryMode = deliveryMode; return this; }
        public Builder priority(Integer priority)
        {   this.priority = priority; return this; }
        public Builder correlationId(String correlationId)
        {   this.correlationId = correlationId; return this; }
        public Builder replyTo(String replyTo)
        {   this.replyTo = replyTo; return this; }
        public Builder expiration(String expiration)
        {   this.expiration = expiration; return this; }
        public Builder messageId(String messageId)
        {   this.messageId = messageId; return this; }
        public Builder timestamp(Date timestamp)
        {   this.timestamp = timestamp; return this; }
        public Builder type(String type)
        {   this.type = type; return this; }
        public Builder userId(String userId)
        {   this.userId = userId; return this; }
        public Builder appId(String appId)
        {   this.appId = appId; return this; }

        public Metadata build() {
            return new Metadata
                ( contentType
                , contentEncoding
                , headers
                , deliveryMode
                , priority
                , correlationId
                , replyTo
                , expiration
                , messageId
                , timestamp
                , type
                , userId
                , appId
                );
        }
    }
}
