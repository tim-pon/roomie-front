package com.example.roomie.core.util.network

import androidx.lifecycle.LiveData
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A Retrofit adapter that converts the Call into a LiveData of Response.
 * @param <R>
 */
class LiveDataCallAdapter<R>(private val responseType: Type) :
    CallAdapter<R, LiveData<Response<R>>> {

    override fun responseType() = responseType


    override fun adapt(call: Call<R>): LiveData<Response<R>> {
        return object : LiveData<Response<R>>() {

            private var started = AtomicBoolean(false)

            override fun onActive() {
                super.onActive()

                if (started.compareAndSet(false, true)) {
                    call.enqueue(object : Callback<R> {

                        override fun onResponse(call: Call<R>, response: Response<R>) {
                            postValue(response)
                        }

                        override fun onFailure(call: Call<R>, t: Throwable) {
                            postValue(Response.error(408, t.localizedMessage!!.toResponseBody()))

                        }
                    })
                }
            }
        }
    }
}