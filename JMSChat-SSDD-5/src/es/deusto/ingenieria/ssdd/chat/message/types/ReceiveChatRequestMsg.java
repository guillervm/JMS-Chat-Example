package es.deusto.ingenieria.ssdd.chat.message.types;

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;

import org.apache.activemq.command.ActiveMQTextMessage;

public class ReceiveChatRequestMsg extends ActiveMQTextMessage {
	public ReceiveChatRequestMsg(String myNick, String toNick) {
		super();
		try {
			this.setStringProperty("To", toNick);
			this.setStringProperty("From", myNick);
			this.setStringProperty("Class", ReceiveChatRequestMsg.class.getCanonicalName());
		} catch (MessageNotWriteableException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
