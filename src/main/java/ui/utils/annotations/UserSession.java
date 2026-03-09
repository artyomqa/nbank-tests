package ui.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UserSession {
    // Количество пользователей
    int users() default 1;
    // Количество счетов для каждого пользователя (поддерживается 0-2)
    int userAccounts() default 1;
    // Порядковый номер пользователя, под которым авторизовываемся
    int auth() default 1;
}
