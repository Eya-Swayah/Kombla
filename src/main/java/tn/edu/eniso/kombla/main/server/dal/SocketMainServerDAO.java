/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tn.edu.eniso.kombla.main.server.dal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.gaming.atom.model.DefaultPlayer;
import net.vpc.gaming.atom.model.DefaultSprite;
import net.vpc.gaming.atom.model.ModelPoint;
import net.vpc.gaming.atom.model.Player;
import net.vpc.gaming.atom.model.Sprite;
import tn.edu.eniso.kombla.main.client.dal.MainClientDAOListener;
import tn.edu.eniso.kombla.main.shared.dal.ProtocolConstants;
import tn.edu.eniso.kombla.main.shared.model.DynamicGameModel;
import tn.edu.eniso.kombla.main.shared.model.StartGameInfo;

/**
 *
 * @author eya
 */
public class SocketMainServerDAO implements MainServerDAO {
    
       ServerSocket server;
    //String serverAddress;
    int serverPort;
    MainServerDAOListener listener;
    DataOutputStream out;
    DataInputStream in;
    
public void start(MainServerDAOListener listener, Map<String, Object> properties) {
        this.listener = listener;
        //this.serverAddress = properties.get("serverAddress").toString();
        this.serverPort = (int) properties.get("serverPort");
  
           try {
             ServerSocket  server = new ServerSocket(serverPort);
           } catch (IOException ex) {
               Logger.getLogger(SocketMainServerDAO.class.getName()).log(Level.SEVERE, null, ex);
           }
new Thread(()->{ 
   
while (true ){

            try {
                Socket s= server.accept() ;
           new Thread (()->{
                    try {
                        processClient(new ClientSession(-1,s,null,null));
                    } catch (IOException ex) {
                        Logger.getLogger(SocketMainServerDAO.class.getName()).log(Level.SEVERE, null, ex);
                    }
           
           }).start();

            } catch (IOException ex) {
                Logger.getLogger(SocketMainServerDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
}
//public void  processClient (ClientSession){}

}).start(); 
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void sendModelChanged(DynamicGameModel dynamicGameModel) {
        for (Map.Entry<Integer, ClientSession> entry : playerToSocketMap.entrySet()) { //envoyer le nouveau modele à tous les clients
Integer playerId = entry.getKey(); //playerId: clé du playerToSocketMap
DataOutputStream out = entry.getValue().out;
        
            try { 
                out.writeInt((int) dynamicGameModel.getFrame());
            } catch (IOException ex) {
                Logger.getLogger(SocketMainServerDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
            
         for (Sprite sprite : dynamicGameModel.getSprites()) {
         
    try {
        writeSprite(sprite,out);
    } catch (IOException ex) {
        Logger.getLogger(SocketMainServerDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
         }
         for (Player p : dynamicGameModel.getPlayers()) {
         writePlayer((DefaultPlayer) p,out) ;
         
         }
         
        
        }
        //dynamicGameModel.getSprites().
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    private Map<Integer, ClientSession> playerToSocketMap = new HashMap<>() ;

    private void processClient(ClientSession clientSession) throws IOException {
        clientSession.out= clientSession.out= new DataOutputStream(clientSession.socket.getOutputStream());
         clientSession.in=  new DataInputStream(clientSession.socket.getInputStream());
        int commande=clientSession.in.readInt();
        if (commande==ProtocolConstants.CONNECT){
        String name=clientSession.in.readUTF();
        StartGameInfo startGameInfo=listener.onReceivePlayerJoined(name);
        //writeStartGameInfo(startGameInfo,clientSession);
        playerToSocketMap.put(clientSession.idPlayer, clientSession);
        
        
        }
        else{
            
            clientSession.out.writeInt(ProtocolConstants.KO);
        }
        
      while(true){
       int position = clientSession.in.readInt() ;
      
      switch(position) {
          case ProtocolConstants.LEFT :
              listener.onReceiveMoveLeft(clientSession.idPlayer);
              break ; 
          case ProtocolConstants.RIGHT :
              listener.onReceiveMoveRight(clientSession.idPlayer);
              break ; 
          case ProtocolConstants.DOWN :
              listener.onReceiveMoveDown(clientSession.idPlayer);
              break ;
      
      }
      
      
      } 
      //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void writeStartGameInfo(StartGameInfo gameInfo, ClientSession cs) throws IOException{
        int playerId = gameInfo.getPlayerId();
        int[][] maze  = gameInfo.getMaze() ;
        
        cs.out.write(ProtocolConstants.OK);
        cs.out.writeInt(playerId);
        cs.out.writeInt(maze.length);
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze.length; j++) {          
                cs.out.write(maze[i][j]);                
            }           
        }
    }
    
    
   public void writePlayer(DefaultPlayer player, DataOutputStream out) {
        try {
            
            
            out.writeInt(player.getId());
            out.writeUTF(player.getName());
            out.writeInt(player.getProperties().size());
            
            for (Map.Entry<String, Object> entry : player.getProperties().entrySet())
            {
                out.writeUTF(entry.getKey());
                out.writeUTF((String) entry.getValue());
            }
                
                
                } catch (IOException ex) {
            Logger.getLogger(SocketMainServerDAO.class.getName()).log(Level.SEVERE, null, ex);
                }

    }
     public void writeSprite(Sprite sprite,DataOutputStream out) throws IOException{
        out.writeInt(sprite.getId());
        out.writeUTF( sprite.getKind());
        out.writeUTF(sprite.getName());
        out.writeInt(sprite.getLocation().getIntX());
        out.writeInt(sprite.getLocation().getIntY());
        out.writeDouble(sprite.getDirection());
        for (Map.Entry<String, Object> entry : sprite.getProperties().entrySet())
        {
             out.writeUTF(entry.getKey());
             out.writeUTF((String) entry.getValue());
        }

    }

   

   private class  ClientSession {
       int idPlayer;
       Socket socket ;
       DataInputStream in;
       DataOutputStream out;

      

        public ClientSession(int idPlayer, Socket socket, DataInputStream in, DataOutputStream out) {
            this.idPlayer = idPlayer;
            this.socket = socket;
            this.in = in;
            this.out = out;
        }
       
        
    }
   
   
}
