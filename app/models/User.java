package models;

import controllers.Security;
import java.util.ArrayList;
import play.db.jpa.*;
import javax.persistence.*;
import java.util.List;
import play.mvc.Scope.Session;


@Entity
public class User extends Model 
{

    public String email;
    public String login;
    public String fullname;
    //LINKEDIN
    public String LinkedInId;
    public String linkedInToken;
    public String pictureUrl;
    public String headline;
    public String industry;
    //bud2mail
    public String emailServer;
    public String emailUserName;
    public String emailPassword;
    //Exp&Leveling
    public int xp;
    public int level;
    public double xpToReach;
    public boolean levelUp;
    //Flags
    public boolean isAdmin;
    public boolean isSuperUser;
    public boolean mustRegister=true;
    

    @ManyToMany(mappedBy="members", cascade=CascadeType.ALL)
    public List<Group> groups;

    @ManyToMany
    public List<Bud> viewableBuds;
    
    
    @ManyToOne
    public Bud walletBud;
    
    @ManyToOne
    public Bud lookBud;

    @OneToMany
    public List<Event> createdEvents;
    
    
    @OneToMany
    public List<Choice> choicesToDo = new ArrayList<Choice>();

    public User(String login,String email, String fullname,boolean isadmin)
    {
        this.login = login;
        this.email = email;
        this.fullname = fullname;
        this.isAdmin = isadmin;
        this.viewableBuds = new ArrayList<Bud>();

    }
    
    public User()
    {
        this.viewableBuds = new ArrayList<Bud>();
    }

    public static User connect(String linkedInId) 
    {
        return find("byLinkedInId", linkedInId).first();
    }

    public User addViewableBud(Bud r)
    {
        
        if(!this.viewableBuds.contains(r))
        {
            this.viewableBuds.add(r);
            this.save();

        }
        return this;
    }

    public User removeViewableBud(Bud r)
    {
        if(this.viewableBuds.contains(r))
        {
            this.viewableBuds.remove(r);
            this.save();
        }
        return this;
    }
    
    public User addBudToTheWallet(Bud r)
    {
        
        this.walletBud = r;
        this.save();
        return this;
    }

    public User removeBudFromTheWallet(Bud r)
    {
        this.walletBud = null;
        this.save();
        return this;
    }
    
    public User isLookingTheBud(Bud r)
    {
        this.lookBud = r;
        this.save();
        return this;
    }

    public static User findByLinkedInId(String linkedInId) 
    {
            return find("byLinkedInId", linkedInId).first();
    }
    
    public User addXP(long toAdd)
    {       
        //Init Case
        if(this.xp == 0 && this.level == 0)
        {
            this.level = 1;
        }
        
        long xpCheck = this.xp + toAdd;
        double points = 0;
        
        for(int i=1;i<=this.level;i++)
        {
            points += Math.floor(this.level + 300 * Math.pow(2, this.level / 7.));
        }
        this.xpToReach = Math.floor(points / 4);
        
        if(xpCheck>this.xpToReach)
        {
            this.level+=1; 
            this.xp = (int) (0 + (xpCheck-xpToReach));
            points = 0;
            for(int i=1;i<=this.level;i++)
            {
                points += Math.floor(this.level + 300 * Math.pow(2, this.level / 7.));
            }           
            this.xpToReach = Math.floor(points / 4);          
            this.levelUp = true;
        }
        else
        {
            this.xp += toAdd;
            this.levelUp = false;
        }
        
        this.save();
        
        return this;
    }
    
    
    public static void linkedinOAuthCallback(play.modules.linkedin.LinkedInProfile profile) 
    {
        
        System.out.println("Handle LinkedIn OAuth Callback: " + profile);
        User user = User.find("LinkedInId",profile.getId()).first();

        if(user == null || user.linkedInToken == null) 
        {
            user = new User();
            user.fullname = (new StringBuffer().append(profile.getFirstName()).append(" ").append(profile.getLastName())).toString();
            user.LinkedInId = profile.getId();
            user.industry = profile.getIndustry();
            user.headline = profile.getHeadline();
            user.pictureUrl = profile.getPictureUrl();
            user.linkedInToken = profile.getAccessToken();
            user.isAdmin = true;
            user.mustRegister = false;
            user = user.save();
            
        } 
        else 
        {
            System.out.println("Found User: " + user.fullname);
            user.linkedInToken = profile.getAccessToken();
            user.save();
        }
        if ( user == null ) 
        {
            throw new RuntimeException("Could not store or lookup user with LinkedIn Profile: " + profile);
        }
        Session.current().put("username", user.fullname);
        Session.current().put("token",user.linkedInToken);
        Session.current().put("LinkedInId",user.LinkedInId);
        
            
    }
    
    public String toString()
    {
        return email;
    }





}
