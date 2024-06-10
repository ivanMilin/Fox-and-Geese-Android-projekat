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
    
    private int[][] boardMatrix = {
            {1, 3, 1, 3, 1, 3, 1, 3},
            {3, 0, 3, 0, 3, 0, 3, 0},
            {0, 3, 0, 3, 0, 3, 0, 3},
            {3, 0, 3, 0, 3, 0, 3, 0},
            {0, 3, 0, 3, 0, 3, 0, 3},
            {3, 0, 3, 0, 3, 0, 3, 0},
            {0, 3, 0, 3, 0, 3, 0, 3},
            {3, 2, 3, 0, 3, 0, 3, 0}
    };
    
    private int[][] startAgainMatrix = {
            {1, 3, 1, 3, 1, 3, 1, 3},
            {3, 0, 3, 0, 3, 0, 3, 0},
            {0, 3, 0, 3, 0, 3, 0, 3},
            {3, 0, 3, 0, 3, 0, 3, 0},
            {0, 3, 0, 3, 0, 3, 0, 3},
            {3, 0, 3, 0, 3, 0, 3, 0},
            {0, 3, 0, 3, 0, 3, 0, 3},
            {3, 0, 3, 2, 3, 0, 3, 0}
    };

    //getters and setters
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public int[][] getBoardMatrix() {
        return boardMatrix;
    }
    
    public void updateMatrix(int row, int col, int value)
    {
        int[][] matrix = getBoardMatrix();

        if (row >= 0 && row < matrix.length && col >= 0 && col < matrix[0].length)
        {
            matrix[row][col] = value;
        } else {
            throw new IndexOutOfBoundsException("Row or column index out of bounds");
        }
    }

    //Konstruktor klase, prima kao argument socket kao vezu sa uspostavljenim klijentom
    public ConnectedChatRoomClient(Socket socket, ArrayList<ConnectedChatRoomClient> allClients) {
        this.socket = socket;
        this.allClients = allClients;

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
                        //ako je userName null to znaci da je terminiran klijent thread
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
                    ////////CEKAMO PORUKU/////////
                } 
                else {
                    //vec nam je korisnik poslao korisnicko ime, poruka koja je 
                    //stigla je za nekog drugog korisnika iz chat room-a (npr Milana) u 
                    //formatu Milan: Cao Milane, kako si?
                    //System.out.println("cekam poruku");
                    String line = this.br.readLine();
                    System.out.println(line);
                    //System.out.println("stigla poruka");
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
                        else if(line.startsWith("ChallengeAccepted ="))
                        {
                            //ChallengeAccepted =jovan,ivan
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
                        //RemoveFigure =ivan#6,2,2
                        //UpdateTable =ivan#7,1,2
                        else if(line.startsWith("UpdateTable ="))
                        {
                            
                            String[] lineSplited = (line.trim()).split("=");
                            String[] nameCoordinate = lineSplited[1].split("#");
                            String name = nameCoordinate[0];

                            String[] coordinates = nameCoordinate[1].split(",");
                            int row = Integer.parseInt(coordinates[0]);
                            int col = Integer.parseInt(coordinates[1]);
                            int value = Integer.parseInt(coordinates[2]);
                            
                            String whoseTurn = nameCoordinate[2];
                          
                            System.out.println("row:col:value:whoseTurn"+row+","+col+","+value+","+whoseTurn);
                            
                            if((row == 0 && value == 2) || (row == 7 && value == 1))
                            {
                                for (ConnectedChatRoomClient clnt : this.allClients) {
                                    //prosledi poruku namenjenom korisniku
                                    clnt.pw.println("GameOver =");
                                    System.out.println("GameOver =");
                                }
                            }
                            
                            for (ConnectedChatRoomClient clnt : this.allClients) {
                                if (clnt.getUserName().equals(name)) 
                                {
                                    //prosledi poruku namenjenom korisniku
                                    clnt.pw.println(line);
                                    System.out.println("Usao sam u proveru UpdateTable");
                                } 
                            }
                            
                        }
                        else if(line.startsWith("RemoveFigure ="))
                        {
                            
                            String[] lineSplited = (line.trim()).split("=");
                            String[] nameCoordinate = lineSplited[1].split("#");
                            String name = nameCoordinate[0];
                            
                            for (ConnectedChatRoomClient clnt : this.allClients) {
                                if (clnt.getUserName().equals(name)) 
                                {
                                    //prosledi poruku namenjenom korisniku
                                    clnt.pw.println(line);
                                    System.out.println("Usao sam u proveru RemoveFigure");
                                } 
                            }
                        }
                        else if(line.startsWith("RequestDenied ="))
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
                        else if(line.startsWith("TerminateGame ="))
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