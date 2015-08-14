package io.vertx.blog.first;

import java.util.List;

import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.mongo.MongoClient;
import io.vertx.rxjava.ext.web.Route;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import rx.Observable;
import rx.functions.Action1;

public class MyFirstVerticle extends AbstractVerticle {

	public static final String WHISKIES_COLLECTION = "whiskies";
	private MongoClient mongoClient;

	@Override
	public void start(Future<Void> future) throws Exception {
		mongoClient = MongoClient.createShared(vertx, config());
		Router router = Router.router(vertx);
		bindHelloMessage(router);
		bindStaticResources(router);
		bindApiResources(router);
		createServer(future, router);
	}

	private void bindHelloMessage(Router router) {
		router.route("/").handler(routingContext ->
						routingContext.response()
								.putHeader("content-type", "text/html")
								.end("<h1>Hello from my first Vert.x 3 application</h1>")
		);
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
		Observable<List<JsonObject>> getWhiskies = mongoClient.findObservable(WHISKIES_COLLECTION, new JsonObject());
		getWhiskies.subscribe((whiskies) -> {
			whiskies.forEach((whisky) -> {
				unmarshallId(whisky);
			});
			routingContext.response()
					.putHeader("content-type", "application/json")
					.end(Json.encodePrettily(whiskies));
		});
	}

	private void unmarshallId(JsonObject whisky) {
		whisky.put("id", whisky.getValue("_id"));
		whisky.remove("_id");
	}

	private void addOne(RoutingContext routingContext) {
		final Whisky whisky = Json.decodeValue(routingContext.getBodyAsString(), Whisky.class);
		Observable<String> updateObservable = mongoClient.saveObservable(WHISKIES_COLLECTION, whisky.asJsonObject());

		updateObservable.subscribe(
				(success) -> {
					whisky.setId(success);
					routingContext.response()
							.setStatusCode(201)
							.putHeader("content-type", "application/json")
							.end(Json.encodePrettily(whisky));
				},
				handleThrowable(routingContext));
	}

	private JsonObject queryForId(String id) {
		return new JsonObject().put("_id", id);
	}

	private void getOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			Observable<JsonObject> findOne =
					mongoClient.findOneObservable(WHISKIES_COLLECTION, queryForId(id), new JsonObject());

			findOne.subscribe(
					(whisky) -> {
						if (whisky != null) {
							unmarshallId(whisky);
							routingContext.response()
									.putHeader("content-type", "application/json")
									.end(Json.encodePrettily(whisky));
						} else {
							routingContext.response().setStatusCode(404).end();
						}
					},
					handleThrowable(routingContext));
		}
	}

	private void updateOne(RoutingContext routingContext) {
		final String id = routingContext.request().getParam("id");
		JsonObject json = routingContext.getBodyAsJson();
		if (id == null || json == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			Observable<JsonObject> find =
					mongoClient.findOneObservable(WHISKIES_COLLECTION, queryForId(id), new JsonObject());

			Observable<Void> replace = find.flatMap(
					(whisky) -> {
						if (whisky == null) {
							routingContext.response().setStatusCode(404).end();
							return Observable.empty();
						} else {
							return mongoClient.replaceObservable(WHISKIES_COLLECTION, queryForId(id), json);
						}
					}
			).doOnError(handleThrowable(routingContext));

			replace.subscribe(
					(whisky) ->
							routingContext.response()
									.putHeader("content-type", "application/json")
									.end(Json.encodePrettily(json)),
					handleThrowable(routingContext)
			);
		}
	}

	private Action1<Throwable> handleThrowable(RoutingContext routingContext) {
		return (throwable) -> {
			throwable.printStackTrace();
			routingContext.response()
					.setStatusCode(500)
					.end(throwable.toString());
		};
	}

	private void deleteOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			mongoClient.remove(WHISKIES_COLLECTION, queryForId(id),
					(v) -> routingContext.response().setStatusCode(204).end());
		}
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