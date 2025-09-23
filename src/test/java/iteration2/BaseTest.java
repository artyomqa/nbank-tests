package iteration2;

import io.restassured.RestAssured;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class BaseTest {
    protected static final float MAX_DEPOSIT_AMOUNT = 5000.00f;
    protected static final float MAX_TRANSFER_AMOUNT = 10000.00f;

    protected SoftAssertions softly;

    @BeforeAll
    public static void globalSetup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 4111;
    }

    @BeforeEach
    public void setupTest() {
        softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }
}
