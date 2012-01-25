/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package models;

import play.db.jpa.*;

import javax.persistence.*;

@Entity
public class Result extends Model {

    @OneToOne
    public Bud origin;

    @ManyToOne
    public Mission mission;
    
    @ManyToOne
    public User actor;
    public boolean assigned;

    public boolean isASuccess=false;
    public boolean isAFail=false;
    public boolean define=false;
    public String status;
    
    
    

    public Result(Bud bud)
    {
        this.origin = bud;
        this.status = "non-définit";
    }

    public void isASuccess(User actor)
    {
        this.isASuccess = true;
        this.isAFail = false;
        this.define = true;
        this.actor = actor;
        this.status = "Succès";
        this.save();
    }

    public void isAFail(User actor)
    {
        this.isASuccess = false;
        this.isAFail = true;
        this.define = true;
        this.actor = actor;
        this.status = "Raté";
        this.save();
    }
    
    public Result ActOn(User user)
    {
        this.actor = user;
        this.assigned = true;
        this.status = "Assigné en attente d'un résultat";
        this.save();
        
        return this;
    }

    public Result reOpen()
    {
        this.assigned = false;
        this.status = "non-définit";

        this.save();
        return this;
    }

}
