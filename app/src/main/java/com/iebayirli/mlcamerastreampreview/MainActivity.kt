package com.iebayirli.mlcamerastreampreview

import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import com.huawei.hms.mlsdk.classification.MLImageClassification
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (!checkPermission())
            requestPermissions(arrayOf(cameraPermission), CAMERA_REQ_CODE)

        mCameraStreamPreview.mBaseTransactor.getResult<MLImageClassification>().observe(this,
            Observer {
                it?.analyseList?.let {
                    Log.d(TAG, "Size: ${it.size()}")
                }
            })
    }

    private fun startCamera() {
        mCameraStreamPreview.start()
    }

    override fun onStop() {
        super.onStop()
        mCameraStreamPreview.stop()
    }

    override fun onResume() {
        super.onResume()
        if (checkPermission()) {
            startCamera()
        }
    }

    override fun onDestroy() {
        mCameraStreamPreview.release()
        super.onDestroy()
    }

    private fun checkPermission(): Boolean {
        return checkSelfPermission(cameraPermission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQ_CODE -> {
                if (grantResults.isEmpty() && grantResults[0] != Activity.RESULT_OK) {
                    finish()
                }
            }
        }
    }

    companion object {
        const val CAMERA_REQ_CODE = 101
        const val TAG = "Analyzer Result: "
        const val cameraPermission = android.Manifest.permission.CAMERA
    }

}