package ui.utils.extensions;

import common.steps.User;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ui.pages.BasePage;
import ui.utils.annotations.UserSession;
import ui.utils.storage.SessionStorage;

public class UserSessionExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        UserSession annotation = context.getRequiredTestMethod().getAnnotation(UserSession.class);
        if (annotation == null) return;

        SessionStorage.clear();

        for (int i = 0; i < annotation.users(); i++) {
            User user = new User.Builder()
                    .createRandomUser()
                    .createAccounts(annotation.userAccounts())
                    .build();

            SessionStorage.addUsers(user);
        }

        BasePage.auth(SessionStorage.getUser(annotation.auth()));
    }
}
