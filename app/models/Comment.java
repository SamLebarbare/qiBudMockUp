package models;


import play.db.jpa.*;
import javax.persistence.*;
import java.io.File;
import java.util.*;

@Entity
public class Comment extends Model
{

    @Lob
    public String title;

    public Date createdAt;

    public String filepath;

    @ManyToOne
    public User creator;

    @ManyToOne
    public Bud bud;

    public Comment(User u,String content,Bud r)
    {
        this.title = content;
        this.createdAt = new Date();
        this.creator = u;
        this.bud = r;
    }

}
