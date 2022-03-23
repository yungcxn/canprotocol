package de.can;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;

public class UDPEndpoint {

    int ports[] = new int[]{9830,9831,9832};
    int usingport = 0;

    final String searchcode = "?";
    final String ackcode = "hi";

    private DatagramSocket socket;

    private DatagramPacket receivingpacket;
    private byte[] recbuf = new byte[256];
    private boolean receiving;

    private boolean sending;

    public boolean beginTCP = false;

    public int rand;
    public int targetnum;

    private Thread inThread;
    private Thread senderThread;

    public int tcpport = 0;

    private boolean partnerFound = false;
    private boolean partnerAckd = false;

    public enum State{UNDECIDED, SEND, RECEIVE};

    public State initstate = State.UNDECIDED;

    public void init(){
        Random ran = new Random();
        this.rand = ran.nextInt(2147483647);
        safePrintln("Starting tower with id " + rand);
        for(int port: ports){
            if(usingport == 0){
                try {
                    socket = new DatagramSocket(port);
                    usingport = port;


                } catch (SocketException e) {
                    safePrintln("Port " + port + " didnt work...");
                    e.printStackTrace();
                }
            }

        }
        safePrintln("using port: " + usingport);

        recbuf = new byte[256];
        this.receivingpacket = new DatagramPacket(recbuf, recbuf.length);
        this.receiving = true;
        this.inThread = new Thread(() -> {
            while(receiving){
                try {
                    safePrintln("receiving packet...");
                    socket.receive(receivingpacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                handleReceive();
            }
        });



        this.sending = true;
        this.senderThread = new Thread(() -> {


            while(sending){
                if(!partnerFound && !partnerAckd){
                    sendPartnerSearch();

                }else if(partnerFound && !partnerAckd){
                    sendPartnerAck();
                    if(tempcounter < repeatingafter){
                        sendPartnerSearch();
                    }
                    tempcounter++;

                }else if(partnerFound && partnerAckd){
                    if(tempcounter1 < repeatingafter){
                        sendPartnerAck();
                        tempcounter1++;
                    }else{
                        safePrintln("closing...");
                        this.sending = false;
                        socket.close();
                        beginTCP = true;
                    }

                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        this.senderThread.start();

        this.inThread.start();
    }


    private int tempcounter = 0;
    private int tempcounter1 = 0;
    private int repeatingafter = 5;
    private void sendPartnerAck() {

        safePrintln("Acknowledging partner...");
        String msg = "hi " + targetnum + " " +  rand + " " + usingport;

        ArrayList<DatagramPacket> packetsToSend = new ArrayList<>();

        for(int port: ports){
            if(port != usingport){
                try {
                    packetsToSend.add(new DatagramPacket(msg.getBytes(), msg.getBytes().length, InetAddress.getByName("localhost"), port));

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }


            }
        }

        try {
            for(DatagramPacket p: packetsToSend){
                safePrintln("sending: " + msg);
                socket.send(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendPartnerSearch() {

        safePrintln("Search partner...");
        String msg = "? " + rand;

        ArrayList<DatagramPacket> packetsToSend = new ArrayList<>();

        for(int port: ports){
            if(port != usingport){
                try {
                    packetsToSend.add(new DatagramPacket(msg.getBytes(), msg.getBytes().length, InetAddress.getByName("localhost"), port));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            for(DatagramPacket p: packetsToSend){
                safePrintln("sending: " + msg);
                socket.send(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void handleReceive(){

        String received = new String(receivingpacket.getData(), 0, receivingpacket.getLength());
        safePrintln("received: " + received);
        if(received.startsWith(searchcode)){
            this.targetnum = Integer.parseInt(received.split(" ")[1]);
            partnerFound = true;
            safePrintln("Targetnum: " + targetnum);
        }
        if(received.startsWith(ackcode)){
            safePrintln("ackcode received " + received + "; is " + targetnum + " == " + received.split(" ")[2]);
            if(this.targetnum == Integer.parseInt(received.split(" ")[2])){
                if(this.rand > targetnum){
                    tcpport = Integer.parseInt(received.split(" ")[3]);
                    this.initstate = State.SEND;
                }else{
                    tcpport = usingport;
                    this.initstate = State.RECEIVE;
                }

                partnerAckd = true;
                initTCP();
            }

        }
    }


    public void initTCP(){
        safePrintln("TCP connection... " + initstate.toString());
        this.receiving = false;

    }

    public void safePrintln(String s) {
        synchronized (System.out) {
            System.out.println(s);
        }
    }

}
