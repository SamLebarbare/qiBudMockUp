package models;

import notifiers.Mails;
import play.db.jpa.*;
import com.google.gson.*;
import javax.persistence.*;
import java.io.File;
import java.util.*;

@Entity
public class Bud extends Model implements java.lang.Comparable
{
    @Lob
    public String budTitle;
    
    public boolean isASimpleBud = true;
    @Lob
    public String content;

    public Date createdAt;

    public String filepath;
    
    public long qi;

    @ManyToOne
    public User creator;
    
    @ManyToOne
    public User actor;

    @OneToOne
    public Group group;
    
    @OneToOne
    public Sphere sphere;

    @OneToOne(mappedBy="origin")
    public Action action;
    public boolean isAction = false;

    @OneToOne(mappedBy="origin")
    public Idea idea;
    public boolean isIdea = false;

    @OneToOne(mappedBy="origin")
    public Bug bug;
    public boolean isBug = false;

    @OneToOne(mappedBy="origin")
    public Choice choice;
    public boolean isChoice = false;

    @OneToOne(mappedBy="origin")
    public Mission mission;
    public boolean isMission = false;

    @OneToOne(mappedBy="origin")
    public Project project;
    public boolean isProject = false;

    @OneToOne(mappedBy="origin")
    public Result result;
    public boolean isResult = false;
    
    @OneToOne(mappedBy="origin")
    public Team team;
    public boolean isATeam = false; 

    @OneToMany(mappedBy="viewableBuds")
    public List<User> viewers;
    
    
    @OneToMany(mappedBy="lookBud")
    public List<User> userLookingMe;
    
    @OneToOne
    public User inUserWallet;

    @OneToMany(mappedBy="bud", cascade=CascadeType.ALL)
    public List<Comment> comments;

    @ManyToMany(cascade=CascadeType.PERSIST)
    public Set<Tag> tags;


    //Subbud
    @ManyToMany
    @JoinTable(name="bud_subbuds")
    public List<Bud> subBuds;

    @ManyToOne
    public Bud father;


    public Bud(Group group,User creator, String title,String content,Date date)
    {
        this.comments = new ArrayList<Comment>();
        this.tags = new TreeSet<Tag>();
        this.creator = creator;
        this.budTitle = title;
        this.content = content;
        this.createdAt = date;
        this.group = group;
        this.sphere = group.sphere;
        this.qi = 0;
    }

    public Bud setMyActor(User u)
    {
        this.actor = u;
        this.save();
        return this;
    }
    public Bud tagItWith(String name)
    {
        tags.add(models.Tag.findOrCreateByName(name));
        return this;
    }


    public static List<Bud> findTaggedWith(String tag)
    {
        return Bud.find("select distinct p from Bud p join p.tags as t where t.name = ?", tag).fetch();
    }
    //Subbud
    public Bud addSubBud(Bud r)
    {
        r.initFather(this);
        r.save();
        this.subBuds.add(r);
        this.save();
        return this;
    }

    public Bud initFather(Bud r)
    {
        this.father = r;
        this.save();
        return this;
    }

    public List<User> getViewers()
    {
        return User.find("select u from User u join u.viewableBuds as r where r.id = ? ",this.id).fetch();

    }
    //CONTENT
    
    public Bud addContent(String c)
    {
        this.content = this.content + c;
        this.save();
        return this;
    }
    //COMMENTS
    public Bud addCommentAndFile(User author, String content,File file)
    {
        Comment newCom = new Comment(author,content,this);
        newCom.filepath = file.getName();
        newCom.save();
        this.comments.add(newCom);
        this.save();

        Mails.commentaire(author,content,this);
        return this;
    }
    public Bud addComment(User author, String content)
    {
        Comment newCom = new Comment(author,content,this).save();
        this.comments.add(newCom);
        this.save();

        Mails.commentaire(author,content,this);

        return this;
    }


    public void reset4morph()
    {
        this.isAction = false;
        this.isIdea = false;
        this.isMission = false;
        this.isProject = false;
        this.isResult = false;
        this.isChoice = false;
        this.isBug = false;
        this.isATeam = false;
        this.isASimpleBud = false;

        this.save();
    }


    public Bud detach(Bud sub)
    {
        this.subBuds.remove(sub);
        this.save();
        return this;
    }
    public Bud attach(Bud sub)
    {
        this.subBuds.add(sub);
        this.save();
        return this;
    }
    public Bud changeMyFather(Bud father)
    {
        this.father = father;
        this.save();
        return this;
    }
    
    
    public void LoadBaseQi()
    {
        this.qi = this.subBuds.size() + this.viewers.size();
        this.save();
    }
    public String toString()
    {
        return budTitle;
    }
    
