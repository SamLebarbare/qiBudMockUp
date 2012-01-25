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
public class Project extends Model {


    @OneToOne
    public Bud origin;
    @OneToMany(mappedBy="project")
    public List<Mission> missions;
    @OneToMany(mappedBy="project")
    public List<Idea> ideas;

    public String status;
    
    @ManyToOne
    public User actor;
    public boolean assigned;


    public Project(Bud bud)
    {
        this.origin = bud;
        this.missions = new ArrayList<Mission>();
        this.ideas = new ArrayList<Idea>();
        this.status = "En attente de missions";
    }


    public Mission addMission(Mission m)
    {
        this.missions.add(m);
        this.status = "Missions en cours";
        this.save();
        return m;
    }
    
    public void checkMissions()
    {
        if(this.missions.size()>0)
        {
            boolean ended = true;
            for(Mission m : this.missions)
            {
                if(!m.status.equals("Terminée"))
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
                this.status = "Missions en cours";
                this.save();
            }
        }
    }
    
    public Project ActOn(User user)
    {
        this.actor = user;
        this.origin.setMyActor(user);
        this.assigned = true;
        this.status = "Assigné";
        this.save();
        
        return this;
    }

    public Project reOpen()
    {
        this.assigned = false;
        this.status = "Libre";

        this.save();
        return this;
    }


}
