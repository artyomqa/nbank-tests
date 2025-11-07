package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

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
        editProfileHeader.shouldBe(Condition.visible);
        return this;
    }

    public EditProfilePage changeName(String newName) {
        enterNameInput.setValue(newName);
        saveButton.click();
        return this;
    }

    public EditProfilePage goToHome() {
        homeButton.click();
        return this;
    }
}
