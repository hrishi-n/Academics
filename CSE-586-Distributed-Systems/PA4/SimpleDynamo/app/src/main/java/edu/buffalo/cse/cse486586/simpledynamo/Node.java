package edu.buffalo.cse.cse486586.simpledynamo;

public class Node {
    private String nodeID;
    private String pred1;

    public String getNodeID() {
        return nodeID;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    public String getPred1() {
        return pred1;
    }

    public void setPred1(String pred1) {
        this.pred1 = pred1;
    }

    public String getSucc1() {
        return succ1;
    }

    public void setSucc1(String succ1) {
        this.succ1 = succ1;
    }

    public String getPred2() {
        return pred2;
    }

    public void setPred2(String pred2) {
        this.pred2 = pred2;
    }

    public String getSucc2() {
        return succ2;
    }

    public void setSucc2(String succ2) {
        this.succ2 = succ2;
    }

    private String succ1;
    private String pred2;
    private String succ2;
}