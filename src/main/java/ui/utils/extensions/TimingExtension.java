package ui.utils.extensions;

import common.configs.Config;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private final boolean loggingEnabled = Config.getBoolean("log.testDuration.enabled");
    private final Map<String, LocalTime> startTimes = new HashMap<>();

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        if (!loggingEnabled) return;

        String testName = "[" + context.getRequiredTestClass().getPackageName() + "] " + context.getDisplayName();
        startTimes.put(testName, LocalTime.now());
        System.out.printf("\uD83D\uDE80 Thread: %s | Test: %s  | Start time: %s\n", Thread.currentThread().getName(), testName,LocalTime.now());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        if (!loggingEnabled) return;

        String testName = "[" + context.getRequiredTestClass().getPackageName() + "] " + context.getDisplayName();
        Duration testDuration = Duration.between(startTimes.get(testName), LocalTime.now());
        System.out.printf("\uD83C\uDFC1 Thread: %s | Test: %s  | Test duration: %s\n", Thread.currentThread().getName(), testName, getFormattedTime(testDuration));
    }

    private String getFormattedTime(Duration duration) {
        return String.format("%02d:%02d:%02d.%03d",
                duration.toHours(),
                duration.toMinutesPart(),
                duration.toSecondsPart(),
                duration.toMillisPart());
    }
}
