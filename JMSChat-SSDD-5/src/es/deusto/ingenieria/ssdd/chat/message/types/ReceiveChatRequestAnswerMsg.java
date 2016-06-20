package es.deusto.ingenieria.ssdd.chat.message.types;

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;

import org.apache.activemq.command.ActiveMQTextMessage;

public class ReceiveChatRequestAnswerMsg extends ActiveMQTextMessage {
	public ReceiveChatRequestAnswerMsg(String myNick, String toNick, String answer) {
		super();
		try {
			this.setStringProperty("To", toNick);
			this.setStringProperty("From", myNick);
			this.setStringProperty("Class", ReceiveChatRequestAnswerMsg.class.getCanonicalName());
			this.setText(answer);
		} catch (MessageNotWriteableException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
