package com.eyewind.sdk.demo.v2

import android.app.Activity
import androidx.annotation.Size
import com.ew.sdk.AdListener
import com.ew.sdk.SDKAgent
import com.ew.sdk.ads.model.AdBase
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.Type

object SDKTools {

    private const val COMPLETE_LEVEL_NUM_PARAMETER = "interstitial_time_line_v2"
    private const val INTERSTITIAL_AD_TIME = "interstitial_ad"
    private const val NO_AD_VERSION_NAME = "no_ad_version_name"
    private const val CAN_SHOW_BANNER = "banner_ad"

    private lateinit var sdkApplicationImp : SDKApplicationImp
    private var isFirstStart = true

    private var listenerList = mutableListOf<SDKListener>()

    fun initApplication(sdkApplicationImp : SDKApplicationImp){
        this.sdkApplicationImp = sdkApplicationImp
        SDKAgent.setTransparentNavBar(true)
        SDKAgent.setHomeShowInterstitial(false)
        SDKAgent.setUnityZoneId("rewardedVideo")
        SDKAgent.setVersionCheckEnable(false)//关闭版本检测
        SDKAgent.setAdListener(object :AdListener(){

            private var isRewarded = false

            override fun onAdNoFound(p0: AdBase?) = Unit

            override fun onAdError(p0: AdBase?, p1: String?, p2: java.lang.Exception?) = Unit

            override fun onAdShow(p0: AdBase?) {
                super.onAdShow(p0)
                listenerList.downForeach {
                    it.onAdShow(p0)
                }
            }

            override fun onAdClosed(p0: AdBase?) {
                super.onAdClosed(p0)
                listenerList.downForeach {
                    it.onAdClose(p0,isRewarded)
                }
                isRewarded = false
            }

            override fun onAdClicked(p0: AdBase?) {
                super.onAdClicked(p0)
                listenerList.downForeach {
                    it.onAdClick(p0)
                }
            }

            override fun onRewarded(p0: AdBase?) {
                super.onRewarded(p0)
                isRewarded = true
            }

            override fun onAdLoadSucceeded(p0: AdBase?) {

            }
        })
    }


    fun getSdkInstance(sdkActivityImp:SDKActivityImp,
                       isShowInterstitialOnResume:Boolean=true,
                       isShowBanner:Boolean=true,
                       isInitSDK:Boolean=true):SDKInstance{
        return SDKInstance(sdkActivityImp,isShowInterstitialOnResume,isShowBanner,isInitSDK)
    }

