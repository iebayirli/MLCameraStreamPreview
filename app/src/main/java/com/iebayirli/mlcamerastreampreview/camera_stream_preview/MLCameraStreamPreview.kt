package com.iebayirli.mlcamerastreampreview.camera_stream_preview

import android.content.Context
import android.content.res.Configuration
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.core.content.res.use
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.classification.MLImageClassification
import com.huawei.hms.mlsdk.classification.MLImageClassificationAnalyzer
import com.huawei.hms.mlsdk.common.LensEngine
import com.huawei.hms.mlsdk.common.MLAnalyzer
import com.huawei.hms.mlsdk.face.MLFace
import com.huawei.hms.mlsdk.face.MLFaceAnalyzer
import com.huawei.hms.mlsdk.handkeypoint.MLHandKeypointAnalyzer
import com.huawei.hms.mlsdk.handkeypoint.MLHandKeypointAnalyzerFactory
import com.huawei.hms.mlsdk.handkeypoint.MLHandKeypoints
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentation
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationAnalyzer
import com.huawei.hms.mlsdk.objects.MLObject
import com.huawei.hms.mlsdk.objects.MLObjectAnalyzer
import com.huawei.hms.mlsdk.scd.MLSceneDetection
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzer
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerFactory
import com.huawei.hms.mlsdk.skeleton.MLSkeleton
import com.huawei.hms.mlsdk.skeleton.MLSkeletonAnalyzer
import com.huawei.hms.mlsdk.skeleton.MLSkeletonAnalyzerFactory
import com.huawei.hms.mlsdk.text.MLText
import com.huawei.hms.mlsdk.text.MLTextAnalyzer
import com.iebayirli.mlcamerastreampreview.R
import java.io.IOException
import java.lang.Exception


