package com.pablodev.weatherapp.utils

import android.util.Log

class Logger private constructor(private val tag: String) {

    companion object {
        fun getInstance(clazz: Class<*>): Logger {
            return Logger(clazz.simpleName)
        }
    }

    fun debug(message: String) {
        Log.d(tag, message)
    }

    fun error(message: String) {
        Log.e(tag, message)
    }

    fun info(message: String) {
        Log.i(tag, message)
    }
}