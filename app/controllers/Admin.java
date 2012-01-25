package controllers;

import models.Group;
import models.Bud;
import models.User;
import models.Event;
import play.*;
import play.mvc.*;
import play.data.validation.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import models.StatefulModel;
import notifiers.Mails;
import play.mvc.Scope.Session;


@Check("admin")
@With(Secure.class)
public class Admin extends Controller
{
    @Before
    static void setConnectedUser()
    {
        Security.authenticate(Session.current().get("username"), Session.current().getAuthenticityToken());
        if(Security.isConnected())
        {
        
            
           User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
           if(user==null)
           {
               
           }
           else
           {
                renderArgs.put("user", user.fullname);
                renderArgs.put("uid", user.id);
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
                
                
            }
        }
    }


    //WEBSOCKET Listener
    /*public static class WebSocket extends WebSocketController
    {

      public static void listen() 
      {
         while(inbound.isOpen())
         {
            String event = (String) await(StatefulModel.instance.event.nextEvent());
            System.out.println(event);
            outbound.send(event);
         }
      }
   }*/

    public static void index()
    {
        //Charge la liste des groupes
        List<Group> groups = Group.findAll();
        //Retrouve l'utilisateur connecté
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        //Charge toutes les demandes avant classification
        List<Bud>  allBuds = Bud.find("select r from Bud r order by r.priority desc").fetch();

        //Collection qui contiendra les requetes assignée à l'utilisateur
        List<Bud> myBuds = new ArrayList<Bud>();
        //Collection qui contiendra les requetes non-assignée
        List<Bud> freeBuds = new ArrayList<Bud>();
        //Collection qui contiendra les requetes mise en backlog
        List<Bud> backlogBuds = new ArrayList<Bud>();


        //Suppresion des requêtes ne devant pas être affichée à l'utilisateur courant
        //Ajoute au liste correspondant
        Iterator<Bud> budToRemove = allBuds.iterator();

        while(budToRemove.hasNext())
        {
            Bud r = budToRemove.next();

            myBuds.add(r);




        }

        budToRemove = allBuds.iterator();

        while(budToRemove.hasNext())
        {
            Bud r = budToRemove.next();

            freeBuds.add(r);
  
        }

        budToRemove = allBuds.iterator();

        while(budToRemove.hasNext())
        {
            Bud r = budToRemove.next();

            backlogBuds.add(r);

        }

        render(groups,freeBuds,myBuds,backlogBuds);
    }


    public static void historique()
    {

        List<Group> groups = Group.findAll();

        

        List<Bud>  allBuds = Bud.findAll();


        render(groups,allBuds);
    }





    public static void graphs()
    {
     

        List<Bud>  allBuds = Bud.findAll();


        render(allBuds);
    }


    public static void getBudsPerDay()
    {
        List<Bud> views = Bud.findAll();
        List<String> data = new ArrayList<String>();

        for(Bud r : views)
        {
            data.add("1");
        }
        renderJSON(data);
    }

    public static void show(Long id)
    {
        Bud r = Bud.findById(id);
        List<User> users = r.getViewers();
        render(r,users);
    }
    public static void share(Long id)
    {
        
        List<Group> groups = Group.findAll();
        List<User> users = User.find("order by fullName asc").fetch();

        
        render(id,groups,users);
    }
    
    
    
    public static void budAssignActor(Long rid)
    {
        Bud r = Bud.findById(rid);
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        r.action.ActOn(user);

        Event ev = new Event(user,"assign",r);
        ev.save();
        
        flash.success("Merci!");
        index();
    }


    public static void budClose(Long rid)
    {
        Bud r = Bud.findById(rid);
        User user = User.findByLinkedInId(Session.current().get("LinkedInId"));
        r.action.Close();

        Event ev = new Event(user,"close",r);
        ev.save();

        flash.success("Action terminée");
        Application.show(r.id);
    }

   


    
}
