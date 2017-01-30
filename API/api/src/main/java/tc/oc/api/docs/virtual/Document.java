package tc.oc.api.docs.virtual;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.document.DocumentMeta;
import tc.oc.api.document.DocumentRegistry;
import tc.oc.api.message.Message;
import tc.oc.api.document.DocumentSerializer;

/**
 * Base interface for serializable API documents, including documents stored
 * in the database ({@link Model}s and {@link PartialModel}s) and directives
 * exchanged through the API ({@link Message}s).
 *
 * {@link Document}s are serialized differently than normal objects. No fields
 * are included in serialization by default. Rather, the {@link Serialize}
 * annotation is used to mark serialized fields. Methods can also be annotated
 * with {@link Serialize} to mark them as getters or setters. Getter methods
 * must return a value and take no parameters, while setter methods must take
 * exactly one parameter. If a class or interface is annotated with
 * {@link Serialize}, all fields and methods declared in that class will be
 * included in serialization.
 *
 * {@link DocumentSerializer} is responsible for serializing and deserializing
 * {@link Document}s. It uses {@link DocumentRegistry} to get a {@link DocumentMeta}
 * for a class, which knows about all the getters and setters. Registration
 * happens on demand and is entirely automatic.
 */
public interface Document {}
