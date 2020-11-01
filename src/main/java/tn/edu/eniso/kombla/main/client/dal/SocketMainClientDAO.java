/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tn.edu.eniso.kombla.main.client.dal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.gaming.atom.model.DefaultPlayer;
import net.vpc.gaming.atom.model.DefaultSprite;
import net.vpc.gaming.atom.model.ModelPoint;
import net.vpc.gaming.atom.model.Player;
import net.vpc.gaming.atom.model.Sprite;
import tn.edu.eniso.kombla.main.shared.dal.ProtocolConstants;
import tn.edu.eniso.kombla.main.shared.model.DynamicGameModel;
import tn.edu.eniso.kombla.main.shared.model.StartGameInfo;

/**
 *
 * @author nourhene
 */
public class SocketMainClientDAO implements MainClientDAO {

    Socket s;
    String serverAddress;
    int serverPort;
    MainClientDAOListener listener;
    DataOutputStream out;
    DataInputStream in;

    @Override
    public void start(MainClientDAOListener listener, Map<String, Object> properties) {
        this.listener = listener;
        this.serverAddress = properties.get("serverAddress").toString();
        this.serverPort = (int) properties.get("serverPort");

        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void onLoopRecieveModelChanged() {
        new Thread(() -> {
            try {
                int listPlayerSize, listSpriteSize;
                List<Sprite> listSprite = new ArrayList<Sprite>();
                List<Player> listPlayer = new ArrayList<Player>();
                long frame;
                DynamicGameModel dgame = new DynamicGameModel();
                frame = in.readLong();      //Lire la valeur de frame
                //Lire le nombre de Players
                listPlayerSize = in.readInt();
                //Récupérer les joueurs
                for (int i = 0; i < listPlayerSize; i++) {
                    listPlayer.add(readPlayer());
                }
                listSpriteSize = in.readInt();      //Lire le nombre de Sprites
                //Récupérer les Sprites
                for (int i = 0; i < listSpriteSize; i++) {
                    listSprite.add(readSprite());
                }
                //Sauvegarder les données dans le modèle 
                dgame.setFrame(frame);
                dgame.setPlayers(listPlayer);
                dgame.setSprites(listSprite);
                //Transférer les données au métier
                listener.onModelChanged(dgame);
            } catch (IOException ex) {
                Logger.getLogger(SocketMainClientDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();
    }

    public StartGameInfo readStartGameInfo() {
        StartGameInfo gameInfo = null;
        try {
            int playerId;
            int mazeSize;
            playerId = in.readInt();
            mazeSize = in.readInt();
            int[][] maze = new int[mazeSize][mazeSize];
            for (int i = 0; i < mazeSize; i++) {
                for (int j = 0; j < mazeSize; j++) {

                    maze[i][j] = in.readInt();

                }

            }
            gameInfo = new StartGameInfo(playerId, maze);

        } catch (IOException ex) {
            Logger.getLogger(SocketMainClientDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return gameInfo;
    }

    @Override
    public StartGameInfo connect() {
        StartGameInfo gameInfo;
        try {
            s = new Socket(serverAddress, serverPort);

            out = new DataOutputStream(s.getOutputStream());
            out.write(ProtocolConstants.CONNECT);
            out.writeUTF("player1");

            in = new DataInputStream(s.getInputStream());
            int read = in.readInt();
            if (read == ProtocolConstants.OK) {
                this.onLoopRecieveModelChanged();
                gameInfo = this.readStartGameInfo();

            } else if (in.readInt() == ProtocolConstants.KO) {

            }

        } catch (IOException ex) {
            Logger.getLogger(SocketMainClientDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

    }

    public DefaultPlayer readPlayer() {
        DefaultPlayer defaultPlayer = null;
        int mapSize;
        try {
            defaultPlayer = new DefaultPlayer();
            defaultPlayer.setId(in.readInt());
            defaultPlayer.setName(in.readUTF());
            mapSize = in.readInt();
            for (int i = 0; i < mapSize; i++) {
                defaultPlayer.setProperty(in.readUTF(), in.readUTF());

            }

        } catch (IOException ex) {
            Logger.getLogger(SocketMainClientDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return defaultPlayer;
    }

    public DefaultSprite readSprite() {
        DefaultSprite defaultSprite = null;
        int mapSize;
        try {
            defaultSprite = new DefaultSprite();
            defaultSprite.setId(in.readInt());
            defaultSprite.setKind(serverAddress);
            defaultSprite.setName(in.readUTF());
            defaultSprite.setLocation(new ModelPoint(in.readDouble(), in.readDouble()));
            defaultSprite.setDirection(in.readDouble());
            defaultSprite.setPlayerId(in.readInt());
            mapSize = in.readInt();
            for (int i = 0; i < mapSize; i++) {
                defaultSprite.setProperty(in.readUTF(), in.readUTF());

            }

        } catch (IOException ex) {
            Logger.getLogger(SocketMainClientDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return defaultSprite;
    }

    @Override
    public void sendMoveLeft() {
        try {
            out.write(ProtocolConstants.LEFT);
        } catch (IOException ex) {
            Logger.getLogger(SocketMainClientDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void sendMoveRight() {
        try {
            out.write(ProtocolConstants.RIGHT);
        } catch (IOException ex) {
            Logger.getLogger(SocketMainClientDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void sendMoveUp() {
        try {
            out.write(ProtocolConstants.UP);
        } catch (IOException ex) {
            Logger.getLogger(SocketMainClientDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void sendMoveDown() {
        try {
            out.write(ProtocolConstants.DOWN);
        } catch (IOException ex) {
            Logger.getLogger(SocketMainClientDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void sendFire() {
        try {
            out.write(ProtocolConstants.FIRE);
        } catch (IOException ex) {
            Logger.getLogger(SocketMainClientDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
