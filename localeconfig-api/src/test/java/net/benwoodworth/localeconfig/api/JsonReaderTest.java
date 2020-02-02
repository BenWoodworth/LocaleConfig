package net.benwoodworth.localeconfig.api;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonReaderTest {
    private BufferedReader stringReader(String string) {
        return new BufferedReader(
                new InputStreamReader(
                        new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8))
                )
        );
    }

    @Test
    void readLocaleJson() throws IOException, ParseException {
        String testJson = ("" +
                "{" +
                "'a.b.c' \n\t :    'abcd',\n\t" +
                "'\\u0aA7 \\t \\n \\b \\f' : 'asdf',\r\n\n\t" +
                "'null-value': null\n" +
                "}"
        ).replace("'", "\"");

        Map<String, String> expected = new HashMap<>();
        expected.put("a.b.c", "abcd");
        expected.put("\u0aA7 \t \n \b \f", "asdf");
        expected.put("null-value", null);

        assertEquals(
                expected,
                JsonReader.readLocaleJson(stringReader(testJson))
        );
    }
}
