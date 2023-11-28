Spectrum is a [JUnit 5](https://junit.org/junit5/docs/current/user-guide/){:target="_blank"} and [Selenium 4](https://www.selenium.dev/){:target="_blank"} framework that aims to
simplify the writing of e2e tests.
Main features:

* automatic [execution video](#automatic-execution-video-generation) generation
* automatic [log and html report](#automatically-generated-reports) generation
* automatic coverage report generation by reading a [testbook](#testbook-coverage)
* automatic [mail/slack notifications](#events-consumers) with reports as attachments
* fully configurable providing human-readable and [declarative yaml files](#configuration)
* out-of-the-box defaults to let you run tests with no additional configuration

Spectrum leverages JUnit's extension model to initialise and inject all the needed objects
directly in your test classes, so that you can focus just on writing the logic to test your application.

> ⚠️ <b>Eclipse IDE</b><br/>
> The archetype injects a demo test that leverages Lombok. If you use Eclipse IDE, check [how to enable Lombok](https://projectlombok.org/setup/eclipse){:target="_blank"}.

## Glossary

| Acronym | Meaning                                                                                                                     |
|---------|-----------------------------------------------------------------------------------------------------------------------------|
| AUT     | Application Under Test                                                                                                      |
| POM     | [Page Object Model](https://www.selenium.dev/documentation/test_practices/encouraged/page_object_models/){:target="_blank"} |
| QG      | Quality Gate                                                                                                                |
| POJO    | Plain Old java Object                                                                                                       |

## Setup

> ⚠️ <b>JDK</b><br/>
> Since Spectrum is compiled with a jdk 17, you need a [jdk 17+](https://jdk.java.net/java-se-ri/17){:target="_blank"} to be able to run your tests.
> If you get an `Unsupported major.minor version` exception, the reason is that you're using an incompatible java version.

### Spectrum Archetype

You should leverage the latest published version of the [Spectrum Archetype](https://mvnrepository.com/artifact/io.github.giulong/spectrum-archetype){:target="_blank"} to create a
new project.
You can either use it via your IDE, or run this from command line:

{% include codeHeader.html %}

```shell
mvn archetype:generate -DarchetypeGroupId=io.github.giulong -DarchetypeArtifactId=spectrum-archetype -DarchetypeVersion=LATEST -DinteractiveMode=false -DgroupId=<GROUP ID> -DartifactId=<ARTIFACT ID> -Dversion=<VERSION> -DoutputDirectory=<DESTINATION>
```

Needless to say that `<GROUP ID>`, `<ARTIFACT ID>`, `<VERSION>`, and `<DESTINATION>` are placeholders that you need to replace with actual values.

> ⚠️ <b>Maven archetype:generate</b><br/>
> If you want to tweak the behaviour of the command above, for example to generate the project in interactive mode, check the
> official [archetype:generate docs](https://maven.apache.org/archetype/maven-archetype-plugin/generate-mojo.html){:target="_blank"}.

The project created will contain a demo test you can immediately run.
If you don't want to leverage the archetype, you can manually add the [Spectrum dependency](https://mvnrepository.com/artifact/io.github.giulong/spectrum){:target="_blank"} to your
project:

{% include codeHeader.html %}

```xml

<dependency>
    <groupId>io.github.giulong</groupId>
    <artifactId>spectrum</artifactId>
    <version>LATEST</version>
    <scope>test</scope>
</dependency>
```

> ⚠️ <b>Lombok Library</b><br/>
> The demo test injected by the archetype uses [Lombok](https://projectlombok.org/){:target="_blank"} to generate getters. Lombok is internally used in Spectrum, and provided as a
> transitive
> dependency, so you can already use it.
>
> Be sure to check its docs to understand how to configure it in your IDE.
> If you don't want to leverage it, you can safely write getters the old way.

### Test creation

In general, all you need to do is create a **JUnit 5** test class and make it extend the `SpectrumTest` class:

{% include codeHeader.html %}

```java
import io.github.giulong.spectrum.SpectrumTest;
import org.junit.jupiter.api.Test;

public class HelloWorldIT extends SpectrumTest<Void> {

    @Test
    public void dummyTest() {
        webDriver.get(configuration.getApplication().getBaseUrl());
    }
}
```

> ⚠️ <b>Running with Maven</b><br/>
> If you run tests with Maven, the name of your test classes should end with `IT` as in the example above: `HelloWorldIT`,
> to leverage the [default inclusions](https://maven.apache.org/surefire/maven-failsafe-plugin/examples/inclusion-exclusion.html){:target="_blank"} of the failsafe plugin.

> 💡 <b>Tip</b><br/>
> The default browser is `chrome`. If you want to use another one, you can switch via the `-Dspectrum.browser` system property, setting its value to
> `firefox` or `edge`:
> * `-Dspectrum.browser=firefox`
> * `-Dspectrum.browser=edge`

> 💡 <b>Tip</b><br/>
> The default log level is `INFO`. If you want to change it, run with `-Dspectrum.log.level=<LEVEL>`,
> for example:
> * `-Dspectrum.log.level=DEBUG`
> * `-Dspectrum.log.level=TRACE`

If you now run the test, you will find a html report generated in the `target/spectrum/reports` folder.

> 💡 <b>Tip</b><br/>
> Spectrum is tested with itself, so in this repo you can find real examples of Spectrum e2e tests.
> They're in the [it]({{ site.repository_url }}/it) and [it-testbook]({{ site.repository_url }}/it-testbook){:target="_blank"} modules. Throughout this doc, you will
> be pointed to specific examples.

---

# SpectrumTest and SpectrumPage

These are the two main entities you will need to know to fully leverage Spectrum:

* your test classes must extend [SpectrumTest](#spectrumtest)
* your test pages must extend [SpectrumPage](#spectrumpage)

## SpectrumTest

Your test classes must extend [SpectrumTest]({{ site.repository_url }}/spectrum/src/main/java/io/github/giulong/spectrum/SpectrumTest.java){:target="_blank"}.
As you might have noticed in the examples above, you need to provide a generic parameter when extending it.
That is the `Data` type of your own. Be sure to check the [Data section](#data) below. In case you don't need any,
you just need to set `Void` as generic.

`SpectrumTest` extends [SpectrumEntity](#spectrumentity) and inherits its fields and methods.

Beyond having direct access to `webDriver`, `configuration`, `data`, and all the other inherited fields
that you don't even need to declare or instantiate, by extending `SpectrumTest`, each `SpectrumPage` that you declare
in your test class will automatically be initialised.

{% include codeHeader.html %}

```java
import io.github.giulong.spectrum.SpectrumTest;
import org.junit.jupiter.api.Test;

public class HelloWorldIT extends SpectrumTest<Void> {

    // page class that extends SpectrumPage. 
    // Simply declare it. Spectrum will initialise an instance and inject it
    private MyPage myPage;

    @Test
    public void dummyTest() {

        // getting direct access to both webDriver and configuration without declaring
        // nor instantiating them. Spectrum does that for you.
        // Here we're opening the landing page of the AUT
        webDriver.get(configuration.getApplication().getBaseUrl());

        // assuming in MyPage we have a WebElement named "button", now we're clicking on it
        myPage.getButton().click();
    }
}
```

> 💡 <b>Example</b><br/>
> Check the [tests]({{ site.repository_url }}/it/src/test/java/io/github/giulong/spectrum/it/tests){:target="_blank"} package to see real examples of SpectrumTests.

## SpectrumPage

As per Selenium's best practices, you should leverage the [page object model](https://www.selenium.dev/documentation/test_practices/encouraged/page_object_models/){:target="_
blank"}
to represent the objects of the web pages you need to interact with.
To fully leverage Spectrum, your pages must extend the [SpectrumPage]({{ site.repository_url }}/spectrum/src/main/java/io/github/giulong/spectrum/SpectrumPage.java){:
target="_blank"} class.

`SpectrumPage` extends [SpectrumEntity](#spectrumentity) and inherits its fields and methods.

Each `SpectrumPage` takes two generics:

1. the page itself
2. the `Data` type of your own, the same used as generic in your SpectrumTests.

For example, assuming you need no data, this would be the signature of a page class named `WebAppPage`:

{% include codeHeader.html %}

```java
import io.github.giulong.spectrum.SpectrumPage;

public class WebAppPage extends SpectrumPage<WebAppPage, Void> {
    // ...
}
```

> 💡 <b>Example</b><br/>
> Check the [pages]({{ site.repository_url }}/it/src/test/java/io/github/giulong/spectrum/it/pages){:target="_blank"} package to see real examples of SpectrumPages.

### SpectrumPage Service Methods

By extending `SpectrumPage`, you will inherit few service methods listed here:

* `open()`:

  You can specify an endpoint for your pages by annotating them like this:

{% include codeHeader.html %}

```java
import io.github.giulong.spectrum.SpectrumPage;
import io.github.giulong.spectrum.interfaces.Endpoint;

@Endpoint("login")
public class WebAppPage extends SpectrumPage<WebAppPage, Void> {
    // ...
}
```

Then, in your tests, you can leverage the `open` method. Spectrum will combine the AUT's base url from the `configuration*.yaml` with the endpoint:

{% include codeHeader.html %}

```yaml
# configuration.yaml
application:
  baseUrl: http://my-app.com
```

{% include codeHeader.html %}

```java
public class HelloWorldIT extends SpectrumTest<Void> {

    private WebAppPage webAppPage;

    @Test
    public void myTest() {
        webAppPage.open();  // will open http://my-app.com/login
    }
}
```

Moreover, `open` will internally call the `waitForPageLoading` method.

* `waitForPageLoading()`:

  This is a method that by default just logs a warning. If you need to check for custom conditions before considering a page fully loaded,
  you should override this method, so that calling `open` on pages will call your implementation automatically.

  For example, you could have a spinner shown by default when opening pages, and disappearing once the page is fully loaded.
  You should override the `waitForPageLoading` like this:

{% include codeHeader.html %}

```java
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOf;

public class WebAppPage extends SpectrumPage<WebAppPage, Void> {

    @FindBy(id = "spinner")
    private WebElement spinner;

    @Override
    public WebAppPage waitForPageLoading() {
        pageLoadWait.until(invisibilityOf(spinner));

        return this;
    }
}
```

💡 <b>Tip</b><br/>
Both the `open` and `waitForPageLoading` methods return the instance calling them.
This is meant to provide a [fluent API](https://en.wikipedia.org/wiki/Fluent_interface){:target="_blank"}, so that you can rely on method chaining.
You should write your service methods with this in mind.
Check [FilesIT]({{ site.repository_url }}/it-testbook/src/test/java/io/github/giulong/spectrum/it_testbook/tests/FilesIT.java){:target="_blank"} for an example:

{% include codeHeader.html %}

```java
uploadPage
        .open()
        .upload(uploadPage.getFileUpload(),FILE_TO_UPLOAD)
        .getSubmit()
        .click();
```

* `isLoaded()`:

  This is a method to check if the caller page is loaded.
  It returns a boolean which is true if the current url is equal to the AUT's base url combined with the page's endpoint.

{% include codeHeader.html %}

```java
public class HelloWorldIT extends SpectrumTest<Void> {

    private WebAppPage webAppPage;

    @Test
    public void myTest() {
        // assuming:
        //  - base url in configuration.yaml is http://my-app.com
        //  - webAppPage is annotated with @Endpoint("login")
        //  
        //  will be true if the current url in the browser is http://my-app.com/login
        boolean loaded = webAppPage.isLoaded();
    }
}
```

## SpectrumEntity

[SpectrumEntity]({{ site.repository_url }}/spectrum/src/main/java/io/github/giulong/spectrum/SpectrumEntity.java){:target="_blank"} is the parent class of
both `SpectrumTest` and `SpectrumPage`.
Whenever extending any of those, you will inherit its fields and methods.

Spectrum takes care of resolving and injecting all the fields below,
so you can directly use them in your tests/pages without caring about declaring nor instantiating them.

| Field            | Description                                                                                                                                                                                                                                     |
|------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| configuration    | maps the result of the merge of all the `configuration*.yaml` files. You can use it to access to all of its values                                                                                                                              |
| extentReports    | instance of the Extent Report                                                                                                                                                                                                                   |
| extentTest       | instance linked to the section of the Extent Report that will represent the current test. You can use it to add info/screenshots programmatically.                                                                                              |
| actions          | instance of Selenium [Actions class](https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/interactions/Actions.html){:target="_blank"}, useful to simulate complex user gestures                                                 |
| webDriver        | instance of the WebDriver running for the current test, configured in a declarative way via `configuration*.yaml`                                                                                                                               |
| implicitWait     | instance of [WebDriverWait](https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/support/ui/WebDriverWait.html){:target="_blank"} with the duration taken from the `webDriver.waits.implicit` in the `configuration.yaml`        |
| pageLoadWait     | instance of [WebDriverWait](https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/support/ui/WebDriverWait.html){:target="_blank"} with the duration taken from the `webDriver.waits.pageLoadTimeout` in the `configuration.yaml` |
| scriptWait       | instance of [WebDriverWait](https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/support/ui/WebDriverWait.html){:target="_blank"} with the duration taken from the `webDriver.waits.scriptTimeout` in the `configuration.yaml`   |
| downloadWait     | instance of [WebDriverWait](https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/support/ui/WebDriverWait.html){:target="_blank"} with the duration taken from the `webDriver.waits.downloadTimeout` in the `configuration.yaml` |
| eventsDispatcher | you can use it to fire custom events. Check the [Custom Events section](#custom-events)                                                                                                                                                         |
| data             | maps the result of the merge of all the `data*.yaml` files. You can use it to access to all of its values                                                                                                                                       |

### SpectrumEntity Service Methods

> ⚠️ <b>Methods returning `T`</b><br/>
> in the list below, the `T` return type means that method returns the caller instance, so you can leverage method chaining.

* `T hover(WebElement)`: hovers on the provided WebElement, leveraging the `actions` field
* `T screenshot()`: adds a screenshot at INFO level to the current test in the Extent Report
* `T screenshotInfo(String)`: adds a screenshot with the provided message and INFO status to the current test in the Extent Report
* `T screenshotWarning(String)`: adds a screenshot status with the provided message and WARN to the current test in the Extent Report
* `T screenshotFail(String)`: adds a screenshot with the provided message and FAIL status to the current test in the Extent Report
* `Media addScreenshotToReport(String, Status)`: adds a screenshot with the provided message and the provided status to the current test in the Extent Report
* `void deleteDownloadsFolder()`: deletes the download folder (its path is provided in the `configuration*.yaml`)
* `T waitForDownloadOf(Path)`: leverages the configurable `downloadWait` to check fluently if the file at the provided path is fully downloaded
* `boolean checkDownloadedFile(String, String)`: leverages the `waitForDownloadOf` method and then compares checksum of the two files provided. Check
  the [File Download section](#file-download)
* `boolean checkDownloadedFile(String)`: leverages the `waitForDownloadOf` method and then compares checksum of the file provided. Check the [File Download section](#file-download)
* `WebElement clearAndSendKeys(WebElement, CharSequence)`: helper method to call Selenium's `clear` and `sendKeys` on the provided WebElement, which is then returned
* `T upload(WebElement, String)`: uploads to the provided WebElement (usually an input field with `type="file"`) the file with the provided name, taken from the
  configurable `runtime.filesFolder`. Check the [File Upload section](#file-upload)
* `boolean isPresent(By)`: checks if the WebElement with the provided `by` is present in the current page
* `boolean isNotPresent(By)`: checks if no WebElement with the provided `by` is present in the current page
* `boolean hasClass(WebElement, String)`: checks if the provided WebElement has the provided css class
* `boolean hasClasses(WebElement, String...)`: checks if the provided WebElement has **all** the provided css classes

---

# Configuration

Spectrum is fully configurable and comes with default values which you can find in
the [configuration.default.yaml]({{ site.repository_url }}/spectrum/src/main/resources/yaml/configuration.default.yaml){:target="_blank"}.
Be sure to check it: each key is properly commented to clarify its purpose.

> ⚠️ <b>Running on *nix</b><br/>
> When running on *nix, the [configuration.default.unix.yaml]({{ site.repository_url }}/spectrum/src/main/resources/yaml/configuration.default.unix.yaml){:target="_blank"}
> will be merged onto the base
> one
> to set filesystem-specific values such as path separators.

To customise these values, you can create the `src/test/resources/configuration.yaml` file in your project.

> ⚠️ <b>Files Extension</b><br/>
> The extension must be `.yaml`. The shortened `.yml` won't work.

Furthermore, you can provide how many profile-specific configurations in the same folder, by naming them
`configuration-<PROFILE>.yaml`, where `<PROFILE>` is a placeholder that you need to replace with the actual profile name.

To let Spectrum pick the right profiles-related configuration, you must run with the `-Dspectrum.profiles` flag,
which is a comma separated list of profile names you want to activate.

> 💡 <b>Example</b><br/>
> When running tests with `-Dspectrum.profiles=test,grid`, Spectrum will merge these files in this order of precedence:
> 1. configuration.default.yaml [Spectrum internal defaults]
> 2. configuration.default.unix.yaml [Spectrum internal defaults for *nix, not read on Windows]
> 3. configuration.yaml [Provided by you]
> 4. configuration-test.yaml [Provided by you. A warning will be raised if not found, no errors]
> 5. configuration-grid.yaml [Provided by you. A warning will be raised if not found, no errors]

Values in the most specific configuration file will take precedence over the others.

> 💡 <b>Tip</b><br/>
> There's no need to repeat everything: configuration files are merged, so it's better to keep values that are common to all the profiles in the base configuration.yaml,
> while providing `<PROFILE>`-specific ones in the `configuration-<PROFILE>.yaml`

⚠️ <b>Merging Lists</b><br/>
Watch out that list-type nodes will not be overridden. Their values will be merged by appending elements! For example, if you have these:

{% include codeHeader.html %}

```yaml
# configuration.yaml
someList:
  - value1
```

{% include codeHeader.html %}

```yaml
# configuration-test.yaml
someList:
  - value2
```

If you run with `-Dspectrum.profiles=test` both files will be loaded and lists will be merged, resulting in:

{% include codeHeader.html %}

```yaml
someList:
  - value1
  - value2
```

> 💡 <b>Tip</b><br/>
> If you need different configurations for the same environment, instead of manually changing values in the configuration*.yaml, you should
> provide different files and choose the right one with the `-Dspectrum.profiles` flag. <br/>
> For example, if you need to be able to run from your local machine alternatively targeting a remote grid or executing browsers in local,
> it's preferable to have something like these two files, where you change just the target runtime:
> * configuration-local-local.yaml
> * configuration-local-grid.yaml
>
> In this way, you don't need to change any configuration file. This is important, since configurations are versioned alongside your tests,
> so you will avoid errors and will keep your scm history clean.
> You need just to activate the right one by creating different run configurations in your IDE.

> 💡 <b>Tip</b><br/>
> Working in a team where devs need different local configurations? You can *gitignore* a file like `configuration-personal.yaml`,
> so that everyone can provide their own configuration without interfering with others.

> 💡 <b>Example</b><br/>
> Check the `application.baseUrl` node in these configurations used in Spectrum's own tests to see an example of merging:
> * [configuration.yaml]({{ site.repository_url }}/it-testbook/src/test/resources/configuration.yaml){:target="_blank"}
> * [configuration-first.yaml]({{ site.repository_url }}/it-testbook/src/test/resources/configuration-first.yaml){:target="_
    blank"} [Actually ignored, active profiles are `local` and `second`]
> * [configuration-second.yaml]({{ site.repository_url }}/it-testbook/src/test/resources/configuration-second.yaml){:target="_blank"}
>
> The very first node of the base `configuration.yaml` linked above sets the active profiles, instructing Spectrum to load the other two configurations,
> and overriding the `application.baseUrl` accordingly:

{% include codeHeader.html %}

```yaml
runtime:
  profiles: local,second
```

## Vars node

The `vars` node is a special one in the `configuration.yaml`. You can use it to define common vars once and refer to them in several nodes.
`vars` is a `Map<String, String>`, so you can define all the keys you need, naming them how you want.

{% include codeHeader.html %}

```yaml
vars:
  commonKey: some-value # commonKey is a name of your choice

node:
  property: ${commonKey} # Will be replaced with `some-value`

anotherNode:
  subNode:
    key: ${commonKey} # Will be replaced with `some-value`
```

## Values interpolation

Plain values (not objects nor arrays) in `configuration*.yaml` and `data*.yaml`  can be interpolated with a dollar-string like this:

{% include codeHeader.html %}

```yaml
object:
  key: ${key:-defaultValue}
```

Where the `:-` is the separator between the name of the key to search for and the default value to use in case the key is not found. The default value is optional: you can just
have `${key}`.

Spectrum will replace the dollar-string with the first value found in this list:

1. `key` in [vars node](#vars-node):

   {% include codeHeader.html %}
    ```yaml
    vars:
    key: value 
    ```
2. system property named `key`: `-Dkey=value`
3. environment variable named `key`
4. `defaultValue` (if provided)

If the provided key can't be found in any of these places, a warning will be raised.
Both key name and default value might contain dots like in `${some.key:-default.value}`

> 💡 <b>Tip</b><br/>
> This trick is used in the internal `configuration.default.yaml` to allow for variables to be read from outside.
> For example, profiles are set like this:

{% include codeHeader.html %}

```yaml
# internal configuration.default.yaml
runtime:
  profiles: ${spectrum.profiles:-local}
```

> This allows you to just run with `-Dspectrum.profiles=...` while having a default, but you can still explicitly set them in your `configuration.yaml`:

{% include codeHeader.html %}

```yaml
# your configuration.yaml
runtime:
  profiles: my-profile,another-one
```

> You can also choose to provide your own variable:

{% include codeHeader.html %}

```yaml
# your configuration.yaml
runtime:
  profiles: ${active-profiles:-local}
```

> This could be useful to create and leverage your own naming convention for env variables.

These are the variables already available in the [configuration.default.yaml]({{ site.repository_url }}/spectrum/src/main/resources/yaml/configuration.default.yaml){:
target="_blank"}.
You can add your own and even override the default ones in your `configuration*.yaml`:

| Variable          | Default Windows              | Default *nix                 |
|-------------------|------------------------------|------------------------------|
| spectrum.profiles | local                        | local                        |
| spectrum.browser  | chrome                       | chrome                       |
| downloadsFolder   | ${user.dir}\target\downloads | ${user.dir}/target/downloads |

## Running on a Grid

By default, browsers run in local. This is because the default value in the internal `configuration.default.yaml` is:

{% include codeHeader.html %}

```yaml
runtime:
  environment:
    local: { }
```

To run on a remote [grid](https://www.selenium.dev/documentation/grid/){:target="_blank"}, you just need to change that node, providing at least the grid url:

{% include codeHeader.html %}

```yaml
runtime:
  environment:
    grid:
      url: https://my-grid-url:4444/wd/hub
      capabilities:
        someCapability: its value
        another: blah
      localFileDetector: true
```

Where the params are:

| Param             | Type               | Default   | Mandatory | Description                                                                                                                                                                                              |
|-------------------|--------------------|-----------|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| url               | String             | null      | ✅         | url of the remote grid                                                                                                                                                                                   |
| capabilities      | Map<String,String> | empty map | ❌         | additional webDriver capabilities to be added to browser-specific ones only when running on a grid                                                                                                       |
| localFileDetector | boolean            | false     | ❌         | if true, allows to transfer files from the client machine to the remote server. [Docs](https://www.selenium.dev/documentation/webdriver/drivers/remote_webdriver/#local-file-detector){:target="_blank"} |

---

# JSON Schema

Json Schema really comes in handy when editing `configuration*.yaml`, since it allows you to have autocompletion.
You can find the JSON Schema for the `configuration*.yaml` in the [spectrum-json-schemas repository](https://github.com/giulong/spectrum-json-schemas){:target="_blank"}.

If your IDE supports json schemas, be sure to pick the right one according to the version of Spectrum you are using.

The path to the raw file is:

`https://raw.githubusercontent.com/giulong/spectrum-json-schemas/main/<SPECTRUM VERSION>/Configuration.json`

where `<SPECTRUM VERSION>` must be replaced with the one you're using.

> 💡 <b>Tip</b><br/>
> Example for version 1.0.0: `https://raw.githubusercontent.com/giulong/spectrum-json-schemas/main/1.0.0/Configuration.json`

---

# Automatic Execution Video Generation

It's possible to have Spectrum generate a video of the execution of each single test, leveraging [JCodec](http://www.jcodec.org/){:target="_blank"}. By default, this is disabled,
so you need to
explicitly activate this feature
in your `configuration.yaml`. Check the `video` node in the [configuration.default.yaml]({{ site.repository_url
}}/spectrum/src/main/resources/yaml/configuration.default.yaml){:target="_blank"} for all
the available
parameters along with their details.

To be precise, the video is generated from screenshots taken during the execution.
You can specify which screenshots to be used as frames providing one or more of these values in the `video.frames` field:

| Frame      | Description                                                                                                                        |
|------------|------------------------------------------------------------------------------------------------------------------------------------|
| autoBefore | Screenshots taken **before** an event happening in the WebDriver                                                                   |
| autoAfter  | Screenshots taken **after** an event happening in the WebDriver                                                                    |
| manual     | Screenshots programmatically taken by you by invoking one of the [SpectrumEntity Service Methods](#spectrumentity-service-methods) |

> ⚠️ <b>Auto screenshots</b><br/>
> Screenshots are taken automatically (with `autoBefore` and `autoAfter`) according to the current log level
> and the `webDriver.events` settings. For example, if running with the default `INFO` log level and the configuration below,
> no screenshot will be taken before clicking any element. It will when raising the log level at `DEBUG` or higher.

{% include codeHeader.html %}

```yaml
webDriver:
  events:
    beforeClick:
      level: DEBUG  # Screenshots for this event are taken only when running at `DEBUG` level or higher
      message: Clicking on %1$s
```

> 💡 <b>Tip</b><br/>
> Setting both `autoBefore` and `autoAfter` is likely to be useless. Let's consider this flow:
> 1. screenshot: before click
> 2. click event
> 3. **---> screenshot: after click**
> 4. **---> screenshot: before set text**
> 5. set text in input field
> 6. screenshot: after set text
>
> The screenshots at bullets 3 and 4 will be equal, and, for performance reason, the second will be discarded.

The video will be saved in the `<extent.reportFolder>/videos/<CLASS NAME>/<TEST NAME>` folder (`extent.reportFolder` is `target` by default)
and attached to the Extent Report as well.

> 💡 <b>Video Configuration Example</b><br/>
> Here's a quick example snippet (remember you just need to provide fields with a value different from the corresponding one in the
> internal [configuration.default.yaml]({{ site.repository_url }}/spectrum/src/main/resources/yaml/configuration.default.yaml){:target="_blank"}):

{% include codeHeader.html %}

```yaml
video:
  frames:
    - autoAfter
    - manual
  extentTest:
    width: 640  # we want a bigger video tag in the report
    height: 480
```

---

# Automatically Generated Reports

After each execution, Spectrum produces two files:

* [log](#log-file)
* [html report](#html-report)

The WebDriver fires events that are automatically logged and added to the html report.
Check the `webDriver.events` node in the [configuration.default.yaml]({{ site.repository_url }}/spectrum/src/main/resources/yaml/configuration.default.yaml){:target="_
blank"} to see the defaults log
levels and messages.

Remember that the log level is set with `-Dspectrum.log.level` and defaults to `INFO`.
Each event with a configured log level equal or higher than the one specified with `-Dspectrum.log.level` will be logged and added to the html report.

Needless to say, you can also log and add info and screenshots to html report programmatically.

## Log file

The log file will contain the same information you see in the console. It will be produced by default under the `target/spectrum/logs` folder.

It's generated using [Logback](https://logback.qos.ch/){:target="_blank"}, and [here]({{ site.repository_url }}/spectrum/src/main/resources/logback.xml){:target="_blank"}
you can find its configuration.
Logs are rotated daily, meaning the results of each execution occurred in the same day will be appended to the same file.

> 💡 <b>Tip</b><br/>
> By default, logs are generated using a colored pattern. In case the console you use doesn't support it (if you see weird characters at the beginning of each line),
> you should deactivate colors by running with `-Dspectrum.log.colors=false`.

> 💡 <b>Tip</b><br/>
> You can provide your own log configuration by adding the `src/test/resources/logback-test.xml`.
> This file will completely override the one provided by Spectrum

## Html report

Spectrum generates a html report using [Extent Reports](https://www.extentreports.com/){:target="_blank"}.
By default, it will be produced under the `target/spectrum/reports` folder.
Check the `extent` node in the [configuration.default.yaml]({{ site.repository_url }}/spectrum/src/main/resources/yaml/configuration.default.yaml){:target="_blank"} to see
how to customise it.

> 💡 <b>Tip</b><br/>
> The default file name of the produced html report contains a timestamp, which is useful to always generate a new file.
> While developing, it could be worth it to override the `extent.fileName` to have a fixed name.
> This way the report will be overridden, so you can keep it open in a browser and just refresh the page after each execution.

You can see an example report here:

![Extent Report](images/extent-screenshot.png)

> 💡 <b>Tip</b><br/>
> You can provide your own *look and feel* by putting additional css rules in the `src/test/resources/css/report.css` file.
> Spectrum will automatically load and apply it to the Extent Report.

Upon a test failure, Spectrum adds a screenshot to the report automatically.

You can also add logs to the report programmatically. Check the [SpectrumEntity Service Methods](#spectrumentity-service-methods) section for details.
For example, to add a screenshot with a message at `INFO` level to the `dummyTest`:

{% include codeHeader.html %}

```java
public class HelloWorldIT extends SpectrumTest<Void> {

    @Test
    public void dummyTest() {
        extentTest.screenshotInfo("Custom message");
    }
}
```

### Custom locators

Selenium doesn't provide any way to get a webElement's locator, by design. So, Spectrum extracts the locator from the `webElement.toString()`.
You can leverage the `extent.locatorRegex` property to extract the important bits
out of it, **using the capturing group** (the one wrapped by parentheses).
For example, for a field annotated like this:

{% include codeHeader.html %}

```java
@FindBys({
        @FindBy(id = "checkboxes"),
        @FindBy(tagName = "input")
})
private List<WebElement> checkboxes;
```

this would be the full `toString()`:

![extent locator full](images/extent-locator-full.jpg)

The regex in the [configuration.default.yaml]({{ site.repository_url }}/spectrum/src/main/resources/yaml/configuration.default.yaml){:target="_blank"} is:

{% include codeHeader.html %}

```yaml
locatorRegex: \s->\s([\w:\s\-.#]+)
```

which extracts just this (mind the capturing group above):

![extent locator](images/extent-locator.jpg)

For example, if you want to shrink it even more, you could add this as `extent.locatorRegex` in your `configuration.yaml`:

{% include codeHeader.html %}

```yaml
locatorRegex: \s->[\w\s]+:\s([()^\w\s\-.#]+)
```

and you'd see this:

![extent locator custom](images/extent-locator-custom.jpg)

---

# Common Use Cases

Here you can find how Spectrum helps you in a few common use cases.

## File Upload

You can add files to be uploaded in the folder specified in the `runtime.filesFolder` node of the `configuration*.yaml`.
This is the default you can see in the internal [configuration.default.yaml]({{ site.repository_url }}/spectrum/src/main/resources/yaml/configuration.default.yaml){:
target="_blank"}:

{% include codeHeader.html %}

```yaml
runtime:
  filesFolder: src/test/resources/files
```

If you have these files in the configured folder:

{% include codeHeader.html %}

```text
root
└─ src
  └─ test
     └─ resources
        └─ files
           ├─ myFile.txt
           └─ document.pdf
```

and in the web page there's an input field with `type="file"`, you can leverage the `upload` method directly in any of your tests/pages like this:

{% include codeHeader.html %}

```java
public class HelloWorldIT extends SpectrumTest<Void> {

    private WebAppPage webAppPage;

    @Test
    public void myTest() {
        WebElement fileUploadInput = webAppPage.getFileUploadInput();

        // leveraging method chaining
        webAppPage.open().upload(fileUploadInput, "myFile.txt");

        // directly invoking the upload
        upload(fileUploadInput, "document.pdf");
    }
}
```

> 💡 <b>Example</b><br/>
> Check the [FilesIT.upload() test]({{ site.repository_url }}/it/src/test/java/io/github/giulong/spectrum/it/tests/FilesIT.java){:target="_blank"} to see a real example

## File Download

Files are downloaded in the folder specified as `vars.downloadsFolder` in the `configuration*.yaml`.
If needed, you should change this value, since this is used in several places, for example in all the browsers' capabilities.
So, this is a useful way to avoid redundancy and to be able to change all the values with one key.

When downloading a file from the AUT, you can leverage Spectrum to check if it's what you expected.
Technically speaking, checking the file's content is beyond the goal of a Selenium test, which aims to check web applications, so its boundary is the browser.

Given a file downloaded from the AUT (so, in the `vars.downloadsFolder`),
Spectrum helps checking it by comparing its SHA 256 checksum with the checksum of a file in the folder specified in the `runtime.filesFolder` node of the `configuration*.yaml`.

Let's explain this with an example. Let's say that:

* you want to check a file downloaded with the name `downloadedFile.txt`
* you rely on the default `filesFolder`, which is `src/test/resources/files`:

You need to place an exact copy of that file in that folder:

{% include codeHeader.html %}

```text
root
└─ src
  └─ test
     └─ resources
        └─ files
           └─ downloadedFile.txt
```

Now you can leverage the `checkDownloadedFile(String)` method like this:

{% include codeHeader.html %}

```java
public class HelloWorldIT extends SpectrumTest<Void> {

    private WebAppPage webAppPage;

    @Test
    public void myTest() {
        assertTrue(checkDownloadedFile("downloadedFile.txt"));
    }
}
```

If the files are the same, their checksum will match, and that assertion will pass.
In case you need to check a file with a different name, for example if the AUT generates file names dynamically,
you can leverage the overloaded `checkDownloadedFile(String, String)` method, which takes the names of both the downloaded file and the one to check:

{% include codeHeader.html %}

```java
public class HelloWorldIT extends SpectrumTest<Void> {

    private WebAppPage webAppPage;

    @Test
    public void myTest() {
        // Parameters order matters:
        //  - the first file will be searched in the downloadsFolder
        //  - the second file will be searched in the filesFolder
        assertTrue(checkDownloadedFile("downloadedFile.txt", "fileToCheck.txt"));
    }
}
```

> 💡 <b>Example</b><br/>
> Check the [FilesIT.download() test]({{ site.repository_url }}/it/src/test/java/io/github/giulong/spectrum/it/tests/FilesIT.java){:target="_blank"} to see a real example

---

# Data

As a general best practice, test code should only contain flow logic and assertions, while data should be kept outside.
Spectrum embraces this by leveraging dedicated yaml files. This is completely optional, you can run all your tests without any data file.

By default, you can create `data*.yaml` files under the `src/test/resources/data` folder.
Data files will be loaded and merged following the same conventions of `configurations*.yaml` files.

> 💡 <b>Example</b><br/>
> When running tests with `-Dspectrum.profiles=test`, Spectrum will merge these files in this order of precedence:
> 1. data.yaml
> 2. data-test.yaml

For data files to be properly unmarshalled, you must create the corresponding POJOs and set the fqdn of your parent data class in the configuration.yaml.

Let's see an example. Let's say we want to test the AUT with two users having two different roles (admin and guest).
Both will have the same set of params, such as a name and a password to login.

We need to take four steps:

* Create the yaml describing this scenario:
   {% include codeHeader.html %}
    ```yaml
    # data.yaml
    users:
      admin:
        name: ada
        password: secret
      guest:
        name: bob
        password: pwd
    ```

* Create the POJO mapping the yaml above:

{% include codeHeader.html %}

```java
package your.package_name;  // this must be set in the configuration.yaml. Keep reading below :)

import lombok.Getter;

import java.util.Map;

@Getter
public class Data {

    private Map<String, User> users;

    @Getter
    public static class User {
        private String name;
        private String password;
    }
}
```

> 💡 <b>Tip</b><br/>
> The `User` class in the snippet above is declared as a static inner class. This is not mandatory, you could have plain public classes in their own java file.

* Make Spectrum aware of your Data class by providing its fqdn in the configuration.yaml:
   
{% include codeHeader.html %}
```yaml
# configuration.yaml    
data:
  fqdn: your.package_name.Data  # Format: <package name>.<class name>
```

* Declare the Data class as generic in the SpectrumTest(s) and/or SpectrumPage(s) that will use it:
   {% include codeHeader.html %}
    ```java
    import io.github.giulong.spectrum.SpectrumTest;
    import org.junit.jupiter.api.Test;
    import your.package_name.Data;
   
    public class SomeIT extends SpectrumTest<Data> { // <-- Mind the generic here
        
        @Test
        public void someTestMethod() {
            // We can now use the data object leveraging its getters.
            // No need to declare/instantiate the 'data' field: Spectrum is taking care of injecting it.
            // You can directly use it as it is here.
            data.getUsers().get("admin").getName();
        }
    }
    ```

   {% include codeHeader.html %}
    ```java
    import io.github.giulong.spectrum.SpectrumPage;
    import your.package_name.Data;
   
    public class SomePage extends SpectrumPage<SomePage, Data> { // <-- Mind the generic here
        
        public void someServiceMethod() {
            data.getUsers().get("admin").getName();
        }
    }
    ```

The `Data` generic must be specified only in those classes actually using it. There's no need to set it everywhere.

> 💡 <b>Tip</b><br/>
> For the sake of completeness, you can name the `Data` POJO as you prefer.
> You can name it `MySuperShinyWhatever.java` and have this as generic in you SpectrumTest(s):
> `public class SomeIT extends SpectrumTest<MySuperShinyWhatever> {`
>
> That said, I don't really see any valid use case for this. Let me know if you see one.
> Probably, could be useful to have different Data classes to be used in different tests, so to have different and clearer names.
> In this scenario, they could be loaded from different `configuration*.yaml`.

> 💡 <b>Example: parameterized tests</b><br/>
> Check the [data.yaml]({{ site.repository_url }}/it/src/test/resources/data/data.yaml){:target="_blank"} and how it's used in
> the [LoginFormIT]({{ site.repository_url }}/it/src/test/java/io/github/giulong/spectrum/it/tests/LoginFormIT.java){:target="_blank"}.
> Look for the usage of `data.getUsers()` in that class.

---

# Parallel Execution

Spectrum tests can be run in parallel by leveraging [JUnit Parallel Execution](https://junit.org/junit5/docs/snapshot/user-guide/#writing-tests-parallel-execution){:target="_
blank"}

---

# Event Sourcing

Spectrum leverages event sourcing, meaning throughout the execution it fires events at specific moments.
These events are sent in broadcast to a list of consumers.
Each consumer defines the events it's interested into. Whenever an event is fired, any consumer interested into that is notified,
performing the action it's supposed to (more in the [Events Consumers section](#events-consumers) below).

Each [event]({{ site.repository_url }}/spectrum/src/main/java/io/github/giulong/spectrum/pojos/events/Event.java){:target="_blank"} defines a set of keys that consumers can
use to define the events they
want to be
notified about. Most of them can be used in consumers with the type of match specified below:

| Field Name                                | Type                                                                                                                                                        | Match |
|-------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|-------|
| [primaryId](#primaryid-and-secondaryid)   | String                                                                                                                                                      | regex |
| [secondaryId](#primaryid-and-secondaryid) | String                                                                                                                                                      | regex |
| [tags](#tags)                             | Set\<String>                                                                                                                                                | exact |
| [reason](#reason)                         | String                                                                                                                                                      | regex |
| [result](#result)                         | [Result]({{ site.repository_url }}/spectrum/src/main/java/io/github/giulong/spectrum/enums/Result.java){:target="_blank"}                            | exact |
| [context](#context)                       | [ExtensionContext](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/ExtensionContext.html){:target="_blank"} | -     |

Let's see them in detail:

### PrimaryId and SecondaryId

`primaryId` and `secondaryId` are strings through which you can identify each event. For example, for each test method, their value is:

* `primaryId` &rarr; \<CLASS NAME>
* `secondaryId` &rarr; \<TEST NAME>

Let's see what they mean with an example. Given the following test:

{% include codeHeader.html %}

```java
public class HelloWorldIT extends SpectrumTest<Void> {

    @Test
    public void dummyTest() {
        ...
    }
}
```

Spectrum will fire an event with:

* `primaryId` &rarr; "HelloWorldIT"
* `secondaryId` &rarr; "dummyTest()" (Yes, the method name ends with the parenthesis)

If the `@DisplayName` is provided for either the class and/or the method, those will be used. Given:

{% include codeHeader.html %}

```java

@DisplayName("Class display name")
public class HelloWorldIT extends SpectrumTest<Void> {

    @Test
    @DisplayName("Method display name")
    public void dummyTest() {
        ...
    }
}
```

Spectrum will fire an event with:

* `primaryId` &rarr; "Class display name"
* `secondaryId` &rarr; "Method display name"

### Tags

Tags are a set of strings used to group events together. For example, all test methods will have the "test" tag.
This way, instead of attaching a consumer to a specific event (with primary and secondary id, for example) you can listen
to all events tagged in a particular way, such as all the tests.

> 💡 <b>Example</b><br/>
> Check the `eventsConsumers` in the [configuration.default.yaml]({{ site.repository_url }}/spectrum/src/main/resources/yaml/configuration.default.yaml){:target="_blank"}.
> Internal consumers need to take actions after each test is done, meaning they listen to events tagged with "test":

{% include codeHeader.html %}

```yaml
eventsConsumers:
  - extentTest: # We need to add an entry to the Extent Report once each test is done
    events:
      - reason: after
        tags: [ test ]
```

### Reason

Reason specifies why an event has been fired.

### Result

This is the result of the executed test. Of course, this will be available only in events fired after tests execution, as per the table below.

### Context

The JUnit's ExtensionContext is attached to each event. It's not considered when matching events, but it can be useful
in custom templates to access objects stored in it. For example, the default [slack.json template]({{ site.repository_url
}}/spectrum/src/main/resources/templates/slack.json){:target="_blank"}
uses it to print class and test names:

{% include codeHeader.html %}

```json
"text": "*Name*\n*Class*: `${event.context.parent.get().displayName}`\n*Test*: `${event.context.displayName}`"
```

---

## Events fired

Spectrum fires these events. The first column in the table below highlights the moment when that specific event is fired.
The other columns are the event's keys, with blank values being nulls.

| When                      | primaryId     | secondaryId  | tags     | reason | result                                                 |
|---------------------------|---------------|--------------|----------|--------|--------------------------------------------------------|
| Suite started             |               |              | \[suite] | before |                                                        |
| Suite ended               |               |              | \[suite] | after  |                                                        |
| Class started (BeforeAll) | \<CLASS NAME> |              | \[class] | before |                                                        |
| Class ended (AfterAll)    | \<CLASS NAME> |              | \[class] | after  |                                                        |
| Test started (BeforeEach) | \<CLASS NAME> | \<TEST NAME> | \[test]  | before |                                                        |
| Test ended (AfterEach)    | \<CLASS NAME> | \<TEST NAME> | \[test]  | after  | NOT_RUN \| SUCCESSFUL \| FAILED \| ABORTED \| DISABLED |

> 💡 <b>Tip</b><br/>
> If you're not sure about a particular event, when it's fired and what are the actual values of its keys,
> you can always run with `-Dspectrum.log.level=TRACE` and look into logs. You'll find something like "Dispatching event ...":

{% include codeHeader.html %}

```text
18:00:05.076 D EventsDispatcher          | Dispatching event Event(primaryId=null, secondaryId=null, tags=[suite], reason=before, result=null)
18:00:05.081 T EventsConsumer            | ExtentTestConsumer matchers for Event(primaryId=null, secondaryId=null, tags=[suite], reason=before, result=null)
18:00:05.081 T EventsConsumer            | reasonMatches: false
18:00:05.081 T EventsConsumer            | resultMatches: false
18:00:05.081 T EventsConsumer            | TestBookConsumer matchers for Event(primaryId=null, secondaryId=null, tags=[suite], reason=before, result=null)
18:00:05.081 T EventsConsumer            | reasonMatches: false
18:00:05.081 T EventsConsumer            | resultMatches: false
```

## Custom Events

Since `eventsDispatcher` is injected in every `SpectrumTest` and `SpectrumPage`, you can programmatically send custom events and listen to them:

{% include codeHeader.html %}

```java

@DisplayName("Class display name")
public class HelloWorldIT extends SpectrumTest<Void> {

    @Test
    @DisplayName("Method display name")
    public void dummyTest() {
        eventsDispatcher.fire("myCustom primary Id", "my custom reason");
    }
}
```

> 💡 <b>Example</b><br/>
> Check the [DemoIT.events()]({{ site.baseurl }}{{ post.url }}/it/src/test/java/io/github/giulong/spectrum/it/tests/DemoIT.java){:target="_blank"} test to see how to fire custom
> events,
> and the related [configuration.yaml]({{ site.github.url }}/it/src/test/resources/configuration.yaml){:target="_blank"} to check how `eventsConsumers` are set, leveraging regex
> matches
> (more on this below).

## Events Consumers

Time to understand what to do with events: we need consumers!

Given the events' keys defined above, the consumers list will be looped to check if any of those is interested into being notified.
This is done by comparing the fired event's keys with each consumer's events' keys.

Here below are the checks applied. Order matters:
as soon as a check is satisfied, the consumer is notified, and the subsequent checks will not be considered.
Spectrum will proceed with inspecting the next consumer.

1. reason and primaryId and secondaryId
2. reason and just primaryId
3. reason and tags
4. result and primaryId and secondaryId
5. result and just primaryId
6. result and tags

> ⚠️ <b>Matches applied</b><br/>
> You can specify regexes to match Reason, primaryId, and secondaryId.<br/>
> To match tags and result you need to provide the exact value.

> ⚠️ <b>Tags matchers condition</b><br/>
> In the conditions above, "tags" means that the set of tags of the fired event and the set of tags of the consumer event must intersect.
> This means that they don't need to be all matching, it's enough to have at least one match among all the tags. Few examples:

| Fired event's tags | Consumer event's tags | Match        |
|--------------------|-----------------------|--------------|
| \[ tag1, tag2 ]    | \[ tag1, tag2 ]       | ✅ tag1, tag2 |
| \[ tag1, tag2 ]    | \[ tag2 ]             | ✅ tag2       |
| \[ tag1 ]          | \[ tag1, tag2 ]       | ✅ tag1       |
| \[ tag1, tag2 ]    | \[ tag666 ]           | ❌            |

> ⚠️ <b>Consumers exceptions</b><br/>
> Each consumer handles events silently, meaning if any exception is thrown during the handling of an event,
> that will be logged and the execution of the tests will continue without breaking. This is meant to avoid that errors like network issues
> when sending an email can cause the whole suite to fail.

> 💡 <b>Tip</b><br/>
> You can configure how many consumers you need. Each consumer can listen to many events.

Let's now see how to configure few consumers:

> 💡 <b>Example: reason and primaryId and secondaryId</b><br/>
> We want to send a Slack notification before and after a specific test, and an email just after:

{% include codeHeader.html %}

```yaml
eventsConsumers:
  - slack:
      events:
        - primaryId: Class Name
          secondaryId: test name
          reason: before
        - primaryId: Class Name
          secondaryId: test name
          reason: after
  - mail:
      events:
        - primaryId: Class Name
          secondaryId: test name
          reason: after
```

> 💡 <b>Example: result and tags</b><br/>
> We want to send a mail notification if the whole suite fails:

{% include codeHeader.html %}

```yaml
eventsConsumers:
  - mail:
      events:
        - result: FAILED
          tags: [ suite ]
```

> 💡 <b>Example: custom event by primaryId and reason</b><br/>

{% include codeHeader.html %}

```yaml
eventsConsumers:
  - slack:
      events:
        - primaryId: primary.*  # every event which primaryId is starting with "primary" 
          reason: custom-event
```

> 💡 <b>Tip</b><br/>
> Consumers send notification using templates that leverage [FreeMarker](https://freemarker.apache.org/){:target="_blank"}.
> You can do the same in your custom templates, by accessing and evaluating all the event's fields directly in the template,
> and apply logic, if needed.

> 💡 <b>Tip</b><br/>
> You may add how many consumers you want, so if you want to use different templates just add different consumers and provide a template for each.
> Otherwise, if you set many events on the same consumer, they will share the template.

### Mail Consumer

You can leverage this consumer to send email notification. Spectrum uses [Simple java Mail](https://www.simplejavamail.org/){:target="_blank"},
and you can configure it with the file `src/test/resources/simplejavamail.properties`, as specified in
the [docs](https://www.simplejavamail.org/configuration.html#section-config-properties){:target="_blank"}.

For example, to send an email via GMail, you can use these properties by replacing placeholders with actual values:

{% include codeHeader.html %}

```properties
simplejavamail.transportstrategy=SMTP_TLS
simplejavamail.smtp.host=smtp.gmail.com
simplejavamail.smtp.port=587
simplejavamail.smtp.username=<YOUR GMAIL ADDRESS>
simplejavamail.smtp.password=<YOUR GMAIL PASSWORD>
simplejavamail.defaults.trustedhosts=smtp.gmail.com
simplejavamail.defaults.subject=Spectrum Notification
simplejavamail.defaults.to.name=<RECIPIENT NAME>
simplejavamail.defaults.to.address=<RECIPIENT EMAIL ADDRESS>
```

Actual configurations of any email provider to be used are out of scope. For the provided snippet,
check [Google's docs](https://support.google.com/accounts/answer/185833?p=InvalidSecondFactor){:target="_blank"} on how to generate an app password.

Check Simple java Mail's docs to see all the [available properties](https://www.simplejavamail.org/configuration.html#section-available-properties){:target="_blank"}.

> ⚠️ <b>Mail Template</b><br/>
> The default [mail.html template]({{ site.repository_url }}/spectrum/src/main/resources/templates/mail.html){:target="_blank"} is meant to be used to notify about each test
> result,
> as per the snippet below. It might not be correctly interpolated if used on other events.

{% include codeHeader.html %}

```yaml
mail:
  events:
    - reason: after
      tags: [ test ]
```

>
> If you want to provide a custom template there are two ways:
> * provide a template with a custom name under `src/test/resources/templates`:

{% include codeHeader.html %}

```yaml
mail:
  template: my-template.txt # The extension doesn't really matter.
  events:
    - reason: after
      tags: [ test ]
```

> * simply create the file `src/test/resources/templates/mail.html`. This will override the internal default, so there's no need to explicitly provide the `template` parameter.

> 💡 <b>Tip</b><br/>
> You may add how many consumers you want, so if you want to use different templates just add different consumers and provide a template for each.
> Otherwise, if you set many events on the same consumer, they will share the template.

You can also specify a list of attachments, by providing:

* the name they will have in the email
* the path to the file

For example, it's useful to send the html report and/or testbook when the suite is complete:

{% include codeHeader.html %}

```yaml
mail:
  events:
    - reason: after
      tags: [ suite ]
  attachments:
    - name: report
      file: target/spectrum/reports/report.html
    - name: testbook
      file: target/spectrum/testbook/testbook.html
```

> ⚠️ <b>Mail Attachments</b><br/>
> Mind that, like in the snippet above, attachments are specified at consumer level.
> This means for all the events of a specific consumer, all the attachments will be sent.
> If you need to send different sets of attachments, provide different consumers:

{% include codeHeader.html %}

```yaml
eventsConsumers:
  - mail:
      events:
        - reason: after
          tags: [ suite ]
      attachments:
        - name: report
          file: target/spectrum/reports/report.html
  - mail:
      events:
        - reason: after
          tags: [ class ]
      attachments:
        - name: attachment
          file: path/to/attachment
```

### Slack Consumer

A few steps are needed to configure your Slack Workspace to receive notifications from Spectrum:

1. You need to log in and create an app [from here](https://api.slack.com/apps){:target="_blank"} by following these steps:
    1. click on the **Create New App** button:<br/><br/>
       ![slack-new-app.png](images/slack-new-app.png)
    2. choose to create it **from an app manifest**<br/><br/>
       ![slack-manifest.png](images/slack-manifest.png)
    3. Select your workspace, delete the default yaml manifest and copy this one:<br/><br/>
       {% include codeHeader.html %}
       ```yaml
       display_information:
         name: Spectrum
         description: Notification from Spectrum Selenium Framework
         background_color: "#2c2d30"
       features:
         bot_user:
           display_name: Spectrum
           always_online: false
       oauth_config:
         scopes:
           bot:
             - channels:read
             - chat:write
             - incoming-webhook
       settings:
         org_deploy_enabled: false
         socket_mode_enabled: false
         token_rotation_enabled: false
       ```
    4. Click on **Next** and then **Create**<br/><br/>
2. You should have been redirected to the **Basic Information** page of the newly created app. From there:
    1. Install the app to Workspace:<br/><br/>
       ![slack-install-to-workspace.png](images/slack-install-to-workspace.png)
    2. Choose the channel where you want to receive the notifications and click **Allow**:<br/><br/>
       ![slack-channel.png](images/slack-channel.png)
3. Go in the **OAuth & Permissions** page and copy the **Bot User OAuth Token**. You will need this in the `configuration*.yaml` (see last bullet)
   ![slack-token.png](images/slack-token.png)
4. In Slack:
    1. open the channel you chose in the previous steps and invite the Spectrum app by sending this message: `/invite @Spectrum`. You should see this after sending it:<br/><br/>
       ![slack-add-app.png](images/slack-add-app.png)
    2. right-click on the channel you chose in the previous steps and select **View channel details**:<br/><br/>
       ![slack-channel-details.png](images/slack-channel-details.png)
    3. copy the **Channel ID** from the details overlay:<br/><br/>
       ![slack-channel-id.png](images/slack-channel-id.png)
5. Configure the Slack consumer(s) in your `configuration*.yaml` by providing the **token** and the **Channel ID** from the previous steps:
   {% include codeHeader.html %}
   ```yaml
   - slack:
       token: xoxb-***
       channel: C05***
       template: slack-suite.json
       events:
         - reason: before
           tags: [ suite ]
   ```
6. If everything is configured correctly, with the consumer above you should receive a notification at the very beginning of your test suite.

> ⚠️ <b>Slack Template</b><br/>
> The default [slack.json template]({{ site.repository_url }}/spectrum/src/main/resources/templates/slack.json){:target="_blank"} is meant to be used to notify about each
> test result,
> as per the snippet below. It might not be correctly interpolated if used on other events.

{% include codeHeader.html %}

```yaml
slack:
  events:
    - reason: after
      tags: [ test ]
```

>
> If you want to provide a custom template there are two ways:
> * provide a template with a custom name under `src/test/resources/templates`:

{% include codeHeader.html %}

```yaml
slack:
  template: my-template.txt # The extension doesn't really matter.
  events:
    - reason: after
      tags: [ test ]
```

> * simply create the file `src/test/resources/templates/slack.json`. This will override the internal default, so there's no need to explicitly provide the path.
>

> 💡 <b>Tip</b><br/>
> To test the slack handler works as expected, you can provide a simple `template.txt` with just an "Hello World from Spectrum" in it.

---

# TestBook (Coverage)

Talking about coverage for e2e tests is not so straightforward. Coverage makes sense for unit tests, since they directly run against methods,
so it's easy to check which lines were covered during the execution.
On the other hand, e2e tests run against a long living instance of the AUT, with no visibility on the source code.

As e2e tests are tied to the business functionalities of the AUT, so should be their coverage.
Spectrum helps you to keep track of which functionalities are covered by parsing a testbook, in which you can declare all the tests
of your full suite. When running them, Spectrum will check which were actually executed and which not, generating a dedicated report in several formats of your choice.

These are the information needed in a testbook:

| Field      | Type   | Default | Mandatory | Description                                                                                     |
|------------|--------|---------|-----------|-------------------------------------------------------------------------------------------------|
| Class Name | String | null    | ✅         | enclosing class name                                                                            |
| Test Name  | String | null    | ✅         | name of the test method                                                                         |
| Weight     | int    | 1       | ❌         | optional number representing the importance of the related test, with regards to all the others |

In short, we need to uniquely identify each test by their class name and method name.
Method name alone is not enough, since there might be test methods with the same name in different classes.

You can also give a weight to each test: you can give a higher weight to those tests that are meant to check functionalities of the AUT that are more important or critical.
In the report produced, tests will be also aggregated considering their weights. In this way, the final coverage percentage varies based on the weights,
meaning critical functionalities will have a higher impact on the outcome.

Additionally, you can set a Quality Gate, which is a boolean condition that will be evaluated to mark the suite as successful or not.

These are the testbook parameters you need to configure:

| Parameter                        | Description                                                                                 |
|----------------------------------|---------------------------------------------------------------------------------------------|
| [qualityGate](#quality-gate)     | object holding the condition to be evaluated to consider the execution successful or failed |
| [parser](#testbook-parsers)      | object specifying the format of the provided testbook                                       |
| [reporters](#testbook-reporters) | list of objects to specify which kind of report(s) to produce                               |

## Quality Gate

The QG node has only one property, which is the boolean condition to be evaluated:

{% include codeHeader.html %}

```yaml
qualityGate:
  condition: ${weightedSuccessful.percentage} > 60
```

The example above means that the execution is considered successful if more than 60% of the weighted tests are successful.

The condition is evaluated leveraging [FreeMarker](https://freemarker.apache.org/){:target="_blank"}, meaning you can write complex conditions using the variables
briefly explained below. They're all put in the `vars` map in the [TestBook.java]({{ site.repository_url
}}/spectrum/src/main/java/io/github/giulong/spectrum/utils/testbook/TestBook.java){:target="_
blank"}
(check the `mapVars()` method).

> ⚠️ <b>FreeMarker</b><br/>
> Explaining how FreeMarker works, and how to take the most out of it, goes beyond the goal of this doc.
> Please check its own [docs](https://freemarker.apache.org/){:target="_blank"}.

Generic variables:

| Variable                                                                                                                                                  | Description                                                                        |
|-----------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| mappedTests                                                                                                                                               | map of tests executed and found in the provided testbook                           |
| unmappedTests                                                                                                                                             | map of tests executed but not found in the provided testbook                       |
| groupedMappedTests                                                                                                                                        | like mappedTests, but grouped by class names                                       |
| groupedUnmappedTests                                                                                                                                      | like unmappedTests, but grouped by class names                                     |
| [statistics]({{ site.repository_url }}/spectrum/src/main/java/io/github/giulong/spectrum/pojos/testbook/TestBookStatistics.java){:target="_blank"} | object containing all the object reported in the lists below, plus additional ones |
| [qg]({{ site.repository_url }}/spectrum/src/main/java/io/github/giulong/spectrum/pojos/testbook/QualityGate.java){:target="_blank"}                | qualityGate node from `configuration*.yaml`                                        |
| timestamp                                                                                                                                                 | when the testbook was generated                                                    |

Each key in the lists below is an instance of the inner static class [Statistics]({{ site.repository_url
}}/spectrum/src/main/java/io/github/giulong/spectrum/pojos/testbook/TestBookStatistics.java){:
target="_blank"},
and it holds both a `total` int field and a `percentage` double field.
For example, given the `successful` key here below, you can access:

* `${successful.total}`
* `${successful.percentage}`

Statistics of tests mapped in the testbook:

* successful
* failed
* aborted
* disabled
* notRun

Statistics of tests, mapped or not in the provided testbook, based on their weights:

* grandSuccessful
* grandFailed
* grandAborted
* grandDisabled
* grandNotRun

Statistics of tests mapped in the testbook, based on their weights

* weightedSuccessful
* weightedFailed
* weightedAborted
* weightedDisabled
* weightedNotRun

Statistics of all tests, mapped or not in the testbook, based on their weights

* grandWeightedSuccessful
* grandWeightedFailed
* grandWeightedAborted
* grandWeightedDisabled
* grandWeightedNotRun

> 💡 <b>Tip</b><br/>
> It's hard to explain and grasp each of these vars. The best way is to:
> 1. check the [default html template]({{ site.repository_url }}/spectrum/src/main/resources/testbook/template.html){:target="_blank"} and
     the [default txt template]({{ site.repository_url }}/spectrum/src/main/resources/testbook/template.txt){:target="_blank"}
> 2. run your suite with the html reporter and/or the txt reporter as explained below
> 3. check the outcome

## TestBook Parsers

### Txt TestBook Parser

You can provide a txt file where each line is in this format:

`<CLASS NAME>::<TEST NAME>##<WEIGHT>`

For example:

{% include codeHeader.html %}

```text
test class::my test
another class::another test
another class::weighted test##123
```

The first and second rows above maps just a class name and a test name, while the third provides also an explicit weight.

### Csv TestBook Parser

You can provide a csv file where each line is in this format:

`<CLASS NAME>,<TEST NAME>,<WEIGHT>`

For example:

{% include codeHeader.html %}

```text
test class,my test
another class,another test
another class,weighted test,123
```

The first and second rows above maps just a class name and a test name, while the third provides also an explicit weight.

### Yaml TestBook Parser

You can provide a yaml file where you have class names as keys in the root of the file. Each of those is mapped to a list of objects made of two fields:

* name
* weight (optional)

For example:

{% include codeHeader.html %}

```yaml
test class:
  - name: my test

another class:
  - name: another test
  - name: weighted test
    weight: 123
```

## TestBook Reporters

All the reporters below have default values for their parameters, which means you can just configure them as empty objects like:

{% include codeHeader.html %}

```yaml
reporters:
  - log: { }
  - txt: { }
  - html: { }
```

Of course, they're all optional: you can add just those you want.

Below you will find the output produced by the default internal template for each reporter.
Those are the real outputs produced when running Spectrum's own e2e tests you can find in the [it-testbook]({{ site.repository_url }}/it-testbook){:target="_blank"} module.
This means you can:

* check the it_testbook's module [testbook.yaml]({{ site.repository_url }}/it-testbook/src/test/resources/testbook.yaml){:target="_blank"}
* check the actual it_testbook's module [tests]({{ site.repository_url }}/it-testbook/src/test/java/io/github/giulong/spectrum/it_testbook/tests){:target="_blank"} and
  especially their `@DisplayName`
* look at the produced reports shown below for each reporter

> ⚠️ <b>it-testbook module's reports</b><br/>
> The default templates have a "Mapped Tests" and "Unmapped Tests" sections at the bottom,
> in which tests are grouped by their classes.
>
> As you will see from the reports produced below, the `testbook.yaml` used in the `it-testbook` module maps tests that are not present in the suite.
>
> This means all those will be shown in the "Mapped Tests" as "Not Run",
> while all the tests actually executed will appear in the "Unmapped Tests" section with their respective results.

If you want, you can provide a custom template of yours. As for all the other templates (such as those used in events consumers),
you can leverage [FreeMarker](https://freemarker.apache.org/){:target="_blank"}.

For each reporter:

* the snippets below will contain all the customisable parameters for each reporter
* values reported in the snippets below are the defaults (no need to provide them)
* `template` is a path relative to `src/test/resources`
* `output` is the path relative to the project's root, and might contain the `{timestamp}` placeholder

### Log TestBook Reporter

{% include codeHeader.html %}

```yaml
log:
  template: testbook/template.txt
```

Here is the output produced by the default internal template, for tests of the `it-testbook` module:

{% include codeHeader.html %}

```text
##########################################################################################################

                                        SPECTRUM TESTBOOK RESULTS
                                    Generated on: 30/07/2023 15:40:35

##########################################################################################################

STATISTICS
----------------------------------------------------------------------------------------------------------
Mapped Tests:                      4
Unmapped Tests:                    8
Grand Total:              4 + 8 = 12
Total Weighted:                    7
Grand Total Weighted:     7 + 8 = 15
----------------------------------------------------------------------------------------------------------

MAPPED WEIGHTED TESTS RATIO
[Ratio of tests mapped in the TestBook, based on their weights]
----------------------------------------------------------------------------------------------------------
Successful:     0/7      0% <>
Failed:         0/7      0% <>
Aborted:        0/7      0% <>
Disabled:       0/7      0% <>
Not run:        7/7    100% <==================================================================>
----------------------------------------------------------------------------------------------------------

GRAND TOTAL WEIGHTED TESTS RATIO
[Ratio of all tests, mapped or not, based on their weights]
----------------------------------------------------------------------------------------------------------
Successful:    6/15     40% <==========================>
Failed:        1/15   6.67% <====>
Aborted:       0/15      0% <>
Disabled:      1/15   6.67% <====>
Not run:       7/15  46.67% <===============================>
----------------------------------------------------------------------------------------------------------

MAPPED TESTS RATIO
[Ratio of tests mapped in the TestBook]
----------------------------------------------------------------------------------------------------------
Successful:     0/4      0% <>
Failed:         0/4      0% <>
Aborted:        0/4      0% <>
Disabled:       0/4      0% <>
Not run:        4/4    100% <==================================================================>
----------------------------------------------------------------------------------------------------------

GRAND TOTAL TESTS RATIO
[Ratio of tests, mapped or not, based on their weights]
----------------------------------------------------------------------------------------------------------
Successful:    6/12     50% <=================================>
Failed:        1/12   8.33% <=====>
Aborted:       0/12      0% <>
Disabled:      1/12   8.33% <=====>
Not run:       4/12  33.33% <======================>
----------------------------------------------------------------------------------------------------------

QUALITY GATE:
----------------------------------------------------------------------------------------------------------
| Condition: ${weightedSuccessful.percentage} > 60
| Evaluated: 0 > 60
| Result:    KO
----------------------------------------------------------------------------------------------------------
MAPPED TESTS:
----------------------------------------------------------------------------------------------------------
| Test Name                                                                        | Weight | Result     |
| Hello World Selenium-------------------------------------------------------------|--------|------------|
|   - Successful                                                                   |      1 | Not Run    |
|   - This is my first selenium!                                                   |      2 | Not Run    |
|   - another test                                                                 |      3 | Not Run    |
----------------------------------------------------------------------------------------------------------
| Test Name                                                                        | Weight | Result     |
| Second Class---------------------------------------------------------------------|--------|------------|
|   - skipped                                                                      |      1 | Not Run    |
----------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------

UNMAPPED TESTS:
----------------------------------------------------------------------------------------------------------
| Test Name                                                                        | Weight | Result     |
| Demo test------------------------------------------------------------------------|--------|------------|
|   - Skipped Test                                                                 |      1 | Disabled   |
|   - Sending custom events                                                        |      1 | Successful |
|   - This one should fail for demonstration purposes                              |      1 | Failed     |
----------------------------------------------------------------------------------------------------------
| Test Name                                                                        | Weight | Result     |
| Login Form leveraging the data.yaml----------------------------------------------|--------|------------|
|   - with user giulio we expect login to be successful: false                     |      1 | Successful |
|   - with user tom we expect login to be successful: true                         |      1 | Successful |
----------------------------------------------------------------------------------------------------------
| Test Name                                                                        | Weight | Result     |
| Files Test-----------------------------------------------------------------------|--------|------------|
|   - upload                                                                       |      1 | Successful |
|   - download                                                                     |      1 | Successful |
----------------------------------------------------------------------------------------------------------
| Test Name                                                                        | Weight | Result     |
| Checkbox Page--------------------------------------------------------------------|--------|------------|
|   - testWithNoDisplayName()                                                      |      1 | Successful |
----------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------

##########################################################################################################
```

### Txt TestBook Reporter

{% include codeHeader.html %}

```yaml
txt:
  template: testbook/template.txt
  output: target/spectrum/testbook/testbook-{timestamp}.txt
```

For the sake of completeness, the output file was manually copied [here]({{ site.repository_url }}/src/main/resources/images/testbook.txt){:target="_blank"}.
It's the same that is logged, but saved to a dedicated file, so that you can send it as an attachment in an email, for example.
Or you can provide different templates to log a shorter report and send the full thing to a file, it's up to you!

### Html TestBook Reporter

{% include codeHeader.html %}

```yaml
html:
  template: testbook/template.html
  output: target/spectrum/testbook/testbook-{timestamp}.html
```

For the sake of completeness, the output file was manually copied [here]({{ site.repository_url }}/src/main/resources/images/testbook.html){:target="_blank"}.
This is what it looks like when opened in a browser:

![Html TestBook Reporter](images/html-testbook.png)

## Full TestBook Examples

{% include codeHeader.html %}

```yaml
testBook:
  qualityGate:
    condition: ${weightedSuccessful.percentage} > 60  # Execution successful if more than 60% of the weighted tests are successful
  parser:
    yaml:
      path: testbook.yaml # we provided the yaml testbook in src/test/resources/testbook.yaml
  reporters:
    - log: { }  # the report will be logged
    - txt:
        output: target/spectrum/testbook/testbook.txt # a text report will be produced at this path
    - html:
        output: target/spectrum/testbook/testbook.html # a html report will be produced at this path
```

{% include codeHeader.html %}

```yaml
testBook:
  qualityGate:
    condition: ${weightedSuccessful.percentage} > 60  # Execution successful if more than 60% of the weighted tests are successful
  parser:
    yaml:
      path: testbook.yaml # we provided the yaml testbook in src/test/resources/testbook.yaml
  reporters:
    - txt:
        template: template.txt  # we want to produce a text report based on a custom template in src/test/resources
    - html:
        template: my-custom-template.html # src/test/resources/my-custom-template.html
        output: some/path/testbook.html # a html report will be produced at this path
```

{% include codeHeader.html %}

```yaml
testBook:
  qualityGate:
    condition: ${weightedSuccessful.percentage} > 40 || ${failed} < 10  # We want the testbook to be marked as successful if we have at least 40% of successful weighted tests or less than 10 tests (not considering their weights!!) failed
  parser:
    txt:
      path: testbook.txt # we provided the yaml testbook in src/test/resources/testbook.txt
  reporters:
    - log: { }  # we just want the report to be logged
```

---

# Project Structure

Let's see how your project will look like. Few assumptions for this example:

* you defined base values plus three profiles, each with its own set of Data:
    * [base] &rarr; `configuration.yaml` + `data.yaml`
    * local &rarr; `configuration-local.yaml` + `data-local.yaml`
    * test &rarr; `configuration-test.yaml` + `data-test.yaml`
    * uat &rarr; `configuration-uat.yaml` + `data-uat.yaml`
* you configured the yaml testbook parser, which will read the `testbook.yaml`
* you configured both a html and a txt testbook reporters, which will produce `testbook.html` and `testbook.txt` reports

{% include codeHeader.html %}

```text
root
└─ src
|  └─ test
|     ├─ java
|     |  └─ com.your.tests
|     |     └─ ...
|     └─ resources
|        ├─ data
|        |  ├─ data.yaml
|        |  ├─ data-local.yaml
|        |  ├─ data-test.yaml
|        |  └─ data-uat.yaml
|        ├─ configuration.yaml
|        ├─ configuration-local.yaml
|        ├─ configuration-test.yaml
|        ├─ configuration-uat.yaml
|        └─ testbook.yaml
├─ target
|  └─ spectrum
|     |─ logs
|     |  └─ spectrum.log   # rotated daily
|     |─ reports
|     |  |─ screenshots    # folder where Extent Reports screenshots are saved
|     |  |─ videos         # folder where videos are saved
|     |  └─ report.html    # by default the name ends with the timestamp
|     └─ testbook
|        |─ testbook.html  # by default the name ends with the timestamp
|        └─ testbook.txt   # by default the name ends with the timestamp
└─ pom.xml
```

---

# Bugs Report and Feature Requests

Found a bug? Want to request a new feature? Please [open an issue](https://github.com/giulong/spectrum/issues/new/choose){:target="_blank"}.

---

Creator: [Giulio Longfils ![LinkedIn](https://i.stack.imgur.com/gVE0j.png)](https://www.linkedin.com/in/giuliolongfils/){:target="_blank"}
