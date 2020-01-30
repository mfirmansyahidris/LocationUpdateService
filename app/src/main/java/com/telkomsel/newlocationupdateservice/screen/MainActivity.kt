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
import com.telkomsel.newlocationupdateservice.service.LocationUpdateService
import com.telkomsel.newlocationupdateservice.utils.PermissionManager
import com.telkomsel.newlocationupdateservice.utils.Utils

class MainActivity : BaseActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private lateinit var myReceive: MyReceiver

    // A reference to the service used to get location updates.
    private var mService: LocationUpdateService? = null

    // tracks the bound state of the service
    private var mBound = false

    override fun getLayoutResource(): Int = R.layout.activity_main

    override fun getToolbarResource(): Int = 0

    override fun getToolbarTitle(): String = ""

    override fun setToolbarActionButton(): Boolean = false

    // Monitors the state of the connection to the service.
    private val mServiceConnection = object : ServiceConnection{
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i(Utils().tag, "service is disconnected")
            mService = null
            mBound = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.i(Utils().tag, "service is connected")
            val binder = service as LocationUpdateService.LocalBinder
            mService = binder.service
            mBound = true
            
            runRequestLocation()
        }
    }

    override fun mainCode() {
        Log.d(Utils().tag, "creating activity")
        myReceive = MyReceiver()
    }

    override fun onStart() {
        super.onStart()

        Log.d(Utils().tag, "starting activity")

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(Intent(this, LocationUpdateService::class.java), mServiceConnection, Context.BIND_AUTO_CREATE)

        //to remove location service
        //mService?.removeLocationUpdate()
    }

    override fun onResume() {
        super.onResume()

        Log.d(Utils().tag, "resuming activity")

        LocalBroadcastManager.getInstance(this).registerReceiver(myReceive, IntentFilter(LocationUpdateService().actionBroadcast))
    }

    override fun onPause() {
        Log.d(Utils().tag, "pausing activity")
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceive)
        super.onPause()
    }

    override fun onStop() {
        Log.d(Utils().tag, "stopping activity")
        if (mBound){
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection)
            mBound = false
        }
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    inner class MyReceiver: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(Utils().tag, "receiving from BroadcastReceiver")
            val location = intent?.getParcelableExtra<Location>(LocationUpdateService().extraLocation)
            if(location != null){
                Toast.makeText(this@MainActivity, Utils().getLocationText(location), Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.i(Utils().tag, "share preference change")
        // Update the UI state depending on whether location updates are being requested.
        if(key.equals(Utils().keyRequestingLocationUpdates)){
            Log.i(Utils().tag, "${sharedPreferences?.getBoolean(Utils().keyRequestingLocationUpdates, false)}")
        }
    }

    private fun runRequestLocation(){
        PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
            .registerOnSharedPreferenceChangeListener(this@MainActivity)

        // Check that the user hasn't revoked permissions by going to Settings.
        PermissionManager(this@MainActivity).requestLocationPermission {
            if(it){
                mService?.requestLocationUpdates()
            }
        }
    }
}
