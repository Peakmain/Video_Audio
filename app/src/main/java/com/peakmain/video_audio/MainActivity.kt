package com.peakmain.video_audio

import android.util.Log
import com.peakmain.ui.recyclerview.listener.OnItemClickListener
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

    }

    override fun initData() {
        mData.add("native异常捕获")
        val adapter = BaseRecyclerStringAdapter(this, data = mData)
        recycler_view.adapter = adapter
        adapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                when (position){
                    0->{
                        NativeCrashMonitor.nativeCrashCreate()
                    }
                }
            }

        })
    }

}
