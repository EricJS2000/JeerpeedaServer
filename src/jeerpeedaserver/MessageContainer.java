/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jeerpeedaserver;

/**
 *
 * @author Administrator
 */
public class MessageContainer {
    
    public WarriorObj Warrior = null;
    public String Message;
    
    MessageContainer(WarriorObj warrior, String message)
    {
        this.Warrior = warrior;
        this.Message = message;
    }
    
}
