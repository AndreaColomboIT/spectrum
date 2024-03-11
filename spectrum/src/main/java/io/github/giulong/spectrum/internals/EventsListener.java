package io.github.giulong.spectrum.internals;

import com.aventstack.extentreports.ExtentTest;
import io.github.giulong.spectrum.enums.Frame;
import io.github.giulong.spectrum.utils.Configuration;
import io.github.giulong.spectrum.utils.Configuration.Drivers.Events;
import io.github.giulong.spectrum.types.TestData;
import io.github.giulong.spectrum.utils.video.Video;
import lombok.Builder;
import lombok.Generated;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.events.WebDriverListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.aventstack.extentreports.markuputils.ExtentColor.YELLOW;
import static com.aventstack.extentreports.markuputils.MarkupHelper.createLabel;
import static io.github.giulong.spectrum.enums.Frame.AUTO_AFTER;
import static io.github.giulong.spectrum.enums.Frame.AUTO_BEFORE;
import static java.util.UUID.randomUUID;
import static org.openqa.selenium.OutputType.BYTES;

@Slf4j
@Builder
public class EventsListener implements WebDriverListener {

    private static final String TAG = "<.*?>";

    private Pattern locatorPattern;
    private ExtentTest extentTest;
    private Video video;
    private TestData testData;
    private WebDriver driver;
    private Events events;

    protected String extractSelectorFrom(final WebElement webElement) {
        final String fullWebElement = webElement.toString();
        final Matcher matcher = locatorPattern.matcher(fullWebElement);

        final List<String> locators = new ArrayList<>();
        while (matcher.find()) {
            locators.add(matcher.group(1));
        }

        return String.join(" -> ", locators);
    }

    protected List<String> parse(final Object[] args, final String format) {
        return Arrays
                .stream(args)
                .map(arg -> String.format(format, arg instanceof WebElement
                        ? extractSelectorFrom((WebElement) arg)
                        : arg))
                .toList();
    }

    protected List<String> parse(final Object[] args) {
        return parse(args, "%s");
    }

    @SneakyThrows
    public Path record(final Frame frame) {
        final Path screenshotPath = testData.getScreenshotFolderPath().resolve(String.format("%s-%s.png", frame.getValue(), randomUUID()));

        if (video.shouldRecord(screenshotPath.getFileName().toString())) {
            log.trace("Recording frame {}", frame);
            return Files.write(screenshotPath, ((TakesScreenshot) driver).getScreenshotAs(BYTES));
        }

        log.trace("Not recording frame {}", frame);
        return null;
    }

    @SneakyThrows
    protected void apply(final boolean condition, final Consumer<String> logConsumer, final Consumer<String> extentConsumer,
                         final Frame frame, final Configuration.Drivers.Event event, final Object... args) {
        final long wait = event.getWait();
        if (wait > 0) {
            log.debug("Waiting {} ms before event processing", wait);
            Thread.sleep(wait);
        }

        if (condition) {
            final String logMessage = String.format(event.getMessage(), parse(args).toArray());
            final String extentMessage = String.format(event.getMessage(), parse(args, "<code>%s</code>").toArray());

            logConsumer.accept(logMessage.replaceAll(TAG, ""));
            record(frame);
            extentConsumer.accept(extentMessage);
        }
    }

    protected void listen(final Frame frame, final Configuration.Drivers.Event event, final Object... args) {
        switch (event.getLevel().levelStr) {
            case "OFF" -> {
            }
            case "TRACE" -> apply(log.isTraceEnabled(), log::trace, extentTest::info, frame, event, args);
            case "DEBUG" -> apply(log.isDebugEnabled(), log::debug, extentTest::info, frame, event, args);
            case "INFO" -> apply(log.isInfoEnabled(), log::info, extentTest::info, frame, event, args);
            case "WARN" -> apply(log.isWarnEnabled(), log::warn, message -> extentTest.warning(createLabel(message, YELLOW)), frame, event, args);
            default -> log.warn("Message '{}' won't be logged. Wrong log level set in configuration.yaml. Choose one among OFF, TRACE, DEBUG, INFO, WARN",
                    String.format(event.getMessage(), parse(args).toArray()));
        }
    }

