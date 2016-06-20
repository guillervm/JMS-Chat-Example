package es.deusto.ingenieria.ssdd.chat.message.types;

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;

import org.apache.activemq.command.ActiveMQTextMessage;

public class ReceiveMessageMsg extends ActiveMQTextMessage {
	public ReceiveMessageMsg(String toNick, String message) {
		super();
		try {
			this.setStringProperty("To", toNick);
			this.setText(message);
			this.setStringProperty("Class", ReceiveMessageMsg.class.getCanonicalName());
		} catch (MessageNotWriteableException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
