package com.github.giulong.spectrum.utils.testbook.reporters;

import com.github.giulong.spectrum.utils.FileUtils;
import lombok.Getter;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
public class TxtTestBookReporter extends LogTestBookReporter {

    @SuppressWarnings("FieldMayBeFinal")
    private String output = "target/spectrum/testbook/testbook-{timestamp}.txt";

    @Override
    @SneakyThrows
    public void doOutputFrom(final String interpolatedTemplate) {
        final Path outputPath = Paths.get(FileUtils.getInstance().interpolateTimestampFrom(output));
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, interpolatedTemplate.getBytes());
    }
}
