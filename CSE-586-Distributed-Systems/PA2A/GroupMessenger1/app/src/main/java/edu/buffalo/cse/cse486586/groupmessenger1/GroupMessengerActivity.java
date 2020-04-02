package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    static final String TAG = GroupMessengerActivity.class.getSimpleName();

    // Referenced format from PA1
    static final int REMOTE_PORT_AVD0 = 11108;
    static final int REMOTE_PORT_AVD1 = 11112;
    static final int REMOTE_PORT_AVD2 = 11116;
    static final int REMOTE_PORT_AVD3 = 11120;
    static final int REMOTE_PORT_AVD4 = 11124;
    static final int SERVER_PORT = 10000;

    // Sequence number for file name
    static int SEQ_NUM_FNAME=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */

        // Referenced from PA1
        // Added permission in AndroidManifest.xml
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);


        String portStr = telephonyManager.getLine1Number().substring(telephonyManager.getLine1Number().length() - 4);


        final String port = String.valueOf((Integer.parseInt(portStr) * 2));

        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {

            Log.e(TAG, "Can't create a ServerSocket");
        }




        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */


        // Referenced from PA1 - keyDown Action Listener
        // Send button on click action listener
        Button b1 = (Button)findViewById(R.id.button4);
        b1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                EditText e1 = (EditText) findViewById(R.id.editText1);
                String message = e1.getText().toString();
                e1.setText("");
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, message, port);
            }
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    // Referenced from PA1
    // Created server and client tasks which extend AsyncTask
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            Context con1 = getBaseContext();

            //Referenced from PA1

            /*
             * Referenced documents for the code include:
             *
             * Official Oracle Socket Communications documentation:
             * 1. https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html
             * 2. https://www.oracle.com/technetwork/java/socket-140484.html
             *
             * Android Programming Tutorials: https://developer.android.com/guide/
             *
             * AsyncTask Documentation: https://developer.android.com/reference/android/os/AsyncTask
             *
             * Standard InputStreamReader, BufferedReader and PrintWriter classes and objects referenced
             * from standard Java Platform SE 8 library.
             *
             * Method 'publishProgress(update)' referenced from AsyncTask documentation mentioned above.
             *
             * */

            try {

                // Make the server listen to the port all the time
                while (true) {

                    Socket soc = serverSocket.accept();


                    // Read from the socket for the message

                    InputStreamReader is = new InputStreamReader(soc.getInputStream());
                    BufferedReader b1 = new BufferedReader(is);

                    String s1 = b1.readLine();

                    // Write the message to the socket so that the client can check

                    PrintWriter p1 = new PrintWriter(soc.getOutputStream(), true);
                    p1.println(s1);

                    /*
                    * Referenced from OnPTClickListener.java file
                    * https://developer.android.com/reference/android/net/Uri
                    * https://developer.android.com/reference/android/content/ContentResolver
                    * https://stackoverflow.com/questions/7426451/what-are-uri-contentvalues
                    *
                    * */

                    ContentValues cv = new ContentValues();
                    cv.put("key", SEQ_NUM_FNAME+"");
                    cv.put("value", s1);

                    Uri.Builder u1 = new Uri.Builder();
                    u1.authority("edu.buffalo.cse.cse486586.groupmessenger1.provider");
                    u1.scheme("content");

                    Uri u2 = u1.build();



                    ContentResolver cr1 = con1.getContentResolver();
                    cr1.insert(u2, cv);

                    SEQ_NUM_FNAME += 1;

                    // Publish updates using publishProgress() and triggering a call to
                    // onProgressUpdate() on UI thread
                    publishProgress(s1);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onProgressUpdate(String... strings) {
            /*
             * The following code displays what is received in doInBackground().
             */

            String strReceived = strings[0].trim();
            TextView textView = (TextView) findViewById(R.id.textView1);
            textView.append(strReceived + "\n");

        }

    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {

                //Array to create five sockets for five avds
                Integer[] avdPorts = {REMOTE_PORT_AVD0,REMOTE_PORT_AVD1,REMOTE_PORT_AVD2,REMOTE_PORT_AVD3,REMOTE_PORT_AVD4};
                for(int i=0;i<5;i++) {

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),avdPorts[i]);

                    String msgToSend = msgs[0];

                    // Write message to socket for the server

                    PrintWriter p1 = new PrintWriter(socket.getOutputStream(), true);
                    p1.println(msgToSend);

                    // Read msg from socket that was sent by the server

                    InputStreamReader is = new InputStreamReader(socket.getInputStream());
                    BufferedReader b1 = new BufferedReader(is);

                    String s2 = b1.readLine();


                    // acknowledge that the correct message is recieved and only then close the socket
                    if (s2.equals(msgToSend)) {

                        socket.close();
                    }

                    socket.close();
                }
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }
            catch(Exception e){
                System.out.println("Heyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
                e.printStackTrace();
            }

            return null;
        }
    }


}
