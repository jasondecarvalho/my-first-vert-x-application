package io.vertx.blog.first;

import java.util.LinkedHashMap;
import java.util.Map;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
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

	private void bindApiResources(Router router) {
		router.get("/api/whiskies").handler(this::getAll);
		router.route("/api/whiskies*").handler(BodyHandler.create());
		router.post("/api/whiskies").handler(this::addOne);
		router.get("/api/whiskies/:id").handler(this::getOne);
		router.put("/api/whiskies/:id").handler(this::updateOne);
		router.delete("/api/whiskies/:id").handler(this::deleteOne);
	}

	private void getAll(RoutingContext routingContext) {
		routingContext.response()
				.putHeader("content-type", "application/json")
				.end(Json.encodePrettily(products.values()));
	}

	private void addOne(RoutingContext routingContext) {
		final Whisky whisky = Json.decodeValue(routingContext.getBodyAsString(), Whisky.class);
		products.put(whisky.getId(), whisky);
		routingContext.response()
				.setStatusCode(201)
				.putHeader("content-type", "application/json")
				.end(Json.encodePrettily(whisky));
	}

	private void getOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			Integer idAsInteger = Integer.valueOf(id);
			if (products.containsKey(idAsInteger)) {
				routingContext.response()
						.putHeader("content-type", "application/json")
						.end(Json.encodePrettily(products.get(idAsInteger)));
			} else {
				routingContext.response().setStatusCode(404).end();
			}
		}
	}

	private void updateOne(RoutingContext routingContext) {
		final String id = routingContext.request().getParam("id");
		JsonObject json = routingContext.getBodyAsJson();
		if (id == null || json == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			final Integer idAsInteger = Integer.valueOf(id);
			Whisky whisky = products.get(idAsInteger);
			if (whisky == null) {
				routingContext.response().setStatusCode(404).end();
			} else {
				whisky.setName(json.getString("name"));
				whisky.setOrigin(json.getString("origin"));
				routingContext.response()
						.putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(whisky));
			}
		}
	}

	private void deleteOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			Integer idAsInteger = Integer.valueOf(id);
			products.remove(idAsInteger);
		}
		routingContext.response().setStatusCode(204).end();
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