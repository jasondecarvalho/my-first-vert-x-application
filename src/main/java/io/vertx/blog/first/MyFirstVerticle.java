package io.vertx.blog.first;

import java.util.LinkedHashMap;
import java.util.Map;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

public class MyFirstVerticle extends AbstractVerticle {

	private Map<Integer, Whisky> products = new LinkedHashMap<>();

	@Override
	public void start(Future<Void> future) {
		createSomeData();
		Router router = Router.router(vertx);
		bindHelloMessage(router);
		bindStaticResources(router);
		bindApiResources(router);
		createServer(future, router);
	}

	private void createSomeData() {
		Whisky bowmore = new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay");
		products.put(bowmore.getId(), bowmore);
		Whisky talisker = new Whisky("Talisker 57° North", "Scotland, Island");
		products.put(talisker.getId(), talisker);
	}

	private void bindHelloMessage(Router router) {
		router.route("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response
					.putHeader("content-type", "text/html")
					.end("<h1>Hello from my first Vert.x 3 application</h1>");
		});
	}

	private Route bindStaticResources(Router router) {
		return router.route("/assets/*").handler(StaticHandler.create("assets"));
	}

	private Route bindApiResources(Router router) {
		return router.get("/api/whiskies").handler(this::getAll);
	}

	private void getAll(RoutingContext routingContext) {
		routingContext.response()
				.putHeader("content-type", "application/json")
				.end(Json.encodePrettily(products.values()));
	}

	private void createServer(Future<Void> future, Router router) {
		vertx
				.createHttpServer()
				.requestHandler(router::accept)
				.listen(
						config().getInteger("http.port", 8080),
						result -> {
							if (result.succeeded()) {
								future.complete();
							} else {
								future.fail(result.cause());
							}
						}
				);
	}
}