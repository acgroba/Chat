/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package usersserver;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

/**
 *
 * @author abraham
 */
public class UsersServer implements javax.jms.MessageListener {

    private Session mySess;
    private Queue queueAuth;
   // private Topic topicAuth;
    private ConnectionFactory myConnFactory;
    private Connection myConn;
    private MessageConsumer authConsumer;
    private MessageProducer authProducer;
    private Map<String, String> users;

    public UsersServer() {
        try {
            // No-standard way of getting Context Factory
            this.users= new HashMap<>();
            myConnFactory = new com.sun.messaging.ConnectionFactory();
            myConn = myConnFactory.createConnection();
            mySess = myConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // No-standard way of getting destination
            queueAuth = mySess.createQueue("auth");
           // topicAuth = mySess.createTopic("authTopic");
            authConsumer = mySess.createConsumer(queueAuth);
            authConsumer.setMessageListener(this);
            authProducer = mySess.createProducer(null);
             myConn.start();
        } catch (Exception jmse) {
            System.out.println("Exception occurred : " + jmse.toString());
            jmse.printStackTrace();
        }
    }

 
    
    public boolean login(String name, String password){
        if(users.containsKey(name) & users.get(name).equals(password) ){
            return true;
        }
        else return false;
    }
    
    public boolean register(String name, String password){
        if(users.containsKey(name) ){
            return false;
        }
        else {
            users.put(name, password);
            return true;
        }
    }
    public void onMessage(Message msg) {
        
        try {
            if (msg instanceof MapMessage) {
                
                MapMessage message = (MapMessage) msg;
                int code = message.getInt("code");
                String name = message.getString("name");
                String password = message.getString("password");
                
                switch (code){
                    case 1:
                        if(this.register(name,password)){
                            TextMessage mensaje= mySess.createTextMessage("success");
                            
                            mensaje.setStringProperty("receiver",name);
                           mensaje.setJMSCorrelationID(msg.getJMSCorrelationID());
                            
                            authProducer.send(msg.getJMSReplyTo(),mensaje);
                        }
                        else{
                            TextMessage mensaje= mySess.createTextMessage("fail");
                            System.out.println("mal");
                            
                            mensaje.setStringProperty("receiver",name);
                           mensaje.setJMSCorrelationID(msg.getJMSCorrelationID());
                           
                            authProducer.send(msg.getJMSReplyTo(),mensaje);
                        }
                        break;
                    case 2:
                        if(this.login(name,password)){
                            TextMessage mensaje= mySess.createTextMessage("success");
                           
                            mensaje.setStringProperty("receiver",name);
                            mensaje.setJMSCorrelationID(msg.getJMSCorrelationID());
                           
                            authProducer.send(msg.getJMSReplyTo(),mensaje);
                   
                        }
                        else{
                            TextMessage mensaje= mySess.createTextMessage("fail");
                            
                            mensaje.setStringProperty("receiver",name);
                        mensaje.setJMSCorrelationID(msg.getJMSCorrelationID());
                           
                            authProducer.send(msg.getJMSReplyTo(),mensaje);
                   
                        }
                        break;
                }

            }
        } catch (JMSException jmse) {
           System.out.println("Exception occurred : " + jmse.toString());
            jmse.printStackTrace();
        }

    }
     public static void main(String[] args) throws InterruptedException {
        UsersServer inst = new UsersServer();
        while (true) {
           
            
           Thread.sleep(1000);

        }
        
    }
}