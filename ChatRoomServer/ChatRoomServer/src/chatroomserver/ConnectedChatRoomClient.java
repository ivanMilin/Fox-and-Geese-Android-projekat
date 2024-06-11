package chatroomserver;

import java.io.BufferedReader;
import java.io.IOException;
//import java.io.InputStream;
import java.io.InputStreamReader;
//import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
//import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectedChatRoomClient implements Runnable {

    //atributi koji se koriste za komunikaciju sa klijentom
    private Socket socket;
    private String userName;
    private BufferedReader br;
    private PrintWriter pw;
    private ArrayList<ConnectedChatRoomClient> allClients;
    
    ChatRoomServer parent;
    
    //getters and setters
    public String getUserName() {return userName;}
    //==============================================================================================
    public void setUserName(String userName) {this.userName = userName;}
    //==============================================================================================
    private void notifyClients() 
    {
        for (ConnectedChatRoomClient clnt : this.allClients) {
            clnt.pw.println("GameOver =");
            System.out.println("GameOver =");
        }
    }
    //==============================================================================================
    //Konstruktor klase, prima kao argument socket kao vezu sa uspostavljenim klijentom
    public ConnectedChatRoomClient(Socket socket, ArrayList<ConnectedChatRoomClient> allClients, ChatRoomServer parent) {
        this.socket = socket;
        this.allClients = allClients;
        this.parent = parent;

        //iz socket-a preuzmi InputStream i OutputStream
        try {
            //posto se salje tekst, napravi BufferedReader i PrintWriter
            //kojim ce se lakse primati/slati poruke (bolje nego da koristimo Input/Output stream
            this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
            this.pw = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()), true);
            //zasad ne znamo user name povezanog klijenta
            this.userName = "";
        } catch (IOException ex) {
            Logger.getLogger(ConnectedChatRoomClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //==============================================================================================
    /**
     * Metoda prolazi i pravi poruku sa trenutno povezanik korisnicima u formatu
     * Users: ImePrvog ImeDrugog ImeTreceg ... kada se napravi poruka tog
     * formata, ona se salje svim povezanim korisnicima
     */
    void connectedClientsUpdateStatus() {
        //priprema string sa trenutno povezanim korisnicima u formatu 
        //Users: Milan Dusan Petar
        //i posalji svim korisnicima koji se trenutno nalaze u chat room-u
        String connectedUsers = "Users:";
        for (ConnectedChatRoomClient c : this.allClients) {
            connectedUsers += " " + c.getUserName();
        }

        //prodji kroz sve klijente i svakom posalji info o novom stanju u sobi
        for (ConnectedChatRoomClient svimaUpdateCB : this.allClients) {
            svimaUpdateCB.pw.println(connectedUsers);
        }
        System.out.println(connectedUsers);
    }
    //==============================================================================================
    @Override
    public void run() {
        //Server prima od svakog korisnika najpre njegovo korisnicko ime
        //a kasnije poruke koje on salje ostalim korisnicima u chat room-u
        while (true) {
            try {
                //ako nije poslato ime, najpre cekamo na njega
                if (this.userName.equals("")) {
                    
                    
                    this.userName = this.br.readLine();
                    if (this.userName != null) {
                        System.out.println("Connected user: " + this.userName);
                        //informisi sve povezane klijente da imamo novog 
                        //clana u chat room-u
                        connectedClientsUpdateStatus();
                    } else {
                        System.out.println("Disconnected user: " + this.userName);
                        for (ConnectedChatRoomClient cl : this.allClients) {
                            if (cl.getUserName().equals(this.userName)) {
                                this.allClients.remove(cl);
                                break;
                            }
                        }
                        connectedClientsUpdateStatus();
                        break;
                    }
                } 
                else {
                    String line = this.br.readLine();
                    //System.out.println(line);
                    if (line != null) {
                        
                        if(line.startsWith("GameRequest ="))
                        {
                            String[] lineSplited = (line.trim()).split("=");
                            String[] names = lineSplited[1].split(":");
                            String[] forWhofromWho = names[0].split(",");
                            String forWho = forWhofromWho[0];

                            for (ConnectedChatRoomClient clnt : this.allClients) {
                                if (clnt.getUserName().equals(forWho)) 
                                {
                                    //prosledi poruku namenjenom korisniku
                                    clnt.pw.println(line);
                                    
                                } 
                            }
                        }
                        if(line.startsWith("ChallengeAccepted ="))
                        {
                            //ChallengeAccepted =jovan,ivan
                            String[] lineSplited = (line.trim()).split("=");
                            String[] names = lineSplited[1].split(":");
                            String[] forWhofromWho = names[0].split(",");
                            String forWho = forWhofromWho[0];
                            
                            for (ConnectedChatRoomClient clnt : this.allClients) {
                                if (clnt.getUserName().equals(forWho)) 
                                {
                                    clnt.pw.println(line);
                                } 
                            }
                            //System.out.println(matrixToString(boardMatrix));
                        }
                        //RemoveFigure =ivan#6,2,2
                        if(line.startsWith("RemoveFigure ="))
                        {
                            System.out.println(line);
                            
                            String[] lineSplited = (line.trim()).split("=");
                            String[] nameCoordinate = lineSplited[1].split("#");
                            String name = nameCoordinate[0];
                            
                            String[] coordinates = nameCoordinate[1].split(",");
                            int row = Integer.parseInt(coordinates[0]);
                            int col = Integer.parseInt(coordinates[1]);
                            int value = Integer.parseInt(coordinates[2]);
                            
                            for (ConnectedChatRoomClient clnt : this.allClients) 
                            {
                                if (clnt.getUserName().equals(name)) 
                                {
                                    clnt.pw.println(line);
                                    //System.out.println("Usao sam u proveru RemoveFigure");
                                } 
                            }
                            
                            parent.updateMatrix(row,col,0);
                            //System.out.println(matrixToString(boardMatrix));
                            //parent.printMatrix();
                        }
                        //UpdateTable =ivan#7,1,2
                        if(line.startsWith("UpdateTable ="))
                        {
                            System.out.println(line);
                            
                            String[] lineSplited = (line.trim()).split("=");
                            String[] nameCoordinate = lineSplited[1].split("#");
                            String name = nameCoordinate[0];

                            String[] coordinates = nameCoordinate[1].split(",");
                            int row = Integer.parseInt(coordinates[0]);
                            int col = Integer.parseInt(coordinates[1]);
                            int value = Integer.parseInt(coordinates[2]);
                            
                            String whoseTurn = nameCoordinate[2];
                          
                            //System.out.println("row:col:value:whoseTurn"+row+","+col+","+value+","+whoseTurn);
                            
                            for (ConnectedChatRoomClient clnt : this.allClients) {
                                if (clnt.getUserName().equals(name)) 
                                {
                                    clnt.pw.println(line);
                                } 
                            }
                            
                            parent.updateMatrix(row,col,value);
                            //System.out.println(matrixToString(boardMatrix));
                            parent.printMatrix();
                            
                            if((row == 0 && value == 2) || (row == 7 && value == 1))
                            {
                                notifyClients();
                                //System.out.println(matrixToString(boardMatrix));
                                parent.printMatrix();
                            }
                            
                            int [][] boardMatrixx = parent.getBoardMatrix();
                            
                            int rows = boardMatrixx.length;
                            int cols = boardMatrixx[0].length;

                            for (int roww = 0; roww < rows; roww++) 
                            {
                                if (boardMatrixx[roww][0] == 2) 
                                {
                                    if (roww > 0 && roww < rows - 1) 
                                    {
                                        if (boardMatrixx[roww - 1][1] == 1 && boardMatrixx[roww + 1][1] == 1) 
                                        {
                                            notifyClients();
                                            return;
                                        }
                                    }
                                }

                                if (boardMatrixx[roww][cols - 1] == 2) 
                                {
                                    if (roww > 0 && roww < rows - 1) 
                                    {
                                        if (boardMatrixx[roww - 1][cols - 2] == 1 && boardMatrixx[roww + 1][cols - 2] == 1) 
                                        {
                                            notifyClients();
                                            return;
                                        }
                                    }
                                }
                                
                                if (roww > 0 && roww < rows - 1) 
                                {  
                                    for (int coll = 1; coll < cols - 1; coll++) {
                                        if (boardMatrixx[roww][coll] == 2) 
                                        {
                                            if (boardMatrixx[roww - 1][coll - 1] == 1 && boardMatrixx[roww - 1][coll + 1] == 1 &&
                                                boardMatrixx[roww + 1][coll - 1] == 1 && boardMatrixx[roww + 1][coll + 1] == 1) {
                                                notifyClients();
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }                       
                        if(line.startsWith("RequestDenied ="))
                        {
                            String[] lineSplited = (line.trim()).split("=");
                            String messageForWho = lineSplited[1];
                            
                            for (ConnectedChatRoomClient clnt : this.allClients) 
                            {
                                if (clnt.getUserName().equals(messageForWho)) 
                                {
                                    //prosledi poruku namenjenom korisniku
                                    clnt.pw.println(line);
                                } 
                            }
                        }
                        if(line.startsWith("TerminateGame ="))
                        {
                            String[] lineSplited = (line.trim()).split("=");
                            String messageForWho = lineSplited[1];
                            
                            for (ConnectedChatRoomClient clnt : this.allClients) {
                                if (clnt.getUserName().equals(messageForWho)) 
                                {
                                    //prosledi poruku namenjenom korisniku
                                    clnt.pw.println(line);
                                } 
                            }
                        }
                        if(line.startsWith("RestartBoard ="))
                        {
                            parent.resetBoardMatrix();
                            //System.out.println(matrixToString(boardMatrix));
                            parent.printMatrix();
                        }
                    } else {
                        //slicno kao gore, ako je line null, klijent se diskonektovao
                        //ukloni tog korisnika iz liste povezanih korisnika u chat room-u
                        //i obavesti ostale da je korisnik napustio sobu
                        System.out.println("Disconnected user: " + this.userName);

                        //Ovako se uklanja element iz kolekcije 
                        //ne moze se prolaziti kroz kolekciju sa foreach a onda u 
                        //telu petlje uklanjati element iz te iste kolekcije
                        Iterator<ConnectedChatRoomClient> it = this.allClients.iterator();
                        while (it.hasNext()) {
                            if (it.next().getUserName().equals(this.userName)) {
                                it.remove();
                            }
                        }
                        connectedClientsUpdateStatus();

                        this.socket.close();
                        break;
                    }

                }
            } catch (IOException ex) {
                System.out.println("Disconnected user: " + this.userName);
                //npr, ovakvo uklanjanje moze dovesti do izuzetka, pogledajte kako je 
                //to gore uradjeno sa iteratorom
                for (ConnectedChatRoomClient cl : this.allClients) {
                    if (cl.getUserName().equals(this.userName)) {
                        this.allClients.remove(cl);
                        connectedClientsUpdateStatus();
                        return;
                    }
                }

            }

        }
    }

}