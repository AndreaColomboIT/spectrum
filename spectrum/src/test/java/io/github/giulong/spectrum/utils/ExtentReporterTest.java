package io.github.giulong.spectrum.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.ExtentSparkReporterConfig;
import io.github.giulong.spectrum.SpectrumTest;
import io.github.giulong.spectrum.pojos.Configuration;
import io.github.giulong.spectrum.types.TestData;
import io.github.giulong.spectrum.utils.video.Video;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.aventstack.extentreports.Status.*;
import static com.aventstack.extentreports.markuputils.ExtentColor.*;
import static com.aventstack.extentreports.markuputils.MarkupHelper.createLabel;
import static com.aventstack.extentreports.reporter.configuration.Theme.DARK;
import static io.github.giulong.spectrum.extensions.resolvers.ExtentTestResolver.EXTENT_TEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExtentReporter")
class ExtentReporterTest {

    private static MockedStatic<TestData> testDataMockedStatic;
    private static MockedStatic<FreeMarkerWrapper> freeMarkerWrapperMockedStatic;
    private static MockedStatic<Path> pathMockedStatic;
    private static MockedStatic<FileUtils> fileUtilsMockedStatic;

    @Mock
    private ExtentSparkReporterConfig extentSparkReporterConfig;

    @Mock
    private FileUtils fileUtils;

    @Mock
    private TestData.TestDataBuilder testDataBuilder;

    @Mock
    private TestData testData;

    @Mock
    private Path path;

    @Mock
    private Path absolutePath;

    @Mock
    private Path directory1Path;

    @Mock
    private Path directory2Path;

    @Mock
    private File folder;

    @Mock
    private File file1;

    @Mock
    private File file2;

    @Mock
    private File directory1;

    @Mock
    private File directory2;

    @Mock
    private Configuration.Extent extent;

    @Mock
    private ExtentReports extentReports;

    @Mock
    private ExtentTest extentTest;

    @Mock
    private Video.ExtentTest videoExtentTest;

    @Mock
    private Retention retention;

    @Mock
    private RuntimeException exception;

    @Mock
    private ExtensionContext context;

    @Mock
    private ExtensionContext parentContext;

    @Mock
    private ExtensionContext.Store store;

    @Mock
    private SpectrumTest<?> spectrumTest;

    @Captor
    private ArgumentCaptor<Function<String, ExtentTest>> functionArgumentCaptor;

    @Captor
    private ArgumentCaptor<Markup> markupArgumentCaptor;

    @Captor
    private ArgumentCaptor<Markup> skipMarkupArgumentCaptor;

    @InjectMocks
    private ExtentReporter extentReporter;

    @BeforeEach
    public void beforeEach() {
        ReflectionUtils.setField("fileUtils", extentReporter, fileUtils);
        testDataMockedStatic = mockStatic(TestData.class);
        freeMarkerWrapperMockedStatic = mockStatic(FreeMarkerWrapper.class);
        pathMockedStatic = mockStatic(Path.class);
        fileUtilsMockedStatic = mockStatic(FileUtils.class);
    }

    @AfterEach
    public void afterEach() {
        testDataMockedStatic.close();
        freeMarkerWrapperMockedStatic.close();
        pathMockedStatic.close();
        fileUtilsMockedStatic.close();
    }

    @Test
    @DisplayName("getInstance should return the singleton")
    public void getInstance() {
        //noinspection EqualsWithItself
        assertSame(ExtentReporter.getInstance(), ExtentReporter.getInstance());
    }

    @Test
    @DisplayName("setupFrom should init the extent reports")
    public void setupFrom() {
        final String reportFolder = "reportFolder";
        final String fileName = "fileName";
        final String reportName = "reportName";
        final String documentTitle = "documentTitle";
        final String theme = "DARK";
        final String timeStampFormat = "timeStampFormat";
        final String css = "css";
        final String absolutePathToString = "absolute\\Path\\To\\String";
        final String absolutePathToStringReplaced = "absolute/Path/To/String";

        when(extent.getReportFolder()).thenReturn(reportFolder);
        when(extent.getFileName()).thenReturn(fileName);
        when(Path.of(reportFolder, fileName)).thenReturn(path);
        when(path.toAbsolutePath()).thenReturn(absolutePath);
        when(absolutePath.toString()).thenReturn(absolutePathToString);
        when(extent.getReportName()).thenReturn(reportName);
        when(extent.getDocumentTitle()).thenReturn(documentTitle);
        when(extent.getTheme()).thenReturn(theme);
        when(extent.getTimeStampFormat()).thenReturn(timeStampFormat);
        when(FileUtils.getInstance()).thenReturn(fileUtils);
        when(fileUtils.read("/css/report.css")).thenReturn(css);

        MockedConstruction<ExtentReports> extentReportsMockedConstruction = mockConstruction(ExtentReports.class);

        MockedConstruction<ExtentSparkReporter> extentSparkReporterMockedConstruction = mockConstruction(ExtentSparkReporter.class, (mock, context) -> {
            assertEquals(absolutePathToStringReplaced, context.arguments().get(0));
            when(mock.config()).thenReturn(extentSparkReporterConfig);
        });

        assertEquals(extentReporter, extentReporter.setupFrom(extent));

        verify(extentSparkReporterConfig).setDocumentTitle(documentTitle);
        verify(extentSparkReporterConfig).setReportName(reportName);
        verify(extentSparkReporterConfig).setTheme(DARK);
        verify(extentSparkReporterConfig).setTimeStampFormat(timeStampFormat);
        verify(extentSparkReporterConfig).setCss(css);

        final ExtentReports extentReports = extentReportsMockedConstruction.constructed().get(0);
        verify(extentReports).attachReporter(extentSparkReporterMockedConstruction.constructed().toArray(new ExtentSparkReporter[0]));

        extentSparkReporterMockedConstruction.close();
        extentReportsMockedConstruction.close();
    }

