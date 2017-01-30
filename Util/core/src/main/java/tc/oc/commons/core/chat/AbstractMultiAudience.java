package tc.oc.commons.core.chat;

import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;

public abstract class AbstractMultiAudience implements Audience {

    protected abstract Iterable<? extends Audience> getAudiences();

    @Override
    public void sendMessage(BaseComponent message) {
        for(Audience a : getAudiences()) a.sendMessage(message);
    }

    @Override
    public void sendWarning(BaseComponent message, boolean audible) {
        for(Audience a : getAudiences()) a.sendWarning(message, audible);
    }

    @Override
    public void playSound(Sound sound) {
        for(Audience a : getAudiences()) a.playSound(sound);
    }

    @Override
    public void sendHotbarMessage(BaseComponent message) {
        for(Audience a : getAudiences()) a.sendHotbarMessage(message);
    }

    @Override
    public void showTitle(@Nullable BaseComponent title, @Nullable BaseComponent subtitle, int inTicks, int stayTicks, int outTicks) {
        for(Audience a : getAudiences()) a.showTitle(title, subtitle, inTicks, stayTicks, outTicks);
    }

    @Override
    public void hideTitle() {
        for(Audience a : getAudiences()) a.hideTitle();
    }

    @Override
    public void sendMessage(String message) {
        for(Audience a : getAudiences()) a.sendMessage(message);
    }

    @Override
    public void sendWarning(String message, boolean audible) {
        for(Audience a : getAudiences()) a.sendWarning(message, audible);
    }
}
