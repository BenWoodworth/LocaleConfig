package net.benwoodworth.localeconfig.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocaleApi {
    // Replacing ! with . prevents package relocation smartly changing this package String
    private static final String PACKAGE = "net!benwoodworth!localeconfig!api".replace('!', '.');

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

    /**
     * Load locales from the specified resource directory.
     *
     * <ul>
     *     <li>Locale files should be named *.json, e.g. en.json, en-US.json, etc.</li>
     *     <li>The locale json should be an object containing only null/string entries.</li>
     *     <li>English (en.json) is used as a default/fallback, and should not contain null entries.</li>
     *     <li>Null entries will fallback to a broader locale, then English. e.g. zh-TW -> zh -> en</li>
     *     <li>All locale files should contain the same locale keys.</li>
     * </ul>
     *
     * @param namespace         The namespace for the locale keys. Must not contain a colon.
     * @param localeResourceDir The resource directory containing locale json files. e.g. /locales
     */
    public static void load(@NotNull String namespace, @NotNull String localeResourceDir) {
        if (LocaleApi.class.getPackage().getName().equals(PACKAGE)) {
            logErr(namespace, "The package " + PACKAGE + " should be relocated to avoid conflicts");
        }

        if (namespace.contains(":")) {
            throw new IllegalArgumentException("Namespace must not contain a colon.");
        }

        Map<LocaleKey, Map<String, String>> locales;

        URL localeResourceDirUrl = LocaleApi.class.getResource(localeResourceDir);
        if (localeResourceDirUrl == null) {
            throw new RuntimeException("Could not find locale resource directory '" + localeResourceDir + "'");
        }

        try {
            locales = LocaleFileLoader.loadLocaleFiles(namespace, localeResourceDirUrl);
        } catch (Exception e) {
            e.printStackTrace();
            locales = new HashMap<>();
        }

        localeTextProvider = LocaleTextProvider.create(namespace, locales);
    }

    @Nullable
    public static LocaleText get(@NotNull String localeTextKey) {
        return get(getLocaleTextProvider().getServerLocale(), localeTextKey);
    }

    @Nullable
    public static LocaleText get(@NotNull Locale locale, @NotNull String localeTextKey) {
        return getLocaleTextProvider().getText(locale, localeTextKey);
    }

    @Nullable
    public static LocaleText get(@NotNull org.spongepowered.api.entity.living.player.Player player, @NotNull String localeTextKey) {
        return get(getLocale(player), localeTextKey);
    }

    @Nullable
    public static LocaleText get(@NotNull org.bukkit.entity.Player player, @NotNull String localeTextKey) {
        return get(getLocale(player), localeTextKey);
    }

    @Nullable
    public static LocaleText get(@NotNull net.md_5.bungee.api.connection.ProxiedPlayer player, @NotNull String localeTextKey) {
        return get(getLocale(player), localeTextKey);
    }

    @NotNull
    public static Locale getLocale(@NotNull org.spongepowered.api.entity.living.player.Player player) {
        try {
            return player.getLocale();
        } catch (NoSuchMethodError e) {
            return localeTextProvider.getServerLocale();
        }
    }

    @NotNull
    public static Locale getLocale(@NotNull org.bukkit.entity.Player player) {
        try {
            return Locale.forLanguageTag(player.getLocale().replace('_', '-'));
        } catch (NoSuchMethodError e) {
            return localeTextProvider.getServerLocale();
        }
    }

    @NotNull
    public static Locale getLocale(@NotNull net.md_5.bungee.api.connection.ProxiedPlayer player) {
        try {
            return player.getLocale();
        } catch (NoSuchMethodError e) {
            return localeTextProvider.getServerLocale();
        }
    }
}