    @Test
    @DisplayName("cleanupOldReports should delete the proper number of old reports and the corresponding directories")
    public void cleanupOldReports() {
        final int total = 123;
        final String reportFolder = "reportFolder";
        final String file1Name = "file1Name";
        final String file2Name = "file2Name";
        final String directory1Name = "directory1Name";
        final String directory2Name = "directory2Name";

        when(extent.getRetention()).thenReturn(retention);
        when(retention.getTotal()).thenReturn(total);
        when(extent.getReportFolder()).thenReturn(reportFolder);

        when(Path.of(reportFolder)).thenReturn(path);
        when(path.toFile()).thenReturn(folder);
        when(folder.listFiles()).thenReturn(new File[]{file1, file2, directory1, directory2});

        when(file1.isDirectory()).thenReturn(false);
        when(file2.isDirectory()).thenReturn(false);
        when(directory1.isDirectory()).thenReturn(true);
        when(directory2.isDirectory()).thenReturn(true);
        when(file1.getName()).thenReturn(file1Name);
        when(file2.getName()).thenReturn(file2Name);
        when(directory1.getName()).thenReturn(directory1Name);
        when(directory2.getName()).thenReturn(directory2Name);
        when(directory1.toPath()).thenReturn(directory1Path);
        when(directory2.toPath()).thenReturn(directory2Path);

        when(retention.deleteOldArtifactsFrom(anyList())).thenReturn(2);

        when(FileUtils.getInstance()).thenReturn(fileUtils);
        when(fileUtils.removeExtensionFrom(file1Name)).thenReturn(directory1Name);
        when(fileUtils.removeExtensionFrom(file2Name)).thenReturn(directory2Name);

        extentReporter.cleanupOldReports(extent);

        verify(fileUtils).deleteDirectory(directory1Path);
        verify(fileUtils).deleteDirectory(directory2Path);
    }

    @Test
    @DisplayName("createExtentTestFrom should create the test from the provided testData and return it")
    public void createExtentTestFrom() {
        final String testId = "testId";
        final String classDisplayName = "classDisplayName";
        final String methodDisplayName = "methodDisplayName";

        when(testData.getTestId()).thenReturn(testId);
        when(testData.getClassDisplayName()).thenReturn(classDisplayName);
        when(testData.getMethodDisplayName()).thenReturn(methodDisplayName);
        when(extentReports.createTest(String.format("<div id=\"%s\">%s</div>%s", testId, classDisplayName, methodDisplayName))).thenReturn(extentTest);

        assertEquals(extentTest, extentReporter.createExtentTestFrom(testData));
    }

    @Test
    @DisplayName("attachVideo should add the video in the provided extent test")
    public void attachVideo() {
        final String testId = "testId";
        final int width = 123;
        final int height = 456;

        when(testData.getTestId()).thenReturn(testId);
        when(testData.getVideoPath()).thenReturn(path);
        when(videoExtentTest.getWidth()).thenReturn(width);
        when(videoExtentTest.getHeight()).thenReturn(height);

        extentReporter.attachVideo(extentTest, videoExtentTest, testData);

        verify(extentTest).info(String.format("<video id=\"video-%s\" controls width=\"%d\" height=\"%d\" src=\"%s\" type=\"video/mp4\"/>", testId, width, height, path));
    }

    @Test
    @DisplayName("logTestStartOf should log the start label in the provided test")
    public void logTestStartOf() {
        extentReporter.logTestStartOf(extentTest);

        ArgumentCaptor<Markup> markupArgumentCaptor = ArgumentCaptor.forClass(Markup.class);
        verify(extentTest).info(markupArgumentCaptor.capture());
        Markup markup = markupArgumentCaptor.getValue();
        assertEquals(createLabel("START TEST", GREEN).getMarkup(), markup.getMarkup());
    }

