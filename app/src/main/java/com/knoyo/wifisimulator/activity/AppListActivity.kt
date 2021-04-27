package com.knoyo.wifisimulator.activity

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.widget.*
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.gson.GsonBuilder
import com.knoyo.wifisimulator.R
import com.knoyo.wifisimulator.bean.AppInfo
import com.knoyo.wifisimulator.preferences.WifiInfoPrefs
import com.knoyo.wifisimulator.util.AppsUtil
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_app_list.*
import kotlinx.android.synthetic.main.layout_app_item.*


/**
 * @Title: AppListActivity类
 * @Package: com.knoyo.wifisimulator.activity
 * @Description: 应用列表页面
 * @author XueLong xuelongqy@foxmail.com
 * @date 2018/7/6 12:15
 * @update_author
 * @update_time
 * @version V1.0
 * @exception
 */
class AppListActivity : AppCompatActivity() {

    // WIFI信息配置文件
    private lateinit var wifiInfoPrefs: WifiInfoPrefs

    // 应用信息工具
    private val appUtil = AppsUtil(this)

    // 所有应用信息
    private var appsInfoList: MutableList<AppInfo> = mutableListOf()
    private var appsInfoListFilter: MutableList<AppInfo> = mutableListOf()

    // Gson
    private val gson = GsonBuilder().create()

    var mAdapter: AppAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list)
        // 初始化
        init()
    }

    /**
     * @Title: init方法
     * @Class: AppListActivity
     * @Description: 初始化方法
     * @author XueLong xuelongqy@foxmail.com
     * @date 2018/7/6 12:18
     * @update_author
     * @update_time
     * @version V1.0
     * @param
     * @return
     * @throws
     */
    private fun init() {
        // 初始化WIFI信息配置
        wifiInfoPrefs = WifiInfoPrefs(this)
        // 获取所有应用信息
        showLoading()
        appUtil.getAllAppsInfo() {
            hideLoading()
            // 同步模拟应用
            appsInfoList.clear()
            appsInfoList.addAll(it)
            syncSimulationApps()

            appsInfoListFilter = appsInfoList
            // 初始化应用列表适配器
            initAdapter()

        }

    }

    private fun initAdapter() {
        Log.i("TAG", "initAdapter: size=${appsInfoListFilter.size}")
        mAdapter = AppAdapter(appsInfoListFilter)
        mAdapter?.setOnItemChildClickListener { adapter, view, position ->

            var item = mAdapter?.getItem(position)
            item?.let {
                val isChecked = !item.isSimulation
                item.isSimulation = isChecked
                appsInfoList.find {
                    it.packageName == item.packageName
                }.apply {
                    this?.isSimulation = isChecked
                }

                updateSimulationApps()
                mAdapter?.notifyDataSetChanged()
            }

        }
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.adapter = mAdapter

    }

    /**
     * @Title: syncSimulationApps方法
     * @Class: AppListActivity
     * @Description: 同步模拟App
     * @author XueLong xuelongqy@foxmail.com
     * @date 2018/7/6 13:00
     * @update_author
     * @update_time
     * @version V1.0
     * @param
     * @return
     * @throws
     */
    private fun syncSimulationApps() {
        // 获取配置中应用列表
        val simulationAppList = gson.fromJson<ArrayList<String>>(wifiInfoPrefs.apps, object : TypeToken<ArrayList<String>>() {}.type)
        // 判断是否为空
        simulationAppList ?: return
        // 设置模拟应用状态
        simulationAppList.forEach { packageName ->
            appsInfoList.find {
                it.packageName == packageName
            }.apply {
                this?.isSimulation = true
            }
        }

        //排序置顶
        appsInfoList.sortByDescending { it.isSimulation }

    }

    private fun showLoading() {
        tvLoading?.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        tvLoading?.visibility = View.GONE
    }

    /**
     * @Title: updateSimulationApps方法
     * @Class: AppListActivity
     * @Description: 更新模拟App
     * @author XueLong xuelongqy@foxmail.com
     * @date 2018/7/6 13:01
     * @update_author
     * @update_time
     * @version V1.0
     * @param
     * @return
     * @throws
     */
    fun updateSimulationApps() {
        // 缓存模拟应用列表
        val simulationAppList = arrayListOf<String>()

        appsInfoList.filter {
            it.isSimulation
        }.apply {
            this.let {
                it.forEach {
                    simulationAppList.add(it.packageName)
                }
            }
        }
        // 保存模拟应用列表到配置
        wifiInfoPrefs.apps = gson.toJson(simulationAppList)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.getItemId()) {
            R.id.menu_search -> {
                showSearchDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSearchDialog() {
        Log.d("TAG", "search: ")
        val editText = EditText(this)
        AlertDialog.Builder(this)
                .setTitle("输入关键词")
                .setView(editText)
                .setPositiveButton("确定", object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        search(editText.text.toString())
                    }

                }).show()

    }

    private fun search(name: String) {

        var resultList = mutableListOf<AppInfo>()
        appsInfoList.filter {
            it.name.contains(name)
        }?.apply {
            resultList.addAll(this)
        }

        appsInfoListFilter = resultList
        mAdapter?.replaceData(appsInfoListFilter)
    }


    inner class AppAdapter(list: MutableList<AppInfo>) : BaseQuickAdapter<AppInfo, BaseViewHolder>(R.layout.layout_app_item, list) {
        override fun convert(helper: BaseViewHolder, item: AppInfo) {
            helper.setText(R.id.lai_app_name, item.name)
            helper.setText(R.id.lai_package_name, item.packageName)
            helper.setImageDrawable(R.id.lai_app_icon, item.icon)
            helper.setChecked(R.id.lai_app_check, item.isSimulation)


            helper.addOnClickListener(R.id.llRoot)
        }

    }

}
