package com.example.foxandgeese;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class GameBoard extends AppCompatActivity {

    private static final int BOARD_SIZE = 8;
    private TextView turnText;
    private int[][] boardMatrix = {
            {1, 3, 1, 3, 1, 3, 1, 3},
            {3, 0, 3, 0, 3, 0, 3, 0},
            {0, 3, 0, 3, 0, 3, 0, 3},
            {3, 0, 3, 0, 3, 0, 3, 0},
            {0, 3, 0, 3, 0, 3, 0, 3},
            {3, 0, 3, 0, 3, 0, 3, 0},
            {0, 3, 0, 3, 0, 3, 0, 3},
            {3, 0, 3, 0, 3, 0, 3, 0}
    };
    private View selectedCell = null;
    private int selectedRow = -1;
    private int selectedCol = -1;

    private boolean disabledBoard = false;

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

        placeBlueCircleRandomly();

        TableLayout tableLayout = findViewById(R.id.gameBoard);
        turnText = findViewById(R.id.turnText);

        // Get the screen width
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        // Calculate the size of each cell based on the screen width
        int cellSize = screenWidth / BOARD_SIZE;

        // Initialize the board UI based on the matrix
        initializeBoardUI(tableLayout, cellSize);

        // Example: Show "It's my turn" text
        showTurnText();

        // Set button click listeners
        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button_play);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(GameBoard.this, "Button 1 clicked", Toast.LENGTH_SHORT).show();
                turnText.setText("Button 1 clicked");
                disabledBoard = false;
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(GameBoard.this, "Button 2 clicked", Toast.LENGTH_SHORT).show();
                turnText.setText("Button 2 clicked");
                disabledBoard = false;
            }
        });
    }

    private void placeBlueCircleRandomly() {
        // Define the possible positions for the blue circle in the last row
        int[] positions = {1, 3, 5, 7};

        // Randomly select one of these positions
        Random random = new Random();
        int selectedPosition = positions[random.nextInt(positions.length)];

        // Place the blue circle in the selected position
        boardMatrix[7][selectedPosition] = 2;
    }

    public void initializeBoardUI(TableLayout tableLayout, int cellSize) {
        for (int row1 = 0; row1 < BOARD_SIZE; row1++) {
            TableRow tableRow = new TableRow(this);
            for (int col1 = 0; col1 < BOARD_SIZE; col1++) {
                TextView cell = new TextView(this);
                TableRow.LayoutParams params = new TableRow.LayoutParams(cellSize, cellSize);
                cell.setLayoutParams(params);

                switch (boardMatrix[row1][col1]) {
                    case 1:
                        cell.setBackgroundResource(R.drawable.red_circle_cell);
                        break;
                    case 2:
                        cell.setBackgroundResource(R.drawable.blue_circle_cell);
                        break;
                    case 3:
                        cell.setBackgroundResource(R.drawable.black_cell);
                        break;
                    case 0:
                    default:
                        cell.setBackgroundResource(R.drawable.white_cell);
                        break;
                }

                cell.setTag(row1 + "," + col1);
                int finalCol = col1;
                int finalRow = row1;
                cell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleCellClick(v, finalRow, finalCol);
                    }
                });

                tableRow.addView(cell);
            }
            tableLayout.addView(tableRow);
        }
    }

    private void handleCellClick(View cell, int row, int col) {
        if(!disabledBoard) {
            if (selectedCell == null) {
                // Select the cell if it contains a circle
                if (boardMatrix[row][col] == 1 || boardMatrix[row][col] == 2) {
                    selectedCell = cell;
                    selectedRow = row;
                    selectedCol = col;
                    cell.setBackgroundResource(R.drawable.white_cell);
                    Toast.makeText(this, "Selected cell: " + cell.getTag(), Toast.LENGTH_SHORT).show();
                }
            } else {
                // Move the circle to the new cell if it's a white cell
                if (boardMatrix[row][col] == 0) {
                    int selectedCircleResource = boardMatrix[selectedRow][selectedCol] == 1 ?
                            R.drawable.red_circle_cell : R.drawable.blue_circle_cell;

                    boardMatrix[row][col] = boardMatrix[selectedRow][selectedCol];
                    boardMatrix[selectedRow][selectedCol] = 0;

                    cell.setBackgroundResource(selectedCircleResource);
                    selectedCell = null;
                    selectedRow = -1;
                    selectedCol = -1;
                    Toast.makeText(this, "Moved the circle to a new cell!", Toast.LENGTH_SHORT).show();
                    disabledBoard = true;
                } else {
                    // Deselect if an invalid move
                    if (boardMatrix[selectedRow][selectedCol] == 1) {
                        selectedCell.setBackgroundResource(R.drawable.red_circle_cell);
                    } else {
                        selectedCell.setBackgroundResource(R.drawable.blue_circle_cell);
                    }
                    selectedCell = null;
                    selectedRow = -1;
                    selectedCol = -1;
                    Toast.makeText(this, "Invalid move!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showTurnText() {
        turnText.setVisibility(View.VISIBLE);
        disabledBoard = false;
    }
}