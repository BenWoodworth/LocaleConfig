package net.benwoodworth.localeconfig.api;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonReaderTest {
    private BufferedReader stringReader(String string) {
        return new BufferedReader(
                new InputStreamReader(
                        new ByteArrayInputStream(string.getBytes(Charsets.UTF_8))
                )
        );
    }

    @Test
    void readLocaleJson() throws IOException, ParseException {
        String testJson = ("" +
                "{" +
                "'a.b.c' \n\t :    'abcd',\n\t" +
                "'\\u0aA7 \\t \\n \\b \\f' : 'asdf'" +
                "}"
        ).replace("'", "\"");

        Map<Object, Object> expected = ImmutableMap.builder()
                .put("a.b.c", "abcd")
                .put("\u0aA7 \t \n \b \f", "asdf")
                .build();

        assertEquals(
                expected,
                JsonReader.readLocaleJson(stringReader(testJson))
        );
    }
}
