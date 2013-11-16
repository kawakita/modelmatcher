package com.CornellTech.ModelMatcher;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
public class TrayStore {
	  // The SharedPreferences object in which trays are stored
	  private final SharedPreferences mPrefs;

	  // The name of the resulting SharedPreferences
	  private static final String SHARED_PREFERENCE_NAME =
	      TrayEntry.class.getSimpleName();

	  // Create the SharedPreferences storage with private access only
	  public TrayStore(Context context) {
	    mPrefs =
	        context.getSharedPreferences(
	            SHARED_PREFERENCE_NAME,
	            Context.MODE_PRIVATE);   
	  }

	  /**
	   * Returns a stored tray by its id, or returns {@code null}
	   * if it's not found.
	   */
	  public Tray getTray(String id) {

	    String name = mPrefs.getString(
	        getTrayFieldKey(id, TrayUtils.KEY_NAME),
	        TrayUtils.INVALID_STRING_VALUE);

	    double datetime = mPrefs.getFloat(
	        getTrayFieldKey(id, TrayUtils.KEY_DATETIME),
	        TrayUtils.INVALID_FLOAT_VALUE);

	    int n = mPrefs.getInt(
	        getTrayFieldKey(id, TrayUtils.KEY_N_MODEL_FILES),
	        TrayUtils.INVALID_INT_VALUE);

	    // If none of the values is incorrect, return the object
	    if (
	        name != TrayUtils.INVALID_STRING_VALUE &&
	        datetime != TrayUtils.INVALID_FLOAT_VALUE &&
	        n != TrayUtils.INVALID_INT_VALUE) {

	      // Return a true tray object
	      return new Tray(name, datetime, n);

	      // Otherwise, return null.
	    } else {
	      return null;
	    }
	  }

	  /**
	   * Save a tray.
	   */
	  public void setTray(String id, Tray tray) {

	    /*
	     * Get a SharedPreferences editor instance. Among other
	     * things, SharedPreferences ensures that updates are atomic
	     * and non-concurrent
	     */
	    Editor editor = mPrefs.edit();

	    // Write the tray values to SharedPreferences
	    editor.putString(
	            getTrayFieldKey(id, TrayUtils.KEY_NAME),
	            tray.getName());    
	    editor.putFloat(
	    		getTrayFieldKey(id, TrayUtils.KEY_DATETIME),
	        (float) tray.getDatetime());

	    editor.putInt(
	    		getTrayFieldKey(id, TrayUtils.KEY_N_MODEL_FILES),
	        (int) tray.getN());

	    // Add tray ID to keep track of registered trays.
	    Set<String> tray_IDs = getAllRegisteredTrayIDs();
	    tray_IDs.add(id);
	    editor.putStringSet(TrayUtils.KEY_IDS, tray_IDs);
	    Log.d(TrayUtils.APPTAG, Arrays.toString(tray_IDs.toArray()));

	    // Commit the changes
	    editor.commit();
	  }

	  public void clearTray(String id) {

	    // Remove a flattened tray object from storage by removing all of its keys
	      Editor editor = mPrefs.edit();
	      editor.remove(getTrayFieldKey(id, TrayUtils.KEY_NAME));
	      editor.remove(getTrayFieldKey(id, TrayUtils.KEY_DATETIME));
	      editor.remove(getTrayFieldKey(id, TrayUtils.KEY_N_MODEL_FILES));

	      // Remove tray ID to keep track of registered trays.
	      Set<String> tray_IDs = getAllRegisteredTrayIDs();
	      tray_IDs.remove(id);
	      editor.putStringSet(TrayUtils.KEY_IDS, tray_IDs);
	      Log.d(TrayUtils.APPTAG, Arrays.toString(tray_IDs.toArray()));

	      editor.commit();
	  }

	  public Set<String> getAllRegisteredTrayIDs() {
		Set<String> ids = new HashSet<String>();
		ids = mPrefs.getStringSet(TrayUtils.KEY_IDS, ids);
	    return ids;
	  }  

	  /**
	   * Given a Trays object's ID and the name of a field, return the key name of the
	   * object's values in SharedPreferences.
	   */

	  private String getTrayFieldKey(String id, String fieldName) {

	    return
	        TrayUtils.KEY_PREFIX +
	        id +
	        "_" +
	        fieldName;
	  }
	
	  public void setMatchedString(String id, String matchedString) {
		  Editor editor = mPrefs.edit();
		  editor.putString(id,matchedString);
		  editor.commit();
	  }
	  
	  public String getMatchedString(String id) {
		  String matchedString = mPrefs.getString(id, "-");
		  return matchedString;
	  }
	  
	  public void removeMatchedString(String id) {
		  Editor editor = mPrefs.edit();
		  editor.remove(id);
		  editor.commit();
	  }
}
