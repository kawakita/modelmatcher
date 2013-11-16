package com.CornellTech.ModelMatcher;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ModelAdapter extends BaseAdapter {
    
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader; 
    
    public ModelAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
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
            vi = inflater.inflate(R.layout.list_checkbox, null);

        TextView modelname = (TextView)vi.findViewById(R.id.modelname); 
        TextView dim = (TextView)vi.findViewById(R.id.dim); 
        TextView material = (TextView)vi.findViewById(R.id.material); 
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.list_image);
        CheckBox ch_box = (CheckBox)vi.findViewById(R.id.chbox);
        
        HashMap<String, String> model = new HashMap<String, String>();
        model = data.get(position);
        
        // Setting all values in listview
        modelname.setText(model.get(TrayView.KEY_ID));
        dim.setText(model.get(TrayView.KEY_DIM));
        material.setText(model.get(TrayView.KEY_MATERIAL));
        imageLoader.DisplayImage(model.get(TrayView.KEY_THUMB_URL), thumb_image);
        ch_box.setChecked(Boolean.valueOf(model.get(TrayView.KEY_CHECK)));
        return vi;
    }
}