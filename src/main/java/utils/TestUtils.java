package utils;

public class TestUtils {
    public static float getCorrectAmount(float value) {
        return (float) Math.round((value) * 100) / 100;
    }
}
