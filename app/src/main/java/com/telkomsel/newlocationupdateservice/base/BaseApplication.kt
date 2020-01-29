package com.telkomsel.newlocationupdateservice.base

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy

/**
 ****************************************
created by -fi-
.::manca.fi@gmail.com ::.

29/01/2020, 09:57 AM
 ****************************************
 */

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val formatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(false)
            .methodCount(2)
            .methodOffset(5)
            .tag("com.telkomsel.tdr")
            .build()
        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))
    }
}