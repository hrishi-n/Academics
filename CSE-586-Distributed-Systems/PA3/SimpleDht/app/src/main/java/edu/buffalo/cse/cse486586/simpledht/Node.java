package edu.buffalo.cse.cse486586.simpledht;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Node {
    String nodeID;
    String predecessor;
    String successor;


    public boolean checkCondition(String id,Node n, String largestNode){

        try{
            //convert text
            BigInteger idVal = new BigInteger(id, 16);;
            BigInteger maxNodeVal = new BigInteger(largestNode, 16);
            BigInteger nodeVal = new BigInteger((genHash1(n.nodeID)), 16);
            BigInteger successorVal = new BigInteger((genHash1(n.successor)), 16);



            if((idVal.compareTo(nodeVal) > 0 && idVal.compareTo(successorVal) <= 0)||
                    (idVal.compareTo(nodeVal) > 0 && idVal.compareTo(successorVal) > 0 && nodeVal.compareTo(successorVal) > 0)||
                    (idVal.compareTo(nodeVal) < 0 && idVal.compareTo(successorVal) < 0 && nodeVal.compareTo(successorVal) > 0)||
                    (nodeVal.compareTo(maxNodeVal) == 0 && idVal.compareTo(nodeVal) < 0 && idVal.compareTo(successorVal) < 0)) {
                return true;
            }
            else {
                return false;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    private String genHash1(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}