    @Override
    @Generated
    public void beforeAnyCall(final Object target, final Method method, final Object[] args) {
        listen(AUTO_BEFORE, events.getBeforeAnyCall(), target, method, Arrays.toString(args));
    }

    @Override
    @Generated
    public void afterAnyCall(final Object target, final Method method, final Object[] args, final Object result) {
        listen(AUTO_AFTER, events.getAfterAnyCall(), target, method, Arrays.toString(args), result);
    }

    @Override
    @Generated
    public void onError(final Object target, final Method method, final Object[] args, final InvocationTargetException e) {
        listen(AUTO_AFTER, events.getOnError(), target, method, Arrays.toString(args), e.getMessage());
    }

    @Override
    @Generated
    public void beforeAnyWebDriverCall(final WebDriver webDriver, final Method method, final Object[] args) {
        listen(AUTO_BEFORE, events.getBeforeAnyWebDriverCall(), webDriver, method, Arrays.toString(args));
    }

    @Override
    @Generated
    public void afterAnyWebDriverCall(final WebDriver webDriver, final Method method, final Object[] args, final Object result) {
        listen(AUTO_AFTER, events.getAfterAnyWebDriverCall(), webDriver, method, Arrays.toString(args), result);
    }

    @Override
    @Generated
    public void beforeGet(final WebDriver webDriver, final String url) {
        listen(AUTO_BEFORE, events.getBeforeGet(), webDriver, url);
    }

    @Override
    @Generated
    public void afterGet(final WebDriver webDriver, final String url) {
        listen(AUTO_AFTER, events.getAfterGet(), webDriver, url);
    }

    @Override
    @Generated
    public void beforeGetCurrentUrl(final WebDriver webDriver) {
        listen(AUTO_BEFORE, events.getBeforeGetCurrentUrl(), webDriver);
    }

    @Override
    @Generated
    public void afterGetCurrentUrl(final WebDriver webDriver, final String result) {
        listen(AUTO_AFTER, events.getAfterGetCurrentUrl(), result, webDriver);
    }

    @Override
    @Generated
    public void beforeGetTitle(final WebDriver webDriver) {
        listen(AUTO_BEFORE, events.getBeforeGetTitle(), webDriver);
    }

    @Override
    @Generated
    public void afterGetTitle(final WebDriver webDriver, final String result) {
        listen(AUTO_AFTER, events.getAfterGetTitle(), webDriver, result);
    }

    @Override
    @Generated
    public void beforeFindElement(final WebDriver webDriver, final By locator) {
        listen(AUTO_BEFORE, events.getBeforeFindElement(), webDriver, locator);
    }

    @Override
    @Generated
    public void afterFindElement(final WebDriver webDriver, final By locator, final WebElement result) {
        listen(AUTO_AFTER, events.getAfterFindElement(), webDriver, locator, result);
    }

    @Override
    @Generated
    public void beforeFindElements(final WebDriver webDriver, final By locator) {
        listen(AUTO_BEFORE, events.getBeforeFindElements(), webDriver, locator);
    }

    @Override
    @Generated
    public void afterFindElements(final WebDriver webDriver, final By locator, final List<WebElement> result) {
        listen(AUTO_AFTER, events.getAfterFindElements(), webDriver, locator, result);
    }

    @Override
    @Generated
    public void beforeGetPageSource(final WebDriver webDriver) {
        listen(AUTO_BEFORE, events.getBeforeGetPageSource(), webDriver);
    }

    @Override
    @Generated
    public void afterGetPageSource(final WebDriver webDriver, final String result) {
        listen(AUTO_AFTER, events.getAfterGetPageSource(), webDriver, result.replace("<", "&lt;").replace(">", "&gt;"));
    }

    @Override
    @Generated
    public void beforeClose(final WebDriver webDriver) {
        listen(AUTO_BEFORE, events.getBeforeClose(), webDriver);
    }

    @Override
    @Generated
    public void afterClose(final WebDriver webDriver) {
        listen(AUTO_AFTER, events.getAfterClose(), webDriver);
    }

    @Override
    @Generated
    public void beforeQuit(final WebDriver webDriver) {
        listen(AUTO_BEFORE, events.getBeforeQuit(), webDriver);
    }

