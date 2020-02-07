package net.benwoodworth.localeconfig.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

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
        return Substitutor.substitute(text, substitutions);
    }
}
