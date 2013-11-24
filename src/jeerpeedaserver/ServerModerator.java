package jeerpeedaserver;

import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.file.*;

//Authored by Team Jeerpeeda
//Build v0.1
//11082013

public class ServerModerator extends Thread {

    //We build containers for our warriors & for transferring battle actions/updates
    private final ArrayList<WarriorObj> warriors = new ArrayList();
    private final ArrayList<MessageContainer> battleQueue = new ArrayList();
    
    //Method for adding new warriors to the moderator
    public synchronized void addWarrior(WarriorObj newWarrior) 
    {
        warriors.add(newWarrior);
        //Set the id of the warrior to be the list size
        newWarrior.WarriorID = warriors.size();
        
        newWarrior.Status = "NeedName";
        sendMessageToWarrior(new MessageContainer(newWarrior, "Welcome new Warrior.  Please enter a warrior name:"));
    }
    private WarriorObj FindWarriorByName(String name)
    {
        WarriorObj findWarrior = null;
        for (WarriorObj warrior: warriors)
        {
            if(warrior.WarriorName.equalsIgnoreCase(name))
            {
                findWarrior = warrior;
                break;
            }
        }
        return findWarrior;
    }
    
    private void ResolveConflict(Conflict currentConflict)
    {
        //ToDo deterime Helth of attacker and target.  For now, hardcode 10
        WarriorObj attackerWarrior = currentConflict.Attacker;
        WarriorObj targetWarrior = currentConflict.Target;

        targetWarrior.HealthLevel = targetWarrior.HealthLevel - 10;
        
        MessageContainer attackerMessage = new MessageContainer(attackerWarrior, targetWarrior.WarriorName + " Defended with " +
                currentConflict.Defence + ". Conflict is over.  Your new health level is " + attackerWarrior.HealthLevel + ".");
        battleQueue.add(attackerMessage);
        MessageContainer targetMessage = new MessageContainer(targetWarrior, "Conflict with " + 
                attackerWarrior.WarriorName + "  is over.  Your new health level is " + targetWarrior.HealthLevel + ".");
        battleQueue.add(targetMessage);
        notify();
        
        attackerWarrior.Status = "Ready";
        
        if(targetWarrior.HealthLevel > 0)
            targetWarrior.Status = "Ready";
        UpdateWarriorFile(targetWarrior);
        targetWarrior.currentConfict = null;
    }
    
    //Display List of Registered Warrior to all clients
    public synchronized  void DisplayWarriorList()
    {
        String message = "The BattleFields has changed.  Current Warriors on BattleFields:\n";
        for (int i=0; i<warriors.size(); i++) 
        {
           WarriorObj warrior = (WarriorObj) warriors.get(i);
           if(warrior.IsRegistered)
           {
               message += "Name=" +  warrior.WarriorName + 
                       ", Health=" + warrior.HealthLevel + 
                       ", Place of Origin=" + warrior.PlaceOfOrigin + 
                       ", Description=" + warrior.Description + "\n";
           }
        }
        battleQueue.add(new MessageContainer(null, message));
        notify();
    }
    
    //Method for deleting warriors from the server & battlefield
    public synchronized void deleteWarrior(WarriorObj existingWarrior)
    {
        //Kill the client connection
        existingWarrior.thisWarriorListener.interrupt();
        
        int clientIndex = warriors.indexOf(existingWarrior);
        if (clientIndex != -1)
           warriors.remove(clientIndex);
        
        String warriorInfo = "";
        //Will read file under JeerpeedaServer Directory.  Added System.getProperty("user.dir") just to make sure we read the right loaction
        String filePath = System.getProperty("user.dir") + "/" + existingWarrior.WarriorName + ".wdat";
        Path path = FileSystems.getDefault().getPath(filePath);

        //Permanently delete the warrior's file
        File file = new File(path.toString());
        file.delete();
        
        DisplayWarriorList();
    }
 
