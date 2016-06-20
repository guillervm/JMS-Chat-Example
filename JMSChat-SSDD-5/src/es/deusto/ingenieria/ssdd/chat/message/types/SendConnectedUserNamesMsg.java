package es.deusto.ingenieria.ssdd.chat.message.types;

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;

import org.apache.activemq.command.ActiveMQTextMessage;

public class SendConnectedUserNamesMsg extends ActiveMQTextMessage {
	public SendConnectedUserNamesMsg() {
		super();
		try {
			this.setStringProperty("To", "all");
			this.setStringProperty("Class", SendConnectedUserNamesMsg.class.getCanonicalName());
		} catch (MessageNotWriteableException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
