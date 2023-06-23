package com.github.giulong.spectrum.utils.webdrivers;

import com.github.giulong.spectrum.browsers.Browser;
import com.github.giulong.spectrum.pojos.Configuration;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DockerEnvironment")
class DockerEnvironmentTest {

    @Mock
    private Configuration configuration;

    @Mock
    private Browser<?> browser;

    @Mock
    private WebDriverManager webDriverManager;

    @InjectMocks
    private DockerEnvironment dockerEnvironment;

    @Test
    @DisplayName("buildFrom should configure webDriverManager with docker")
    public void buildFrom() {
        when(browser.getWebDriverManager()).thenReturn(webDriverManager);
        when(webDriverManager.browserInDocker()).thenReturn(webDriverManager);

        dockerEnvironment.buildFrom(configuration, browser, null);

        verify(webDriverManager).create();
    }
}
