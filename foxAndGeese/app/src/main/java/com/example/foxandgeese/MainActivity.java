package com.example.foxandgeese;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    Spinner spinner;
    Button button_play;
    Button button_connect;
    EditText et_username;
    EditText et_ipAddress;

    public static String USERNAME_EXTRA;
    public static String OPPONENT_EXTRA;

    String myOponent;


    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;

    AlertDialog.Builder builder;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //new Thread(new ReceiveMessageFromServer(MainActivity.this)).start();

        spinner = (Spinner) findViewById(R.id.spinner);
        button_play = (Button) findViewById(R.id.button_home);
        button_connect = (Button) findViewById(R.id.button_connect);
        et_username = (EditText) findViewById(R.id.et_username);
        et_ipAddress = (EditText) findViewById(R.id.et_ipAddress);
        builder = new AlertDialog.Builder(this);

        MainActivity.this.spinner.setEnabled(false);
        MainActivity.this.button_play.setEnabled(false);

        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = et_username.getText().toString().trim();
                if (!username.isEmpty() && !et_ipAddress.toString().isEmpty())
                {
                    connectToServer();
                    et_username.setEnabled(false);
                    et_ipAddress.setEnabled(false);
                    //new Thread(new ReceiveMessageFromServer(MainActivity.this)).start();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "You forgot to insert username or IpAddress!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //btnDialogAccept.setOnClickListener(new View.OnClickListener() {
        button_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(!et_username.getText().toString().equals(spinner.getSelectedItem().toString()))
                {
                    String porukaZaSlanje = "GameRequest =" + spinner.getSelectedItem().toString() + "," + et_username.getText().toString() + ":wants to play with you";
                    myOponent = spinner.getSelectedItem().toString();
                    //Toast.makeText(MainActivity.this, porukaZaSlanje, Toast.LENGTH_SHORT).show();
                    sendMessage(porukaZaSlanje);
                }
                else
                    Toast.makeText(MainActivity.this, "You can't play with yourself!", Toast.LENGTH_SHORT).show();

            }
        });
    }
    //==============================================================================================
    public void loginGUI() {
        Intent intent = new Intent(this, GameBoard.class);
        String myNamemyOponent = et_username.getText().toString().trim() +","+myOponent;

        System.out.println("+++++++++++++++++++++++++++++++++++++++++");
        System.out.println("MainActivity - "+myNamemyOponent);
        System.out.println("+++++++++++++++++++++++++++++++++++++++++");

        intent.putExtra(USERNAME_EXTRA, myNamemyOponent);
        startActivity(intent);
    }
    //==============================================================================================
    public void connectToServer() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Singleton singleton = Singleton.getInstance();
                singleton.setIpAddress(et_ipAddress.getText().toString().trim());
                if (singleton != null) {
                    MainActivity.this.socket = singleton.socket;
                    MainActivity.this.br = singleton.br;
                    MainActivity.this.pw = singleton.pw;

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.spinner.setEnabled(true);
                            MainActivity.this.button_play.setEnabled(true);
                            sendMessage(MainActivity.this.et_username.getText().toString());

                            new Thread(new ReceiveMessageFromServer(MainActivity.this)).start();
                        }
                    });
                }
                else
                {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Failed connection with server", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
    //==============================================================================================
    public void sendMessage(String message) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (MainActivity.this.pw != null) {
                    MainActivity.this.pw.println(message);
                    System.out.println("Message to server: " + message);
                }
            }
        }).start();
    }
    //==============================================================================================
    public static void serverNotAvailable() {
        System.out.println("Server is not available");
    }
    //==============================================================================================
    public static void serverAvailable() {
        System.out.println("Connected on server");
    }
    //==============================================================================================
    public void displayMessageFromReceiveMessageFromServer(String string)
    {
        Toast.makeText(this, "String from" + string, Toast.LENGTH_SHORT).show();
    }
    //==============================================================================================
    public PrintWriter getPw() {return pw;}
    //==============================================================================================
    public BufferedReader getBr() {
        return br;
    }
    //==============================================================================================
    public Spinner getSpinner() {
        return spinner;
    }
    //==============================================================================================
    public EditText getEt_username() {
        return et_username;
    }
    //==============================================================================================
    public void setMyOponent(String myOponent) {
        this.myOponent = myOponent;
    }
    //==============================================================================================

}