package tc.oc.api.queue;

import tc.oc.api.message.MessageQueue;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.minecraft.suspend.SuspendableBinder;

public class QueueManifest extends HybridManifest {

    @Override
    protected void configure() {
        bindAndExpose(QueueClientConfiguration.class)
            .to(QueueClientConfigurationImpl.class);

        bindAndExpose(QueueClient.class).asEagerSingleton();
        bindAndExpose(Exchange.Direct.class).asEagerSingleton();
        bindAndExpose(Exchange.Fanout.class).asEagerSingleton();
        bindAndExpose(Exchange.Topic.class).asEagerSingleton();
        bindAndExpose(PrimaryQueue.class).asEagerSingleton();

        publicBinder().forOptional(MessageQueue.class)
                      .setBinding().to(PrimaryQueue.class);

        final SuspendableBinder suspendables = new SuspendableBinder(publicBinder());
        suspendables.addBinding().to(PrimaryQueue.class);
    }
}
