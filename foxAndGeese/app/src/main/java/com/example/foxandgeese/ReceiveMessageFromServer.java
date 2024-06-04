package com.example.foxandgeese;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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

                    if(forWho.equals(parent.getEt_username().getText().toString()))
                    {
                        System.out.println("Usao sam u proveru if-a");
                        parent.loginGUI();
                    }
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
