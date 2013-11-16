package com.CornellTech.ModelMatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class HausdorffImageFinder extends Activity {
	
	private String TrayId;
	private String TrayFolder;
	private ArrayList<String> modelsInTray;
	private String currentImage;
	private Mat currentMat;
	private long currentMatAddr;
	
	private Map<String, Double> matches = new HashMap<String,Double>();
	
	private ArrayList<String> getImageFiles(String f) {
	    File folder = new File(TrayFolder + "/" + f);
	    String[] allfiles = folder.list();
	    ArrayList<String> imagefiles = new ArrayList<String>();
	    for(String name : allfiles)
	    {
	        if (name.toLowerCase().contains(".jpg"))
	        {
	        	imagefiles.add(name);
	        }
	    }		
	    return imagefiles;		
	}
	private ArrayList<String> getModelsInTray() {
	    File folder = new File(TrayFolder);
	    String[] names = folder.list();
	    ArrayList<String> folders = new ArrayList<String>();
	    for(String name : names)
	    {
	        if (new File(TrayFolder + "/" + name).isDirectory())
	        {
	            folders.add(name);
	        }
	    }		
	    return folders;
	}
	//public static native double hausdorff(String p1, String p2);
	public static native double hausdorff(long p1, long p2);
	public static native int mainautocrop(String p1, String p2);
	  
      /** Called when the activity is first created. */
      @Override
      public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            /** Create a TextView and set it to display
            * text loaded from a native method.
            */
            System.loadLibrary("hausdorffFinder");
            Log.i("hausdorff","imagefinder is here.");

      }
    private double getBestScore(String model) {
    	double bestScore = Double.MAX_VALUE;
    	ArrayList<String> filesInModel = getImageFiles(model);
    	for (String sprite: filesInModel) {
    		String testImage = TrayFolder + "/" + model + "/" + sprite;
    		//double score = hausdorff(currentImage, testImage);
    	    Mat testMat = Highgui.imread(testImage);
            //Imgproc.Canny(testMat, testMat, 80, 100);
            //Imgproc.cvtColor(testMat, testMat, Imgproc.COLOR_RGB2GRAY, 4);    		
    		double score = hausdorff(currentMatAddr, testMat.getNativeObjAddr());
    		if(score < bestScore) bestScore = score;
    	}    	
    	return bestScore;
    }
	  @Override
	protected void onResume() {
	    super.onResume();
	    TextView tv = new TextView(this);
	    tv.setText("Calculating...please wait");
	    setContentView(tv);
	    TrayId = getIntent().getStringExtra(TrayUtils.TRAY_ID); 
	    modelsInTray = getIntent().getStringArrayListExtra(TrayUtils.SORT_ORDER);
	    //currentMatAddr = getIntent().getLongExtra(TrayUtils.CURRENT_MAT_ADDRESS, 0);
	    //currentMat = getIntent().getExtras().getParcelable(TrayUtils.CURRENT_MAT);
	    
	    TrayFolder = TrayUtils.STORAGE_PATH + TrayId;
	    //modelsInTray = this.getModelsInTray();
	    currentImage = TrayFolder + TrayUtils.CURRENT_PIC;
	    
	    //int c = mainautocrop(currentImage,currentImage);
	    
	    currentMat = Highgui.imread(currentImage);
	    
	    currentMatAddr = currentMat.getNativeObjAddr();
        //Imgproc.Canny(currentMat, currentMat, 80, 100);
        //Imgproc.cvtColor(currentMat, currentMat, Imgproc.COLOR_RGB2GRAY, 4);
	    
	    
	    for (String model : modelsInTray) {
	    	double bestScore = getBestScore(model);
	    	matches.put(model, bestScore);
	    }
	    
	    matches = sortByValue(matches);
	    ArrayList<String> modelsOrdered = new ArrayList<String>();
	    for (Map.Entry<String, Double> entry : matches.entrySet())
	    {
	        modelsOrdered.add(entry.getKey());
	    }	    
	    //tv.setText(modelsOrdered.toString());
        //setContentView(tv);
		Intent i = new Intent(getApplicationContext(), TrayView.class);
		i.putExtra(TrayUtils.TRAY_ID, TrayId);  
		i.putStringArrayListExtra(TrayUtils.SORT_ORDER, modelsOrdered); 
		startActivity(i);
	  }
	  
	  public static Map<String, Double> sortByValue(Map<String, Double> map) {
	        List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(map.entrySet());

	        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {

	            public int compare(Map.Entry<String, Double> m1, Map.Entry<String, Double> m2) {
	                return (m1.getValue()).compareTo(m2.getValue());
	            }
	        });

	        Map<String, Double> result = new LinkedHashMap<String, Double>();
	        for (Map.Entry<String, Double> entry : list) {
	            result.put(entry.getKey(), entry.getValue());
	        }
	        return result;
	    }	  
	  
}
