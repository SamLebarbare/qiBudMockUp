import models.*;
//import play.*;
import play.jobs.*;
import play.test.*;

//import javax.inject.Inject;
//import javax.naming.NamingException;
//import javax.naming.directory.Attributes;


//import org.springframework.ldap.core.DistinguishedName;
//import org.springframework.ldap.core.LdapTemplate;
//import org.springframework.ldap.core.AttributesMapper;
//import org.springframework.ldap.filter.AndFilter;
//import org.springframework.ldap.filter.EqualsFilter;

//import java.util.List;


@OnApplicationStart
public class Bootstrap extends Job
{
    //@Inject private static LdapTemplate ldap;

    public void doJob() {
        // Check if the database is empty
        if(Group.count() == 0)
        {
            Group ng = new Group("BetaBudosphere");
            ng.save();
        }
           
    }



}
