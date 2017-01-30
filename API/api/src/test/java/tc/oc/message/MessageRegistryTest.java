package tc.oc.message;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import javax.inject.Inject;

import com.google.common.reflect.TypeToken;
import org.junit.Test;
import tc.oc.ApiTest;
import tc.oc.api.docs.Server;
import tc.oc.api.message.Message;
import tc.oc.api.message.MessageRegistry;
import tc.oc.api.message.types.ModelUpdate;

import static org.junit.Assert.*;
import static tc.oc.test.Assert.*;

public class MessageRegistryTest extends ApiTest {

    @Inject MessageRegistry registry;

    @Test
    public void testResolveGenericMessage() throws Exception {
        final TypeToken<? extends Message> token = registry.resolve("ModelUpdate", Optional.of("Server"));
        assertAssignableTo(new TypeToken<ModelUpdate<Server>>(){}, token);

        final Type type = token.getType();
        assertInstanceOf(ParameterizedType.class, type);
        final ParameterizedType pType = (ParameterizedType) type;
        assertEquals(ModelUpdate.class, pType.getRawType());
        assertEquals(Server.class, pType.getActualTypeArguments()[0]);
    }
}
