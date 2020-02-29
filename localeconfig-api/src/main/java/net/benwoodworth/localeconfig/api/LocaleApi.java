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

    private static boolean canGetSpongePlayerLocale = true;
    private static boolean canGetBukkitPlayerLocale = true;
    private static boolean canGetBungeePlayerLocale = true;

    private LocaleApi() {
    }

    static void log(String namespace, String message) {
        System.out.println("[LocaleConfig] " + namespace + ": " + message);
    }

    static void logErr(String namespace, String message) {
        System.err.println("[LocaleConfig] " + namespace + ": " + message);
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

    /**
     * Gets LocaleText for the specified locale and localeTextKey.
     * <p>
     * The locale text key can specify a namespace with "namespace:locale.text.key".
     * <p>
     * If no namespace is specified, the namespace that was used in {@link #load(String, String)} will be used.
     *
     * @param locale        the locale of the text to get, or null for server default.
     * @param localeTextKey the key of the locale text to get.
     */
    @Nullable
    public static LocaleText get(@Nullable Locale locale, @NotNull String localeTextKey) {
        return getLocaleTextProvider().getText(locale, localeTextKey);
    }

    /**
     * Gets LocaleText using the default locale.
     *
     * @see #get(Locale, String)
     */
    @Nullable
    public static LocaleText get(@NotNull String localeTextKey) {
        return get((Locale) null, localeTextKey);
    }

    /**
     * Gets LocaleText for the specified player's locale and localeTextKey.
     *
     * @see #get(Locale, String)
     */
    @Nullable
    public static LocaleText get(@NotNull org.spongepowered.api.entity.living.player.Player player, @NotNull String localeTextKey) {
        return get(getLocale(player), localeTextKey);
    }

    /**
     * Gets LocaleText for the specified player's locale and localeTextKey.
     *
     * @see #get(Locale, String)
     */
    @Nullable
    public static LocaleText get(@NotNull org.bukkit.entity.Player player, @NotNull String localeTextKey) {
        return get(getLocale(player), localeTextKey);
    }

    /**
     * Gets LocaleText for the specified player's locale and localeTextKey.
     *
     * @see #get(Locale, String)
     */
    @Nullable
    public static LocaleText get(@NotNull net.md_5.bungee.api.connection.ProxiedPlayer player, @NotNull String localeTextKey) {
        return get(getLocale(player), localeTextKey);
    }

    /**
     * Gets the specified player's locale.
     *
     * @return the player's locale, or null if unable to get it.
     * @see #get(Locale, String)
     */
    @Nullable
    public static Locale getLocale(@NotNull org.spongepowered.api.entity.living.player.Player player) {
        if (!canGetSpongePlayerLocale) {
            return null;
        }

        try {
            return player.getLocale();
        } catch (NoSuchMethodError e) {
            canGetSpongePlayerLocale = false;
            return null;
        }
    }

    /**
     * Gets the specified player's locale.
     *
     * @return the player's locale, or null if unable to get it.
     * @see #get(Locale, String)
     */
    @Nullable
    public static Locale getLocale(@NotNull org.bukkit.entity.Player player) {
        if (!canGetBukkitPlayerLocale) {
            return null;
        }

        String localeTag;
        try {
            localeTag = player.getLocale();
        } catch (NoSuchMethodError e) {
            canGetBukkitPlayerLocale = false;
            return null;
        }

        if (localeTag == null) {
            return null;
        }

        try {
            return Locale.forLanguageTag(localeTag.replace('_', '-'));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the specified player's locale.
     *
     * @return the player's locale, or null if unable to get it.
     * @see #get(Locale, String)
     */
    @Nullable
    public static Locale getLocale(@NotNull net.md_5.bungee.api.connection.ProxiedPlayer player) {
        if (!canGetBungeePlayerLocale) {
            return null;
        }

        try {
            return player.getLocale();
        } catch (NoSuchMethodError e) {
            canGetBungeePlayerLocale = false;
            return null;
        }
    }
}
