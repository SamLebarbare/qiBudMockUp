package controllers;

import notifiers.Mails;
import play.*;
import play.mvc.*;
import play.data.validation.*;
import java.io.File;
import java.lang.reflect.Array;
import java.util.*;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import models.*;
import play.mvc.Scope.Session;




@With(Secure.class)
public class Application extends Controller {


    @Before
    static void setConnectedUser()
    {
        Security.authenticate(Session.current().get("username"), Session.current().get("token"));
        if(Security.isConnected())
        {                   
           User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
           if(user==null)
           {
               Site.index();
           }
           else
           {
                renderArgs.put("user", user.fullname);
                renderArgs.put("uid", user.id);
                
                //XP&LVL
                if(user.level==0)
                {
                    user.addXP(1);
                }
                renderArgs.put("uxp", user.xp);
                renderArgs.put("uxp2r", user.xpToReach);
                renderArgs.put("ulvl", user.level);
                renderArgs.put("ulvlUp", user.levelUp);    
                //Wallet&View
                if(user.walletBud!=null)
                {
                    
                    renderArgs.put("isWalletEmpty", false);
                    renderArgs.put("walletBudId", user.walletBud.id);
                    renderArgs.put("walletBudTitle", user.walletBud.budTitle);
                    renderArgs.put("lookingBudId", params.get("id"));
                    
                }
                else
                {
                    renderArgs.put("isWalletEmpty", true);
                }
                //Choices
                if(user.choicesToDo.size()>0)
                {
                    renderArgs.put("mustTakeChoices", "yes");
                    renderArgs.put("choices", user.choicesToDo);
                }               
                else
                {
                    renderArgs.put("mustTakeChoices", "no");
                }
                
                
            }
        }
        
       
    }
    
    public static void profil(Long uid)
    {
        User userProfil = User.findById(uid);
        
        
        render(userProfil);
    }
    
    
    
    public static void profilJSON(Long uid)
    {
        User userProfil = User.findById(uid);
        List<Bud> creator = Bud.find("creator", userProfil).fetch();
        List<Bud> actor = Bud.find("actor", userProfil).fetch();
        
        
        int creatorCount = creator.size();
        int actorCount = actor.size();
        int creaScore=1;
        int curiusScore=1;
        int orgaScore=1;
        int reaScore=1;
        int psolvScore=1;
        int pmScore=1;
        int leadScore=1;
        
        for(Bud b : creator)
        {
            if(b.isIdea)
            {
                creaScore += 1;
            }
            if(b.isASimpleBud)
            {
                curiusScore +=1;
            }
            if(b.isProject || b.isMission || b.isAction)
            {
                orgaScore+=1;
            }
        }
        
        for(Bud b : actor)
        {
            if(b.isIdea)
            {
                reaScore += 1;
            }
            if(b.isBug)
            {
                psolvScore +=1;
            }
            if(b.isProject)
            {
                pmScore+=1;
            }
            if(b.isMission)
            {
                leadScore+=1;
            }
            if(b.isAction)
            {
                reaScore+=1;
            }
        }
       
               
        
        request.format = "json";
        render(userProfil,creatorCount,actorCount,creaScore,curiusScore,orgaScore,reaScore,psolvScore,pmScore,leadScore);
    }
    
    public static void index(int nbresult)
    {
        User currentuser = User.findByLinkedInId(Session.current().get("LinkedInId"));

        if(currentuser.mustRegister)
        {
            render("@register",currentuser);
        }
        else
        {
            if(nbresult==0)
                nbresult=5;
            
            
            
            
            List<Group> groups = Group.findAll();
            List<Team> teams = Team.find("origin.isATeam = true").fetch();
            List<Event> allEvents = Event.findAll();
            
        
            List<Bud> myBuds = Bud.find("creator", currentuser).fetch();
            int budCount = myBuds.size();
            
            List<Action> freeActions = Action.find("assigned", false).fetch();
            List<Action> myActions = Action.find("assigned = ? and actor = ? and status <> 'Terminée'", true,currentuser).fetch();
            
            List<Project> myProjects = Project.find("assigned = ? and actor = ? and status <> 'Terminé'", true,currentuser).fetch();
            List<Mission> myMissions = Mission.find("assigned = ? and actor = ? and status <> 'Terminée'", true,currentuser).fetch();
            
            List<Event> evBuds = new ArrayList<Event>();
            List<Event> evTeams = new ArrayList<Event>();
            List<Event> evActions = new ArrayList<Event>();
            List<Event> evIdeas = new ArrayList<Event>();
            List<Event> evBugs = new ArrayList<Event>();
            List<Event> evProjects = new ArrayList<Event>();
            List<Event> evMissions = new ArrayList<Event>();
            List<Event> evResults= new ArrayList<Event>();
            
            Collections.sort(allEvents, Collections.reverseOrder());
            List<Event> events = new ArrayList<Event>();
            if(allEvents.size()>5)
            {
                events = allEvents.subList(0, nbresult);
            }
            else
            {
                events = allEvents;
            }
            
            
            for(Event e : events)
            {
                if(e.on.isASimpleBud)
                {
                    evBuds.add(e);
                }
                if(e.on.isATeam)
                {
                    evTeams.add(e);
                }
                if(e.on.isAction)
                {
                    evActions.add(e);
                }
                if(e.on.isIdea)
                {
                    evIdeas.add(e);
                }
                if(e.on.isBug)
                {
                    evBugs.add(e);
                }
                if(e.on.isProject)
                {
                    evProjects.add(e);
                }
                if(e.on.isResult)
                {
                    evResults.add(e);
                }
                if(e.on.isMission)
                {
                    evMissions.add(e);
                }
                
            }
            
            Collections.sort(evBuds, Collections.reverseOrder());
            Collections.sort(evTeams, Collections.reverseOrder());
            Collections.sort(evActions, Collections.reverseOrder());
            Collections.sort(evIdeas, Collections.reverseOrder());
            Collections.sort(evBugs, Collections.reverseOrder());
            Collections.sort(evProjects, Collections.reverseOrder());
            Collections.sort(evResults, Collections.reverseOrder());
            Collections.sort(evMissions, Collections.reverseOrder());
            

            render(nbresult,currentuser,budCount,groups,teams,events,evBuds,evActions,evIdeas,evBugs,evTeams,evResults,evMissions,freeActions,myBuds,myActions,myProjects,myMissions);
        }
    }
    
