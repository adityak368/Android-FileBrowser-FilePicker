package com.aditya.filebrowser.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aditya.filebrowser.FileResolution;
import com.aditya.filebrowser.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private List<File> fileList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView fileName;
        public TextView fileModified;
        public ImageView fileIcon;

        public MyViewHolder(View view) {
            super(view);
            fileName = (TextView) view.findViewById(R.id.filename);
            fileModified = (TextView) view.findViewById(R.id.filemodifiedinfo);
            fileIcon = (ImageView) view.findViewById(R.id.file_icon);
        }
    }


    public CustomAdapter(List<File> fileList) {
        this.fileList = fileList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.file_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        File f = fileList.get(position);
        holder.fileIcon.setImageResource(FileResolution.getFileIcon(f));
        int length = 0;
        String children = "";
        if(f.isDirectory()) {
            if(f.listFiles()!=null)
                length = f.listFiles().length;
            children = " (" +length + ")";
        }

        holder.fileName.setText(f.getName() + children);
        try {
            Date d = new Date(f.lastModified());
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            holder.fileModified.setText("Last Modified : " + formatter.format(d));
        } catch (Exception e) {

        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }
}