package com.mkstudio.r2048.util

import android.util.Log

private class CustomLogger {
    val MYTAG = "mk.devs"

    fun debug(msg:String) {
        Log.d(MYTAG, msg)
    }
    fun info(msg:String) {
        Log.i(MYTAG, msg)
    }
    fun error(msg:String) {
        Log.e(MYTAG, msg)
    }
    fun warning(msg:String) {
        Log.w(MYTAG, msg)
    }
}

private val logger = CustomLogger()

fun myLogD(msg:String) {
    logger.debug(msg)
}
fun myLogI(msg:String) {
    logger.info(msg)
}
fun myLogE(msg:String) {
    logger.error(msg)
}
fun myLogW(msg:String) {
    logger.warning(msg)
}