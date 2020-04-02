package edu.buffalo.cse.cse486586.groupmessenger1;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLOutput;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */

        //ContentValues contains the "key"(file name) and "value"(text in the file)
        /*
        * Referenced documents and sites for the below code:
        *
        * https://developer.android.com/training/data-storage/files#java
        * https://developer.android.com/reference/android/content/ContentValues
        * https://developer.android.com/reference/android/content/Context
        *
        * */



        try {
            // get context of the instance
            Context c1 = this.getContext();
            // Referenced from PA1

            FileOutputStream outputStream = c1.openFileOutput(values.get("key").toString(), Context.MODE_PRIVATE);
            outputStream.write(values.get("value").toString().getBytes());
            outputStream.close();


        } catch (Exception e) {
            System.out.println("ContentProvider - Insert Function");
            e.printStackTrace();
        }

        Log.v("insert", values.toString());
        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */

        /*
        * Referenced sources include:
        *
        * https://developer.android.com/reference/android/content/Context#openFileInput(java.lang.String)
        * https://www.tutorialspoint.com/java/io/fileinputstream_read_byte.htm
        * https://medium.com/@xabaras/creating-a-cursor-from-a-list-with-matrixcursor-ab71877ecf2c
        * https://stackoverflow.com/questions/22629568/couldnt-load-memtrack-module-logcat-error
        *
        * */
        //System.out.println(selection);

        //Create a matrix cursor to query the data from the device storage.
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"key","value"});

        try {
            //get context of the instance
            Context c2 = this.getContext();
            FileInputStream inputStream = c2.openFileInput(selection);


            /*
            This did not work!

            InputStreamReader is = new InputStreamReader(inputStream);
            BufferedReader vr = new BufferedReader(is);
            StringBuffer sb = new StringBuffer();
            String lines;
            while((lines = vr.readLine())!=null){
                sb.append(lines);

            }
            String data1 = sb.toString();*/


            /*This worked!!!!!!*/
            byte[] fileData = new byte[100];
            int c=inputStream.read(fileData);


            String data = "";
            for(byte b:fileData){
                data+=(char)b;
            }
            //System.out.println(data);

            data=data.substring(0,c);

            MatrixCursor.RowBuilder row = matrixCursor.newRow();

            row.add("key", selection);
            row.add("value", data);


            inputStream.close();
            //matrixCursor.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }


        Log.v("query", selection);
        return matrixCursor;
    }
}
