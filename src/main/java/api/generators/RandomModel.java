package api.generators;

import api.utils.annotations.GeneratePattern;
import com.mifmif.common.regex.Generex;

import java.lang.reflect.Field;

public class RandomModel {
    public static <T> T generate(Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(GeneratePattern.class)) {
                    GeneratePattern annotation = field.getAnnotation(GeneratePattern.class);
                    String regex = annotation.regex();
                    String value = new Generex(regex).random();

                    field.setAccessible(true);
                    field.set(instance, value);
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Не удалось сгенерировать данные для класса " + clazz.getSimpleName(), e);
        }
    }
}
