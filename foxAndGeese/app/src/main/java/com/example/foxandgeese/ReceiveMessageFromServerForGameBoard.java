package com.example.foxandgeese;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;

public class ReceiveMessageFromServerForGameBoard implements Runnable{

    GameBoard parent;
    BufferedReader br;
    String nameTurn;

    public ReceiveMessageFromServerForGameBoard(GameBoard parent) {
        this.parent = parent;
        this.br = parent.getBr();
        nameTurn = "";
    }

    @Override
    public void run()
    {
        while (true) {
            try {
                String line = this.br.readLine();
                System.out.println("-> ReceiveMessageFromServerForGameBoard <-" + line);

                parent.displayMessageFromReceiveMessageFromServer(line);
                if(line.startsWith("UpdateTable =")) {


                    String[] lineSplited = (line.trim()).split("=");
                    String[] nameCoordinate = lineSplited[1].split("#");
                    String name = nameCoordinate[0];
                    String[] coordinates = nameCoordinate[1].split(",");

                    int row = Integer.parseInt(coordinates[0]);
                    int col = Integer.parseInt(coordinates[1]);
                    int value = Integer.parseInt(coordinates[2]);

                    if(name.equals(parent.getMyUsername()))
                    {
                        parent.setTurnText(lineSplited[0]);
                        parent.updateCellBackground(row,col,value);
                    }

                }
                else if(line.startsWith("RemoveFigure =")) {
                    String[] lineSplited = (line.trim()).split("=");
                    String[] nameCoordinate = lineSplited[1].split("#");
                    String name = nameCoordinate[0];
                    String[] coordinates = nameCoordinate[1].split(",");

                    int row = Integer.parseInt(coordinates[0]);
                    int col = Integer.parseInt(coordinates[1]);
                    int value = Integer.parseInt(coordinates[2]);

                    if(name.equals(parent.getMyUsername()))
                    {
                        parent.setTurnText(lineSplited[0]);
                        parent.updateCellBackground(row,col,0);
                    }

                }
            }
            catch (IOException ex) {
                GameBoard.serverNotAvailable();

                parent.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //parent.displayMessageFromReceiveMessageFromServer("Nista nisam primio");
                    }
                });
            }
        }
    }

}
