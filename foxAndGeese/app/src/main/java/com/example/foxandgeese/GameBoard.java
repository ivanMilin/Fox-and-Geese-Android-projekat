package com.example.foxandgeese;

import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GameBoard extends AppCompatActivity {

    private static final int BOARD_SIZE = 8;
    private TextView turnText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.game_board);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TableLayout tableLayout = findViewById(R.id.chessBoard);
        turnText = findViewById(R.id.turnText);

        // Get the screen width
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        // Calculate the size of each cell based on the screen width
        int cellSize = screenWidth / BOARD_SIZE;

        for (int row = 0; row < BOARD_SIZE; row++) {
            TableRow tableRow = new TableRow(this);
            for (int col = 0; col < BOARD_SIZE; col++) {
                TextView cell = new TextView(this);
                TableRow.LayoutParams params = new TableRow.LayoutParams(cellSize, cellSize);
                cell.setLayoutParams(params);

                if ((row + col) % 2 == 0) {
                    // White cell background
                    cell.setBackgroundResource(R.drawable.white_cell);

                    // Add red circles to white cells in the first row
                    if (row == 0) {
                        cell.setBackgroundResource(R.drawable.red_circle_cell);
                    }

                    // Add a blue circle to one of the white cells in the last row
                    if (row == BOARD_SIZE - 1 && col == 1)/*(col = 1 || col = 3 || col = 5)*/ {
                        cell.setBackgroundResource(R.drawable.blue_circle_cell);
                    }
                } else {
                    // Black cell background
                    cell.setBackgroundResource(R.drawable.black_cell);
                }

                cell.setTag(row + "," + col);
                cell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String position = (String) v.getTag();
                        Toast.makeText(GameBoard.this, "Cell pressed: " + position, Toast.LENGTH_SHORT).show();
                    }
                });

                tableRow.addView(cell);
            }
            tableLayout.addView(tableRow);
        }

        // Example: Show "It's my turn" text
        showTurnText();

        // Set button click listeners
        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(GameBoard.this, "Button 1 clicked", Toast.LENGTH_SHORT).show();
                turnText.setText("Button 1 clicked");
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(GameBoard.this, "Button 2 clicked", Toast.LENGTH_SHORT).show();
                turnText.setText("Button 2 clicked");
            }
        });
    }

    private void showTurnText() {
        turnText.setVisibility(View.VISIBLE);
    }

    private void hideTurnText() {
        turnText.setVisibility(View.GONE);
    }
}