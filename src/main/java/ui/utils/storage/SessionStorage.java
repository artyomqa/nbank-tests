package ui.utils.storage;

import common.steps.User;

import java.util.Arrays;
import java.util.LinkedList;

public class SessionStorage {
    private static final SessionStorage INSTANCE = new SessionStorage();
    private final LinkedList<User> users = new LinkedList<>();

    private SessionStorage() {}

    public static void addUsers(User... users) {
        INSTANCE.users.addAll(Arrays.asList(users));
    }

    public static void clear() {
        INSTANCE.users.clear();
    }

    public static User getUser(int number) {
        if (INSTANCE.users.isEmpty()) return null;
        return INSTANCE.users.get(number - 1);
    }

    public static User getUser() {
        return getUser(1);
    }
}
