package eu.lubsen.rummikub

import com.xenomachina.argparser.ArgParser
import eu.lubsen.rummikub.core.ServerVerticle
import io.vertx.core.Vertx

fun main(args : Array<String>) {
    ArgParser(args).parseInto(::CliArguments).run {
        Vertx.vertx().deployVerticle(ServerVerticle(ssl))
    }
}