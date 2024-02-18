package io.github.giulong.spectrum.drivers;

import io.appium.java_client.mac.Mac2Driver;
import io.appium.java_client.mac.options.Mac2Options;

import java.net.URL;

public class Mac2 extends Appium<Mac2Options, Mac2Driver> {

    @Override
    public void buildCapabilities() {
        capabilities = new Mac2Options(configuration
                .getWebDriver()
                .getMac2()
                .getCapabilities());
    }

    @Override
    public Mac2Driver buildDriverFor(final URL url) {
        return new Mac2Driver(url, capabilities);
    }
}
