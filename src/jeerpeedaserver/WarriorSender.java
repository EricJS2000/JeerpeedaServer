package jeerpeedaserver;

import java.io.*;
import java.net.*;
import java.util.*;
 
public class WarriorSender extends Thread
{
    private Vector mMessageQueue = new Vector();
 
    private ServerModerator mServerModerator;
    private WarriorObj mWarriorInfo;
    private PrintWriter mOut;
 
    public WarriorSender(WarriorObj aWarriorInfo, ServerModerator aServerModerator)
    throws IOException
    {
        mWarriorInfo = aWarriorInfo;
        mServerModerator = aServerModerator;
        Socket socket = aWarriorInfo.warriorSocket;
        mOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    }
 
    /**
     * Adds given message to the message queue and notifies this thread
     * (actually getNextMessageFromQueue method) that a message is arrived.
     * sendMessage is called by other threads (ServeDispatcher).
     */
    public synchronized void sendMessage(MessageContainer aMessage)
    {
        mMessageQueue.add(aMessage);
        notify();
    }
 
    /**
     * @return and deletes the next message from the message queue. If the queue
     * is empty, falls in sleep until notified for message arrival by sendMessage
     * method.
     */
    private synchronized MessageContainer getNextMessageFromQueue() throws InterruptedException
    {
        while (mMessageQueue.size()==0)
           wait();
        MessageContainer message = (MessageContainer) mMessageQueue.get(0);
        mMessageQueue.removeElementAt(0);
        return message;
    }
 
    /**
     * Sends given message to the client's socket.
     */
    private void sendMessageToClient(String aMessage)
    {
        mOut.println(aMessage);
        mOut.flush();
    }
 
    /**
     * Until interrupted, reads messages from the message queue
     * and sends them to the client's socket.
     */
    public void run()
    {
        try 
        {
           while (!isInterrupted()) 
           {
               MessageContainer messageContainer = getNextMessageFromQueue();
               sendMessageToClient(messageContainer.Message);
           }// end of while loop
        } catch (Exception e) {
           // Communication problem
        }
 
        // Communication is broken. Interrupt both listener and sender threads
        mWarriorInfo.thisWarriorListener.interrupt();
        mServerModerator.deleteWarrior(mWarriorInfo);
    } // end of public run
 
} // public class WarriorSender
