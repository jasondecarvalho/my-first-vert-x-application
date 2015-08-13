package io.vertx.blog.first;

import java.io.IOException;
import java.net.ServerSocket;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MyFirstVerticleTest {

	private Vertx vertx;
	private int port;

	@Before
	public void setUp(TestContext context) throws IOException {
		vertx = Vertx.vertx();
		port = getAvailablePort();
		DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(new JsonObject().put("http.port", port));
		vertx.deployVerticle(MyFirstVerticle.class.getName(), deploymentOptions, context.asyncAssertSuccess());
	}

	private int getAvailablePort() throws IOException {
		ServerSocket socket = new ServerSocket(0);
		int port = socket.getLocalPort();
		socket.close();
		return port;
	}

	@After
	public void tearDown(TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}

	@Test
	public void testMyApplication(TestContext context) {
		final Async async = context.async();

		vertx.createHttpClient().getNow(port, "localhost", "/",
				response ->
						response.handler(body -> {
							context.assertTrue(body.toString().contains("Hello"));
							async.complete();
						})
		);
	}

	@Test
	public void checkThatTheIndexPageIsServed(TestContext context) {
		Async async = context.async();
		vertx.createHttpClient().getNow(port, "localhost", "/assets/index.html", response -> {
			context.assertEquals(response.statusCode(), 200);
			context.assertEquals(response.headers().get("content-type"), "text/html");
			response.bodyHandler(body -> {
				context.assertTrue(body.toString().contains("<title>My Whisky Collection</title>"));
				async.complete();
			});
		});
	}

	@Test
	public void testGetWhiskies(TestContext context) {
		Async async = context.async();
		vertx.createHttpClient().getNow(port, "localhost", "/api/whiskies", response -> {
			context.assertEquals(response.statusCode(), 200);
			context.assertEquals(response.headers().get("content-type"), "application/json");
			response.bodyHandler(body -> {
				context.assertTrue(body.toString().equals("[ {\n" +
						"  \"id\" : 0,\n" +
						"  \"name\" : \"Bowmore 15 Years Laimrig\",\n" +
						"  \"origin\" : \"Scotland, Islay\"\n" +
						"}, {\n" +
						"  \"id\" : 1,\n" +
						"  \"name\" : \"Talisker 57° North\",\n" +
						"  \"origin\" : \"Scotland, Island\"\n" +
						"} ]"));
				async.complete();
			});
		});
	}
}