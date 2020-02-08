package net.benwoodworth.localeconfig.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        Map<Locale, Map<String, String>> locales;
        try {
            locales = LocaleFileLoader.loadLocaleFiles(namespace, localeResourceDir);
        } catch (Exception e) {
            e.printStackTrace();
            locales = new HashMap<>();
        }

        localeTextProvider = LocaleTextProvider.create(namespace, locales);
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
        return getText(Locale.forLanguageTag(locale.replace('_', '-')), localeKey);
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
