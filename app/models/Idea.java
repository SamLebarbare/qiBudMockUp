/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package models;

import play.db.jpa.*;

import javax.persistence.*;

@Entity
public class Idea extends Model {

    @OneToOne
    public Bud origin;

    @ManyToOne
    public Project project;

    @ManyToOne
    public Mission mission;

    public Idea(Bud bud)
    {
        this.origin = bud;
    }

}
