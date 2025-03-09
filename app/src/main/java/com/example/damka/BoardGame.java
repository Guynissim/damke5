package com.example.damka;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BoardGame extends View {
    private Square[][] squares;
    private final int NUM_OF_SQUARES = 8;
    private Soldier selectedSoldier = null;
    private boolean isSoldierJumped = false; // Checks if jump
    private int winnerside = 0;//Red - 1, Blue - 2, no winner yet - 0
    private boolean isMoving = false;// true - for FB updates, false - for ACTION_MOVE updates

    //Colors:
    private int crown;
    private final int playerOneColor;
    private final int playerTwoColor;
    private final int kingOneCrown;
    private final int kingTwoCrown;

    private final int squareOne;//Bright - without soldiers
    private final int squareTwo;//Dark - with soldiers

    // Fields for the Firebase:
    private GameSessionManager gameSessionManager;
    private int[][] boardState;
    List<List<Long>> boardStateFromFB;
    private String gameId, playerId;
    private boolean isPlayerTurn = false;
    private int isPlayer1;

    public BoardGame(Context context, GameSessionManager gameSessionManager, String gameId, String playerId, int isPlayer1, int[][] boardState) {
        super(context);
        this.squares = new Square[NUM_OF_SQUARES][NUM_OF_SQUARES];
        this.gameSessionManager = gameSessionManager;
        this.gameId = gameId;
        this.playerId = playerId;
        this.boardState = boardState;
        this.isPlayer1 = isPlayer1;
        playerOneColor = ContextCompat.getColor(getContext(), R.color.soldier_player_one);
        playerTwoColor = ContextCompat.getColor(getContext(), R.color.soldier_player_two);
        kingOneCrown = ContextCompat.getColor(getContext(), R.color.king_crown_one);
        kingTwoCrown = ContextCompat.getColor(getContext(), R.color.king_crown_two);
        squareOne = ContextCompat.getColor(getContext(), R.color.square_one);
        squareTwo = ContextCompat.getColor(getContext(), R.color.square_two);
        startListeningToGameSession();
    }

    public void startListeningToGameSession() {
        gameSessionManager.listenToGameSession(gameId, new GameSessionManager.GameStateListener() {
            @Override
            public void onGameStateChanged(Map<String, Object> gameState) {
                boardStateFromFB = (List<List<Long>>) gameState.get("boardState");
                boolean turn = (boolean) gameState.get("turn"); // Get turn from Firebase

                Log.d("DEBUG", "Turn from Firebase: " + turn);
                Log.d("DEBUG", "Player " + isPlayer1 + ", my playerId: " + playerId);


                if (isPlayer1 == 1 && turn || isPlayer1 == 2 && !turn) {
                    isPlayerTurn = true;
                } else
                    isPlayerTurn = false;

                invalidate(); // Redraw board
            }

            @Override
            public void onError(@NonNull Exception e) {
                Log.e("BoardGame", "Error listening to game session", e);
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = canvas.getWidth() / NUM_OF_SQUARES;
        if (!isMoving) { // FB updates
            makeSquaresArray(w);//Makes the new/first array from the FB.
        }
        drawBoard(canvas);//Draws the whole board
    }

    private void drawBoard(Canvas canvas) {
        // First, draw all squares
        for (int i = 0; i < NUM_OF_SQUARES; i++) {
            for (int j = 0; j < NUM_OF_SQUARES; j++) {
                if (squares[i][j] != null) {
                    squares[i][j].draw(canvas); // Draw all board squares first
                }
            }
        }
        // Then, draw all soldiers on top of the board
        for (int i = 0; i < NUM_OF_SQUARES; i++) {
            for (int j = 0; j < NUM_OF_SQUARES; j++) {
                if (squares[i][j] != null && squares[i][j].soldier != null) {
                    if (selectedSoldier != null && squares[i][j].soldier.isIdentical(selectedSoldier)) {
                        selectedSoldier.draw(canvas);
                    } else {
                        squares[i][j].soldier.draw(canvas);
                    }
                }
            }
        }
    }

    private void makeSquaresArray(int w) {
        int x = 0;
        int y = 200;
        int h = w;
        int color;
        if (boardState != null) {
            for (int i = 0; i < NUM_OF_SQUARES; i++) {
                for (int j = 0; j < NUM_OF_SQUARES; j++) {
                    int state = boardState[i][j];

                    if (i % 2 == 0) //Even Line
                    {
                        if (j % 2 == 0)
                            color = squareOne;
                        else
                            color = squareTwo;
                    } else //Odd Line
                    {
                        if (j % 2 == 0)
                            color = squareTwo;
                        else
                            color = squareOne;

                    }
                    squares[i][j] = new Square(x, y, color, w, h, i, j);
                    x = x + w;

                    // Create soldiers based on the state
                    if (state == 1)  // Side 1 soldier
                        squares[i][j].soldier = new Soldier(w / 2 + squares[i][j].x, h / 2 + squares[i][j].y, playerOneColor, w / 3, i, j, 1);
                    else if (state == 2)  // Side 2 soldier
                        squares[i][j].soldier = new Soldier(w / 2 + squares[i][j].x, h / 2 + squares[i][j].y, playerTwoColor, w / 3, i, j, 2);
                    else if (state == 3) // Side 1 king
                        squares[i][j].soldier = new King(w / 2 + squares[i][j].x, h / 2 + squares[i][j].y, playerOneColor, kingOneCrown, w / 3, i, j, 1);
                    else if (state == 4) // Side 2 king
                        squares[i][j].soldier = new King(w / 2 + squares[i][j].x, h / 2 + squares[i][j].y, playerTwoColor, kingTwoCrown, w / 3, i, j, 2);
                }
                y = y + h;
                x = 0;
            }
        }
    }

    public void updateBoardState(int[][] boardState) {
        for (int i = 0; i < boardState.length; i++) {
            Log.d("Row " + i, Arrays.toString(boardState[i]));
        }
        this.boardState = boardState;
        invalidate();
    }

    private void handleMove() {
        updateBoardState(getBoardStateFromSquares());
        Map<String, Object> gameState = new HashMap<>();
        gameState.put("boardState", getCurrentBoardStateAsList());
        gameState.put("turn", isPlayer1 == 2);// player1 next - true, player2 next - false
        gameSessionManager.updateGameState(gameId, gameState);
        invalidate();
    }

    private int[][] getBoardStateFromSquares() {
        int[][] updatedBoardState = new int[NUM_OF_SQUARES][NUM_OF_SQUARES];
        for (int i = 0; i < NUM_OF_SQUARES; i++) {
            for (int j = 0; j < NUM_OF_SQUARES; j++) {
                updatedBoardState[i][j] = squares[i][j].getState();
            }
        }
        return updatedBoardState;
    }

    private List<List<Integer>> getCurrentBoardStateAsList() {
        List<List<Integer>> boardStateList = new ArrayList<>();
        for (int i = 0; i < boardState.length; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < boardState[i].length; j++) {
                row.add(boardState[i][j]);
            }
            boardStateList.add(row);
        }
        return boardStateList;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                for (int i = 0; i < NUM_OF_SQUARES; i++) {
                    for (int j = 0; j < NUM_OF_SQUARES; j++) {
                        Square square = squares[i][j];
                        if (square != null && square.didUserTouchMe((int) touchX, (int) touchY) && square.soldier != null) {
                            selectedSoldier = square.soldier;
                            Log.d("ACTION_DOWN", "Selected soldier at: " + i + ", " + j);
                            return true;
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (selectedSoldier != null) {
                    int centerX = (int) touchX;
                    int centerY = (int) touchY;
                    selectedSoldier.Move(centerX, centerY);
                    Log.d("ACTION_MOVE", "Soldier at: " + centerX + ", " + centerY);
                    isMoving = true;
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                isMoving = false;
                if (selectedSoldier != null) {
                    if (!isPlayerTurn) {
                        selectedSoldier.Move(selectedSoldier.lastX, selectedSoldier.lastY);
                        Toast.makeText(getContext(), "Wait for you turn!", Toast.LENGTH_SHORT).show();
                        invalidate();
                    } else if (isPlayer1 != selectedSoldier.side) {
                        selectedSoldier.Move(selectedSoldier.lastX, selectedSoldier.lastY);
                        Toast.makeText(getContext(), "These are not your soldiers!", Toast.LENGTH_SHORT).show();
                        invalidate();
                    } else { //if Turn and side are correct, Checking if Move is valid
                        updateColumnAndRow(selectedSoldier);// Updates current column and Row for checking Movement.
                        if (!isValidSquare(selectedSoldier)) {
                            selectedSoldier.Move(selectedSoldier.lastX, selectedSoldier.lastY);
                        }
                        invalidate();
                        selectedSoldier = null;
                        return true;
                    }
                }
                break;

            default:
                break;
        }
        return true;
    }

    private boolean isValidSquare(Soldier soldier) {
        King king;
        for (int i = 0; i < NUM_OF_SQUARES; i++) {
            for (int j = 0; j < NUM_OF_SQUARES; j++) {
                Square square = squares[i][j];
                if (square.didUserTouchMe(soldier.x, soldier.y) && square.soldier == null && square.color == squareTwo) {
                    Log.d("Square Check", "Square checked: column=" + j + ", row=" + i);
                    Log.d("Soldier Before", "Soldier: column=" + soldier.column + ", row=" + soldier.row);
                    isSoldierJumped = false;
                    if (soldier instanceof King) {
                        king = (King) soldier;
                        if (isValidMove(king)) {
                            Toast.makeText(getContext(), "Valid Move!", Toast.LENGTH_SHORT).show();
                            king.Move(square.x + square.width / 2, square.y + square.height / 2);
                            square.soldier = king;
                            squares[king.lastColumn][king.lastRow].soldier = null;
                            updateLastPosition(king);
                            handleMove();
                            Log.d("Snap Success", "King snapped to valid square: " + square.x + ", " + square.y);
                            invalidate();
                            if (isSoldierJumped) {
                                checkAndDisplayWinner();
                            }
                            return true;
                        }
                    } else if (isValidMove(soldier)) {
                        Toast.makeText(getContext(), "Valid Move!", Toast.LENGTH_SHORT).show();
                        soldier.Move(square.x + square.width / 2, square.y + square.height / 2);
                        squares[soldier.lastColumn][soldier.lastRow].soldier = null;
                        updateLastPosition(soldier);

                        if (soldier.side == 1 && soldier.column == 7 || soldier.side == 2 && soldier.column == 0) {
                            king = becomeKing(soldier);
                            square.soldier = king;
                        } else
                            square.soldier = soldier;
                        Log.d("Snap Success", "Soldier snapped to valid square: " + square.x + ", " + square.y);
                        handleMove();
                        invalidate();
                        if (isSoldierJumped) {
                            winnerside = isGameOver();
                            if (winnerside == 1)
                                Toast.makeText(getContext(), "The winner side is BLUE!!!", Toast.LENGTH_SHORT).show();
                            if (winnerside == 2)
                                Toast.makeText(getContext(), "The winner side is Red!!!", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private King becomeKing(Soldier soldier) {
        int x = soldier.x;
        int y = soldier.y;
        int side = soldier.side;
        int column = soldier.column;
        int row = soldier.row;
        int color = soldier.color;
        int radius = soldier.radius;
        if (side == 1)
            crown = kingOneCrown;
        if (side == 2)
            crown = kingTwoCrown;
        return new King(x, y, color, crown, radius, column, row, side);
    }

    public boolean isValidMove(King king) {
        int columnStep = Math.abs(king.column - king.lastColumn);
        int rowStep = Math.abs(king.row - king.lastRow);
        Log.d("KingMove", "Validating king move: columnStep = " + columnStep + ", rowStep = " + rowStep);
        // Ensure the movement is diagonal and involves at least one step
        if (columnStep != rowStep || columnStep == 0) {
            Log.d("KingMove", "Invalid move: not diagonal or no movement.");
            return false;
        }
        int colDirection = (king.column - king.lastColumn) > 0 ? 1 : -1; // 1: Down, -1: Up
        int rowDirection = (king.row - king.lastRow) > 0 ? 1 : -1; // 1: Right, -1: Left
        boolean hasJumped = false; // Track if a jump has occurred
        boolean pathClear = true; // Ensure the path is valid
        int jumpedEnemyColumn = -1;
        int jumpedEnemyRow = -1;
        for (int i = 1; i <= columnStep; i++) {
            int intermediateColumn = king.lastColumn + i * colDirection;
            int intermediateRow = king.lastRow + i * rowDirection;
            Square intermediateSquare = squares[intermediateColumn][intermediateRow];
            Soldier middleSoldier = intermediateSquare.soldier;
            if (middleSoldier != null) {
                if (middleSoldier.side != king.side && !hasJumped) {
                    // Valid jump over an opponent soldier
                    hasJumped = true;
                    jumpedEnemyColumn = intermediateColumn;
                    jumpedEnemyRow = intermediateRow;
                    Log.d("KingMove", "Jumping over enemy soldier at column=" + intermediateColumn + ", row=" + intermediateRow);
                } else {
                    // Either jumping over multiple pieces or over a teammate
                    Log.d("KingMove", "Move failed: path is blocked or invalid jump.");
                    pathClear = false;
                    break;
                }
            }
        }
        // Ensure the destination square is empty
        Square destinationSquare = squares[king.column][king.row];
        if (!pathClear || destinationSquare.soldier != null) {
            Log.d("KingMove", "Move failed: destination is not valid.");
            return false;
        }

        // Validate landing for jumps
        if (hasJumped) {
            int jumpDistance = Math.abs(king.column - jumpedEnemyColumn);
            if (jumpDistance < 1) {
                Log.d("KingMove", "Move failed: jump landing is too close.");
                return false;
            }
            // Remove the jumped-over soldier
            squares[jumpedEnemyColumn][jumpedEnemyRow].soldier = null;
            isSoldierJumped = true;//King made a jump
            Log.d("KingMove", "Removed jumped-over enemy soldier at column=" + jumpedEnemyColumn + ", row=" + jumpedEnemyRow);
        }
        Log.d("KingMove", "Move is valid.");
        return true;
    }


    private boolean isValidSingleStepForSide1(Soldier soldier) {
        if (soldier.row == 0) {
            return soldier.column - 1 == soldier.lastColumn && soldier.row + 1 == soldier.lastRow;
        } else if (soldier.row == 7) {
            return soldier.column - 1 == soldier.lastColumn && soldier.row - 1 == soldier.lastRow;
        } else {
            return (soldier.column - 1 == soldier.lastColumn && soldier.row + 1 == soldier.lastRow) ||
                    (soldier.column - 1 == soldier.lastColumn && soldier.row - 1 == soldier.lastRow);
        }
    }

    private boolean isValidSingleStepForSide2(Soldier soldier) {
        if (soldier.row == 0) {
            return soldier.column + 1 == soldier.lastColumn && soldier.row + 1 == soldier.lastRow;
        } else if (soldier.row == 7) {
            return soldier.column + 1 == soldier.lastColumn && soldier.row - 1 == soldier.lastRow;
        } else {
            return (soldier.column + 1 == soldier.lastColumn && soldier.row + 1 == soldier.lastRow) ||
                    (soldier.column + 1 == soldier.lastColumn && soldier.row - 1 == soldier.lastRow);
        }
    }

    private boolean isValidJumpForSide1(Soldier soldier) {
        if (soldier.column - 2 == soldier.lastColumn && soldier.row + 2 == soldier.lastRow &&
                isEnemySoldier(squares[soldier.column - 1][soldier.row + 1].soldier, soldier.side)) {
            squares[soldier.column - 1][soldier.row + 1].soldier = null;
            return true;
        } else if (soldier.column - 2 == soldier.lastColumn && soldier.row - 2 == soldier.lastRow &&
                isEnemySoldier(squares[soldier.column - 1][soldier.row - 1].soldier, soldier.side)) {
            squares[soldier.column - 1][soldier.row - 1].soldier = null;
            return true;
        }
        return false;
    }

    private boolean isValidJumpForSide2(Soldier soldier) {
        if (soldier.column + 2 == soldier.lastColumn && soldier.row + 2 == soldier.lastRow &&
                isEnemySoldier(squares[soldier.column + 1][soldier.row + 1].soldier, soldier.side)) {
            squares[soldier.column + 1][soldier.row + 1].soldier = null;
            return true;
        } else if (soldier.column + 2 == soldier.lastColumn && soldier.row - 2 == soldier.lastRow &&
                isEnemySoldier(squares[soldier.column + 1][soldier.row - 1].soldier, soldier.side)) {
            squares[soldier.column + 1][soldier.row - 1].soldier = null;
            return true;
        }
        return false;
    }

    public boolean isValidMove(Soldier soldier) {
        int step = Math.abs(soldier.column - soldier.lastColumn);

        if (step == 1) {
            if (soldier.side == 1) {
                if (isValidSingleStepForSide1(soldier)) {
                    return true;
                }
            } else if (isValidSingleStepForSide2(soldier)) {
                return true;
            }
        } else if (step == 2) {
            if (soldier.side == 1) {
                if (isValidJumpForSide1(soldier)) {
                    isSoldierJumped = true;
                    checkAndDisplayWinner();
                    return true;
                }
            } else if (isValidJumpForSide2(soldier)) {
                isSoldierJumped = true;
                checkAndDisplayWinner();
                return true;
            }
        }
        return false;
    }

    // Updates soldier's column and row
    private void updateColumnAndRow(Soldier soldier) {
        for (int i = 0; i < NUM_OF_SQUARES; i++) {
            for (int j = 0; j < NUM_OF_SQUARES; j++) {
                if (squares[i][j].didUserTouchMe(soldier.x, soldier.y)) {
                    soldier.column = i;
                    soldier.row = j;
                    break;
                }
            }
        }
    }

    // Helper method to check if a soldier belongs to the enemy side
    private boolean isEnemySoldier(Soldier soldier, int currentSide) {
        return soldier != null && soldier.side != currentSide;
    }

    private void updateLastPosition(Soldier soldier) {
        // Update only when soldier has successfully snapped into this square
        soldier.lastX = soldier.x;
        soldier.lastY = soldier.y;
        // Update soldier's current row and column
        soldier.lastColumn = soldier.column;
        soldier.lastRow = soldier.row;
    }

    private int isGameOver() {
        boolean side1HasSoldiers = false;
        boolean side2HasSoldiers = false;
        for (int i = 0; i < NUM_OF_SQUARES; i++) {
            for (int j = 0; j < NUM_OF_SQUARES; j++) {
                Soldier soldier = squares[i][j].soldier;
                if (soldier != null) {
                    if (soldier.side == 1) {
                        side1HasSoldiers = true;
                    } else if (soldier.side == 2) {
                        side2HasSoldiers = true;
                    }
                }
                if (side1HasSoldiers && side2HasSoldiers) {
                    return 0; // Game is not over, both sides have soldiers
                }
            }
        }
        // Determine the winner
        if (!side1HasSoldiers) {
            return 2; // Side 2 wins
        } else if (!side2HasSoldiers) {
            return 1; // Side 1 wins
        }

        return 0; // This shouldn't happen, but just in case
    }

    public void checkAndDisplayWinner() {
        if (isSoldierJumped == true) {
            int winnerside = isGameOver();
            if (winnerside == 0)
                return; // No winner yet
            // Ensure gameId is not null
            if (gameId == null || gameId.isEmpty()) {
                Log.e("BoardGame", "Game ID is null! Cannot update winner.");
                return;
            }
            Toast.makeText(getContext(), "The winner side is " + (winnerside == 1 ? "Red" : "Blue") + "!!!", Toast.LENGTH_SHORT).show();
            gameSessionManager.updateWinner(gameId, winnerside);
        }
    }
}




