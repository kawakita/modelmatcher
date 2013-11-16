package com.CornellTech.ModelMatcher;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

/* Changing from ListActivity to Activity */
public class TrayList extends Activity {

	  // Persistent storage for trays
	  private TrayStore mPrefs;

	  /* adding */
	  // All static variables
	  static final String URL = "http://t-odd.com/trays.xml";
	  // XML node keys
	  // XML node keys
	  static final String KEY_TRAY = "tray"; // parent node
	  static final String KEY_ID = "id";
	  static final String KEY_DATE = "date";
	  static final String KEY_NUMITEMS = "numitems";
	  static final String KEY_THUMB_URL = "thumb_url";
	  
	  static List<String> allTrayIDset;
	  
	  ListView list;
	  LazyAdapter adapter;
	  /* end adding */
	  
	  public void onCreate(Bundle icicle) {
	    super.onCreate(icicle);

	    // Instantiate a new tray storage area
	    mPrefs = new TrayStore(this);

	    // Get all registered tray IDs from the store.
	    allTrayIDset = new ArrayList<String>(mPrefs.getAllRegisteredTrayIDs());
	    Collections.sort(allTrayIDset);
	    
	    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); 
	    
	    //ListView listView = getListView();
	    
	    /* adding */
	    setContentView(R.layout.main);

		list=(ListView)findViewById(R.id.list);
		
		// Getting adapter by passing xml data ArrayList
        adapter=new LazyAdapter(this, getTraysList());        
        list.setAdapter(adapter);
	    /* end adding */
        
        File f = new File(TrayUtils.STORAGE_PATH);
        if(!f.exists()) f.mkdirs();        
	    
	    // this has to go after the listView
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title_plus);
	    
        // Add new tray using plus button
        ImageButton plus_btn = (ImageButton) findViewById(R.id.plus_btn);
        plus_btn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
    	       	Intent i = new Intent(getApplicationContext(), TrayEntry.class);
    	      	startActivity(i); 
             }
        });
        
        // changed from listView to list
	    list.setOnItemClickListener(new OnItemClickListener() {
	      public void onItemClick(AdapterView<?> parent, View view,
	          int position, long id) {
	    	 
	        /***************************************
	         * Launch Tray activity tagged with the selected tray ID.
	         ***************************************/
	      	
	    	Intent i = new Intent(getApplicationContext(), TrayView.class);	    	
	    	Collections.sort(allTrayIDset);  
	      	String item = allTrayIDset.get(position);
	      	i.putExtra(TrayUtils.TRAY_ID, item);
	      	startActivity(i);    	 	      	
	      }
	    });
	    	    
	    list.setOnItemLongClickListener(new OnItemLongClickListener() {
	    	public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                    int position, long id) {
	    			    		
	    		String mTrayID = allTrayIDset.get(position); 
			    mPrefs.clearTray(mTrayID);
			    Toast.makeText(getApplicationContext(), "Tray deleted", Toast.LENGTH_SHORT).show();
			    
			    // Get all registered tray IDs from the store.
			    allTrayIDset = new ArrayList<String>(mPrefs.getAllRegisteredTrayIDs());
			    Collections.sort(allTrayIDset);

				list=(ListView)findViewById(R.id.list);
				
				// Getting adapter by passing xml data ArrayList
		        adapter=new LazyAdapter(TrayList.this, getTraysList());        
		        list.setAdapter(adapter);
			    
		        mPrefs.removeMatchedString(mTrayID);
		        
                return true;
            }
		});
	  }

	  @Override
	  public void onResume() {
	    super.onResume();
	    
	    // Get all registered tray IDs from the store.
	    allTrayIDset = new ArrayList<String>(mPrefs.getAllRegisteredTrayIDs());
	    Collections.sort(allTrayIDset);

		list=(ListView)findViewById(R.id.list);
		
		// Getting adapter by passing xml data ArrayList
        adapter=new LazyAdapter(this, getTraysList());        
        list.setAdapter(adapter);
	  }
	  
	  public ArrayList<HashMap<String, String>> getTraysList()
	  {
		    ArrayList<HashMap<String, String>> traysList = new ArrayList<HashMap<String, String>>();
		    
			XMLParser parser = new XMLParser();
			String xml = parser.getXmlFromUrl(URL); // getting XML from URL
			Document doc = parser.getDomElement(xml); // getting DOM element
			
			NodeList nl = doc.getElementsByTagName(KEY_TRAY);
			// looping through all song nodes <song>
			for (int i = 0; i < nl.getLength(); i++) {
				// creating new HashMap
				HashMap<String, String> map = new HashMap<String, String>();
				Element e = (Element) nl.item(i);
				
				// Check if the tray is in the store
				String trayID = parser.getValue(e, KEY_ID);
				if (allTrayIDset.contains(trayID))
				{
					// adding each child node to HashMap key => value
					map.put(KEY_TRAY, trayID);
					map.put(KEY_ID, parser.getValue(e, KEY_ID));
					map.put(KEY_DATE, parser.getValue(e, KEY_DATE));
					// change this to reflect change in matches
					int count = countMatchedString(mPrefs.getMatchedString(trayID));
					if (count >= 0) {
						map.put(KEY_NUMITEMS, String.valueOf(Integer.valueOf(parser.getValue(e, KEY_NUMITEMS)) - count));
					}
					else {
						map.put(KEY_NUMITEMS, parser.getValue(e, KEY_NUMITEMS));
					}
					// change this to reflect change in matches
					String currentURL = parser.getValue(e, KEY_THUMB_URL);					
					String pieChartURL = currentURL.substring(0,currentURL.lastIndexOf("_")-1) + String.valueOf(count)+"_5.png";
					map.put(KEY_THUMB_URL, pieChartURL);
					// adding HashList to ArrayList
					traysList.add(map);
				}
			}
		  
		  return traysList;
	  }
	  
	  
	  private int countMatchedString(String matchedString)
	  {
	    // sum up values in array to update TrayList
		int count = -1;
	    if (!matchedString.equals("-")) {
			count = 0;
		    for (int i = 0; i < matchedString.length(); i++)
		    {
		    	if (matchedString.charAt(i) == '1')
		    		count++;
		    }
	    }
	    return count;
	    
	  }
}
