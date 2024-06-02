package com.example.foxandgeese;

import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Singleton {
    // Static variable reference of single_instance
    // of type Singleton
    private static Singleton single_instance = null;

    public Socket socket;
    public BufferedReader br;
    public PrintWriter pw;

    // Private constructor
    // restricted to this class itself
    private Singleton() throws IOException {
        //loopback address is 10.0.2.2 for Android
        this.socket = new Socket("10.0.2.2", 6001);
        this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.pw = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()), true);
        if (this.socket == null) {
            //Toast.makeText(this, "Server is not available !", Toast.LENGTH_SHORT).show();
            this.single_instance = null;
        }
        else {
            //Toast.makeText(this, "Server is available !", Toast.LENGTH_SHORT).show();
        }
    }

    // Static method to create instance of Singleton class
    public static synchronized Singleton getInstance()
    {
        try{
            if (single_instance == null)
                single_instance = new Singleton();
        } catch (IOException e) {
            //Toast.makeText(this, "Server is not available !", Toast.LENGTH_SHORT).show();
            single_instance = null;
        }

        return single_instance;
    }
}
