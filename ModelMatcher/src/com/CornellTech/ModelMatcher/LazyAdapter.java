package com.CornellTech.ModelMatcher;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LazyAdapter extends BaseAdapter {
    
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader; 
    
    public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.list_row, null);

        TextView trayname = (TextView)vi.findViewById(R.id.trayname); 
        TextView date = (TextView)vi.findViewById(R.id.date); 
        TextView numitems = (TextView)vi.findViewById(R.id.numitems); 
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.list_image);
        
        HashMap<String, String> tray = new HashMap<String, String>();
        tray = data.get(position);
        
        // Setting all values in listview
        trayname.setText(tray.get(TrayList.KEY_ID));
        date.setText(tray.get(TrayList.KEY_DATE));
        numitems.setText(tray.get(TrayList.KEY_NUMITEMS));
        imageLoader.DisplayImage(tray.get(TrayList.KEY_THUMB_URL), thumb_image);
        return vi;
    }
}