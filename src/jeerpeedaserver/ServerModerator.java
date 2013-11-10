package jeerpeedaserver;

import java.util.*;
import java.net.*;
import java.text.*;

//Authored by Team Jeerpeeda
//Build v0.2
//11082013

public class ServerModerator extends Thread {

    //We build containers for our warriors & for holding queue entries
    private final ArrayList<WarriorObj> warriors = new ArrayList();
    private final ArrayList<String> battleQueue = new ArrayList();
    
    //Construct the standard combat menu
    private final String combatMenu = "1. Slash\n2. Stab\n3. Block\n4. Parry (riposte)\n";
    
    //Method for adding new warriors to the moderator & log
    public synchronized void addWarrior(WarriorObj newWarrior) {
        warriors.add(newWarrior);

        battleQueue.add("(server_message)-" + getTimeStamp() + ": New client/warrior connection from" + 
                "\nAddress (IP:Port):  " + getClientAddress(newWarrior) + "\n\n");
        notify();
}
    
    //Method for deleting warriors from the server.  Makes announcement of a death.
    public synchronized void deleteWarrior(WarriorObj existingWarrior)
    {
        int clientIndex = warriors.indexOf(existingWarrior);
        if (clientIndex != -1)
           warriors.remove(clientIndex);
        
        battleQueue.add("(server_message)-" + getTimeStamp() + ": Client/warrior connection from" + 
                "\nAddress (IP:Port):  " + getClientAddress(existingWarrior) + "\n\n");
        notify();
    }
 
    /*
     * Adds given message to the moderator's battle queue with a tag of the
     * ID of the warrior that sent the message.  For example, "0:Kick-1" would
     * signify warrior 0 kicking warrior 1.
     */
    public synchronized void warriorCommandsQueue(WarriorObj existingWarrior, String newCommand)
    {
        newCommand = warriors.indexOf(existingWarrior) + ":" + newCommand;
        battleQueue.add(newCommand);
        notify();
    }
 
    /*
     * Pulls next command from queue stack in battleQueue, returns the value to
     * the run() method for parsing and finally removes the command from queue
     */
    private synchronized String getNextMessageFromQueue() throws InterruptedException
    {
        while (battleQueue.isEmpty())
           wait();
        String warriorCommand = (String) battleQueue.get(0);
        battleQueue.remove(0);
        return warriorCommand;
    }
 
    /**
     * Sends given message to all clients in the client list. Actually the
     * message is added to the client sender threads message queue and this
     * client sender thread is notified.
     */
    private synchronized void sendMessageToAllClients(String aMessage)
    {
        for (int i=0; i<warriors.size(); i++) {
           WarriorObj clientInfo = (WarriorObj) warriors.get(i);
           clientInfo.thisWarriorSender.sendMessage(aMessage);
        }
    }
    
    /**
     * Sends given message to specific client. Actually the
     * message is added to the client sender threads message queue and this
     * client sender thread is notified.
     */
    private synchronized void sendMessageToClient(WarriorObj existingWarrior, String aMessage)
    {
           existingWarrior.thisWarriorSender.sendMessage(aMessage);
    }    

    public String getClientAddress(WarriorObj existingWarrior)
    {
        Socket socket = existingWarrior.warriorSocket;
        String senderIP = socket.getInetAddress().getHostAddress();
        String senderPort = "" + socket.getPort();
        return senderIP + ":" + senderPort;
    }
    
    public String getTimeStamp()
    {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
        String formattedDate = sdf.format(date);
        return formattedDate;
    }
    
    public String getWarriorList(WarriorObj thisWarrior)
    {
        String warriorList = "Choose a Warrior to attack:\n";
        for (int i=0; i<warriors.size(); i++) {
           WarriorObj clientInfo = (WarriorObj) warriors.get(i);
           
           //Skip adding the requesting warrior to their own attack menu! 
           if (thisWarrior.warriorName == clientInfo.warriorName)
                continue;
           warriorList = warriorList + i + ":" + clientInfo.warriorName + "\n";
        }
        
        if (warriors.size() > 1)
            return warriorList;
        else
            return "Waiting for more warriors...";
    }    
    
    public String getWarriorHealth(WarriorObj thisWarrior)
    {
        String warriorHealth = thisWarrior.warriorName + ", your current health is " +
                thisWarrior.warriorHealth + "\n";
        return warriorHealth;
    }        
    
    public void parseWarriorCommand(String warriorCommand)
    {
        /* Gather the warrior object for this command by first parsing out the
        * Warrior's ID from the command String and then retrieving the specific Warrior
        */
        int warriorID = Integer.parseInt(warriorCommand.substring(0, warriorCommand.indexOf(":")));
        String parsedWarriorCommand = warriorCommand.substring(warriorCommand.indexOf(":")+1);
                
        WarriorObj thisWarrior = (WarriorObj) warriors.get(warriorID);
        
        /* If this warrior command contains their chosen name, assign it to the object
           and issue a public announcement to the clients about the new combatant */ 
        if (parsedWarriorCommand.contains("(warrior_name)"))
        {
            thisWarrior.warriorName = parsedWarriorCommand.substring(parsedWarriorCommand.indexOf(")")+1);
            sendMessageToAllClients("(combat moderator) A new Warrior has arrived!\n" +
                       "(combat moderator) All Hail " + thisWarrior.warriorName + "!!\n");
            //Send the user's warrior health & the warrior list
            sendMessageToClient(thisWarrior, getWarriorHealth(thisWarrior));
            sendMessageToClient(thisWarrior, getWarriorList(thisWarrior));
        }
        else
        {
            
        }
            
    }    
    
    
    
    /**
     * Infinitely reads messages from the queue and decide whether the command
     * is for clients or server recording only.
     */
    public void run()
    {
        //Initialized server message (one-time only)
        battleQueue.add("(server_message)-" + getTimeStamp() + " The server has been initiated\n\n");
        try {
           while (true) {
               String warriorCommand = getNextMessageFromQueue();
               
               //Parse the next queue command; if it's a local server message,
               //then output to local server log/interface
               if (warriorCommand.startsWith("(server_message)"))
                    System.out.println(warriorCommand);
               else
                    parseWarriorCommand(warriorCommand);
           }
        } catch (InterruptedException ie) {
           // Thread interrupted. Stop its execution
        }
    }
}
