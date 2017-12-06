package net.jadefisher.kkemail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.mail.Session;

/**
 * Created by jfisher on 12/4/17.
 */
public class KKEmailer {

  private static final String BODY_TEMPLATE = "Hello _NAME_,\n"
      + "\n"
      + "Merry Christmas!\n"
      + "\n"
      + "Lucky you, you're buying for _OTHER_ this year. Good luck with that!\n"
      + "\n"
      + "One idea you could use is _GIFTIDEA_ as I'm sure it would be a winner.\n"
      + "\n"
      + "Who knows, maybe someone will get you _IDEAFORYOU_.\n"
      + "\n"
      + "Cheers,\n"
      + "KK Master\n"
      + "\n"
      + "P.S. Let's update for inflation and make it $110 this year.";

  private static final String SUBJECT_TEMPLATE = "2017 KK allocation for _NAME_";

  public static void main(String... args) {
    new KKEmailer().sendEmails(
        Stream.of(
            new Member("john", "john@gmail.com", "polo black cologne", "Jo"),
            new Member("Jo", "jo@gmail.com", "a laser level", "john")
        ).collect(Collectors.toSet()),
        "mail.iinet.net.au",
        25,
        "no-reply@iinet.net.au",
        new File("/Users/jfisher/temp/emails"));
  }

  public void sendEmails(Set<Member> members,
      String host,
      int port,
      String replyEmail,
      File storageDir) {
    Set<Member> receivers = new HashSet<>();
    Set<MailMeta> messages = new HashSet<>();

    // create the mail
    members.stream().forEach(gifter -> {
      Member receiver = getAtRandom(
          members.stream()
              .filter(member ->
                  !receivers.contains(member) &&
                      !gifter.spouse.equals(member.name) &&
                      member != gifter)
              .collect(Collectors.toSet()));

      String body = resolve(BODY_TEMPLATE, gifter, receiver);
      String subject = resolve(SUBJECT_TEMPLATE, gifter, receiver);

      messages.add(new MailMeta(gifter.name, gifter.email, replyEmail, subject, body));

      receivers.add(receiver);
    });

    // write the files
    storageDir.mkdirs();
    for (MailMeta meta : messages) {
      try (FileOutputStream fos = new FileOutputStream(
          storageDir.getAbsolutePath() + "/" + meta.id + ".mail");
          ObjectOutputStream oos = new ObjectOutputStream(fos)) {
        oos.writeObject(meta);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // send the emails
    // Get system properties
    Properties properties = System.getProperties();

    // Setup mail server
    properties.setProperty("mail.smtp.host", host);
    properties.setProperty("mail.smtp.port", String.valueOf(port));

    // Get the default Session object.
    Session session = Session.getDefaultInstance(properties);

    for (MailMeta meta : messages) {
      EmailHelper.send(
          EmailHelper.createEmail(session, meta.to, meta.from, meta.subject, meta.body));
    }
  }

  private Member getAtRandom(Set<Member> members) {
    return members.stream()
        .collect(Collectors.toList())
        .get(ThreadLocalRandom.current().nextInt(0, members.size()));
  }

  private String resolve(String template, Member gifter, Member receiver) {
    return template
        .replace("_NAME_", gifter.name)
        .replace("_IDEAFORYOU_", gifter.giftIdea)
        .replace("_OTHER_", receiver.name)
        .replace("_GIFTIDEA_", receiver.giftIdea);
  }


  static class Member {

    private String name;
    private String email;
    private String giftIdea;
    private String spouse;

    public Member(String name, String email, String giftIdea, String spouse) {
      this.name = name;
      this.email = email;
      this.giftIdea = giftIdea;
      this.spouse = spouse;
    }
  }

  static class MailMeta implements Serializable {

    private String id;
    private String to;
    private String from;
    private String subject;
    private String body;

    public MailMeta(String id, String to, String from, String subject, String body) {
      this.id = id;
      this.to = to;
      this.from = from;
      this.subject = subject;
      this.body = body;
    }
  }
}
