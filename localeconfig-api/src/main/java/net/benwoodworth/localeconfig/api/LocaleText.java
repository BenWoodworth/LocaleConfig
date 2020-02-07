package net.benwoodworth.localeconfig.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocaleText {
    @NotNull
    private final Locale locale;

    @NotNull
    private final String text;

    LocaleText(@NotNull Locale locale, @NotNull String text) {
        this.locale = locale;
        this.text = text;
    }

    @NotNull
    public Locale getLocale() {
        return locale;
    }

    /**
     * @return the raw, unformatted locale text.
     */
    @NotNull String getText() {
        return text;
    }

    @Override
    public int hashCode() {
        return Objects.hash(locale, text);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LocaleText)) return false;

        LocaleText localeText = (LocaleText) obj;
        return Objects.equals(locale, localeText.locale) &&
                Objects.equals(text, localeText.text);
    }

    @Override
    @NotNull
    public String toString() {
        return "LocaleText(locale=" + getLocale() + ", text=" + getText() + ")";
    }

    /**
     * Substitute named values in the locale text:
     * <ul>
     *     <li><code>$key</code> and <code>${key}</code> will be substituted for values in the substitution map.</li>
     *     <li><code>$$</code> will be substituted for <code>$</code></li>
     * </ul>
     *
     * <p>
     *     Keys must start with a letter, and must only contain letters, numbers, and underscores.
     * </p>
     *
     * @param substitutions the values to substitute.
     * @return the substituted locale text.
     */
    @NotNull
    public String substitute(@NotNull Map<@NotNull String, @Nullable Object> substitutions) {
        return substituteText(text, substitutions);
    }

    /**
     * Substitute named values in the locale text.
     *
     * <p>
     * Usage:
     * <pre>
     *     substitute(
     *         "key1", value1,
     *         "key2", value2,
     *         ...
     *     )
     *     </pre>
     * </p>
     *
     * @param substitutionPairs The placeholder/value pairs to be substituted.
     * @return the substituted locale text.
     * @see LocaleText#substitute(Map)
     */
    @NotNull
    public String substitute(Object... substitutionPairs) {
        if (substitutionPairs == null) {
            throw new IllegalArgumentException("substitutionPairs cannot be null");
        } else if (substitutionPairs.length % 2 != 0) {
            throw new IllegalArgumentException("Must have an even number of arguments");
        }

        Map<String, Object> substitutions = new HashMap<>();
        for (int i = 0; i < substitutionPairs.length; i += 2) {
            Object key = substitutionPairs[i];
            Object value = substitutionPairs[i + 1];

            if (!(key instanceof String)) {
                throw new IllegalArgumentException("Argument at index " + i + " is not a String: " + key);
            }

            substitutions.put((String) key, value);
        }

        return substitute(substitutions);
    }

    // region substituteText()

    private static Map<String, String> globalSubstitutions = new HashMap<>();

    // region Global Substitutions
    static {
        globalSubstitutions.put("$", "$");
        globalSubstitutions.put("dark_red", "\u00A74");
        globalSubstitutions.put("red", "\u00A7c");
        globalSubstitutions.put("gold", "\u00A76");
        globalSubstitutions.put("yellow", "\u00A7e");
        globalSubstitutions.put("dark_green", "\u00A72");
        globalSubstitutions.put("green", "\u00A7a");
        globalSubstitutions.put("aqua", "\u00A7b");
        globalSubstitutions.put("dark_aqua", "\u00A73");
        globalSubstitutions.put("dark_blue", "\u00A71");
        globalSubstitutions.put("blue", "\u00A79");
        globalSubstitutions.put("light_purple", "\u00A7d");
        globalSubstitutions.put("dark_purple", "\u00A75");
        globalSubstitutions.put("white", "\u00A7f");
        globalSubstitutions.put("gray", "\u00A77");
        globalSubstitutions.put("dark_gray", "\u00A78");
        globalSubstitutions.put("black", "\u00A70");
        globalSubstitutions.put("reset", "\u00A7r");
        globalSubstitutions.put("bold", "\u00A7l");
        globalSubstitutions.put("italic", "\u00A7o");
        globalSubstitutions.put("underline", "\u00A7n");
        globalSubstitutions.put("strike", "\u00A7m");
        globalSubstitutions.put("obfuscate", "\u00A7k");
    }
    // endregion

    private static Pattern keyPattern = Pattern.compile("[a-zA-Z_]\\w*");
    private static Pattern subPattern = Pattern.compile("\\$(?:(\\$|\\w+)|\\{\\s*(\\w+)\\s*}|)|[^$]*");

    @NotNull
    static String substituteText(@NotNull String text, @NotNull Map<@NotNull String, @Nullable Object> substitutions) {
        // Validate keys
        for (String key : substitutions.keySet()) {
            if (!keyPattern.matcher(key).matches()) {
                throw new IllegalArgumentException("Invalid substitution key: '" + key + "'");
            }

            if (globalSubstitutions.containsKey(key)) {
                throw new IllegalArgumentException("Invalid substitution key: '" + key + "' clashes with global key");
            }
        }

        // Substitute
        Matcher matcher = subPattern.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String matchedKey = matcher.group(1);
            if (matchedKey == null) {
                matchedKey = matcher.group(2);
            }

            if (matchedKey != null) {
                if (substitutions.containsKey(matchedKey)) {
                    result.append(substitutions.get(matchedKey));
                } else if (globalSubstitutions.containsKey(matchedKey)) {
                    result.append(globalSubstitutions.get(matchedKey));
                } else {
                    result.append(matcher.group());
                }
            } else {
                result.append(matcher.group());
            }
        }

        return result.toString();
    }

    // endregion
}
