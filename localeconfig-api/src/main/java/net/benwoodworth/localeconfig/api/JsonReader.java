package net.benwoodworth.localeconfig.api;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

class JsonReader {
    private JsonReader() {
    }

    public static Map<String, String> readLocaleJson(BufferedReader reader) throws IOException, ParseException {
        return readLocaleElement(reader);
    }

    private static void readWs(BufferedReader reader) throws IOException {
        while (true) {
            reader.mark(1);

            switch (reader.read()) {
                case -1:
                    return;

                case 0x0020:
                case 0x000A:
                case 0x000D:
                case 0x0009:
                    break;

                default:
                    reader.reset();
                    return;
            }
        }
    }

    private static Map<@NotNull String, @NotNull String> readLocaleElement(BufferedReader reader) throws IOException, ParseException {
        readWs(reader);
        Map<String, String> result = readLocaleObject(reader);
        readWs(reader);

        int ch = reader.read();
        if (ch != -1) {
            throw new ParseException("Expecting EOF, but found '" + (char) ch + "'", 0);
        }
        return result;
    }

    private static Map<@NotNull String, @NotNull String> readLocaleObject(BufferedReader reader) throws IOException, ParseException {
        int ch = reader.read();
        if (ch != '{') {
            throw new ParseException("Expecting object, but found '" + (char) ch + "'", 0);
        }

        Map<String, String> result = new HashMap<>();

        readWs(reader);

        reader.mark(1);
        ch = reader.read();
        if (ch == '}') {
            return result;
        } else {
            reader.reset();
            readLocaleMember(reader, result);
        }

        while (true) {
            readWs(reader);

            reader.mark(1);
            ch = reader.read();
            switch (ch) {
                case ',':
                    readLocaleMember(reader, result);
                    break;

                case '}':
                    return result;

                case -1:
                    throw new ParseException("Expecting ',' or '}', but found EOF", 0);

                default:
                    throw new ParseException("Expecting ',' or '}', but found '" + (char) ch + "'", 0);
            }
        }
    }

    private static void readLocaleMember(BufferedReader reader, Map<String, String> toMap) throws IOException, ParseException {
        readWs(reader);
        String key = readString(reader);
        readWs(reader);

        int ch = reader.read();
        if (ch != ':') {
            throw new ParseException("Expecting colon, but found '" + (char) ch + "'", 0);
        }

        readWs(reader);
        String value = readString(reader);
        readWs(reader);

        toMap.put(key, value);
    }

    private static String readString(BufferedReader reader) throws IOException, ParseException {
        if (reader.read() != '"') {
            throw new ParseException("Expecting open quotes", 0);
        }

        String result = readCharacters(reader);

        if (reader.read() != '"') {
            throw new ParseException("Expecting close quotes", 0);
        }

        return result;
    }

    private static String readCharacters(BufferedReader reader) throws IOException, ParseException {
        StringBuilder sb = new StringBuilder();

        while (true) {
            int ch = readCharacter(reader);
            if (ch == -1) {
                break;
            } else {
                sb.append((char) ch);
            }
        }

        return sb.toString();
    }

    private static int readCharacter(BufferedReader reader) throws IOException, ParseException {
        reader.mark(1);

        int ch = reader.read();
        switch (ch) {
            case '"':
                reader.reset();
                return -1;

            case '\\':
                return readEscape(reader);

            default:
                if (0x20 <= ch && ch <= 0x10FFFF) {
                    return (char) ch;
                } else {
                    reader.reset();
                    return -1;
                }
        }
    }

    private static int readEscape(BufferedReader reader) throws IOException, ParseException {
        int ch = reader.read();
        switch (ch) {
            case '"':
                return '\"';
            case '\\':
                return '\\';
            case '/':
                return '/';
            case 'b':
                return '\b';
            case 'f':
                return '\f';
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 't':
                return '\t';
            case 'u':
                return (readHex(reader) << 12) +
                        (readHex(reader) << 8) +
                        (readHex(reader) << 4) +
                        readHex(reader);
            case -1:
                throw new ParseException("Expecting escape character but found EOF", 0);
            default:
                throw new ParseException("Invalid escape sequence: \\" + (char) ch, 0);
        }
    }

    private static int readHex(BufferedReader reader) throws IOException, ParseException {
        int ch = reader.read();

        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        } else if ('a' <= ch && ch <= 'f') {
            return ch - 'a' + 0xA;
        } else if ('A' <= ch && ch <= 'F') {
            return ch - 'A' + 0xA;
        } else if (ch == -1) {
            throw new ParseException("Expecting hex character, but found EOF", 0);
        } else {
            throw new ParseException("Invalid hex character: " + (char) ch, 0);
        }
    }
}