    @Override
    @Generated
    public void afterQuit(final WebDriver webDriver) {
        listen(AUTO_AFTER, events.getAfterQuit(), webDriver);
    }

    @Override
    @Generated
    public void beforeGetWindowHandles(final WebDriver webDriver) {
        listen(AUTO_BEFORE, events.getBeforeGetWindowHandles(), webDriver);
    }

    @Override
    @Generated
    public void afterGetWindowHandles(final WebDriver webDriver, final Set<String> result) {
        listen(AUTO_AFTER, events.getAfterGetWindowHandles(), webDriver, result);
    }

    @Override
    @Generated
    public void beforeGetWindowHandle(final WebDriver webDriver) {
        listen(AUTO_BEFORE, events.getBeforeGetWindowHandle(), webDriver);
    }

    @Override
    @Generated
    public void afterGetWindowHandle(final WebDriver webDriver, final String result) {
        listen(AUTO_AFTER, events.getAfterGetWindowHandle(), webDriver, result);
    }

    @Override
    @Generated
    public void beforeExecuteScript(final WebDriver webDriver, final String script, final Object[] args) {
        listen(AUTO_BEFORE, events.getBeforeExecuteScript(), webDriver, script, Arrays.toString(args));
    }

    @Override
    @Generated
    public void afterExecuteScript(final WebDriver webDriver, final String script, final Object[] args, final Object result) {
        listen(AUTO_AFTER, events.getAfterExecuteScript(), webDriver, script, Arrays.toString(args), result);
    }

    @Override
    @Generated
    public void beforeExecuteAsyncScript(final WebDriver webDriver, final String script, final Object[] args) {
        listen(AUTO_BEFORE, events.getBeforeExecuteAsyncScript(), webDriver, script, Arrays.toString(args));
    }

    @Override
    @Generated
    public void afterExecuteAsyncScript(final WebDriver webDriver, final String script, final Object[] args, final Object result) {
        listen(AUTO_AFTER, events.getAfterExecuteAsyncScript(), webDriver, script, Arrays.toString(args), result);
    }

    @Override
    @Generated
    public void beforePerform(final WebDriver webDriver, final Collection<Sequence> actions) {
        listen(AUTO_BEFORE, events.getBeforePerform(), webDriver, actions);
    }

    @Override
    @Generated
    public void afterPerform(final WebDriver webDriver, final Collection<Sequence> actions) {
        listen(AUTO_AFTER, events.getAfterPerform(), webDriver, actions);
    }

    @Override
    @Generated
    public void beforeResetInputState(final WebDriver webDriver) {
        listen(AUTO_BEFORE, events.getBeforeResetInputState(), webDriver);
    }

    @Override
    @Generated
    public void afterResetInputState(final WebDriver webDriver) {
        listen(AUTO_AFTER, events.getAfterResetInputState(), webDriver);
    }

    @Override
    @Generated
    public void beforeAnyWebElementCall(final WebElement element, final Method method, final Object[] args) {
        listen(AUTO_BEFORE, events.getBeforeAnyWebElementCall(), element, method, element, Arrays.toString(args));
    }

    @Override
    @Generated
    public void afterAnyWebElementCall(final WebElement element, final Method method, final Object[] args, final Object result) {
        listen(AUTO_AFTER, events.getAfterAnyWebElementCall(), element, method, element, Arrays.toString(args), result);
    }

    @Override
    @Generated
    public void beforeClick(final WebElement element) {
        listen(AUTO_BEFORE, events.getBeforeClick(), element);
    }

    @Override
    @Generated
    public void afterClick(final WebElement element) {
        listen(AUTO_AFTER, events.getAfterClick(), element);
    }

    @Override
    @Generated
    public void beforeSubmit(final WebElement element) {
        listen(AUTO_BEFORE, events.getBeforeSubmit(), element);
    }

    @Override
    @Generated
    public void afterSubmit(final WebElement element) {
        listen(AUTO_AFTER, events.getAfterSubmit(), element);
    }

    @Override
    @Generated
    public void beforeSendKeys(final WebElement element, final CharSequence... keysToSend) {
        listen(AUTO_BEFORE, events.getBeforeSendKeys(), element, Arrays.toString(keysToSend));
    }

