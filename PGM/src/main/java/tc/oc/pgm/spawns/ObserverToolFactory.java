package tc.oc.pgm.spawns;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import tc.oc.commons.bukkit.configuration.ConfigUtils;
import tc.oc.commons.bukkit.item.ItemBuilder;
import tc.oc.commons.bukkit.localization.Translations;
import tc.oc.commons.core.localization.LocaleMap;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.pgm.kits.GlobalItemParser;
import tc.oc.pgm.xml.InvalidXMLException;

/**
 * Creates some of the items for the observer hotbar
 */
public class ObserverToolFactory {

    public static final String EDIT_WAND_PERMISSION = "worldedit.wand";

    private final Logger logger;
    private final Configuration config;
    private final SAXBuilder saxBuilder;
    private final GlobalItemParser itemParser;

    private final LocaleMap<ItemStack> howToBooks = new LocaleMap<>();

    @Inject private ObserverToolFactory(Loggers loggers, Configuration config, SAXBuilder saxBuilder, GlobalItemParser itemParser) {
        this.saxBuilder = saxBuilder;
        this.logger = loggers.get(getClass());
        this.config = config;
        this.itemParser = itemParser;
    }

    public ItemStack getTeleportTool(Player player) {
        return new ItemBuilder()
            .material(Material.COMPASS)
            .name(ChatColor.BLUE.toString() + ChatColor.BOLD + Translations.get().t("teleportTool.displayName", player))
            .get();
    }

    public ItemStack getEditWand(Player player) {
        return new ItemBuilder()
            .material(Material.RABBIT_FOOT)
            .name(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + Translations.get().t("editWand.displayName", player))
            .get();
    }

    public boolean canUseEditWand(Permissible permissible) {
        return permissible.hasPermission(EDIT_WAND_PERMISSION);
    }

    public @Nullable ItemStack getHowToBook(Player player) {
        final Locale locale = player.getCurrentLocale();
        ItemStack book = howToBooks.get(locale);

        if(book == null) {
            final Path baseFile = ConfigUtils.getPath(config, "howto-book-file", null);
            if(baseFile == null) return null;

            book = loadHowToBook(baseFile.resolve(locale.toLanguageTag().replace('-', '_') + ".xml"));
            if(book != null) {
                logger.fine("Loaded how-to book for locale " + locale);
                howToBooks.put(locale, book);
            }
        }

        return book;
    }

    @Nullable ItemStack loadHowToBook(Path file) {
        try {
            if(!Files.isRegularFile(file)) return null;
            return itemParser.parseBook(saxBuilder.build(file.toFile())
                                                  .getRootElement());
        }
        catch(JDOMException | IOException | InvalidXMLException e) {
            logger.log(Level.SEVERE, "Failed to parse how-to book from XML file " + file, e);
            return null;
        }
    }
}
