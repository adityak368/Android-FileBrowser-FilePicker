//package com.aditya.filebrowser;
//
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.os.AsyncTask;
//import android.widget.Toast;
//
//import com.Adityak.test2.AesEncrypter;
//import com.Adityak.test2.Constants;
//import com.Adityak.test2.utils.Filename_old;
//import com.Adityak.test2.utils.ZipFiles;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//
//import javax.crypto.SecretKey;
//import javax.crypto.spec.SecretKeySpec;
//
///**
// * Created by adik on 10/18/2015.
// */
//public class FileUtils {
//
//
//    static public boolean deleteDirectory(File path) {
//        if (path.exists()) {
//            File[] files = path.listFiles();
//            if (files == null) {
//                return true;
//            }
//            for (int i = 0; i < files.length; i++) {
//                if (files[i].isDirectory()) {
//                    deleteDirectory(files[i]);
//                } else {
//                    files[i].delete();
//                }
//            }
//        }
//        return (path.delete());
//    }
//
//    // If targetLocation does not exist, it will be created.
//    public static void copyDirectory(File sourceLocation, File targetLocation)
//            throws IOException {
//
//        if (sourceLocation.isDirectory()) {
//            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
//                throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
//            }
//
//            String[] children = sourceLocation.list();
//            for (int i = 0; i < children.length; i++) {
//                copyDirectory(new File(sourceLocation, children[i]),
//                        new File(targetLocation, children[i]));
//            }
//        } else {
//
//            // make sure the directory we plan to store the recording in exists
//            File directory = targetLocation.getParentFile();
//            if (directory != null && !directory.exists() && !directory.mkdirs()) {
//                throw new IOException("Cannot create dir " + directory.getAbsolutePath());
//            }
//
//            InputStream in = new FileInputStream(sourceLocation);
//            OutputStream out = new FileOutputStream(targetLocation);
//
//            // Copy the bits from instream to outstream
//            byte[] buf = new byte[1024];
//            int len;
//            while ((len = in.read(buf)) > 0) {
//                out.write(buf, 0, len);
//            }
//            in.close();
//            out.close();
//        }
//    }
//
//
//    public class Movetask extends AsyncTask<String , Void, Integer> {
//
//        ProgressDialog pDialog;
//        Context con;
//        String destination;
//
//        public Movetask(Context context,String dest)
//        {
//            con = context;
//            destination = dest;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            // super.onPreExecute();
//            pDialog = new ProgressDialog(con);
//            pDialog.setMessage("Please Wait....");
//            pDialog.setCancelable(false);
//            pDialog.show();
//        }
//
//        @Override
//        protected Integer doInBackground(String... params) {
//            // TODO Auto-generated method stub
//
//            ArrayList<String> files = Operations.getInstance().getFiles();
//            for (int i = 0; i < files.size(); i++) {
//                File source = new File(files.get(i));
//                try {
//                    FileUtils.copyDirectory(source, new File(destination + "/" + source.getName()));
//                    if(source.isDirectory())
//                        FileUtils.deleteDirectory(source);
//                    else
//                        source.delete();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            Operations.getInstance().setAction(Constants.NULL);
//
//            return 1;
//        }
//
//
//        protected void onPostExecute(Integer progress) {
//            // dismiss the dialog once done
//            if(pDialog!=null)
//                if(pDialog.isShowing())
//                    pDialog.dismiss();
//
//        }
//    }
//
//    public class Copytask extends AsyncTask<String , Void, Integer>{
//
//        ProgressDialog pDialog;
//        Context con;
//        String destination;
//
//        public Copytask(Context context,String dest)
//        {
//            con = context;
//            destination = dest;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            // super.onPreExecute();
//            pDialog = new ProgressDialog(con);
//            pDialog.setMessage("Please Wait....");
//            pDialog.setCancelable(false);
//            pDialog.show();
//        }
//
//        @Override
//        protected Integer doInBackground(String... params) {
//            // TODO Auto-generated method stub
//            ArrayList<String> files = Operations.getInstance().getFiles();
//
//            for (int i = 0; i < files.size(); i++) {
//                File source = new File(files.get(i));
//                try {
//                    FileUtils.copyDirectory(source, new File(destination+ "/" + source.getName()));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            Operations.getInstance().setAction(Constants.NULL);
//
//            return 1;
//        }
//
//
//        protected void onPostExecute(Integer progress) {
//            // dismiss the dialog once done
//            if(pDialog!=null)
//                if(pDialog.isShowing())
//                    pDialog.dismiss();
//
//        }
//    }
//
//    public class Deletetask extends AsyncTask<String, Void, Integer> {
//
//        ProgressDialog pDialog;
//        Context con;
//        String source;
//
//        public Deletetask(Context context, String sourc)
//        {
//            con = context;
//            source = sourc;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            // super.onPreExecute();
//            pDialog = new ProgressDialog(con);
//            pDialog.setMessage("Please Wait....");
//            pDialog.show();
//        }
//
//        @Override
//        protected Integer doInBackground(String... params) {
//            // TODO Auto-generated method stub
//            FileUtils.deleteDirectory(new File(source));
//            return 1;
//        }
//
//
//        protected void onPostExecute(Integer progress) {
//            // dismiss the dialog once done
//            if(pDialog!=null)
//                if(pDialog.isShowing())
//                    pDialog.dismiss();
//        }
//
//    }
//
//    public static void initDir()
//    {
//        String DIRS[] = {Constants.APPDIR,Constants.LOCKEDDIR, Constants.UNLOCKEDDIR};
//        for(int i=0;i<DIRS.length;i++) {
//            File f = new File(DIRS[i]);
//            if (!f.isDirectory()) {
//                f.mkdir();
//            }
//        }
//    }
//
//    public class Lock extends AsyncTask<String, Void, Integer> {
//
//        boolean status = true;
////        ProgressDialog pDialog;
//        Context con;
//        String destination;
//        ArrayList<File> filesToZip;
//
//        public Lock(Context context,String dest,ArrayList<File> toZip)
//        {
//            con = context;
//            destination = dest;
//            filesToZip = toZip;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            // super.onPreExecute();
////            pDialog = new ProgressDialog(con);
////            pDialog.setMessage("Please Wait....");
////            pDialog.setCancelable(false);
////            pDialog.show();
//            Toast.makeText(con, "File(s) Are Getting Locked...", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        protected Integer doInBackground(String... params) {
//            // TODO Auto-generated method stub
//
//            //File f = new File(params[0]);
//            //Filename file = new Filename(f.getName(), '/', '.');
//            //AesEncrypter encrypter;
//            //encrypter = new AesEncrypter(generate(Constants.ENCPASSWORD));
//            FileUtils.initDir();
//            //encrypter.encrypt(new FileInputStream(params[0]), new FileOutputStream(Constants.LOCKEDDIR + f.getName()));
//            status = ZipFiles.zipFiles(Constants.ENCPASSWORD, filesToZip, destination);
//            //f.delete();
//            return 1;
//        }
//
//
//        protected void onPostExecute(Integer progress) {
//            // dismiss the dialog once done
////            if(pDialog!=null)
////                if(pDialog.isShowing())
////                    pDialog.dismiss();
//            if (status) {
//                Toast.makeText(con, "File(s) Locked Successfully", Toast.LENGTH_SHORT).show();
//            }
//            else
//            {
//                Toast.makeText(con, "Could Not Complete Operation!", Toast.LENGTH_SHORT).show();
//            }
//        }
//
//    }
//
//
//
//    public class Unlock extends AsyncTask<String, Void, Integer> {
//
////        ProgressDialog pDialog;
//        Context con;
//        String destination;
//        ArrayList<File> filesToUnZip;
//        boolean status = true;
//
//        public Unlock(Context context,String dest,ArrayList<File> toUnZip)
//        {
//            con = context;
//            destination = dest;
//            filesToUnZip = toUnZip;
//        }
//        @Override
//        protected void onPreExecute() {
//            // super.onPreExecute();
////            pDialog = new ProgressDialog(MainActivityUnlock.this);
////            pDialog.setMessage("Please Wait....");
////            pDialog.show();
//            Toast.makeText(con, "File(s) Are Getting Unlocked...", Toast.LENGTH_SHORT).show();
//
//        }
//
//        @Override
//        protected Integer doInBackground(String... params) {
//            // TODO Auto-generated method stub
//            FileUtils.initDir();
//            for(int i=0;i<filesToUnZip.size();i++)
//            {
//                Filename in = new Filename(filesToUnZip.get(i).getAbsolutePath(),'/','.');
//                if(in.extension().equals("enc") || in.extension().equals("noext"))
//                {
//                    String path = filesToUnZip.get(i).getAbsolutePath();
//                    Filename_old file = new Filename_old(path,'/' , '.');
//                    Filename_old outputfile = new Filename_old(file.filename(), '/', '.');
//                    String ext = file.type();
//                    AesEncrypter encrypter;
//                    try {
//                        FileUtils.initDir();
//                        encrypter = new AesEncrypter(generate(Constants.ENCPASSWORD));
//                        if(ext.equals("noext")){
//                            encrypter.decrypt(new FileInputStream(path),new FileOutputStream(Constants.UNLOCKEDDIR+outputfile.getunlockfilename()));
//                        }
//                        else
//                            encrypter.decrypt(new FileInputStream(path),new FileOutputStream(Constants.UNLOCKEDDIR+outputfile.getunlockfilename()+"."+file.type()));
//                        //Log.d("Adi", "/sdcard/Lock My Files Data/Unlocked/"+file.getunlockfilename()+"."+file.type());
//                        //f.delete();
//                    } catch (UnsupportedEncodingException e1) {
//                        // TODO Auto-generated catch block
//                        e1.printStackTrace();
//                    }
//                    catch (FileNotFoundException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//
//                }
//                else if(in.extension().equals("zip"))
//                {
//                    ArrayList<File> tounzip = new ArrayList<File>();
//                    tounzip.add(filesToUnZip.get(i));
//                    status = ZipFiles.unzipFiles(Constants.ENCPASSWORD,tounzip,destination);
//                }
//            }
//
//
//            return 1;
//        }
//
//
//        protected void onPostExecute(Integer progress) {
//            // dismiss the dialog once done
//            if (status) {
//                Toast.makeText(con, "File(s) Unlocked Successfully", Toast.LENGTH_SHORT).show();
//            }
//            else
//            {
//                Toast.makeText(con, "Could Not Complete Operation!", Toast.LENGTH_SHORT).show();
//            }
//        }
//
//    }
//
//
//
//    SecretKey generate(String pass) throws UnsupportedEncodingException {
//
//        byte[] key = (pass).getBytes("UTF-8");
//        int oldSize = java.lang.reflect.Array.getLength(key);
//        Class elementType = key.getClass().getComponentType();
//        Object newArray = java.lang.reflect.Array.newInstance(
//                elementType, 16);
//        int preserveLength = Math.min(oldSize, 16);
//        if (preserveLength > 0)
//            System.arraycopy(key, 0, newArray, 0, preserveLength);
//
//        SecretKey key1 = new SecretKeySpec((byte[]) newArray, "AES");
//
//        return key1;
//    }
//
//}
//