    /**
     * Adds given message to the dispatchers message queue and notifies this
     * thread to wake up the message queue reader (getNextMessageFromQueue method).
     * dispatchMessage method is called by other threads (ClientListener) when
     * a message is arrived.
     */
    public synchronized void warriorCommandsQueue(MessageContainer messageContainer)
    {

        WarriorObj warrior = messageContainer.Warrior;
        String warriorMessage = messageContainer.Message;
        String moderatorMessage = "";
        Socket socket = warrior.warriorSocket;
        String senderIP = socket.getInetAddress().getHostAddress();
        String senderPort = "" + socket.getPort();
        
        if(!warrior.IsRegistered)
        {
         
            switch (warrior.Status) 
            {
                case "NeedName":  
                    if(!warriorMessage.isEmpty())
                    {
                        //Set Warrior Property 
                        warrior.WarriorName = warriorMessage;
                        
                        if(!DoesWarriorFileExist(warrior))
                        {
                            //Ask for new Warrior Property
                            moderatorMessage = "Please enter a warrior Place of Origin:";
                            warrior.Status = "NeedPlaceOfOrigin";
                        }
                        else
                        {
                            //Load Warrior
                            warrior = OpenWarriorFile(warrior);
                            moderatorMessage = "Welcome new Warrior. All Hail " + warrior.WarriorName + "!!!\n" +
                                "(To attack a warrior, enter a warrior name, colon, then weapon. i.e.'name:weapon')";;
                        }                    
                    }
                    else
                    {   
                        //Reask question
                        moderatorMessage = "Please enter a warrior name:";
                        warrior.Status = "NeedName";
                        
                    }
                    break;
                case "NeedPlaceOfOrigin":  
                    if(!warriorMessage.isEmpty())
                    {
                        //Set Warrior Property 
                        warrior.PlaceOfOrigin = warriorMessage;
                        //Ask for new Warrior Property
                        moderatorMessage = "Please enter a warrior Description:";
                        warrior.Status = "NeedDescription";                 
                    }
                    else
                    {   
                        //Reask question
                        moderatorMessage = "Please enter a warrior Place of Origin:";
                        warrior.Status = "NeedPlaceOfOrigin";
                    }
                    break; 
                case "NeedDescription":  
                    if(!warriorMessage.isEmpty())
                    {
                        //Set Warrior Property 
                        warrior.Description = warriorMessage;
                        
                        //Load Warrior
                        warrior = OpenWarriorFile(warrior);
                        moderatorMessage = "Welcome new Warrior. All Hail " + warrior.WarriorName + "!!!\n" +
                                "(To attack a warrior, enter a warrior name, colon, then weapon. i.e.'name:weapon')";
                    }
                    else
                    {   
                        //Reask question
                        moderatorMessage = "Please enter a warrior Description:";
                        warrior.Status = "NeedDescription";
                    }

                    break; 
            }
            messageContainer = new MessageContainer(warrior, moderatorMessage);
            battleQueue.add(messageContainer);
            notify();
        }
        else if(warriorMessage.contains(":"))
        {
            //This is an attacking message
            String[] subStrings = warriorMessage.split(":");
            String targetName = subStrings[0];
            String weapon = subStrings[1];
            WarriorObj targetWarrior = FindWarriorByName(targetName);
            if(targetWarrior != null)
            {
                warrior.Status = "Attacking";
                targetWarrior.Status = "Defending";
                MessageContainer attackerMessage = new MessageContainer(warrior, "You are currently attacking " + 
                        targetWarrior.WarriorName + " with a " + weapon + ".");
                battleQueue.add(attackerMessage);
                MessageContainer targetMessage = new MessageContainer(targetWarrior, "You are currently be attacked by " + 
                        warrior.WarriorName + " with a " + weapon + ".\n"+
                        "Enter a defence for this attack:");
                battleQueue.add(targetMessage);
                notify();
                
                Conflict conflict = new Conflict(warrior, targetWarrior, weapon);
                targetWarrior.currentConfict = conflict;
            }
            else
            {
                messageContainer = new MessageContainer(warrior, "Could not find Warror: " + targetName);
                battleQueue.add(messageContainer);
                notify();
            }
        }
        else if(warrior.currentConfict != null)
        {
            //This is a responce to a attack
            Conflict currentConflict = warrior.currentConfict;
            currentConflict.Defence = warriorMessage;
            ResolveConflict(currentConflict);
            
        }
        else if(warriorMessage.contains("ImDead"))
        {
            //This warrior is dead as a doornail- remove it from the array & notify all
                    //Send public message of the death
            MessageContainer killMsg = new MessageContainer(null, "A warrior has fallen.. " + 
                "farewell mighty " + warrior.WarriorName);
            sendMessageToAllClients(killMsg);
            deleteWarrior(warrior);
        }
        
        else
        {
            //Just a message
           /* moderatorMessage = "Warrior:" + warrior.WarriorName + " ID: " + senderIP + ":" + senderPort + " : " + warriorMessage;
            messageContainer.Message = moderatorMessage;
            battleQueue.add(messageContainer);
            notify();
            */
        }

    }

    /**
     * Pulls next command from queue stack in battleQueue and for now
     * just dumps it out to all the clients for testing purposes
     */
    private synchronized MessageContainer getNextMessageFromQueue()
    throws InterruptedException
    {
        while (battleQueue.isEmpty())
           wait();
        MessageContainer messageContainer = (MessageContainer) battleQueue.get(0);
        battleQueue.remove(0);
        return messageContainer;
    }
 
    /**
     * Sends given message to all clients in the client list. Actually the
     * message is added to the client sender threads message queue and this
     * client sender thread is notified.
     */
    private synchronized void sendMessageToAllClients(MessageContainer messageContainer)
    {
        for (int i=0; i<warriors.size(); i++) {
           WarriorObj warrior = (WarriorObj) warriors.get(i);
           warrior.thisWarriorSender.sendMessage(messageContainer);
        }
    }
    
