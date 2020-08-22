package eu.lubsen.rummikub.util

import eu.lubsen.rummikub.model.Move

sealed class Result<T> {
    abstract fun isSuccess() : Boolean
    abstract fun isFailure() : Boolean
    /**
     * Chain a next result to the current instance.
     * A chain propagates the first Failure it encounters, or the last Success
     */
    fun <U> chain(next: Result<U>) : Result<U> {
        return if (this.isFailure())
            Failure((this as Failure).message())
        else
            next
    }

    fun <R, A> chain(next: (A)->Result<R>, arg : A) : Result<R> {
        return if (this.isFailure())
            Failure((this as Failure).message())
        else
            next(arg)
    }
}

class Success<T> constructor(private val value : T) : Result<T>() {
    fun result() : T = value

    override fun isSuccess(): Boolean {
        return true
    }

    override fun isFailure(): Boolean {
        return false
    }
}

class Failure<T> constructor(private val reason : String) : Result<T>() {
    fun message() : String = reason

    override fun isSuccess(): Boolean {
        return false
    }

    override fun isFailure(): Boolean {
        return true
    }
}