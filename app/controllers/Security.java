package controllers;

import models.User;
import play.mvc.Scope.Session;
//import javax.inject.Inject;
//import org.springframework.ldap.core.DistinguishedName;
//import org.springframework.ldap.core.LdapTemplate;
//import org.springframework.ldap.filter.AndFilter;
//import org.springframework.ldap.filter.EqualsFilter;
//import javax.naming.directory.SearchControls;
//import javax.naming.NamingEnumeration;




public class Security extends Secure.Security
{

    //@Inject private static LdapTemplate ldap;


    static boolean authenticate(String username,String password)
    {
        /*if(!password.equals(""))
        {
            AndFilter filter = new AndFilter();

            filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("sAMAccountName", username));
            
            return ldap.authenticate(DistinguishedName.EMPTY_PATH, filter.encode(), password);
        }
        else
        {
            return false;
        }*/
        User user = User.find("fullname = ? and linkedInToken = ? ",username,password).first();
        if(user!=null)
        {

           return true;
            
        }
        else
        {
            return false;
        }    
        
        

    }
                
            
    static void onDisconnected()
    {
        Application.index(50);
    }
    static void onAuthenticated()
    {
        Application.index(50);
    }

    static boolean check(String profile)
    {
        if("admin".equals(profile))
        {
            return User.findByLinkedInId(Session.current().get("LinkedInId")).isAdmin;
        }
        if("superuser".equals(profile))
        {
            return User.findByLinkedInId(Session.current().get("LinkedInId")).isSuperUser;
        }
        return false;
    }





}
