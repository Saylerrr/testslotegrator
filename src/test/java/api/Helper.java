package api;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class Helper {

    private static final String BASE_URL = "https://testslotegrator.com";
    private static final String EMAIL = "kriko1@mail.ru";
    private static final String PASSWORD = "BhN5zSbBVNxv";

    public static void setBaseUri() {
        RestAssured.baseURI = BASE_URL;
    }

    public static RequestSpecification authRequest(String token) {
        return given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token);
    }

    public static RequestSpecification login() {
        String body = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", EMAIL, PASSWORD);

        return given()
                .contentType("application/json")
                .body(body);
    }
}
