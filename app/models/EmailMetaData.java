package models;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Message.RecipientType;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.StringEscapeUtils;

import com.ocpsoft.pretty.time.PrettyTime;
import play.data.binding.As;


public class EmailMetaData {

    public int id;
    public String subject;
    public String from;
    //public String to;
    public String dateReceived;   
    @As(",")
    public List<String> recipientsTo;
    @As(",")
    public List<String> recipientsCc;
    public String replyTo;                              
    // Only used when sending email. Should probably use a 'profile'
    public String content;

    public boolean isSeen;
    public boolean isFlagged;
    public boolean isAnswered;
    public boolean isRecent;

    public EmailMetaData() {
        // Left empty
    }

    public EmailMetaData(Message message) throws MessagingException {
        this.from = StringEscapeUtils.escapeHtml(message.getFrom()[0].toString());
        this.subject = message.getSubject();
        PrettyTime p = new PrettyTime();
        this.dateReceived = p.format(message.getReceivedDate());
        this.id = message.getMessageNumber();
        Address[] toAddresses = message.getRecipients(RecipientType.TO);
        Address[] ccAddresses = message.getRecipients(RecipientType.CC);
        this.recipientsTo = arrayToList(toAddresses);
        this.recipientsCc = arrayToList(ccAddresses);
        this.isRecent = message.getFlags().contains(Flags.Flag.RECENT);
        this.isSeen = message.getFlags().contains(Flags.Flag.SEEN);
        this.isAnswered = message.getFlags().contains(Flags.Flag.ANSWERED);
        this.isFlagged = message.getFlags().contains(Flags.Flag.FLAGGED);
   }

    private List<String> arrayToList(Address[] addresses) {
        if (addresses == null) {
            return null;
        }
        List<String> to = new ArrayList<String>();
        for (Address address : addresses) {
            to.add(StringEscapeUtils.escapeHtml(address.toString()));
        }
        return to;
    }

}