package net.benwoodworth.localeconfig.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

abstract class LocaleTextProvider {
    private LocaleTextProvider() {
    }

    protected abstract String getNamespace();

    @NotNull
    abstract Locale getServerLocale();

    @Nullable
    LocaleText getText(@NotNull Locale locale, @NotNull String localeTextKey) {
        String namespace;
        String namespacedLocaleTextKey;
        {
            int colonIndex = localeTextKey.indexOf(':');
            if (colonIndex == -1) {
                namespace = getNamespace();
                namespacedLocaleTextKey = localeTextKey;
            } else {
                int nextColonIndex = localeTextKey.indexOf(':', colonIndex + 1);
                if (nextColonIndex != -1) {
                    throw new IllegalArgumentException("Locale key must have at most one colon for a namespace");
                }

                namespace = localeTextKey.substring(0, colonIndex);
                namespacedLocaleTextKey = localeTextKey.substring(colonIndex + 1);
            }
        }

        LocaleKey localeKey = new LocaleKey(locale);
        do {
            LocaleText localeText = getText(localeKey, locale, namespace, namespacedLocaleTextKey);
            if (localeText != null) {
                return localeText;
            }

            localeKey = localeKey.broadened();
        } while (!localeKey.equals(LocaleKey.DEFAULT));

        return getText(localeKey, locale, namespace, namespacedLocaleTextKey);
    }

    @Nullable
    protected abstract LocaleText getText(
            @NotNull LocaleKey localeKey,
            @NotNull Locale locale,
            @NotNull String namespace,
            @NotNull String namespacedLocaleTextKey
    );

    static LocaleTextProvider create(@NotNull String namespace, @NotNull Map<LocaleKey, Map<String, String>> locales) {
        validateLocales(namespace, locales);
        return new SoftLocaleTextProvider(namespace, locales);
    }

    private static void validateLocales(String namespace, Map<LocaleKey, Map<String, String>> locales) {
        if (!locales.containsKey(LocaleKey.ENGLISH)) {
            LocaleApi.logErr(namespace, "English (en) locale is missing. English is used as the default locale.");
            return;
        }

        Map<String, String> englishLocaleTexts = locales.get(LocaleKey.ENGLISH);
        List<String> englishLocaleTextKeys = new ArrayList<>(englishLocaleTexts.keySet());
        englishLocaleTextKeys.sort(String::compareTo);

        // Null values in english
        for (String localeTextKey : englishLocaleTextKeys) {
            if (englishLocaleTexts.get(localeTextKey) == null) {
                LocaleApi.logErr(namespace, "en/" + localeTextKey + " is null");
            }
        }

        // Missing/extra keys in other locales
        for (LocaleKey localeKey : locales.keySet()) {
            if (localeKey.equals(LocaleKey.ENGLISH)) {
                continue;
            }

            Map<String, String> localeTexts = locales.get(localeKey);
            for (String englishLocaleTextKey : englishLocaleTextKeys) {
                if (!localeTexts.containsKey(englishLocaleTextKey)) {
                    LocaleApi.logErr(namespace, localeKey.toString() + " is missing a key: " + englishLocaleTextKey);
                }
            }

            List<String> localeTextKeys = new ArrayList<>(localeTexts.keySet());
            localeTextKeys.sort(String::compareTo);
            for (String localeTextKey : localeTextKeys) {
                if (!englishLocaleTexts.containsKey(localeTextKey)) {
                    LocaleApi.logErr(namespace, localeKey.toString() + " has an extra key: " + localeTextKey);
                }
            }
        }
    }

    private static class SoftLocaleTextProvider extends LocaleTextProvider {
        private String namespace;
        private Map<LocaleKey, Map<String, String>> locales;

        SoftLocaleTextProvider(@NotNull String namespace, Map<LocaleKey, Map<String, String>> locales) {
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
        protected LocaleText getText(
                @NotNull LocaleKey localeKey,
                @NotNull Locale locale,
                @NotNull String namespace,
                @NotNull String namespacedLocaleTextKey
        ) {
            if (getNamespace().equals(namespace)) {
                Map<String, String> localeTextKeys = locales.get(localeKey);
                if (localeTextKeys != null) {
                    String text = localeTextKeys.get(namespacedLocaleTextKey);
                    if (text != null) {
                        return new LocaleText(locale, text);
                    }
                }
            }

            return null;
        }
    }
}