class MLCameraStreamPreview(
    private val mContext: Context,
    attrs: AttributeSet?
) : ViewGroup(mContext, attrs) {

    private val mSurfaceView: SurfaceView = SurfaceView(mContext)
    private var mStartRequested = false
    private var mSurfaceAvailable = false
    private var mLensEngine: LensEngine? = null
    private lateinit var mlAnalyzer: MLAnalyzer<*>
    lateinit var mBaseTransactor: BaseTransactor<*>


    var transactResult: ((MLAnalyzer.Result<*>?) -> Unit)? = null
    var destroy: (() -> Unit)? = null


    init {
        if (attrs != null) {
            mContext.obtainStyledAttributes(attrs,
                R.styleable.MLCameraStreamPreview, 0, 0).use {
                cast(
                    it.getEnum(
                        R.styleable.MLCameraStreamPreview_analyzerType,
                        AnalyzerTypes.def
                    )
                )
                mLensEngine = LensEngine.Creator(mContext, mlAnalyzer)
                    .applyDisplayDimension(
                        it.getInteger(
                            R.styleable.MLCameraStreamPreview_lensEngineDisplayWidth,
                            480
                        ),
                        it.getInteger(
                            R.styleable.MLCameraStreamPreview_lensEngineDisplayHeight,
                            640
                        )
                    )
                    .setLensType(
                        it.getInt(R.styleable.MLCameraStreamPreview_lensEngineLensType, 1)
                    )
                    .applyFps(
                        it.getFloat(R.styleable.MLCameraStreamPreview_lensEngineFps, 20f)
                    )
                    .enableAutomaticFocus(
                        it.getBoolean(
                            R.styleable.MLCameraStreamPreview_lensEngineAutomaticFocus,
                            true
                        )
                    )
                    .create()


                mSurfaceView.holder.addCallback(SurfaceCallback())
                this.addView(mSurfaceView)

            }
        }
    }

    private fun cast(type: AnalyzerTypes) {
        when (type) {
            AnalyzerTypes.TextAnalyzer -> {
                mlAnalyzer = MLTextAnalyzer.Factory(mContext).create()
                setTransactor<MLText.Block>(mlAnalyzer as MLTextAnalyzer)
            }
            AnalyzerTypes.ImageClassification -> {
                mlAnalyzer = MLAnalyzerFactory.getInstance().localImageClassificationAnalyzer
                setTransactor<MLImageClassification>(mlAnalyzer as MLImageClassificationAnalyzer)
            }
            AnalyzerTypes.ObjectAnalyzer -> {
                mlAnalyzer = MLAnalyzerFactory.getInstance().localObjectAnalyzer
                setTransactor<MLObject>(mlAnalyzer as MLObjectAnalyzer)
            }
            AnalyzerTypes.ImageSegmentationAnalyzer -> {
                mlAnalyzer = MLAnalyzerFactory.getInstance().imageSegmentationAnalyzer
                setTransactor<MLImageSegmentation>(mlAnalyzer as MLImageSegmentationAnalyzer)
            }
            AnalyzerTypes.FaceAnalyzer -> {
                mlAnalyzer = MLAnalyzerFactory.getInstance().faceAnalyzer
                setTransactor<MLFace>(mlAnalyzer as MLFaceAnalyzer)
            }
            AnalyzerTypes.SkeletonAnalyzer -> {
                mlAnalyzer = MLSkeletonAnalyzerFactory.getInstance().skeletonAnalyzer
                setTransactor<MLSkeleton>(mlAnalyzer as MLSkeletonAnalyzer)
            }
            AnalyzerTypes.HandKeypointAnalyzer -> {
                mlAnalyzer = MLHandKeypointAnalyzerFactory.getInstance().handKeypointAnalyzer
                setTransactor<MLHandKeypoints>(mlAnalyzer as MLHandKeypointAnalyzer)
            }
            AnalyzerTypes.SceneDetectionAnalyzer -> {
                mlAnalyzer = MLSceneDetectionAnalyzerFactory.getInstance().sceneDetectionAnalyzer
                setTransactor<MLSceneDetection>(mlAnalyzer as MLSceneDetectionAnalyzer)
            }
            else -> throw Exception("Invalid analyzer type.")
        }
    }

    private fun <T> setTransactor(analyzer: MLAnalyzer<T>) {
        mBaseTransactor = BaseTransactor<T>(
            {
                transactResult?.invoke(it)
            },
            {
                destroy?.invoke()
            }).apply {
            analyzer.setTransactor(this)
        }
    }

    fun start() {
        if (!mStartRequested) {
            Pair(mLensEngine, mlAnalyzer).letCheckNull { lensEngine, mlAnalyzer ->
                mStartRequested = true
                startIfReady()
            }
        }
    }

    fun release() {
        mLensEngine?.let {
            mLensEngine!!.release()
            mLensEngine = null
        }
    }

    fun stop() {
        mLensEngine?.let {
            mLensEngine!!.close()
        }
    }

    private fun startIfReady() {
        mlAnalyzer?.let {
            if (mStartRequested && mSurfaceAvailable) {
                try {
                    mLensEngine!!.run(mSurfaceView.holder)
                    mStartRequested = false
                } catch (e: Exception) {
                    Log.e(TAG, "Message: ${e.message}")
                }

            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var previewWidth = 480
        var previewHeight = 360
        mLensEngine?.let {
            val size = mLensEngine!!.displayDimension
            if (size != null) {
                previewWidth = size.width
                previewHeight = size.height
            }
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode) {
            val tmp = previewWidth
            previewWidth = previewHeight
            previewHeight = tmp
        }
        val viewWidth = right - left
        val viewHeight = bottom - top
        val childWidth: Int
        val childHeight: Int
        var childXOffset = 0
        var childYOffset = 0
        val widthRatio = viewWidth.toFloat() / previewWidth.toFloat()
        val heightRatio = viewHeight.toFloat() / previewHeight.toFloat()


        // To fill the view with the camera preview, while also preserving the correct aspect ratio,
        // it is usually necessary to slightly oversize the child and to crop off portions along one
        // of the dimensions. We scale up based on the dimension requiring the most correction, and
        // compute a crop offset for the other dimension.
        if (widthRatio > heightRatio) {
            childWidth = viewWidth
            childHeight = (previewHeight.toFloat() * widthRatio).toInt()
            childYOffset = (childHeight - viewHeight) / 2
        } else {
            childWidth = (previewWidth.toFloat() * heightRatio).toInt()
            childHeight = viewHeight
            childXOffset = (childWidth - viewWidth) / 2
        }

        for (i in 0 until this.childCount) {
            // One dimension will be cropped. We shift child over or up by this offset and adjust
            // the size to maintain the proper aspect ratio.
            getChildAt(i).layout(
                -1 * childXOffset, -1 * childYOffset, childWidth - childXOffset,
                childHeight - childYOffset
            )
        }
        try {
            startIfReady()
        } catch (e: IOException) {
            Log.e(TAG, "Could not start camera source.", e)
        }
    }

    private inner class SurfaceCallback : SurfaceHolder.Callback {
        override fun surfaceCreated(surface: SurfaceHolder) {
            mSurfaceAvailable = true
            mLensEngine?.let {
                it.close()
            }
            try {
                startIfReady()
            } catch (e: IOException) {
                Log.e(
                    TAG,
                    "Could not start camera source.",
                    e
                )
            }
        }

        override fun surfaceDestroyed(surface: SurfaceHolder) {
            mSurfaceAvailable = false
        }

        override fun surfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int
        ) {
            try {
                startIfReady()
            } catch (e: IOException) {
                Log.e(
                    TAG,
                    "Could not start camera source.",
                    e
                )
            }
        }
    }

    private val isPortraitMode: Boolean
        get() {
            val orientation = mContext.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return false
            }
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return true
            }
            Log.d(
                TAG,
                "isPortraitMode returning false by default"
            )
            return false
        }


    companion object {
        private val TAG = MLCameraStreamPreview::class.java.simpleName
    }
}

inline fun <A, B, R> Pair<A?, B?>.letCheckNull(block: (A, B) -> R): R? =
    when (null) {
        first, second -> null
        else -> block(first as A, second as B)
    }

inline fun <reified T : Enum<T>> TypedArray.getEnum(index: Int, default: T) =
    getInt(index, -1).let {
        if (it >= 0) enumValues<T>()[it] else default
    }

enum class AnalyzerTypes {
    TextAnalyzer,
    ImageClassification,
    ObjectAnalyzer,
    ImageSegmentationAnalyzer,
    FaceAnalyzer,
    SkeletonAnalyzer,
    HandKeypointAnalyzer,
    SceneDetectionAnalyzer,
    def;
}
