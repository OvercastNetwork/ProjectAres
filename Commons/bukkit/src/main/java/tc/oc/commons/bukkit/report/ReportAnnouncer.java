package tc.oc.commons.bukkit.report;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.api.docs.Report;
import tc.oc.api.docs.Server;
import tc.oc.api.message.MessageListener;
import tc.oc.api.message.MessageService;
import tc.oc.api.message.types.ModelUpdate;
import tc.oc.commons.bukkit.channels.AdminChannel;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.minecraft.scheduler.MainThreadExecutor;

@Singleton
public class ReportAnnouncer implements PluginFacet, MessageListener {

    private final ReportConfiguration config;
    private final ReportFormatter reportFormatter;
    private final MessageService primaryQueue;
    private final MainThreadExecutor executor;
    private final Server localServer;
    private final AdminChannel adminChannel;
    private final Audiences audiences;

    @Inject ReportAnnouncer(ReportConfiguration config, ReportFormatter reportFormatter, MessageService primaryQueue, MainThreadExecutor executor, Server localServer, AdminChannel adminChannel, Audiences audiences) {
        this.config = config;
        this.reportFormatter = reportFormatter;
        this.primaryQueue = primaryQueue;
        this.executor = executor;
        this.localServer = localServer;
        this.adminChannel = adminChannel;
        this.audiences = audiences;
    }

    @Override
    public boolean isActive() {
        return config.crossServer();
    }

    @Override
    public void enable() {
        primaryQueue.bind(ModelUpdate.class);
        primaryQueue.subscribe(this, executor);
    }

    @Override
    public void disable() {
        primaryQueue.unsubscribe(this);
    }

    @HandleMessage
    public void broadcast(ModelUpdate<Report> message) {
        if(localServer._id().equals(message.document().server_id()) ||
           (config.crossServer() && config.families().contains(message.document().family()))) {

            final List<? extends BaseComponent> formatted = reportFormatter.format(message.document(), true, false);
            adminChannel.viewers()
                        .filter(viewer -> viewer.hasPermission(ReportPermissions.RECEIVE))
                        .forEach(viewer -> audiences.get(viewer).sendMessages(formatted));
        }
    }
}
