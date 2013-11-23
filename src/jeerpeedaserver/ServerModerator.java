package jeerpeedaserver;

import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.util.Random; 

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
        String attackerWeapon = currentConflict.Weapon;
        WarriorObj targetWarrior = currentConflict.Target;
        String targetWeapon = currentConflict.Defence;
        int blockSuccesful = 0;
        int swordDamage = 20;
        int knifeDamage = 10;
        int axeDamage = 20;
        int oneOfThreeAttackOptions = 0;
        String attackOption = attackerWeapon.toUpperCase();

        if ( targetWeapon.equalsIgnoreCase("BLOCK") )
          {
            Random rand = new Random(); 
            int fiftyPercentChanceToBlock = rand.nextInt(100);
            
            if (fiftyPercentChanceToBlock > 50)
              { // reduce damage taken
                blockSuccesful = 10; //reduce attack
              }                     
          }// end of defence check
        else
        { // target did not defend can be be change later to different value
            blockSuccesful = 0;
        } // end of blocking
        
        if (attackOption.equals("SWORD"))
        { // check for weapong selection
            oneOfThreeAttackOptions = 1;            
        }
        else if (attackOption.equals("KNIFE"))
        { // check for weapong selection
            oneOfThreeAttackOptions = 2; 
        }
        else if (attackOption.equals("AXE"))
        { // check for weapong selection
            oneOfThreeAttackOptions = 3; 
        }
        else
        {
            oneOfThreeAttackOptions = 0; 
        }
        if ((attackerWarrior.HealthLevel<=0) || (targetWarrior.HealthLevel<=0))
                { // check to see if a warrior is dead
                    if (attackerWarrior.HealthLevel==0)
                    { // check on attackers health
                        MessageContainer attackerMessage = new MessageContainer(targetWarrior, "Warrior " + attackerWarrior.WarriorName +
                         " is dead and cannot attack  " + targetWarrior.WarriorName + ".");                    
                        battleQueue.add(attackerMessage);
                    }
                    else
                    { // then the targets health is low
                         MessageContainer targetMessage = new MessageContainer(attackerWarrior, "Warrior " +  targetWarrior.WarriorName + 
                           " is dead and cannot be attacked by " + attackerWarrior.WarriorName + ".");
                         battleQueue.add(targetMessage);
                    }
                    //
                    notify();
                } // end of if statement to check on warriors health
                else
                { // begin of one of 3 attacks
                    
                 switch (oneOfThreeAttackOptions) 
                 {
                  case 1:                    
                    targetWarrior.HealthLevel= (targetWarrior.HealthLevel - swordDamage) + blockSuccesful;	
                    break;                      
                  case 2:
                    targetWarrior.HealthLevel = (targetWarrior.HealthLevel - knifeDamage) + blockSuccesful;
                    break;
                  case 3:
                    targetWarrior.HealthLevel = (targetWarrior.HealthLevel - axeDamage) + blockSuccesful;
                    break; 
                  case 0:
                  { 
                    MessageContainer attackerMessage = new MessageContainer(attackerWarrior, " Unkown attack with " +
                    currentConflict.Defence + ". Conflict is over.  Your new health level is " + attackerWarrior.HealthLevel + ".");
                    battleQueue.add(attackerMessage);
                    break;
                  
                  }
                  default:
                    {
                    MessageContainer attackerMessage = new MessageContainer(attackerWarrior, " Unkown attack with " +
                    currentConflict.Defence + ". Conflict is over.  Your new health level is " + attackerWarrior.HealthLevel + ".");
                    battleQueue.add(attackerMessage);
                    break;
                    }
                 } // end of attach switch statement  
                
                 // attackerWarrior.HealthLevel = attackerWarrior.HealthLevel + 10;
                 // targetWarrior.HealthLevel = targetWarrior.HealthLevel - 10;
        
                 MessageContainer attackerMessage = new MessageContainer(attackerWarrior, targetWarrior.WarriorName + " Defended with " +
                    currentConflict.Defence + ". Conflict is over.  Your new health level is " + attackerWarrior.HealthLevel + ".");
                 battleQueue.add(attackerMessage);
                 MessageContainer targetMessage = new MessageContainer(targetWarrior, "Conflict with " + 
                    attackerWarrior.WarriorName + "  is over.  Your new health level is " + targetWarrior.HealthLevel + ".");
                 battleQueue.add(targetMessage);
                 notify();
                  } // end of 'else' where one of 3 attacks
        attackerWarrior.Status = "Ready";
        targetWarrior.Status = "Ready";
        targetWarrior.currentConfict = null;
        // set the attacker and target warriors status ready for more fights
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
        //Update warrior file before removing warrior
        CloseWarriorFile(existingWarrior);
        
        
        int clientIndex = warriors.indexOf(existingWarrior);
        if (clientIndex != -1)
           warriors.remove(clientIndex);
        
        DisplayWarriorList();
    }
 
    public synchronized void warriorIsDead(WarriorObj warrior)
    {
        //Send message to everyone that warrior is dead
        String message = "Warrior " + warrior.WarriorName + " is dead!";
        battleQueue.add(new MessageContainer(null, message));
        notify();
        
        deleteWarrior(warrior);
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
                           // found warrior name
                            //Load Warrior
                            warrior = OpenWarriorFile(warrior);
                            if (warrior.HealthLevel == 0)
                            { 
                              // check if warroir is alive
                            moderatorMessage = "Warrior " + warrior.WarriorName + " is dead and cannot fight.\n";
                            warrior.Status = "Dead";
                            deleteWarrior(warrior);
                            messageContainer = new MessageContainer(warrior, moderatorMessage);
                            battleQueue.add(messageContainer);
                            notify();
                            
                            }
                            else
                            {
                            moderatorMessage = "Welcome new Warrior. All Hail " + warrior.WarriorName + "!!!\n" +
                                "(To attack a warrior, enter a warrior name, colon (:), then weapon [Sword, Knife, or Axe]. i.e.'name:weapon')";
                            }
                        }  // found warrior name                  
                    }
                    else
                    {                           //Reask question
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
                            "(To attack a warrior, enter a warrior name, colon (:), then weapon [Sword, Knife, or Axe]. i.e.'name:weapon')";
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
        } // end of if warrior.IsRegistered
        else if(warriorMessage.contains(":"))
        {
            //This is an attacking message
            String[] subStrings = warriorMessage.split(":");
            String targetName = subStrings[0];
            String weapon = subStrings[1];
            String attackerWeaponChoose;
            attackerWeaponChoose = weapon.toUpperCase();
            WarriorObj targetWarrior = FindWarriorByName(targetName);
            
            if(targetWarrior != null)
            {
                if ( (attackerWeaponChoose.equals("SWORD")) || (attackerWeaponChoose.equals("KNIFE")) || (attackerWeaponChoose.equals("AXE")) )
                { // checking on attacking warriors attack option
                warrior.Status = "Attacking";
                targetWarrior.Status = "Defending";
                MessageContainer attackerMessage = new MessageContainer(warrior, "You are currently attacking " + 
                        targetWarrior.WarriorName + " with a " + weapon + ".");
                battleQueue.add(attackerMessage);
                MessageContainer targetMessage = new MessageContainer(targetWarrior, "You are currently be attacked by " + 
                        warrior.WarriorName + " with a " + weapon + ".\n"+
                        "To defend this attack type in (block):");
                battleQueue.add(targetMessage);
                notify();
                
                Conflict conflict = new Conflict(warrior, targetWarrior, attackerWeaponChoose);
                targetWarrior.currentConfict = conflict;
                
                } // end of if statement
             else
             {
                MessageContainer attackerMessage = new MessageContainer(warrior, "You are currently attacking " + 
                targetWarrior.WarriorName + " with a unknow weapon try again.");
                battleQueue.add(attackerMessage);   
             }
                
            } // end of if traget name is not null
                        
            else
            {
                messageContainer = new MessageContainer(warrior, "Could not find Warror: " + targetName);
                battleQueue.add(messageContainer);
                notify();
            }
        } // end of if warriorMessage.contains ":"
        else if(warrior.currentConfict != null)
        {
            //This is a responce to a warrior that is already in a conflict on the battle feild
            Conflict currentConflict = warrior.currentConfict;
            currentConflict.Defence = warriorMessage;
            ResolveConflict(currentConflict);
            
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
           } // end of while loop
        } catch (InterruptedException ie) {
           // Thread interrupted. Stop its execution
        }
    } // end of void run
    
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
                warriorInfo += "HealthLevel=" + warrior.HealthLevel;
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
    } // private WarriorObj OpenWarriorFile
    
    
    
    private void CloseWarriorFile(WarriorObj warrior)
    {
        String warriorInfo = "";
        //Will read file under JeerpeedaServer Directory.  Added System.getProperty("user.dir") just to make sure we read the right loaction
        String filePath = System.getProperty("user.dir") + "/" + warrior.WarriorName + ".wdat";
        Path path = FileSystems.getDefault().getPath(filePath);

        //use file class to check if file exist
        File file = new File(filePath);
        if (file.isFile() && file.canRead())
        {
            Writer writer = null;

            try 
            {
                //Overide old file with new file
                writer = new BufferedWriter( new FileWriter( filePath));
                
                warriorInfo = "WarriorName=" + warrior.WarriorName;
                warriorInfo += "HealthLevel=" + warrior.HealthLevel;
                warriorInfo += ";PlaceOfOrigin=" + warrior.PlaceOfOrigin;
                warriorInfo += ";Description=" + warrior.Description;
                writer.write(warriorInfo);
                
            } 
            catch (IOException ex) 
            {
                 // report
            } 
            finally 
            {
                try {writer.close();} catch (Exception ex) {}
            }
        }
        else
        {
            //File not found.  This should not happen because every warrior has a file
            
        }    
            
    } // private Void CloseWarriorFile
    
    
    
} // end of public class ServerModerator 
