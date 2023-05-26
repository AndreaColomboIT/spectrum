package com.giuliolongfils.spectrum;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.model.Media;
import com.giuliolongfils.spectrum.browsers.Browser;
import com.giuliolongfils.spectrum.pojos.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.aventstack.extentreports.Status.*;
import static com.giuliolongfils.spectrum.SpectrumEntity.SCREEN_SHOT_FOLDER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;
import static org.openqa.selenium.OutputType.BYTES;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpectrumEntity")
class SpectrumEntityTest {

    private static final String UUID_REGEX = "([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})\\.png";

    @Mock
    private WebDriverWait downloadWait;

    @Mock
    private Configuration configuration;

    @Mock
    private Configuration.Runtime runtime;

    @Mock
    private Configuration.Extent extent;

    @Mock
    private ExtentTest extentTest;

    @SuppressWarnings("rawtypes")
    @Mock
    private Browser browser;

    @Mock(extraInterfaces = TakesScreenshot.class)
    private WebDriver webDriver;

    @Mock
    private WebElement webElement;

    @Mock
    private Actions actions;

    @Captor
    private ArgumentCaptor<Function<WebDriver, Boolean>> functionArgumentCaptor;

    @InjectMocks
    private DummySpectrumEntity<?> spectrumEntity;

    @BeforeEach
    public void beforeEach() {
        SpectrumEntity.configuration = configuration;
    }

    private Path addScreenshotToReportStubs() throws IOException {
        final Path reportsFolder = Files.createTempDirectory("reportsFolder");
        reportsFolder.toFile().deleteOnExit();

        when(configuration.getExtent()).thenReturn(extent);
        when(extent.getReportFolder()).thenReturn(reportsFolder.toString());
        when(configuration.getRuntime()).thenReturn(runtime);
        //noinspection unchecked
        when(runtime.getBrowser()).thenReturn(browser);
        when(browser.takesPartialScreenshots()).thenReturn(false);

        when(((TakesScreenshot) webDriver).getScreenshotAs(BYTES)).thenReturn(new byte[]{1, 2, 3});

        return reportsFolder;
    }

    private void addScreenshotToReportVerifications(final Path reportsFolder, final Media screenShot, final Status status) throws IOException {
        assertNotNull(screenShot);

        final String screenShotName = screenShot.getPath();
        final Path screenShotPath = Paths.get(reportsFolder.toString(), screenShotName);
        assertTrue(Files.exists(screenShotPath));
        assertThat(screenShotName, matchesPattern(SCREEN_SHOT_FOLDER + "/" + UUID_REGEX));
        verify(((TakesScreenshot) webDriver)).getScreenshotAs(BYTES);
        verify(webDriver, never()).findElement(By.tagName("body"));
        verify(extentTest).log(status, "<div class=\"screenshot-container\">blah</div>", screenShot);

        Files.delete(screenShotPath);
    }

    @Test
    @DisplayName("hover should move the pointer over the provided webElement")
    public void hover() {
        when(actions.moveToElement(webElement)).thenReturn(actions);
        spectrumEntity.hover(webElement);
        verify(actions).perform();
    }

    @Test
    @DisplayName("infoWithScreenshot should delegate to addScreenshotToReport")
    public void infoWithScreenshot() throws IOException {
        addScreenshotToReportVerifications(addScreenshotToReportStubs(), spectrumEntity.infoWithScreenshot("blah"), INFO);
    }

    @Test
    @DisplayName("warningWithScreenshot should delegate to addScreenshotToReport")
    public void warningWithScreenshot() throws IOException {
        addScreenshotToReportVerifications(addScreenshotToReportStubs(), spectrumEntity.warningWithScreenshot("blah"), WARNING);
    }

    @Test
    @DisplayName("failWithScreenshot should delegate to addScreenshotToReport")
    public void failWithScreenshot() throws IOException {
        addScreenshotToReportVerifications(addScreenshotToReportStubs(), spectrumEntity.failWithScreenshot("blah"), FAIL);
    }

    @Test
    @DisplayName("addScreenshotToReport should add the provided message to the report, at the provided status level and attaching a screenshot")
    public void addScreenshotToReport() throws IOException {
        addScreenshotToReportVerifications(addScreenshotToReportStubs(), spectrumEntity.addScreenshotToReport("blah", INFO), INFO);
    }

    @Test
    @DisplayName("addScreenshotToReport should work also if the browser can take partial screenshots")
    public void addScreenshotToReportTakesPartialScreenshots() throws IOException {
        final Path reportsFolder = Files.createTempDirectory("reportsFolder");
        reportsFolder.toFile().deleteOnExit();

        when(configuration.getExtent()).thenReturn(extent);
        when(extent.getReportFolder()).thenReturn(reportsFolder.toString());
        when(configuration.getRuntime()).thenReturn(runtime);
        //noinspection unchecked
        when(runtime.getBrowser()).thenReturn(browser);
        when(browser.takesPartialScreenshots()).thenReturn(true);
        when(webDriver.findElement(By.tagName("body"))).thenReturn(webElement);
        when(webElement.getScreenshotAs(BYTES)).thenReturn(new byte[]{1, 2, 3});

        final Media screenShot = spectrumEntity.addScreenshotToReport("blah", INFO);
        assertNotNull(screenShot);

        final String screenShotName = screenShot.getPath();
        final Path screenShotPath = Paths.get(reportsFolder.toString(), screenShotName);
        assertTrue(Files.exists(screenShotPath));
        assertThat(screenShotName, matchesPattern(SCREEN_SHOT_FOLDER + "/" + UUID_REGEX));
        verify(extentTest).log(INFO, "<div class=\"screenshot-container\">blah</div>", screenShot);

        Files.delete(screenShotPath);
    }

