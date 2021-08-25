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
        mNavigationBuilder!!.setTitleText("MMKV的读写").create()
        MMKV.init(this)
        mmkv = MMKV.defaultMMKV()
    }

    override fun initData() {
    }

    fun readClick(view: View) {
        ToastUtils.showLong(mmkv!!.getString("name1"))
    }
    fun writeClick(view: View) {
        val start = System.currentTimeMillis()
        for (i in 0..2) {
            mmkv?.putString("name$i", "peakmain$i")
        }


        val time = System.currentTimeMillis() - start
        LogUtils.e("write: 时间花销  $time")
    }
}