package io.vertx.blog.first;

import static com.jayway.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

import com.jayway.restassured.RestAssured;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RestIT {

	@BeforeClass
	public static void configureRestAssured() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = Integer.getInteger("http.port", 8080);
	}

	@AfterClass
	public static void unconfigureRestAssured() {
		RestAssured.reset();
	}

	@Test
	public void checkWeCanAddAndGetAndUpdateAndDeleteAProduct() {
		Whisky whisky = given().body("{\"name\":\"Jameson\", \"origin\":\"Ireland\"}")
				.request().post("/api/whiskies")
				.thenReturn().as(Whisky.class);
		assertThat(whisky.getName()).isEqualToIgnoringCase("Jameson");
		assertThat(whisky.getOrigin()).isEqualToIgnoringCase("Ireland");

		get("/api/whiskies/" + whisky.getId())
				.then().assertThat()
				.statusCode(200)
				.body("name", equalTo("Jameson"))
				.body("origin", equalTo("Ireland"))
				.body("id", equalTo(whisky.getId()));

		given().body("{\"name\":\"Teachers\", \"origin\":\"Scotland\"}")
				.request().put("/api/whiskies/" + whisky.getId())
				.then().assertThat()
				.statusCode(200);

		get("/api/whiskies/" + whisky.getId())
				.then().assertThat()
				.statusCode(200)
				.body("name", equalTo("Teachers"))
				.body("origin", equalTo("Scotland"))
				.body("id", equalTo(whisky.getId()));

		delete("/api/whiskies/" + whisky.getId())
				.then().assertThat()
				.statusCode(204);

		get("/api/whiskies/" + whisky.getId()).then()
				.assertThat()
				.statusCode(404);
	}

	@Test
	public void checkUpdateNullProductReturns400() {
		given().body("{\"name\":\"Teachers\", \"origin\":\"Scotland\"}")
				.request().put("/api/whiskies/")
				.then().assertThat()
				.statusCode(404);
	}

	@Test
	public void checkUpdateUnknownProductReturns404() {
		given().body("{\"name\":\"Teachers\", \"origin\":\"Scotland\"}")
				.request().put("/api/whiskies/1")
				.then().assertThat()
				.statusCode(404);
	}
}