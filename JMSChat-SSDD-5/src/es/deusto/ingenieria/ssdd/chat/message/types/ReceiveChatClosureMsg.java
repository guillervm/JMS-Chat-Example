package es.deusto.ingenieria.ssdd.chat.message.types;

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;

import org.apache.activemq.command.ActiveMQTextMessage;

public class ReceiveChatClosureMsg extends ActiveMQTextMessage {
	public ReceiveChatClosureMsg(String toNick) {
		super();
		try {
			this.setStringProperty("To", toNick);
			this.setStringProperty("Class", ReceiveChatClosureMsg.class.getCanonicalName());
		} catch (MessageNotWriteableException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
