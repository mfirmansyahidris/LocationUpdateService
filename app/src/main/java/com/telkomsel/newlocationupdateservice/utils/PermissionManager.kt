package com.telkomsel.newlocationupdateservice.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.telkomsel.newlocationupdateservice.R

/**
 ****************************************
created by -fi-
.::manca.fi@gmail.com ::.

29/01/2020, 10:50 AM
 ****************************************
 */

class PermissionManager(private val context: Activity) {

    @TargetApi(Build.VERSION_CODES.P)
    private fun requestForegroundPermission(listener: (Boolean) -> Unit){
        Dexter.withActivity(context)
            .withPermissions(Manifest.permission.FOREGROUND_SERVICE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        listener(true)
                    }

                    if (report.isAnyPermissionPermanentlyDenied) {
                        showSettingsDialog(context.getString(R.string.msg_reqPermissionForeground))
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener {
                Toast.makeText(
                    context,
                    context.getString(R.string.msg_permissionError),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()
    }

    fun requestLocationPermission(listener: (Boolean) -> Unit) {
        Dexter.withActivity(context)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                            requestForegroundPermission { foregroundAllowed ->
                                if(foregroundAllowed){
                                    listener(true)
                                }else{
                                    listener(false)
                                }
                            }
                        }else{
                            listener(true)
                        }
                    }else{
                        listener(false)
                    }

                    if (report.isAnyPermissionPermanentlyDenied) {
                        showSettingsDialog(context.getString(R.string.msg_reqPermissionLocation))
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener {
                Toast.makeText(
                    context,
                    context.getString(R.string.msg_permissionError),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()
    }

    private fun showSettingsDialog(message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.msg_reqPermissionTitle))
        builder.setMessage(message)
        builder.setPositiveButton(context.getString(R.string.msg_reqPermissionActionSetting)) { dialog, _ ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton(context.getString(R.string.msg_reqPermissionActionCancel)) { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivityForResult(intent, 101)
    }
}