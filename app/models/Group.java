package models;

import play.db.jpa.*;

import javax.persistence.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import notifiers.Mails;

@Entity
@Table(name="lab_group")
public class Group extends Model
{
    public String groupName;


    public String iconpath;

    @ManyToOne
    public Sphere sphere;
    
    @ManyToMany
    public List<User> members;

    @OneToMany(mappedBy="group", cascade=CascadeType.ALL)
    public List<Bud> buds;

    public Group(String groupName)
    {
        this.members = new ArrayList<User>();
        this.groupName = groupName;
        this.iconpath = "it_icon.png";

    }

    public Group addUser(User user)
    {
        members.add(user);
        this.save();
        return this;
    }

    public Bud newBudAndFile(Group group,User creator,String title, String content, File file)
    {
        Bud newR = new Bud(group,creator,title,content,new Date());
        newR.filepath = file.getName();
        newR.save();
        this.buds.add(newR);
        this.save();
        //Notification au membres du group
        for(User m : this.members)
        {
            Mails.newbud(m,newR);
        }
        return newR;
    }

    public Bud newBud(Group group,User creator,String title, String content)
    {
        Bud newR = new Bud(group,creator,title,content,new Date()).save();
        this.buds.add(newR);
        this.save();
         //Notification au membres du group
        for(User m : this.members)
        {
            Mails.newbud(m,newR);
        }
        return newR;
    }

    public String toString()
    {
        return groupName;
    }


}
