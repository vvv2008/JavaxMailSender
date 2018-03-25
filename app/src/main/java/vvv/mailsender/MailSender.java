package vvv.mailsender;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Parameters in build.gradle (Module: app)
 * minSdkVersion >= 19
 * compile 'com.sun.mail:android-mail:1.6.0'
 * compile 'com.sun.mail:android-activation:1.6.0'
 * <p>
 * Warning !
 * version 1.6.1 NOT WORK NOW
 */

public class MailSender {
    private static final String TAG = "__MailSender";

    interface OnSent {
        void onSent(boolean isMailSent);
    }

    public enum ViaAccount {gmail, mailru, yandex}

    private class MailAccount {
        private String email;
        private String login;
        private String password;
        private Properties connectionProperties = new Properties();
    }

    private MailAccount gmail;
    private MailAccount mailru;
    private MailAccount yandex;


    public MailSender() {

        /**
         * Warning !
         * Google is blocking unsafe applications trying to send mail.
         * Notification comes to the email.
         * You need to go into the settings:
         * https://myaccount.google.com/lesssecureapps
         * and allow unverified applications.
         */
        gmail = new MailAccount();
        gmail.email = "test201803231428@gmail.com";
        gmail.login = "test201803231428";
        gmail.password = "Test_20180323";
        gmail.connectionProperties.put("mail.smtp.host", "smtp.gmail.com");
        gmail.connectionProperties.put("mail.smtp.port", 465);
        gmail.connectionProperties.put("mail.smtp.socketFactory.port", 465);
        gmail.connectionProperties.put("mail.smtp.auth", true);
        gmail.connectionProperties.put("mail.smtp.starttls.enable", true);
        gmail.connectionProperties.put("mail.smtp.ssl.trust", "*");
        gmail.connectionProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // if use ssl

        mailru = new MailAccount();
        mailru.email = "test201803231428@mail.ru";
        mailru.login = "test201803231428";
        mailru.password = "Tost_20180323";
        mailru.connectionProperties.put("mail.smtp.host", "smtp.mail.ru");
        mailru.connectionProperties.put("mail.smtp.port", 465);
        mailru.connectionProperties.put("mail.smtp.socketFactory.port", 465);
        mailru.connectionProperties.put("mail.smtp.auth", true);
        mailru.connectionProperties.put("mail.smtp.starttls.enable", false);
        mailru.connectionProperties.put("mail.smtp.ssl.trust", "*");
        mailru.connectionProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // if use ssl

        yandex = new MailAccount();
        yandex.email = "test201803231428@yandex.ru";
        yandex.login = "test201803231428";
        yandex.password = "Most_20180323";
        yandex.connectionProperties.put("mail.smtp.host", "smtp.yandex.ru");
        yandex.connectionProperties.put("mail.smtp.port", 465);
        yandex.connectionProperties.put("mail.smtp.socketFactory.port", 465);
        yandex.connectionProperties.put("mail.smtp.auth", true);
        yandex.connectionProperties.put("mail.smtp.starttls.enable", false);
        yandex.connectionProperties.put("mail.smtp.ssl.trust", "*");
        yandex.connectionProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // if use ssl
    }

    public void send(ViaAccount viaAccount, ArrayList<String> receivers, String subject, String text, ArrayList<String> files, OnSent onSentInterface) {
        Log.v(TAG, "send()");
        MailAccount account = null;
        switch (viaAccount) {
            case gmail:
                account = gmail;
                break;
            case mailru:
                account = mailru;
                break;
            case yandex:
                account = yandex;
                break;
        }
        boolean isSent = javaxSend(account, receivers, subject, text, files);
        onSentInterface.onSent(isSent);
    }

    private boolean javaxSend(final MailAccount account, ArrayList<String> receivers, String subject, String text, ArrayList<String> files) {
        Log.v(TAG, "javaxSend()");
        Session session = Session.getInstance(account.connectionProperties,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(account.login, account.password);
                    }
                });
        session.setDebug(true); // Set false or comment after debugging
        try {
            InternetAddress[] recipients = new InternetAddress[receivers.size()];
            for (int i = 0; i < receivers.size(); i++) {
                recipients[i] = new InternetAddress(receivers.get(i));
            }
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(account.email));
            message.setRecipients(Message.RecipientType.TO, recipients);
            if (subject != null) message.setSubject(subject);
            Multipart multipart = new MimeMultipart();
            if (text != null && text.length() > 0) {
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText(text);
                multipart.addBodyPart(messageBodyPart);
            }
            if (files != null && files.size() > 0) attachFiles(multipart, files);
            message.setContent(multipart);
            Transport.send(message);
        } catch (Exception e) {
            Log.e(TAG, "Exception e-mail sending", e);
            return false;
        } catch (Error e) {
            Log.e(TAG, "Error e-mail sending", e);
            return false;
        }
        Log.i(TAG, "E-mail sent!");
        return true;
    }

    private void attachFiles(Multipart multipart, ArrayList<String> files) throws Exception {
        Log.v(TAG, "attachFiles()");
        File f;
        for (String filename : files) {
            f = new File(filename);
            DataSource source = new FileDataSource(f);
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(f.getName());
            multipart.addBodyPart(messageBodyPart);
        }
    }
}
