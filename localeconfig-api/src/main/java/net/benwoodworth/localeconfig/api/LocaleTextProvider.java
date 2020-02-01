package net.benwoodworth.localeconfig.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

abstract class LocaleTextProvider {
    private LocaleTextProvider() {
    }

    protected abstract String getNamespace();

    @NotNull
    abstract Locale getServerLocale();

    @Nullable
    private static Locale broadenLocale(Locale locale) {
        if (locale.getVariant().length() != 0) {
            return new Locale(locale.getLanguage(), locale.getCountry());
        } else if (locale.getCountry().length() != 0) {
            return new Locale(locale.getLanguage());
        } else {
            return null;
        }
    }

    @Nullable
    private LocaleText getText(@NotNull Locale locale, @NotNull String localeKey) {
        String namespace;
        String namespacedLocaleKey;

        int colonIndex = localeKey.indexOf(':');
        if (colonIndex == -1) {
            namespace = getNamespace();
            namespacedLocaleKey = localeKey;
        } else {
            int nextColonIndex = localeKey.indexOf(':', colonIndex + 1);
            if (nextColonIndex != -1) {
                throw new IllegalArgumentException("Locale key must have at most one colon for a namespace");
            }

            namespace = localeKey.substring(0, colonIndex);
            namespacedLocaleKey = localeKey.substring(colonIndex + 1);
        }

        Locale tryLocale = locale;
        boolean triedEnglish = false;
        do {
            LocaleText localeText = getText(tryLocale, namespace, namespacedLocaleKey);
            if (localeText != null) {
                return localeText;
            }

            if (!triedEnglish && tryLocale.equals(Locale.ENGLISH)) {
                triedEnglish = true;
            }

            tryLocale = broadenLocale(locale);
        } while (tryLocale != null);

        if (!triedEnglish) {
            return getText(Locale.ENGLISH, namespace, namespacedLocaleKey);
        } else {
            return null;
        }
    }

    @Nullable
    abstract LocaleText getText(@NotNull Locale locale, @NotNull String namespace, @NotNull String namespacedLocaleKey);

    static LocaleTextProvider create(@NotNull String namespace, @NotNull Map<Locale, Map<String, String>> locales) {
        validateLocales(namespace, locales);
        return new SoftLocaleTextProvider(namespace, locales);
    }

    private static void validateLocales(String namespace, Map<Locale, Map<String, String>> locales) {
        if (!locales.containsKey(Locale.ENGLISH)) {
            LocaleApi.logErr(namespace, "English (en) locale is missing. English is used as the default locale.");
            return;
        }

        Map<String, String> englishLocaleTexts = locales.get(Locale.ENGLISH);
        List<String> englishLocaleKeys = new ArrayList<>(englishLocaleTexts.keySet());
        englishLocaleKeys.sort(String::compareTo);

        // Null values in english
        for (String localeKey : englishLocaleKeys) {
            if (englishLocaleTexts.get(localeKey) == null) {
                LocaleApi.logErr(namespace, "en/" + localeKey + " is null");
            }
        }

        // Missing/extra keys in other locales
        for (Locale locale : locales.keySet()) {
            if (locale.equals(Locale.ENGLISH)) {
                continue;
            }

            Map<String, String> localeTexts = locales.get(locale);
            for (String englishLocaleKey : englishLocaleKeys) {
                if (!localeTexts.containsKey(englishLocaleKey)) {
                    LocaleApi.logErr(namespace, locale.toString() + " is missing a key: " + englishLocaleKey);
                }
            }

            List<String> localeKeys = new ArrayList<>(localeTexts.keySet());
            localeKeys.sort(String::compareTo);
            for (String localeKey : localeKeys) {
                if (!englishLocaleTexts.containsKey(localeKey)) {
                    LocaleApi.logErr(namespace, locale.toString() + " has an extra key: " + localeKey);
                }
            }
        }
    }

    private static class SoftLocaleTextProvider extends LocaleTextProvider {
        private String namespace;
        private Map<Locale, Map<String, String>> locales;

        SoftLocaleTextProvider(@NotNull String namespace, Map<Locale, Map<String, String>> locales) {
            this.namespace = namespace;
            this.locales = locales;
        }

        @Override
        protected String getNamespace() {
            return namespace;
        }

        @Override
        @NotNull
        Locale getServerLocale() {
            return Locale.ENGLISH;
        }

        @Override
        @Nullable
        LocaleText getText(@NotNull Locale locale, @NotNull String namespace, @NotNull String namespacedLocaleKey) {
            if (getNamespace().equals(namespace)) {
                Map<String, String> localeKeys = locales.get(locale);
                if (localeKeys != null) {
                    String text = localeKeys.get(namespacedLocaleKey);
                    if (text != null) {
                        return new LocaleText(locale, text);
                    }
                }
            }

            return null;
        }
    }
}
