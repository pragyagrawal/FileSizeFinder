package com.binarybricks.pragya.filesizefinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by PRAGYA on 9/13/2016.
 */
public class TopFileListAdapter extends ArrayAdapter<FileProperties> {

    public TopFileListAdapter(Context context, List<FileProperties> fileProperties) {
        super(context,0, fileProperties);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        FileProperties fileProperties = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.top_biggest_file_list_item, parent, false);
        }

        // Lookup view for data population
        TextView tvFileName = (TextView) convertView.findViewById(R.id.tvFileName);
        TextView tvFileSize = (TextView) convertView.findViewById(R.id.tvFileSize);
        // Populate the data into the template view using the data object
        tvFileName.setText(fileProperties.getFileName());
        tvFileSize.setText(String.valueOf(fileProperties.getFileSize()/(1024*1024))+" MB");
        // Return the completed view to render on screen
        return convertView;
    }
}