    private synchronized void sendMessageToWarrior(MessageContainer messageContainer)
    {
        WarriorObj warrior = messageContainer.Warrior;
        warrior.thisWarriorSender.sendMessage(messageContainer);
    } 
 
    /**
     * Infinitely reads messages from the queue and dispatch them
     * to all clients connected to the server.
     */
    public void run()
    {
        battleQueue.add(new MessageContainer(null, "The server has been initiated\n"));
        try {
           while (true) {
               MessageContainer messageContainer = getNextMessageFromQueue();
               if(messageContainer.Warrior != null)
               {
                   sendMessageToWarrior(messageContainer);
               }
               else
               {
                   sendMessageToAllClients(messageContainer);
               }
               
               System.out.println(messageContainer);
           }
        } catch (InterruptedException ie) {
           // Thread interrupted. Stop its execution
        }
    }
    
    private Boolean DoesWarriorFileExist(WarriorObj warrior)
    {
        Boolean doesWarriorFileExist = false;
        String filePath = System.getProperty("user.dir") + "/" + warrior.WarriorName + ".wdat";
        File file = new File(filePath);
        if (file.isFile() && file.canRead())
        {
            doesWarriorFileExist = true;
        }
        
        return doesWarriorFileExist;
    }
    
    private WarriorObj OpenWarriorFile(WarriorObj warrior)
    {
        String warriorInfo = "";
        //Will read file under JeerpeedaServer Directory.  Added System.getProperty("user.dir") just to make sure we read the right loaction
        String filePath = System.getProperty("user.dir") + "/" + warrior.WarriorName + ".wdat";
        Path path = FileSystems.getDefault().getPath(filePath);

        //use file class to check if file exist
        File file = new File(filePath);
        if (file.isFile() && file.canRead())
        {
          try 
          {
              InputStream in = null;
                try 
                {
                    in = Files.newInputStream(path);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String fileInput = null;
                    while ((fileInput = reader.readLine()) != null) 
                    {
                        warriorInfo += fileInput;
                    }
                    
                    if(!warriorInfo.isEmpty())
                    {
                         for (String subString: warriorInfo.split(";"))
                         {
                            if(!subString.isEmpty())
                            {
                                String[] subStrings = subString.split("=");
                                String keyString = subStrings[0];
                                String valueString = subStrings[1];
                                
                                 switch (keyString) {
                                    case "WarriorName":  
                                        warrior.WarriorName = valueString;
                                        break;
                                    case "HealthLevel":  
                                        warrior.HealthLevel =  Integer.parseInt(valueString);
                                        break;
                                    case "PlaceOfOrigin":  
                                        warrior.PlaceOfOrigin = valueString;
                                        break;
                                    case "Description":  
                                        warrior.Description = valueString;
                                        break;
                                 }

                            }
                         }
                         warrior.IsRegistered = true;
                         warrior.Status = "Ready";
                         DisplayWarriorList();

                    }
                
                } 
                finally 
                {
                    in.close();
                }
          } 
          catch (IOException ex) 
          {
            // Appropriate error handling here.
          }
        }
        else
        {
            //File not found.  Create File
            BufferedWriter writer = null;
            try
            {
                writer = new BufferedWriter( new FileWriter( filePath));
                warriorInfo = "WarriorName=" + warrior.WarriorName;
                warriorInfo = "HealthLevel=" + warrior.HealthLevel;
                warriorInfo += ";PlaceOfOrigin=" + warrior.PlaceOfOrigin;
                warriorInfo += ";Description=" + warrior.Description;
                writer.write(warriorInfo);
                
                warrior.IsRegistered = true;
                warrior.Status = "Ready";
                DisplayWarriorList();
            }
            catch ( IOException e)
            {
            }
            finally
            {
                try
                {
                    if ( writer != null)
                    writer.close( );
                }
                catch ( IOException e)
                {
                }
            }
        }    
            
            
        return warrior;
    }
    
    private WarriorObj UpdateWarriorFile(WarriorObj warrior)
    {
        String warriorInfo = "";
        //Will read file under JeerpeedaServer Directory.  Added System.getProperty("user.dir") just to make sure we read the right loaction
        String filePath = System.getProperty("user.dir") + "/" + warrior.WarriorName + ".wdat";
        Path path = FileSystems.getDefault().getPath(filePath);

        File file = new File(path.toString());
        file.delete();
        
            BufferedWriter writer = null;
            try
            {
                writer = new BufferedWriter( new FileWriter( filePath));
                warriorInfo = "WarriorName=" + warrior.WarriorName;
                warriorInfo = "HealthLevel=" + warrior.HealthLevel;
                warriorInfo += ";PlaceOfOrigin=" + warrior.PlaceOfOrigin;
                warriorInfo += ";Description=" + warrior.Description;
                writer.write(warriorInfo);
            }
            catch ( IOException e)
            {
            }
            finally
            {
                try
                {
                    if ( writer != null)
                    writer.close( );
                }
                catch ( IOException e)
                {
                }
            }        
        
        return warrior;
    }
}
