package com.eyewind.sdk.demo.v2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity(),SDKTools.SDKActivityImp {

    private val sdkTools = SDKTools.getSdkInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sdkTools.onCreate(this)
    }

    fun onRewardClick(view:View){
        sdkTools.showVideo {

        }
    }

    fun onInterstitialClick(view:View){
        sdkTools.showInterstitial {

        }
    }

    override fun onPause() {
        super.onPause()
        sdkTools.onPause(this)
    }

    override fun onResume() {
        super.onResume()
        sdkTools.onResume(this,false,true)
    }

    override fun onDestroy() {
        super.onDestroy()
        sdkTools.onDestroy(this)
    }

    override fun onInterstitialClose() {
    }

    override fun onVideoClose() {
    }

    override fun onVideoLoaded() {
    }

    override fun onShowVideoError() {
    }
}
