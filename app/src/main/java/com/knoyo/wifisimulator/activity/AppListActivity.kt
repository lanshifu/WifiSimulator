package com.knoyo.wifisimulator.activity

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.*
import android.widget.*
import com.google.gson.GsonBuilder
import com.knoyo.wifisimulator.R
import com.knoyo.wifisimulator.bean.AppInfo
import com.knoyo.wifisimulator.preferences.WifiInfoPrefs
import com.knoyo.wifisimulator.util.AppsUtil
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_app_list.*


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
    private lateinit var appsInfoList: MutableList<AppInfo>
    private lateinit var appsInfoListFilter: MutableList<AppInfo>
    // 应用列表适配器
    private lateinit var appListAdapter: BaseAdapter
    // Gson
    private val gson = GsonBuilder().create()

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
        wifiInfoPrefs =  WifiInfoPrefs(this)
        // 获取所有应用信息
        appUtil.getAllAppsInfo() {
            // 同步模拟应用
            appsInfoList = it
            syncSimulationApps()

            appsInfoListFilter = it
            // 初始化应用列表适配器
            initAdapter()

        }

    }

    private fun initAdapter() {
        appListAdapter = object : BaseAdapter() {
            // 获取条目视图
            override fun getView(p0: Int, p1: View?, p2: ViewGroup): View? {
                // 判断列表是否为空
                if (appsInfoListFilter.size == 0) {
                    return null
                }
                // 缓存视图容器
                var convertView = p1
                // 缓存应用条目视图容器
                val appItemViewHolder: AppItemViewHolder
                // 设置(获取)视图加载器
                if (convertView == null) {
                    // 加载布局
                    val mInflater = LayoutInflater.from(this@AppListActivity)
                    convertView = mInflater.inflate(R.layout.layout_app_item, p2, false)
                    // 加载视图
                    appItemViewHolder = AppItemViewHolder()
                    appItemViewHolder.icon = convertView!!.findViewById(R.id.lai_app_icon)
                    appItemViewHolder.name = convertView.findViewById(R.id.lai_app_name)
                    appItemViewHolder.isSimulation = convertView.findViewById(R.id.lai_app_check)
                    convertView.tag = appItemViewHolder
                } else {
                    appItemViewHolder = convertView.tag as AppItemViewHolder
                }
                // 设置数据
                val appInfo = appsInfoListFilter.get(p0)
                appItemViewHolder.icon.setImageDrawable(appInfo.icon)
                appItemViewHolder.name.text = appInfo.name
                appItemViewHolder.isSimulation.isChecked = appInfo.isSimulation
                appItemViewHolder.isSimulation.setOnCheckedChangeListener { _, b ->
                    // 设置勾选事件
                    appInfo.isSimulation = b
                    // 更新模拟App配置
                    updateSimulationApps()
                }
                return convertView
            }

            // 获取应用条目信息
            override fun getItem(p0: Int): Any? {
                return when (appsInfoListFilter.size) {
                    0 -> null
                    else -> appsInfoListFilter.get(p0)
                }
            }

            // 获取条目ID号
            override fun getItemId(p0: Int): Long {
                return when (appsInfoListFilter.size) {
                    0 -> 0L
                    else -> p0.toLong()
                }
            }

            // 获取总数
            override fun getCount(): Int {
                return appsInfoListFilter.size
            }
        }
        // 设置适配器
        al_app_list.adapter = appListAdapter
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
        simulationAppList.forEach { name ->
            appsInfoList.find {
                it.name == name
            }.apply {
                this?.isSimulation = true
            }
        }
    }

    private fun showLoading(){

    }

    private fun hideLoading(){

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


        appsInfoList.find {
            it.isSimulation
        }.apply {
            this?.let {
                simulationAppList.add(it.name)
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

        when(item.getItemId()){
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
                .setPositiveButton("确定",object :DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        Toast.makeText(this@AppListActivity, "点击了${editText.text}", Toast.LENGTH_SHORT).show()
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
        appListAdapter.notifyDataSetChanged()
    }
}

/**
 * @Title: AppItemViewHolder类
 * @Package: com.knoyo.wifisimulator.activity
 * @Description: 应用条目视图容器
 * @author XueLong xuelongqy@foxmail.com
 * @date 2018/7/6 12:40
 * @update_author
 * @update_time
 * @version V1.0
 * @exception
*/
class AppItemViewHolder {
    lateinit var icon: ImageView
    lateinit var name: TextView
    lateinit var isSimulation: CheckBox
}