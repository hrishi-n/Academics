package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

import static android.content.ContentValues.TAG;

public class SimpleDhtProvider extends ContentProvider {
    static final String TAG = "SimpleDht";

    static final int SERVER_PORT = 10000;

    static final String PORT0 = "5554";
    static final String PORT1 = "5556";
    static final String PORT2 = "5558";
    static final String PORT3 = "5560";
    static final String PORT4 = "5562";
    static final String[] remotePortList = {PORT0, PORT1, PORT2, PORT3, PORT4};

    static String cPort;
    static int numNodes = 1;
    static String largestNode;

    static ArrayList<String> keyList = new ArrayList<String>();
    
    static ArrayList<String> nodeHashes = new ArrayList<String>();
    
    static HashMap<String, String> portMapping = new HashMap<String, String>();

    static Node node;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        ArrayList<String> files = new ArrayList<String>();

        if(!selection.contains("P_")){
            selection = "P_" + cPort + "-" + selection;
        }


        try {
            Socket client = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(node.successor) * 2);


            if(selection.split("-")[1].equals("*")){

                files.addAll(keyList);
                if(genHash(node.nodeID).compareTo(genHash(node.successor)) == 0 || selection.contains(node.successor)) {
                    for(String fileName : files){
                        getContext().deleteFile(fileName);
                    }

                } else {


                    String message = "DELETE:" + selection;
                    PrintWriter p1 = new PrintWriter(client.getOutputStream(), true);
                    BufferedReader b1 = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    p1.println(message);
                    String contents = "";
                    while (!client.isClosed()) {
                        String ack = b1.readLine();
                        
                        if (ack.contains("ACK_MSG_DELETED")) {
                            if(ack.length() > ("ACK_MSG_DELETED-").length()){
                                contents = ack.split("-")[1];
                            } else {
                                contents = "";
                            }

                            client.close();
                        }
                    }
                    for(String fileName : files){
                        getContext().deleteFile(fileName);
                    }
                }
            }
            else if(selection.split("-")[1].equals("@")){
                files.addAll(keyList);
                for(String fileName : files){
                    
                    getContext().deleteFile(fileName);
                    count += 1;
                }
            }
            else{
               
                String hashFileName = genHash(selection.split("-")[1]);
                if((genHash(node.nodeID).compareTo(genHash(node.predecessor)) > 0 && hashFileName.compareTo(genHash(node.predecessor)) > 0 && hashFileName.compareTo(genHash(node.nodeID)) <= 0)
                        || (genHash(node.nodeID).compareTo(genHash(node.predecessor)) < 0 && ((hashFileName.compareTo(genHash(node.predecessor)) > 0 && hashFileName.compareTo(genHash(node.nodeID)) > 0)
                        || (hashFileName.compareTo(genHash(node.predecessor)) < 0 && hashFileName.compareTo(genHash(node.nodeID)) <= 0)))
                        || (genHash(node.nodeID).compareTo(genHash(node.predecessor)) == 0 && genHash(node.nodeID).compareTo(genHash(node.successor)) == 0)){

                    files.add(selection.split("-")[1]);

                    for(String fileName : files){

                        getContext().deleteFile(fileName);
                    }

                } else {
                    //Socket client = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(node.successor) * 2);

                    String message = "DELETE:" + selection;
                    PrintWriter p1 = new PrintWriter(client.getOutputStream(), true);
                    BufferedReader b1 = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    p1.println(message);
                    String contents = "";
                    while (!client.isClosed()) {
                        String ack = b1.readLine();
                        if (ack.contains("ACK_MSG_DELETED")) {
                            if(ack.length() > ("ACK_MSG_DELETED-").length()){
                                contents = ack.split("-")[1];
                            } else {
                                contents = "";
                            }

                            client.close();
                        }
                    }
                }

            }

        }
        catch (Exception e){
            e.printStackTrace();
        }

        return count;
    }


    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String fileName = (String) values.get("key");
        String fileContent = (String) values.get("value");

        try {
            String hashFileName = genHash(fileName);
            if((genHash(node.nodeID).compareTo(genHash(node.predecessor)) > 0
                    && hashFileName.compareTo(genHash(node.predecessor)) > 0
                    && hashFileName.compareTo(genHash(node.nodeID)) <= 0)
                    || (genHash(node.nodeID).compareTo(genHash(node.predecessor)) < 0
                        && ((hashFileName.compareTo(genHash(node.predecessor)) > 0
                            && hashFileName.compareTo(genHash(node.nodeID)) > 0)
                            || (hashFileName.compareTo(genHash(node.predecessor)) < 0
                                && hashFileName.compareTo(genHash(node.nodeID)) <= 0)))
                    || (genHash(node.nodeID).compareTo(genHash(node.predecessor)) == 0
                    && genHash(node.nodeID).compareTo(genHash(node.successor)) == 0)){
                    
                FileOutputStream outputStream = getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
                outputStream.write(fileContent.getBytes());
                outputStream.close();
                keyList.add(fileName);

            } else {

                Socket client = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(node.successor) * 2);

                String message = "INSERT:" + fileName + ":" + fileContent;


                PrintWriter p1 = new PrintWriter(client.getOutputStream(), true);

                InputStreamReader is = new InputStreamReader( client.getInputStream());
                BufferedReader b1 = new BufferedReader(is);
                p1.println(message);
                String contents = "";
                while (!client.isClosed()) {
                    String ack = b1.readLine();

                    if (ack.contains("acknow_msg" + "_INSERTED")) {
                        client.close();
                    }
                }
            }
            
        } catch (Exception e) {
            
            e.printStackTrace();
        }
        
        return uri;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        if(!selection.contains("P_")){
            selection = "P_" + cPort + "-" + selection;
        }
        
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"key","value"});
        ArrayList<String> numFilesRead = new ArrayList<String>();
        try {
            
            
            if(selection.split("-")[1].equals("*")){
                numFilesRead.addAll(keyList);
                if(genHash(node.nodeID).compareTo(genHash(node.successor)) == 0
                        || selection.contains(node.successor)) {

                    String fileContent;
                    byte[] content = new byte[50];
                    for(String fileName : numFilesRead){

                            FileInputStream is = getContext().openFileInput(fileName);

                            int length = is.read(content);

                            fileContent = new String(content).substring(0, length);

                            MatrixCursor.RowBuilder mRowBuilder = matrixCursor.newRow();

                            mRowBuilder.add("key", fileName);
                            mRowBuilder.add("value", fileContent);

                            is.close();
                        }




                } else {

                    Socket client = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(node.successor) * 2);

                    String message = "QUERY:" + selection;

                    PrintWriter p1 = new PrintWriter(client.getOutputStream(), true);

                    InputStreamReader is = new InputStreamReader( client.getInputStream());
                    BufferedReader b1 = new BufferedReader(is);

                    p1.println(message);
                    String contents = "";
                    while (!client.isClosed()) {
                        String ack = b1.readLine();
        
                        if (ack.contains("acknow_msg" + "_KEY_VALUE")) {
                            if(ack.length() > ("acknow_msg" + "_KEY_VALUE-").length()){
                                contents = ack.split("-")[1];
                            } else {
                                contents = "";
                            }

                            client.close();
                        }
                    }
                    int p = 0;
                    String[] pairs = contents.length() > 0 ? contents.split(":") : null;
                    while(pairs != null && pairs.length > 0 && p < pairs.length) {
                        MatrixCursor.RowBuilder mRowBuilder = matrixCursor.newRow();
                        mRowBuilder.add("key", pairs[p]);
                        mRowBuilder.add("value", pairs[p + 1]);
                        p += 2;
                    }
                    MatrixCursor currentMatrixCursor = new MatrixCursor(new String[]{"key","value"});

                    String fileContent;
                    byte[] content = new byte[50];
                    for(String fileName : numFilesRead){

                        FileInputStream is1 = getContext().openFileInput(fileName);

                        int length = is1.read(content);

                        fileContent = new String(content).substring(0, length);

                        MatrixCursor.RowBuilder mRowBuilder = currentMatrixCursor.newRow();

                        mRowBuilder.add("key", fileName);
                        mRowBuilder.add("value", fileContent);

                        is1.close();
                    }

                    String contentPairs1 = getContent(currentMatrixCursor);
                    String[]   contentsvals = contentPairs1.split(":");
                    int q = 0;
                    while (q + 1 < contentsvals.length) {
                        MatrixCursor.RowBuilder mRowBuilder = matrixCursor.newRow();

                        mRowBuilder.add("key", contentsvals[q]);
                        mRowBuilder.add("value", contentsvals[q+ 1]);
                        q+= 2;
                    }

                }
            }
            else if(selection.split("-")[1].equals("@")){
                String fileContent;
                byte[] content = new byte[50];
                numFilesRead.addAll(keyList);
                for(String fileName : numFilesRead){

                    FileInputStream is = getContext().openFileInput(fileName);

                    int length = is.read(content);

                    fileContent = new String(content).substring(0, length);

                    MatrixCursor.RowBuilder mRowBuilder = matrixCursor.newRow();

                    mRowBuilder.add("key", fileName);
                    mRowBuilder.add("value", fileContent);

                    is.close();
                }
            }
            else{
        
                String hashFileName = genHash(selection.split("-")[1]);
                if((genHash(node.nodeID).compareTo(genHash(node.predecessor)) > 0
                        && hashFileName.compareTo(genHash(node.predecessor)) > 0
                        && hashFileName.compareTo(genHash(node.nodeID)) <= 0)
                        || (genHash(node.nodeID).compareTo(genHash(node.predecessor)) < 0
                        && ((hashFileName.compareTo(genHash(node.predecessor)) > 0
                        && hashFileName.compareTo(genHash(node.nodeID)) > 0)
                        || (hashFileName.compareTo(genHash(node.predecessor)) < 0
                        && hashFileName.compareTo(genHash(node.nodeID)) <= 0)))
                        || (genHash(node.nodeID).compareTo(genHash(node.predecessor)) == 0
                        && genHash(node.nodeID).compareTo(genHash(node.successor)) == 0)){
                    numFilesRead.add(selection.split("-")[1]);
                    String fileContent;
                    byte[] content = new byte[50];
                    for(String fileName : numFilesRead){

                        FileInputStream is = getContext().openFileInput(fileName);

                        int length = is.read(content);
                        fileContent = new String(content).substring(0, length);

                        MatrixCursor.RowBuilder mRowBuilder = matrixCursor.newRow();

                        mRowBuilder.add("key", fileName);
                        mRowBuilder.add("value", fileContent);
                        is.close();
                    }

                } else {

                    String[] contentPairs = null;



                    Socket client = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(node.successor) * 2);

                    String message = "QUERY:" + selection;

                    PrintWriter p1 = new PrintWriter(client.getOutputStream(), true);

                    InputStreamReader is = new InputStreamReader( client.getInputStream());
                    BufferedReader b1 = new BufferedReader(is);

                    p1.println(message);
                    String contents = "";
                    while (!client.isClosed()) {
                        String ack = b1.readLine();
        
                        if (ack.contains("acknow_msg" + "_KEY_VALUE")) {
                            if(ack.length() > ("acknow_msg" + "_KEY_VALUE-").length()){
                                contents = ack.split("-")[1];
                            } else {
                                contents = "";
                            }

                            client.close();
                        }
                    }
                    int a1 = 0;
                    contentPairs = contents.length() > 0 ? contents.split(":") : null;
                    
                    while(contentPairs != null && contentPairs.length > 0 && a1 < contentPairs.length) {
                        MatrixCursor.RowBuilder mRowBuilder = matrixCursor.newRow();
                        mRowBuilder.add("key", contentPairs[a1]);
                        mRowBuilder.add("value", contentPairs[a1 + 1]);
                        a1 += 2;
                    }




                }

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        
        return matrixCursor;
    }


    private String getContent(MatrixCursor matrixCursor) {
        
        String content = "";
        try {
            int keyIndex = matrixCursor.getColumnIndex("key");
            int valueIndex = matrixCursor.getColumnIndex("value");
            matrixCursor.moveToFirst();
            if (matrixCursor.getCount() > 0) {
                do {
                    String returnKey = matrixCursor.getString(keyIndex);
                    String returnValue = matrixCursor.getString(valueIndex);
                    content = content + returnKey + ":" + returnValue + ":";
        
                } while (matrixCursor.moveToNext());

                matrixCursor.close();
                if (content.length() > 0) {
                    content = content.substring(0, content.length() - 1);
                }
            }
        } catch(Exception e){
            content = "";
            return content;
        }
        //matrixCursor.moveToFirst();
        //System.out.println(matrixCursor.getCount());
        return content;
    }


    @Override
    public boolean onCreate() {

        TelephonyManager telManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String portString = telManager.getLine1Number().substring(telManager.getLine1Number().length() - 4);
        final String portNumber = String.valueOf((Integer.parseInt(portString)) * 2);

        try{
        
            cPort = portString;
            node = new Node();
            node.nodeID = portString;
            node.successor = portString;
            node.predecessor = portString;
            largestNode = genHash(PORT0);
            portMapping.put(genHash(PORT0), PORT0);
            portMapping.put(genHash(PORT1), PORT1);
            portMapping.put(genHash(PORT2), PORT2);
            portMapping.put(genHash(PORT3), PORT3);
            portMapping.put(genHash(PORT4), PORT4);

            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
            if(!portNumber.equals((Integer.parseInt(PORT0)*2)+"")){
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, (Integer.parseInt(PORT0)*2)+"", cPort);
            } else{
                nodeHashes.add(genHash(PORT0));
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return true;
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void>{

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority("edu.buffalo.cse.cse486586.simpledht.provider");
            uriBuilder.scheme("content");
            Uri mUri = uriBuilder.build();


            try{
                while(true){
                    Socket client = serverSocket.accept();
                    InputStreamReader is = new InputStreamReader(client.getInputStream());


                    BufferedReader reader = new BufferedReader(is);
                    PrintWriter ackSend = new PrintWriter(client.getOutputStream(), true);
                    String msg;

                    if((msg = reader.readLine()) != null) {
                        List<String> portList = Arrays.asList(remotePortList);
                        if (portList.contains(msg) && msg.length() == 4) {
                            if (numNodes == 1 && node.nodeID.equals(PORT0)) {
                                node.successor = msg;
                                node.predecessor = msg;
                                
                                if(genHash(msg).compareTo(largestNode)>0){
                                    largestNode = genHash(msg);
                                }

                                ackSend.println("acknow_msg" + ":" + node.nodeID + ":" + node.nodeID);
                            } else {
                                Node insertLoc = searchPred(msg, node);

                                ackSend.println("acknow_msg" + ":" + insertLoc.nodeID + ":" + insertLoc.successor);
                                
                                if(genHash(msg).compareTo(largestNode)>0){
                                    largestNode = genHash(msg);
                                }

                            }
                            numNodes++;
                            nodeHashes.add(genHash(msg));
                        } else if (msg.contains("get")) {
                            ackSend.println("send" + ":" + node.nodeID + ":" + node.predecessor + ":" + node.successor);
                        } else if (msg.contains("SUCCESSOR") || msg.contains("PREDECESSOR")) {

                            if (msg.contains("SUCCESSOR")) {
                                node.successor = msg.split(":")[1];
                            } else {
                                node.predecessor = msg.split(":")[1];
                            }
                            ackSend.println("acknow_msg_NODE_MODIFIED");
                        }else if(msg.contains("QUERY")){
                            String ackMsg = "acknow_msg_KEY_VALUE";
                            Cursor resultCursor = query(mUri, null, msg.split(":")[1], null, null);
                            ackMsg = ackMsg + "-" + getContent((MatrixCursor)resultCursor);
                            ackSend.println(ackMsg);
                        } else if(msg.contains("DELETE")){
                            String ackMsg = "acknow_msg_DELETED";

                            int count = delete(mUri, msg.split(":")[1], null);
                            ackMsg = ackMsg + "-" + String.valueOf(count);

                            ackSend.println(ackMsg);
                        }
                        else if(msg.contains("INSERT")) {
                            String ackMsg = "acknow_msg"+"_INSERTED";

                            ContentValues mContentValues = new ContentValues();
                            mContentValues.put("key", msg.split(":")[1]);
                            mContentValues.put("value", msg.split(":")[2]);

                            insert(mUri, mContentValues);
                            ackSend.println(ackMsg);
                        }  else {
                            ackSend.println("acknow_msg");
                            publishProgress(msg);
                        }
                    }

                }
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {

        }

    }

    private class ClientTask extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... params) {

            try {
                Socket client = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(params[0]));
        
                String message = params[1];

                PrintWriter p1 = new PrintWriter(client.getOutputStream(), true);

                InputStreamReader is = new InputStreamReader( client.getInputStream());
                BufferedReader b1 = new BufferedReader(is);

                p1.println(message);
                while (!client.isClosed()) {
                    String ack = b1.readLine();
        
                    if(ack.equalsIgnoreCase("acknow_msg")){
        
                        client.close();
                    }else if (ack.contains("acknow_msg")) {
                        String[] contents = ack.split(":");
        

                        node.predecessor = contents[1];
                        node.successor = contents[2];
        
                        client.close();
                    }
        
                }
        
                client = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(node.predecessor) * 2);
        
                message = "SUCCESSOR:"+cPort;
                p1 = new PrintWriter(client.getOutputStream(), true);
                b1 = new BufferedReader(new InputStreamReader(client.getInputStream()));
                p1.println(message);
                while (!client.isClosed()) {
                    String ack = b1.readLine();
        
                    if(ack.equalsIgnoreCase("acknow_msg_NODE_MODIFIED")){

                        client.close();
                    }

                }

                
                client = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(node.successor) * 2);
        
                message = "PREDECESSOR:"+cPort;
                p1 = new PrintWriter(client.getOutputStream(), true);
                b1 = new BufferedReader(new InputStreamReader(client.getInputStream()));
                p1.println(message);
                while (!client.isClosed()) {
                    String ack = b1.readLine();
        
                    if(ack.equalsIgnoreCase("acknow_msg_NODE_MODIFIED")){
                        client.close();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }



    public Node searchPred(String id, Node n){
        //Node n = this;


        try {
            while(!(n.checkCondition(genHash(id),n, largestNode))){
                Socket client = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(n.successor)*2);


                PrintWriter p1 = new PrintWriter(client.getOutputStream(), true);

                InputStreamReader is = new InputStreamReader( client.getInputStream());
                BufferedReader b1 = new BufferedReader(is);


                p1.println("get");
                while (!client.isClosed()) {
                    String ack = b1.readLine();

                    if(ack.contains("send")){

                        n = new Node();
                        String[] rcvNode = ack.split(":");
                        n.nodeID = rcvNode[1];
                        n.predecessor = rcvNode[2];
                        n.successor = rcvNode[3];

                        client.close();
                    }
                }
                p1.close();
                b1.close();


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return n;
    }



}
