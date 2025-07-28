package api;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiTest {

    //private static String TOKEN2 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImtyaWtvMUBtYWlsLnJ1IiwiaWQiOiI2ODgzOGQyMDg4ZjZjMjNlZGNkNzc0Y2IiLCJyb2xlIjoiOGY5YmZlOWQxMzQ1MjM3Y2IzYjJiMjA1ODY0ZGEwNzUiLCJwb3NpdGlvbiI6ImNhZWE4MzQwZTJkMTg2YTU0MDUxOGQwODYwMmFhMDY1IiwiaWF0IjoxNzUzNjQxNDcwLCJleHAiOjE3NTM3Mjc4NzB9.-WEgNilQ9vCXPbENPBWS6roNZ3EIfNiJVUnRYpAztxg";
    static String TOKEN;
    static List<Integer> userId = new ArrayList<>();
    static List<String> userEmail = new ArrayList<>();


    @BeforeAll
    static void setup() {
        Helper.setBaseUri();
    }

    @Test
    @Order(1)
    void testLoginAndGetToken() {

        Response response = Helper.login()
                .when()
                .post("/api/tester/login")
                .andReturn();

        TOKEN = response.jsonPath().getString("accessToken");
        //System.out.println(TOKEN);
        response.then().statusCode(200);
    }

    @Test
    @Order(2)
    void register12Users() {
        IntStream.range(0, 12).forEach(i -> {
            String username = "user" + i;
            String email = "user" + i + "@test.com";

            Map<String, Object> user = Map.of(
                    "currency_code", "USD",
                    "email", email,
                    "name", "Name" + i,
                    "password_change", "pass1234",
                    "password_repeat", "pass1234",
                    "surname", "Surname" + i,
                    "username", username
            );

            Response response = Helper.authRequest(TOKEN)
                    .body(user)
                    .when()
                    .post("/api/automationTask/create")
                    .then()
                    .statusCode(201)
                    .body("username", equalTo(username))
                    .body("email", equalTo(email))
                    .body("name", equalTo("Name" + i))
                    .body("surname", equalTo("Surname" + i))
                    .extract()
                    .response();
            //System.out.println(response.asString());

            userEmail.add(response.jsonPath().getString("email"));

            Map<String, Object> users = response.jsonPath().getMap("$");
            Assertions.assertEquals(5, users.size(), "Количество полей в ответе не совпадает");

            Object id = response.jsonPath().get("id");
            Assertions.assertTrue(id instanceof Integer, "id должен быть Integer");
            userId.add(response.jsonPath().getInt("id"));
        });
    }

    @Test
    @Order(3)
    void getOneUser() {
        String testEmail = userEmail.get(0);

        Map<String, Object> payload = Map.of("email", testEmail);

        Response response = Helper.authRequest(TOKEN)
                .body(payload)
                .when()
                .post("/api/automationTask/getOne")
                .then()
                .body("email", equalTo(testEmail))
                .body("username", equalTo("user0"))
                .body("name", equalTo("Name0"))
                .body("surname", equalTo("Surname0"))
                .statusCode(200)
                .extract()
                .response();

        Map<String, Object> users = response.jsonPath().getMap("$");
        Assertions.assertEquals(5, users.size(), "Количество полей в ответе не совпадает");
    }

    @Test
    @Order(4)
    void getAllUsersSortedByName() {
        Response response = Helper.authRequest(TOKEN)
                .when()
                .get("/api/automationTask/getAll")
                .then()
                .statusCode(200)
                .extract()
                .response();

        List<Map<String, Object>> users = response.jsonPath().getList("$");
        Assertions.assertFalse(users.isEmpty());

        List<Map<String, Object>> sortedUsersByName = users.stream()
                .sorted(Comparator.comparing(u -> String.valueOf(u.get("name"))))
                .collect(Collectors.toList());

        //System.out.println(sortedUsersByName);

    }

    @Test
    @Order(5)
    void deleteAllCreatedUsers() {
        if (userId.isEmpty()) {
            Assertions.fail("Список id пользователей пуст – нечего удалять.");
            return;
        }
        userId.forEach(id -> {
            Helper.authRequest(TOKEN)
                    .when()
                    .delete("/api/automationTask/deleteOne/" + id)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(id));
        });
    }

    @Test
    @Order(6)
    void getAllUsersAndEnsureEmpty() {
        Response response = Helper.authRequest(TOKEN)
                .when()
                .get("/api/automationTask/getAll")
                .then()
                .statusCode(200)
                .extract()
                .response();

        List<Map<String, Object>> users = response.jsonPath().getList("$");

        Assertions.assertTrue(users.isEmpty(), "Список пользователей не пуст.");
    }
}

