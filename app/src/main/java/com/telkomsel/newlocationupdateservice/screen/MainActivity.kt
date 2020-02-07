package com.telkomsel.newlocationupdateservice.screen

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.telkomsel.newlocationupdateservice.R
import com.telkomsel.newlocationupdateservice.base.BaseActivity
import com.telkomsel.newlocationupdateservice.base.BaseActivityLocation
import com.telkomsel.newlocationupdateservice.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_info.*
import java.lang.Exception
import kotlin.math.abs
import kotlin.math.sign

class MainActivity : BaseActivity(), BaseActivityLocation, OnMapReadyCallback {
    private lateinit var mapFragment: SupportMapFragment
    private var mMap: GoogleMap? = null
    private var mK: Marker? = null
    private var onFocus = true

    private lateinit var sheetBehavior: BottomSheetBehavior<View>

    private var hasMarker = false

    override fun getLayoutResource(): Int = R.layout.activity_main

    override fun setListener(): BaseActivityLocation = this

    override fun mainCode() {
        sheetBehavior = BottomSheetBehavior.from<View>(bs_)

        fab_focus_info.setOnClickListener { fabInfoAction() }

        mapFragment = supportFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
    }

    private fun fabInfoAction() {
        if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else if (sheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun fabFocusLocation(){
        onFocus = true
    }

    override fun onLocationUpdate(location: Location) {
        addMarker(location, mMap)
    }

    override fun onResume() {
        super.onResume()
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0
    }

    private fun addMarker(lastLocation: Location, googleMap: GoogleMap?){
        if(hasMarker){
            if(onFocus) setCamera(mMap, lastLocation)
            animateMarker(lastLocation, mK)
        }else{
            mMap = googleMap

            val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)
            mK = mMap?.addMarker(MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(createBitmap()))
                .anchor(0.5F, 0.5F)
            )
            animateMarker(lastLocation, mK)
            setCamera(mMap, lastLocation)
            hasMarker = true
        }

    }

    private fun setCamera(p0: GoogleMap?, location: Location){
        val cu = CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 16F)
        p0?.animateCamera(cu)
    }

    @Suppress("DEPRECATION")
    private fun createBitmap(): Bitmap? {
        val height = 80
        val width = 80

        val bitmapDraw = resources.getDrawable(R.drawable.ic_location) as BitmapDrawable
        val b = bitmapDraw.bitmap
        return Bitmap.createScaledBitmap(b, width, height, false)
    }


    private fun animateMarker(destination: Location, marker: Marker?){
        marker?.let {
            val startPosition = marker.position
            val endPosition = LatLng(destination.latitude, destination.longitude)

            val startRotation = marker.rotation
            val latLngInterpolator = LatLngInterpolator.LinearFixed()
            val valueAnimator = ValueAnimator.ofFloat(0F, 1F)
            valueAnimator.apply {
                duration = 1000
                interpolator = LinearInterpolator()
            }
            valueAnimator.addUpdateListener {
                try {
                    val v = it.animatedFraction
                    val newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition)
                    marker.position = newPosition
                    marker.rotation = computeRotation(v, startRotation, destination.bearing)
                } catch (e: Exception){
                    Log.e(Utils().tag, "error get rotation: $e")
                }
            }
            valueAnimator.start()
        }
    }

    private interface LatLngInterpolator{
        fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng
        class LinearFixed : LatLngInterpolator {
            override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
                val lat = (b.latitude - a.latitude) * fraction + a.latitude
                var lngDelta = b.longitude - a.longitude
                if (abs(lngDelta) > 180) {
                    lngDelta -= sign(lngDelta) * 360
                }
                val lng = lngDelta * fraction + a.longitude
                return LatLng(lat, lng)
            }
        }
    }

    private fun computeRotation(fraction: Float, start: Float, end: Float): Float {
        val normalizeEnd = end - start
        val normalizeEndAbs = (normalizeEnd + 360) % 360

        val direction  = if(normalizeEndAbs > 180) - 1 else 1
        val rotation = if(direction > 0) normalizeEndAbs else normalizeEndAbs - 360

        val result = fraction * rotation + start
        return (result + 360) % 360
    }
}