    public static void mail2bud()
    {
        User cuser = User.findByLinkedInId(Session.current().get("LinkedInId"));
        boolean succes=false;
        String error = "";
        int newMsg=0;
        List<EmailMetaData> emails = new ArrayList<EmailMetaData>();
        
        if(cuser.emailServer!=null&&cuser.emailUserName!=null&&cuser.emailPassword!=null)
        {
            EMailDAO emailDAO = new EMailDAO("imap",cuser.emailServer,143,cuser.emailUserName,cuser.emailPassword);
            
            try
            {
                emailDAO.connect();


                Folder folder = emailDAO.openFolder("INBOX", Folder.READ_ONLY);

                int page = 1;
                int rows = 10;
                final int messageCount = folder.getUnreadMessageCount();
                final int records = ((page * rows) > messageCount) ? messageCount : page * rows;
                final int start = (page - 1) * rows > 0 ? (page - 1) * rows : 1;
                newMsg = messageCount;
                System.out.println("start [" + start + "] records [" + records + "]");
                emails = emailDAO.getMessages(folder, start, records);

                succes=true;
                emailDAO.close();
                

            }
            catch(Exception e)
            {
                error = "Erreur: " + e.getMessage();
                succes=false;
            }
            render(cuser,succes,error,newMsg,emails);
        }
        else
        {
            render(cuser,error);
        }
        
    }
    public static void mail2BudConfig(String server,String userName,String password)
    {
        User cuser = User.findByLinkedInId(Session.current().get("LinkedInId"));
        
        cuser.emailUserName = userName;
        cuser.emailServer = server;
        cuser.emailPassword = password;
        cuser.save();
        mail2bud();
    }
    public static void mail2budNew(String title,int messageId)
    {
        User cuser = User.findByLinkedInId(Session.current().get("LinkedInId"));
        List<Group> groups = Group.findAll();
        String content = "";
        EMailDAO emailDAO = new EMailDAO("imap",cuser.emailServer,143,cuser.emailUserName,cuser.emailPassword);
        
        try
        {
            emailDAO.connect();
            String [] folders = {"INBOX"};
            Message message = emailDAO.getMessage(folders, Folder.READ_ONLY, messageId);
            Part p = message;
            
            
            if(p.isMimeType("text/plain"))
            {
                content += emailDAO.getContent(message,message.getContentType());
            }
            else
            {
                Multipart mpart = (Multipart) p.getContent();           

                for (int i = 0; i < mpart.getCount(); i++) 
                {
                    System.out.println(i);
                    BodyPart bodyPart = mpart.getBodyPart(i);
                    if (!Message.ATTACHMENT.equals(bodyPart.getDisposition())) 
                    {
                        System.out.println(bodyPart.getDisposition());
                        content += emailDAO.getContent("INBOX", messageId, bodyPart, bodyPart.getContentType());

                    } 

                }
            }
            
            
           
            
           
               
            
            emailDAO.close();
            
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        render(cuser,groups,title,content);
    }
    public static void mail2budTest()
    {
        User cuser = User.findByLinkedInId(Session.current().get("LinkedInId"));
        EMailDAO emailDAO = new EMailDAO("imap",cuser.emailServer,143,cuser.emailUserName,cuser.emailPassword);
        boolean succes=false;
        String error = "";
        int newMsg=0;
        List<EmailMetaData> emails = new ArrayList<EmailMetaData>();
        try
        {
            emailDAO.connect();
            
            
            Folder folder = emailDAO.openFolder("INBOX", Folder.READ_ONLY);
            
            int page = 1;
            int rows = 10;
            final int messageCount = folder.getMessageCount();
            final int records = ((page * rows) > messageCount) ? messageCount : page * rows;
            final int start = (page - 1) * rows > 0 ? (page - 1) * rows : 1;
            newMsg = messageCount / 20 + 1;
            System.out.println("start [" + start + "] records [" + records + "]");
            emails = emailDAO.getMessages(folder, start, records);
            
            succes=true;
            emailDAO.close();
            
        }
        catch(Exception e)
        {
            error = "Erreur: " + e.getMessage();
            succes=false;
        }
        
           
        render(cuser,succes,error,newMsg,emails);
    }
    
    public static void graphs()
    {
     

        List<Bud>  allBuds = Bud.findAll();


        render(allBuds);
    }
    
    public static void mybuds()
    {
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));

