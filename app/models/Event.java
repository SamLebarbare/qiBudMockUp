/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package models;
import play.db.jpa.*;

import javax.persistence.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 *
 * @author sloup
 */
@Entity
public class Event extends Model implements java.lang.Comparable
{

    public Date at;
    public String type;

    @ManyToOne
    public User who;

    @OneToOne
    public Group inGroup;

    public String what;

    @OneToOne
    public Bud on;

    public String how;


    public Event(User who,String what)
    {
        this.who = who;     
        this.what = what;
        this.type = "simple";
        this.at = new Date();
    }

    public Event(User who,String type, Bud on)
    {

        this.who = who;
        this.on = on;
        this.type = type;

        if(type.equals("new"))
        {
            this.what =  who.fullname + " a créé un bud « " + on.budTitle + " »";
        }
        if(type.equals("newsub"))
        {
            this.what =  who.fullname + " a créé un sous bud  « " + on.budTitle + " » sous « " + on.father.budTitle + " » ";
        }
        if(type.equals("assign"))
        {
            this.what =  who.fullname + " c'est assigné un bud action « " + on.budTitle + " »";
        }
        if(type.equals("close"))
        {
            this.what =  who.fullname + " a terminé un bud action « " + on.budTitle + " »";
        }
        if(type.equals("backlog"))
        {
            this.what = who.fullname + " a mis un bud action « " + on.budTitle + " » en backlog";
        }
        if(type.equals("share"))
        {
            this.what = who.fullname + " a partagé un bud « " + on.budTitle + " » avec";
            for(User u : on.viewers)
            {
                this.what = this.what + " " + u.fullname;
            }
        }
        if(type.equals("reopen"))
        {
            this.what = who.fullname +" a réouvert un bud action « " + on.budTitle + " »";
        }
        if(type.equals("unfollow"))
        {
            this.what = who.fullname + " ne suis plus « " + on.budTitle + " »";
        }

        if(type.equals("comment"))
        {
            Comment c = on.comments.get(on.comments.size()-1);
            String thumbText;
            if(c.title.length()> 30)
            {
                thumbText = c.title.substring(0, 29) + "...";
            }
            else
            {
                thumbText = c.title;
            }

            this.what = "« " + thumbText + " » à propos du bud « " + on.budTitle + " »";
        }

        this.inGroup = on.group;
        
        this.at = new Date();

    }

    @Override
    public String toString()
    {
        return this.who.fullname + this.what;
    }

    public int compareTo(Object other) {
      Date date1 = ((Event) other).at;
      Date date2 = this.at;
      if (date1.after(date2))  return -1;
      else if(date1.equals(date2)) return 0;
      else return 1;
   }


}
