package io.github.giulong.spectrum.browsers;

import io.github.giulong.spectrum.pojos.Configuration;
import io.github.giulong.spectrum.utils.webdrivers.Environment;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebDriverBuilder;
import org.openqa.selenium.remote.service.DriverService;
import org.openqa.selenium.support.ThreadGuard;

import java.util.Map;

@Slf4j
public abstract class Browser<T extends AbstractDriverOptions<?>, U extends DriverService, V extends DriverService.Builder<U, V>> {

    protected static final ThreadLocal<WebDriver> WEB_DRIVER_THREAD_LOCAL = new ThreadLocal<>();

    protected T capabilities;

    public abstract DriverService.Builder<U, V> getDriverServiceBuilder();

    public abstract void buildCapabilitiesFrom(Configuration.WebDriver webDriverConfiguration);

    public abstract void mergeGridCapabilitiesFrom(Map<String, String> gridCapabilities);

    public synchronized WebDriver build(final Configuration configuration) {
        final Configuration.WebDriver webDriverConfiguration = configuration.getWebDriver();
        buildCapabilitiesFrom(webDriverConfiguration);

        final Environment environment = configuration.getRuntime().getEnvironment();
        capabilities.setAcceptInsecureCerts(true);

        final RemoteWebDriverBuilder webDriverBuilder = RemoteWebDriver.builder().oneOf(capabilities);
        final WebDriver webDriver = environment.setupFrom(this, webDriverBuilder);
        log.debug("Capabilities: {}", capabilities.toJson());

        final Configuration.WebDriver.Waits waits = webDriverConfiguration.getWaits();
        webDriver
                .manage()
                .timeouts()
                .implicitlyWait(waits.getImplicit())
                .pageLoadTimeout(waits.getPageLoadTimeout())
                .scriptTimeout(waits.getScriptTimeout());

        WEB_DRIVER_THREAD_LOCAL.set(ThreadGuard.protect(webDriver));

        return WEB_DRIVER_THREAD_LOCAL.get();
    }

    public void shutdown() {
        WEB_DRIVER_THREAD_LOCAL.get().quit();
    }
}
