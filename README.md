# JMS-Chat-Example
JMS Chat Example implemented in 2013.

## Project definition
The purpose of this project is adapting the previous chat examples for working over JMS. The new version of the chat will provide the same basic chat functionality, but using an indirect communication system.

The JMS provider will be Apache ActiveMQ (<a href="http://activemq.apache.org">http://activemq.apache.org</a>).

## Design
### Functional Description
The features of the chat are:
* Messages will have a maximum length of 100 characters. The text entry won't allow the user to exceed this limit.
* Connected users list and chat requests will be updated dynamically.
* Some nicknames will be forbidden, such as _false_, and some characters, such as "/", in order to prevent errors.
* Nicknames can't be repeated.

### Architecture
The architecture of the chat will be the following:
* Apache ActiveMQ JMS provider.
* There is not a management server.
* JMS clients (which will be implemented).
* Messages:
    * Header: standard.
    * Properties (if needed):
        * To (receiver).
        * From (sender).
    * Body: TextMessage message type.
* Management objects:
    * One topic for sending and receiving messages called _SSDD5.Topic.Stream_.
    * Synchronous message reading (temporal dependency): clients can only receive messages sent after joining the topic and while they are connected.
    * Subscription type: no durable.
    * Selectors and filters: messages filtered by _to_ property.

### Protocol
The protocol will use the following message types:
* ReceiveConnectionMsg
    * It's received when a new user connects. The processing of the message adds the name to the connected users list.
    * Header: standard.
    * Extra properties: _class_.
    * Body: nick.
* ReceiveDisconnectionMsg
    * It's received when a user disconnects. The processing of this message removes the name from the connected users list.
    * Header: standard.
    * Extra properties: _class_.
    * Body: nick.
* AskForConnectedUserNamesMsg
    * Sends a _SendConnectedUserNamesMsg_ asking for the connected user names.
    * Header: standard.
    * Extra properties: _class_.
    * Body: _(empty)_.
* ReceiveConnectedUserNamesMsg
    * It's received when a user asks for the connected user names. It's ignored if the user is connected.
    * Header: standard.
    * Extra properties: _class_.
    * Body: nick`/`nick`/`...
* ReceiveChatRequestMsg
    * It's received when a user sends a chat request to another user. If the receptor of the request is already chatting or waiting for a chat request answer the request is refused automatically.
    * Header: standard.
    * Extra properties: _class_, _to_ and _from_.
    * Body: _(empty)_.
* ReceiveChatRequestAnswerMsg
    * It's received when a user answers to the chat request sent by another user. Answers can be _true_, _refuser_ or _chatting_.
    * Header: standard.
    * Extra properties: _class_, _to_ and _from_.
    * Body: answer.
* ReceiveChatClosureMsg
    * It's received when the person chatting with the user closes the chat. It closes the chat.
    * Header: standard.
    * Extra properties: _class_ and _to_.
    * Body: _(empty)_.
* ReceiveMessageMsg
    * It's received when the person chatting with the user sends a message.
    * Header: standard.
    * Extra properties: _class_ and _to_.
    * Body: text.
* SendConnectedUserNamesMsg
    * It's received when a new user wants to connect and asks for the connected usernames. It sends a _ReceiveConnectedUserNamesMsg_ message,
    * Header: standard.
    * Extra properties: _class_.
    * Body: _(empty)_.

### Failure Model
Action which causes the failure | Failure | Possible solution
--------------------------- | --------------------------- | ---------------------------
Close the window without disconnecting | User permanently connected | Automatically disconnect when the window closes or not allowing to close the window if connected
A user tries to open a chat with another user who is already chatting | The user gets blocked | The server refuses the request automatically
The user list isn't updated | The user can't start chats with users connected after | Update the list dynamically, when receiving other users' connections and disconnections.
A user tries to open a chat and the other user doesn't answer to the request | The user who tries to open the chat gets blocked | Set a timeout
A user connects with a username in use | JMS exception | Catch the exception and notify the user that the nick is already in use