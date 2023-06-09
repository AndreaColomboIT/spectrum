package com.github.giulong.spectrum;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.github.giulong.spectrum.pojos.Configuration;
import com.github.giulong.spectrum.pojos.testbook.TestBookTest;
import com.github.giulong.spectrum.utils.FileUtils;
import com.github.giulong.spectrum.utils.FreeMarkerWrapper;
import com.github.giulong.spectrum.utils.YamlUtils;
import com.github.giulong.spectrum.utils.testbook.TestBook;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

import java.nio.file.Path;
import java.util.*;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Slf4j
public class SpectrumSessionListener implements LauncherSessionListener {

    public static final String DEFAULT_CONFIGURATION_YAML = "yaml/configuration.default.yaml";
    public static final String CONFIGURATION_YAML = "configuration.yaml";
    public static final String ENV_NODE = "/runtime/env";
    public static final String VARS_NODE = "/vars";
    public static final Map<String, String> VARS = new HashMap<>();

    private final YamlUtils yamlUtils = YamlUtils.getInstance();
    private final FileUtils fileUtils = FileUtils.getInstance();
    private final FreeMarkerWrapper freeMarkerWrapper = FreeMarkerWrapper.getInstance();

    @Getter
    protected static Configuration configuration;

    @Getter
    protected static ExtentReports extentReports;

    @Override
    public void launcherSessionOpened(final LauncherSession session) {
        final Properties spectrumProperties = fileUtils.readProperties("/spectrum.properties");
        log.info(String.format(Objects.requireNonNull(fileUtils.read("/banner.txt")), spectrumProperties.getProperty("version")));

        parseConfiguration();
        parseTestBook();
        initExtentReports();

        freeMarkerWrapper.setupFrom(configuration.getFreeMarker());
    }

    @Override
    public void launcherSessionClosed(final LauncherSession session) {
        configuration.getApplication().getTestBook().flush();
        extentReports.flush();
    }

    protected void parseConfiguration() {
        final String envConfiguration = String.format("configuration-%s.yaml", parseEnv());
        parseVars(envConfiguration);

        configuration = yamlUtils.readInternal(DEFAULT_CONFIGURATION_YAML, Configuration.class);
        yamlUtils.updateWithFile(configuration, CONFIGURATION_YAML);
        yamlUtils.updateWithFile(configuration, envConfiguration);

        log.trace("Configuration:\n{}", yamlUtils.write(configuration));
    }

    protected String parseEnv() {
        return Optional
                .ofNullable(yamlUtils.readInternalNode(ENV_NODE, CONFIGURATION_YAML, String.class))
                .orElse(yamlUtils.readInternalNode(ENV_NODE, DEFAULT_CONFIGURATION_YAML, String.class));
    }

    @SuppressWarnings("unchecked")
    protected void parseVars(final String envConfiguration) {
        VARS.putAll(yamlUtils.readInternalNode(VARS_NODE, DEFAULT_CONFIGURATION_YAML, Map.class));
        VARS.putAll(Optional.ofNullable(yamlUtils.readNode(VARS_NODE, CONFIGURATION_YAML, Map.class)).orElse(new HashMap<>()));
        VARS.putAll(Optional.ofNullable(yamlUtils.readNode(VARS_NODE, envConfiguration, Map.class)).orElse(new HashMap<>()));
    }

    protected void parseTestBook() {
        final TestBook testBook = configuration.getApplication().getTestBook();
        final List<TestBookTest> tests = testBook.getParser().parse();
        final Map<String, Set<TestBookTest>> groupedMappedTests = testBook.getGroupedMappedTests();

        testBook
                .getMappedTests()
                .putAll(tests
                        .stream()
                        .collect(toMap(t -> String.format("%s %s", t.getClassName(), t.getTestName()), identity())));

        tests.forEach(t -> updateGroupedTests(groupedMappedTests, t.getClassName(), t));
    }

    public void updateGroupedTests(final Map<String, Set<TestBookTest>> groupedTests, final String className, final TestBookTest test) {
        final Set<TestBookTest> tests = groupedTests.getOrDefault(className, new HashSet<>());
        tests.add(test);
        groupedTests.put(className, tests);
    }

    protected void initExtentReports() {
        final Configuration.Extent extent = configuration.getExtent();
        final String reportPath = getReportsPathFrom(extent.getReportFolder(), extent.getFileName());
        final String reportName = extent.getReportName();
        final ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);

        sparkReporter.config().setDocumentTitle(extent.getDocumentTitle());
        sparkReporter.config().setReportName(reportName);
        sparkReporter.config().setTheme(Theme.valueOf(extent.getTheme()));
        sparkReporter.config().setTimeStampFormat(extent.getTimeStampFormat());
        sparkReporter.config().setCss(fileUtils.read("/css/report.css"));

        extentReports = new ExtentReports();
        extentReports.attachReporter(sparkReporter);

        log.info("After the execution, you'll find the '{}' report at file:///{}", reportName, reportPath);
    }

    protected String getReportsPathFrom(final String reportFolder, final String fileName) {
        final String resolvedFileName = fileUtils.interpolateTimestampFrom(fileName);
        return Path.of(System.getProperty("user.dir"), reportFolder, resolvedFileName).toString().replace("\\", "/");
    }
}
