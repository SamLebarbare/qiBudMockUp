package controllers;

import play.*;
import play.mvc.*;


@Check("superuser")
@With(Secure.class)
public class Users extends CRUD {




}
