package models;

import play.db.jpa.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
public class Team extends Model
{
    public String teamName;
    public String iconpath;
    
    
    @OneToOne
    public Bud origin;
    
    @OneToMany
    public List<Action> actions;
    @OneToMany
    public List<Idea> ideas;
    @OneToMany
    public List<Project> projects;
    @OneToMany
    public List<Mission> missions;
    @OneToMany
    public List<Bug> bugs;
    
    
    @ManyToMany
    public List<User> members;  

    public Team(Bud bud)
    {
        this.origin = bud;
        this.members = new ArrayList<User>();
        this.teamName = bud.budTitle;
        this.missions = new ArrayList<Mission>();
        this.projects = new ArrayList<Project>();
        this.ideas = new ArrayList<Idea>();
        this.bugs = new ArrayList<Bug>();
        this.iconpath = "it_icon.png";
    }

    public boolean addUser(User user)
    {
        if(!members.contains(user))
        {
            members.add(user);
            this.save();
            return true;
        }
        return false;
    }
    
    public Team addMission(Mission m)
    {
        this.missions.add(m);
        this.save();
        return this;
    }

    public String toString()
    {
        return teamName;
    }
}
