package com.telkomsel.newlocationupdateservice.base

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

/**
 ****************************************
created by -fi-
.::manca.fi@gmail.com ::.

29/01/2020, 09:57 AM
 ****************************************
 */

abstract class BaseActivity : AppCompatActivity() {
    protected var toolbar: Toolbar? = null

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResource())
        if (getToolbarResource() != 0) {
            toolbar = findViewById(getToolbarResource())
            toolbar?.title = getToolbarTitle()
            setSupportActionBar(toolbar)
            supportActionBar?.setDefaultDisplayHomeAsUpEnabled(setToolbarActionButton())
            supportActionBar?.setDisplayHomeAsUpEnabled(setToolbarActionButton())
        }
        mainCode()
    }

    protected abstract fun getLayoutResource(): Int
    protected abstract fun getToolbarResource(): Int
    protected abstract fun getToolbarTitle(): String
    protected abstract fun setToolbarActionButton(): Boolean
    protected abstract fun mainCode()
}