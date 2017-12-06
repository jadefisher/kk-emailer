package net.jadefisher.kkemail;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

/**
 * Created by jfisher on 12/5/17.
 */
public class EmailHelper {

  public static MimeMessage createEmail(final Session session, final String to, final String from,
      final String subject, final String body) {
    MimeMessage message = null;
    try {
      // Create a default MimeMessage object.
      message = new MimeMessage(session);

      // Set From: header field of the header.
      message.setFrom(new InternetAddress(from));

      // Set To: header field of the header.
      message.addRecipient(RecipientType.TO, new InternetAddress(to));

      // Set Subject: header field
      message.setSubject(subject);

      // Now set the actual message
      message.setText(body);
    } catch (MessagingException mex) {
      mex.printStackTrace();
    }
    return message;
  }

  public static void send(final Message message) {
    try {
      Transport.send(message);
    } catch (MessagingException mex) {
      mex.printStackTrace();
    }
  }
}
