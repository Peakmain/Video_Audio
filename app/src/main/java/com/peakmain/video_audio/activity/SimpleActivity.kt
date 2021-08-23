package com.peakmain.video_audio.activity

import android.content.Intent
import com.peakmain.ui.recyclerview.listener.OnItemClickListener
import com.peakmain.video_audio.R
import com.peakmain.video_audio.activity.simple.H264PlayerActivity
import com.peakmain.video_audio.basic.BaseActivity
import com.peakmain.video_audio.basic.BaseRecyclerStringAdapter
import kotlinx.android.synthetic.main.activity_main.*

/**
 * author ：Peakmain
 * createTime：2021/8/22
 * mail:2726449200@qq.com
 * describe：
 */
class SimpleActivity:BaseActivity(){
    private var mData: ArrayList<String> = ArrayList()
    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        mNavigationBuilder!!.setTitleText("音视频简单使用").create()
    }

    override fun initData() {
        mData.add("MediaCodec实现H264音视频播放")
        val adapter = BaseRecyclerStringAdapter(this, data = mData)
        recycler_view.adapter = adapter
        adapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                when (position){
                    0->{
                        startActivity(Intent(this@SimpleActivity,H264PlayerActivity::class.java))
                    }
                }
            }

        })
    }

}