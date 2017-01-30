package tc.oc.commons.bukkit.whisper;

import org.bukkit.command.CommandSender;
import tc.oc.commons.bukkit.nick.Identity;

public interface WhisperSender {
    void send(CommandSender sender, Identity from, Identity to, String content);
}
