package tc.oc.api.queue;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;

import tc.oc.api.config.ApiConfiguration;

/**
 * The primary queue aka server queue aka reply queue
 */
@Singleton
public class PrimaryQueue extends Queue {

    private final Exchange.Direct direct;
    private final Exchange.Fanout fanout;

    @Inject PrimaryQueue(ApiConfiguration config, Exchange.Direct direct, Exchange.Fanout fanout) {
        super(new Consume(config.primaryQueueName(), false, false, true, null));
        this.direct = direct;
        this.fanout = fanout;
    }

    @Override
    public void connect() throws IOException {
        super.connect();
        bind(direct, name());
        bind(fanout, "");
    }
}
