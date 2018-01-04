package tc.oc.commons.bukkit.report;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingBuilder;
import me.anxuiz.settings.types.BooleanType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import tc.oc.api.docs.Report;
import tc.oc.api.docs.Server;
import tc.oc.api.message.MessageListener;
import tc.oc.api.message.MessageQueue;
import tc.oc.api.message.types.ModelUpdate;
import tc.oc.commons.bukkit.channels.AdminChannel;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.BukkitSound;
import tc.oc.commons.bukkit.settings.SettingManagerProvider;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.minecraft.scheduler.MainThreadExecutor;

@Singleton
public class ReportAnnouncer implements PluginFacet, MessageListener {

    static final Setting SOUND_SETTING = new SettingBuilder()
            .name("ReportSound").alias("rs")
            .summary("Hear a sound when someone sends a report")
            .type(new BooleanType())
            .defaultValue(true).get();

    private static final BukkitSound REPORT_SOUND = new BukkitSound(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 2);

    private final ReportConfiguration config;
    private final ReportFormatter reportFormatter;
    private final MessageQueue primaryQueue;
    private final MainThreadExecutor executor;
    private final Server localServer;
    private final AdminChannel adminChannel;
    private final Audiences audiences;
    private final SettingManagerProvider settings;

    @Inject ReportAnnouncer(ReportConfiguration config, ReportFormatter reportFormatter, MessageQueue primaryQueue, MainThreadExecutor executor, Server localServer, AdminChannel adminChannel, Audiences audiences, SettingManagerProvider settings) {
        this.config = config;
        this.reportFormatter = reportFormatter;
        this.primaryQueue = primaryQueue;
        this.executor = executor;
        this.localServer = localServer;
        this.adminChannel = adminChannel;
        this.audiences = audiences;
        this.settings = settings;
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
                        .forEach(viewer -> {
                            Audience audience = audiences.get(viewer);
                            audience.sendMessages(formatted);
                            if (viewer instanceof Player && (boolean)settings.getManager((Player)viewer).getValue(SOUND_SETTING)) {
                                audience.playSound(REPORT_SOUND);
                            }
                        });
        }
    }
}
