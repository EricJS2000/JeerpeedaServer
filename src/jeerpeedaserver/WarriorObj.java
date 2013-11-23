package jeerpeedaserver;

import java.net.*;

/**
 *
 * @author dmcnmac
 */
public class WarriorObj {
    
    public int WarriorID = 0;
    public String WarriorName = "Unnamed";
    public Boolean IsRegistered = false;
    public String Status = "";
    
    public Socket warriorSocket = null;
    public WarriorListener thisWarriorListener = null;
    public WarriorSender thisWarriorSender = null;
    
    public int HealthLevel = 100;
    public String PlaceOfOrigin = "";
    public String Description  = "";
    public Conflict currentConfict  = null;
    
   /* 
    private int warriorID = 0;
    private String warriorName = "Unnamed";
    private Boolean isRegistered = false;
    
    public int getWarriorID  ( )
    {
        return warriorID;
    }
    public void setWarriorID (int warriorID)
    {
        this.warriorID = warriorID;
    } 
    
    public String getWarriorName  ( )
    {
        return warriorName;
    }
    public void setWarriorName (String warriorName)
    {
        this.warriorName = warriorName;
    } 
    
    public Boolean getIsRegistered  ( )
    {
        return isRegistered;
    }
    public void setIsRegistered (Boolean isRegistered)
    {
        this.isRegistered = isRegistered;
    } 
    */
} // public class WarriorObj
