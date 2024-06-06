package com.example.foxandgeese;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.IOException;

public class ReceiveMessageFromServer implements Runnable{

    MainActivity parent;
    BufferedReader br;
    String nameTurn;

    public ReceiveMessageFromServer(MainActivity parent) {
        this.parent = parent;
        this.br = parent.getBr();
        nameTurn = "";
    }

    String forWho_gameover;
    String fromWho_gameover;

    @Override
    public void run()
    {
        while (true) {
            try {
                String line = this.br.readLine();

                if (line.startsWith("Users:")) {
                    String[] names = line.split(":")[1].trim().split(" ");
                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {
                            parent.displayMessageFromReceiveMessageFromServer(line);
                            parent.getSpinner().setAdapter(null);
                            Spinner spinner = parent.getSpinner();
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(parent, android.R.layout.simple_spinner_dropdown_item, names);
                            spinner.setAdapter(adapter);
                        }
                    });
                }
                //GameRequest =jovan,ivan: wants to play with you
                else if(line.startsWith("GameRequest ="))
                {
                    System.out.println(line);
                    String[] lineSplited = (line.trim()).split("=");
                    String[] names = lineSplited[1].split(":");
                    String[] forWhofromWho = names[0].split(",");
                    String forWho = forWhofromWho[0];
                    String fromWho = forWhofromWho[1];
                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(forWho.equals(parent.getEt_username().getText().toString()))
                            {
                                System.out.println("PROVERA DA LI SAM USAO OVDE");
                                AlertDialog.Builder builder = new AlertDialog.Builder(parent);

                                builder.setTitle("Game request")
                                        .setMessage("Player " + fromWho  + " wants to play with you")
                                        .setCancelable(false)
                                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                parent.sendMessage("ChallengeAccepted ="+fromWho+","+forWho);
                                                parent.loginGUI();
                                                dialog.cancel();
                                            }
                                        })
                                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        })
                                        .show();
                            }
                        }
                    });
                }
                else if(line.startsWith("ChallengeAccepted ="))
                {
                    System.out.println("usao sam u 'line.startsWith(GameRequest =)'");
                    String[] lineSplited = (line.trim()).split("=");
                    String[] names = lineSplited[1].split(":");
                    String[] forWhofromWho = names[0].split(",");
                    String forWho = forWhofromWho[0];
                    String fromWho = forWhofromWho[1];
                    forWho_gameover = forWho;
                    fromWho_gameover = forWho;

                    if(forWho.equals(parent.getEt_username().getText().toString()))
                    {
                        System.out.println("Usao sam u proveru if-a");
                        parent.loginGUI();
                    }
                }
                else if(line.startsWith("UpdateTable =")) {
                    String[] lineSplited = (line.trim()).split("=");
                    String[] nameCoordinate = lineSplited[1].split("#");
                    String name = nameCoordinate[0];
                    String[] coordinates = nameCoordinate[1].split(",");

                    int row = Integer.parseInt(coordinates[0]);
                    int col = Integer.parseInt(coordinates[1]);
                    int value = Integer.parseInt(coordinates[2]);

                    if (name.equals(parent.getEt_username().getText().toString())) {
                        Intent intent = new Intent("UPDATE_CELL");
                        intent.putExtra("row", row);
                        intent.putExtra("col", col);
                        intent.putExtra("value", value);
                        LocalBroadcastManager.getInstance(parent).sendBroadcast(intent);
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


                    if (name.equals(parent.getEt_username().getText().toString())) {
                        Intent intent = new Intent("UPDATE_CELL");
                        intent.putExtra("row", row);
                        intent.putExtra("col", col);
                        intent.putExtra("value", 0);
                        LocalBroadcastManager.getInstance(parent).sendBroadcast(intent);
                    }
                }
                else if(line.startsWith("GameOver ="))
                {
                    System.out.println(line);
                    Intent intent = new Intent("GAME_OVER");
                    intent.putExtra("forWho_gameover", forWho_gameover);
                    intent.putExtra("fromWho_gameover", fromWho_gameover);
                    LocalBroadcastManager.getInstance(parent).sendBroadcast(intent);
                }

            }
            catch (IOException ex) {
                MainActivity.serverNotAvailable();

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
