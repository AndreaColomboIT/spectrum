package com.giuliolongfils.spectrum.utils.testbook.reporters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.giuliolongfils.spectrum.pojos.testbook.TestBook;
import com.giuliolongfils.spectrum.pojos.testbook.TestBookResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

@Slf4j
public class LogTestBookReporter extends TestBookReporter {

    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private String template = "/testbook/template.txt";

    @JsonIgnore
    private int longestName;

    @Override
    public void updateWith(final TestBook testBook) {
        longestName = Stream.of(testBook.getTests().keySet(), testBook.getUnmappedTests().keySet()).flatMap(Set::stream)
                .collect(toSet())
                .stream().max(comparingInt(String::length))
                .orElse("")
                .length();

        log.info(parse(template, testBook));
    }

    @Override
    public String getTestsReplacementFrom(TestBook testBook) {
        return format(testBook.getTests());
    }

    @Override
    public String getUnmappedTestsReplacementFrom(TestBook testBook) {
        return format(testBook.getUnmappedTests());
    }

    public String format(final Map<String, TestBookResult> tests) {
        return tests
                .entrySet()
                .stream()
                .map(e -> String.format("%" + longestName + "s -> %-8s", e.getKey(), e.getValue().getStatus().getValue()))
                .collect(joining("\n\t\t"));
    }
}
