package net.benwoodworth.localeconfig.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

final class LocaleKey {
    @NotNull
    static LocaleKey DEFAULT = new LocaleKey(null, null, null);
    static LocaleKey ENGLISH = new LocaleKey(Locale.ENGLISH);

    private final String language;
    private final String country;
    private final String variant;

    private LocaleKey(@Nullable String language, @Nullable String country, @Nullable String variant) {
        this.language = (language == null || language.isEmpty()) ? null : language;
        this.country = (country == null || country.isEmpty()) ? null : country;
        this.variant = (variant == null || variant.isEmpty()) ? null : variant;
    }

    LocaleKey(Locale locale) {
        this(
                locale.getLanguage().toLowerCase(Locale.ENGLISH),
                locale.getCountry().toLowerCase(Locale.ENGLISH),
                locale.getVariant().toLowerCase(Locale.ENGLISH)
        );
    }

    @NotNull
    LocaleKey broadened() {
        if (variant != null) {
            return new LocaleKey(language, country, null);
        }
        if (country != null) {
            return new LocaleKey(language, null, null);
        }
        return DEFAULT;
    }

    @Override
    public String toString() {
        return "LocaleKey{" + language + ", " + country + ", " + variant + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocaleKey that = (LocaleKey) o;
        return Objects.equals(language, that.language) &&
                Objects.equals(country, that.country) &&
                Objects.equals(variant, that.variant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(language, country, variant);
    }
}
