package net.benwoodworth.localeconfig.api;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

abstract class Platform {
    private Map<Locale, Map<String, String>> locales = null;

    private Platform() {
        loadLocales();
    }


    @NotNull Map<Locale, Map<String, String>> getLocales() {
        if (locales == null) {
            locales = loadLocales();
        }

        return locales;
    }


    protected abstract @NotNull Map<Locale, Map<String, String>> loadLocales();

    private static Platform platform = null;

    static @NotNull Platform getPlatform() {
        if (platform != null) {
            return platform;
        }

        try {
            Class.forName("org.bukkit.Bukkit");
            platform = new BukkitPlatform();
            return platform;
        } catch (ClassNotFoundException ignored) {
        }

        try {
            Class.forName("org.spongepowered.api.Sponge");
            platform = new SpongePlatform();
            return platform;
        } catch (ClassNotFoundException ignored) {
        }

        platform = new DefaultPlatform();
        return platform;
    }

    private static class DefaultPlatform extends Platform {
        @Override
        protected @NotNull Map<Locale, Map<String, String>> loadLocales() {
            return new HashMap<>();
        }
    }

    private static class BukkitPlatform extends Platform {
        @Override
        protected @NotNull Map<Locale, Map<String, String>> loadLocales() {
            URL bukkitLocales = Bukkit.class.getResource("/assets/minecraft/lang");

            if (bukkitLocales != null) {
                try {
                    return LocaleFileLoader.loadLocaleFiles("minecraft", bukkitLocales);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return new HashMap<>();
        }
    }

    private static class SpongePlatform extends Platform {
        @Override
        protected @NotNull Map<Locale, Map<String, String>> loadLocales() {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }
}