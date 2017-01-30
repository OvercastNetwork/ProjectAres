package tc.oc.api.document;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.inject.CreationException;
import com.google.inject.ProvisionException;
import tc.oc.api.docs.virtual.Document;
import tc.oc.api.document.DocumentMeta;
import tc.oc.api.document.DocumentRegistry;
import tc.oc.api.document.Getter;
import tc.oc.api.exceptions.SerializationException;
import tc.oc.commons.core.logging.Loggers;

@Singleton
public class DocumentSerializer implements JsonSerializer<Document>, JsonDeserializer<Document> {

    protected final Logger logger;
    protected final DocumentRegistry documentRegistry;

    @Inject DocumentSerializer(Loggers loggers, DocumentRegistry documentRegistry) {
        this.logger = loggers.get(getClass());
        this.documentRegistry = documentRegistry;
    }

    @Override
    public JsonElement serialize(Document document, Type documentType, JsonSerializationContext context) {
        final JsonObject documentJson = new JsonObject();
        final DocumentMeta<?> documentMeta = documentRegistry.getMeta(document.getClass());
        final TypeToken documentTypeToken = TypeToken.of(documentType);

        for(Map.Entry<String, Getter> entry : documentMeta.getters().entrySet()) {
            final String name = entry.getKey();
            final Getter getter = entry.getValue();
            final Type resolvedType = getter.resolvedType(documentTypeToken);

            final Object value;
            try {
                value = getter.get(document);
            } catch(Exception e) {
                throw new SerializationException("Exception reading property " + resolvedType + " " + documentType + "." + name, e);
            }

            try {
                documentJson.add(name, context.serialize(value, resolvedType));
            } catch(Exception e) {
                throw new SerializationException("Exception serializing property " + resolvedType + " " + documentType + "." + name + " = " + value, e);
            }
        }

        return documentJson;
    }

    @Override
    public Document deserialize(JsonElement rawJson, Type documentType, JsonDeserializationContext context) throws JsonParseException {
        final TypeToken documentTypeToken = TypeToken.of(documentType);
        final DocumentMeta<?> documentMeta = documentRegistry.getMeta(documentTypeToken.getRawType());
        final Class<? extends Document> instantiableType = documentMeta.type();

        if(!(rawJson instanceof JsonObject)) {
            throw new JsonSyntaxException("Expected JSON object while deserializing " + instantiableType.getName() + ", not " + rawJson.getClass().getSimpleName());
        }
        final JsonObject documentJson = (JsonObject) rawJson;
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        for(Map.Entry<String, Getter> entry : documentMeta.getters().entrySet()) {
            final String name = entry.getKey();
            final Getter getter = entry.getValue();
            final Type resolvedType = getter.resolvedType(documentTypeToken);
            final JsonElement propertyJson = documentJson.get(name);

            try {
                if(propertyJson == null || propertyJson.isJsonNull()) {
                    if(!getter.isNullable()) {
                        throw new NullPointerException("Missing value for non-nullable property " + name);
                    }
                } else {
                    builder.put(name, context.deserialize(propertyJson, resolvedType));
                }
            } catch(Exception e) {
                throw new SerializationException("Exception deserializing property " + resolvedType + " " + documentType + "." + name + " = " + propertyJson, e);
            }
        }

        try {
            return documentRegistry.instantiate(documentMeta, builder.build());
        } catch(ProvisionException | CreationException e) {
            throw new SerializationException("Exception instantiating document " + instantiableType.getName(), e);
        }
    }
}
