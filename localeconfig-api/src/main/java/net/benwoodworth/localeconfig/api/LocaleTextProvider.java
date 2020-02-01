package net.benwoodworth.localeconfig.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

abstract class LocaleTextProvider {
    private LocaleTextProvider() {
    }

    protected abstract String getNamespace();

    @NotNull
    abstract Locale getServerLocale();

    @Nullable
    LocaleText getText(@NotNull Locale locale, @NotNull String localeKey) {
        int colonIndex = localeKey.indexOf(':');
        if (colonIndex == -1) {
            return getLocalText(locale, localeKey);
        }

        int nextColonIndex = localeKey.indexOf(':', colonIndex + 1);
        if (nextColonIndex == -1) {
            String namespace = localeKey.substring(0, colonIndex);
            String namespacedLocaleKey = localeKey.substring(colonIndex + 1);

            if (namespace.equals(getNamespace())) {
                return getLocalText(locale, namespacedLocaleKey);
            } else {
                return getGlobalText(locale, namespace, namespacedLocaleKey);
            }
        }

        throw new IllegalArgumentException("Locale key must have at most one colon for a namespace");
    }

    @Nullable
    abstract LocaleText getLocalText(@NotNull Locale locale, @NotNull String localeKey);

    @Nullable
    abstract LocaleText getGlobalText(@NotNull Locale locale, @NotNull String namespace, @NotNull String namespacedLocaleKey);

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
        @NotNull Locale getServerLocale() {
            return Locale.ENGLISH;
        }

        @Override
        @Nullable LocaleText getLocalText(@NotNull Locale locale, @NotNull String localeKey) {
            Map<String, String> localeKeys = locales.get(locale);
            if (localeKeys != null) {
                String text = localeKeys.get(localeKey);
                if (text != null) {
                    return new LocaleText(locale, text);
                }
            }

            return null;
        }

        @Override
        @Nullable LocaleText getGlobalText(@NotNull Locale locale, @NotNull String namespace, @NotNull String namespacedLocaleKey) {
            return null;
        }
    }
}
