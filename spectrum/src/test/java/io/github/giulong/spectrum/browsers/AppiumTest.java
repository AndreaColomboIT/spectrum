package io.github.giulong.spectrum.browsers;

import io.appium.java_client.service.local.AppiumServiceBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static io.github.giulong.spectrum.browsers.Appium.APP_CAPABILITY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Appium")
class AppiumTest {

    @Mock
    private Map<String, Object> capabilities;

    @InjectMocks
    private Android appium;

    @Test
    @DisplayName("getDriverServiceBuilder should return an instance of AppiumDriverServiceBuilder")
    public void getDriverServiceBuilder() {
        MockedConstruction<AppiumServiceBuilder> appiumServiceBuilderMockedConstruction = mockConstruction(AppiumServiceBuilder.class);

        final AppiumServiceBuilder actual = (AppiumServiceBuilder) appium.getDriverServiceBuilder();
        assertEquals(appiumServiceBuilderMockedConstruction.constructed().getFirst(), actual);

        appiumServiceBuilderMockedConstruction.close();
    }

    @Test
    @DisplayName("adjustCapabilitiesFrom should set the app path absolute if it's relative")
    public void adjustCapabilitiesFrom() {
        final String appPath = "relative/path";
        final String appAbsolutePath = System.getProperty("user.dir") + "/relative/path";

        when(capabilities.get(APP_CAPABILITY)).thenReturn(appPath);

        assertEquals(capabilities, appium.adjustCapabilitiesFrom(capabilities));

        verify(capabilities).put(APP_CAPABILITY, appAbsolutePath);
    }

    @Test
    @DisplayName("adjustCapabilitiesFrom should just return the provided capabilities if the app path absolute is already absolute")
    public void adjustCapabilitiesFromAbsolute() {
        final String appPath = "/absolute/path";

        when(capabilities.get(APP_CAPABILITY)).thenReturn(appPath);

        assertEquals(capabilities, appium.adjustCapabilitiesFrom(capabilities));

        verify(capabilities, never()).put(anyString(), anyString());
    }
}