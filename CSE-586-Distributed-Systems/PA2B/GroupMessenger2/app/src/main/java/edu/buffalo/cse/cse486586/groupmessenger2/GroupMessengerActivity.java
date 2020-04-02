package edu.buffalo.cse.cse486586.groupmessenger2;

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

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.TreeMap;

import java.util.PriorityQueue;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    static final String TAG = "GroupMessengerActivity";

    static final int REMOTE_PORT_AVD0 = 11108;
    static final int REMOTE_PORT_AVD1 = 11112;
    static final int REMOTE_PORT_AVD2 = 11116;
    static final int REMOTE_PORT_AVD3 = 11120;
    static final int REMOTE_PORT_AVD4 = 11124;
    static final int SERVER_PORT = 10000;

    static Integer[] avdPorts = {REMOTE_PORT_AVD0, REMOTE_PORT_AVD1, REMOTE_PORT_AVD2, REMOTE_PORT_AVD3, REMOTE_PORT_AVD4};
    static List<Integer> list_ports = new ArrayList<Integer>(Arrays.asList(avdPorts));

    
    static int seqnum1 = 0;
    static int fc = 0;

    // Sequence number for file name
    static int SEQ_NUM_FNAME = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);


        String portStr = telephonyManager.getLine1Number().substring(telephonyManager.getLine1Number().length() - 4);


        final String port = String.valueOf((Integer.parseInt(portStr) * 2));

        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        }
        catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
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
        Button sendButton = (Button) findViewById(R.id.button4);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText e1 = (EditText) findViewById(R.id.editText1);
                String message = e1.getText().toString();
                e1.setText("");
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, message, port);
            }
        });
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            Context con1 = getBaseContext();

            int sug_seq = 0;

            //Using priority queue to custom order messages
            PriorityQueue<Messages> pqm_msgs = new PriorityQueue<Messages>();

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
             * https://www.geeksforgeeks.org/priority-queue-class-in-java-2/
             * https://www.geeksforgeeks.org/implement-priorityqueue-comparator-java/
             *
             * */


            try{
                while(true){
                    Socket client = serverSocket.accept();

                    InputStreamReader is = new InputStreamReader(client.getInputStream());
                    BufferedReader b1 = new BufferedReader(is);


                    PrintWriter p1 = new PrintWriter(client.getOutputStream(), true);

                    String msg;

                    //check if msg is for suggesting sequence number or not
                    if((msg = b1.readLine()) != null){


                        // System.out.println(msg);
                        int aseq = Integer.parseInt(msg.split("-")[2]);
                        String m1 = msg.split("-")[1];
                        String port = msg.split("-")[0];
                        String stat = msg.split("-")[3];
                        if(stat.equals("notseq")) {
                            //System.out.println(port);
                            if (port.matches("^[0-9]{5}$")) {

                                //iterate over the messages or queue
                                Iterator<Messages> msgItr = pqm_msgs.iterator();
                                Messages mm = null;
                                while (msgItr.hasNext()) {
                                    //System.out.println(mm.getMessage());
                                    mm = msgItr.next();

                                    if (mm.getMessage().equals(m1)) {
                                        pqm_msgs.remove(mm);
                                        break;
                                    }
                                }

                                if (Integer.parseInt(port) != fc){
                                    pqm_msgs.add(new Messages(Integer.parseInt(port), m1, aseq, true));
                                }


                                p1.println("ack");
                            }
                        }

                        // if not, add msg to queue and increase sug seq.
                        else {
                            if (port.matches("^[0-9]{5}$")) {
                                if (list_ports.contains(Integer.parseInt(port))) {

                                    pqm_msgs.add(new Messages(Integer.parseInt(port),m1, aseq, false));

                                    p1.println("ack" + (sug_seq));


                                    if(SEQ_NUM_FNAME<sug_seq){
                                        sug_seq = sug_seq + 1;
                                    }
                                    else{
                                        sug_seq =SEQ_NUM_FNAME + 1;
                                    }

                                }
                            }


                        }

                        //deliver all files in the queue which are deliverable and remove them from queue
                        Messages head = null;
                        if((head = pqm_msgs.peek())!=null){
                            while ((head = pqm_msgs.peek()) != null && head.isd_status()) {


                                ContentValues cv = new ContentValues();
                                cv.put("key", SEQ_NUM_FNAME+"");
                                cv.put("value", head.getMessage());

                                Uri.Builder u1 = new Uri.Builder();
                                u1.authority("edu.buffalo.cse.cse486586.groupmessenger2.provider");
                                u1.scheme("content");

                                Uri u2 = u1.build();

                                ContentResolver cr1 = con1.getContentResolver();
                                cr1.insert(u2, cv);
                                SEQ_NUM_FNAME += 1;

                                publishProgress(head.getMessage());
                                pqm_msgs.remove();
                            }
                        }


                        //check if msg if from failed client. if so, remove it from queue
                        if((head = pqm_msgs.peek()) != null && head.getportNum()==fc){
                            pqm_msgs.remove(head);
                        }

                    }

                }

            } catch (IOException e){
                Log.e(TAG, e.toString());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... strings) {


            String strReceived = strings[0].trim();
            TextView textView = (TextView) findViewById(R.id.textView1);
            textView.append(strReceived + "\n");

        }
    }



    private class ClientTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {


            int sug_Num = 0;

            //contact all the ports and get the suggested sequnce numbers for each message and take th maximum number
            for (int i=0;i<list_ports.size();i++) {
                try {
                    String message = msgs[0];
                    String port= msgs[1];

                    Socket client = new Socket();
                    client.connect(new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), list_ports.get(i)));

                    //set socket timeout for the socket
                    client.setSoTimeout(500);


                    PrintWriter p1 = new PrintWriter(client.getOutputStream(), true);


                    InputStreamReader is = new InputStreamReader(client.getInputStream());
                    BufferedReader b1 = new BufferedReader(is);

                    //send all the message details to the server along with sequence number
                    p1.println(port + "-" + message + "-" + seqnum1+"-"+"fromseq");
                    while (client.isClosed()==false) {
                        String s1 = b1.readLine();
                        if (s1.contains("ack")) {
                            if (s1.length() > 3) {
                                int p_SeqNum = Integer.parseInt(s1.charAt(3)+"");
                                    //get the maximum sequence number ever seen by the process
                                if (sug_Num < p_SeqNum) {
                                    sug_Num = p_SeqNum;
                                }

                            }
                        } else {
                            client.close();
                        }
                    }

                    Thread.sleep(500);
                } catch (Exception e) {

                    System.out.println("Seq Num: " + e.toString());

                }
            }
            seqnum1 += 1;

            for (int i=0;i<list_ports.size();i++) {
                try {
                    Socket client = new Socket();
                    client.connect(new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), list_ports.get(i)));
                    client.setSoTimeout(500);


                    String message = msgs[0];
                    String port = msgs[1];


                    PrintWriter p1 = new PrintWriter(client.getOutputStream(), true);

                    InputStreamReader is = new InputStreamReader(client.getInputStream());
                    BufferedReader b1 = new BufferedReader(is);

                    //send the message with the minimum sequence number found to the server, then close the circuit.
                    p1.println(port+"-"+message + "-" + sug_Num + "-" + "notseq");
                    while (client.isClosed()==false) {
                        String s1 = b1.readLine();

                        System.out.println(s1);
                        if (s1.equals("ack")) {
                                client.close();

                        }
                        else{
                            client.close();
                        }


                    }

                    Thread.sleep(500);
                } catch (Exception e) {
                    // remove the failed client
                    fc = list_ports.get(i);
                    list_ports.remove(new Integer(fc));
                    System.out.println("After Seq Num: " + e.toString());
                }

            }

            return null;


        }

    }
}
