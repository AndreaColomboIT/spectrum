package io.github.giulong.spectrum.utils.webdrivers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.giulong.spectrum.browsers.Browser;
import io.github.giulong.spectrum.utils.Configuration;
import org.openqa.selenium.WebDriver;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.WRAPPER_OBJECT;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@JsonTypeInfo(use = NAME, include = WRAPPER_OBJECT)
@JsonSubTypes({
        @Type(value = LocalEnvironment.class, name = "local"),
        @Type(value = GridEnvironment.class, name = "grid"),
})
public abstract class Environment {

    @JsonIgnore
    protected final Configuration configuration = Configuration.getInstance();

    public abstract WebDriver setupFor(Browser<?, ?, ?> browser);

    public abstract void shutdown();
}
