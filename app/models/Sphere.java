package models;

import play.db.jpa.*;

import javax.persistence.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
public class Sphere extends Model
{
    public String sphereName;

    public String logoImg;

    @ManyToMany
    public List<Group> groups;

    @OneToMany(mappedBy="sphere", cascade=CascadeType.ALL)
    public List<Bud> buds;

    public Sphere(String sphereName)
    {
        this.groups = new ArrayList<Group>();
        this.sphereName = sphereName;
        this.logoImg = "it_icon.png";

    }

    public Sphere addGroup(Group group)
    {
        groups.add(group);
        this.save();
        return this;
    }

    

    public String toString()
    {
        return sphereName;
    }


}
