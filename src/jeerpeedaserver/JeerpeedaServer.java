package jeerpeedaserver;

import java.io.*;
import java.net.*;

//Authored by Team Jeerpeeda
//Build v0.3
//11082013

public class JeerpeedaServer {

    public static void main(String[] args) {
        int listenPort = 1025;

        //Open the server socket on our specified port
        ServerSocket serverSocket = null;
        try { 
            serverSocket = new ServerSocket(listenPort);
        } catch (IOException e) {
        System.err.println("Could not open server on TCP port " + listenPort);
        System.exit(-1);
        }
        
        //Fire up the combat moderator on a new thread
        ServerModerator serverMod = new ServerModerator();
        serverMod.start();
    
        while (true) {
           try {
               Socket socket = serverSocket.accept();
               WarriorObj warriorInfo = new WarriorObj();
               warriorInfo.warriorSocket = socket;
               WarriorListener warriorListener =
                   new WarriorListener(warriorInfo, serverMod);
               WarriorSender warriorSender =
                   new WarriorSender(warriorInfo, serverMod);
               warriorInfo.thisWarriorListener = warriorListener;
               warriorInfo.thisWarriorSender = warriorSender;
               warriorListener.start();
               warriorSender.start();
               serverMod.addWarrior(warriorInfo);
           } catch (IOException ioe) {
               ioe.printStackTrace();
           }
        }    
    }
}
