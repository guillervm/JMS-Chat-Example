package es.deusto.ingenieria.ssdd.chat.client.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Observer;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;

import es.deusto.ingenieria.ssdd.chat.data.Message;
import es.deusto.ingenieria.ssdd.chat.data.User;
import es.deusto.ingenieria.ssdd.chat.message.types.AskForConnectedUserNamesMsg;
import es.deusto.ingenieria.ssdd.chat.message.types.ReceiveChatClosureMsg;
import es.deusto.ingenieria.ssdd.chat.message.types.ReceiveChatRequestAnswerMsg;
import es.deusto.ingenieria.ssdd.chat.message.types.ReceiveChatRequestMsg;
import es.deusto.ingenieria.ssdd.chat.message.types.ReceiveConnectedUserNamesMsg;
import es.deusto.ingenieria.ssdd.chat.message.types.ReceiveConnectionMsg;
import es.deusto.ingenieria.ssdd.chat.message.types.ReceiveDisconnectionMsg;
import es.deusto.ingenieria.ssdd.chat.message.types.ReceiveMessageMsg;
import es.deusto.ingenieria.ssdd.chat.message.types.SendConnectedUserNamesMsg;
import es.deusto.ingenieria.ssdd.util.observer.local.LocalObservable;

public class ChatClientController implements MessageListener {
	private String serverIP;
	private int serverPort;
	private User connectedUser;
	private User chatReceiver;
	private LocalObservable observable;
	private List<String> connectedUsers;
	private String connectionFactoryName = "TopicConnectionFactory";		
	private String topicJNDIName = "ssdd.topic";	
	private String subscriberID = "";
	private TopicConnection topicConnection = null;
	private TopicSession topicSession = null;
	private TopicSubscriber topicSubscriber = null;
	
	public ChatClientController() {
		this.observable = new LocalObservable();
		this.serverIP = null;
		this.serverPort = -1;
		
		connectedUsers = new ArrayList<String>();
	}
	
	public String getConnectedUser() {
		if (this.connectedUser != null) {
			return this.connectedUser.getNick();
		} else {
			return null;
		}
	}
	
	public String getChatReceiver() {
		if (this.chatReceiver != null) {
			return this.chatReceiver.getNick();
		} else {
			return null;
		}
	}
	
	public String getServerIP() {
		return this.serverIP;
	}
	
	public int gerServerPort() {
		return this.serverPort;
	}
	
	public boolean isConnected() {
		return this.connectedUser != null;
	}
	
	public boolean isChatSessionOpened() {
		return this.chatReceiver != null;
	}
	
	public void addLocalObserver(Observer observer) {
		this.observable.addObserver(observer);
	}
	
	public void deleteLocalObserver(Observer observer) {
		this.observable.deleteObserver(observer);
	}
	
	private void resetWhenNoConnected() {
		this.connectedUser = null;
		this.connectedUsers.clear();
		this.serverIP = "";
		this.serverPort = -1;
		subscriberID = "";
	}
	
