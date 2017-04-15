//package com.aditya.filebrowser;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.DialogInterface;
//import android.content.DialogInterface.OnClickListener;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import android.util.DisplayMetrics;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.CheckBox;
//import android.widget.CompoundButton;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.Adityak.test2.utils.UIUtils;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.util.ArrayList;
//import java.util.List;
//
//public class MultiSelectList extends AppCompatActivity {
//    Toolbar toolbar;
//    ArrayAdapter<Model> adapter;
//    List<Model> list;
//    ListView lv;
//    /**
//     * Called when the activity is first created.
//     */
//
//    public void onCreate(Bundle icicle) {
//        super.onCreate(icicle);
//        setContentView(R.layout.multipleselect);
//        toolbar = (Toolbar) findViewById(R.id.tool_bar);
//        setSupportActionBar(toolbar);
//        lv = (ListView) findViewById(R.id.multipleselectlist);
//        adapter = new InteractiveArrayAdapter(this,
//                getModel());
//        lv.setAdapter(adapter);
//    }
//
//    private List<Model> getModel() {
//        list = new ArrayList<Model>();
//
//
//        File f = new File(getIntent().getExtras().getString("Path"));
//
//        File[] files = f.listFiles();
//
//        list.clear();
//
//        if (files != null) {
//
//            for (int i = 0; i < files.length; i++) {
//                if (files[i].isDirectory())
//                    continue;
//                else {
//                    list.add(new Model(files[i].getName(),files[i].getAbsolutePath()));
//                }
//            }
//            if (list.size() == 0) {
//                Toast.makeText(getApplicationContext(), "No Files In This Directory", Toast.LENGTH_SHORT).show();
//                onBackPressed();
//            }
//
//        }
//
//        //adapter.notifyDataSetChanged();
//
//
//        // Initially select one of the items
//        //list.get(1).setSelected(true);
//        return list;
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.checkbox_menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        if (item.getItemId() == R.id.cbexit) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(MultiSelectList.this);
//            builder.setMessage("Are you sure you want to exit?");
//            builder.setCancelable(false);
//            builder.setPositiveButton("Yes", new OnClickListener() {
//
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    // TODO Auto-generated method stub
//
//                    Intent intent = new Intent(MultiSelectList.this, com.aditya.filebrowser.filemanager.MainActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    intent.putExtra("EXIT", true);
//                    startActivity(intent);
//                    finish();
//                }
//            });
//
//            builder.setNegativeButton("No", new OnClickListener() {
//
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    // TODO Auto-generated method stub
//                    dialog.cancel();
//                }
//            });
//
//            builder.show();
//        }
//
//
//        if (item.getItemId() == R.id.cbdelete) {
//            for (int i = 0; i < list.size(); i++) {
//
//                if (list.get(i).isSelected()) {
//                    File source = new File(list.get(i).getPath());
//                    source.delete();
//                }
//
//            }
//            finish();
//            UIUtils.ShowToast("File(s) Deleted Successfully!", MultiSelectList.this);
//        }
//        if (item.getItemId() == R.id.cbmove) {
//
//            Operations.getInstance().setAction(Constants.MOVE);
//            Operations.getInstance().resetFiles();
//            for(int i=0;i<list.size();i++)
//            {
//                if(list.get(i).isSelected())
//                {
//                    Operations.getInstance().addFile(list.get(i).getPath());
//                }
//            }
//            UIUtils.ShowToast("File(s) Saved To Clipboard", MultiSelectList.this);
//        }
//        if (item.getItemId() == R.id.cbcopy) {
//
//            Operations.getInstance().setAction(Constants.COPY);
//            Operations.getInstance().resetFiles();
//            for(int i=0;i<list.size();i++)
//            {
//                if(list.get(i).isSelected())
//                {
//                    Operations.getInstance().addFile(list.get(i).getPath());
//                }
//            }
//            UIUtils.ShowToast("File(s)Saved To Clipboard", MultiSelectList.this);
//        }
//        if (item.getItemId() == R.id.cbselectall) {
//            for (int i = 0; i < list.size(); i++) {
//                list.get(i).setSelected(true);
//            }
//            adapter.notifyDataSetChanged();
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    class InteractiveArrayAdapter extends ArrayAdapter<Model> {
//
//        private final List<Model> list;
//        private final Activity context;
//
//        public InteractiveArrayAdapter(Activity context, List<Model> list) {
//            super(context, R.layout.multipleselectlistitem, list);
//            this.context = context;
//            this.list = list;
//        }
//
//        class ViewHolder {
//            protected TextView text;
//            protected CheckBox checkbox;
//            protected ImageView imageview;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            View view = null;
//            if (convertView == null) {
//                LayoutInflater inflator = context.getLayoutInflater();
//                view = inflator.inflate(R.layout.multipleselectlistitem, null);
//                final ViewHolder viewHolder = new ViewHolder();
//                viewHolder.text = (TextView) view.findViewById(R.id.label);
//                viewHolder.checkbox = (CheckBox) view.findViewById(R.id.check);
//                viewHolder.imageview = (ImageView) view.findViewById(R.id.checkboxiv);
//                viewHolder.checkbox
//                        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//                            @Override
//                            public void onCheckedChanged(CompoundButton buttonView,
//                                                         boolean isChecked) {
//                                Model element = (Model) viewHolder.checkbox
//                                        .getTag();
//                                element.setSelected(buttonView.isChecked());
//
//                            }
//                        });
//                view.setTag(viewHolder);
//                viewHolder.checkbox.setTag(list.get(position));
//            } else {
//                view = convertView;
//                ((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
//            }
//            ViewHolder holder = (ViewHolder) view.getTag();
//            holder.text.setText(list.get(position).getName());
//            holder.checkbox.setChecked(list.get(position).isSelected());
//
//
//            Filename file = new Filename(list.get(position).getName(), '/', '.');
//
//            if (file.extension().equals("jpg") || file.extension().equals("bmp") || file.extension().equals("png") || file.extension().equals("gif")) {
//
//                //fileicon.setImageResource(R.drawable.image_icon);
//                File f = new File(getIntent().getExtras().getString("Path") + "/" + list.get(position).getName());
//                try {
//                    int THUMBNAIL_SIZE = 32;
//                    switch (getResources().getDisplayMetrics().densityDpi) {
//                        case DisplayMetrics.DENSITY_LOW:
//                            THUMBNAIL_SIZE = 32;
//                            break;
//                        case DisplayMetrics.DENSITY_MEDIUM:
//                            THUMBNAIL_SIZE = 48;
//                            break;
//                        case DisplayMetrics.DENSITY_HIGH:
//                            THUMBNAIL_SIZE = 64;
//                            break;
//                        default:
//                            THUMBNAIL_SIZE = 48;
//                            break;
//                    }
//
//                    FileInputStream fis = new FileInputStream(f);
//                    Bitmap imageBitmap = BitmapFactory.decodeStream(fis);
//
//                    imageBitmap = Bitmap.createScaledBitmap(imageBitmap, THUMBNAIL_SIZE, THUMBNAIL_SIZE, false);
//
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                    Log.d("ADI", "asdasd");
//                    holder.imageview.setImageBitmap(imageBitmap);
//
//
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//
//
//            } else if (file.extension().equals("mp3") || file.extension().equals("wav") || file.extension().equals("wma") || file.extension().equals("aac") || file.extension().equals("mid")) {
//                holder.imageview.setImageResource(R.drawable.music_icon);
//            } else if (file.extension().equals("3gp") || file.extension().equals("3g2") || file.extension().equals("avi") || file.extension().equals("flv") || file.extension().equals("mov") || file.extension().equals("mp4") || file.extension().equals("mpg") || file.extension().equals("rm") || file.extension().equals("vob") || file.extension().equals("wmv")) {
//                holder.imageview.setImageResource(R.drawable.videos_icon);
//            } else if (file.extension().equals("pdf")) {
//                holder.imageview.setImageResource(R.drawable.adobe_document_icon);
//            } else if (file.extension().equals("apk")) {
//                holder.imageview.setImageResource(R.drawable.ic_launcher);
//            } else if (file.extension().equals("txt")) {
//                holder.imageview.setImageResource(R.drawable.txt_icon);
//            } else if (file.extension().equals("html") || file.extension().equals("htm") || file.extension().equals("php")) {
//                holder.imageview.setImageResource(R.drawable.internet_icon);
//            } else if (file.extension().equals("zip")) {
//                holder.imageview.setImageResource(R.drawable.compressed_file_zip_icon);
//            } else if (file.extension().equals("rar")) {
//                holder.imageview.setImageResource(R.drawable.compressed_file_rar_icon);
//            } else {
//                holder.imageview.setImageResource(R.drawable.document_blank_icon);
//            }
//            return view;
//        }
//    }
//
//    class Model {
//
//        private String name;
//        private String path;
//        private boolean selected;
//
//        public Model(String name,String path) {
//            this.name = name;
//            this.path = path;
//            selected = false;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public boolean isSelected() {
//            return selected;
//        }
//
//        public void setSelected(boolean selected) {
//            this.selected = selected;
//        }
//
//        public String getPath() {
//            return path;
//        }
//
//        public void setPath(String path) {
//            this.path = path;
//        }
//    }
//
//}
//
