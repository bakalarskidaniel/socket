import java.io.IOException;
import static java.lang.System.in;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
 
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
 
/** 
 * @ServerEndpoint gives the relative name for the end point
 * This will be accessed via ws://localhost:8080/EchoChamber/echo
 * Where "localhost" is the address of the host,
 * "EchoChamber" is the name of the package
 * and "echo" is the address to access this class from the server
 */
//@ServerEndpoint("/echo") 
@ServerEndpoint(value = "/rooms/{roomnumber}")

public class EchoServer {
    
     String roomId;
    /**
     * @OnOpen allows us to intercept the creation of a new session.
     * The session class allows us to send data to the user.
     * In the method onOpen, we'll let the user know that the handshake was 
     * successful.
     */
    
    private static final Set<Session> sessions = new HashSet<>();
    
  
    
    public static void addSession(Session session){
        sessions.add(session);
    }
    
    public static void removeSession(Session session){
        sessions.remove(session);
    }
    
    public static void sendToSession(Session session, String msg){
        try {
            session.getBasicRemote().sendText(msg);
        } catch (IOException ex) {
            Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void sendToAllSessions(String msg){
        sessions.forEach((s) -> {
            EchoServer.sendToSession(s, msg);
        });
        
    }
    
    public static void sendToAllConnectionsInRoom(String roomId, String msg){
        sessions.forEach((s) -> {
            String roomNumber = s.getUserProperties().get("roomnumber").toString();
            if (roomId == null ? roomNumber == null : roomId.equals(roomNumber)){
                
            EchoServer.sendToSession(s, msg);
            }
            
        });
    }
    
    /**
     *
     * @param session
     * @param roomnumber
     */


    @OnOpen
    public void onOpen(Session session , @PathParam("roomnumber") final String roomnumber){
        EchoServer.addSession(session);
        System.out.println(session.getId() + " has opened a connection" + roomnumber);
        
        session.getUserProperties().put("roomnumber", roomnumber);
        
       
        roomId = session.getUserProperties().get("roomnumber").toString();
        
        try {
            session.getBasicRemote().sendText("Connection Established" + roomnumber);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
 
    /**
     * When a user sends a message to the server, this method will intercept the message
     * and allow us to react to it. For now the message is read as a String.
     */
    @OnMessage
    public void onMessage(String message, Session session){
        System.out.println("Message from " + session.getId() + ": " + message);
        String roomNumber = session.getUserProperties().get("roomnumber").toString();
        
       // EchoServer.sendToAllSessions(message);
        EchoServer.sendToAllConnectionsInRoom(roomNumber, message);
    }
 
    /**
     * The user closes the connection.
     * 
     * Note: you can't send messages to the client from this method
     */
    @OnClose
    public void onClose(Session session){
        EchoServer.removeSession(session);
        System.out.println("Session " +session.getId()+" has ended");
    }
}

