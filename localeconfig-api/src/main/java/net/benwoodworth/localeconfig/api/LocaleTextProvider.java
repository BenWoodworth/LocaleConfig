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
    abstract LocaleText getLocalText(@NotNull Locale locale, @NotNull String namespacedLocaleKey);

    @Nullable
    abstract LocaleText getGlobalText(@NotNull Locale locale, @NotNull String namespace, @NotNull String namespacedLocaleKey);
}
