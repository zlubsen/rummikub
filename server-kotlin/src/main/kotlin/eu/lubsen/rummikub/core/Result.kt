package eu.lubsen.rummikub.core

sealed class Result<T>

class Success<T> constructor(private val value : T) : Result<T>() {
    fun result() : T = value
}

class Failure<T> constructor(private val reason : String) : Result<T>() {
    fun message() : String = reason
}