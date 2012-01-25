/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package models;

import java.util.ArrayList;
import java.util.List;
import play.db.jpa.*;

import javax.persistence.*;

@Entity
public class Bug extends Model {

    @OneToOne
    public Bud origin;

    @ManyToOne
    public Project project;

    @ManyToOne
    public Mission mission;
    
    @OneToMany(mappedBy="bug")
    public List<Action> actions;
    
    public String status;
    @ManyToOne
    public User actor;
    public boolean assigned;

    public Bug(Bud bud)
    {
        this.origin = bud;
        this.status = "Ouvert";
        this.actions = new ArrayList<Action>();
    }
    
  
    
    public Bug ActOn(User user)
    {
        this.actor = user;
        this.origin.setMyActor(user);
        this.assigned = true;
        this.status = "Assigné";
        this.save();
        
        return this;
    }
    
    public Action addAction(Action a)
    {
        this.actions.add(a);
        this.status = "Actions en cours";
        this.save();
        return a;
    }

    ///STATE MANAGEMENT
    public Bug Close()
    {
        this.status = "Résolu";
        this.save();

        return this;
    }

    public Bug reOpen()
    {
        this.assigned = false;
        this.status = "Ouvert";

        this.save();
        return this;
    }
    
    public void checkActions()
    {
        if(this.actions.size()>0)
        {
            boolean ended = true;
            for(Action a : this.actions)
            {
               
                if(!a.status.equals("Terminée"))
                {
                    ended = false;
                }
            }
            if(ended==true)
            {
                this.status = "Résolu";
                this.save();
            }
            else
            {
                this.status = "Actions en cours";
            }
        }
      
    }

}
