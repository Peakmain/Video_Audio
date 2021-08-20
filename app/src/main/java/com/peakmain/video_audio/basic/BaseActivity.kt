package com.peakmain.video_audio.basic

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.peakmain.ui.navigationbar.DefaultNavigationBar
import com.peakmain.video_audio.R

/**
 * author ：Peakmain
 * createTime：2020/3/9
 * mail:2726449200@qq.com
 * describe：
 */
abstract class BaseActivity : AppCompatActivity() {
    @JvmField
    var mNavigationBuilder: DefaultNavigationBar.Builder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(getLayoutId())
        mNavigationBuilder = DefaultNavigationBar.Builder(this, findViewById(android.R.id.content))
                .hideLeftText()
                .setDisplayHomeAsUpEnabled(true)
                .setNavigationOnClickListener(View.OnClickListener { finish() })
                .hideRightView()
                .setToolbarBackgroundColor(R.color.color_802F73F6)
        initView()
        initData()

    }

    protected abstract fun getLayoutId(): Int
    protected abstract fun initView()
    protected abstract fun initData()
}