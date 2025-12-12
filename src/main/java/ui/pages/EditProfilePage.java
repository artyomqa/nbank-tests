package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static io.qameta.allure.Allure.step;

public class EditProfilePage extends BasePage<EditProfilePage> {
    private final SelenideElement editProfileHeader = $$("h1").findBy(Condition.text("Edit Profile"));
    private final SelenideElement enterNameInput = $("input[placeholder='Enter new name']");
    private final SelenideElement saveButton = $$("button").findBy(Condition.text("Save Changes"));
    private final SelenideElement homeButton = $$("button").findBy(Condition.text("Home"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    @Override
    public EditProfilePage shouldBeOpened() {
        step("[UI] Проверяем, что страница " + url() + " открыта", () -> {
            editProfileHeader.shouldBe(Condition.visible);
            attachScreenshot();
        });

        return this;
    }

    public EditProfilePage changeName(String newName) {
        step("[UI] Вводим новое имя пользователя", () -> {
            enterNameInput.setValue(newName);
            attachScreenshot();
        });

        step("[UI] Клик по кнопке Save Changes", () -> {
            saveButton.click();
        });

        return this;
    }

    public EditProfilePage goToHome() {
        step("[UI] Клик по кнопке Home", () -> {
            homeButton.click();
        });

        return this;
    }
}
