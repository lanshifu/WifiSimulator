package com.knoyo.wifisimulator.activity

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.knoyo.wifisimulator.R
import kotlinx.android.synthetic.main.activity_connected_wifi_list.*
import kotlin.concurrent.thread


/**
 * @author lanxiaobin
 * @date 4/10/21
 */
class ConnectedWifiListActivity : AppCompatActivity() {

    @Volatile
    var list = mutableListOf<WifiInfo>()
    var myAdapter = MyAdapter(list)
    var mHandler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connected_wifi_list)

        setTitle("选择wifi")

        initView()

        initData()
    }

    private fun initData() {


//        thread {
//            list = WifiPasswordUtil.readWifiPassworld() ?: mutableListOf()
//            mHandler.post {
//
//                myAdapter = MyAdapter(list)
//                mRecyclerView.layoutManager = LinearLayoutManager(this)
//                mRecyclerView.adapter = myAdapter
//                Log.i("TAG", "initData: MyAdapter")
//            }
//        }

        // wifi管理器
        val wifiManager = this.getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        val configs = wifiManager.configuredNetworks

        // 显示输出
        list.clear()
        for (config in configs) {
            Log.i("WifiConfig", "---------------")
            val wifiInfo = WifiInfo()
            wifiInfo.Ssid = config.SSID
            wifiInfo.BSSID = config.BSSID
            Log.i("WifiConfig", wifiInfo.toString())

            list.add(wifiInfo)
        }

        myAdapter = MyAdapter(list)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.adapter = myAdapter

    }

    private fun initView() {
        mRecyclerView.layoutManager = LinearLayoutManager(this)
    }


}

class MyAdapter(list: MutableList<WifiInfo>) : BaseQuickAdapter<WifiInfo, BaseViewHolder>(R.layout.item_connected_wifi, list) {
    override fun convert(helper: BaseViewHolder, item: WifiInfo) {

        helper.setText(R.id.tvTitle, item.Ssid)

    }

}