	public int connect(String ip, int port, String name) {
		this.serverIP = ip;
		this.serverPort = port;
		int ret = -1;
		name = name.trim();
		subscriberID = name;
		
		try {
			Context ctx = new InitialContext();
			
			TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) ctx.lookup(connectionFactoryName);
			Topic myTopic = (Topic) ctx.lookup(topicJNDIName);
			// Avoid using jndi.properties to use the provided ip and port using the next 4 lines and commeting the upper 2
			//ctx.removeFromEnvironment(Context.PROVIDER_URL);
			//ctx.addToEnvironment(Context.PROVIDER_URL, "tcp://"+this.serverIP+":"+this.serverPort);
			//TopicConnectionFactory topicConnectionFactory = new ActiveMQConnectionFactory("tcp://"+this.serverIP+":"+this.serverPort);
			//Topic myTopic = new ActiveMQTopic("SSDD5.Topic.Stream");

			topicConnection = topicConnectionFactory.createTopicConnection();
			topicConnection.setClientID(subscriberID);
			topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			//topicSubscriber = topicSession.createDurableSubscriber(myTopic, subscriberID, "To = '"+ subscriberID +"' OR To = 'all'", false);
			topicSubscriber = topicSession.createSubscriber(myTopic, "To = '"+ subscriberID +"' OR To = 'all'", false);
			topicSubscriber.setMessageListener(this);
			topicConnection.start();
			this.connectedUser = new User();
			connectedUser.setNick(name);
			ReceiveConnectionMsg m = new ReceiveConnectionMsg(name);
			send(m);
			System.out.println("Connected");
			askForNames();
			return 1;
		} catch (Exception e) {
			System.err.println("# TopicSubscriberTest Error: " + e.getMessage());
			if (e.getMessage().contains("already connected")) {
				ret = 0;
			}
			resetWhenNoConnected();
		} finally {	
			Thread t = new Thread(new Runnable() {		
				@Override
				public void run() {
					try {
						while (connectedUser != null) {}
					} catch (Exception ex) {
						System.err.println("# TopicSubscriberTest Error: " + ex.getMessage());
					}
				}
			});
			t.setDaemon(true);
			t.start();	
		}
		
