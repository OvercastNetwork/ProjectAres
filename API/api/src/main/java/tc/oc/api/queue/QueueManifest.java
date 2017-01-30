package tc.oc.api.queue;

import tc.oc.api.connectable.ConnectableBinder;
import tc.oc.api.message.MessageQueue;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.minecraft.suspend.SuspendableBinder;

public class QueueManifest extends HybridManifest {

    @Override
    protected void configure() {
        bindAndExpose(QueueClientConfiguration.class)
            .to(QueueClientConfigurationImpl.class);

        bindAndExpose(QueueClient.class);
        bindAndExpose(Exchange.Direct.class);
        bindAndExpose(Exchange.Fanout.class);
        bindAndExpose(Exchange.Topic.class);
        bindAndExpose(PrimaryQueue.class);

        publicBinder().forOptional(MessageQueue.class)
                      .setBinding().to(PrimaryQueue.class);

        // These will connect in the order listed here.
        // TODO: figure out the order from their dependencies.
        final ConnectableBinder services = new ConnectableBinder(publicBinder());
        services.addBinding().to(QueueClient.class);
        services.addBinding().to(Exchange.Direct.class);
        services.addBinding().to(Exchange.Fanout.class);
        services.addBinding().to(Exchange.Topic.class);
        services.addBinding().to(PrimaryQueue.class);

        final SuspendableBinder suspendables = new SuspendableBinder(publicBinder());
        suspendables.addBinding().to(PrimaryQueue.class);
    }
}
