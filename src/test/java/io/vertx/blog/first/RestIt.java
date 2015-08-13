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
	public void checkThatWeCanRetrieveIndividualProduct() {
		final int id = get("/api/whiskies").then()
				.assertThat()
				.statusCode(200)
				.extract()
				.jsonPath().getInt("find { it.name=='Bowmore 15 Years Laimrig' }.id");

		get("/api/whiskies/" + id).then()
				.assertThat()
				.statusCode(200)
				.body("name", equalTo("Bowmore 15 Years Laimrig"))
				.body("origin", equalTo("Scotland, Islay"))
				.body("id", equalTo(id));
	}

	@Test
	public void checkWeCanAddAndDeleteAProduct() {
		Whisky whisky = given().body("{\"name\":\"Jameson\", \"origin\":\"Ireland\"}")
				.request().post("/api/whiskies")
				.thenReturn().as(Whisky.class);
		assertThat(whisky.getName()).isEqualToIgnoringCase("Jameson");
		assertThat(whisky.getOrigin()).isEqualToIgnoringCase("Ireland");
		assertThat(whisky.getId()).isNotZero();

		get("/api/whiskies/" + whisky.getId())
				.then().assertThat()
				.statusCode(200)
				.body("name", equalTo("Jameson"))
				.body("origin", equalTo("Ireland"))
				.body("id", equalTo(whisky.getId()));

		delete("/api/whiskies/" + whisky.getId())
				.then().assertThat()
				.statusCode(204);

		get("/api/whiskies/" + whisky.getId()).then()
				.assertThat()
				.statusCode(404);
	}
}