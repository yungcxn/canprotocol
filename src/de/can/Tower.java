package de.can;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;


public class Tower {


    public Tower(){

    }

    public void init(){
        UDPEndpoint udp = new UDPEndpoint();
        udp.init();

        while(!udp.beginTCP){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        TCPEndpoint tcp = new TCPEndpoint(udp.initstate, udp.tcpport);
        tcp.init();


        tcp.sendln("Was geeeeht von " + udp.rand);

        String lastprinted = "";

        while(true){

            if(lastprinted != tcp.lastSent){
                System.out.println(tcp.lastSent + " - got sent");
                lastprinted = tcp.lastSent;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


}
