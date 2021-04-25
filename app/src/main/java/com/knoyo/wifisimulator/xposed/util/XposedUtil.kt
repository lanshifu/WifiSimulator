package com.knoyo.wifisimulator.xposed.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle

/**
 * @Title: XposedUtil类
 * @Package: com.knoyo.wifisimulator.xposed.util
 * @Description: Sposed工具类
 * @author XueLong xuelongqy@foxmail.com
 * @date 2018/5/4 13:34
 * @update_author
 * @update_time
 * @version V1.0
 * @exception
*/
object XposedUtil {
    /**
     * @Title: isXposedInstalled方法
     * @Class: XposedUtil
     * @Description: 检验Xpose是否安装
     * @author XueLong xuelongqy@foxmail.com
     * @date 2018/5/4 13:11
     * @update_author
     * @update_time
     * @version V1.0
     * @param Xposed是否安装
     * @return
     * @throws
    */
    fun isXposedInstalled(): Boolean {
        val stack = Thread.currentThread().stackTrace
        for (i in stack.size - 3 until stack.size) {
            if (stack[i].toString().contains("de.robv.android.xposed.XposedBridge"))
                return true
        }
        return false
    }

    /**
     * @Title: isXposedActive方法
     * @Class: XposedUtil
     * @Description: 判断xposed是否激活(注: 激活后会使用hook返回true)
     * @author XueLong xuelongqy@foxmail.com
     * @date 2018/5/4 13:10
     * @update_author
     * @update_time
     * @version V1.0
     * @param Xposed是否激活
     * @return
     * @throws
    */
    fun isXposedActive(): Boolean {
        return false
    }

    //查询用户是否勾选了你这么模块(太极中)
    fun isExpModuleActive(context: Context?): Boolean {
        var isExp = false
        requireNotNull(context) { "context must not be null!!" }
        try {
            val contentResolver = context.contentResolver
            val uri: Uri = Uri.parse("content://me.weishu.exposed.CP/")
            var result: Bundle? = null
            try {
                result = contentResolver.call(uri, "active", null, null)
            } catch (e: RuntimeException) {
                // TaiChi is killed, try invoke
                try {
                    val intent = Intent("me.weishu.exp.ACTION_ACTIVE")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e1: Throwable) {
                    return false
                }
            }
            if (result == null) {
                result = contentResolver.call(uri, "active", null, null)
            }
            if (result == null) {
                return false
            }
            isExp = result.getBoolean("active", false)
        } catch (ignored: Throwable) {
        }
        return isExp
    }
}