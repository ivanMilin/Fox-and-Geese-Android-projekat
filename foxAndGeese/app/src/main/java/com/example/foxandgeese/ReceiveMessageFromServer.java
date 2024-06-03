package com.example.foxandgeese;

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
                        }
                    });
                }
            }
            catch (IOException ex) {
                MainActivity.serverNotAvailable();

                parent.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parent.displayMessageFromReceiveMessageFromServer("Nista nisam primio");
                    }
                });
            }
        }
    }

}
