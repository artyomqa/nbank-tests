package api.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TestUtils {
    public static float getCorrectAmount(float value) {
        return (float) Math.round(value * 100) / 100;
    }

    public static BigDecimal getCorrectBigDecimal(float value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
