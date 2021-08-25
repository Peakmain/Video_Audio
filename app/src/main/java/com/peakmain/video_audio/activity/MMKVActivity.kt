package com.peakmain.video_audio.activity

import android.view.View
import com.peakmain.ui.utils.LogUtils
import com.peakmain.ui.utils.ToastUtils
import com.peakmain.video_audio.R
import com.peakmain.video_audio.basic.BaseActivity
import com.peakmain.video_audio.simple.mmkv.MMKV

/**
 * author ：Peakmain
 * createTime：2021/8/24
 * mail:2726449200@qq.com
 * describe：
 */
class MMKVActivity : BaseActivity() {
    private var mmkv: MMKV? = null
    override fun getLayoutId(): Int {
     return R.layout.activity_mmkv
    }

    override fun initView() {
        MMKV.init(this)
        mmkv = MMKV.defaultMMKV()
    }

    override fun initData() {
    }

    fun readClick(view: View) {
        ToastUtils.showLong(mmkv!!.getInt("name11",0).toString())
    }
    fun writeClick(view: View) {
        val start = System.currentTimeMillis()
        for (i in 0..999) {
            mmkv?.putInt("name$i", i)
        }


        val time = System.currentTimeMillis() - start
        LogUtils.e("write: 时间花销  $time")
    }
}