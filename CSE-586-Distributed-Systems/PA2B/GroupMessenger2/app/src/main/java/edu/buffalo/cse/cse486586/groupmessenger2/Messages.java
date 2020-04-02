package edu.buffalo.cse.cse486586.groupmessenger2;

public class Messages implements Comparable<Messages>{
    private int seqNum;
    private String message;
    private int portNum;
    private boolean d_status;


    public int getportNum() {
        return portNum;
    }

    public void setportNum(int portNum) {
        this.portNum = portNum;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
    }

    public boolean isd_status() {
        return d_status;
    }

    public void setd_status(boolean d_status) {
        this.d_status = d_status;
    }

    public Messages(int portNum, String message, int seqNum, boolean d_status){
        this.portNum = portNum;
        this.message = message;
        this.seqNum = seqNum;
        this.d_status = d_status;
    }



    //order according to seqnum, if both are same then delivaery status or else, order by portnum(X.Y)
    @Override
    public int compareTo(Messages m2) {
        if (this.getSeqNum() > m2.getSeqNum())
            return 1;
        else if (this.getSeqNum() < m2.getSeqNum())
            return -1;
        if(!m2.isd_status() && this.isd_status())
            return 1;
        else if(!this.isd_status() && m2.isd_status())
            return -1;
        if (this.getportNum() > m2.getportNum())
            return 1;
        else
            return -1;

    }


}