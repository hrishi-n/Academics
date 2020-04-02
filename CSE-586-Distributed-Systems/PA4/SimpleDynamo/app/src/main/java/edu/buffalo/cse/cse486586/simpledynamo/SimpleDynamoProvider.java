package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SimpleDynamoProvider extends ContentProvider {

	static final int SERVER_PORT = 10000;

	static final String PORT0 = "5554";
	static final String PORT1 = "5556";
	static final String PORT2 = "5558";
	static final String PORT3 = "5560";
	static final String PORT4 = "5562";
	static final String[] remotePortList = {PORT0, PORT1, PORT2, PORT3, PORT4};
	static String cPort;

	static HashMap<String,String> cur_map_stat = new HashMap<String, String>();
	static Set<String> insert_list = new TreeSet<String>();
	
	static PriorityQueue<String> nodeIDs = new PriorityQueue<String>();
	static HashMap<String, String> nodePortMap = new HashMap<String, String>();

	public class NetworkRing {
		ArrayList<Node> nodeList;
		public NetworkRing(HashMap<String, String> nodeHashMap,PriorityQueue<String> nodeIds ){
			nodeList = new ArrayList<Node>();
			ArrayList<String> nodesList1 = new ArrayList<String>(nodeIds);
			int nodeCount = nodesList1.size();

			for (String node:nodesList1){
				Node node1 = new Node();
				int index=nodesList1.indexOf(node);
				node1.setNodeID(nodeHashMap.get(nodesList1.get(index)));
				int i;

				if(index - 1 < 0){
					i=index-1+nodeCount;
				}
				else{
					i=index-1;
				}
				node1.setPred1(nodeHashMap.get(nodesList1.get(i)));
				if(index - 2 < 0){
					i=index-2+nodeCount;
				}
				else{
					i=index-2;
				}

				node1.setPred2(nodeHashMap.get(nodesList1.get(i)));

				if(index + 1 >= nodeCount){
					i=(index+1)%nodeCount;
				}
				else{
					i=index+1;
				}
				node1.setSucc1(nodeHashMap.get(nodesList1.get(i)));

				if(index + 2 >= nodeCount){
					i=(index+2)%nodeCount;
				}
				else{
					i=index+2;
				}


				node1.setSucc2(nodeHashMap.get(nodesList1.get(i)));
				nodeList.add(node1);

			}
		}
	}


	static NetworkRing networkRing;
	static boolean isQuery = false;


	static HashMap<String,String> pred1map = new HashMap<String, String>();
	static HashMap<String,String> pred2map = new HashMap<String, String>();
	
	static HashMap<String,String> succ1Map = new HashMap<String, String>();
	static HashMap<String,String> succ2Map = new HashMap<String, String>();
	
	
	static HashMap<String,String> insertedMap = new HashMap<String, String>();
	static ArrayList<String> node_List = new ArrayList<String>();
	static Node node;
	static String[] device_files=new String[]{};


	@Override
	public boolean onCreate() {

		TelephonyManager telManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		String portString = telManager.getLine1Number().substring(telManager.getLine1Number().length() - 4);

		cPort = portString;
		try{
			nodeIDs.add(genHash(PORT4));
			nodeIDs.add(genHash(PORT1));
			nodeIDs.add(genHash(PORT0));
			nodeIDs.add(genHash(PORT2));
			nodeIDs.add(genHash(PORT3));

			nodePortMap.put(genHash(PORT0), PORT0);
			nodePortMap.put(genHash(PORT1), PORT1);
			nodePortMap.put(genHash(PORT2), PORT2);
			nodePortMap.put(genHash(PORT3), PORT3);
			nodePortMap.put(genHash(PORT4), PORT4);

			networkRing = new NetworkRing(nodePortMap,nodeIDs);


			int ctr = 0;
			int nodeCount = networkRing.nodeList.size();
			while(ctr < nodeCount){
				if(node==null){
					if(networkRing.nodeList.get(ctr).getNodeID().equals(cPort)) {node=networkRing.nodeList.get(ctr);}
				}
				ctr++;
			}

			device_files = getContext().fileList();

			node_List.add(node.getSucc1());
			node_List.add(node.getSucc2());
			node_List.add(node.getPred1());
			node_List.add(node.getPred2());

			isQuery = true;

			
			ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
			new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
			
			
			new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, null, "REJOIN_"+cPort);

		} catch (Exception e){
			e.printStackTrace();
		}

		return true;
	}

	private Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {


		if(selection.equals("EXISTING") && device_files.length > 0){
			System.out.println("Hrishikesh Nitturkar");
			for(String fileName : device_files){
				getContext().deleteFile(fileName);
			}
		}
		else {
			ArrayList<String> filesToDelete = new ArrayList<String>();
			try {

				if (selectionArgs != null && selectionArgs[0].equals("REPLICA")) {

					if (selection.equals("@")) {
						insert_list.addAll(Arrays.asList(device_files));
						filesToDelete.addAll(insert_list);
					}
					else{
						filesToDelete.add(selection);
					}
					for (String fileName : filesToDelete) {

						getContext().deleteFile(fileName);
						insert_list.remove(fileName);
						Node destinationNode = new Node();
						String hashFileName = genHash(fileName);
						int ctr = 0;
						int nodeCount = networkRing.nodeList.size();
						while (ctr < nodeCount && ctr + 1 < nodeCount) {
							if(destinationNode.getNodeID()==null) {
								if (hashFileName.compareTo(genHash(networkRing.nodeList.get(ctr).getNodeID())) > 0 && hashFileName.compareTo(genHash(networkRing.nodeList.get(ctr + 1).getNodeID())) <= 0)
									destinationNode = networkRing.nodeList.get(ctr + 1);
							}
							ctr++;
						}


						if(destinationNode.getNodeID()==null){destinationNode = networkRing.nodeList.get(0);}


						if (destinationNode.getNodeID().equals(node.getNodeID())) {
							cur_map_stat.remove(fileName);
						} else if (destinationNode.getSucc1().equals(node.getNodeID())) {
							pred1map.remove(fileName);
						} else if (destinationNode.getSucc2().equals(node.getNodeID())) {
							pred2map.remove(fileName);
						}
					}

				} else {
					if (selection.equals("*")) {
						insert_list.addAll(Arrays.asList(device_files));
						filesToDelete.addAll(insert_list);
						if (genHash(node.getNodeID()).equals(genHash(node.getSucc1()))|| selection.contains(node.getSucc1())) {
							for (String fileName : filesToDelete) {
								getContext().deleteFile(fileName);
								insert_list.remove(fileName);
								cur_map_stat.remove(fileName);
							}

						} else {
							ArrayList<String> otherNodesList = new ArrayList<String>();

							int ctr = 0;
							int nodeCount = networkRing.nodeList.size();

							while(ctr < nodeCount){
								if(node.getNodeID().compareTo(networkRing.nodeList.get(ctr).getNodeID()) != 0)
									otherNodesList.add(networkRing.nodeList.get(ctr).getNodeID());
								ctr++;
							}

							for (String fileName : filesToDelete) {
								getContext().deleteFile(fileName);
								cur_map_stat.remove(fileName);
							}

							String func = "NODE_D:";

							for(String nodeStr : otherNodesList) {

								Socket client = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(nodeStr) * 2);

								String message = func+ "@";
								func = "REPLICA_D:";
								PrintWriter p1 = new PrintWriter(client.getOutputStream(), true);
								BufferedReader b1 = new BufferedReader(new InputStreamReader(client.getInputStream()));
								p1.println(message);
								while (!client.isClosed()) {
									String ack = b1.readLine();
									if(ack == null){
										client.close();
										continue;
									}

									if (ack.contains(nodeStr + "_DELETED")) {
										client.close();
									}
								}

							}
						}
					} else if (selection.equals("@")) {
						insert_list.addAll(Arrays.asList(device_files));
						filesToDelete.addAll(insert_list);

						for (String fileName : filesToDelete) {

							getContext().deleteFile(fileName);
							insert_list.remove(fileName);
							cur_map_stat.remove(fileName);
						}
					} else {

						String hashFileName = genHash(selection);

						Node destinationNode = new Node();

						int ctr = 0;
						int nodeCount = networkRing.nodeList.size();
						while (ctr < nodeCount && ctr + 1 < nodeCount) {
							if(destinationNode.getNodeID()==null) {
								if (hashFileName.compareTo(genHash(networkRing.nodeList.get(ctr).getNodeID())) > 0 && hashFileName.compareTo(genHash(networkRing.nodeList.get(ctr + 1).getNodeID())) <= 0)
									destinationNode = networkRing.nodeList.get(ctr + 1);
							}
							ctr++;
						}


						if(destinationNode.getNodeID()==null){destinationNode = networkRing.nodeList.get(0);}


						if (destinationNode.getNodeID().equals(node.getNodeID())) {
							ArrayList<String> destinationNodeStr = new ArrayList<String>();
							filesToDelete.add(selection);
							for (String fileName : filesToDelete) {

								getContext().deleteFile(fileName);
								cur_map_stat.remove(fileName);
								
							}
							destinationNodeStr.add(node.getSucc1());
							succ1Map.remove(selection);
							destinationNodeStr.add(node.getSucc2());
							succ2Map.remove(selection);


							for(String nodeStr : destinationNodeStr) {

								Socket client = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(nodeStr) * 2);

								String message = "REPLICA_D:" + selection;

								PrintWriter p1 = new PrintWriter(client.getOutputStream(), true);
								BufferedReader b1 = new BufferedReader(new InputStreamReader(client.getInputStream()));
								p1.println(message);

								while (!client.isClosed()) {
									String ack = b1.readLine();
									if(ack == null){
										client.close();
										continue;
									}

									if (ack.contains(nodeStr + "_DELETED")) {
										client.close();
									}
								}

							}


						} else {
							ArrayList<String> destinationNodeStr = new ArrayList<String>();
							destinationNodeStr.add(destinationNode.getNodeID());

							if (destinationNode.getSucc1().equals(node.getNodeID())) {
								destinationNodeStr.add(destinationNode.getSucc2());
								pred1map.remove(selection);
								filesToDelete.add(selection);
								for (String fileName : filesToDelete) {

									getContext().deleteFile(fileName);
									insert_list.remove(fileName);
									cur_map_stat.remove(fileName);

								}


							} else if (destinationNode.getSucc2().equals(node.getNodeID())) {
								destinationNodeStr.add(destinationNode.getSucc1());
								pred2map.remove(selection);
								filesToDelete.add(selection);
								for (String fileName : filesToDelete) {

									getContext().deleteFile(fileName);
									insert_list.remove(fileName);
									cur_map_stat.remove(fileName);

								}
							} else {
								destinationNodeStr.add(destinationNode.getSucc1());
								destinationNodeStr.add(destinationNode.getSucc2());
							}

							String func = "NODE_D:";
							for(String nodeStr : destinationNodeStr) {

								Socket client = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(nodeStr) * 2);

								String message = func + selection;
								func = "REPLICA_D:";
								PrintWriter p1 = new PrintWriter(client.getOutputStream(), true);
								BufferedReader b1 = new BufferedReader(new InputStreamReader(client.getInputStream()));
								p1.println(message);

								while (!client.isClosed()) {
									String ack = b1.readLine();
									if(ack == null){
										client.close();
										continue;
									}

									if (ack.contains( nodeStr + "_DELETED")) {
										client.close();
									}
								}
							}

						}


					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		String fileName = (String) values.get("key");
		String value = (String) values.get("value");
		System.out.println(value+" value");
		String instruction = "";
		String[] values1 = value.split("-");
		System.out.println(values1.length);
		String fileContent = values1[0];
		if(values1.length>1){
			instruction = values1[1];
		}
		Date date = new Date();

		long file_time = date.getTime();


		try {
			ArrayList<String> destinationNodeStr = new ArrayList<String>();
			String hashFileName = genHash(fileName);
			Node destinationNode = new Node();

			int ctr = 0;
			int nodeCount = networkRing.nodeList.size();
				while (ctr < nodeCount && ctr + 1 < nodeCount) {
					if(destinationNode.getNodeID()==null) {
						if (hashFileName.compareTo(genHash(networkRing.nodeList.get(ctr).getNodeID())) > 0 && hashFileName.compareTo(genHash(networkRing.nodeList.get(ctr + 1).getNodeID())) <= 0)
							destinationNode = networkRing.nodeList.get(ctr + 1);
					}
					ctr++;
				}


			if(destinationNode.getNodeID()==null){destinationNode = networkRing.nodeList.get(0);}



			if(instruction != null && (instruction.equals("REPLICA") || instruction.equals("REJOIN"))){

				FileOutputStream outputStream = getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
				outputStream.write(fileContent.getBytes());
				outputStream.close();
				insert_list.add(fileName);
				insertedMap.put(fileName, fileContent+"@"+file_time);
				if(destinationNode.getSucc1().equals(node.getNodeID())){
					pred1map.put(fileName, fileContent+"@"+file_time);
				} else if(destinationNode.getSucc2().equals(genHash(node.getNodeID()))){
					pred2map.put(fileName, fileContent+"@"+file_time);
				} else if(destinationNode.getNodeID().equals(node.getNodeID())){
					cur_map_stat.put(fileName, fileContent+"@"+file_time);



					succ1Map.put(fileName, fileContent+"@"+file_time);
					succ2Map.put(fileName, fileContent+"@"+file_time);
					if(instruction.equals("REPLICA")) {
						destinationNodeStr.add(node.getSucc1());
						destinationNodeStr.add(node.getSucc2());

					}
				}
			}
			else {
				while (isQuery);
				if (destinationNode.getNodeID().equals(node.getNodeID())) {

					FileOutputStream outputStream = getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
					outputStream.write(fileContent.getBytes());
					outputStream.close();

					insert_list.add(fileName);

					insertedMap.put(fileName, fileContent+"@"+file_time);
					cur_map_stat.put(fileName, fileContent+"@"+file_time);


					destinationNodeStr.add(node.getSucc1());
					succ1Map.put(fileName, fileContent+"@"+file_time);

					destinationNodeStr.add(node.getSucc2());
					succ2Map.put(fileName, fileContent+"@"+file_time);

				} else {
					destinationNodeStr.add(destinationNode.getNodeID());

					if(destinationNode.getSucc1().equals(node.getNodeID())) {

						pred1map.put(fileName, fileContent+"@"+file_time);
						destinationNodeStr.add(destinationNode.getSucc2());

						FileOutputStream outputStream = getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
						outputStream.write(fileContent.getBytes());
						outputStream.close();

						insert_list.add(fileName);
						insertedMap.put(fileName, fileContent+"@"+file_time);

					}
					else if(destinationNode.getSucc2().equals(node.getNodeID())) {

						pred2map.put(fileName, fileContent+"@"+file_time);
						destinationNodeStr.add(destinationNode.getSucc1());

						FileOutputStream outputStream = getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
						outputStream.write(fileContent.getBytes());
						outputStream.close();

						insert_list.add(fileName);
						insertedMap.put(fileName, fileContent+"@"+file_time);

					}
					else {

						destinationNodeStr.add(destinationNode.getSucc1());
						destinationNodeStr.add(destinationNode.getSucc2());

					}

				}
			}
			String message = "INSERT_REPLICA_" + cPort + ":" + fileName + ":" + fileContent;
			for(String nodeStr : destinationNodeStr) {
				Socket client = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(nodeStr) * 2);
				client.setSoTimeout(500);
				PrintWriter p1 = new PrintWriter(client.getOutputStream(), true);
				BufferedReader b1 = new BufferedReader(new InputStreamReader(client.getInputStream()));
				p1.println(message);
				while (!client.isClosed()) {
					String ack = b1.readLine();
					if(ack == null){
						client.close();
						continue;
					}
					if (ack.contains("REPLICATION_DONE")) {
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
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		MatrixCursor matrixCursor = new MatrixCursor(new String[]{"key", "value"});
		ArrayList<String> filesToRead = new ArrayList<String>();
		try {

			if(selectionArgs != null && selectionArgs[0].equals("REPLICA_Q")){
				if (selection.equals("@")) {
					insert_list.addAll(Arrays.asList(device_files));
					filesToRead.addAll(insert_list);
				}
				else{filesToRead.add(selection);}

				FileInputStream inputStream;
				String fileContent;
				byte[] content = new byte[50];

				for(String fileName : filesToRead) {

					inputStream = getContext().openFileInput(fileName);

					int length = inputStream.read(content);

					fileContent = new String(content).substring(0, length);

					MatrixCursor.RowBuilder mRowBuilder = matrixCursor.newRow();

					mRowBuilder.add("key", fileName);
					mRowBuilder.add("value", fileContent);

					inputStream.close();
				}



			}
			else {
				if (selection.equals("*")) {
					insert_list.addAll(Arrays.asList(device_files));
					filesToRead.addAll(insert_list);
					if (genHash(node.getNodeID()).equals(genHash(node.getSucc1())) || selection.contains(node.getSucc1())) {

						FileInputStream inputStream;
						String fileContent;
						byte[] content = new byte[50];
						
						for(String fileName : filesToRead) {

							inputStream = getContext().openFileInput(fileName);

							int length = inputStream.read(content);

							fileContent = new String(content).substring(0, length);

							MatrixCursor.RowBuilder mRowBuilder = matrixCursor.newRow();

							mRowBuilder.add("key", fileName);
							mRowBuilder.add("value", fileContent);

							inputStream.close();
						}


					} else {
						ArrayList<String> otherNodesList = new ArrayList<String>();

						int ctr = 0;
						int nodeCount = networkRing.nodeList.size();

						while(ctr < nodeCount){
							if(node.getNodeID().compareTo(networkRing.nodeList.get(ctr).getNodeID()) != 0)
								otherNodesList.add(networkRing.nodeList.get(ctr).getNodeID());
							ctr++;
						}



						matrixCursor = getAllData(otherNodesList, "@");

						MatrixCursor mc = new MatrixCursor(new String[]{"key", "value"});

						FileInputStream inputStream;
						String fileContent;
						byte[] content = new byte[50];
						
						for(String fileName : filesToRead) {

							inputStream = getContext().openFileInput(fileName);

							int length = inputStream.read(content);

							fileContent = new String(content).substring(0, length);

							MatrixCursor.RowBuilder mRowBuilder = mc.newRow();

							mRowBuilder.add("key", fileName);
							mRowBuilder.add("value", fileContent);

							inputStream.close();
						}



						
						String keyValPairs ="";

						int keyIndex = mc.getColumnIndex("key");
						int valueIndex = mc.getColumnIndex("value");
						mc.moveToFirst();
						if (mc.getCount() > 0) {
							do {
								String fileName = mc.getString(keyIndex);
								String fileData = mc.getString(valueIndex);
								keyValPairs+=fileName + ":" + fileData + ":";
							} while (mc.moveToNext());

							mc.close();
							if (keyValPairs.length() > 0) {
								keyValPairs=keyValPairs.substring(0,keyValPairs.length() - 1);
							}
						}



						String[] keyValPairArr = keyValPairs.split(":");
						ctr = 0;
						while (ctr + 1 < keyValPairArr.length) {
							MatrixCursor.RowBuilder mRowBuilder = matrixCursor.newRow();

							mRowBuilder.add("key", keyValPairArr[ctr]);
							mRowBuilder.add("value", keyValPairArr[ctr + 1]);
							ctr += 2;
						}

					}
				} else if (selection.equals("@")) {
					while(isQuery);
					insert_list.addAll(Arrays.asList(device_files));
					filesToRead.addAll(insert_list);
					FileInputStream inputStream;
					String fileContent;
					byte[] content = new byte[50];
					for(String fileName : filesToRead) {

						inputStream = getContext().openFileInput(fileName);

						int length = inputStream.read(content);

						fileContent = new String(content).substring(0, length);

						MatrixCursor.RowBuilder mRowBuilder = matrixCursor.newRow();

						mRowBuilder.add("key", fileName);
						mRowBuilder.add("value", fileContent);

						inputStream.close();
					}

				} else {

					String hashFileName = genHash(selection);

					Node destinationNode = new Node();

					int ctr = 0;
					int nodeCount = networkRing.nodeList.size();
					while (ctr < nodeCount && ctr + 1 < nodeCount) {
						if(destinationNode.getNodeID()==null) {
							if (hashFileName.compareTo(genHash(networkRing.nodeList.get(ctr).getNodeID())) > 0 && hashFileName.compareTo(genHash(networkRing.nodeList.get(ctr + 1).getNodeID())) <= 0)
								destinationNode = networkRing.nodeList.get(ctr + 1);
						}
						ctr++;
					}


					if(destinationNode.getNodeID()==null){destinationNode = networkRing.nodeList.get(0);}

					ArrayList<String> destinationNodeStr = new ArrayList<String>();
					destinationNodeStr.add(destinationNode.getNodeID());
					destinationNodeStr.add(destinationNode.getSucc1());
					destinationNodeStr.add(destinationNode.getSucc2());

					System.out.println("Hrishikesh Nitturkar!@#");
					if (destinationNode.getNodeID().equals(node.getNodeID())) {
						matrixCursor = getAllData(destinationNodeStr, selection);
					} else {
						Collections.reverse(destinationNodeStr);
						matrixCursor = getAllData(destinationNodeStr, selection);

					}

				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}

		return matrixCursor;
	}

	private MatrixCursor getAllData(ArrayList<String> nodeList, String selection) {

		MatrixCursor matrixCursor = new MatrixCursor(new String[]{"key", "value"});
		String[] keyValPairs;
		String instruction = "REPLICA_Q:";
		String contents = "";
		try {
			for (String nodeStr : nodeList) {
				if (nodeStr.equals(node.getNodeID()) && !isQuery) {
					ArrayList<String> filesToRead = new ArrayList<String>();
					filesToRead.add(selection);
					String result = "";

					FileInputStream inputStream;
					String fileContent;
					byte[] content = new byte[50];
					matrixCursor = new MatrixCursor(new String[]{"key", "value"});
					for (String fileName : filesToRead) {
						inputStream = getContext().openFileInput(fileName);

						int length = inputStream.read(content);

						fileContent = new String(content).substring(0, length);

						MatrixCursor.RowBuilder mRowBuilder = matrixCursor.newRow();

						mRowBuilder.add("key", fileName);
						mRowBuilder.add("value", fileContent);

						inputStream.close();
					}
					int keyIndex = matrixCursor.getColumnIndex("key");
					int valueIndex = matrixCursor.getColumnIndex("value");
					matrixCursor.moveToFirst();
					if (matrixCursor.getCount() > 0) {
						while (matrixCursor.moveToNext()) {
							String returnKey = matrixCursor.getString(keyIndex);
							String returnValue = matrixCursor.getString(valueIndex);
							result+=returnKey + ":" + returnValue + ":";
						}

						matrixCursor.close();
						if (!result.equals("")) {
							result=result.substring(0,result.length() - 1);
						}
					}



					if (result != null && result.contains(":"))
						contents += result + ":";
				} else {

					try {


						Socket client = new Socket();
						client.connect(new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(nodeStr) * 2));
						String message = instruction + selection;



						PrintWriter p1 = new PrintWriter(client.getOutputStream(), true);
						BufferedReader b1 = new BufferedReader(new InputStreamReader(client.getInputStream()));
						p1.println(message);

						while (!client.isClosed()) {
							String ack = b1.readLine();
							if (ack == null) {
								client.close();
								continue;
							}

							if (ack.contains(nodeStr + "_KEY_VALUE")) {
								if (ack.length() > (nodeStr + "_KEY_VALUE::").length()) {
									contents += ack.split("::")[1] + ":";
								}

								client.close();
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			int ctr = 0;
			if (contents.length() > 0) {
				contents = contents.substring(0, contents.length() - 1);
			}


			if(contents.length()<0){
				keyValPairs=null;
			}
			else{
				keyValPairs = contents.split(":");
			}


			HashMap<String, String> keyValMap = new HashMap<String, String>();
			while (keyValPairs != null && keyValPairs.length > 0 && ctr < keyValPairs.length) {
				if (!selection.equals("@")) {
					if (keyValMap.containsKey(keyValPairs[ctr + 1])) {
						MatrixCursor.RowBuilder mRowBuilder = matrixCursor.newRow();
						mRowBuilder.add("key", keyValMap.get(keyValPairs[ctr + 1]));
						mRowBuilder.add("value", keyValPairs[ctr + 1]);
						break;
					} else if (ctr + 2 >= keyValPairs.length) {
						MatrixCursor.RowBuilder mRowBuilder = matrixCursor.newRow();
						mRowBuilder.add("key", keyValPairs[ctr]);
						mRowBuilder.add("value", keyValPairs[ctr + 1]);
						break;
					} else {
						keyValMap.put(keyValPairs[ctr + 1], keyValPairs[ctr]);
					}
				} else {
					MatrixCursor.RowBuilder mRowBuilder = matrixCursor.newRow();
					mRowBuilder.add("key", keyValPairs[ctr]);
					mRowBuilder.add("value", keyValPairs[ctr + 1]);

				}
				ctr += 2;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return matrixCursor;
	}

	private class ServerTask extends AsyncTask<ServerSocket, String, Void>{

		@Override
		protected Void doInBackground(ServerSocket... sockets) {
			ServerSocket serverSocket = sockets[0];
			try{
				while(true){
					Socket client = serverSocket.accept();
					BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
					PrintWriter p1 = new PrintWriter(client.getOutputStream(), true);
					String msg;
					Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledynamo.provider");

					if((msg = reader.readLine()) != null) {
						System.out.println("Message: "+msg);

						if(msg.contains("INSERT_NODE")) {
							String ackMsg = cPort+"_INSERTED";

							ContentValues mContentValues = new ContentValues();
							mContentValues.put("key", msg.split(":")[1]);
							String values = msg.split(":")[2]+"-INSERT";
							System.out.println(values+" INSERT");
							mContentValues.put("value", values);

							insert(mUri, mContentValues);
							p1.println(ackMsg);
						} else if(msg.contains("INSERT_REPLICA")){
							String ackMsg = cPort+"_REPLICATION_DONE";

							ContentValues mContentValues = new ContentValues();
							mContentValues.put("key", msg.split(":")[1]);

							String values = msg.split(":")[2]+"-REPLICA";
							System.out.println(values+" REPLICA");
							mContentValues.put("value", values);

							insert(mUri, mContentValues);

							p1.println(ackMsg);
						} else if(msg.contains("QUERY_NODE")){
							String ackMsg = cPort+"_KEY_VALUE";

							Cursor resultCursor = query(mUri, null, msg.split(":")[1], null, null);


							MatrixCursor matrixCursor = (MatrixCursor)resultCursor;
							String a1 = "";

							int key = matrixCursor.getColumnIndex("key");
							int value = matrixCursor.getColumnIndex("value");
							matrixCursor.moveToFirst();
							if (matrixCursor.getCount() > 0) {
								do {
									String returnKey = matrixCursor.getString(key);
									String returnValue = matrixCursor.getString(value);
									a1+=returnKey + ":" + returnValue + ":";
								} while (matrixCursor.moveToNext());

								matrixCursor.close();
								if (a1.length() > 0) {
									a1=a1.substring(0,a1.length() - 1);
								}
							}

							ackMsg+="::"+a1;

							p1.println(ackMsg);
						} else if(msg.contains("REPLICA_Q")){
							String ackMsg = cPort+"_KEY_VALUE";

							String[] queryReplica = {"REPLICA_Q"};
							Cursor resultCursor = query(mUri, null, msg.split(":")[1], queryReplica, null);

							MatrixCursor matrixCursor = (MatrixCursor)resultCursor;
							String a1 = "";

							int key = matrixCursor.getColumnIndex("key");
							int value = matrixCursor.getColumnIndex("value");
							matrixCursor.moveToFirst();
							if (matrixCursor.getCount() > 0) {
								do {
									String returnKey = matrixCursor.getString(key);
									String returnValue = matrixCursor.getString(value);
									a1+=returnKey + ":" + returnValue + ":";
								} while (matrixCursor.moveToNext());

								matrixCursor.close();
								if (a1.length() > 0) {
									a1=a1.substring(0,a1.length() - 1);
								}
							}

							ackMsg+="::"+a1;



							p1.println(ackMsg);
						} else if(msg.contains("NODE_D")){
							String ackMsg = cPort+"_DELETED";

							int count = delete(mUri, msg.split(":")[1], null);
							ackMsg+="::" + count;

							p1.println(ackMsg);
						} else if(msg.contains("REPLICA_D")){
							String ackMsg = cPort+"_DELETED";

							String[] replica = {"REPLICA"};
							int count = delete(mUri, msg.split(":")[1], replica);
							ackMsg+="::" + count;

							p1.println(ackMsg);
						} else if(msg.contains("REJOIN")){
							String ackMsg = cPort+"_REJOIN_KEYVALUE";


							System.out.println("MSG_ACK: "+msg);
							String rejoinedNode = msg.split("_")[1];

							System.out.println("Rejoined Node: "+rejoinedNode);

							String keyValPairs = "";

							if(node.getPred1().equals(rejoinedNode)){


								String result = "";
								for(Map.Entry<String,String> keyVal : pred1map.entrySet()){
									result+=keyVal.getKey()+":"+keyVal.getValue()+":";
								}
								if(result.length() > 0)
									result=result.substring(0,result.length() - 1);

								keyValPairs+=result;

								pred1map.clear();
							} else if(node.getPred2().equals(rejoinedNode)){

								String result = "";
								for(Map.Entry<String,String> keyVal : pred2map.entrySet()){
									result+=keyVal.getKey()+":"+keyVal.getValue()+":";
								}
								if(result.length() > 0)
									result=result.substring(0,result.length() - 1);

								keyValPairs+=result;


								pred2map.clear();
							} else if(node.getSucc1().equals(rejoinedNode)){

								String result = "";
								for(Map.Entry<String,String> keyVal : cur_map_stat.entrySet()){
									result+=keyVal.getKey()+":"+keyVal.getValue()+":";
								}
								if(result.length() > 0)
									result=result.substring(0,result.length() - 1);

								keyValPairs+=result;


								if(keyValPairs.length() != 0 && keyValPairs.contains(":"))
									keyValPairs+=":";

								cur_map_stat.clear();


								result = "";
								for(Map.Entry<String,String> keyVal : succ1Map.entrySet()){
									result+=keyVal.getKey()+":"+keyVal.getValue()+":";
								}
								if(result.length() > 0)
									result=result.substring(0,result.length() - 1);

								keyValPairs+=result;

								succ1Map.clear();
							} else{

								String result = "";
								for(Map.Entry<String,String> keyVal : cur_map_stat.entrySet()){
									result+=keyVal.getKey()+":"+keyVal.getValue()+":";
								}
								if(result.length() > 0){result=result.substring(0,result.length() - 1);}

								keyValPairs+=result;



								if(keyValPairs.length() != 0 && keyValPairs.contains(":"))
									keyValPairs+=":";

								cur_map_stat.clear();

								result = "";
								for(Map.Entry<String,String> keyVal : succ2Map.entrySet()){
									result+=keyVal.getKey()+":"+keyVal.getValue()+":";
								}
								if(result.length() > 0)
									result=result.substring(0,result.length() - 1);

								keyValPairs+=result;


								succ2Map.clear();
							}
							ackMsg+="::" + keyValPairs;

							p1.println(ackMsg);

						} else {
							p1.println("acknowledgement");
							publishProgress(msg);
						}
					}
					
				}
			}  catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
	}




	private class ClientTask extends AsyncTask<String, Void, Void>{
		@Override
		protected Void doInBackground(String... params) {
			for(String remotePort : node_List) {
				try {               
					Socket client = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(remotePort) * 2);
					String message = params[1];
					PrintWriter p1 = new PrintWriter(client.getOutputStream(), true);
					BufferedReader b1 = new BufferedReader(new InputStreamReader(client.getInputStream()));
					p1.println(message);
					Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledynamo.provider");
					while (!client.isClosed()) {
						String ack = b1.readLine();
						if(ack == null){
							client.close();
							continue;
						}

						if (ack.contains(remotePort + "_REJOIN_KEYVALUE")) {

							client.close();
							if (ack.split("::").length > 1) {
								String[] keyVals = ack.split("::")[1].split(":");
								int ctr = 0;
								int len = keyVals.length;
								while (ctr + 1 < len) {
									if(!insertedMap.containsKey(keyVals[ctr]) || (insertedMap.containsKey(keyVals[ctr])
											&& Long.parseLong(insertedMap.get(keyVals[ctr]).split("@")[1])< Long.parseLong(keyVals[ctr + 1].split("@")[1]))) {

										ContentValues mContentValues = new ContentValues();
										mContentValues.put("key", keyVals[ctr]);


										String values = keyVals[ctr + 1].split("@")[0]+"-REJOIN";
										mContentValues.put("value", values);
										System.out.println(values+" REJOIN");

										insert(mUri, mContentValues);
									}
									ctr += 2;
								}
							}
						} else if (ack.contains("acknowledgement" + remotePort)) {
							client.close();
						} 
					}

					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			isQuery = false;
			return null;
		}

	}


	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String genHash(String input) throws NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte[] sha1Hash = sha1.digest(input.getBytes());
		Formatter formatter = new Formatter();
		for (byte b : sha1Hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

}