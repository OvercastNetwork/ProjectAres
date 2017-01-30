package tc.oc.api.message;

import tc.oc.api.docs.virtual.Document;

/**
 * A {@link Document} representing a request or response through the API.
 *
 * Every message type must be explicitly registered through a {@link MessageBinder}.
 * Registration associates a type name with a base type and an instantiable type.
 *
 * The base type is the common ancestor of all types that represent
 * this message. Serialization uses the base type to figure out the
 * type name for outgoing messages.
 *
 * The instantiable type is the class that will represent incoming messages.
 * This class must have a no-args constructor, and should include every field
 * that any incoming message might have.
 */
public interface Message extends Document {
}
