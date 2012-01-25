package controllers;

import play.*;
import play.mvc.*;
import java.util.List;
import models.Bud;


@Check("superuser")
@With(Secure.class)
public class Buds extends CRUD {




}
