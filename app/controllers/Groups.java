package controllers;

import play.*;
import play.mvc.*;


@Check("superuser")
@With(Secure.class)
public class Groups extends CRUD {

    
}
