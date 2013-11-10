package jeerpeedaserver;

import java.net.*;

/**
 *
 * @author dmcnmac
 */
public class WarriorObj {
    public Socket warriorSocket = null;
    public WarriorListener thisWarriorListener = null;
    public WarriorSender thisWarriorSender = null;
    public String warriorName = "Unnamed";
    public int warriorHealth = 100;
}
