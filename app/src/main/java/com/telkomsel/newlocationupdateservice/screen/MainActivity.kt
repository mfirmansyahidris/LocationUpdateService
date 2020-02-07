package com.telkomsel.newlocationupdateservice.screen

import android.content.*
import android.location.Location
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.telkomsel.newlocationupdateservice.R
import com.telkomsel.newlocationupdateservice.base.BaseActivity
import com.telkomsel.newlocationupdateservice.base.BaseActivityLocation
import com.telkomsel.newlocationupdateservice.service.LocationUpdateService
import com.telkomsel.newlocationupdateservice.utils.PermissionManager
import com.telkomsel.newlocationupdateservice.utils.Utils

class MainActivity : BaseActivity(), BaseActivityLocation {

    override fun getLayoutResource(): Int = R.layout.activity_main

    override fun setListener(): BaseActivityLocation = this

    override fun mainCode() {
        Log.d(Utils().tag, "creating activity")
    }

    override fun onLocationUpdate(location: Location) {
        
    }

}
