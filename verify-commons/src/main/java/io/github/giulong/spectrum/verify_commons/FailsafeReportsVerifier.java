package io.github.giulong.spectrum.verify_commons;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

import static lombok.AccessLevel.PRIVATE;
import static org.slf4j.event.Level.ERROR;
import static org.slf4j.event.Level.INFO;

@NoArgsConstructor(access = PRIVATE)
@Slf4j
public class FailsafeReportsVerifier {

    private static final FailsafeReportsVerifier INSTANCE = new FailsafeReportsVerifier();

    private static final Path BASE_DIR = Path.of(System.getProperty("user.dir")).getParent();

    private static final String FORMAT = "%-10s %10s %10s %s";

    private static final String ERROR_MARKER = "<----";

    private final ObjectMapper mapper = new XmlMapper();

    public static FailsafeReportsVerifier getInstance() {
        return INSTANCE;
    }

    @SneakyThrows
    public Report read(final Path filePath) {
        return mapper.readValue(filePath.toFile(), Report.class);
    }

    public boolean verifyCompletedAre(final Report report, final int completed) {
        return report.completed == completed;
    }

    public boolean verifyErrorsAre(final Report report, final int errors) {
        return report.errors == errors;
    }

    public boolean verifyFailuresAre(final Report report, final int failures) {
        return report.failures == failures;
    }

    public boolean verifySkippedAre(final Report report, final int skipped) {
        return report.skipped == skipped;
    }

    public boolean verifyResultsAre(final String module, final String driver, final int completed, final int errors, final int failures, final int skipped) {
        final Path filePath = Path.of(module, "target", "failsafe-reports", String.format("failsafe-%s.xml", driver));
        final Report report = read(BASE_DIR.resolve(filePath));
        final boolean result = verifyCompletedAre(report, completed) &&
                verifyErrorsAre(report, errors) &&
                verifyFailuresAre(report, failures) &&
                verifySkippedAre(report, skipped);


        final String header = String.format(FORMAT, "", "expected", "actual", "");
        final String completedLine = String.format(FORMAT, "completed:", completed, report.completed, completed != report.completed ? ERROR_MARKER : "");
        final String errorsLine = String.format(FORMAT, "errors:", errors, report.errors, errors != report.errors ? ERROR_MARKER : "");
        final String failuresLine = String.format(FORMAT, "failures:", failures, report.failures, failures != report.failures ? ERROR_MARKER : "");
        final String skippedLine = String.format(FORMAT, "skipped:", skipped, report.skipped, skipped != report.skipped ? ERROR_MARKER : "");

        log
                .atLevel(result ? INFO : ERROR)
                .log("""
                                                        
                                Results for report '{}' are:
                                
                                {}
                                {}
                                {}
                                {}
                                {}
                                          """,
                        filePath,
                        header,
                        completedLine,
                        errorsLine,
                        failuresLine,
                        skippedLine);

        return result;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @SuppressWarnings("unused")
    public static class Report {
        private int completed;
        private int errors;
        private int failures;
        private int skipped;
    }
}