    @Override
    @Generated
    public void afterSendKeys(final WebElement element, final CharSequence... keysToSend) {
        listen(AUTO_AFTER, events.getAfterSendKeys(), element, Arrays.toString(keysToSend));
    }

    @Override
    @Generated
    public void beforeClear(final WebElement element) {
        listen(AUTO_BEFORE, events.getBeforeClear(), element);
    }

    @Override
    @Generated
    public void afterClear(final WebElement element) {
        listen(AUTO_AFTER, events.getAfterClear(), element);
    }

    @Override
    @Generated
    public void beforeGetTagName(final WebElement element) {
        listen(AUTO_BEFORE, events.getBeforeGetTagName(), element);
    }

    @Override
    @Generated
    public void afterGetTagName(final WebElement element, final String result) {
        listen(AUTO_AFTER, events.getAfterGetTagName(), element, result);
    }

    @Override
    @Generated
    public void beforeGetAttribute(final WebElement element, final String name) {
        listen(AUTO_BEFORE, events.getBeforeGetAttribute(), element, name);
    }

    @Override
    @Generated
    public void afterGetAttribute(final WebElement element, final String name, final String result) {
        listen(AUTO_AFTER, events.getAfterGetAttribute(), element, name, result);
    }

    @Override
    @Generated
    public void beforeIsSelected(final WebElement element) {
        listen(AUTO_BEFORE, events.getBeforeIsSelected(), element);
    }

    @Override
    @Generated
    public void afterIsSelected(final WebElement element, final boolean result) {
        listen(AUTO_AFTER, events.getAfterIsSelected(), element, result);
    }

    @Override
    @Generated
    public void beforeIsEnabled(final WebElement element) {
        listen(AUTO_BEFORE, events.getBeforeIsEnabled(), element);
    }

    @Override
    @Generated
    public void afterIsEnabled(final WebElement element, final boolean result) {
        listen(AUTO_AFTER, events.getAfterIsEnabled(), element, result);
    }

    @Override
    @Generated
    public void beforeGetText(final WebElement element) {
        listen(AUTO_BEFORE, events.getBeforeGetText(), element);
    }

    @Override
    @Generated
    public void afterGetText(final WebElement element, final String result) {
        listen(AUTO_AFTER, events.getAfterGetText(), element, result);
    }

    @Override
    @Generated
    public void beforeFindElement(final WebElement element, final By locator) {
        listen(AUTO_BEFORE, events.getBeforeFindWebElement(), element, locator);
    }

    @Override
    @Generated
    public void afterFindElement(final WebElement element, final By locator, final WebElement result) {
        listen(AUTO_AFTER, events.getAfterFindWebElement(), element, locator, result);
    }

    @Override
    @Generated
    public void beforeFindElements(final WebElement element, final By locator) {
        listen(AUTO_BEFORE, events.getBeforeFindWebElements(), element, locator);
    }

    @Override
    @Generated
    public void afterFindElements(final WebElement element, final By locator, final List<WebElement> result) {
        listen(AUTO_AFTER, events.getAfterFindWebElements(), element, locator, result);
    }

    @Override
    @Generated
    public void beforeIsDisplayed(final WebElement element) {
        listen(AUTO_BEFORE, events.getBeforeIsDisplayed(), element);
    }

    @Override
    @Generated
    public void afterIsDisplayed(final WebElement element, final boolean result) {
        listen(AUTO_AFTER, events.getAfterIsDisplayed(), element, result);
    }

    @Override
    @Generated
    public void beforeGetLocation(final WebElement element) {
        listen(AUTO_BEFORE, events.getBeforeGetLocation(), element);
    }

    @Override
    @Generated
    public void afterGetLocation(final WebElement element, final Point result) {
        listen(AUTO_AFTER, events.getAfterGetLocation(), element, result);
    }

    @Override
    @Generated
    public void beforeGetSize(final WebElement element) {
        listen(AUTO_BEFORE, events.getBeforeGetSize(), element);
    }

    @Override
    @Generated
    public void afterGetSize(final WebElement element, final Dimension result) {
        listen(AUTO_AFTER, events.getAfterGetSize(), element, result);
    }

