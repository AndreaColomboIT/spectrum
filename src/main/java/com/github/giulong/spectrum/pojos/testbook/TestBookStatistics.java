package com.github.giulong.spectrum.pojos.testbook;

import com.github.giulong.spectrum.enums.TestBookResult;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class TestBookStatistics {

    private final AtomicInteger grandTotal = new AtomicInteger();
    private final AtomicInteger totalWeighted = new AtomicInteger();
    private final AtomicInteger grandTotalWeighted = new AtomicInteger();

    private final Map<TestBookResult, TestStatistics> totalCount = new HashMap<>();
    private final Map<TestBookResult, TestStatistics> grandTotalCount = new HashMap<>();
    private final Map<TestBookResult, TestStatistics> totalWeightedCount = new HashMap<>();
    private final Map<TestBookResult, TestStatistics> grandTotalWeightedCount = new HashMap<>();

    @Getter
    public static class TestStatistics {
        private final AtomicInteger total = new AtomicInteger();
        private final AtomicDouble percentage = new AtomicDouble();
    }
}
