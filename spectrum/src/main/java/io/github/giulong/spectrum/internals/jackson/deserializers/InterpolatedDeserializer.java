package io.github.giulong.spectrum.internals.jackson.deserializers;

import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.giulong.spectrum.SpectrumSessionListener.VARS;

@Slf4j
public abstract class InterpolatedDeserializer<T> extends JsonDeserializer<T> {

    public static final Pattern PATTERN = Pattern.compile("(?<placeholder>\\$\\{(?<varName>[\\w.]+)(:-(?<defaultValue>[\\w~.:/\\\\]+))?})");

    String interpolate(final String value, final String currentName) {
        final Matcher matcher = PATTERN.matcher(value);

        String interpolatedValue = value;
        while (matcher.find()) {
            final String varName = matcher.group("varName");
            final String placeholder = matcher.group("placeholder");
            final String defaultValue = matcher.group("defaultValue");
            final String envVar = System.getenv(varName);
            final String envVarOrPlaceholder = envVar != null ? envVar : placeholder;
            final String systemProperty = System.getProperty(varName, envVarOrPlaceholder);

            interpolatedValue = interpolatedValue.replace(placeholder, VARS.getOrDefault(varName, systemProperty));

            if (value.equals(interpolatedValue)) {
                if (defaultValue == null) {
                    log.warn("No variable found to interpolate '{}' for key '{}'", value, currentName);
                } else {
                    log.trace("No variable found to interpolate '{}' for key '{}'. Using provided default '{}'", value, currentName, defaultValue);
                    interpolatedValue = value.replace(placeholder, defaultValue);
                }
            } else {
                log.trace("Interpolated value for key '{}: {}' -> '{}'", currentName, value, interpolatedValue);
            }
        }

        return interpolatedValue;
    }
}
