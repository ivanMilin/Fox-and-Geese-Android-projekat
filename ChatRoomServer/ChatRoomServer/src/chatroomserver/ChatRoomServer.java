package chatroomserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatRoomServer {
    
    private int[][] boardMatrix = 
    {
        {1, 3, 1, 3, 1, 3, 1, 3},
        {3, 0, 3, 0, 3, 0, 3, 0},
        {0, 3, 0, 3, 0, 3, 0, 3},
        {3, 0, 3, 0, 3, 0, 3, 0},
        {0, 3, 0, 3, 0, 3, 0, 3},
        {3, 0, 3, 0, 3, 0, 3, 0},
        {0, 3, 0, 3, 0, 3, 0, 3},
        {3, 2, 3, 0, 3, 0, 3, 0}
    };
    
    private int[][] restartMatrix = 
    {
            {1, 3, 1, 3, 1, 3, 1, 3},
            {3, 0, 3, 0, 3, 0, 3, 0},
            {0, 3, 0, 3, 0, 3, 0, 3},
            {3, 0, 3, 0, 3, 0, 3, 0},
            {0, 3, 0, 3, 0, 3, 0, 3},
            {3, 0, 3, 0, 3, 0, 3, 0},
            {0, 3, 0, 3, 0, 3, 0, 3},
            {3, 0, 3, 2, 3, 0, 3, 0}
    };

    private ServerSocket ssocket;
    private int port;
    private ArrayList<ConnectedChatRoomClient> clients;

    public ServerSocket getSsocket() {
        return ssocket;
    }

    public void setSsocket(ServerSocket ssocket) {
        this.ssocket = ssocket;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    //==============================================================================================
    public int[][] getBoardMatrix() {return boardMatrix;}
    //==============================================================================================
    public void updateMatrix(int row, int col, int value) 
    {
        if (row >= 0 && row < boardMatrix.length && col >= 0 && col < boardMatrix[0].length) 
        {
            boardMatrix[row][col] = value;
        } 
        else 
        {
            System.out.println("Invalid row or column index.");
        }
    }
    //==============================================================================================
    private String matrixToString(int[][] matrix) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : matrix) {
            sb.append(Arrays.toString(row)).append("\n");
        }
        return sb.toString();
    }
    //==============================================================================================
    public void printMatrix() 
    {
        for (int[] row : boardMatrix) 
        {
            for (int cell : row) 
            {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }
    //==============================================================================================
    public void resetBoardMatrix() {
        for (int i = 0; i < restartMatrix.length; i++) 
        {
            for (int j = 0; j < restartMatrix[i].length; j++) 
            {
                boardMatrix[i][j] = restartMatrix[i][j];
            }
        }
    }
    //==============================================================================================
    /**
     * Prihvata u petlji klijente i za svakog novog klijenta kreira novu nit. Iz
     * petlje se moze izaci tako sto se na tastaturi otkuca Exit.
     */
    public void acceptClients() {
        Socket client = null;
        Thread thr;
        while (true) {
            try {
                System.out.println("Waiting for new clients..");
                client = this.ssocket.accept();
            } catch (IOException ex) {
                Logger.getLogger(ChatRoomServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (client != null) {
                //Povezao se novi klijent, kreiraj objekat klase ConnectedChatRoomClient
                //koji ce biti zaduzen za komunikaciju sa njim
                ConnectedChatRoomClient clnt = new ConnectedChatRoomClient(client, clients, this);
                //i dodaj ga na listu povezanih klijenata jer ce ti trebati kasnije
                clients.add(clnt);
                //kreiraj novu nit (konstruktoru prosledi klasu koja implementira Runnable interfejs)
                thr = new Thread(clnt);
                //..i startuj ga
                thr.start();
            } else {
                break;
            }
        }
    }

    public ChatRoomServer(int port) {
        this.clients = new ArrayList<>();
        try {
            this.port = port;
            this.ssocket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(ChatRoomServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        ChatRoomServer server = new ChatRoomServer(6001);

        System.out.println("Server pokrenut, slusam na portu 6001");

        //Prihvataj klijente u beskonacnoj petlji
        server.acceptClients();

    }

}
