package gitcc.util;

import gitcc.config.Config;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailUtil {

	private final Config config;

	public EmailUtil(Config config) {
		this.config = config;
	}

	public void send(Exception e) throws Exception {
		Properties props = new Properties();
		props.put("mail.smtp.host", config.getEmailSmtp());
		Session session = Session.getDefaultInstance(props, null);
		Message msg = new MimeMessage(session);
		InternetAddress addressFrom = new InternetAddress(config
				.getEmailSender());
		msg.setFrom(addressFrom);

		List<String> recipients = config.getAllEmails();
		InternetAddress[] addressTo = new InternetAddress[recipients.size()];
		for (int i = 0; i < recipients.size(); i++) {
			addressTo[i] = new InternetAddress(recipients.get(i));
		}
		msg.setRecipients(Message.RecipientType.TO, addressTo);

		msg.setSubject("[gitcc] " + e.getMessage());
		StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer));
		msg.setContent(writer.toString(), "text/plain");
		Transport.send(msg);
	}
}
