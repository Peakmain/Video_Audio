package com.peakmain.video_audio.activity

import android.view.View
import com.peakmain.ui.constants.PermissionConstants
import com.peakmain.ui.utils.PermissionUtils
import com.peakmain.video_audio.R
import com.peakmain.video_audio.basic.BaseActivity
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.activity_h264_player.*

/**
 * author ：Peakmain
 * createTime：2021/8/27
 * mail:2726449200@qq.com
 * describe：
 */
class CameraActivity : BaseActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_camera
    }

    override fun initView() {
        mNavigationBuilder?.setTitleText("Camera使用")?.create()
        if(!PermissionUtils.hasPermission(PermissionConstants.CAMERA)){
            PermissionUtils.request(this,100,PermissionConstants.getPermissions(PermissionConstants.CAMERA),
                object :PermissionUtils.OnPermissionListener{
                    override fun onPermissionDenied(deniedPermissions: List<String>) {

                    }

                    override fun onPermissionGranted() {

                    }

                }
            )
        }
    }

    override fun initData() {
    }

    fun cameraClick(view: View) {
        surface.startCaptrue()
    }

}