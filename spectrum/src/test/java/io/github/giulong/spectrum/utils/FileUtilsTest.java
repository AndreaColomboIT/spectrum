package io.github.giulong.spectrum.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.stream.Stream;

import static java.lang.System.lineSeparator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileUtils")
class FileUtilsTest {

    @InjectMocks
    private FileUtils fileUtils;

    @Test
    @DisplayName("getInstance should return the singleton")
    public void getInstance() {
        //noinspection EqualsWithItself
        assertSame(FileUtils.getInstance(), FileUtils.getInstance());
    }

    @DisplayName("read should return the correct result")
    @ParameterizedTest(name = "reading file {0} we expect {1}")
    @MethodSource("valuesProvider")
    public void read(String file, String expected) {
        assertEquals(expected, fileUtils.read(file));
    }

    public static Stream<Arguments> valuesProvider() {
        return Stream.of(
                arguments("/test.yaml", "key: value" + lineSeparator() + "objectKey:" + lineSeparator() + "  objectField: objectValue" + lineSeparator() + "internalKey:" + lineSeparator() + "  field: ignored"),
                arguments("not-existing", ""));
    }

    @DisplayName("readTemplate should return the correct result")
    @ParameterizedTest(name = "reading file {0} we expect {1}")
    @MethodSource("readTemplateValuesProvider")
    public void readTemplate(String file, String expected) {
        assertEquals(expected, fileUtils.readTemplate(file));
    }

    public static Stream<Arguments> readTemplateValuesProvider() {
        return Stream.of(
                arguments("template-test.yaml", "key: value" + lineSeparator() + "objectKey:" + lineSeparator() + "  objectField: objectValue" + lineSeparator() + "internalKey:" + lineSeparator() + "  field: ignored"),
                arguments("not-existing", ""));
    }

    @Test
    @DisplayName("interpolate should return the provided file with the placeholder replaced with vars from the provided map")
    public void interpolate() {
        assertEquals(
                "key: value" + lineSeparator() + "objectKey:" + lineSeparator() + "  objectField: objectValue",
                fileUtils.interpolate("/interpolate.yaml", Map.of("{{value}}", "value", "{{objectValue}}", "objectValue")));
    }

    @DisplayName("interpolateTimestampFrom should replace the timestamp from the provided file name")
    @ParameterizedTest(name = "with fileName {0} we expect {2}")
    @MethodSource("fileNamesProvider")
    public void interpolateTimestampFrom(final String fileName, final String expected) {
        assertThat(fileUtils.interpolateTimestampFrom(fileName), matchesPattern(expected));
    }

    public static Stream<Arguments> fileNamesProvider() {
        return Stream.of(
                arguments("fileName.html", "fileName.html"),
                arguments("fileName-{timestamp}.html", "fileName-[0-9]{2}-[0-9]{2}-[0-9]{4}_[0-9]{2}-[0-9]{2}-[0-9]{2}.html"),
                arguments("fileName-{timestamp:dd-MM-yyyy_HH-mm-ss}.html", "fileName-[0-9]{2}-[0-9]{2}-[0-9]{4}_[0-9]{2}-[0-9]{2}-[0-9]{2}.html"),
                arguments("fileName-{timestamp:dd-MM-yyyy}.html", "fileName-[0-9]{2}-[0-9]{2}-[0-9]{4}.html")
        );
    }
}