    @Override
    @Generated
    public void beforeGetCssValue(final WebElement element, final String propertyName) {
        listen(AUTO_BEFORE, events.getBeforeGetCssValue(), element, propertyName);
    }

    @Override
    @Generated
    public void afterGetCssValue(final WebElement element, final String propertyName, final String result) {
        listen(AUTO_AFTER, events.getAfterGetCssValue(), element, propertyName, result);
    }

    @Override
    @Generated
    public void beforeAnyNavigationCall(final WebDriver.Navigation navigation, final Method method, final Object[] args) {
        listen(AUTO_BEFORE, events.getBeforeAnyNavigationCall(), navigation, method, Arrays.toString(args));
    }

    @Override
    @Generated
    public void afterAnyNavigationCall(final WebDriver.Navigation navigation, final Method method, final Object[] args, final Object result) {
        listen(AUTO_AFTER, events.getAfterAnyNavigationCall(), navigation, method, Arrays.toString(args), result);
    }

    @Override
    @Generated
    public void beforeTo(final WebDriver.Navigation navigation, final String url) {
        listen(AUTO_BEFORE, events.getBeforeTo(), navigation, url);
    }

    @Override
    @Generated
    public void afterTo(final WebDriver.Navigation navigation, final String url) {
        listen(AUTO_AFTER, events.getAfterTo(), navigation, url);
    }

    @Override
    @Generated
    public void beforeTo(final WebDriver.Navigation navigation, final URL url) {
        listen(AUTO_BEFORE, events.getBeforeTo(), navigation, url);
    }

    @Override
    @Generated
    public void afterTo(final WebDriver.Navigation navigation, final URL url) {
        listen(AUTO_AFTER, events.getAfterTo(), navigation, url);
    }

    @Override
    @Generated
    public void beforeBack(final WebDriver.Navigation navigation) {
        listen(AUTO_BEFORE, events.getBeforeBack(), navigation);
    }

    @Override
    @Generated
    public void afterBack(final WebDriver.Navigation navigation) {
        listen(AUTO_AFTER, events.getAfterBack(), navigation);
    }

    @Override
    @Generated
    public void beforeForward(final WebDriver.Navigation navigation) {
        listen(AUTO_BEFORE, events.getBeforeForward(), navigation);
    }

    @Override
    @Generated
    public void afterForward(final WebDriver.Navigation navigation) {
        listen(AUTO_AFTER, events.getAfterForward(), navigation);
    }

    @Override
    @Generated
    public void beforeRefresh(final WebDriver.Navigation navigation) {
        listen(AUTO_BEFORE, events.getBeforeRefresh(), navigation);
    }

    @Override
    @Generated
    public void afterRefresh(final WebDriver.Navigation navigation) {
        listen(AUTO_AFTER, events.getAfterRefresh(), navigation);
    }

    @Override
    @Generated
    public void beforeAnyAlertCall(final Alert alert, final Method method, final Object[] args) {
        listen(AUTO_BEFORE, events.getBeforeAnyAlertCall(), alert, method, Arrays.toString(args));
    }

    @Override
    @Generated
    public void afterAnyAlertCall(final Alert alert, final Method method, final Object[] args, final Object result) {
        listen(AUTO_AFTER, events.getAfterAnyAlertCall(), alert, method, Arrays.toString(args), result);
    }

    @Override
    @Generated
    public void beforeAccept(final Alert alert) {
        listen(AUTO_BEFORE, events.getBeforeAccept(), alert);
    }

    @Override
    @Generated
    public void afterAccept(final Alert alert) {
        listen(AUTO_AFTER, events.getAfterAccept(), alert);
    }

    @Override
    @Generated
    public void beforeDismiss(final Alert alert) {
        listen(AUTO_BEFORE, events.getBeforeDismiss(), alert);
    }

    @Override
    @Generated
    public void afterDismiss(final Alert alert) {
        listen(AUTO_AFTER, events.getAfterDismiss(), alert);
    }

    @Override
    @Generated
    public void beforeGetText(final Alert alert) {
        listen(AUTO_BEFORE, events.getBeforeGetText(), alert);
    }

