package io.github.giulong.spectrum.drivers;

import io.github.giulong.spectrum.utils.Configuration;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.service.DriverService;

public class Chrome extends Chromium<ChromeOptions, ChromeDriverService, ChromeDriverService.Builder> {

    @Override
    public DriverService.Builder<ChromeDriverService, ChromeDriverService.Builder> getDriverServiceBuilder() {
        return new ChromeDriverService.Builder();
    }

    @Override
    public void buildCapabilities() {
        final Configuration.Drivers driversConfiguration = configuration.getDrivers();
        final Configuration.Drivers.Chrome chrome = driversConfiguration.getChrome();

        capabilities = new ChromeOptions().addArguments(chrome.getArgs());

        chrome.getCapabilities().forEach(capabilities::setExperimentalOption);
        setLoggingPreferencesFrom(driversConfiguration.getLogs());
    }
}
