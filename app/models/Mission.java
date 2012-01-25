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
public class Mission extends Model {

    @OneToOne
    public Bud origin;
    @OneToMany(mappedBy="mission")
    public List<Result> results;
    @OneToMany(mappedBy="mission")
    public List<Action> actions;
    @OneToMany(mappedBy="mission")
    public List<Idea> ideas;

    @ManyToOne
    public Project project;

    public String status;
    
    @ManyToOne
    public User actor;
    public boolean assigned;

    public Mission(Bud bud)
    {
        this.origin = bud;
        this.actions = new ArrayList<Action>();
        this.results = new ArrayList<Result>();
        this.status = "En attente d'actions";

    }

    public Action addAction(Action a)
    {
        if(!this.actions.contains(a))
        {
            this.actions.add(a);
            this.status = "Actions en cours";
            this.save();
        }
        return a;
    }

    public Result addResult(Result r)
    {
        this.results.add(r);
        this.save();
        return r;
    }

    public Idea addIdea(Idea i)
    {
        this.ideas.add(i);
        this.save();
        return i;
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
                this.status = "Terminée";
                this.save();
            }
            else
            {
                this.status = "Actions en cours";
            }
        }
      
    }
    
    public Mission ActOn(User user)
    {
        this.actor = user;
        this.assigned = true;
        this.origin.setMyActor(user);
        this.status = "Assignée";
        this.save();
        
        return this;
    }
    
    public Mission reOpen()
    {
        this.assigned = false;
        this.status = "Libre";

        this.save();
        return this;
    }

    

    
}
