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

    public static String EXTRA_MY_USERNAME ;

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
        builder = new AlertDialog.Builder(this);

        MainActivity.this.spinner.setEnabled(false);
        MainActivity.this.button_play.setEnabled(false);

        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = et_username.getText().toString().trim();
                if (!username.isEmpty())
                {
                    connectToServer();
                    et_username.setEnabled(false);
                    //new Thread(new ReceiveMessageFromServer(MainActivity.this)).start();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "You forgot to insert username!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //btnDialogAccept.setOnClickListener(new View.OnClickListener() {
        button_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sendMessage("MRS MAJMUNEEE");

                builder.setTitle("Game request")
                        .setMessage("Player ________ wants to play with you")
                        .setCancelable(false)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loginGUI(v);
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
                //loginGUI(v);
                //create new thread to react on server's messages
                //new Thread(new ReceiveMessageFromServer(MainActivity.this)).start();
            }
        });
    }
    //==============================================================================================
    public void loginGUI(View view) {
        Intent intent = new Intent(this, GameBoard.class);
        String m = et_username.getText().toString().trim();
        intent.putExtra(EXTRA_MY_USERNAME, m);
        startActivity(intent);
    }
    //==============================================================================================
    public void connectToServer() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Singleton singleton = Singleton.getInstance();
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
    public BufferedReader getBr() {
        return br;
    }

    public Spinner getSpinner() {
        return spinner;
    }
}