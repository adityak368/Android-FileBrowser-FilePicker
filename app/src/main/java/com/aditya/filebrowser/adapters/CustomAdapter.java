package com.aditya.filebrowser.adapters;

import android.content.Context;
import android.support.transition.Visibility;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.aditya.filebrowser.Constants;
import com.aditya.filebrowser.FileResolution;
import com.aditya.filebrowser.R;
import com.aditya.filebrowser.models.FileItem;
import com.aditya.filebrowser.utils.AssortedUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    public void selectAll() {
        for(int i=0;i<fileList.size();i++) {
            fileList.get(i).setSelected(true);
        }
        notifyDataSetChanged();
    }

    public void selectItem(int position) {
        fileList.get(position).setSelected(true);
        notifyDataSetChanged();
    }

    public enum CHOICE_MODE {
        SINGLE_CHOICE,
        MULTI_CHOICE
    }

    private List<FileItem> fileList;
    private CHOICE_MODE currMode;
    private Context mContext;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView fileName;
        public TextView fileModified;
        public ImageView fileIcon;
        public CheckBox selectcb;

        public MyViewHolder(View view) {
            super(view);
            fileName = (TextView) view.findViewById(R.id.filename);
            fileModified = (TextView) view.findViewById(R.id.filemodifiedinfo);
            fileIcon = (ImageView) view.findViewById(R.id.file_icon);
            selectcb = (CheckBox) view.findViewById(R.id.selectFile);
        }
    }


    public CustomAdapter(List<FileItem> fileList,Context mContext) {
        this.fileList = fileList;
        this.currMode = CHOICE_MODE.SINGLE_CHOICE;
        this.mContext = mContext;
    }

    public void setChoiceMode(CHOICE_MODE mode) {
        this.currMode = mode;
        if(mode==CHOICE_MODE.SINGLE_CHOICE)
            for(FileItem item : fileList) {
                item.setSelected(false);
            }
    }

    public CHOICE_MODE getChoiceMode() {
        return this.currMode;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.file_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        File f = fileList.get(position).getFile();
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
            SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATE_FORMAT);
            String fileSize = "";
            if(AssortedUtils.GetPrefs(Constants.SHOW_FOLDER_SIZE,mContext).equalsIgnoreCase("true")) {
                if (f.isDirectory()) {
                    fileSize = FileUtils.byteCountToDisplaySize(FileUtils.sizeOfDirectory(f)) +  " | ";
                } else {
                    fileSize = FileUtils.byteCountToDisplaySize(FileUtils.sizeOf(f)) +  " | ";
                }
            }
            holder.fileModified.setText(fileSize + "Last Modified : " + formatter.format(d));
        } catch (Exception e) {

        }
        if(getChoiceMode()==CHOICE_MODE.MULTI_CHOICE) {
            holder.selectcb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    fileList.get(position).setSelected(isChecked);
                }
            });
            holder.selectcb.setChecked(fileList.get(position).isSelected());
        } else {
            holder.selectcb.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public List<FileItem> getSelectedItems() {
        List<FileItem> selectedItems = new ArrayList<FileItem>();
        if(getChoiceMode()==CHOICE_MODE.MULTI_CHOICE) {
            for(FileItem item : fileList) {
                if(item.isSelected())
                    selectedItems.add(item);
            }
        }
        return selectedItems;
    }
}