package tc.oc.api.http;

import tc.oc.commons.core.inject.HybridManifest;

public class HttpManifest extends HybridManifest {

    @Override
    protected void configure() {
        expose(HttpClient.class);
        bind(HttpClient.class).asEagerSingleton();
        bind(HttpClientConfiguration.class)
            .to(HttpClientConfigurationImpl.class);
    }
}
