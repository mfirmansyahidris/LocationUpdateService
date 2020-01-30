package com.telkomsel.newlocationupdateservice.utils

import android.content.Context
import android.location.Location
import android.preference.PreferenceManager
import com.telkomsel.newlocationupdateservice.R
import java.text.DateFormat
import java.util.*

/**
 ****************************************
created by -fi-
.::manca.fi@gmail.com ::.

29/01/2020, 11:49 AM
 ****************************************
 */

class Utils{
    val tag = "LocationUpdate_tag"
    val keyRequestingLocationUpdates = "requestingLocationUpdate"

    /*
    *return the {@code location} object as a human readable string.
    *@param location the {@link location}
    * */
    fun getLocationText(location: Location?): String{
        return if (location == null)
            "Unknown location"
        else
            "(" + location.latitude + ", " + location.longitude + ")"
    }

    fun getLocationTitle(context: Context): String {
        return context.getString(R.string.location_updated, DateFormat.getDateTimeInstance().format(Date()))
    }

    /*
    *stores the location update state in sharePreferences
    *@param requestingLocationUpdates the location updates state
    * */

    fun setRequestingLocationUpdates(context: Context, requestingLocationUpdates: Boolean){
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(keyRequestingLocationUpdates, requestingLocationUpdates)
            .apply()
    }

    /*
    * Returns true if requesting location updates, otherwise returns false.
    * @param context The {@link Context}.
    * */

    fun requestingLocationUpdates(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(keyRequestingLocationUpdates, false)
    }
}