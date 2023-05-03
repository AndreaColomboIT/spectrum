package com.giuliolongfils.spectrum.util;

import com.aventstack.extentreports.ExtentTest;
import com.giuliolongfils.spectrum.extensions.SpectrumExtension;
import com.giuliolongfils.spectrum.extensions.resolvers.*;
import com.giuliolongfils.spectrum.interfaces.Endpoint;
import com.giuliolongfils.spectrum.internal.EventsListener;
import com.giuliolongfils.spectrum.pojos.Configuration;
import com.giuliolongfils.spectrum.pojos.WebDriverWaits;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
public abstract class SpectrumTest<Data> extends SpectrumEntity {

    @RegisterExtension
    public static final SpectrumExtension SPECTRUM_EXTENSION = new SpectrumExtension();

    @RegisterExtension
    public static final ConfigurationResolver CONFIGURATION_RESOLVER = new ConfigurationResolver();

    @RegisterExtension
    public static final SpectrumUtilResolver SPECTRUM_UTIL_RESOLVER = new SpectrumUtilResolver(
            CONFIGURATION_RESOLVER.getConfiguration()
    );

    @RegisterExtension
    public static final ExtentReportsResolver EXTENT_REPORTS_RESOLVER = new ExtentReportsResolver(
            CONFIGURATION_RESOLVER.getConfiguration().getExtent()
    );

    @RegisterExtension
    public static final ExtentTestResolver EXTENT_TEST_RESOLVER = new ExtentTestResolver(
            EXTENT_REPORTS_RESOLVER.getExtentReports(),
            SPECTRUM_UTIL_RESOLVER.getSpectrumUtil()
    );

    @RegisterExtension
    public static final WebDriverResolver WEB_DRIVER_RESOLVER = new WebDriverResolver(
            CONFIGURATION_RESOLVER.getConfiguration()
    );

    @RegisterExtension
    public static final WebDriverWaitsResolver WEB_DRIVER_WAITS_RESOLVER = new WebDriverWaitsResolver(
            CONFIGURATION_RESOLVER.getConfiguration().getWebDriver()
    );

    @RegisterExtension
    public final DataResolver<Data> dataResolver = new DataResolver<>(
            CONFIGURATION_RESOLVER.getConfiguration().getData()
    );

    @RegisterExtension
    public static final ActionsResolver ACTIONS_RESOLVER = new ActionsResolver();

    protected static Configuration configuration;
    protected static EventsListener eventsListener;

    @Getter
    protected WebDriver webDriver;
    protected WebDriverWaits webDriverWaits;
    protected Data data;
    protected List<SpectrumPage<Data>> spectrumPages;

    public void initPages() {
        final Class<?> clazz = this.getClass();
        log.debug("Initializing pages of test '{}'", clazz.getSimpleName());

        final List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));

        Class<?> superClazz = clazz.getSuperclass();
        while (superClazz.getSuperclass() != SpectrumEntity.class) {
            log.debug("Initializing also pages in superclass {}", superClazz.getSimpleName());
            fields.addAll(Arrays.asList(superClazz.getDeclaredFields()));
            superClazz = superClazz.getSuperclass();
        }

        spectrumPages = fields
                .stream()
                .filter(f -> SpectrumPage.class.isAssignableFrom(f.getType()))
                .map(this::initPage)
                .collect(toList());
    }

    @SneakyThrows
    public SpectrumPage<Data> initPage(final Field f) {
        log.debug("Initializing page {}", f.getName());

        @SuppressWarnings("unchecked")
        final SpectrumPage<Data> spectrumPage = (SpectrumPage<Data>) f.getType().getDeclaredConstructor().newInstance();

        f.setAccessible(true);
        f.set(this, spectrumPage);

        final String className = spectrumPage.getClass().getSimpleName();
        log.debug("BeforeAll hook: injecting already resolved fields into an instance of {}", className);

        spectrumPage.configuration = configuration;
        spectrumPage.data = data;

        final Endpoint endpointAnnotation = spectrumPage.getClass().getAnnotation(Endpoint.class);
        final String endpointValue = endpointAnnotation != null ? endpointAnnotation.value() : "";
        log.debug("The endpoint of the page {} is '{}'", className, endpointValue);
        spectrumPage.endpoint = endpointValue;

        return spectrumPage;
    }

    @BeforeAll
    public static void spectrumTestParallelBeforeAll(final SpectrumUtil spectrumUtil, final Configuration configuration) {
        SpectrumEntity.spectrumUtil = spectrumUtil;
        SpectrumTest.configuration = configuration;
    }

    @BeforeEach
    public void spectrumTestParallelBeforeEach(final WebDriver webDriver, final WebDriverWaits webDriverWaits, final ExtentTest extentTest,
                                               final Actions actions, final Data data) {
        this.webDriver = webDriver;
        this.webDriverWaits = webDriverWaits;
        this.extentTest = extentTest;
        this.actions = actions;
        this.data = data;

        initPages();

        // TODO ci serve questo ciclo facendo già l'initPages?
        spectrumPages.forEach(spectrumPage -> {
            spectrumPage.webDriver = this.webDriver;
            spectrumPage.webDriverWaits = this.webDriverWaits;
            spectrumPage.extentTest = this.extentTest;
            spectrumPage.eventsListener = eventsListener;
            spectrumPage.actions = this.actions;

            PageFactory.initElements(spectrumPage.webDriver, spectrumPage);
        });
    }

    @AfterEach
    public void spectrumTestParallelAfterEach() {
        webDriver.quit();
    }
}
