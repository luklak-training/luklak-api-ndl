package com.example.luklak_api.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class ApiVerticle extends AbstractVerticle {

  public void start(Promise<Void> startPromise) throws Exception {
    super.start();

    vertx.createHttpServer().requestHandler(this::handleRequest).listen(8080, result -> {
      if (result.succeeded()) {
        System.out.println("Luklak api started on port 8080!");
        startPromise.complete();
      } else {
        System.err.println("Fail to start Luklak api!");
        startPromise.fail(result.cause());
      }
    });
  }


  private void handleRequest(HttpServerRequest request) {
    JsonObject requestBody = new JsonObject();

    request.bodyHandler(buffer -> {
      if (buffer.length() != 0) {
        requestBody.put("id", Json.encode(buffer.toJsonObject().getInteger("id")));
      }

      EventBus eventBus = vertx.eventBus();
      request.response()
        .putHeader("accept", "application/json")
        .putHeader("content-type", "application/json");;

      MultiMap headers = MultiMap.caseInsensitiveMultiMap()
          .add("endpoint", request.path())
            .add("method", request.method().name());

      eventBus.request("student", requestBody, new DeliveryOptions().setHeaders(headers)).toCompletionStage()
        .whenComplete((item, err) -> {
          if(err!=null){
            request.response().setStatusCode(500).end(err.getMessage());
          }
          request.response().setStatusCode(200).end(item.body().toString());
        });
    });
  }
}
