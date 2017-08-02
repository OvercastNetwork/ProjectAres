package tc.oc.commons.bukkit.localization;

import java.util.Locale;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import tc.oc.commons.core.localization.Locales;

import static tc.oc.test.Assert.*;
import static org.junit.Assert.*;

public class LocalesTest {

    @Test
    public void matcher() throws Exception {
        Set<Locale> avail = ImmutableSet.of(new Locale("en", "US"),
                                            new Locale("en", "CA"),
                                            new Locale("en"),
                                            new Locale("fr"));
        assertSequence(
            Locales.match(new Locale("en"), avail),
            new Locale("en"), new Locale("en", "US"), new Locale("en", "CA")
        );

        assertSequence(
            Locales.match(new Locale("en", "CA"), avail),
            new Locale("en", "CA"), new Locale("en"), new Locale("en", "US")
        );

        assertSequence(
            Locales.match(new Locale("en", "US"), avail),
            new Locale("en", "US"), new Locale("en"), new Locale("en", "CA")
        );

        assertSequence(
            Locales.match(new Locale("en", "GB"), avail),
            new Locale("en"), new Locale("en", "US"), new Locale("en", "CA")
        );

        assertSequence(
            Locales.match(new Locale("fr"), avail),
            new Locale("fr"), Locales.DEFAULT_LOCALE
        );

        assertSequence(
            Locales.match(new Locale("es"), avail),
            Locales.DEFAULT_LOCALE
        );
    }
}
