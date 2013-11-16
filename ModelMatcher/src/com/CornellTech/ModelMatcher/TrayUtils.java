package com.CornellTech.ModelMatcher;

import android.os.Environment;

public final class TrayUtils {
    public static final String TRAY_ID =
            "com.CornellTech.ModelMatcher.TRAY_ID";
    public static final String CURRENT_MAT =
            "com.CornellTech.ModelMatcher.CURRENT_MAT";        
    public static final String CURRENT_MAT_ADDRESS =
            "com.CornellTech.ModelMatcher.CURRENT_MAT_ADDRESS";  
    public static final String SORT_ORDER =
            "com.CornellTech.ModelMatcher.SORT_ORDER";      
    
    public static final String KEY_PREFIX =
            "com.CornellTech.ModelMatcher.KEY";    
    
    public static final String KEY_IDS = "com.CornellTech.ModelMatcher.KEY_IDS";
    public static final String KEY_NAME = "com.CornellTech.ModelMatcher.KEY_NAME";
    public static final String KEY_DATETIME = "com.CornellTech.ModelMatcher.KEY_DATETIME";
    public static final String KEY_N_MODEL_FILES = "com.CornellTech.ModelMatcher.KEY_N_MODEL_FILES";
    public static final String APPTAG = "Model Matching";
    
    //public static final String STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ModelMatcher";
    public static final String CURRENT_PIC = "/currentimage.jpg";
    
    // Invalid values, used to test storage when retrieving trays
    public static final long INVALID_LONG_VALUE = -999l;

    public static final float INVALID_FLOAT_VALUE = -999.0f;

    public static final int INVALID_INT_VALUE = -999;
    
    public static final String INVALID_STRING_VALUE = "INVALID";    
    public static final String STORAGE_PATH = "/mnt/sdcard/ModelMatcher/";
}
