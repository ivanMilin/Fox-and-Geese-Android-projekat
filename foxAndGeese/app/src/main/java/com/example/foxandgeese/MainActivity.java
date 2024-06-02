package com.example.foxandgeese;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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
    EditText et_username;

    private Socket socket;

    private BufferedReader br;
    private PrintWriter pw;

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

        spinner = (Spinner) findViewById(R.id.spinner);
        button_play = (Button) findViewById(R.id.button_play);
        et_username = (EditText) findViewById(R.id.et_username);
    }

    public void loginGUI( View view)
    {
        Intent intent = new Intent(this, GameBoard.class);
        startActivity(intent);
    }

    /*
    public void connectToServer(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                Singleton singleton = Singleton.getInstance();
                if (singleton != null){
                    MainActivity.this.socket = singleton.socket;
                    MainActivity.this.br = singleton.br;
                    MainActivity.this.pw = singleton.pw;

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.spinner.setActivated(true);
                            MainActivity.this.button_play.setActivated(true);
                        }
                    });
                }
            }
        }).start();
    }

    public void sendMessage(String message){

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (MainActivity.this.pw != null){
                    MainActivity.this.pw.println(message);
                    System.out.println("Message to server: " + message);
                }
            }
        }).start();
    }
    */
}