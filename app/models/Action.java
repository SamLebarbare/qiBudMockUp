/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package models;

/**
 *
 * @author sloup
 */
import play.db.jpa.*;

import javax.persistence.*;

@Entity
public class Action extends Model {


    @OneToOne
    public Bud origin;

    @ManyToOne
    public Mission mission;
    
    @ManyToOne
    public Bug bug;

    public String status;
   
    

    @ManyToOne
    public User actor;
    public boolean assigned;


    public Action(Bud bud)
    {
        this.origin = bud;
        this.assigned = false;
        this.status = "Libre";
        
    }

    public Action ActOn(User user)
    {
        this.actor = user;
        this.origin.setMyActor(user);
        this.assigned = true;
        this.status = "En attente d'un résultat";
        this.save();
        
        return this;
    }

    ///STATE MANAGEMENT
    public Action Close()
    {
        this.status = "Terminée";
        this.save();

        if(this.origin.father!=null)
        {
            if(this.origin.father.isMission)
            {
                this.origin.father.mission.checkActions();
            }
            if(this.origin.father.isBug)
            {
                this.origin.father.bug.checkActions();
            }
        }


        return this;
    }

    public Action reOpen()
    {
        this.assigned = false;
        this.status = "Libre";
        this.save();
        if(this.origin.father!=null)
        {
            if(this.origin.father.isMission)
            {
                this.origin.father.mission.checkActions();
            }
            if(this.origin.father.isBug)
            {
                this.origin.father.bug.checkActions();
            }
        }
        return this;
    }

}
