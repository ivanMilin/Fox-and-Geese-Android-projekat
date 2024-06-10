package com.example.foxandgeese;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;

public class GameBoard extends AppCompatActivity {

    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;

    String myUsername, myOponent;
    String myUsernamemyOponent;

    int removedFigure;
    TableLayout tableLayout;
    Button button_play;
    Button button_home;

    Thread serverThread;

    int cellSize;

    int index = 0;

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
            {3, 2, 3, 0, 3, 0, 3, 0}
    };

    private int[][] startAgainMatrix = {
            {1, 3, 1, 3, 1, 3, 1, 3},
            {3, 0, 3, 0, 3, 0, 3, 0},
            {0, 3, 0, 3, 0, 3, 0, 3},
            {3, 0, 3, 0, 3, 0, 3, 0},
            {0, 3, 0, 3, 0, 3, 0, 3},
            {3, 0, 3, 0, 3, 0, 3, 0},
            {0, 3, 0, 3, 0, 3, 0, 3},
            {3, 0, 3, 2, 3, 0, 3, 0}
    };

    private View selectedCell = null;
    private int selectedRow = -1;
    private int selectedCol = -1;

    private boolean disabledBoard = false;
    String nextTurn;

    private boolean isFoxTurn = true;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("UPDATE_CELL")) {
                int row = intent.getIntExtra("row", -1);
                int col = intent.getIntExtra("col", -1);
                int value = intent.getIntExtra("value", -1);
                nextTurn = intent.getStringExtra("whichTurn");

                if (nextTurn != null) {
                    turnText.setText(nextTurn);
                    isFoxTurn = nextTurn.equals("Fox turn");
                }

                updateCellBackground(row, col, value);
                updateMatrix(row, col, value);
            }
            else if (intent.getAction().equals("GAME_OVER"))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(GameBoard.this);

                builder.setTitle("Game over")
                        .setMessage("Wanna play again")
                        .setCancelable(false)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                resetGameBoard();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                sendMessage("TerminateGame ="+myOponent);
                                finish();
                            }
                        })
                        .show();
            }
            else if(intent.getAction().equals("TERMINATE_GAME"))
            {
                finish();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("UPDATE_CELL");
        filter.addAction("GAME_OVER");
        filter.addAction("TERMINATE_GAME");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("Test Unregistering receiver"); // Add logging
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.game_board);

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("UPDATE_CELL"));
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("GAME_OVER"));
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("TERMINATE_GAME"));

        button_play = findViewById(R.id.button_play);
        button_home = findViewById(R.id.button_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        connectToServer();

        Intent intent = getIntent();
        myUsernamemyOponent = intent.getStringExtra(MainActivity.USERNAME_EXTRA);

        System.out.println("+++++++++++++++++++++++++++++++++++++++++");
        System.out.println("GameBoard - "+myUsernamemyOponent);
        System.out.println("+++++++++++++++++++++++++++++++++++++++++");

        String[] splited = myUsernamemyOponent.split(",");
        myUsername = splited[0];
        myOponent  = splited[1];

        tableLayout = findViewById(R.id.gameBoard);
        turnText = findViewById(R.id.turnText);

        turnText.setText("Fox turn");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        cellSize = screenWidth / BOARD_SIZE;

        initializeBoardUI(tableLayout, cellSize, boardMatrix);
        setupCellClickListeners(tableLayout);

        showTurnText();

        final boolean[] isOriginalState1 = {true};
        final boolean[] isOriginalState2 = {true};

        button_play.setText(myUsername);
        button_home.setText(myOponent);
        button_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (isOriginalState1[0])
                {
                    button_play.setBackgroundColor(Color.RED);
                    button_play.setTextColor(Color.WHITE);
                }
                else
                {
                    button_play.setBackgroundColor(Color.WHITE);
                    button_play.setTextColor(Color.RED);
                }

                isOriginalState1[0] = !isOriginalState1[0];
                disabledBoard = false;
            }
        });

        button_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (isOriginalState2[0])
                {
                    button_home.setBackgroundColor(Color.RED);
                    button_home.setTextColor(Color.WHITE);
                }
                else
                {
                    button_home.setBackgroundColor(Color.WHITE);
                    button_home.setTextColor(Color.RED);
                }

                isOriginalState2[0] = !isOriginalState2[0];
                disabledBoard = false;

            }
        });
    }

    //==============================================================================================
    private void placeBlueCircleRandomly(int[][] boardMatrix) {
        // Define the possible positions for the blue circle in the last row
        int[] positions = {1, 3, 5, 7};

        // Randomly select one of these positions
        Random random = new Random();
        int selectedPosition = positions[random.nextInt(positions.length)];

        // Place the blue circle in the selected position
        boardMatrix[7][selectedPosition] = 2;
    }
    //==============================================================================================
    public void initializeBoardUI(TableLayout tableLayout, int cellSize, int[][] boardMatrix) {
        tableLayout.removeAllViews();
        for (int row = 0; row < BOARD_SIZE; row++) {
            TableRow tableRow = new TableRow(this);
            for (int col = 0; col < BOARD_SIZE; col++) {
                TextView cell = new TextView(this);
                TableRow.LayoutParams params = new TableRow.LayoutParams(cellSize, cellSize);
                cell.setLayoutParams(params);

                switch (boardMatrix[row][col]) {
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

                cell.setTag(row + "," + col);
                tableRow.addView(cell);
            }
            tableLayout.addView(tableRow);
        }
    }
    //==============================================================================================
    private void handleCellClick(View cell, int row, int col) {
        if (!disabledBoard) {
            if (selectedCell == null) {
                // Ensure correct figure is selected based on current turn
                if ((isFoxTurn && boardMatrix[row][col] == 2) || (!isFoxTurn && boardMatrix[row][col] == 1)) {
                    selectedCell = cell;
                    selectedRow = row;
                    selectedCol = col;
                    cell.setBackgroundResource(R.drawable.white_cell);

                    removedFigure = boardMatrix[row][col];
                    boardMatrix[row][col] = 0; // Set the current cell to empty

                    String messageToSend = "RemoveFigure =" + myOponent + "#" + selectedRow + "," + selectedCol + "," + removedFigure;
                    sendMessage(messageToSend);
                } else {
                    Toast.makeText(this, "Invalid selection! It's " + (isFoxTurn ? "Fox" : "Geese") + " turn.", Toast.LENGTH_SHORT).show();
                }
            } else {
                boolean isValidMove = false;

                if (removedFigure == 1) {
                    if (row == selectedRow + 1 && boardMatrix[row][col] == 0) {
                        isValidMove = true;
                    }
                } else if (removedFigure == 2) {
                    if ((row == selectedRow + 1 || row == selectedRow - 1) && boardMatrix[row][col] == 0) {
                        isValidMove = true;
                    }
                }

                if (isValidMove) {
                    int selectedCircleResource = (removedFigure == 1) ? R.drawable.red_circle_cell : R.drawable.blue_circle_cell;
                    boardMatrix[row][col] = removedFigure;
                    cell.setBackgroundResource(selectedCircleResource);

                    isFoxTurn = !isFoxTurn; // Toggle the turn
                    String nextTurn = isFoxTurn ? "Fox turn" : "Geese turn";
                    turnText.setText(nextTurn);
                    String messageToSend = "UpdateTable =" + myOponent + "#" + row + "," + col + "," + removedFigure + "#" + nextTurn;
                    sendMessage(messageToSend);

                    selectedCell = null;
                    selectedRow = -1;
                    selectedCol = -1;
                    //disabledBoard = true;
                } else {
                    // Deselect if an invalid move and restore the figure to its original position
                    if (removedFigure == 1) {
                        selectedCell.setBackgroundResource(R.drawable.red_circle_cell);
                    } else {
                        selectedCell.setBackgroundResource(R.drawable.blue_circle_cell);
                    }
                    boardMatrix[selectedRow][selectedCol] = removedFigure;

                    String nextTurn = isFoxTurn ? "Fox turn" : "Geese turn";
                    turnText.setText(nextTurn);
                    String messageToSend = "UpdateTable =" + myOponent + "#" + selectedRow + "," + selectedCol + "," + removedFigure + "#" + nextTurn;
                    sendMessage(messageToSend);

                    // Reset selection
                    selectedCell = null;
                    selectedRow = -1;
                    selectedCol = -1;
                    Toast.makeText(this, "Invalid move!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    //==============================================================================================
    private String matrixToString(int[][] matrix) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : matrix) {
            sb.append(Arrays.toString(row)).append("\n");
        }
        return sb.toString();
    }
    //==============================================================================================
    public void setupCellClickListeners(TableLayout tableLayout) {
        for (int row = 0; row < tableLayout.getChildCount(); row++) {
            TableRow tableRow = (TableRow) tableLayout.getChildAt(row);
            for (int col = 0; col < tableRow.getChildCount(); col++) {
                View cell = tableRow.getChildAt(col);
                final int finalRow = row;
                final int finalCol = col;
                cell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleCellClick(v, finalRow, finalCol);
                    }
                });
            }
        }
    }
    //==============================================================================================
    public void showTurnText() {
        turnText.setVisibility(View.VISIBLE);
        disabledBoard = false;
    }
    //==============================================================================================
    public void setTurnText(String string) {
        turnText.setText(string);
    }
    //==============================================================================================
    public void connectToServer() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Singleton singleton = Singleton.getInstance();
                if (singleton != null) {
                    GameBoard.this.socket = singleton.socket;
                    GameBoard.this.br = singleton.br;
                    GameBoard.this.pw = singleton.pw;
                }
                else
                {
                    System.out.println("Failed connection with server");
                }
            }
        }).start();
    }
    //==============================================================================================
    public void sendMessage(String message)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (GameBoard.this.pw != null){
                    GameBoard.this.pw.println(message);
                    System.out.println("Message to server: " + message);
                }
            }
        }).start();
    }
    //==============================================================================================
    public int[][] getBoardMatrix() {
        return boardMatrix;
    }
    //==============================================================================================
    public BufferedReader getBr() {
        return br;
    }
    //==============================================================================================
    public String getMyUsername() {
        return myUsername;
    }
    //==============================================================================================
    public static void serverNotAvailable() {
        System.out.println("Server is not available");
    }
    //==============================================================================================
    public void updateMatrix(int row, int col, int value)
    {
        int[][] matrix = getBoardMatrix();

        if (row >= 0 && row < matrix.length && col >= 0 && col < matrix[0].length)
        {
            matrix[row][col] = value;
        } else {
            throw new IndexOutOfBoundsException("Row or column index out of bounds");
        }
    }
    //==============================================================================================
    public void updateCellBackground(int row, int col, int drawableResource) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            throw new IndexOutOfBoundsException("Row or column index out of bounds");
        }

        System.out.println("###########################################################");
        Log.d("BoardMatrix", matrixToString(boardMatrix));
        System.out.println("###########################################################");

        TableRow tableRow = (TableRow) tableLayout.getChildAt(row);
        TextView cell = (TextView) tableRow.getChildAt(col);

        switch (drawableResource) {
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
    }
    //==============================================================================================
    public void displayMessageFromReceiveMessageFromServer(String string)
    {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }
    //==============================================================================================
    private void resetGameBoard() {
        // Copy the startAgainMatrix to boardMatrix
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                boardMatrix[i][j] = startAgainMatrix[i][j];
            }
        }

        // Reinitialize the board UI
        initializeBoardUI(tableLayout, cellSize, boardMatrix);
        setupCellClickListeners(tableLayout);
    }

}