package eu.lubsen.rummikub

import eu.lubsen.rummikub.core.ServerVerticle
import io.vertx.core.Vertx

fun main(args : Array<String>) {
    Vertx.vertx().deployVerticle(ServerVerticle())
}