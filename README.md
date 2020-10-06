# Camera Stream Preview for Huawei ML Kit Services

ML Camera Stream Preview is a custom view that helps you to easily and quickly use ML Kit Services which can works with camera stream. 
 - It provides LensEngine capabilities without initialization.
 - To get the results detected by the ML Kit services. You don't need to create Transactor class for each ML Kit services that you use in your app.
 

## How to use? ##

Camera Stream Preview is not designed for use like library. You can clone the project and add the relevant classes on your app. You can change parts the way you use them. For basic usage:

- First, if you want to use the camera stream preview like in this project don't forget to add the all relevant ML Kit service SDKs into your app-level build.gradle file.
>In HMS ML Kit, currently, there are eight services can run on camera streams. These services are:
Text Recognition, Image Classification, Object Detection and Tracking, Image Segmentation, Face Detection, Skeleton Detection, Hand Keypoint Detection, Scene Detection
- After adding the SDKs., Copy the camera_stream_preview package into your project. Don't forget the change package name in these classes.
- And you will need attrs file also. Go to res>values and you will see the attrs.xml file. Copy this file also into your project.
- After completing the steps above, you are ready to use MLCameraStreamPreview.

### In Xml; ###
- Open the activity/fragment's xml file that you want to use MLCameraStreamPreview.

```
<com.iebayirli.mlcamerastreampreview.camera_stream_preview.MLCameraStreamPreview
        android:id="@+id/mCameraStreamPreview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:analyzerType="IMAGE_CLASSIFICATION_ANALYZER"
        app:lensEngineLensType="BACK_LENS"
        app:lensEngineFps="30"
        app:lensEngineAutomaticFocus="true"
        app:lensEngineDisplayHeight="1440"
        app:lensEngineDisplayWidth="1080"/>
```
- Then add the MLCameraStreamPreview as you can see the above. 

Let's examine attributes;
1. analyzerType: We are giving the analyzer type that want to use. You must set this attribute before you use the MLCameraStreamPreview. Otherwise you will get an error.
2. Other attributes are about LensEngine properties. You don't need to set these attributes for use MLCameraStreamPreview. But if you want to use customized LensEngine instance you can set relevant parameters.
> In MLCameraStreamPreview class the default LensEngine properties are:
--  displayDimension(480,640)
--  lensType(LensEngine.BACK_LENS)
--  fps(20f)
--  automaticFocus(true)

### In Activity/Fragment; ###
>Before starting MLCameraStreamPreview, make sure the app has camera permission. Otherwise you will not get an error but you will see the black screen.

You can start MLCameraStreamPreview like below after getting camera permission or wherever you want.
```
 mCameraStreamPreview.start()
```

Also, don't forget to integrate MLCameraStreamPreview with lifecycle events.

```
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
```

### For retreive detected results; ###

Sample code:
```
mCameraStreamPreview.mBaseTransactor.getResult<MLImageClassification>().observe(this,
            Observer {
                it?.analyseList?.let {
                    Log.d(TAG, "Size: ${it.size()}")
                }
            })
```

We are observing getResult<_T_>() method that inside BaseTransactor class for retrieving detected result.

For casting detected result to the relevant analyzer result, we need to give specific result type to the getResult<_T_>() function. In sample, we used MLImageClassification Analyzer therefore we gave MLImageClassification type to the method.

The other analyzers and result types:

Analyzer | Result type| 
--- | --- 
MLTextAnalyzer | MLText.Block | 
MLImageClassificationAnalyer | MLImageClassification | 
MLObjectAnalyzer | MLObject | 
MLImageSegmentationAnalyer | MLImageSegmentation | 
MLFaceAnalyzer | MLFace | 
MLSkeletenAnalyzer | MLSkeleton | 
MLHandkeypointAnalyzer | MLHandKeypoints | 
MLSceneDetectionAnalyzer | MLSceneDetection | 


With following these steps you can implement and use MLCameraStreamPreview. Hope it helps with your projects. Thank you.
