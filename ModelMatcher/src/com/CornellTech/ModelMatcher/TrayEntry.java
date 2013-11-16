package com.CornellTech.ModelMatcher;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import android.app.Activity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
public class TrayEntry extends Activity {

	  // Persistent storage for trays
	  private TrayStore mPrefs;
	  
	  // Store a list of trays to add
	  List<Tray> mCurrentTrays;	  

	  //Handle to name entry in the UI
	  private EditText mName;
	  private Tray mUITray;
	  
	  private String mTrayID;
	  private static final String KEY_MODEL = "model";
	  private static final String KEY_MATCH_URLS = "match_urls";
	  private static final String KEY_THUMB_URL = "thumb_url";
	  private HttpURLConnectionExample http = new HttpURLConnectionExample();
	  private String trayURL = "";
	  private String baseURL = "http://t-odd.com";
	  
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);

		    // Instantiate a new tray storage area
		    mPrefs = new TrayStore(this);

		    // Instantiate the current List of trays
		    mCurrentTrays = new ArrayList<Tray>();

		    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); 
		    
		    // Attach to the main UI
		    setContentView(R.layout.activity_tray_entry);

		    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		    
		    // Get handles to the editor fields in the UI
		    mName = (EditText) findViewById(R.id.value_name);
	  }
	  
	  @Override
	  protected void onResume() {
	    super.onResume();

	    String TrayId = getIntent().getStringExtra(TrayUtils.TRAY_ID); 
	    mUITray = mPrefs.getTray(TrayId);
	    
	    if (mUITray != null) {

	      mName.setText(mUITray.getName());
	      mTrayID = mName.getText().toString();
	      ImageView image = (ImageView) findViewById(R.id.image_view);;	      
	      Bitmap bmp = BitmapFactory.decodeFile(TrayUtils.STORAGE_PATH + mTrayID + TrayUtils.CURRENT_PIC);
	      image.setImageBitmap(bmp);	      
	    } else {

	    }
	  }
	  
	  @Override
	  public boolean onCreateOptionsMenu(Menu menu) {
	    // MenuInflater inflater = getMenuInflater();
	    // inflater.inflate(R.menu.menu, menu);
	    return false;
	  }

	  /*
	   * Save the current  settings in SharedPreferences.
	   */
	  @Override
	  protected void onPause() {
	    super.onPause();
	  }	  
	  
      Thread thread = new Thread(){
      @Override
      public void run() {
    	  try {
    		  Thread.sleep(700); // As I am using LENGTH_LONG in Toast
              TrayEntry.this.finish();
          } catch (Exception e) {
              e.printStackTrace();
          }
        }  
      };
	  
	  public void onRegisterClicked(View view) {
		  mTrayID = mName.getText().toString();
		  
		  /* adding */
		  // Check that this tray exists in xml file containing active trays
		  
		  final String URL = "http://t-odd.com/trays.xml";
		  // XML node keys
		  final String KEY_TRAY = "tray"; // parent node
		  final String KEY_ID = "id";
		  final String KEY_NUMITEMS = "numitems";
		  
		  ArrayList<HashMap<String, String>> traysList = new ArrayList<HashMap<String, String>>();
		    
		  XMLParser parser = new XMLParser();
		  String xml = parser.getXmlFromUrl(URL); // getting XML from URL
		  Document doc = parser.getDomElement(xml); // getting DOM element
			
		  NodeList nl = doc.getElementsByTagName(KEY_TRAY);
		  // looping through all tray nodes <tray>
		  for (int i = 0; i < nl.getLength(); i++) {
			  // creating new HashMap
			  HashMap<String, String> map = new HashMap<String, String>();
			  Element e = (Element) nl.item(i);
			  // adding each child node to HashMap key => value
			  map.put(KEY_TRAY, parser.getValue(e, KEY_TRAY));
			  map.put(KEY_ID, parser.getValue(e, KEY_ID));
	
			  // adding HashMap to ArrayList
			  traysList.add(map);
		  }
		  
		  boolean foundTray = false;
		  for (HashMap<String, String> tray : traysList)
		  {
			  String trayID = tray.get(KEY_ID);
			  if (mTrayID.equals(trayID))
			  {
				  foundTray = true;
				  break;
			  }
		  }
		  
		  EditText trayID = (EditText) findViewById(R.id.value_name);
		  if (!foundTray)
		  {
			  Toast.makeText(getApplicationContext(), "Invalid tray ID", Toast.LENGTH_SHORT).show();
			  //trayID.requestFocus();
		  }
		  else
		  {
		  /* end adding */
		  
			  mUITray = new Tray(mTrayID, (double) System.currentTimeMillis(), 1);
	
			  // Store this flat version in SharedPreferences
			  mPrefs.setTray(mName.getText().toString(), mUITray);
			  boolean success = (new File(TrayUtils.STORAGE_PATH + mTrayID)).mkdirs();
			  Toast.makeText(getApplicationContext(), "Tray added", 1000).show();
			  
			  trayURL = baseURL + "/" + mTrayID + ".xml";
			  getModelURLsList();
			  thread.start();
			  // added	  
		  }
	  }	  
	  
	  public void onTakePicClicked(View view) {
		 mTrayID = mName.getText().toString();
      	 Intent i = new Intent(getApplicationContext(), ModelMatcher.class);
      	 i.putExtra(TrayUtils.TRAY_ID, mTrayID);
      	 startActivity(i);    		  
	  }	  	  

	  private int getNModelFiles(String trayDir) {
		  File saveDir = new File(trayDir);
		  if(saveDir.exists()) {
			  return 1;
		  }
		  else return 0;
	  }
	  
	  //TODO: change to access server
	  private void saveImageToAndroid(String modelName, String url) {
		String fname = url.substring(url.lastIndexOf("/")+1);
		System.out.println(fname);
		String outfile = TrayUtils.STORAGE_PATH + mTrayID + "/" + modelName + "/" + fname;
		if ((new File(TrayUtils.STORAGE_PATH + mTrayID + "/" + modelName).exists())) System.out.println(outfile);
		byte[] array;
		try {
			array = http.getFile(url);
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(outfile);
				fos.write(array);
				fos.close();					
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	  }
	 
	  
	  public void getModelURLsList()
	  {		System.out.println(trayURL);
			XMLParser parser = new XMLParser();
			String xml = parser.getXmlFromUrl(trayURL); // getting XML from URL
			System.out.println(xml);
			
			Document doc = parser.getDomElement(xml); // getting DOM element
			
			NodeList nl = doc.getElementsByTagName(KEY_MODEL);
			
			// looping through all song nodes <song>
			for (int i = 0; i < nl.getLength(); i++) {
				
				Element e = (Element) nl.item(i);
				
				String thumb_url = parser.getValue(e, KEY_THUMB_URL);
				String match_urls = parser.getValue(e, KEY_MATCH_URLS);
				
				String model_name = thumb_url.substring(thumb_url.lastIndexOf("/")+1,thumb_url.lastIndexOf("."));
				boolean folder_created = (new File(TrayUtils.STORAGE_PATH + mTrayID + "/" + model_name)).mkdirs();
				
				thumb_url = thumb_url.substring(0, thumb_url.lastIndexOf("/")+1);
				String[] match_urls_arr = match_urls.split(",");
				for (String match_url : match_urls_arr)
				{
					System.out.println(model_name);
					System.out.println(thumb_url + model_name + "/" + match_url);
					saveImageToAndroid(model_name, thumb_url + model_name + "/" + match_url);
				}

			}
	  }	  

}
