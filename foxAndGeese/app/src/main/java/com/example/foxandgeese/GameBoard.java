package com.example.foxandgeese;

import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.GridLayout;
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

        TableLayout tableLayout = findViewById(R.id.main);

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
                cell.setBackgroundColor((row + col) % 2 == 0 ? Color.WHITE : Color.BLACK);
                cell.setTag(row + "," + col);
                cell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String position = (String) v.getTag();
                        Toast.makeText(GameBoard.this, "Cell pressed: " + position, Toast.LENGTH_SHORT).show();
                    }
                });

                // Set padding and text size for better appearance
                cell.setPadding(8, 8, 8, 8);
                cell.setTextSize(18);
                cell.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                tableRow.addView(cell);
            }
            tableLayout.addView(tableRow);
        }
    }
}