package com.CornellTech.ModelMatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.CornellTech.ModelMatcher.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class ModelMatcher extends Activity implements CvCameraViewListener2, OnClickListener {
    private static final String    TAG = "Canny::Activity";
    
    private Button takeShot;
    private static final int       VIEW_MODE_RGBA     = 0;
    private static final int       VIEW_MODE_GRAY     = 1;
    private static final int       VIEW_MODE_CANNY    = 2;
    private static final int       VIEW_MODE_FEATURES = 5;

    private int                    mViewMode;
    private Mat                    mRgba, mRgbaDilated;
    private Mat                    mIntermediateMat;
    private Mat                    mGray;

    private MenuItem               mItemPreviewRGBA;
    private MenuItem               mItemPreviewGray;
    private MenuItem               mItemPreviewCanny;
    private MenuItem               mItemPreviewFeatures;

    private CameraBridgeViewBase   mOpenCvCameraView;
    private Mat myFrame;
    private File savedImage;
    private ArrayList<String> sortOrder;
    
    private String mTrayID;
    
    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }
    
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("opencv_java");
                    
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ModelMatcher() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.canny_view);
        
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.canny_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mViewMode = VIEW_MODE_CANNY;

        ArrayList<View> views = new ArrayList<View>();
        views.add(findViewById(R.id.snapshot));
        mOpenCvCameraView.addTouchables(views);
        
        takeShot = (Button)findViewById(R.id.snapshot);
        
        takeShot.setOnClickListener(new OnClickListener() {
        
        
			@Override
			public void onClick(View v) {
				String path = TrayUtils.STORAGE_PATH + mTrayID + TrayUtils.CURRENT_PIC;
				//Toast.makeText(getApplicationContext(), path, Toast.LENGTH_SHORT).show();
				Highgui.imwrite(path, myFrame);
				Intent i = new Intent(getApplicationContext(), HausdorffImageFinder.class);
				//long imageAddress = myFrame.getNativeObjAddr();
				i.putExtra(TrayUtils.TRAY_ID, mTrayID);
				i.putStringArrayListExtra(TrayUtils.SORT_ORDER, sortOrder); 
				//send native object address of image preview to avoid unnecessary I/O
				/*i.putExtra(TrayUtils.CURRENT_MAT_ADDRESS, imageAddress);
				Bundle b = new Bundle();
				b.putParcelable(TrayUtils.CURRENT_MAT, (Parcelable) myFrame  );				
				i.putExtras(b);*/
		      	startActivity(i);    
			}
		});        
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        mTrayID = getIntent().getStringExtra(TrayUtils.TRAY_ID); 
        sortOrder = getIntent().getStringArrayListExtra(TrayUtils.SORT_ORDER);
        Toast.makeText(getApplicationContext(), sortOrder.toString(), Toast.LENGTH_LONG).show();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() { 	
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        Imgproc.Canny(mGray, mIntermediateMat, 80, 100);
        //Imgproc.cvtColor(mIntermediateMat, mIntermediateMat, Imgproc.COLOR_GRAY2RGBA, 4);
        //Imgproc.dilate(mIntermediateMat, mIntermediateMat, new Mat());
        
        //this.myFrame = mIntermediateMat;
        this.myFrame = mRgba;

        return mIntermediateMat;
    }

    public static Mat CannyImage(String imgPathIn) {
    	File f = new File(imgPathIn);
    	if (!f.exists()) {
    		System.out.println(f.getAbsolutePath() + " does not exist");
    		System.exit(0);
    	}    	
    	Mat img = Highgui.imread(f.getAbsolutePath());
    	if (img.empty()) {
    		System.out.println("empty");
    		System.exit(0);
    	}
    	Imgproc.Canny(img, img, 80, 100);
    	Imgproc.dilate(img, img, new Mat());
    	return img;
    	
    }
    
    public static double SurfDist(String imgPath1, String imgPath2) {

    	Mat img1 = CannyImage(imgPath1);
    	Mat img2 = CannyImage(imgPath2);

    	FeatureDetector  fd = FeatureDetector.create(FeatureDetector.FAST); 

    	MatOfKeyPoint points1 = new MatOfKeyPoint();
    	MatOfKeyPoint points2 = new MatOfKeyPoint();
    	
    	fd.detect(img1, points1);
    	fd.detect(img2, points2);
    	
        Mat descriptors1 = new Mat();
        Mat descriptors2 = new Mat();
        
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);
        extractor.compute(img1, points1, descriptors1);
        extractor.compute(img2, points2, descriptors2);
        
        MatOfDMatch matches = new MatOfDMatch();

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
        matcher.match(descriptors1, descriptors2, matches);
        
        List<DMatch> matchesList = matches.toList();
        
        Double max_dist = 0.0;
        Double min_dist = 100.0;

        for (int i = 0; i < matchesList.size(); i++)
        {
            Double dist = (double) matchesList.get(i).distance;

            if (dist < min_dist && dist != 0)
            {
                min_dist = dist;
            }

            if (dist > max_dist)
            {
                max_dist = dist;
            }

        }      
    	img1.release();
    	img2.release();
    	return min_dist;
    	
    }  

    public static double Dist(String imgPath1, String imgPath2) {

    	Mat img1 = CannyImage(imgPath1);
    	Mat img2 = CannyImage(imgPath2);

    	FeatureDetector  fd = FeatureDetector.create(FeatureDetector.FAST); 

    	MatOfKeyPoint points1 = new MatOfKeyPoint();
    	MatOfKeyPoint points2 = new MatOfKeyPoint();
    	
    	fd.detect(img1, points1);
    	fd.detect(img2, points2);
    	
        Mat descriptors1 = new Mat();
        Mat descriptors2 = new Mat();
        
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);
        extractor.compute(img1, points1, descriptors1);
        extractor.compute(img2, points2, descriptors2);
        
        MatOfDMatch matches = new MatOfDMatch();

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
        matcher.match(descriptors1, descriptors2, matches);
        
        List<DMatch> matchesList = matches.toList();
        
        Double max_dist = 0.0;
        Double min_dist = 100.0;

        for (int i = 0; i < matchesList.size(); i++)
        {
            Double dist = (double) matchesList.get(i).distance;

            if (dist < min_dist && dist != 0)
            {
                min_dist = dist;
            }

            if (dist > max_dist)
            {
                max_dist = dist;
            }

        }      
    	img1.release();
    	img2.release();
    	return min_dist;
    	
    }    
    public native void FindFeatures(long matAddrGr, long matAddrRgba);
    
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}


}