    public String toJsonString()
    {
        Gson gson = new Gson();
        return gson.toJson(budTitle); 
    }
    
    public String budDetailsToJsonString()
    {
        Gson gson = new Gson();
        if(this.isAction)
        {
            if(this.action.assigned)
            {
                return gson.toJson("<span class='label success'>action: "+ this.action.status + "</span><br>Createur: " + this.creator.fullname + "<br>" + "Qi:" + this.qi + "<br>Acteur: " + this.action.actor.fullname);
            }
            else
            {
                return gson.toJson("<span class='label success'>action: "+ this.action.status + "</span><br>Createur: " + this.creator.fullname + "<br>" + "Qi:" + this.qi);
            }
            
        }
        if(this.isResult)
        {
            if(this.result.define)
            {

                return gson.toJson("<span class='label success'>résultat: "+ this.result.status + "</span><br>Createur: " + this.creator.fullname + "<br>" + "Qi:" + this.qi + "<br>Acteur: " + this.result.actor.fullname);
                
            }
            else
            {
                if(this.result.assigned)
                {
                    return gson.toJson("<span class='label success'>résultat: "+ this.result.status + "</span><br>Createur: " + this.creator.fullname + "<br>" + "Qi:" + this.qi + "<br>Acteur: " + this.result.actor.fullname);
                }
                else
                {
                    return gson.toJson("<span class='label success'>résultat: "+ this.result.status + "</span><br>Createur: " + this.creator.fullname + "<br>" + "Qi:" + this.qi);
                }
                
            }
            
        }
        if(this.isMission)
        {
            if(this.mission.assigned)
            {
                return gson.toJson("<span class='label success'>mission: "+ this.mission.status + "</span><br>Createur: " + this.creator.fullname + "<br>" + "Qi:" + this.qi + "<br>Acteur: " + this.mission.actor.fullname);
                
            }
            else
            {
                return gson.toJson("<span class='label success'>mission: "+ this.mission.status + "</span><br>Createur: " + this.creator.fullname + "<br>" + "Qi:" + this.qi);
            }
            
            
            
        }
        if(this.isATeam)
        {
            String members="";
            
            for(User u :this.team.members)
            {
                members += members + "<br>" + u.fullname;
                
            }
            return gson.toJson("<span class='label success'>team</span><br>Createur: " + this.creator.fullname + "<br>" + "Qi:" + this.qi + "<br>Membres:" + members);
            
            
        }
        if(this.isBug)
        {
            if(this.bug.assigned)
            {
                return gson.toJson("<span class='label success'>bug: "+ this.bug.status + "</span><br>Createur: " + this.creator.fullname + "<br>" + "Qi:" + this.qi + "<br>Acteur: " + this.bug.actor.fullname);
            }
            else
            {
                 return gson.toJson("<span class='label success'>bug: "+ this.bug.status + "</span><br>Createur: " + this.creator.fullname + "<br>" + "Qi:" + this.qi);
            }
  
        }
        if(this.isChoice)
        {

            return gson.toJson("<span class='label success'>choix</span><br>Createur: " + this.creator.fullname + "<br>" + "Qi:" + this.qi);
    
        }
        if(this.isIdea)
        {

            return gson.toJson("<span class='label success'>idée</span><br>Createur: " + this.creator.fullname + "<br>" + "Qi:" + this.qi);
    
        }
        if(this.isProject)
        {
           if(this.project.assigned)
           {
               return gson.toJson("<span class='label success'>projet: "+ this.project.status + "</span><br>Createur: " + this.creator.fullname + "<br>" + "Qi:" + this.qi + "<br>Acteur: " + this.project.actor.fullname);
           }
           else
           {
                return gson.toJson("<span class='label success'>projet: "+ this.project.status + "</span><br>Createur: " + this.creator.fullname + "<br>" + "Qi:" + this.qi);
           }
        }
        if(this.isASimpleBud)
        {
            return gson.toJson("<span class='label success'>bud</span><br>Createur: " + this.creator.fullname + "<br>" + "Qi:" + this.qi);
        }
        else
        {
            return gson.toJson("Createur: " + this.creator.fullname + "<br>" + "Qi:" + this.qi); 
        }      
    }
    

    public int compareTo(Object other) {
      Date date1 = ((Bud) other).createdAt;
      Date date2 = this.createdAt;
      if (date1.after(date2))  return -1;
      else if(date1.equals(date2)) return 0;
      else return 1;
   }



}