    @Override
    @Generated
    public void afterGetText(final Alert alert, final String result) {
        listen(AUTO_AFTER, events.getAfterGetText(), alert, result);
    }

    @Override
    @Generated
    public void beforeSendKeys(final Alert alert, final String text) {
        listen(AUTO_BEFORE, events.getBeforeSendKeys(), alert, text);
    }

    @Override
    @Generated
    public void afterSendKeys(final Alert alert, final String text) {
        listen(AUTO_AFTER, events.getAfterSendKeys(), alert, text);
    }

    @Override
    @Generated
    public void beforeAnyOptionsCall(final WebDriver.Options options, final Method method, final Object[] args) {
        listen(AUTO_BEFORE, events.getBeforeAnyOptionsCall(), options, method, Arrays.toString(args));
    }

    @Override
    @Generated
    public void afterAnyOptionsCall(final WebDriver.Options options, final Method method, final Object[] args, final Object result) {
        listen(AUTO_AFTER, events.getAfterAnyOptionsCall(), options, method, Arrays.toString(args), result);
    }

    @Override
    @Generated
    public void beforeAddCookie(final WebDriver.Options options, final Cookie cookie) {
        listen(AUTO_BEFORE, events.getBeforeAddCookie(), options, cookie);
    }

    @Override
    @Generated
    public void afterAddCookie(final WebDriver.Options options, final Cookie cookie) {
        listen(AUTO_AFTER, events.getAfterAddCookie(), options, cookie);
    }

    @Override
    @Generated
    public void beforeDeleteCookieNamed(final WebDriver.Options options, final String name) {
        listen(AUTO_BEFORE, events.getBeforeDeleteCookieNamed(), options, name);
    }

    @Override
    @Generated
    public void afterDeleteCookieNamed(final WebDriver.Options options, final String name) {
        listen(AUTO_AFTER, events.getAfterDeleteCookieNamed(), options, name);
    }

    @Override
    @Generated
    public void beforeDeleteCookie(final WebDriver.Options options, final Cookie cookie) {
        listen(AUTO_BEFORE, events.getBeforeDeleteCookie(), options, cookie);
    }

    @Override
    @Generated
    public void afterDeleteCookie(final WebDriver.Options options, final Cookie cookie) {
        listen(AUTO_AFTER, events.getAfterDeleteCookie(), options, cookie);
    }

    @Override
    @Generated
    public void beforeDeleteAllCookies(final WebDriver.Options options) {
        listen(AUTO_BEFORE, events.getBeforeDeleteAllCookies(), options);
    }

    @Override
    @Generated
    public void afterDeleteAllCookies(final WebDriver.Options options) {
        listen(AUTO_AFTER, events.getAfterDeleteAllCookies(), options);
    }

    @Override
    @Generated
    public void beforeGetCookies(final WebDriver.Options options) {
        listen(AUTO_BEFORE, events.getBeforeGetCookies(), options);
    }

    @Override
    @Generated
    public void afterGetCookies(final WebDriver.Options options, final Set<Cookie> result) {
        listen(AUTO_AFTER, events.getAfterGetCookies(), options, result);
    }

    @Override
    @Generated
    public void beforeGetCookieNamed(final WebDriver.Options options, final String name) {
        listen(AUTO_BEFORE, events.getBeforeGetCookieNamed(), options, name);
    }

    @Override
    @Generated
    public void afterGetCookieNamed(final WebDriver.Options options, final String name, final Cookie result) {
        listen(AUTO_AFTER, events.getAfterGetCookieNamed(), options, name, result);
    }

    @Override
    @Generated
    public void beforeAnyTimeoutsCall(final WebDriver.Timeouts timeouts, final Method method, final Object[] args) {
        listen(AUTO_BEFORE, events.getBeforeAnyTimeoutsCall(), timeouts, method, Arrays.toString(args));
    }

    @Override
    @Generated
    public void afterAnyTimeoutsCall(final WebDriver.Timeouts timeouts, final Method method, final Object[] args, final Object result) {
        listen(AUTO_AFTER, events.getAfterAnyTimeoutsCall(), timeouts, method, Arrays.toString(args), result);
    }

