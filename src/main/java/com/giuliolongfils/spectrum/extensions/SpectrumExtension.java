package com.giuliolongfils.spectrum.extensions;

import com.giuliolongfils.spectrum.config.FileReader;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Objects;
import java.util.Properties;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

@Slf4j
public class SpectrumExtension implements BeforeAllCallback, AfterAllCallback {

    public static final String CLASS_NAME = "className";

    public SpectrumExtension() {
        log.debug("Building SpectrumExtension");
        final FileReader fileReader = FileReader.getInstance();
        final Properties spectrumProperties = fileReader.readProperties("/spectrum.properties");
        log.info(String.format(Objects.requireNonNull(fileReader.read("/banner.txt")), spectrumProperties.getProperty("version")));
    }

    @Override
    public void beforeAll(final ExtensionContext context) {
        final String className = context.getDisplayName();
        log.info("START execution of tests in class {}", className);
        context.getStore(GLOBAL).put(CLASS_NAME, className);
    }

    @Override
    public void afterAll(final ExtensionContext context) {
        log.info("END execution of tests in class {}", context.getStore(GLOBAL).get(CLASS_NAME));
    }
}
