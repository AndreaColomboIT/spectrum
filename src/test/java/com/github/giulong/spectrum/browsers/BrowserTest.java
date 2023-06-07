package com.github.giulong.spectrum.browsers;

import com.github.giulong.spectrum.pojos.Configuration;
import com.github.giulong.spectrum.utils.webdrivers.Environment;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Browser")
class BrowserTest {

    private MockedStatic<WebDriverManager> webDriverManagerMockedStatic;
    private MockedStatic<RemoteWebDriver> remoteWebDriverMockedStatic;
    private MockedConstruction<ChromeOptions> chromeOptionsMockedConstruction;
    private MockedConstruction<LoggingPreferences> loggingPreferencesMockedConstruction;

    @Mock
    private Configuration configuration;

    @Mock
    private Configuration.WebDriver webDriverConfig;

    @Mock
    private Configuration.WebDriver.Chrome chromeConfig;

    @Mock
    private Level browserLevel;

    @Mock
    private Level driverLevel;

    @Mock
    private Level performanceLevel;

    @Mock
    private Configuration.SeleniumLogs seleniumLogs;

    @Mock
    private WebDriver webDriver;

    @Mock
    private Configuration.WebDriver.Waits waits;

    @Mock
    private WebDriver.Options options;

    @Mock
    private WebDriver.Timeouts timeouts;

    @Mock
    private Duration implicitDuration;

    @Mock
    private Duration pageLoadDuration;

    @Mock
    private Duration scriptDuration;

    @Mock
    private Configuration.Runtime runtime;

    @Mock
    private Environment environment;

    @InjectMocks
    private Chrome browser;

    @BeforeEach
    public void beforeEach() {
        webDriverManagerMockedStatic = mockStatic(WebDriverManager.class);
        remoteWebDriverMockedStatic = mockStatic(RemoteWebDriver.class);
        chromeOptionsMockedConstruction = mockConstruction(ChromeOptions.class);
        loggingPreferencesMockedConstruction = mockConstruction(LoggingPreferences.class);
    }

    @AfterEach
    public void afterEach() {
        webDriverManagerMockedStatic.close();
        remoteWebDriverMockedStatic.close();
        chromeOptionsMockedConstruction.close();
        loggingPreferencesMockedConstruction.close();
    }

    @Test
    @DisplayName("build should return the instance of the requested webdriver")
    public void build() {
        // buildCapabilitiesFrom stubs
        final List<String> arguments = List.of("args");
        when(configuration.getWebDriver()).thenReturn(webDriverConfig);
        when(webDriverConfig.getChrome()).thenReturn(chromeConfig);
        when(chromeConfig.getArguments()).thenReturn(arguments);
        when(configuration.getSeleniumLogs()).thenReturn(seleniumLogs);
        when(seleniumLogs.getBrowser()).thenReturn(browserLevel);
        when(seleniumLogs.getDriver()).thenReturn(driverLevel);
        when(seleniumLogs.getPerformance()).thenReturn(performanceLevel);
        when(chromeConfig.getExperimentalOptions()).thenReturn(Map.of("one", "value"));

        when(waits.getImplicit()).thenReturn(implicitDuration);
        when(waits.getPageLoadTimeout()).thenReturn(pageLoadDuration);
        when(waits.getScriptTimeout()).thenReturn(scriptDuration);
        when(configuration.getRuntime()).thenReturn(runtime);
        when(runtime.getEnvironment()).thenReturn(environment);
        when(environment.buildFrom(configuration, browser)).thenReturn(webDriver);
        when(webDriver.manage()).thenReturn(options);
        when(options.timeouts()).thenReturn(timeouts);
        when(webDriverConfig.getWaits()).thenReturn(waits);
        when(timeouts.implicitlyWait(implicitDuration)).thenReturn(timeouts);
        when(timeouts.pageLoadTimeout(pageLoadDuration)).thenReturn(timeouts);
        when(timeouts.scriptTimeout(scriptDuration)).thenReturn(timeouts);

        assertEquals(webDriver, browser.build(configuration));
    }
}