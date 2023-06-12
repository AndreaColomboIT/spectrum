package com.github.giulong.spectrum;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.model.Media;
import com.github.giulong.spectrum.interfaces.Shared;
import com.github.giulong.spectrum.internals.EventsListener;
import com.github.giulong.spectrum.pojos.Configuration;
import com.github.giulong.spectrum.utils.events.EventsDispatcher;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.aventstack.extentreports.MediaEntityBuilder.createScreenCaptureFromPath;
import static com.aventstack.extentreports.Status.*;
import static org.openqa.selenium.OutputType.BYTES;

@Slf4j
public abstract class SpectrumEntity<Data> {

    public static final String SCREEN_SHOT_FOLDER = "screenshots";
    public static final String HASH_ALGORITHM = "SHA-256";

    @Shared
    protected Configuration configuration;

    @Shared
    protected ExtentReports extentReports;

    @Shared
    protected ExtentTest extentTest;

    @Shared
    protected Actions actions;

    @Shared
    protected EventsListener eventsListener;

    @Shared
    protected WebDriver webDriver;

    @Shared
    protected WebDriverWait implicitWait;

    @Shared
    protected WebDriverWait pageLoadWait;

    @Shared
    protected WebDriverWait scriptWait;

    @Shared
    protected WebDriverWait downloadWait;

    @Shared
    protected EventsDispatcher eventsDispatcher;

    @Shared
    protected Data data;

    public List<Field> getSharedFields() {
        return Arrays
                .stream(SpectrumEntity.class.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Shared.class))
                .toList();
    }

    public void hover(final WebElement webElement) {
        actions.moveToElement(webElement).perform();
    }

    public Media infoWithScreenshot(final String msg) {
        return addScreenshotToReport(msg, INFO);
    }

    public Media warningWithScreenshot(final String msg) {
        return addScreenshotToReport(msg, WARNING);
    }

    public Media failWithScreenshot(final String msg) {
        return addScreenshotToReport(msg, FAIL);
    }

    @SneakyThrows
    public Media addScreenshotToReport(final String msg, final Status status) {
        final String fileName = String.format("%s.png", UUID.randomUUID());
        final Path screenshotPath = Path.of(configuration.getExtent().getReportFolder(), SCREEN_SHOT_FOLDER, fileName).toAbsolutePath();
        final TakesScreenshot takesScreenshot = configuration.getRuntime().getBrowser().takesPartialScreenshots()
                ? webDriver.findElement(By.tagName("body"))
                : ((TakesScreenshot) webDriver);

        Files.createDirectories(screenshotPath.getParent());
        Files.write(screenshotPath, takesScreenshot.getScreenshotAs(BYTES));

        final Media screenshot = createScreenCaptureFromPath(Path.of(SCREEN_SHOT_FOLDER, fileName).toString()).build();
        extentTest.log(status, "<div class=\"screenshot-container\">" + msg + "</div>", screenshot);

        return screenshot;
    }

    @SneakyThrows
    public void deleteDownloadsFolder() {
        final String downloadFolder = configuration.getRuntime().getDownloadsFolder();
        final Path downloadPath = Path.of(downloadFolder);

        if (Files.exists(downloadPath)) {
            log.info("About to delete downloads folder '{}'", downloadFolder);

            try (Stream<Path> files = Files.walk(downloadPath)) {
                //noinspection ResultOfMethodCallIgnored
                files.map(Path::toFile).forEach(File::delete);
            }
        }

        Files.createDirectories(downloadPath);
    }

    public void waitForDownloadOf(final Path path) {
        downloadWait.until(driver -> Files.exists(path) && path.toFile().length() > 0);
    }

    public boolean checkDownloadedFile(final String file) {
        final Configuration.Runtime runtime = configuration.getRuntime();
        final Path downloadedFile = Path.of(runtime.getDownloadsFolder(), file);
        final Path fileToCheck = Path.of(runtime.getFilesFolder(), file);

        waitForDownloadOf(downloadedFile);

        return Arrays.equals(sha256Of(downloadedFile), sha256Of(fileToCheck));
    }

    @SneakyThrows
    protected static byte[] sha256Of(final Path file) {
        final byte[] digest = MessageDigest.getInstance(HASH_ALGORITHM).digest(Files.readAllBytes(file));

        log.trace("{} of file '{}' is '{}'", HASH_ALGORITHM, file, Arrays.toString(digest));
        return digest;
    }
}