    @Override
    @Generated
    public void beforeImplicitlyWait(final WebDriver.Timeouts timeouts, final Duration duration) {
        listen(AUTO_BEFORE, events.getBeforeImplicitlyWait(), timeouts, duration);
    }

    @Override
    @Generated
    public void afterImplicitlyWait(final WebDriver.Timeouts timeouts, final Duration duration) {
        listen(AUTO_AFTER, events.getAfterImplicitlyWait(), timeouts, duration);
    }

    @Override
    @Generated
    public void beforeSetScriptTimeout(final WebDriver.Timeouts timeouts, final Duration duration) {
        listen(AUTO_BEFORE, events.getBeforeSetScriptTimeout(), timeouts, duration);
    }

    @Override
    @Generated
    public void afterSetScriptTimeout(final WebDriver.Timeouts timeouts, final Duration duration) {
        listen(AUTO_AFTER, events.getAfterSetScriptTimeout(), timeouts, duration);
    }

    @Override
    @Generated
    public void beforePageLoadTimeout(final WebDriver.Timeouts timeouts, final Duration duration) {
        listen(AUTO_BEFORE, events.getBeforePageLoadTimeout(), timeouts, duration);
    }

    @Override
    @Generated
    public void afterPageLoadTimeout(final WebDriver.Timeouts timeouts, final Duration duration) {
        listen(AUTO_AFTER, events.getAfterPageLoadTimeout(), timeouts, duration);
    }

    @Override
    @Generated
    public void beforeAnyWindowCall(final WebDriver.Window window, final Method method, final Object[] args) {
        listen(AUTO_BEFORE, events.getBeforeAnyWindowCall(), window, method, Arrays.toString(args));
    }

    @Override
    @Generated
    public void afterAnyWindowCall(final WebDriver.Window window, final Method method, final Object[] args, final Object result) {
        listen(AUTO_AFTER, events.getAfterAnyWindowCall(), window, method, Arrays.toString(args), result);
    }

    @Override
    @Generated
    public void beforeGetSize(final WebDriver.Window window) {
        listen(AUTO_BEFORE, events.getBeforeGetWindowSize(), window);
    }

    @Override
    @Generated
    public void afterGetSize(final WebDriver.Window window, final Dimension result) {
        listen(AUTO_AFTER, events.getAfterGetWindowSize(), window, result);
    }

    @Override
    @Generated
    public void beforeSetSize(final WebDriver.Window window, final Dimension size) {
        listen(AUTO_BEFORE, events.getBeforeSetSize(), window, size);
    }

    @Override
    @Generated
    public void afterSetSize(final WebDriver.Window window, final Dimension size) {
        listen(AUTO_AFTER, events.getAfterSetSize(), window, size);
    }

    @Override
    @Generated
    public void beforeGetPosition(final WebDriver.Window window) {
        listen(AUTO_BEFORE, events.getBeforeGetPosition(), window);
    }

    @Override
    @Generated
    public void afterGetPosition(final WebDriver.Window window, final Point result) {
        listen(AUTO_AFTER, events.getAfterGetPosition(), window, result);
    }

    @Override
    @Generated
    public void beforeSetPosition(final WebDriver.Window window, final Point position) {
        listen(AUTO_BEFORE, events.getBeforeSetPosition(), window, position);
    }

    @Override
    @Generated
    public void afterSetPosition(final WebDriver.Window window, final Point position) {
        listen(AUTO_AFTER, events.getAfterSetPosition(), window, position);
    }

    @Override
    @Generated
    public void beforeMaximize(final WebDriver.Window window) {
        listen(AUTO_BEFORE, events.getBeforeMaximize(), window);
    }

    @Override
    @Generated
    public void afterMaximize(final WebDriver.Window window) {
        listen(AUTO_AFTER, events.getAfterMaximize(), window);
    }

    @Override
    @Generated
    public void beforeFullscreen(final WebDriver.Window window) {
        listen(AUTO_BEFORE, events.getBeforeFullscreen(), window);
    }

    @Override
    @Generated
    public void afterFullscreen(final WebDriver.Window window) {
        listen(AUTO_AFTER, events.getAfterFullscreen(), window);
    }
}
