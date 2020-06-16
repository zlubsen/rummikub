package eu.lubsen.rummikub.core

sealed class Result

class Success<T> constructor(private val value : T) : Result() {
    fun result() : T = value
}

class Failure constructor(private val reason : String) : Result() {
    fun message() : String = reason
}