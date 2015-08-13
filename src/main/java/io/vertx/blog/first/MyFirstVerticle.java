package io.vertx.blog.first;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class MyFirstVerticle extends AbstractVerticle {

	@Override
	public void start(Future<Void> future) {
		Router router = Router.router(vertx);
		bindHelloMessage(router);
		bindStaticResources(router);
		createServer(future, router);
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