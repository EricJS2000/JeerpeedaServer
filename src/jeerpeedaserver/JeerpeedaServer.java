package jeerpeedaserver;

import java.io.*;
import java.net.*;

//Authored by Team Jeerpeeda
//Build v0.1
//11082013

public class JeerpeedaServer {

    public static void main(String[] args) {
        int listenPort = 2020;

                //Open the server socket on our specified port
        ServerSocket serverSocket = null;
        try { 
            serverSocket = new ServerSocket(listenPort);
        } catch (IOException e) {
        System.err.println("Could not open server on TCP port " + listenPort + " Reason:" + e.getMessage());
        System.exit(-1);
        }
        
        //Fire up the combat moderator on a new thread
        ServerModerator serverMod = new ServerModerator();
        serverMod.start();
    
        while (true) {
           try {
               Socket socket = serverSocket.accept();
               WarriorObj newWarrior = new WarriorObj();
               newWarrior.warriorSocket = socket;
               WarriorListener warriorListener =
                   new WarriorListener(newWarrior, serverMod);
               WarriorSender warriorSender =
                   new WarriorSender(newWarrior, serverMod);
               newWarrior.thisWarriorListener = warriorListener;
               newWarrior.thisWarriorSender = warriorSender;
               warriorListener.start();
               warriorSender.start();
               serverMod.addWarrior(newWarrior);
           } 
           catch (IOException ioe)
           { // to catch any errors
               ioe.printStackTrace();
           }
        } // end of while loop   
    } // end of main
} // end of public class JeerpeedaServer 
