package eu.lubsen.rummikub.core

import com.sun.org.apache.xpath.internal.operations.Bool

enum class ResultType {
    SUCCESS,
    FAILURE
}

class MoveResult<T> constructor(private val type : ResultType) {
    var value : T? = null
    var message : String = ""

    fun isSuccess() : Boolean {
        return type == ResultType.SUCCESS
    }

    fun isFailure() : Boolean {
        return type == ResultType.FAILURE
    }

    fun unwrap() : T {
        return value!!
    }
}

fun <T> moveSuccess(t : T) : MoveResult<T> {
    val result = MoveResult<T>(ResultType.SUCCESS)
    result.value = t
    return result
}

fun <T> moveFailure(msg : String) {
    val result = MoveResult<T>(ResultType.FAILURE)
    result.message = msg
}