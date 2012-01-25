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
public class Choice extends Model {

    @OneToOne
    public Bud origin;

    @ManyToOne
    public Mission mission;
    
    @ManyToMany
    public List<User> mustChoice = new ArrayList<User>();

    public boolean choice=false;
    public boolean isChoose=false;
    

    public Choice(Bud bud)
    {
        this.origin = bud;
    }
    
    public Choice Accept(User u)
    {
        this.choice = true;
        this.mustChoice.remove(u);
        this.isChoose = true;
        this.save();
        System.out.println("Accept");
        if(this.origin.father.isATeam)
        {
            Team t  = Team.find("origin",this.origin.father).first();
            if(t.addUser(u))
            {
                Bud tb  = Bud.findById(t.origin.id);
                tb.addContent("<br>" + u.fullname + " a rejoins l'équipe");
                tb.save();
                Bud b  = Bud.findById(this.origin.id);
                b.addContent("<br>" + u.fullname + " a accepté l'invitation");
                b.save();
                t.save();
            }                   
        }
        return this;
        
    }
    
    public Choice Decline(User u)
    {
        this.choice = false;
        this.mustChoice.remove(u);
        this.isChoose = true;
        this.save();
        if(this.origin.father.isATeam)
        {
            this.origin.addContent("<br>" + u.fullname + " a refusé l'invitation");
            this.origin.save();
        }
        return this;

    }
    
    

}
