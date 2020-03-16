package roomsmanager;

import javax.jms.*;

class MessageSender {
    private Topic topic;
    private MessageProducer producer;
    private Session session;

    MessageSender(Session session) {
        this.session = session;
        try {
            this.topic = session.createTopic("admin");
            this.producer = session.createProducer(topic);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    void sendAdminMsg(String s) {
        try {
            TextMessage message = session.createTextMessage("ADMIN: " + s);
            producer.send(message);

        } catch (Exception jmse) {
            System.out.println("Exception occurred : " + jmse.toString());
            jmse.printStackTrace();
        }
    }

    void sendMention(String user, String room, String msg) {
        try {
            MapMessage message = session.createMapMessage();
            message.setString("mention", user);
            message.setString("room", room);
            message.setString("msg", msg);
            
            producer.send(message);
        } catch (Exception jmse) {
            System.out.println("Exception occurred : " + jmse.toString());
            jmse.printStackTrace();
        }
    }
}
