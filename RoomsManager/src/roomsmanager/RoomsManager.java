
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roomsmanager;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Iterator;
import javax.jms.*;


public class RoomsManager implements MessageListener {
    int TIME=600000;
    ManageRoom manager;
    
    ConnectionFactory myConnFactory;
    Connection myConn;
    Queue userManager;
  
    Session mySess;
    // para comunicarse individualmente con los users
    MessageConsumer userConsumer;
   //para comunicarse con todos los users
 
    MessageSender messageSender;

    

    public RoomsManager() {
        try {
            // No-standard way of getting Context Factory
            myConnFactory = new com.sun.messaging.ConnectionFactory();
            myConn = myConnFactory.createConnection();
            mySess = myConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // No-standard way of getting destination
            userManager = mySess.createQueue("usersRequests");
            userConsumer = mySess.createConsumer(userManager);
            userConsumer.setMessageListener(this);

            messageSender = new MessageSender(mySess);

            myConn.start();
            //mySess.close();
            //myConn.close();
            manager = new ManageRoom(messageSender);
        } catch (Exception jmse) {
            System.out.println("Exception occurred : " + jmse.toString());
            jmse.printStackTrace();
        }
    }

    public String getSalasDisponibles(){
         Iterator rooms = this.manager.getRooms().keySet().iterator();
           if(rooms.hasNext()){
           String roomsList=".-^-.-SALAS DISPONIBLES-.-^-.\n";
            int i=1;
            while(rooms.hasNext()){
                
                roomsList+=i+")"+rooms.next()+"\n";
                i++;
                
            }
             return roomsList;
           }
           else return ".-^-.-SALAS DISPONIBLES-.-^-.\n no rooms";
           
    }
    public static void main(String[] args) throws InterruptedException {
        RoomsManager inst = new RoomsManager();
        while (true) {
           
            inst.messageSender.sendAdminMsg(inst.getSalasDisponibles());
            Thread.sleep(inst.TIME);
        }
    }

    @Override
    public void onMessage(Message msg) {
        
        try {
        if (msg instanceof MapMessage) {
                MapMessage message = (MapMessage) msg;
                int code = message.getInt("code");
                
                String room = message.getString("room");
                String user = message.getString("user");
                String aviso="";
                switch( manager.handleRequest(code, room, user)){
                case 0:
                         
                aviso+="El usuario "+user+" ha ";
                if(code==1) aviso+="creado ";
                else aviso+="borrado ";
                aviso+="la sala "+room+" \n";
                
                break;
                
                case 1:
                aviso+="La sala ya existe";
                break;
                case 2:
                aviso+="La sala no existe";
                break;
                case 3:
                aviso+="El usuario "+user+" ha querido borrar la sala sin ser su propietario";
                break;
                        

                }
                this.messageSender.sendAdminMsg(aviso);
                
                
                

            
        }
        if(msg instanceof TextMessage){
            
            
                if(((TextMessage) msg).getText().equals("getSalas")){
                    
                this.messageSender.sendAdminMsg(getSalasDisponibles());
                }

            
        }
        } catch (JMSException ex) {
                Logger.getLogger(RoomsManager.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
        


}