    class SDKInstance(val sdkActivityImp : SDKActivityImp,
                      var isShowInterstitialOnResume:Boolean=true,
                      var isShowBanner:Boolean=true,
                      var isInitSDK:Boolean=true){

        companion object{
            private var showInterstitialTime: Long = 0L
        }

        fun onCreate(activity: Activity){
            if(!isInitSDK) return
            if(isFirstStart){
                isFirstStart = false
                SDKAgent.autoShowPolicy(false)//设置不自动显示协议
                SDKAgent.setPolicyResult(true)
                SDKAgent.onLoadAds(activity)
            }
            SDKAgent.onCreate(activity)
        }

        fun onPause(activity: Activity){
            if(!isInitSDK) return
            SDKAgent.onPause(activity)
        }

        fun onResume(activity: Activity,isHomeBack:Boolean,isHome:Boolean){
            if(!isInitSDK) return
            SDKAgent.onResume(activity)
            if(isShowInterstitialOnResume){
                showInterstitial(activity,isHomeBack,isHome)
            }
        }

        fun onDestroy(activity: Activity){
            if(!isInitSDK) return
            SDKAgent.onDestroy(activity)
        }


        private var isShowInterstitial = false
        private var isSkipVideo = false
        /**
         * 在各种条件都满足的情况下，显示插屏广告
         */
        fun showInterstitial(activity: Activity,isHomeBack:Boolean,isHome:Boolean,function:()->Unit={}):Boolean{
            if(!canShowAd()) return false
            if(isHomeBack) return false
            if(isSkipVideo){
                isSkipVideo = false
                return false
            }
            //关卡数检测
            val completeNum = sdkApplicationImp.getCompleteLevel()
            val completeNumOnLine = Parameter.getIntParameter(COMPLETE_LEVEL_NUM_PARAMETER,10)
            if(completeNum<completeNumOnLine){
                return false
            }
            val interstitialTime = Parameter.getIntParameter(INTERSTITIAL_AD_TIME,60)*1000
            val nowTime = System.currentTimeMillis()
            if(showInterstitialTime==0L||nowTime- showInterstitialTime>interstitialTime){
                val page = if(isHome) SDKAgent.PAGE_MAIN else  SDKAgent.PAGE_HOME
                if(SDKAgent.hasInterstitial(page)&&!isShowVideo&&!isShowInterstitial){
                    showInterstitialTime = System.currentTimeMillis()
                    addSDKListener(SDKAgent.TYPE_INTERSTITIAL,object : SDKListener(){
                        override fun onAdShow(adBase: AdBase?) {
                            if(adBase!=null){
                            }
                        }
                        override fun onAdClose(adBase: AdBase?,isRewarded:Boolean) {
                            resetInterstitialTime()
                            function()
                            sdkActivityImp.onInterstitialClose()
                        }
                    })
                    SDKAgent.showInterstitial(page)
                    isShowInterstitial = true
                    return true
                }else{
                    resetInterstitialTime()
                }
            }else {
                if(isShowInterstitial){
                    resetInterstitialTime()
                }
            }
            return false
        }

        /**
         * 直接显示插屏广告，如果有插屏广告(用于某些激励场景)
         */
        fun showInterstitial(function: () -> Unit):Boolean{
            if(SDKAgent.hasInterstitial(SDKAgent.PAGE_HOME)){
                SDKAgent.showInterstitial(SDKAgent.PAGE_HOME)
                addSDKListener(SDKAgent.TYPE_INTERSTITIAL,object : SDKListener(){
                    override fun onAdShow(adBase: AdBase?) {
                        adBase?.let {
                        }
                    }
                    override fun onAdClose(adBase: AdBase?,isRewarded:Boolean) {
                        resetInterstitialTime()
                        function()
                        sdkActivityImp.onInterstitialClose()
                    }
                })
                isShowInterstitial = true
                return true
            }
            return false
        }

        fun resetInterstitialTime(){
            showInterstitialTime = System.currentTimeMillis()
            isShowInterstitial = false
        }

        fun showBanner(activity: Activity):Boolean{
            if(!canShowAd()) return false
            if(!isShowBanner)return false
            val canShowBanner = Parameter.getIntParameter(CAN_SHOW_BANNER,0)==1
            if(canShowBanner){
                addSDKListener(SDKAgent.TYPE_BANNER,object : SDKListener(){
                    override fun onAdShow(adBase: AdBase?) {
                        if(adBase!=null){
                        }
                    }
                    override fun onAdClose(adBase: AdBase?,isRewarded:Boolean) {

                    }
                })
                SDKAgent.showBanner(activity)
                return true
            }
            return false
        }

        private var isShowVideo = false
        fun showVideo(function: (Boolean) -> Unit):Boolean{
            if(!isInitSDK)return false
            if(SDKAgent.hasVideo(SDKAgent.PAGE_HOME)){
                addSDKListener(SDKAgent.TYPE_VIDEO,object :SDKListener(){
                    override fun onAdShow(adBase: AdBase?) {
                        if(adBase!=null){
                        }
                    }
                    override fun onAdClose(adBase: AdBase?,isRewarded:Boolean) {
                        if(isRewarded){
                            if(Parameter.getIntParameter("interstitial_skip_video",0)==1){
                                isSkipVideo = true
                            }
                            resetInterstitialTime()
                        }
                        isShowVideo = false
                        sdkActivityImp.onVideoClose()
                        function(isRewarded)
                    }

                    override fun onAdClick(adBase: AdBase?) {
                    }
                })
                SDKAgent.showVideo(SDKAgent.PAGE_HOME)
                isShowVideo = true
                return true
            }
            return false
        }

        fun canShowAd():Boolean{
            if(!isInitSDK)return false
            val value = Parameter.getStringParameter(NO_AD_VERSION_NAME, "0")
            if(value!=sdkApplicationImp.getAppVersion()){
                if(!sdkApplicationImp.isRemoveAd()){
                    if(!sdkApplicationImp.isVip()){
                        if(sdkApplicationImp.canShowAd()){
                            if(!SDKAgent.getCheckCtrl()){
                                return true
                            }
                        }else{
                        }
                    }
                }
            }
            return false
        }

        fun hasVideo():Boolean{
            return SDKAgent.hasVideo(SDKAgent.PAGE_HOME)
        }

        fun hideBanner(activity: Activity){
            SDKAgent.hideBanner(activity)
        }
    }

