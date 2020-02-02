package net.benwoodworth.localeconfig.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    /**
     * Load locales from the specified resource directory.
     *
     * <ul>
     *     <li>Locale files should be named *.json, e.g. en.json, en-us.json, etc.</li>
     *     <li>The locale json should be an object containing only null/string entries.</li>
     *     <li>English (en.json) is used as a default/fallback, and should not contain null entries.</li>
     *     <li>Null entries will fallback to a broader locale, then English. e.g. zh_TW -> zh -> en</li>
     *     <li>All locale files should contain the same locale keys.</li>
     * </ul>
     *
     * @param namespace         The namespace for the locale keys. Must not contain a colon.
     * @param localeResourceDir The resource directory containing locale json files. e.g. /locales
     */
    public static void loadLocales(@NotNull String namespace, @NotNull String localeResourceDir) {
        if (namespace.contains(":")) {
            throw new IllegalArgumentException("Namespace must not contain a colon.");
        }

        URL resourceUrl = LocaleApi.class.getResource(localeResourceDir);
        if (resourceUrl == null) {
            throw new RuntimeException("Could not find locale resource directory '" + localeResourceDir + "'");
        }

        File resourceFile;
        try {
            resourceFile = new File(resourceUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to get file locale directory from URL '" + resourceUrl + "'", e);
        }

        File[] localeFiles = resourceFile.listFiles();
        if (localeFiles == null) {
            throw new IllegalArgumentException("Resource '" + localeResourceDir + "' is not a directory.");
        }

        Map<Locale, Map<String, String>> locales = new HashMap<>();
        for (File localeFile : localeFiles) {
            String fileName = localeFile.getName();
            if (!localeFile.isFile()) {
                logErr(namespace, fileName + " is not a file.");
                continue;
            } else if (!fileName.toLowerCase().endsWith(".json")) {
                logErr(namespace, fileName + " is not a .json file");
                continue;
            }

            try {
                BufferedReader reader = new BufferedReader(new FileReader(localeFile));
                Map<String, String> json = JsonReader.readLocaleJson(reader);

                String localeTag = fileName.substring(0, fileName.length() - 5); // Without .json
                Locale locale = Locale.forLanguageTag(localeTag);

                locales.put(locale, json);
            } catch (Exception e) {
                new Exception("Error loading " + fileName + ": " + e.getMessage(), e).printStackTrace();
            }
        }

        localeTextProvider = LocaleTextProvider.create(namespace, locales);
    }

    /**
     * Load locales from the /locales resource directory.
     *
     * @see LocaleApi::loadLocales(String, String)
     */
    public static void loadLocales(@NotNull String namespace) {
        loadLocales(namespace, "/locales");
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
