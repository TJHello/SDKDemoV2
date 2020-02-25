package com.eyewind.sdk.demo.v2

import android.content.pm.PackageInfo
import com.ew.sdk.BaseApplication

/**
 * 创建者：TJbaobao
 * 时间:2020/2/25 11:11
 * 使用:
 * 说明:
 **/
class TJApplication : BaseApplication() ,SDKTools.SDKApplicationImp{

    override fun onCreate() {
        super.onCreate()
        SDKTools.initApplication(this)
    }

    override fun isRemoveAd(): Boolean {
        return false
    }

    override fun isVip(): Boolean {
        return false
    }

    override fun getAppVersion(): String {
        val packageInfo: PackageInfo = this.packageManager
            .getPackageInfo(this.packageName, 0)
        return packageInfo.versionName
    }

}