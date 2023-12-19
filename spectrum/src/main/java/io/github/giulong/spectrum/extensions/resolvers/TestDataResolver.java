package io.github.giulong.spectrum.extensions.resolvers;

import io.github.giulong.spectrum.pojos.Configuration;
import io.github.giulong.spectrum.types.TestData;
import io.github.giulong.spectrum.utils.FileUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver;

import java.nio.file.Files;
import java.nio.file.Path;

import static io.github.giulong.spectrum.extensions.resolvers.ConfigurationResolver.CONFIGURATION;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

@Slf4j
public class TestDataResolver extends TypeBasedParameterResolver<TestData> {

    public static final String TEST_DATA = "testData";

    private final FileUtils fileUtils = FileUtils.getInstance();

    @Override
    public TestData resolveParameter(final ParameterContext arg0, final ExtensionContext context) throws ParameterResolutionException {
        log.debug("Resolving {}", TEST_DATA);
        final Configuration configuration = context.getRoot().getStore(GLOBAL).get(CONFIGURATION, Configuration.class);
        final Configuration.Extent extent = configuration.getExtent();
        final String reportFolder = extent.getReportFolder();
        final String className = context.getRequiredTestClass().getSimpleName();
        final String methodName = context.getRequiredTestMethod().getName();
        final String fileName = fileUtils.removeExtensionFrom(extent.getFileName());
        final Path screenshotFolderPath = getScreenshotFolderPathForCurrentTest(reportFolder, fileName, className, methodName);
        final Path videoPath = getVideoPathForCurrentTest(configuration.getVideo().isDisabled(), reportFolder, fileName, className, methodName);
        final TestData testData = TestData
                .builder()
                .className(className)
                .methodName(methodName)
                .screenshotFolderPath(screenshotFolderPath)
                .videoPath(videoPath)
                .build();

        context.getStore(GLOBAL).put(TEST_DATA, testData);
        return testData;
    }

    @SneakyThrows
    public Path getScreenshotFolderPathForCurrentTest(final String reportsFolder, final String extentFileName, final String className, final String methodName) {
        final Path path = Path.of(reportsFolder, extentFileName, "screenshots", className, methodName).toAbsolutePath();
        Files.createDirectories(path);
        return path;
    }

    @SneakyThrows
    public Path getVideoPathForCurrentTest(final boolean disabled, final String reportsFolder, final String extentFileName, final String className, final String methodName) {
        if (disabled) {
            log.trace("Video disabled: avoiding video folder creation");
            return null;
        }

        final Path path = Path.of(reportsFolder, extentFileName, "videos", className, methodName).toAbsolutePath();
        Files.createDirectories(path);
        return path.resolve(String.format("%s.mp4", randomUUID()));
    }
}
