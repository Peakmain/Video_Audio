package com.peakmain.video_audio

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.peakmain.ui.recyclerview.listener.OnItemClickListener
import com.peakmain.video_audio.activity.ProjectScreenActivity
import com.peakmain.video_audio.activity.SimpleActivity
import com.peakmain.video_audio.basic.BaseActivity
import com.peakmain.video_audio.basic.BaseRecyclerStringAdapter
import com.peakmain.video_audio.utils.crash.NativeCrashMonitor
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private var mData: ArrayList<String> = ArrayList()
    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        mNavigationBuilder!!.setTitleText("首页").create()
        checkPermission()
    }

    fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1
            )
        }
        return false
    }

    override fun initData() {
        mData.add("simple的使用")
        mData.add("H265实现手机投屏")
        val adapter = BaseRecyclerStringAdapter(this, data = mData)
        recycler_view.adapter = adapter
        adapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                when (position) {
                    0 -> {
                        startActivity(Intent(this@MainActivity, SimpleActivity::class.java))
                    }
                    1->{
                        startActivity(Intent(this@MainActivity, ProjectScreenActivity::class.java))
                    }
                }
            }

        })
    }

}
