package net.benwoodworth.localeconfig.api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

class SubstitutorTest {

    private static Map<String, Object> substitutions;

    @BeforeAll
    private static void setup() {
        substitutions = new HashMap<>();
        substitutions.put("a", "a");
        substitutions.put("b5", "bbbbb");
        substitutions.put("c3", "ccc");
    }

    @Test
    void substitute() {
        // Arrange
        String text = "$a ${a} ${ a   } ${a b c} $b5.0$$ ${c3} $c3$a $xyz ${ $";
        String expected = "a a a ${a b c} bbbbb.0$ ccc ccca $xyz ${ $";

        // Act
        LocaleText localeText = new LocaleText("en", text);
        String actual = localeText.substitute(substitutions);

        // Assert
        assertEquals(expected, actual);
    }
}
