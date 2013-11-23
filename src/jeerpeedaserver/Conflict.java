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
public class Conflict 
{
    // set up the Warrior objects, weapons, and defence
    public WarriorObj Attacker = null;
    public WarriorObj Target = null;
    public String Weapon;
    public String Defence;
    
      Conflict(WarriorObj Attacker, WarriorObj Target, String Weapon)
    {
        this.Attacker = Attacker;
        this.Target = Target;
        this.Weapon = Weapon;
    }
} // end of public class Conflict 
