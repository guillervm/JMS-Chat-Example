package es.deusto.ingenieria.ssdd.chat.message.types;

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;

import org.apache.activemq.command.ActiveMQTextMessage;

public class ReceiveConnectionMsg extends ActiveMQTextMessage {
	public ReceiveConnectionMsg(String myNick) {
		super();
		try {
			this.setText(myNick);
			this.setStringProperty("To", "all");
			this.setStringProperty("Class", ReceiveConnectionMsg.class.getCanonicalName());
		} catch (MessageNotWriteableException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
