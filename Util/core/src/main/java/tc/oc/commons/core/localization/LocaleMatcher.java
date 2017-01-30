package tc.oc.commons.core.localization;

import java.util.Locale;
import java.util.Set;

import com.google.common.base.Preconditions;

public class LocaleMatcher {
    private final Locale defaultLocale;
    private final Set<Locale> supportedLocales;

    public static final int UNRELATED_LANGUAGE = -1;

    public LocaleMatcher(Locale defaultLocale, Set<Locale> supportedLocales) {
        this.defaultLocale = Preconditions.checkNotNull(defaultLocale, "no default locale available");
        this.supportedLocales = Preconditions.checkNotNull(supportedLocales, "no supported locales");
    }

    public Locale closestMatchFor(Locale target) {
        Locale bestMatch = this.defaultLocale;
        int bestMatchScore = UNRELATED_LANGUAGE;

        for (Locale potentialBestMatch : this.supportedLocales) {
            int currentComparison = this.compareLocales(target, potentialBestMatch);

            if(currentComparison > bestMatchScore) {
                bestMatch = potentialBestMatch;
                bestMatchScore = currentComparison;
            }
        }

        return bestMatch;
    }

    public int compareLocales(Locale l1, Locale l2) {
        if(l1.getLanguage().equals(l2.getLanguage())) {
            if(l1.getCountry().equals(l2.getCountry())) {
                return 2;
            } else {
                return 1;
            }
        } else {
            return UNRELATED_LANGUAGE;
        }
    }
}
