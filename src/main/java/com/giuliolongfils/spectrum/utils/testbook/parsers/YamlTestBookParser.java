package com.giuliolongfils.spectrum.utils.testbook.parsers;

import com.giuliolongfils.spectrum.pojos.testbook.TestBookTest;
import com.giuliolongfils.spectrum.pojos.testbook.TestBookYamlData;
import com.giuliolongfils.spectrum.utils.YamlParser;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
public class YamlTestBookParser extends TestBookParser {

    @Override
    public List<TestBookTest> parse() {
        log.debug("Reading lines of yaml testbook");

        return YamlParser.getInstance()
                .read(path, TestBookYamlData.class)
                .entrySet()
                .stream()
                .flatMap(e -> e.getValue()
                        .stream()
                        .map(v -> TestBookTest.builder()
                                .className(e.getKey())
                                .testName(v.getName())
                                .weight(v.getWeight())
                                .build()))
                .collect(toList());
    }
}
