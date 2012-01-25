package notifiers;

import models.*;
import play.mvc.*;

public class Mails extends Mailer {

    public static void cloture(User user,Bud r) {
 
    }

    public static void backlog(User user,Bud r) {

    }

    public static void assigne(User user,Bud r) {

    }


    public static void commentaire(User user,String content,Bud r) {
  
        
    }

    public static void share(User user,User viewer,Bud req)
    {
        setSubject("Demande partagée");
        addRecipient(viewer.email);
        setFrom("no-reply@corum.ch");
        //send(user,viewer,req);
    }


    public static void newuser(User user) {
        setSubject("Nouvel utilisateur: " + user.login);
        addRecipient("sloup@corum.ch");
        setFrom("no-reply@corum.ch");
        //send(user);
    }

    public static void newbud(User user,Bud r) {
        setSubject("Nouvel demande de " + r.creator.fullname);
        addRecipient(user.email);
        setFrom("no-reply@corum.ch");
        //send(r);
    }

}
