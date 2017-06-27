package org.fundaciobit.githubmanager;


import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author anadal
 *
 */
public class SendHTMLMail {
  private static final Log log = LogFactory.getFactory().getInstance(SendHTMLMail.class);

  public static void main(String[] args) {
    try {

      log.info("HOLA CARACOLA !!!!");
      Properties properties = new Properties();

      properties.load(new FileInputStream("mail.properties"));

      new SendHTMLMail().sendMessage(properties, "Subject -----",
          "<h3>This is actual message</h3>", new File("backup.log") );
    } catch (Exception mex) {
      mex.printStackTrace();
    }
  }
  
  
  
  public static void sendMessage(String name, Properties properties, Exception e) throws Exception {
     
    final String subject;
    final String htmlBody;
    if (e == null) {
       subject = "Backup de " + name + " realitzat correctament";
       htmlBody = "";
    } else {
      subject = "Error realitzant backup de " + name + "(" + e.getMessage() + ")";
       htmlBody = ExceptionUtils.getFullStackTrace(e);
    }

    sendMessage(properties, subject, htmlBody, new File("output_" + name + ".txt"), new File("backup.log"));
  }
  
  

  public static void sendMessage(Properties properties, String subject, final String htmlBody,
      File ... attachments) throws Exception {
    
    String mailfrom = properties.getProperty("mail.from");
    
    if (mailfrom == null) {
      log.info("No s'evia cap correu.");
      return;
    }
    
    
    
    // Get the default Session object.
    Session session = Session.getInstance(properties);

    // Create a default MimeMessage object.
    MimeMessage message = new MimeMessage(session);

    // Set From: header field of the header.
    message.setFrom(new InternetAddress(mailfrom));

    // Set To: header field of the header.

    message.addRecipient(Message.RecipientType.TO,
        new InternetAddress(properties.getProperty("mail.to")));

    // Set Subject: header field
    message.setSubject(subject);

    // Send the actual HTML message, as big as you like
    //message.setContent("<h1>This is actual message</h1>", "text/html");
    
    Multipart mp = new MimeMultipart();

    MimeBodyPart htmlPart = new MimeBodyPart();
    htmlPart.setContent(htmlBody, "text/html");
    mp.addBodyPart(htmlPart);

    if (attachments !=null && attachments.length != 0) {
      
      for (int j = 0; j < attachments.length; j++) {
        if (attachments[j] == null || !attachments[j].exists()) {
          continue;          
        }
      
        final byte[] attachmentData = FileUtils.readFileToByteArray(attachments[j]);
      
        MimeBodyPart attachment = new MimeBodyPart();
        attachment.setFileName(attachments[j].getName());
        attachment.setContent(attachmentData, "txt/plain");
        mp.addBodyPart(attachment);
      }
      
    }

    message.setContent(mp);


    // Send message
    Transport.send(message);
    Thread.sleep(1500);
          
    System.out.println("Sent message successfully....");
  }
}