    @DisplayName("getColorOf should return the color corresponding to the provided status")
    @ParameterizedTest()
    @MethodSource("valuesProvider")
    public void getColorOf(final Status status, final ExtentColor expected) {
        assertEquals(expected, extentReporter.getColorOf(status));
    }

    public static Stream<Arguments> valuesProvider() {
        return Stream.of(
                arguments(FAIL, RED),
                arguments(SKIP, AMBER),
                arguments(INFO, GREEN)
        );
    }

    @DisplayName("logTestEnd should create the test in the report and delegate to finalizeTest")
    @ParameterizedTest(name = "with method {0} we expect {1}")
    @CsvSource({
            "noReasonMethod,no reason",
            "reasonMethod,specific reason"
    })
    public void logTestEndDisabled(final String methodName, String expected) throws NoSuchMethodException {
        final String classDisplayName = "classDisplayName";

        logTestEndStubs();
        when(context.getRequiredTestMethod()).thenReturn(getClass().getDeclaredMethod(methodName));
        when(context.getParent()).thenReturn(Optional.of(parentContext));
        when(parentContext.getDisplayName()).thenReturn(classDisplayName);

        extentReporter.logTestEnd(context, SKIP);
        final ExtentTest extentTest = verifyAndGetExtentTest();

        verify(extentTest).skip(skipMarkupArgumentCaptor.capture());
        assertEquals("<span class='badge white-text amber'>Skipped: " + expected + "</span>", skipMarkupArgumentCaptor.getValue().getMarkup());
    }

    @Test
    @DisplayName("logTestEnd should add a screenshot to the report and delegate to finalizeTest")
    public void logTestEndFailed() {
        final String classDisplayName = "classDisplayName";

        logTestEndStubs();
        when(context.getRequiredTestInstance()).thenReturn(spectrumTest);
        when(context.getParent()).thenReturn(Optional.of(parentContext));
        when(parentContext.getDisplayName()).thenReturn(classDisplayName);
        when(context.getExecutionException()).thenReturn(Optional.of(exception));

        extentReporter.logTestEnd(context, FAIL);
        final ExtentTest extentTest = verifyAndGetExtentTest();

        verify(extentTest).fail(exception);
        verify(spectrumTest).screenshotFail("<span class='badge white-text red'>TEST FAILED</span>");
    }

    @Test
    @DisplayName("logTestEnd should add a log in the extent report by default")
    public void logTestEndDefault() {
        final String classDisplayName = "classDisplayName";

        logTestEndStubs();
        when(context.getParent()).thenReturn(Optional.of(parentContext));
        when(parentContext.getDisplayName()).thenReturn(classDisplayName);

        extentReporter.logTestEnd(context, PASS);

        final ExtentTest extentTest = verifyAndGetExtentTest();
        verify(extentTest).log(eq(PASS), markupArgumentCaptor.capture());
        assertEquals("<span class='badge white-text green'>END TEST</span>", markupArgumentCaptor.getValue().getMarkup());
    }

    @Test
    @DisplayName("flush should just flush the extent report")
    public void flush() {
        extentReporter.flush();

        verify(extentReports).flush();
    }

    @Disabled
    @SuppressWarnings("unused")
    private void noReasonMethod() {
    }

    @Disabled("specific reason")
    @SuppressWarnings("unused")
    private void reasonMethod() {
    }

    private void logTestEndStubs() {
        final String classDisplayName = "classDisplayName";
        final String methodDisplayName = "methodDisplayName";
        final String testId = "string-methoddisplayname";

        when(TestData.builder()).thenReturn(testDataBuilder);
        doReturn(String.class).when(context).getRequiredTestClass();
        when(context.getDisplayName()).thenReturn(methodDisplayName);
        when(testDataBuilder.classDisplayName(classDisplayName)).thenReturn(testDataBuilder);
        when(testDataBuilder.methodDisplayName(methodDisplayName)).thenReturn(testDataBuilder);
        when(testDataBuilder.testId(testId)).thenReturn(testDataBuilder);
        when(testDataBuilder.build()).thenReturn(testData);
        when(testData.getTestId()).thenReturn(testId);
        when(testData.getClassDisplayName()).thenReturn(classDisplayName);
        when(testData.getMethodDisplayName()).thenReturn(methodDisplayName);
        when(extentReports.createTest(String.format("<div id=\"%s\">%s</div>%s", testId, classDisplayName, methodDisplayName))).thenReturn(extentTest);

        when(context.getStore(GLOBAL)).thenReturn(store);
        when(store.getOrComputeIfAbsent(eq(EXTENT_TEST), functionArgumentCaptor.capture(), eq(ExtentTest.class))).thenReturn(extentTest);
    }

    private ExtentTest verifyAndGetExtentTest() {
        Function<String, ExtentTest> function = functionArgumentCaptor.getValue();
        return function.apply("value");
    }
}
