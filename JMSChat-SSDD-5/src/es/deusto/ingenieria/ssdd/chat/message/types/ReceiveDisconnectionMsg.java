package es.deusto.ingenieria.ssdd.chat.message.types;

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;

import org.apache.activemq.command.ActiveMQTextMessage;

public class ReceiveDisconnectionMsg extends ActiveMQTextMessage {
	public ReceiveDisconnectionMsg(String myNick) {
		super();
		try {
			this.setText(myNick);
			this.setStringProperty("To", "all");
			this.setStringProperty("Class", ReceiveDisconnectionMsg.class.getCanonicalName());
		} catch (MessageNotWriteableException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
