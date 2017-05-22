package com.mprevisic.user.test.functional;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.response.ExtractableResponse;
import com.jayway.restassured.response.Response;
import com.mprevisic.user.repository.UserRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserApiIT {

	@Autowired
	private UserRepository userRepo;

	private static final Map<String, Object> defaultUser = new HashMap<>();

	private static final Map<String, Object> defaultCredentials = new HashMap<>();

	static {
		defaultUser.put("email", "user@gmail.com");
		defaultUser.put("password", "temp213test");
		defaultUser.put("role", 1);
		defaultUser.put("firstName", "John");
		defaultUser.put("lastName", "Doe");
		defaultUser.put("phoneCode", "+385");
		defaultUser.put("phoneNumber", "999-222-333");
		defaultUser.put("title", "Mr.");

		defaultCredentials.put("email", "user@gmail.com");
		defaultCredentials.put("password", "temp213test");
	}

	@Before
	public void setUp() {
		userRepo.deleteAll();
		RestAssured.port = 8080;
		RestAssured.defaultParser = Parser.JSON;
	}

	/**
	 * Tests HTTP OPTIONS method for session resource
	 */
	@Test
	public void getSessionOptions() {
		when().options("/api/v1/session").then().statusCode(200).header("Allow", "OPTIONS,POST");
	}

	/**
	 * Tests HTTP OPTIONS method for token resource
	 */
	@Test
	public void getTokenOptions() {
		when().options("/api/v1/token").then().statusCode(200).header("Allow", "OPTIONS,POST");
	}

	/**
	 * Tests HTTP OPTIONS method for user resource
	 */
	@Test
	public void getUserOptions() {
		when().options("/api/v1/users/").then().statusCode(200).header("Allow", "OPTIONS,POST,GET,PATCH,DELETE");
	}

	/**
	 * Attempts to get an user without being authenticated
	 */
	@Test
	public void getUserUnauthorized() {
		when().get("/api/v1/users/4").then().statusCode(401);
	}

	/**
	 * Registers a new user
	 */
	@Test
	public void register() {
		given().contentType(ContentType.JSON).body(defaultUser).when().post("/api/v1/users").then().statusCode(201);
	}

	/**
	 * Registers a new user and logs the user in
	 */
	@Test
	public void login() {
		given().contentType(ContentType.JSON).body(defaultUser).when().post("/api/v1/users").then().statusCode(201);

		given().contentType(ContentType.JSON).body(defaultCredentials).when().post("/api/v1/session").then()
				.statusCode(200).cookie("jwt-refresh-token").cookie("jwt-access-token").cookie("xsrf-token");
	}

	/**
	 * Attempts to obtain a session with wrong password of the user
	 */
	@Test
	public void loginBadPassword() {
		given().contentType(ContentType.JSON).body(defaultUser).when().post("/api/v1/users").then().statusCode(201);

		Map<String, Object> credentials = new HashMap<String, Object>();

		given().contentType(ContentType.JSON).body(credentials).when().post("/api/v1/session").then().statusCode(401);
	}

	/**
	 * Obtains a new JTW Access Token by using the JWT Refresh Token
	 */
	@Test
	public void refreshToken() {
		given().contentType(ContentType.JSON).body(defaultUser).when().post("/api/v1/users").then().statusCode(201);

		ExtractableResponse<Response> resp = given().contentType(ContentType.JSON).body(defaultCredentials).when()
				.post("/api/v1/session").then().statusCode(200).cookie("jwt-refresh-token").cookie("jwt-access-token")
				.cookie("xsrf-token").extract();

		String refToken = resp.cookie("jwt-refresh-token");

		given().contentType(ContentType.JSON).cookie("jwt-refresh-token", refToken).when().post("/api/v1/token/").then()
				.statusCode(200).cookie("jwt-access-token").cookie("xsrf-token");
	}

	/**
	 * Deleted user attempts to refresh the access token
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void refreshTokenDeletedUser() {
		Map<String, Object> savedUser = given().contentType(ContentType.JSON).body(defaultUser).when()
				.post("/api/v1/users").then().statusCode(201).extract().body().as(Map.class);

		Integer userId = (Integer) savedUser.get("userId");

		ExtractableResponse<Response> resp = given().contentType(ContentType.JSON).body(defaultCredentials).when()
				.post("/api/v1/session").then().statusCode(200).cookie("jwt-refresh-token").cookie("jwt-access-token")
				.cookie("xsrf-token").extract();

		String refToken = resp.cookie("jwt-refresh-token");

		Map<String, String> cookies = resp.cookies();

		given().header("xsrf-token", cookies.get("xsrf-token")).cookies(cookies).when()
				.delete("/api/v1/users/" + userId).then().statusCode(204);

		given().contentType(ContentType.JSON).cookie("jwt-refresh-token", refToken).when().post("/api/v1/token/").then()
				.statusCode(401);
	}

	/**
	 * Gets a user
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void getUser() {
		Map<String, Object> savedUser = given().contentType(ContentType.JSON).body(defaultUser).when()
				.post("/api/v1/users").then().statusCode(201).extract().body().as(Map.class);

		Integer userId = (Integer) savedUser.get("userId");

		ExtractableResponse<Response> resp = given().contentType(ContentType.JSON).body(defaultCredentials).when()
				.post("/api/v1/session").then().statusCode(200).cookie("jwt-refresh-token").cookie("jwt-access-token")
				.cookie("xsrf-token").extract();

		Map<String, String> cookies = resp.cookies();

		given().header("xsrf-token", cookies.get("xsrf-token")).cookies(cookies).when().get("/api/v1/users/" + userId)
				.then().statusCode(200);
	}

	/**
	 * Tries to get a user with a bad jwt access token
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void badJwtAccessToken() {
		Map<String, Object> savedUser = given().contentType(ContentType.JSON).body(defaultUser).when()
				.post("/api/v1/users").then().statusCode(201).extract().body().as(Map.class);

		Integer userId = (Integer) savedUser.get("userId");

		ExtractableResponse<Response> resp = given().contentType(ContentType.JSON).body(defaultCredentials).when()
				.post("/api/v1/session").then().statusCode(200).cookie("jwt-refresh-token").cookie("jwt-access-token")
				.cookie("xsrf-token").extract();

		Map<String, String> cookies = new HashMap<>(resp.cookies());

		cookies.put("jwt-access-token", "blabla");

		given().header("xsrf-token", cookies.get("xsrf-token")).cookies(cookies).when().get("/api/v1/users/" + userId)
				.then().statusCode(401);
	}

	/**
	 * Tries to get a user with a bad CSRF token
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void badXsrfToken() {
		Map<String, Object> savedUser = given().contentType(ContentType.JSON).body(defaultUser).when()
				.post("/api/v1/users").then().statusCode(201).extract().body().as(Map.class);

		Integer userId = (Integer) savedUser.get("userId");

		ExtractableResponse<Response> resp = given().contentType(ContentType.JSON).body(defaultCredentials).when()
				.post("/api/v1/session").then().statusCode(200).cookie("jwt-refresh-token").cookie("jwt-access-token")
				.cookie("xsrf-token").extract();

		Map<String, String> cookies = resp.cookies();

		given().header("xsrf-token", "xyz123").cookies(cookies).when().get("/api/v1/users/" + userId).then()
				.statusCode(401);
	}

	/**
	 * Tries to get a non-existing user
	 */
	@Test
	public void getNonExistingUser() {
		given().contentType(ContentType.JSON).body(defaultUser).when().post("/api/v1/users").then().statusCode(201);

		ExtractableResponse<Response> resp = given().contentType(ContentType.JSON).body(defaultCredentials).when()
				.post("/api/v1/session").then().statusCode(200).cookie("jwt-refresh-token").cookie("jwt-access-token")
				.cookie("xsrf-token").extract();

		Map<String, String> cookies = resp.cookies();

		given().header("xsrf-token", cookies.get("xsrf-token")).cookies(cookies).when().get("/api/v1/users/666").then()
				.statusCode(404);
	}

	/**
	 * Deletes a user
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void deleteUser() {
		Map<String, Object> savedUser = given().contentType(ContentType.JSON).body(defaultUser).when()
				.post("/api/v1/users").then().statusCode(201).extract().body().as(Map.class);

		Integer userId = (Integer) savedUser.get("userId");

		ExtractableResponse<Response> resp = given().contentType(ContentType.JSON).body(defaultCredentials).when()
				.post("/api/v1/session").then().statusCode(200).cookie("jwt-refresh-token").cookie("jwt-access-token")
				.cookie("xsrf-token").extract();

		Map<String, String> cookies = resp.cookies();

		given().header("xsrf-token", cookies.get("xsrf-token")).cookies(cookies).when()
				.delete("/api/v1/users/" + userId).then().statusCode(204);
	}
	
	/**
	 * Attempts to delete a user without valid CSRF token
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void deleteUserUnauthorized() {
		Map<String, Object> savedUser = given().contentType(ContentType.JSON).body(defaultUser).when()
				.post("/api/v1/users").then().statusCode(201).extract().body().as(Map.class);

		Integer userId = (Integer) savedUser.get("userId");

		ExtractableResponse<Response> resp = given().contentType(ContentType.JSON).body(defaultCredentials).when()
				.post("/api/v1/session").then().statusCode(200).cookie("jwt-refresh-token").cookie("jwt-access-token")
				.cookie("xsrf-token").extract();

		Map<String, String> cookies = resp.cookies();

		given().header("xsrf-token", "xyz123").cookies(cookies).when()
				.delete("/api/v1/users/" + userId).then().statusCode(401);
	}

	/**
	 * Attempts to delete a non-existing user
	 */
	@Test
	public void deleteNonExistingUser() {
		given().contentType(ContentType.JSON).body(defaultUser).when().post("/api/v1/users").then().statusCode(201);

		ExtractableResponse<Response> resp = given().contentType(ContentType.JSON).body(defaultCredentials).when()
				.post("/api/v1/session").then().statusCode(200).cookie("jwt-refresh-token").cookie("jwt-access-token")
				.cookie("xsrf-token").extract();

		Map<String, String> cookies = resp.cookies();

		given().header("xsrf-token", cookies.get("xsrf-token")).cookies(cookies).when().delete("/api/v1/users/666")
				.then().statusCode(404);
	}

	/**
	 * Modifies a user
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void modifyUser() {
		Map<String, Object> savedUser = given().contentType(ContentType.JSON).body(defaultUser).when()
				.post("/api/v1/users").then().statusCode(201).extract().body().as(Map.class);

		Integer userId = (Integer) savedUser.get("userId");

		ExtractableResponse<Response> resp = given().contentType(ContentType.JSON).body(defaultCredentials).when()
				.post("/api/v1/session").then().statusCode(200).cookie("jwt-refresh-token").cookie("jwt-access-token")
				.cookie("xsrf-token").extract();

		Map<String, String> cookies = resp.cookies();

		Map<String, Object> newProps = new HashMap<>();
		newProps.put("role", 22);

		given().header("xsrf-token", cookies.get("xsrf-token")).cookies(cookies).contentType(ContentType.JSON)
				.body(newProps).when().patch("/api/v1/users/" + userId).then().statusCode(200);
	}
	
	/**
	 * Attempts to modify a user without a valid JWT access token
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void modifyUserUnauthorized() {
		Map<String, Object> savedUser = given().contentType(ContentType.JSON).body(defaultUser).when()
				.post("/api/v1/users").then().statusCode(201).extract().body().as(Map.class);

		Integer userId = (Integer) savedUser.get("userId");

		ExtractableResponse<Response> resp = given().contentType(ContentType.JSON).body(defaultCredentials).when()
				.post("/api/v1/session").then().statusCode(200).cookie("jwt-refresh-token").cookie("jwt-access-token")
				.cookie("xsrf-token").extract();

		Map<String, String> cookies = new HashMap<>(resp.cookies());
		cookies.put("jwt-access-token", "blabla");

		Map<String, Object> newProps = new HashMap<>();
		newProps.put("role", 22);

		given().header("xsrf-token", cookies.get("xsrf-token")).cookies(cookies).contentType(ContentType.JSON)
				.body(newProps).when().patch("/api/v1/users/" + userId).then().statusCode(401);
	}

}
