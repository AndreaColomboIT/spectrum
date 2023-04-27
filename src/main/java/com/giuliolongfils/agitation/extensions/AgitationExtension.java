package com.giuliolongfils.agitation.extensions;

import com.giuliolongfils.agitation.config.FileReader;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Objects;
import java.util.Properties;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

@Slf4j
@Getter
public class AgitationExtension implements BeforeAllCallback, AfterAllCallback {

    public static final String CLASS_NAME = "className";

    public AgitationExtension() {
        log.debug("Building AgitationExtension");
        final FileReader fileReader = FileReader.getInstance();
        final Properties agitationProperties = fileReader.readProperties("/agitation.properties");
        log.info(String.format(Objects.requireNonNull(fileReader.read("/banner.txt")), agitationProperties.getProperty("version")));
    }

    @Override
    public void beforeAll(final ExtensionContext context) {
        final String className = context.getRequiredTestClass().getAnnotation(DisplayName.class).value();
        log.info("START execution of tests in class {}", className);
        context.getRoot().getStore(GLOBAL).put(CLASS_NAME, className);
    }

    @Override
    public void afterAll(final ExtensionContext context) {
        log.info("END execution of tests in class {}", context.getRoot().getStore(GLOBAL).get(CLASS_NAME));
    }
}
