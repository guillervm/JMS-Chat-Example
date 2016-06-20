package es.deusto.ingenieria.ssdd.chat.message.types;

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;

import org.apache.activemq.command.ActiveMQTextMessage;

public class ReceiveConnectedUserNamesMsg extends ActiveMQTextMessage {
	public ReceiveConnectedUserNamesMsg(String nicks) {
		super();
		try {
			this.setText(nicks);
			this.setStringProperty("To", "all");
			this.setStringProperty("Class", ReceiveConnectedUserNamesMsg.class.getCanonicalName());
		} catch (MessageNotWriteableException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
