package com.telkomsel.newlocationupdateservice.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.telkomsel.newlocationupdateservice.BuildConfig
import com.telkomsel.newlocationupdateservice.R
import com.telkomsel.newlocationupdateservice.screen.MainActivity
import com.telkomsel.newlocationupdateservice.utils.Utils
import okhttp3.internal.Util

/**
 ****************************************
created by -fi-
.::manca.fi@gmail.com ::.

29/01/2020, 11:05 AM
 ****************************************
 */

class LocationUpdateService: Service() {
    private val tag = LocationUpdateService::class.java.simpleName
    private lateinit var mFusedLocationUpdate: FusedLocationProviderClient
    private lateinit var mLocationCallBack: LocationCallback
    private lateinit var mLocation: Location

    val actionBroadcast = "${BuildConfig.APPLICATION_ID}.broadcast"
    val extraLocation = "${BuildConfig.APPLICATION_ID}.location"
    private val extraStartFromNotification = "${BuildConfig.APPLICATION_ID}.startedFromNotification"

    //the name of the channel for notification
    private val channelId = "channel.${BuildConfig.APPLICATION_ID}"

    private lateinit var mNotificationManager: NotificationManager

    //the identifier for the notification displayed for the foreground service.
    private val notificationId = 12345678 //todo: change with application id in integer

    private lateinit var mLocationRequest: LocationRequest

    //the desired interval for notification updates, inexact, updates may be more less frequent
    private val updateIntervalInMilliseconds: Long = 3000

    //the fastest rate for active location update, updates will never be more frequent than this value
    private val fastestUpdateIntervalInMilliseconds = updateIntervalInMilliseconds / 2

    //provides access to the fused location provider api
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    /*
    *used to check whether to bound activity has really gone away and not unbound as part of an
    * orientation change. we create a foreground service notification only if the former takes
    *place
    * */

    private lateinit var mServiceHandler: Handler


    private var mChangingConfiguration = false

    private val mBinder = LocalBinder()

    override fun onCreate() {
        super.onCreate()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationCallBack = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult!!.lastLocation)
            }
        }

        createLocationRequest()
        getLastLocation()

        val handlerThread = HandlerThread(tag)
        handlerThread.start()

        mServiceHandler = Handler(handlerThread.looper)
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        //android O requires a Notification Channel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = getString(R.string.app_name)

            //create the channel for the notification
            val mChannel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT)

            //set the notification channel for the notification manager
            mNotificationManager.createNotificationChannel(mChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(tag, "service started")

        val startedFromNotification = intent?.getBooleanExtra(extraStartFromNotification, false)

        //we got here because the user decided to remove location updates from the notification
        if(startedFromNotification!!){
            removeLocationUpdate()
            stopSelf()
        }

        //tell the system to not try to recreate the service after it has been killed
        return START_NOT_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        mChangingConfiguration = true
    }

    override fun onBind(intent: Intent?): IBinder? {
        /*
        *called when a client (MainActivity in case of this sample) comes to the foreground
        *and binds with this service. the service should cease to be a foreground service
        * when that happens
        * */

        Log.i(tag, "in onBind()")
        stopForeground(true)
        mChangingConfiguration = false
        return mBinder
    }

    override fun onRebind(intent: Intent?) {
        /*
        * Called when a client (MainActivity in case of this sample) returns to the foreground
        * and binds once again with this service. The service should cease to be a foreground
        * service when that happens.
        * */

        Log.i(tag, "in onRebind()")
        stopForeground(true)
        mChangingConfiguration = false

        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(tag, "last client unbound from service")

        /*
        * called when the last client (MainActivity in case of this sample) unbinds from this
        * service. If this method is called due to a configuration change in MainActivity, we
        * do nothing. Otherwise, we make this service a foreground service.
        * */

        if(!mChangingConfiguration && Utils().requestingLocationUpdates(this)){
            Log.i(tag, "starting foreground service")
            startForeground(notificationId, getNotification())
        }

        return true // ensures onRebind() is called when a client re-bind
    }

    override fun onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null)
    }

    /*
    * Makes a request for location updates. Note that in this sample we merely log the
    * {@link SecurityException}.
    * */
    fun requestLocationUpdates(){
        Log.i(tag, "requesting location updates")
        Utils().setRequestingLocationUpdates(this, true)
        startService(Intent(applicationContext, LocationUpdateService::class.java))
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper())
        }catch (unlikely: SecurityException){
            Utils().setRequestingLocationUpdates(this, false)
            Log.e(tag, "lost location permission. could not request location updates: $unlikely")
        }
    }

    private fun onNewLocation(location: Location){
        Log.i(tag, "new location: $location")

        mLocation = location

        //Notify anyone listening for broadcast about the new location
        val intent = Intent(actionBroadcast)
        intent.putExtra(extraLocation, location)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

        //update notification content if running as a foreground service
        if(serviceIsRunningInForeground(this)){
            mNotificationManager.notify(notificationId, getNotification())
        }

    }

    @Suppress("DEPRECATION")
    private fun serviceIsRunningInForeground(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)){
            if (javaClass.name == service.service.className) {
                if (service.foreground) {
                    return true
                }
            }
        }
        return false
    }

    @Suppress("DEPRECATION")
    private fun getNotification(): Notification {
        val intent = Intent(this, LocationUpdateService::class.java)
        val text = Utils().getLocationText(mLocation)
        intent.putExtra(extraStartFromNotification, true)

        val serviceServiceIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val activityPendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)

        val builder = NotificationCompat.Builder(this)
            .addAction(R.drawable.ic_launch, getString(R.string.launch_activity), activityPendingIntent)
            .addAction(R.drawable.ic_cancel, getString(R.string.remove_location_updates), serviceServiceIntent)
            .setContentText(text)
            .setContentTitle(Utils().getLocationTitle(this))
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_HIGH)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker(text)
            .setWhen(System.currentTimeMillis())

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            builder.setChannelId(channelId)
        }

        return builder.build()
    }

    /*
    * Sets the location parameters
    * */

    private fun createLocationRequest(){
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = updateIntervalInMilliseconds
        mLocationRequest.fastestInterval = fastestUpdateIntervalInMilliseconds
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun getLastLocation(){
        try{
            mFusedLocationClient.lastLocation
                .addOnCompleteListener { task ->
                    if(task.isSuccessful && task.result != null){
                        mLocation = task.result!!
                    }
                }
        }catch (unlikely: SecurityException){
            Log.e(tag, "lost location permission: $unlikely")
        }
    }

    fun removeLocationUpdate(){
        Log.i(tag, "removing location update")
        try{
            mFusedLocationClient.removeLocationUpdates(mLocationCallBack)
            Utils().setRequestingLocationUpdates(this, false)
            stopSelf()
        }catch (unlikely: SecurityException){
            Utils().setRequestingLocationUpdates(this, true)
            Log.e(tag, "lost location permission. could not remove location update: $unlikely")
        }
    }

    /*
    *class used for the client binder. since this service runs in the same process as its
    *clients, we don't need to deal with IPC
    * */
    inner class LocalBinder : Binder() {
        internal val service: LocationUpdateService
            get() = this@LocationUpdateService
    }

}