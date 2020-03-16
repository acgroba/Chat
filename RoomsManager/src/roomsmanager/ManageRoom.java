
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roomsmanager;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author 1511 IRON
 */
public class ManageRoom {
    private final MessageSender messageSender;
    Map<String, Room> rooms = new HashMap<>();

    public ManageRoom(MessageSender messageSender) {
       this.messageSender = messageSender;
    }

    public int handleRequest(int code, String room, String owner) {
        switch (code) {
            case 1:
                return createRoom(room, owner);
            case 2:
                return deleteRoom(room, owner);
            default:
                return -1;
        }
    }

    public int createRoom(String name, String owner) {
        if (!rooms.containsKey(name)) {
            rooms.put(name, new Room(name, owner, messageSender));
             System.out.println("Creada sala "+name+" por "+ owner);
            return 0;
        }
        System.out.println("La sala ya existe");
        return 1;
    }

    public int deleteRoom(String name, String owner) {
        if (!rooms.containsKey(name)) {
            System.out.println("La sala no existe");
            return 2;
        } else {
            if (!rooms.get(name).getOwner().equals(owner)) {
                System.out.println("El usuario  no es el propietario de la sala");
                return 3;
            } else {
                rooms.remove(name);
                System.out.println("Borrada sala " + name + " por " + owner);
                return 0;
            }
        }
    }

    
    public Map<String, Room> getRooms(){
        return rooms;

    }
}