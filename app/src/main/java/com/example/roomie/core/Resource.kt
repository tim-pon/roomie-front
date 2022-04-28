package com.example.roomie.core

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
data class Resource<out T>(val status: Status, val data: T?, val message: String?, val code: Int? = null) {

    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): Resource <T> {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T> apiError(code: Int, msg: String, data: T?): Resource <T> {
            return Resource(Status.ERROR, data, msg, code)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING;
}