		return ret;
	}
	
	public boolean disconnect() {
		if (chatReceiver != null) {
			sendChatClosure();
		}
		
		System.out.println("Disconnected");
		try {
			topicSubscriber.close();
			//topicSession.unsubscribe(subscriberID); // No durable connection doesn't need to unsubscribe. Session closure does it
			topicSession.close();
			topicConnection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
		ReceiveDisconnectionMsg m = new ReceiveDisconnectionMsg(connectedUser.getNick());
		send(m);
		resetWhenNoConnected();
		return true;
	}
	
	private void receiveMessage(String answer) {
		Message m = new Message();
		m.setFrom(chatReceiver);
		m.setTo(connectedUser);
		m.setTimestamp(Calendar.getInstance().getTimeInMillis());
		m.setText(answer);
		this.observable.notifyObservers(m);
	}

	private void closeChat() {
		this.chatReceiver = null;
			
		String message = "chat_closure";
		message+=String.valueOf(false);
		
		this.observable.notifyObservers(message);	
	}

	private void chatRequestAnswer(String from, String answer) {
		if (answer.equalsIgnoreCase("true")) {
			chatReceiver = new User();
			chatReceiver.setNick(from);
			this.observable.notifyObservers("true");
		} else {
			chatReceiver = null;
			this.observable.notifyObservers(answer);
		}
	}
	
	public boolean sendChatRequest(String to) {
		chatReceiver = new User();
		chatReceiver.setNick(to);
		ReceiveChatRequestMsg m = new ReceiveChatRequestMsg(connectedUser.getNick(), to);
		send(m);
		return false;
	}	
	
	public boolean sendChatClosure() {
		ReceiveChatClosureMsg m = new ReceiveChatClosureMsg(chatReceiver.getNick());
		send(m);
		this.chatReceiver = null;
		
		return true;
	}

	private void receiveChatRequest(String otherNick) {
		if (chatReceiver != null) {
			ReceiveChatRequestAnswerMsg m = new ReceiveChatRequestAnswerMsg(connectedUser.getNick(), otherNick, "chatting");
			send(m);
		} else {
			String s = "Chat request received from ";
			s += otherNick;
			this.observable.notifyObservers(s);
		}
	}

	private void receiveNicks(String answer) {
		String[] names = answer.split("/");
		for (String s:names) {
			if (!connectedUsers.contains(s)) {
				connectedUsers.add(s);
			}
		}
	}

	private void askForNames() {
		SendConnectedUserNamesMsg m = new SendConnectedUserNamesMsg();
		send(m);
	}

	private void sendNames() {
		if (connectedUser != null) {
			String s = "";
			for (String user:connectedUsers) {
				s += user;
				s += "/";
			}
			ReceiveConnectedUserNamesMsg m = new ReceiveConnectedUserNamesMsg(s);
			send(m);
		}
	}
	
	private void receiveDisconnect(String answer) {
		connectedUsers.remove(answer);
	}

	private void receiveConnect(String answer) {
		connectedUsers.add(answer);
	}
	
	public List<String> getConnectedUsers() {
		return connectedUsers;
	}
	
	public void acceptChatRequest(String from, String answer) {
		chatReceiver = new User();
		chatReceiver.setNick(from);
		ReceiveChatRequestAnswerMsg m = new ReceiveChatRequestAnswerMsg(connectedUser.getNick(), from, "true");
		send(m);
	}
	
	public void refuseChatRequest(String from, String answer) {
		chatReceiver = null;
		ReceiveChatRequestAnswerMsg m = new ReceiveChatRequestAnswerMsg(connectedUser.getNick(), from, "refused");
		send(m);
	}
	
	public boolean sendMessage(String text) {
		ReceiveMessageMsg m = new ReceiveMessageMsg(chatReceiver.getNick(), text);
		send(m);
		return true;
	}

	public boolean send(javax.jms.Message m) {
		TopicConnection topicConnection = null;
		TopicSession topicSession = null;
		TopicPublisher topicPublisher = null;		
		
		try {
			Context ctx = new InitialContext();
			TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) ctx.lookup(connectionFactoryName);
			Topic myTopic = (Topic) ctx.lookup(topicJNDIName);
			topicConnection = topicConnectionFactory.createTopicConnection();
			topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			topicPublisher = topicSession.createPublisher(myTopic);
			topicPublisher.publish(m);
		} catch (Exception e) {
			System.err.println("# TopicPublisherTest Error: " + e.getMessage());
		} finally {
			try {
				topicPublisher.close();
				topicSession.close();
				topicConnection.close();			
			} catch (Exception ex) {
				System.err.println("# TopicPublisherTest Error: " + ex.getMessage());
			}			
		}
		
		return true;
	}

	@Override
	public void onMessage(javax.jms.Message message) {
		try {
			if (message.getStringProperty("Class").equals(ReceiveConnectionMsg.class.getCanonicalName())) {
				try {
					receiveConnect(((TextMessage)message).getText());
				} catch (JMSException e) {
					e.printStackTrace();
				}
			} else if (message.getStringProperty("Class").equals(ReceiveDisconnectionMsg.class.getCanonicalName())) {
				try {
					receiveDisconnect(((TextMessage)message).getText());
				} catch (JMSException e) {
					e.printStackTrace();
				}
			} else if (message.getStringProperty("Class").equals(AskForConnectedUserNamesMsg.class.getCanonicalName())) {
				askForNames();
			} else if (message.getStringProperty("Class").equals(ReceiveConnectedUserNamesMsg.class.getCanonicalName())) {
				if (connectedUsers.size() <= 1) {
					try {
						receiveNicks(((TextMessage)message).getText());
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			} else if (message.getStringProperty("Class").equals(ReceiveChatRequestMsg.class.getCanonicalName())) {
				try {
					receiveChatRequest(((TextMessage)message).getStringProperty("From"));
				} catch (JMSException e) {
					e.printStackTrace();
				}
			} else if (message.getStringProperty("Class").equals(ReceiveChatRequestAnswerMsg.class.getCanonicalName())) {
				try {
					chatRequestAnswer(((TextMessage)message).getStringProperty("From"), ((TextMessage)message).getText());
				} catch (JMSException e) {
					e.printStackTrace();
				}
			} else if (message.getStringProperty("Class").equals(ReceiveChatClosureMsg.class.getCanonicalName())) {
				closeChat();
			} else if (message.getStringProperty("Class").equals(ReceiveMessageMsg.class.getCanonicalName())) {
				try {
					receiveMessage(((TextMessage)message).getText());
				} catch (JMSException e) {
					e.printStackTrace();
				}
			} else if (message.getStringProperty("Class").equals(SendConnectedUserNamesMsg.class.getCanonicalName())) {
				sendNames();
			}
		} catch (JMSException e1) {
			e1.printStackTrace();
		}
	}
}