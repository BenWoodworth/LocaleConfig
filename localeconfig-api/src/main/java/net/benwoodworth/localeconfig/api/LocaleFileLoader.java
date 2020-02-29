package net.benwoodworth.localeconfig.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class LocaleFileLoader {
    private LocaleFileLoader() {
    }

    static Map<LocaleKey, Map<String, String>> loadLocaleFiles(
            String namespace,
            URL localeResourceDir
    ) throws IOException {
        List<String> localeResources = getResourceFiles(localeResourceDir);

        Map<LocaleKey, Map<String, String>> locales = new HashMap<>();
        for (String localeResource : localeResources) {
            if (!localeResource.toLowerCase().endsWith(".json")) {
                LocaleApi.logErr(namespace, localeResource + " is not a .json file");
                continue;
            }

            try {
                InputStream resourceStream = LocaleApi.class.getResourceAsStream("/" + localeResource);
                BufferedReader reader = new BufferedReader(new InputStreamReader(resourceStream));
                Map<String, String> json = JsonReader.readLocaleJson(reader);

                Locale locale = getLocaleFromResourceName(localeResource);
                locales.put(LocaleKey.get(locale), json);
            } catch (Exception e) {
                new Exception("Error loading " + localeResource + ": " + e.getMessage(), e).printStackTrace();
            }
        }

        locales.put(LocaleKey.DEFAULT, locales.get(LocaleKey.ENGLISH));

        return locales;
    }

    private static Locale getLocaleFromResourceName(String resourceName) {
        int lastSlash = resourceName.lastIndexOf('/');
        int lastDot = resourceName.lastIndexOf('.');

        String localeTag;
        if (lastDot == -1) {
            localeTag = resourceName.substring(lastSlash + 1);
        } else {
            localeTag = resourceName.substring(lastSlash + 1, lastDot);
        }

        return Locale.forLanguageTag(localeTag);
    }

    private static List<String> getResourceFiles(URL resourceUrl) throws IOException {
        if (resourceUrl.getProtocol().equals("jar")) {
            return listJarDirContents(resourceUrl);
        }

        throw new UnsupportedOperationException("Unable to read contents of " + resourceUrl);
    }

    private static List<String> listJarDirContents(URL resourceUrl) throws IOException {
        List<String> result = new ArrayList<>();

        JarURLConnection connection = (JarURLConnection) resourceUrl.openConnection();
        String path = connection.getEntryName();

        JarFile jarFile = connection.getJarFile();

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            if (!entry.getName().equals(path) && entry.getName().startsWith(path) && !entry.isDirectory()) {
                result.add(entry.getName());
            }
        }

        return result;
    }
}
