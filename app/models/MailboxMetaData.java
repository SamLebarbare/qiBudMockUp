package models;

import java.util.ArrayList;
import java.util.List;

public class MailboxMetaData implements Comparable {

    public String id;
    public String text;
    public List<MailboxMetaData> children = new ArrayList<MailboxMetaData>();

    public int compareTo(Object obj) {
       return ((MailboxMetaData)obj).id.compareTo(id);
    }
}
