package tc.oc.pgm.settings;

import javax.inject.Singleton;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingBuilder;
import me.anxuiz.settings.types.BooleanType;
import tc.oc.commons.bukkit.settings.SettingBinder;
import tc.oc.commons.bukkit.settings.SettingCallbackBinder;
import tc.oc.commons.core.inject.HybridManifest;

/**
 * Assorted PGM settings that don't have a proper home yet
 */
public class Settings extends HybridManifest {

    @Override
    protected void configure() {
        final SettingBinder settings = new SettingBinder(publicBinder());
        settings.addBinding().toInstance(ObserverSetting.get());
        settings.addBinding().toInstance(BLOOD);
        settings.addBinding().toInstance(RATINGS);
        settings.addBinding().toInstance(SOUNDS);

        bindAndExpose(ObserversCallback.class).in(Singleton.class);
        new SettingCallbackBinder(publicBinder())
            .changesIn(ObserverSetting.get()).to(ObserversCallback.class);
    }

    public static final Setting BLOOD = new SettingBuilder()
        .name("Blood")
        .summary("See blood when players get hurt")
        .type(new BooleanType())
        .defaultValue(false).get();

    public static final Setting RATINGS = new SettingBuilder()
        .name("Ratings").alias("rate")
        .summary("Automatically show the map rating dialog for maps you haven't rated yet")
        .type(new BooleanType())
        .defaultValue(true).get();

    public static final Setting SOUNDS = new SettingBuilder()
        .name("Sounds").alias("sound")
        .summary("Hear sounds to alert you of the last three seconds of a countdown")
        .type(new BooleanType())
        .defaultValue(true).get();
}
