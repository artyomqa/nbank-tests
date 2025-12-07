package ui.utils.extensions;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import ui.utils.annotations.Browsers;

import java.util.Arrays;

public class BrowserMatchExtension implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Browsers annotation = context.getElement()
                .map(el -> el.getAnnotation(Browsers.class))
                .orElse(null);

        if (annotation == null) {
            return ConditionEvaluationResult.enabled("Нет ограничений по браузеру.");
        }

        String currentBrowser = Configuration.browser;
        boolean isSupported = Arrays.asList(annotation.value()).contains(currentBrowser);

        if (isSupported) {
            return ConditionEvaluationResult.enabled("Текущий браузер " + currentBrowser + " подходит для теста.");
        } else {
            return ConditionEvaluationResult.disabled("Тест пропущен. Текущий браузер " + currentBrowser +
                    " отсутствует в списке допустимых для теста: " + Arrays.toString(annotation.value()));
        }
    }
}
