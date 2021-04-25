package com.knoyo.wifisimulator.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.knoyo.wifisimulator.bean.AppInfo
import kotlin.concurrent.thread


/**
 * @Title: AppsUtil类
 * @Package: com.knoyo.wifisimulator.util
 * @Description: App工具类,用于获取设备中所有的应用信息
 * @author XueLong xuelongqy@foxmail.com
 * @date 2018/7/6 11:36
 * @update_author
 * @update_time
 * @version V1.0
 * @exception
*/
class AppsUtil(val context: Context) {

    val mHandler = Handler(Looper.getMainLooper())

    /**
     * @Title: getAllAppsInfo方法
     * @Class: AppsUtil
     * @Description: 获取所有应用信息
     * @author XueLong xuelongqy@foxmail.com
     * @date 2018/7/6 11:37
     * @update_author
     * @update_time
     * @version V1.0
     * @param
     * @return
     * @throws
    */
    fun getAllAppsInfo(result: (list: MutableList<AppInfo>) -> Unit){

        if (mAppList.isNotEmpty()) {
            result.invoke(mAppList)
            return
        }
        thread {
            // 获取App列表
            val packages = context.packageManager.getInstalledPackages(0)
            mHandler.post {
                packages.forEach {
                    val appInfo = AppInfo(
                            it.applicationInfo.loadIcon(context.packageManager),
                            it.applicationInfo.loadLabel(context.packageManager).toString(),
                            false,
                            it.applicationInfo.packageName,
                    )
                    mAppList.add(appInfo)
                }
                result.invoke(mAppList)

            }
        }

    }


    companion object {
        val mAppList = mutableListOf<AppInfo>()
    }

}