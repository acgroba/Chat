/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user;

/**
 *
 * @author abraham
 */
import com.sun.messaging.ConnectionConfiguration;
import java.util.Random;
import javax.jms.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class User implements javax.jms.MessageListener {

    private boolean flag = false;
    private boolean flag2 = false;
    private String user;
    private String room;
    private String salasDisponibles;
    private ConnectionFactory myConnFactory;
    private Connection myConn;
    private Queue userManager;
    private Topic roomsInfoTopic;
    private Topic roomTopic;
    private Session mySess;
    // para comunicarse con el roomsManager
    private MessageProducer managerProducer;
   
    //para recibir mensajes admin
    private MessageConsumer adminMsgConsumer;
    
    //para conectarse a una sala en topic roomTopic
    private MessageConsumer RoomConsumer;
    private MessageProducer RoomProducer;
    
      // para login
    private Queue queueAuth;
    private MessageProducer authProducer;
    private Destination tempDest;
    private MessageConsumer authConsumer;

    


    public User(String user) {
        try {
            this.user = user;

            this.myConnFactory = new com.sun.messaging.ConnectionFactory();
          
            this.myConn = myConnFactory.createConnection();
             this.myConn.setClientID(this.user);
            this.mySess = myConn.createSession(false, Session.AUTO_ACKNOWLEDGE);

           
           
            this.userManager = mySess.createQueue("usersRequests");
            this.managerProducer = mySess.createProducer(userManager);
       
            
              this.queueAuth = mySess.createQueue("auth");
             // this.topicAuth = mySess.createTopic("authTopic");
            this.authProducer = mySess.createProducer(queueAuth);
           // this.authConsumer = mySess.createConsumer(topicAuth);
             // authConsumer.setMessageListener(this);
          
            
           
            
            this.roomsInfoTopic = mySess.createTopic("admin");
            this.adminMsgConsumer = mySess.createConsumer(roomsInfoTopic);
            adminMsgConsumer.setMessageListener(this);
           
            myConn.start();
        } catch (Exception jmse) {
            System.out.println("Exception occurred : " + jmse.toString());
            jmse.printStackTrace();
        }
    }

    public boolean connectToRoom(String room) {
        try {
            if (salasDisponibles.contains(room)) {
                this.room = room;
                
                this.roomTopic = mySess.createTopic(room.replace(" ",""));
              
                this.RoomConsumer = mySess.createDurableConsumer(roomTopic, this.user);
                RoomConsumer.setMessageListener(this);
                this.RoomProducer = mySess.createProducer(roomTopic);
                System.out.println("HA ENTRADO EN LA SALA: " + room + "; para salir escriba 'exit'");
                return true;
            } else {
                System.out.println("SALA NO DISPONIBLE");
                return false;
            }
        } catch (Exception jmse) {
            System.out.println("Exception occurred : " + jmse.toString());
            jmse.printStackTrace();
            return false;
        }

    }

    public void sendToRoom(String msg) {
        try {
            TextMessage message = mySess.createTextMessage("CHAT "+this.user + " in " + this.room + ": " + msg);
            RoomProducer.send(message);
        } catch (Exception jmse) {
            System.out.println("Exception occurred : " + jmse.toString());
            jmse.printStackTrace();
        }

    }

    @Override
    public void onMessage(Message msg) {
       ;
        try {
            if (msg instanceof MapMessage) {
                MapMessage message = (MapMessage) msg;
                
                if (message.itemExists("mention") && message.itemExists("room")) {
                    // handle mention message
                    String user = message.getString("mention");
                    if (this.user.equals(user)) {
                        System.out.println("MENTION:"+message.getString("msg"));
                    }
                }
            } else {

                TextMessage mensajeTexto = (TextMessage) msg;
                if(mensajeTexto.getText().contains("ADMIN:") ||mensajeTexto.getText().contains("CHAT") ){
                   System.out.println(mensajeTexto.getText());
                if (mensajeTexto.getText().contains("no rooms")) {
                   
                    this.flag = true;
                }
                else {if (mensajeTexto.getText().contains("SALAS DISPONIBLES")) {
                   
                    salasDisponibles = mensajeTexto.getText();
                    if(!mensajeTexto.getText().contains("no rooms")){
                        this.flag=false;
                    }
                }}}
                else{
                 if ( mensajeTexto.getText().equals("success") &&  msg.getStringProperty("receiver").equals(this.user) ) {
                    
                    flag2=true;
                }}
                

            }
        } catch (JMSException jmse) {
            System.out.println("Exception occurred : " + jmse.toString());
            jmse.printStackTrace();
        }
    }

    public void enviarSala(int code, String room, String owner) {

        try {

            MapMessage message = mySess.createMapMessage();
            message.setInt("code", code);
            message.setString("room", room);
            message.setString("user", owner);
          
            managerProducer.send(message);

        } catch (Exception jmse) {
            System.out.println("Exception occurred : " + jmse.toString());
            jmse.printStackTrace();
        }

    }
     private String createRandomString() {
        Random random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }
    public boolean logIn(String name) {
        Scanner scan = new Scanner(System.in);
        //send credentials
        try {
            
            MapMessage message = mySess.createMapMessage();

            message.setInt("code", 2);
            message.setString("name", name);

            System.out.println("Introduzca su contraseña");
            String password = scan.nextLine();
            message.setString("password", password);
             tempDest = mySess.createTemporaryQueue();
            this.authConsumer=mySess.createConsumer(tempDest);
            authConsumer.setMessageListener(this);
            message.setJMSReplyTo(tempDest);
            String correlationId=this.createRandomString();
            message.setJMSCorrelationID(correlationId);
            this.authProducer.send(message);
            Thread.sleep(200);

        } catch (Exception jmse) {
           System.out.println("Exception occurred : " + jmse.toString());
            jmse.printStackTrace();
        }

        //receive credentials
     
           
           
            
            
            
            if (flag2) {
                
                    return true;
                
            }

        
        System.out.println("--------MENÚ:Login incorrecto-------");
        return false;
    }

    public boolean Register(String name) {
        Scanner scan = new Scanner(System.in);
        //send credentials
        try {
            
            MapMessage message = mySess.createMapMessage();

            message.setInt("code", 1);
            message.setString("name", name);

            System.out.println("Cree su contraseña");
            String password = scan.nextLine();
            message.setString("password", password);
            tempDest = mySess.createTemporaryQueue();
            this.authConsumer=mySess.createConsumer(tempDest);
            authConsumer.setMessageListener(this);
         message.setJMSReplyTo(tempDest);
            String correlationId=this.createRandomString();
            message.setJMSCorrelationID(correlationId);
            this.authProducer.send(message);
            Thread.sleep(300);
        } catch (Exception jmse) {
            System.out.println("Exception occurred : " + jmse.toString());
            jmse.printStackTrace();
        }

        //receive credentials
       if(flag2){
           return true;
       }
        System.out.println("--------MENÚ:El usuario ya existe-------");
        return false;
    }
    public static void main(String[] args) {
        boolean permitted = false;
        try {
             User user=null;
            Scanner scan = new Scanner(System.in);
           /* System.out.println("Introduzca 1 si quiere registrarse o 2 si ya tiene una cuenta");
            int opcion = Integer.parseInt(scan.nextLine());

            System.out.println("Inroduzca su nombre");
            String owner = scan.nextLine();

            User user = new User(owner);*/

            while (!permitted) {
                if (user!=null){
                     user.myConn.close();
                     user.mySess.close();
                 }
                String owner;
                System.out.println("Introduzca 1 si quiere registrarse o 2 si ya tiene una cuenta");
                 String opcion = scan.nextLine();
                  
                if (opcion.equals("1")) {
                   System.out.println("Introduzca su nombre");
                    owner = scan.nextLine();
                     user = new User(owner);
                    permitted = user.Register(owner);
                    Thread.sleep(100);
                } else if (opcion.equals("2")) {
                   System.out.println("Introduzca su nombre");
                    owner = scan.nextLine();
                     user = new User(owner);
                    permitted = user.logIn(owner);
                    
                }
                else{
                    System.out.println("Introduzca opción válida");
                }
            }

            

            while (true) {
                System.out.println("--------MENÚ:Introduzca 0 si quiere acceder a una sala, 1 si quiere crear sala, 2 si quiere borrarla--------");
                String code = "";
                code = scan.nextLine();

                if (code.equals("0")) {
                    TextMessage message = user.mySess.createTextMessage("getSalas");
                    user.managerProducer.send(message);
                    Thread.sleep(200);
                    if (!user.flag) {

                        System.out.println("--------MENÚ:Introduzca nombre de la sala a acceder--------");
                        String sala = scan.nextLine();
                        if (user.connectToRoom(sala)) {
                            String msg = "";
                            while (!msg.equals("exit")) {
                                msg = scan.nextLine();
                                user.sendToRoom(msg);

                            }
                            user.RoomConsumer.close();
                            user.RoomProducer.close();
                            user.RoomConsumer = null;
                            user.RoomProducer = null;
                            user.roomTopic = null;
                        }
                    } else {
                        
                        user.flag = false;

                    }

                } else {
                    if (code.equals("1") || code.equals("2")) {
                        TextMessage message = user.mySess.createTextMessage("getSalas");
                        user.managerProducer.send(message);
                        Thread.sleep(100);
                        System.out.println("--------MENÚ:Introduzca nombre de la sala--------");
                        
                        String room = scan.nextLine();

                        user.enviarSala(Integer.parseInt(code), room, user.user);
                    } else {
                        System.out.println("--------MENÚ:Introduzca opción correcta-------");
                    }

                }
            }
        } catch (Exception jmse) {
            System.out.println("Exception occurred : " + jmse.toString());
            jmse.printStackTrace();
        }
    }
}