    @DisplayName("deleteDownloadsFolder should delete and recreate the downloads folder")
    @ParameterizedTest(name = "with value {0}")
    @MethodSource("valuesProvider")
    public void deleteDownloadsFolder(final Path downloadsFolder) {
        downloadsFolder.toFile().deleteOnExit();

        when(configuration.getRuntime()).thenReturn(runtime);
        when(runtime.getDownloadsFolder()).thenReturn(downloadsFolder.toString());

        spectrumEntity.deleteDownloadsFolder();

        assertNotNull(downloadsFolder.toFile());
        assertEquals(0, Objects.requireNonNull(downloadsFolder.toFile().list()).length);
    }

    @Test
    @DisplayName("waitForDownloadOf should return true if the provided file is fully downloaded")
    public void waitForDownloadOf() throws IOException {
        final Path path = Files.createTempFile("fakeFile", ".txt");
        Files.writeString(path, "I'm an airplane!!!");

        spectrumEntity.waitForDownloadOf(path);

        verify(downloadWait).until(functionArgumentCaptor.capture());
        final Function<WebDriver, Boolean> function = functionArgumentCaptor.getValue();

        assertTrue(function.apply(webDriver));
    }

    @Test
    @DisplayName("waitForDownloadOf should wait until the provided file is fully downloaded")
    public void waitForDownloadOfNotYetDone() throws IOException {
        final Path path = Files.createTempFile("fakeFile", ".txt");

        spectrumEntity.waitForDownloadOf(path);

        verify(downloadWait).until(functionArgumentCaptor.capture());
        final Function<WebDriver, Boolean> function = functionArgumentCaptor.getValue();

        assertFalse(function.apply(webDriver));
    }

    @Test
    @DisplayName("waitForDownloadOf should wait until the provided file exist")
    public void waitForDownloadOfNotYetCreated() {
        spectrumEntity.waitForDownloadOf(Paths.get("not existing"));

        verify(downloadWait).until(functionArgumentCaptor.capture());
        final Function<WebDriver, Boolean> function = functionArgumentCaptor.getValue();

        assertFalse(function.apply(webDriver));
    }

    @Test
    @DisplayName("checkDownloadedFile should ")
    public void checkDownloadedFile() throws IOException {
        final Path downloadsFolder = Files.createTempDirectory("downloadsFolder");
        final Path filesFolder = Files.createTempDirectory("filesFolder");
        final Path downloadedFile = Files.createFile(Paths.get(downloadsFolder + "/fakeFile.txt"));
        final Path fileToCheck = Files.createFile(Paths.get(filesFolder + "/fakeFile.txt"));
        final Path wrongDownloadedFile = Files.createFile(Paths.get(downloadsFolder + "/wrongFakeFile.txt"));
        final Path wrongFileToCheck = Files.createFile(Paths.get(filesFolder + "/wrongFakeFile.txt"));
        Files.writeString(downloadedFile, "I'm an airplane!!!");
        Files.writeString(fileToCheck, "I'm an airplane!!!");
        Files.writeString(wrongDownloadedFile, "I'm a teapot...");
        Files.writeString(wrongFileToCheck, "I should have been an airplane...");

        downloadedFile.toFile().deleteOnExit();
        fileToCheck.toFile().deleteOnExit();
        wrongDownloadedFile.toFile().deleteOnExit();
        wrongFileToCheck.toFile().deleteOnExit();
        downloadsFolder.toFile().deleteOnExit();
        filesFolder.toFile().deleteOnExit();

        when(configuration.getRuntime()).thenReturn(runtime);
        when(runtime.getDownloadsFolder()).thenReturn(downloadsFolder.toString());
        when(runtime.getFilesFolder()).thenReturn(filesFolder.toString());

        assertTrue(spectrumEntity.checkDownloadedFile(downloadedFile.getFileName().toString()));
        assertFalse(spectrumEntity.checkDownloadedFile(wrongDownloadedFile.getFileName().toString()));
    }

    @Test
    @DisplayName("sha256Of should return the byte array of the sha digest of the provided file")
    public void sha256Of() throws IOException {
        final Path path = Files.createTempFile("fakeFile", ".txt");
        Files.writeString(path, "I'm an airplane!!!");
        path.toFile().deleteOnExit();
        assertArrayEquals(new byte[]{-84, -101, -4, -117, -46, -98, 10, -68, -51, 127, 64, -87, 51, 9, -1, 13, -39, 103,
                -126, 71, -121, -84, -51, 110, 113, -124, 119, -71, -51, 73, -75, 100}, SpectrumEntity.sha256Of(path));
    }

    public static Stream<Arguments> valuesProvider() throws IOException {
        return Stream.of(
                arguments(Paths.get("abc not existing")),
                arguments(Files.createTempDirectory("downloadsFolder")));
    }

    private static class DummySpectrumEntity<T> extends SpectrumEntity<T> {
    }
}