    object Parameter{

        /**
         * 获取int参数
         */
        fun getIntParameter(key:String,def:Int=0):Int{
            val value = SDKAgent.getOnlineParam(key)
            if(value.isNullOrEmpty()){
                return def
            }
            return valueOf(value,def)
        }


        /**
         * 从min和max字段总随机一个数
         */
        fun getIntRandomParameter(key: String): Int {
            val mmInt = getIntSParameter(key, arrayOf("min", "max"))
            return getIntRandom(mmInt)
        }


        /**
         * 获取String参数
         */
        fun getStringParameter(key: String, def: String?): String? {
            val value = SDKAgent.getOnlineParam(key)
            return if (value == null || value == "") {
                def
            } else value
        }

        /**
         * 直接获取参数并且实例化对象
         */
        fun <T>fromParameter(key:String,aClass: Class<T>):T?{
            val value = getStringParameter(key,null) ?: return null
            return Gson().fromJson(value,aClass)
        }

        fun <T>fromParameter(key:String,type: Type):T?{
            val value = getStringParameter(key,null) ?: return null
            return Gson().fromJson(value,type)
        }

        inline fun <reified T> fromParameter(key: String):T?{
            val value = getStringParameter(key,null) ?: return null
            return Gson().fromJson(value)
        }


        /**
         * 获取多个int参数
         */
        private fun getIntSParameter(key: String, keySubs: Array<String>): IntArray {
            val value = SDKAgent.getOnlineParam(key)
            val valueSubs = IntArray(keySubs.size){0}
            return try {
                val jsonObject = JSONObject(value)
                for ((i, k) in keySubs.withIndex()) {
                    val valueSub = jsonObject.getInt(k)
                    valueSubs[i] = valueSub
                }
                valueSubs
            } catch (e: JSONException) {
                e.printStackTrace()
                valueSubs
            }
        }

        /**
         * 在一个最小值和最大值数组中随机一个数字
         */
        private fun getIntRandom(@Size(min=2,max=2) mmInt: IntArray): Int {
            return mmInt[0] + (Math.random() * (mmInt[1] - mmInt[0])).toInt()
        }

        private fun valueOf(value: String,def:Int=0): Int {
            return try {
                Integer.valueOf(value)
            } catch (e: Exception) {
                def
            }

        }

    }

    fun addSDKListener(tagTemp:String,li: SDKListener){
        val listener = object : SDKListener(){
            override fun onAdShow(adBase: AdBase?) {
                if(adBase!=null){
                    if(this.tag==tagTemp&&adBase.type==tagTemp){
                        li.onAdShow(adBase)
                    }
                }
            }

            override fun onAdClose(adBase: AdBase?,isRewarded:Boolean) {
                if(adBase!=null){
                    if(this.tag==tagTemp&&adBase.type==tagTemp){
                        li.onAdClose(adBase,isRewarded)
                        listenerList.remove(this)
                    }
                }
            }

            override fun onAdClick(adBase: AdBase?) {
                if(adBase!=null){
                    if(this.tag==tagTemp&&adBase.type==tagTemp){
                        li.onAdClick(adBase)
                    }
                }
            }
        }
        listener.tag = tagTemp
        listenerList.downForeach {
            if(it.tag==tagTemp){
                listenerList.remove(it)
            }
        }
        listenerList.add(listener)
    }

    interface SDKApplicationImp{

        fun canShowAd():Boolean{return true}

        /**
         * 已完成的关卡数(用于多少关前不显示插屏广告的在线参数控制)
         */
        fun getCompleteLevel():Int{return 0}

        /**
         * 是否购买了去广告
         */
        fun isRemoveAd():Boolean

        /**
         * 是否购买了VIP
         */
        fun isVip():Boolean

        /**
         * appVersion(用于指定版本不显示任何广告的在线参数控制)
         */
        fun getAppVersion():String

    }

    interface SDKActivityImp{

        fun onInterstitialClose()

        fun onVideoClose()

        fun onVideoLoaded()

        fun onShowVideoError()
    }

    abstract class SDKListener{

        var tag: String = ""

        abstract fun onAdShow(adBase: AdBase?)

        abstract fun onAdClose(adBase: AdBase?,isRewarded:Boolean)

        open fun onAdClick(adBase: AdBase?){}

    }

}