        if(user.mustRegister)
        {
            render("@register",user);
        }
        else
        {
            List<Group> groups = Group.findAll();



            List<Bud> myBuds = new ArrayList<Bud>();

            List<Bud> myPastBuds = new ArrayList<Bud>();

            List<Bud> myViewableBuds = user.viewableBuds;

            List<Bud>  allBuds = Bud.findAll();

            Iterator<Bud> budToRemove = allBuds.iterator();

            while(budToRemove.hasNext())
            {
                Bud r = budToRemove.next();
                if(r.creator == user)
                {
                  myBuds.add(r);
                }
                if(r.creator == user)
                {
                  myPastBuds.add(r);
                }
            }


            //Tri inverse
            Collections.sort(myPastBuds, Collections.reverseOrder());
            Collections.sort(myBuds, Collections.reverseOrder());

            render(groups,myBuds,myPastBuds,myViewableBuds);
        }
    }

    public static void search(String title)
    {
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));

        List<Group> groups = Group.findAll();
        if(title==null)
        {
                title="%";
        }
        else
        {
            String temp = title;
            title= "%" + temp + "%";
            title = title.toUpperCase();

        }

        List<Bud> allBuds = Bud.find("UPPER(budTitle) like ? or UPPER(content) like ?",title,title).fetch();

        List<Bud> resultats = new ArrayList<Bud>();

        Iterator<Bud> budToRemove = allBuds.iterator();

        while(budToRemove.hasNext())
        {
            Bud r = budToRemove.next();
            if(r.creator == user || r.viewers.contains(user))
            {
              resultats.add(r);
            }

        }


        render(resultats,groups);
    }

    public static void register()
    {
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        render(user);
    }
    public static void updateUser(Long userid,@Required String fn,@Required String em)
    {

        if (validation.hasErrors()) {
            flash.error("Merci de remplire toutes les informations demandées!");
            params.flash();
            register();
        }
        User user = User.findById(userid);
        user.fullname = fn;
        user.email = em;
        user.mustRegister = false;
        Group base = Group.findById(1L);
        base.members.add(user);
        base.save();
        user.groups.add(base);
        user.save();

        flash.success("Bienvenue sur QiBud");
        params.flash();

        index(50);
    }

    ///////////////////BUD CONTROLLER///////////////////////
    public static void show(Long id)
    {
        Bud r = Bud.findById(id);
        r.LoadBaseQi();
        if(r.isMission)
        {
            r.mission.checkActions();
            Mission m = r.mission;
            m.save();
        }
        if(r.isProject)
        {
            r.project.checkMissions();
            Project p = r.project;
            p.save();
        }
        if(r.isBug)
        {
            r.bug.checkActions();
            Bug b = r.bug;
            b.save();
        }
        User currentuser = User.findByLinkedInId(Session.current().get("LinkedInId"));
        currentuser.isLookingTheBud(r);
        List<User> users = r.userLookingMe;
        render(r,currentuser,users);
    }
    public static void share(Long id)
    {
        Bud b = Bud.findById(id);
        List<Team> teams = Team.find("origin.isATeam = true order by origin.budTitle asc").fetch();
        List<User> users = User.find("order by fullName asc").fetch();

        render(b,teams,users);
    }
    public static void sendShare(Long rid,Long[] uids,Long[] tids)
    {
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        Bud b = Bud.findById(rid);
        if(uids!=null)
        {
            for(Long uid : uids)
            {
                User u = User.findById(uid);
                u.addViewableBud(b);
            }
        }
        if(tids!=null)
        {
            for(Long tid : tids)
            {
                Team t = Team.findById(tid);
                t.origin.addSubBud(b);          
            }
        }
        

        Event ev = new Event(user,"share",b);
        ev.save();
        show(b.id);
    }
    
    
    
    public static void assign(Long bid)
    {
        Bud b = Bud.findById(bid);
        if(b.isAction || b.isBug || b.isMission || b.isProject || b.isResult)
        {
            List<User> users = User.findAll();
            render(users,b);
        }
        else
        {
            flash.error("Je dois ne suis pas assignable");
            params.flash();
            show(b.id);
        }
  
    }
    public static void assignTo(Long bid,Long uid)
    {
        Bud b = Bud.findById(bid);
        if(b.isAction || b.isBug || b.isMission || b.isProject || b.isResult)
        {
            User user = User.findById(uid);
            if(b.isAction)
            {
                b.action.ActOn(user);
            }
            if(b.isBug)
            {
                b.bug.ActOn(user);
            }
            if(b.isMission)
            {
                b.mission.ActOn(user);
            }
            if(b.isProject)
            {
                b.project.ActOn(user);
            }
            if(b.isResult)
            {
                b.result.ActOn(user);
            }
            show(b.id);
        }
        else
        {
            flash.error("Je dois ne suis pas assignable");
            params.flash();
            show(b.id);
        }
  
    }
    public static void newShare(Long rid,Long[] uids)
    {
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        Bud r = Bud.findById(rid);
        for(Long uid : uids)
        {
            User u = User.findById(uid);
            u.addViewableBud(r);
            Mails.share(user, u, r);
        }

        Event ev = new Event(user,"share",r);
        ev.save();
        mybuds();
    }

    public static void unShare(Long rid)
    {
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        Bud r = Bud.findById(rid);
        user.removeViewableBud(r);
        Event ev = new Event(user,"unfollow",r);
        ev.save();
        index(50);

    }
    
    public static void transfert(Long subBudId,Long toBudId)
    {
        if(subBudId!=toBudId)
        {
            Bud sub = Bud.findById(subBudId);
            Bud from = sub.father;
            Bud to = Bud.findById(toBudId);
            //If the buf have a father
            if(sub.father!=null)
            {
                //We dettach 
                from.detach(sub);            
            }
            sub.changeMyFather(to);
            to.attach(sub);
                        
        }
        show(subBudId);
        
        
    }

    public static void edit(Long rid)
    {
        Bud b = Bud.findById(rid);
        render(b);
    }
    
    public static void editBud(@Required Long rid,@Required String budTitle,@Required String content)
    {
        Bud b = Bud.findById(rid);
        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            edit(rid);
        }
        b.content = content;
        b.budTitle = budTitle;
        b.save();
        show(rid);
    }
    
    public static void bud()
    {
        List<Group> groups = Group.findAll();
        render(groups);
    }

    public static void newBud(@Required Long gid,@Required String budTitle,@Required String content, File file)
    {
        Group group = Group.findById(gid);
        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            bud();
        }
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        user.addXP(5);
        
        if(file!=null)
        {
            File newFile=Play.getFile("/public/shared/" + file.getName());
            file.renameTo(newFile);
            file.delete();
            Bud newR = group.newBudAndFile(group,user,budTitle,content,newFile);
            Event ev = new Event(user,"new",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
        else
        {
            Bud newR = group.newBud(group,user,budTitle,content);
            Event ev = new Event(user,"new",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
        

        //Send Event
        
        StatefulModel.instance.event.publish("NewBud: " + budTitle);
        

    }


    public static void newComment(Long rid,@Required String content,File file,String endbud)
    {
        Bud r = Bud.findById(rid);

        if(endbud==null)
        {
            endbud="false";
        }

        if (validation.hasErrors()) {
            index(50);
        }

        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        user.addXP(2);
        if(file==null)
        {
            r.addComment(user,content);
            if(!endbud.equals("false"))
            {
                r.action.reOpen();
            }
        }
        else
        {
            File newFile=Play.getFile("/public/shared/" + file.getName());
            file.renameTo(newFile);
            file.delete();
            r.addCommentAndFile(user,content,newFile);
            if(!endbud.equals("false"))
            {
                r.action.reOpen();
            }
        }


        Event ev = new Event(user,"comment",r);
        ev.save();
        flash.success("Merci pour votre commentaire %s",user.fullname);

        show(r.id);
    }
    public static void subBudFromComment(Long rid,Long cid,String content)
    {
        Bud father = Bud.findById(rid);
        User commentator = User.findById(cid);
        render(father,content,commentator);
    }
    public static void newSubBudFromComment(Long rid,@Required String budTitle,@Required Long gid,@Required String content, File file,User commentator)
    {
        Bud father = Bud.findById(rid);
        Group group = Group.findById(gid);

        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            subbud(rid);
        }
        
        commentator.addXP(10);
        if(file!=null)
        {
            File newFile=Play.getFile("/public/shared/" + file.getName());
            file.renameTo(newFile);
            file.delete();
            Bud newR = group.newBudAndFile(group,commentator,budTitle,content,newFile);
            father.addSubBud(newR);
            Event ev = new Event(commentator,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
        else
        {
            Bud newR = group.newBud(group,commentator,budTitle,content);
            father.addSubBud(newR);
            Event ev = new Event(commentator,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
  
        StatefulModel.instance.event.publish("NewBud: " + budTitle);
        
    }
    
    ///////////////
    //Wallet MNAGEMNT
    public static void putInMyWallet(Long bid)
   {
       User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
       Bud b = Bud.findById(bid);
       user.addBudToTheWallet(b);
       show(b.id);
   }

    ///////////////////////////////////
    // Adding subBud and typed bud
    //SubBuds
    public static void subbud(Long rid)
    {
        Bud father = Bud.findById(rid);
        List<Group> groups = Group.findAll();
        render(groups,father);
    }

    public static void newSubBud(Long rid,@Required String budTitle,@Required Long gid,@Required String content, File file)
    {
        Bud father = Bud.findById(rid);
        Group group = Group.findById(gid);

        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            subbud(rid);
        }
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        user.addXP(10);
        if(file!=null)
        {
            File newFile=Play.getFile("/public/shared/" + file.getName());
            file.renameTo(newFile);
            file.delete();
            Bud newR = group.newBudAndFile(group,user,budTitle,content,newFile);
            father.addSubBud(newR);
            Event ev = new Event(user,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
        else
        {
            Bud newR = group.newBud(group,user,budTitle,content);
            father.addSubBud(newR);
            Event ev = new Event(user,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
  
        StatefulModel.instance.event.publish("NewBud: " + budTitle);
        
    }
    
    
    public static void addAction(Long rid)
    {
        Bud father = Bud.findById(rid);
        List<Group> groups = Group.findAll();
        render(groups,father);
    }

    public static void newSubAction(Long rid,@Required String budTitle,@Required Long gid,@Required String content, File file)
    {
        Bud father = Bud.findById(rid);
        Group group = Group.findById(gid);

        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            addAction(rid);
        }
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        user.addXP(10);
        if(file!=null)
        {
            File newFile=Play.getFile("/public/shared/" + file.getName());
            file.renameTo(newFile);
            file.delete();
            Bud newR = group.newBudAndFile(group,user,budTitle,content,newFile);
            father.addSubBud(newR);
            
            if(!newR.isAction)
            {
                newR.reset4morph();
                newR.isAction = true;
                newR.save();

                Action oldaction = Action.find("origin", newR).first();
                if(oldaction==null)
                {
                    Action na = new Action(newR);
                    na.actor = user;
                    na.save();
                    if(newR.father!=null)
                    {
                        if(newR.father.isMission)
                        {
                            Bud f = newR.father;
                            Mission m = f.mission;
                            na.mission = m;
                            na.save();
                            m.addAction(na);
                        }
                        if(newR.father.isBug)
                        {
                            System.out.println("Bug père");
                            Bud f = newR.father;
                            Bug b = f.bug;
                            na.bug = b;
                            na.save();
                            b.addAction(na);
                        }
                    }
                }
            }
            Event ev = new Event(user,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
        else
        {
            Bud newR = group.newBud(group,user,budTitle,content);
            father.addSubBud(newR);
            if(!newR .isAction)
            {
                newR.reset4morph();
                newR.isAction = true;
                newR.save();

                Action oldaction = Action.find("origin", newR).first();
                if(oldaction==null)
                {
                    Action na = new Action(newR);
                    na.actor = user;
                    na.save();
                    if(newR.father!=null)
                    {
                        if(newR.father.isMission)
                        {
                            Bud f = newR.father;
                            Mission m = f.mission;
                            na.mission = m;
                            na.save();
                            m.addAction(na);
                        }
                        if(newR.father.isBug)
                        {
                            System.out.println("Bug père");
                            Bud f = newR.father;
                            Bug b = f.bug;
                            na.bug = b;
                            na.save();
                            b.addAction(na);
                        }
                    }
                }
            }
            
            Event ev = new Event(user,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
  
        StatefulModel.instance.event.publish("NewActionBud: " + budTitle);
        
    }
    
    
    public static void addBug(Long rid)
    {
        Bud father = Bud.findById(rid);
        List<Group> groups = Group.findAll();
        render(groups,father);
    }

    public static void newSubBug(Long rid,@Required String budTitle,@Required Long gid,@Required String content, File file)
    {
        Bud father = Bud.findById(rid);
        Group group = Group.findById(gid);

        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            addBug(rid);
        }
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        user.addXP(10);
        if(file!=null)
        {
            File newFile=Play.getFile("/public/shared/" + file.getName());
            file.renameTo(newFile);
            file.delete();
            Bud newR = group.newBudAndFile(group,user,budTitle,content,newFile);
            father.addSubBud(newR);
            
            if(!newR.isBug)
            {
                newR.reset4morph();
                newR.isBug = true;
                newR.save();
                Bug oldbug = Bug.find("origin", newR).first();
                if(oldbug==null)
                {
                    Bug nb = new Bug(newR);
                    nb.save();
                }
            }
            Event ev = new Event(user,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
        else
        {
            Bud newR = group.newBud(group,user,budTitle,content);
            father.addSubBud(newR);
            if(!newR.isBug)
            {
                newR.reset4morph();
                newR.isBug = true;
                newR.save();
                Bug oldbug = Bug.find("origin", newR).first();
                if(oldbug==null)
                {
                    Bug nb = new Bug(newR);
                    nb.save();
                }
            }
            
            Event ev = new Event(user,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
  
        StatefulModel.instance.event.publish("NewBugBud: " + budTitle);
        
    }

   
   
   
    public static void addChoice(Long rid)
    {
        Bud father = Bud.findById(rid);
        List<Group> groups = Group.findAll();
        render(groups,father);
    }

    public static void newSubChoice(Long rid,@Required String budTitle,@Required Long gid,@Required String content, File file)
    {
        Bud father = Bud.findById(rid);
        Group group = Group.findById(gid);

        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            addChoice(rid);
        }
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        user.addXP(10);
        if(file!=null)
        {
            File newFile=Play.getFile("/public/shared/" + file.getName());
            file.renameTo(newFile);
            file.delete();
            Bud newR = group.newBudAndFile(group,user,budTitle,content,newFile);
            father.addSubBud(newR);
            
            if(!newR.isChoice)
            {
                newR.reset4morph();
                newR.isChoice = true;
                newR.save();
                Choice oldchoise = Choice.find("origin", newR).first();
                if(oldchoise==null)
                {
                    Choice nc = new Choice(newR);
                    nc.save();
                }
            }
            Event ev = new Event(user,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
        else
        {
            Bud newR = group.newBud(group,user,budTitle,content);
            father.addSubBud(newR);
            if(!newR.isChoice)
            {
                newR.reset4morph();
                newR.isChoice = true;
                newR.save();
                Choice oldchoise = Choice.find("origin", newR).first();
                if(oldchoise==null)
                {
                    Choice nc = new Choice(newR);
                    nc.save();
                }
            }
            
            Event ev = new Event(user,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
  
        StatefulModel.instance.event.publish("NewChoiceBud: " + budTitle);
        
    }
    
    
    public static void addIdea(Long rid)
    {
        Bud father = Bud.findById(rid);
        List<Group> groups = Group.findAll();
        render(groups,father);
    }

    public static void newSubIdea(Long rid,@Required String budTitle,@Required Long gid,@Required String content, File file)
    {
        Bud father = Bud.findById(rid);
        Group group = Group.findById(gid);

        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            addIdea(rid);
        }
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        user.addXP(10);
        if(file!=null)
        {
            File newFile=Play.getFile("/public/shared/" + file.getName());
            file.renameTo(newFile);
            file.delete();
            Bud newR = group.newBudAndFile(group,user,budTitle,content,newFile);
            father.addSubBud(newR);
            
            if(!newR.isIdea)
            {
                newR.reset4morph();
                newR.isIdea = true;
                newR.save();

                Idea oldidea = Idea.find("origin", newR).first();
                if(oldidea==null)
                {

                    Idea ni = new Idea(newR);
                    ni.save();
                    if(newR.father!=null)
                    {
                        if(newR.father.isMission)
                        {
                            Bud f = newR.father;
                            Mission m = f.mission;
                            ni.mission = m;
                            ni.save();
                            m.addIdea(ni);
                        }
                    }

                }
            }
            Event ev = new Event(user,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
        else
        {
            Bud newR = group.newBud(group,user,budTitle,content);
            father.addSubBud(newR);
            if(!newR.isIdea)
            {
                newR.reset4morph();
                newR.isIdea = true;
                newR.save();

                Idea oldidea = Idea.find("origin", newR).first();
                if(oldidea==null)
                {

                    Idea ni = new Idea(newR);
                    ni.save();
                    if(newR.father!=null)
                    {
                        if(newR.father.isMission)
                        {
                            Bud f = newR.father;
                            Mission m = f.mission;
                            ni.mission = m;
                            ni.save();
                            m.addIdea(ni);
                        }
                    }

                }
            }
            
            Event ev = new Event(user,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
  
        StatefulModel.instance.event.publish("NewChoiceBud: " + budTitle);
        
    }
    
    public static void addMission(Long rid)
    {
        Bud father = Bud.findById(rid);
        List<Group> groups = Group.findAll();
        render(groups,father);
    }

    public static void newSubMission(Long rid,@Required String budTitle,@Required Long gid,@Required String content, File file)
    {
        Bud father = Bud.findById(rid);
        Group group = Group.findById(gid);
        
        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            addMission(rid);
        }
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        user.addXP(10);
        if(file!=null)
        {
            File newFile=Play.getFile("/public/shared/" + file.getName());
            file.renameTo(newFile);
            file.delete();
            Bud newR = group.newBudAndFile(group,user,budTitle,content,newFile);
            father.addSubBud(newR);
            
            if(!newR.isMission)
            {
                newR.reset4morph();
                newR.isMission = true;
                newR.save();
                Mission oldmission = Mission.find("origin", newR).first();
                if(oldmission==null)
                {
                    Mission nm = new Mission(newR);
                    nm.save();
                    if(father.isProject)
                    {
                        father.project.addMission(nm);
                        father.save();
                    }
                }
            }
            Event ev = new Event(user,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
        else
        {
            Bud newR = group.newBud(group,user,budTitle,content);
            father.addSubBud(newR);
            if(!newR.isMission)
            {
                newR.reset4morph();
                newR.isMission = true;
                newR.save();
                Mission oldmission = Mission.find("origin", newR).first();
                if(oldmission==null)
                {
                    Mission nm = new Mission(newR);
                    nm.save();
                    if(father.isProject)
                    {
                        father.project.addMission(nm);
                        father.save();
                    }
                }
            }
            
            Event ev = new Event(user,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
  
        StatefulModel.instance.event.publish("NewMissionBud: " + budTitle);
        
    }
    
    public static void addProject(Long rid)
    {
        Bud father = Bud.findById(rid);
        List<Group> groups = Group.findAll();
        render(groups,father);
    }

    public static void newSubProject(Long rid,@Required String budTitle,@Required Long gid,@Required String content, File file)
    {
        Bud father = Bud.findById(rid);
        Group group = Group.findById(gid);

        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            addProject(rid);
        }
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        user.addXP(10);
        if(file!=null)
        {
            File newFile=Play.getFile("/public/shared/" + file.getName());
            file.renameTo(newFile);
            file.delete();
            Bud newR = group.newBudAndFile(group,user,budTitle,content,newFile);
            father.addSubBud(newR);
            
            if(!newR.isProject)
            {
                newR.reset4morph();
                newR.isProject = true;
                newR.save();
                Project oldproject = Project.find("origin", newR).first();
                if(oldproject==null)
                {
                    Project nm = new Project(newR);
                    nm.save();
                }
            }
            Event ev = new Event(user,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
        else
        {
            Bud newR = group.newBud(group,user,budTitle,content);
            father.addSubBud(newR);
            if(!newR.isProject)
            {
                newR.reset4morph();
                newR.isProject = true;
                newR.save();
                Project oldproject = Project.find("origin", newR).first();
                if(oldproject==null)
                {
                    Project nm = new Project(newR);
                    nm.save();
                }
            }
            
            Event ev = new Event(user,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
  
        StatefulModel.instance.event.publish("NewProjectBud: " + budTitle);
        
    }
    
    public static void addResult(Long rid)
    {
        Bud father = Bud.findById(rid);
        List<Group> groups = Group.findAll();
        render(groups,father);
    }

    public static void newSubResult(Long rid,@Required String budTitle,@Required Long gid,@Required String content, File file)
    {
        Bud father = Bud.findById(rid);
        Group group = Group.findById(gid);

        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            addResult(rid);
        }
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        user.addXP(10);
        if(file!=null)
        {
            File newFile=Play.getFile("/public/shared/" + file.getName());
            file.renameTo(newFile);
            file.delete();
            Bud newR = group.newBudAndFile(group,user,budTitle,content,newFile);
            father.addSubBud(newR);
            
            if(!newR.isResult)
            {
                newR.reset4morph();
                newR.isResult = true;
                newR.save();
                Result oldresult = Result.find("origin", newR).first();
                if(oldresult==null)
                {
                    Result nr = new Result(newR);
                    nr.save();
                }
            }
            Event ev = new Event(user,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
        else
        {
            Bud newR = group.newBud(group,user,budTitle,content);
            father.addSubBud(newR);
            if(!newR.isResult)
            {
                newR.reset4morph();
                newR.isResult = true;
                newR.save();
                Result oldresult = Result.find("origin", newR).first();
                if(oldresult==null)
                {
                    Result nr = new Result(newR);
                    nr.save();
                }
            }
            
            Event ev = new Event(user,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
  
        StatefulModel.instance.event.publish("NewProjectBud: " + budTitle);
        
    }
    
    public static void addTeam(Long rid)
    {
        Bud father = Bud.findById(rid);
        List<Group> groups = Group.findAll();
        render(groups,father);
    }

    public static void newSubTeam(Long rid,@Required String budTitle,@Required Long gid,@Required String content, File file)
    {
        Bud father = Bud.findById(rid);
        Group group = Group.findById(gid);

        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            addTeam(rid);
        }
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        user.addXP(10);
        if(file!=null)
        {
            File newFile=Play.getFile("/public/shared/" + file.getName());
            file.renameTo(newFile);
            file.delete();
            Bud newR = group.newBudAndFile(group,user,budTitle,content,newFile);
            father.addSubBud(newR);
            
            if(!newR.isATeam)
            {
                newR.reset4morph();
                newR.isATeam = true;
                newR.save();
                Team oldteam = Team.find("origin", newR).first();
                if(oldteam==null)
                {
                    Team nt = new Team(newR);
                    nt.save();
                }
                
                flash.put("budtype", "team");
            }
            Event ev = new Event(user,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
        else
        {
            Bud newR = group.newBud(group,user,budTitle,content);
            father.addSubBud(newR);
            if(!newR.isATeam)
            {
                newR.reset4morph();
                newR.isATeam = true;
                newR.save();
                Team oldteam = Team.find("origin", newR).first();
                if(oldteam==null)
                {
                    Team nt = new Team(newR);
                    nt.save();
                }
                
                flash.put("budtype", "team");
            }
            
            Event ev = new Event(user,"newsub",newR);
            ev.save();
            flash.success("Merci!");
            show(newR.id);
        }
  
        StatefulModel.instance.event.publish("NewTeamBud: " + budTitle);
        
    }
    
    ///////////////////////////////////////////////////////////
    //////Morphing
    ///////////////////////////////////////////////////////////
   
    //Action related
    public static void morphInAction(Long bid)
    {
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        Bud b = Bud.findById(bid);
        if(!b.isAction)
        {
            b.reset4morph();
            b.isAction = true;
            b.save();

            Action oldaction = Action.find("origin", b).first();
            if(oldaction==null)
            {
                Action na = new Action(b);
                na.actor = user;
                na.save();
                if(b.father!=null)
                {
                    if(b.father.isMission)
                    {
                        Bud f = b.father;
                        Mission m = f.mission;
                        na.mission = m;
                        na.save();
                        m.addAction(na);
                    }
                    if(b.father.isBug)
                    {
                        Bud f = b.father;
                        Bug bu = f.bug;
                        na.bug = bu;
                        na.save();
                        bu.addAction(na);
                    }
                }
            }
            
            
            
            flash.success("Vous avez transformé ce bud en action!");
            params.flash();
            show(b.id);
        }
        else
        {
            flash.error("Je suis déjà une action!");
            params.flash();
            show(b.id);
        }

    }
    
    public static void takeAction(Long rid)
    {
        Bud b = Bud.findById(rid);
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        b.action.ActOn(user);
        user.addXP(5);
        Event ev = new Event(user,"assign",b);
        ev.save();
        
        flash.success("Merci!");
        show(b.id);
    }
    

    ////Idea related
    public static void morphInIdea(Long bid)
    {
        Bud b = Bud.findById(bid);


        if(!b.isIdea)
        {
            b.reset4morph();
            b.isIdea = true;
            b.save();
            
            Idea oldidea = Idea.find("origin", b).first();
            if(oldidea==null)
            {
                
                Idea ni = new Idea(b);
                ni.save();
                if(b.father!=null)
                {
                    if(b.father.isMission)
                    {
                        Bud f = b.father;
                        Mission m = f.mission;
                        ni.mission = m;
                        ni.save();
                        m.addIdea(ni);
                    }
                }
                
            }

            flash.success("Vous avez transformé ce bud en idée!");
            params.flash();
            show(b.id);
        }
        else
        {
            flash.error("Je suis déjà une idée!");
            params.flash();
            show(b.id);
        }

    }

    public static void morphInMission(Long bid)
    {
        Bud b = Bud.findById(bid);
        if(!b.isMission)
        {
            b.reset4morph();
            b.isMission = true;
            b.save();
            Mission oldmission = Mission.find("origin", b).first();
            if(oldmission==null)
            {
                Mission nm = new Mission(b);
                nm.save();
                for(Bud sb : b.subBuds)
                {
                    if(sb.isAction)
                    {
                        System.out.println("Action ajoutée");
                        nm.addAction(sb.action);
                    }
                }
                
                
            }
            
            flash.success("Vous avez transformé ce bud en mission!");
            params.flash();
            show(b.id);
        }
        else
        {
            flash.error("Je suis déjà une mission!");
            params.flash();
            show(b.id);
        }

    }
    public static void takeMission(Long rid)
    {
        Bud b = Bud.findById(rid);
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        b.mission.ActOn(user);
        user.addXP(5);
        Event ev = new Event(user,"assign",b);
        ev.save();
        
        flash.success("Merci!");
        show(b.id);
    }
    
    //Bug related
    public static void morphInBug(Long bid)
    {
        Bud b = Bud.findById(bid);
        if(!b.isBug)
        {
            b.reset4morph();
            b.isBug = true;
            b.save();
            Bug oldbug = Bug.find("origin", b).first();
            if(oldbug==null)
            {
                Bug nb = new Bug(b);
                nb.save();
            }
            flash.success("Vous avez transformé ce bud en bug!");
            params.flash();
            show(b.id);
        }
        else
        {
            flash.error("Je suis déjà un bug!");
            params.flash();
            show(b.id);
        }

    }
    public static void takeBug(Long rid)
    {
        Bud b = Bud.findById(rid);
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        b.bug.ActOn(user);
        user.addXP(5);
        Event ev = new Event(user,"assign",b);
        ev.save();
        
        flash.success("Merci!");
        show(b.id);
    }
    //Choice related
    public static void morphInChoice(Long bid)
    {
        Bud b = Bud.findById(bid);
        if(!b.isChoice)
        {
            b.reset4morph();
            b.isChoice = true;
            b.save();
            Choice oldchoise = Choice.find("origin", b).first();
            if(oldchoise==null)
            {
                Choice nc = new Choice(b);
                nc.save();
            }
            flash.success("Vous avez transformé ce bud en choix!");
            params.flash();
            show(b.id);
        }
        else
        {
            flash.error("Je suis déjà un choix!");
            params.flash();
            show(b.id);
        }

    }
    public static void acceptChoice(Long cid)
    {
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        user.addXP(2);
        Choice c = Choice.findById(cid);
        c.Accept(user);
        c.save();
        user.choicesToDo.remove(c);
        user.save();
        show(c.origin.id);
        
    }
    public static void declineChoice(Long cid)
    {
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        user.addXP(2);
        Choice c = Choice.findById(cid);
        c.Decline(user);
        c.save();
        user.choicesToDo.remove(c);
        user.save();
        show(c.origin.id);
    }

    public static void morphInProject(Long bid)
    {
        Bud b = Bud.findById(bid);
        if(!b.isProject)
        {
            b.reset4morph();
            b.isProject = true;
            b.save();
            Project oldproject = Project.find("origin", b).first();
            if(oldproject==null)
            {
                Project nm = new Project(b);
                nm.save();
            }
            flash.success("Vous avez transformé ce bud en projet!");
            params.flash();
            show(b.id);
        }
        else
        {
            flash.error("Je suis déjà un projet!");
            params.flash();
            show(b.id);
        }

    }
    public static void takeProject(Long rid)
    {
        Bud b = Bud.findById(rid);
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        b.project.ActOn(user);
        user.addXP(5);
        Event ev = new Event(user,"assign",b);
        ev.save();
        
        flash.success("Merci!");
        show(b.id);
    }
    //Result related
    public static void morphInResult(Long bid)
    {
        Bud b = Bud.findById(bid);
        if(!b.isResult)
        {
            b.reset4morph();
            b.isResult = true;
            b.save();
            Result oldresult = Result.find("origin", b).first();
            if(oldresult==null)
            {
                Result nr = new Result(b);
                nr.save();
            }
            flash.success("Vous avez transformé ce bud en résultat!");
            params.flash();
            show(b.id);
        }
        else
        {
            flash.error("Je suis déjà un résultat!");
            params.flash();
            show(b.id);
        }

    }
    public static void successfullResult(Long bid)
    {
        Bud b = Bud.findById(bid);
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        user.addXP(2);
        b.result.isASuccess(user);
        b.save();
        if(b.father.isAction)
        {
            b.father.action.actor.addXP(b.father.action.origin.qi*10);
            b.father.action.Close();
        }
        if(b.father.isBug)
        {
            b.father.bug.actor.addXP(b.father.bug.origin.qi*10);
            b.father.bug.Close();
        }
        
        
        flash.success("Merci!");
        show(b.id);
        
    }
    public static void failedResult(Long bid)
    {
        Bud b = Bud.findById(bid);
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));       
        user.addXP(2);
        b.result.isAFail(user);
        b.save(); 
        if(b.father.isAction)
        {
            b.father.action.reOpen();
        }
        if(b.father.isBug)
        {
            b.father.bug.reOpen();
        }
        flash.success("Merci!");
        show(b.id);
    }
    
    ///TEAM RELATED
    public static void morphInTeam(Long bid)
    {
        Bud b = Bud.findById(bid);
        if(!b.isATeam)
        {
            b.reset4morph();
            b.isATeam = true;
            b.save();
            Team oldteam = Team.find("origin", b).first();
            if(oldteam==null)
            {
                Team nt = new Team(b);
                nt.save();
            }
            flash.success("Vous avez transformé ce bud en Team!");
            flash.put("budtype", "team");
            params.flash();
            show(b.id);
        }
        else
        {
            flash.error("Je suis déjà une Team!");
            params.flash();
            show(b.id);
        }

    }
    public static void invite(Long bid)
    {
        Bud b = Bud.findById(bid);
        if(b.isATeam)
        {
            List<User> users = User.findAll();
            render(users,b);
        }
        else
        {
            flash.error("Je dois être une Team!");
            params.flash();
            show(b.id);
        }
  
    }
    public static void sendInvite(Long bid,Long[] uids)
    {
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        Bud b = Bud.findById(bid);
        
        for(Long uid : uids)
        {            
            User u = User.findById(uid);
            Group g = Group.findById(1L);
            Bud nbc = new Bud(g,user,"Invitation " + u.fullname,"Voulez-vous rejoindre l'équipe " + b.budTitle + "?",new Date());
            nbc.reset4morph();
            nbc.isChoice = true;
            nbc.save();
            Choice nc = new Choice(nbc);
            nc.mustChoice.add(u);
            nc.save();
            u.choicesToDo.add(nc);
            u.save();  
            b.addSubBud(nbc);
            b.save();
        }

        show(bid);
    }
    
    
    
    
    public static void morphInBud(Long bid)
    {
        Bud b = Bud.findById(bid);

        b.reset4morph();
        b.isASimpleBud = true;
        b.save();

        flash.success("Voilà!");
        params.flash();
        show(b.id);

    }
    
    public static void miniGraphJSON(Long id)
    {
        Bud r = Bud.findById(id);
        request.format = "json";
        render(r);
    }
    public static void budosphereGraphJSON()
    {
        List<Bud> allBuds = Bud.findAll();
        request.format = "json";
        render(allBuds);
    }

}