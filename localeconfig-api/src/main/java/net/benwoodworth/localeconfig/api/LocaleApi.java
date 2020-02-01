package net.benwoodworth.localeconfig.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class LocaleApi {
    private static LocaleTextProvider localeTextProvider = null;

    private LocaleApi() {
    }

    static void log(String namespace, String message) {
        System.out.println("[LocaleApi] " + namespace + ": " + message);
    }

    static void logErr(String namespace, String message) {
        System.err.println("[LocaleApi] " + namespace + ": " + message);
    }

    @NotNull
    private static LocaleTextProvider getLocaleTextProvider() {
        if (localeTextProvider == null) {
            throw new IllegalStateException("Locales have not been loaded. Must call LocaleApi.loadLocales() first.");
        }

        return localeTextProvider;
    }

    public static void loadLocales(@NotNull String namespace, @NotNull String resourceDir) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    public static LocaleText getText(@NotNull String localeKey) {
        return getText(getLocaleTextProvider().getServerLocale(), localeKey);
    }

    @Nullable
    public static LocaleText getText(@NotNull Locale locale, @NotNull String localeKey) {
        return getLocaleTextProvider().getText(locale, localeKey);
    }

    @Nullable
    public static LocaleText getText(@NotNull String locale, @NotNull String localeKey) {
        return getText(Locale.forLanguageTag(locale), localeKey);
    }

    @Nullable
    public static LocaleText getText(@NotNull org.spongepowered.api.entity.living.player.Player player, @NotNull String localeKey) {
        return getText(player.getLocale(), localeKey);
    }

    @Nullable
    public static LocaleText getText(@NotNull org.bukkit.entity.Player player, @NotNull String localeKey) {
        return getText(player.getLocale(), localeKey);
    }

    @Nullable
    public static LocaleText getText(@NotNull net.md_5.bungee.api.connection.ProxiedPlayer player, @NotNull String localeKey) {
        return getText(player.getLocale(), localeKey);
    }
}
