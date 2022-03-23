package de.can;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPEndpoint {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public UDPEndpoint.State state;

    private int port;

    public String dataArr = "";
    public String lastSent = "";

    private Thread readerThread;


    private boolean receiving = false;

    public TCPEndpoint(UDPEndpoint.State state, int port){

        this.state = state;
        this.port = port;

    }

    public void init(){
        try {
            if (this.state == UDPEndpoint.State.RECEIVE) {
                waitForCon();
            } else if (this.state == UDPEndpoint.State.SEND) {
                beginCon();
            } else {
                System.out.println("Error, TCP state undecided");
            }

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            receiving = true;

            this.readerThread = new Thread(() -> {
                while(receiving){
                    try {
                        lastSent = in.readLine();
                        dataArr += "\n" + lastSent;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            this.readerThread.start();

        }catch(Exception e){
            e.printStackTrace();
            stop();
        }

        System.out.println("Init done");


    }

    private void waitForCon() throws IOException {
        System.out.println("Waiting for incoming req...");

        serverSocket = new ServerSocket(port);
        clientSocket = serverSocket.accept();

        System.out.println("connection established!");


    }


    private boolean forceConnection = true;
    private void beginCon() throws IOException, InterruptedException {
        while(forceConnection){
            try{
                clientSocket = new Socket("localhost", port);

                forceConnection = false;
            }catch(ConnectException e){
                System.out.print("Connection didnt work, waiting 500ms...");
                Thread.sleep(500);
            }
        }
        System.out.println("connection established!");
    }

    private void stop(){
        this.receiving = false;

        try {
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendln(String str){
        System.out.println("sending " + str + " via tcp");
        this.out.println(str);
    }



}
