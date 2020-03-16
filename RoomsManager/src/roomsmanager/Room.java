
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roomsmanager;
import javax.jms.*;
/**
 *
 * @author 1511 IRON
 */
public class Room {
    private  ConnectionFactory myConnFactory;
    private  Connection myConn;
    private  Topic myTopic;
    private  Session mySess;
    private MessageConsumer consumer;
    private String name;
    private String owner;
    
    public Room(String n, String o, MessageSender messageSender){
       
        this.name = n;
        this.owner = o;
        try{
         this.myConnFactory = new com.sun.messaging.ConnectionFactory();
            this. myConn = myConnFactory.createConnection();
            this.mySess = myConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            this.myTopic = mySess.createTopic(n);

            // create consumer to listen for mentions
            this.consumer = mySess.createConsumer(myTopic);
            this.consumer.setMessageListener(new MentionListener(messageSender, this.name));

            myConn.start();}
        catch (Exception jmse) {
            System.out.println("Exception occurred : " + jmse.toString());
            jmse.printStackTrace();
        }
    }

    /**
     * @return the name
     */
   
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }
    

}
