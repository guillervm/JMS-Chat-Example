package es.deusto.ingenieria.ssdd.chat.message.types;

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;

import org.apache.activemq.command.ActiveMQTextMessage;

public class AskForConnectedUserNamesMsg extends ActiveMQTextMessage {
	public AskForConnectedUserNamesMsg() {
		super();
		try {
			this.setStringProperty("To", "all");
			this.setStringProperty("Class", AskForConnectedUserNamesMsg.class.getCanonicalName());
		} catch (MessageNotWriteableException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
