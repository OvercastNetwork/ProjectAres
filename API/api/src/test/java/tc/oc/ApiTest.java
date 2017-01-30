package tc.oc;

import java.io.InputStream;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Provides;
import org.junit.Before;
import tc.oc.api.ApiManifest;
import tc.oc.api.config.ApiConfiguration;
import tc.oc.api.http.HttpClient;
import tc.oc.api.http.HttpManifest;
import tc.oc.api.maps.MapService;
import tc.oc.api.maps.NullMapService;
import tc.oc.api.model.ModelBinders;
import tc.oc.api.model.ModelSync;
import tc.oc.api.queue.QueueClient;
import tc.oc.api.queue.QueueManifest;
import tc.oc.api.servers.NullServerService;
import tc.oc.api.servers.ServerService;
import tc.oc.api.sessions.NullSessionService;
import tc.oc.api.sessions.SessionService;
import tc.oc.api.users.NullUserService;
import tc.oc.api.users.UserService;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.inject.TestModule;
import tc.oc.inject.ProtectedBinder;

public abstract class ApiTest {

    protected static final String ISO_DATE = "2000-01-01T00:00:00Z";
    protected static final Instant INSTANT = Instant.parse(ISO_DATE);

    class ApiTestModule extends HybridManifest implements ModelBinders {
        @Override
        protected void configure() {
            install(new TestModule());
            install(new ApiManifest());
            install(new HttpManifest());
            install(new QueueManifest());

            bind(ExecutorService.class)
                .annotatedWith(ModelSync.class)
                .toInstance(Executors.newSingleThreadExecutor());

            bind(ApiConfiguration.class).toInstance(() -> "primary_queue");

            publicBinder().forOptional(ServerService.class).setBinding().to(NullServerService.class);
            publicBinder().forOptional(UserService.class).setBinding().to(NullUserService.class);
            publicBinder().forOptional(SessionService.class).setBinding().to(NullSessionService.class);
            publicBinder().forOptional(MapService.class).setBinding().to(NullMapService.class);

            requestInjection(ApiTest.this);
        }

        @Provides @Named("config.yml")
        InputStream configYml() {
            return ClassLoader.getSystemResourceAsStream("config.yml");
        }
    }

    protected @Inject Gson gson;
    protected @Inject QueueClient queueClient;
    protected @Inject HttpClient httpClient;

    @Before
    public void setUp() {
        final ApiTestModule module = new ApiTestModule();
        //ElementPrinter.visit(module);
        Guice.createInjector(binder -> ProtectedBinder.newProtectedBinder(binder).install(module));
    }
}
