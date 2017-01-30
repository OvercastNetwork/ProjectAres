package tc.oc.commons.bukkit.localization;

import javax.inject.Inject;

import org.junit.Test;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.commons.CommonsBukkitTest;

import static org.junit.Assert.assertTrue;

public class TranslationKeyTests extends CommonsBukkitTest {

    @Inject Translations translations;

    @Test
    public void testAllGamemodesHaveLocalizedNames() throws Exception {
        for(MapDoc.Gamemode gamemode : MapDoc.Gamemode.values()) {
            assertTrue("Gamemode." + gamemode + " has no short name",
                       translations.hasKey(Translations.gamemodeShortName(gamemode)));

            assertTrue("Gamemode." + gamemode + " has no long name",
                       translations.hasKey(Translations.gamemodeLongName(gamemode)));
        }
    }

}
