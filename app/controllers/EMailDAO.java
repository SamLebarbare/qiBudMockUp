package controllers;

import models.EmailMetaData;
import models.MailboxMetaData;
import org.apache.commons.io.IOUtils;

import javax.mail.*;
import javax.mail.Flags.Flag;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class EMailDAO implements Serializable {

    private static final long serialVersionUID = 611947646402810289L;
    private Store store;
    private String protocol;
    private String mailServer;
    private int port;
    private String userName;
    private String password;

    public EMailDAO(String protocol, String mailServer, int port,
                    String userName, String password) {
        this.protocol = protocol;
        this.mailServer = mailServer;
        this.port = port;
        this.userName = userName;
        this.password = password;
    }

    public void connect() throws MessagingException {
        // login to the IMAP server
        Properties props = new Properties();
        Session session = Session.getInstance(props, null);
        session.setDebug(false);
        store = session.getStore(protocol);
        store.connect(mailServer, port, userName, password);
    }

    public List<MailboxMetaData> getFolders() throws MessagingException,
            UnsupportedEncodingException {
        List<Folder> folders = Arrays.asList(store.getDefaultFolder().list());
        List<MailboxMetaData> mailboxes = new ArrayList<MailboxMetaData>();
        addToList(mailboxes, folders);
        return mailboxes;
    }

    private void addToList(List<MailboxMetaData> mailboxes, List<Folder> folders)
            throws MessagingException, UnsupportedEncodingException {
        if (folders == null) {
            return;
        }
        for (Folder folder : folders) {
            MailboxMetaData mailbox = new MailboxMetaData();
            mailbox.id = getFullName(folder);
            mailbox.text = folder.getName();
            mailboxes.add(mailbox);
            addToList(mailbox.children, Arrays.asList(folder.list()));
        }
    }

    public String getFullName(Folder folder) throws MessagingException,
            UnsupportedEncodingException {
        String folderName = URLEncoder.encode(folder.getName(), "UTF-8");
        while (folder.getParent() != null) {
            folder = folder.getParent();
            folderName = URLEncoder.encode(folder.getName(), "UTF-8") + "/" + folderName;
        }
        return folderName.substring(1);
    }

    public void close() throws MessagingException {
        store.close();
    }

    public boolean isConnected() {
        return store != null && store.isConnected();
    }

    public Folder createFolder(String name) throws MessagingException {

        Folder folder = store.getFolder(name);
        try {
            if (!folder.exists()) {
                folder.create(Folder.HOLDS_MESSAGES);
            }
        } catch (FolderNotFoundException e) {
            folder.create(Folder.HOLDS_MESSAGES);
        }
        return folder;
    }

    public void deleteMessage(Message message, String trashMailbox)
            throws MessagingException {
        Folder folder = message.getFolder();
        // If the folder is already open, close it?
        if (folder.isOpen()) {
            folder.close(true);
        }
        folder.open(Folder.READ_WRITE);
        if (trashMailbox != null) {
            Folder deletedFolder = store.getFolder(trashMailbox);
            if (!deletedFolder.exists()) {
                deletedFolder.create(Folder.HOLDS_MESSAGES);
            }
            deletedFolder.open(Folder.READ_WRITE);
            if (!trashMailbox.equals(folder.getName())) {
                folder.copyMessages(new Message[]{message}, deletedFolder);
                message.setFlag(Flag.SEEN, true);
            }
        }
        message.setFlag(Flag.DELETED, true);
        folder.expunge();
    }

    public Folder getDefaultFolder() throws MessagingException {

        return store.getDefaultFolder();
    }

    public Folder getFolder(String name) throws MessagingException {
        return store.getFolder(name);
    }

    public List<EmailMetaData> getMessages(Folder folder, int start, int end)
            throws MessagingException {
        final int numberOfEmail = folder.getMessageCount();
        int s = (numberOfEmail - end);
        int e = (numberOfEmail - start);
        if (s == 0)
            s = 1;
        if (e == 0)
            e = 1;
        System.out.println("getting from [" + s + "] to [" + e + "]");
        Message[] messages = folder.getMessages(s, e);
        List<EmailMetaData> emails = new ArrayList<EmailMetaData>();
        // TODO: Should be somewhere else
        for (Message message : messages) {
            EmailMetaData email = new EmailMetaData(message);
            emails.add(0, email);
        }
        return emails;
    }

    public Message getMessage(String[] folderNames, int mode, int messageId)
            throws MessagingException {
        Folder folder = openFolder(folderNames, mode);
        return folder.getMessage(messageId);
    }

    public Folder openFolder(String folderName, int mode)
            throws MessagingException {
        Folder folder = getFolder(folderName);
        if (!folder.isOpen()) {
            folder.open(mode);
        }
        return folder;
    }

    public Folder openFolder(String[] folderNames, int mode)
            throws MessagingException {
        List<String> folders = new ArrayList<String>(Arrays.asList(folderNames));
        folders.remove(0);

        Folder folder = getFolder(folderNames[0]);
        for (String f : folders) {
            folder = folder.getFolder(f);
        }
        if (!folder.isOpen()) {
            folder.open(mode);
        }

        return folder;
    }

    public Message findMessage(Folder folder, int messageId)
            throws MessagingException {
        return folder.getMessage(messageId);
    }

    public EmailMetaData findEmail(Folder folder, int messageId)
            throws MessagingException, IOException {
        // TODO: Should be somewhere else
        Message message = findMessage(folder, messageId);
        return new EmailMetaData(message);
    }

    public String getContent(Message message) throws MessagingException,
            IOException {
        return getContent(message.getFolder().getName(), message.getMessageNumber(), message, "text/html");
    }

    public String getContent(Message message, String contentType) throws MessagingException,
            IOException {
        return getContent(message.getFolder().getName(), message.getMessageNumber(), message, contentType);
    }

    /**
     * Return the content as text
     *
     * @param message
     * @param contentType
     * @return
     * @throws MessagingException
     * @throws IOException
     */
    public String getContent(String mailbox, int messageId, Part message, String contentType) throws MessagingException,
            IOException {

        if (message.getContentType().startsWith(contentType)) {
            return (String) message.getContent();
        } else if (message.getContent() != null && message.getContent() instanceof Multipart) {
            Multipart part = (Multipart) message.getContent();
            String text = "";
            for (int i = 0; i < part.getCount(); i++) {
                BodyPart bodyPart = part.getBodyPart(i);
                if (!Message.ATTACHMENT.equals(bodyPart.getDisposition())) {
                    text += getContent(mailbox, messageId, bodyPart, contentType);
                } else {
                    String cid = bodyPart.getFileName();
                    String localContentType = bodyPart.getContentType().replaceAll(";.*", "");
                    localContentType =localContentType.substring(localContentType.indexOf("/") + 1);
                    text += "<image src='/attachments/" + mailbox + "/" + messageId + "/" + URLEncoder.encode(cid, "UTF-8") + "/" + localContentType + "/inline'/>";
                }
            }
            return text;
        }
        if (message.getContent() != null && message.getContent() instanceof Part) {
            if (!Message.ATTACHMENT.equals(message.getDisposition())) {
                return getContent(mailbox, messageId, (Part) message.getContent(), contentType);
            }
        }

        return "";
    }


    /**
     * Return the content as text
     *
     * @param message
     * @return
     * @throws MessagingException
     * @throws IOException
     */
    public byte[] getContent(Part message, String cid, String contentType) throws MessagingException,
            IOException {

        if (message.getContent() != null && message.getContent() instanceof Multipart) {
            Multipart part = (Multipart) message.getContent();
            for (int i = 0; i < part.getCount(); i++) {
                BodyPart bodyPart = part.getBodyPart(i);
                if (Message.ATTACHMENT.equals(bodyPart.getDisposition())) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    IOUtils.copy(bodyPart.getDataHandler().getInputStream(), out);
                    return out.toByteArray();
                }
            }
        }
        return null;
    }

}