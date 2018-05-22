package tc.oc.commons.bukkit.report;

import javax.inject.Inject;
import javax.inject.Singleton;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingBuilder;
import me.anxuiz.settings.types.BooleanType;
import org.bukkit.Sound;
import tc.oc.api.docs.Report;
import tc.oc.api.docs.Server;
import tc.oc.api.message.MessageListener;
import tc.oc.api.message.MessageQueue;
import tc.oc.api.message.types.ModelUpdate;
import tc.oc.api.servers.ServerStore;
import tc.oc.commons.bukkit.channels.admin.AdminChannel;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.BukkitSound;
import tc.oc.commons.bukkit.settings.SettingManagerProvider;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.minecraft.scheduler.MainThreadExecutor;

@Singleton
public class ReportAnnouncer implements PluginFacet, MessageListener {

    static final Setting SOUND_SETTING = new SettingBuilder()
            .name("ReportSound").alias("rs")
            .summary("Hear a sound when someone sends a report")
            .type(new BooleanType())
            .defaultValue(true).get();

    private static final BukkitSound REPORT_SOUND = new BukkitSound(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.6f, 1.5f);

    private final ReportConfiguration config;
    private final ReportFormatter reportFormatter;
    private final MessageQueue primaryQueue;
    private final MainThreadExecutor executor;
    private final Server localServer;
    private final ServerStore serverStore;
    private final Audiences audiences;

    @Inject ReportAnnouncer(ReportConfiguration config, ReportFormatter reportFormatter, MessageQueue primaryQueue, MainThreadExecutor executor, Server localServer, ServerStore serverStore, Audiences audiences) {
        this.config = config;
        this.reportFormatter = reportFormatter;
        this.primaryQueue = primaryQueue;
        this.executor = executor;
        this.localServer = localServer;
        this.serverStore = serverStore;
        this.audiences = audiences;
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
        if(serverStore.canCommunicate(localServer._id(), message.document().server_id())) {
            audiences.permission(ReportPermissions.RECEIVE)
                     .sendMessages(reportFormatter.format(message.document(), true, false));
        }
    }
}
