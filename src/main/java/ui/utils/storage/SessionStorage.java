package ui.utils.storage;

import common.steps.User;

import java.util.Arrays;
import java.util.LinkedList;

public class SessionStorage {
    private static final ThreadLocal<SessionStorage> INSTANCE = ThreadLocal.withInitial(SessionStorage::new);
    private final LinkedList<User> users = new LinkedList<>();

    private SessionStorage() {}

    public static void addUsers(User... users) {
        INSTANCE.get().users.addAll(Arrays.asList(users));
    }

    public static void clear() {
        INSTANCE.get().users.clear();
    }

    public static User getUser(int number) {
        if (INSTANCE.get().users.isEmpty()) return null;
        return INSTANCE.get().users.get(number - 1);
    }

    public static User getUser() {
        return getUser(1);
    }
}
