package com.CornellTech.ModelMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TrayView extends Activity {
	
	  // Persistent storage for trays
	  private TrayStore mPrefs;
	
	  // All static variables
	  String URL = "";
	  // XML node keys
	  static final String KEY_MODEL = "model"; // parent node
	  static final String KEY_ID = "id";
	  static final String KEY_DIM = "dim";
	  static final String KEY_MATERIAL = "material";
	  static final String KEY_THUMB_URL = "thumb_url";
	  static final String KEY_CHECK = "check";
	  
	  // booleans of matched models
	  TreeMap<Integer, Boolean> matchedMap = new TreeMap<Integer, Boolean>();
	  int numModels = 0;
	  
	  //Handle to name entry in the UI
	  private TextView mName;
	  private String mTrayID;
	  private ArrayList<HashMap<String,String>> modelsList;
	  private ArrayList<String> sortOrder, allModels;
	  
	  ListView list;
	  ModelAdapter adapter;
		
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);
		    
		    // Instantiate a new tray storage area
		    mPrefs = new TrayStore(this);
			    
		    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); 
		    
		    // Attach to the main UI
		    // setContentView(R.layout.activity_tray_view);
		    setContentView(R.layout.models);
	        
		    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title_cam);
		  
		    // Get handles to the editor fields in the UI
		    mName = (TextView) findViewById(R.id.tray_name);
		    mTrayID = getIntent().getStringExtra(TrayUtils.TRAY_ID); 

	        // Add new tray using plus button
	        ImageButton home_btn = (ImageButton) findViewById(R.id.home_btn);
	        home_btn.setOnClickListener(new View.OnClickListener() {
	             public void onClick(View v) {
	    	      	 Intent i = new Intent(getApplicationContext(), TrayList.class);
	    	      	 startActivity(i);  
	             }
	        });
		    
	        // Add new tray using plus button
	        ImageButton camera_btn = (ImageButton) findViewById(R.id.cam_btn);
	        camera_btn.setOnClickListener(new View.OnClickListener() {
	             public void onClick(View v) {
	    	      	 Intent i = new Intent(getApplicationContext(), ModelMatcher.class);
	    	      	 i.putExtra(TrayUtils.TRAY_ID, mTrayID);
	    	      	 i.putStringArrayListExtra(TrayUtils.SORT_ORDER, sortOrder); 
	    	      	 startActivity(i);  
	             }
	        });
	        	        
	  }

	  @Override
	  protected void onResume() {
		    super.onResume();

		    String TrayID = getIntent().getStringExtra(TrayUtils.TRAY_ID); 
		    
		    // fill out information about tray
		    URL = "http://t-odd.com/" + TrayID + ".xml";
		    
			mName.setText(TrayID);

			// set matchedMap to be pulled from matchedString in mPrefs
			String matchedString = mPrefs.getMatchedString(mTrayID);
		    if (!matchedString.equals("-")) {
		    	for (int pos = 0; pos < matchedString.length(); pos++)
		    	{
		    		if (matchedString.charAt(pos) == '1')
		    			matchedMap.put(pos, true);
		    		else
		    			matchedMap.put(pos, false);
		    	}
		    }
		    
			list=(ListView)findViewById(R.id.modellist);
			modelsList = getModelsList();
			ArrayList<String> receivedSortOrder = getIntent().getStringArrayListExtra(TrayUtils.SORT_ORDER);
			
			if (receivedSortOrder != null) {
				sortOrder = receivedSortOrder;
				System.out.println(sortOrder.toString());
				modelsList = sortedModelsList();
				Toast.makeText(getApplicationContext(), "Models sorted. The best matches are on top.", Toast.LENGTH_SHORT).show();
			}
			else sortOrder = new ArrayList<String>(allModels);
			
	        adapter=new ModelAdapter(this, modelsList);        
	        list.setAdapter(adapter);
	        

	  }
	  
	  private ArrayList<HashMap<String, String>> sortedModelsList() {
		  ArrayList<HashMap<String, String>> sorted = new ArrayList<HashMap<String, String>>();
		  for(String model : sortOrder) {
			  for(HashMap<String, String> hash : modelsList){
				  if(hash.get(KEY_ID).equals(model)) sorted.add(hash);
			  }
		  }
		  return sorted;
	  }
	  public ArrayList<HashMap<String, String>> getModelsList()
	  {
		    ArrayList<HashMap<String, String>> modelsList = new ArrayList<HashMap<String, String>>();
		    sortOrder = new ArrayList<String>();
		    allModels = new ArrayList<String>();
			XMLParser parser = new XMLParser();
			String xml = parser.getXmlFromUrl(URL); // getting XML from URL
			Document doc = parser.getDomElement(xml); // getting DOM element
			
			NodeList nl = doc.getElementsByTagName(KEY_MODEL);
			// looping through all song nodes <song>
			numModels = nl.getLength();
			for (int i = 0; i < numModels; i++) {
				// creating new HashMap
				HashMap<String, String> map = new HashMap<String, String>();
				Element e = (Element) nl.item(i);
				
				// Check if the tray is in the store
				String modelID = parser.getValue(e, KEY_ID);
				allModels.add(modelID);
				
				// adding each child node to HashMap key => value
				map.put(KEY_MODEL, modelID);
				map.put(KEY_ID, parser.getValue(e, KEY_ID));
				map.put(KEY_DIM, parser.getValue(e, KEY_DIM));
				map.put(KEY_MATERIAL, parser.getValue(e, KEY_MATERIAL));
				map.put(KEY_THUMB_URL, parser.getValue(e, KEY_THUMB_URL));
				Boolean checkVal = false;
				if (matchedMap.containsKey(i))
					checkVal = matchedMap.get(i);
				map.put(KEY_CHECK, String.valueOf(checkVal));

				// adding HashMap to ArrayList
				modelsList.add(map);

			}
		  
		  return modelsList;
	  }

	  public void onCheckboxClicked(View view) {
		    // Is the view now checked?
		    boolean checked = ((CheckBox) view).isChecked();
		    
		    list=(ListView)findViewById(R.id.modellist);

		    //int viewId = view.getId();
		    int position = list.getPositionForView(((View) view.getParent()));
		    String model = allModels.get(position);		
		    
		    if(checked) {
		    	while(sortOrder.contains(model)) sortOrder.remove(model);
		    }
		    else if(!sortOrder.contains(model)){
		    	boolean rc = sortOrder.add(model);
		    }
		    System.out.println(model);
		    System.out.println(sortOrder.toString());
		    System.out.println(allModels.toString());
		    // update array of binary indicators
		    matchedMap.put(position, checked);
		    
		    for (int pos = 0; pos < numModels; pos++)
		    {
		    	if (!matchedMap.containsKey(pos))
		    		matchedMap.put(pos, false);
		    }
		    
		    Collection<Boolean> matchedBoolValues = matchedMap.values();
		    String matchedString = "";
		    for (Boolean val : matchedBoolValues)
		    {
		    	String binaryIndicator = val ? "1" : "0";
		    	matchedString += binaryIndicator;
		    }
		    //Toast.makeText(getApplicationContext(), matchedString, 1000).show();
		    
		    mPrefs.setMatchedString(mTrayID, matchedString);
		    
			//Toast.makeText(getApplicationContext(), mTrayID + String.valueOf(count2), 500).show();
		}
}