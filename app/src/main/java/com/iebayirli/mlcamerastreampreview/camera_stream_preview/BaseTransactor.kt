package com.iebayirli.mlcamerastreampreview.camera_stream_preview

import androidx.lifecycle.MutableLiveData
import com.huawei.hms.mlsdk.common.MLAnalyzer


class BaseTransactor<T> : MLAnalyzer.MLTransactor<T>{


    private var resultList = MutableLiveData<MLAnalyzer.Result<T>?>()

    fun <T>getResult() = resultList as MutableLiveData<MLAnalyzer.Result<T>?>

    override fun transactResult(p0: MLAnalyzer.Result<T>?) {
        resultList.postValue(p0)
    }

    override fun destroy() {

    }

}