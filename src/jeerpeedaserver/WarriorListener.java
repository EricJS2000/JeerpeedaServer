package jeerpeedaserver;

import java.io.*;
import java.net.*;
 
public class WarriorListener extends Thread
{
    private final ServerModerator thisServerModerator;
    private final WarriorObj thisWarriorInfo;
    private final BufferedReader mIn;
 
    public WarriorListener(WarriorObj thisWarrior, ServerModerator newServerModerator)
    throws IOException
    {
        thisWarriorInfo = thisWarrior;
        thisServerModerator = newServerModerator;
        Socket socket = thisWarrior.warriorSocket;
        mIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
 
    /**
     * Until interrupted, reads messages from the client socket, forwards them
     * to the server dispatchers queue and notifies the server dispatcher.
     */
    public void run()
    {
        try {
           while (!isInterrupted()) {
               String warriorMessage = mIn.readLine();
               if (warriorMessage == null)
                   break;
               //thisServerModerator.warriorCommandsQueue(thisWarriorInfo, warriorCommand);
               thisServerModerator.warriorCommandsQueue(new MessageContainer(thisWarriorInfo, warriorMessage));
           }
        } catch (IOException ioex) {
           // Problem reading from socket (communication is broken)
        }
 
        // Communication is broken. Interrupt both listener and sender threads
        thisWarriorInfo.thisWarriorSender.interrupt();
        thisServerModerator.deleteWarrior(thisWarriorInfo);
    }
 
}
