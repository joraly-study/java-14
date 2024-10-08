package com.example.sharedreference;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    SharedPreferences themeSettings;

    ImageButton changeTheme;
    Editor setEditor;

    private boolean playerXTurn = true;
    private boolean isBotGame = false;
    private int[][] board = new int[3][3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeSettings = PreferenceManager.getDefaultSharedPreferences(this);

        setCurrentTheme();
        setContentView(R.layout.activity_main);
        changeTheme = findViewById(R.id.imageButton);

        updateThemeButtonIcon();

        changeTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEditor = themeSettings.edit();

                boolean isNightMode = !themeSettings.getBoolean("MODE_NIGHT_ON", false);
                AppCompatDelegate.setDefaultNightMode(isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                setEditor.putBoolean("MODE_NIGHT_ON", isNightMode);
                setEditor.apply();

                updateThemeButtonIcon();

                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                startActivity(intent);
            }
        });

        resetBoard();
    }

    public void onButtonClick(View view) {
        if (playerXTurn) {
            Button button = (Button) view;
            Object tagObject = button.getTag();

            if (tagObject != null) {
                String tag = tagObject.toString();
                int row = Character.getNumericValue(tag.charAt(0));
                int col = Character.getNumericValue(tag.charAt(1));

                if (row >= 0 && row < board.length && col >= 0 && col < board[0].length) {
                    if (board[row][col] == 0) {
                        board[row][col] = 1;
                        button.setText("X");
                        button.setEnabled(false);

                        if (checkWin()) {
                            Toast.makeText(this, "КРЕСТИКИ ОДЕРЖАЛИ ПОБЕДУ В ЭТОЙ СХВАТКЕ РЭП БАТТЛ МАЙНКРАФТ 1.7.10!", Toast.LENGTH_LONG).show();
                            disable();
                        } else if (isBoardFull()) {
                            Toast.makeText(this, "Ничья!", Toast.LENGTH_LONG).show();
                        } else {
                            playerXTurn = false;
                            if (isBotGame) {
                                botMove();
                            }
                        }
                    }
                }
            } else {
                throw new RuntimeException("ТЭГ УКРАЛИ");
            }
        } else if (!isBotGame) {
            Button button = (Button) view;
            Object tagObject = button.getTag();

            if (tagObject != null) {
                String tag = tagObject.toString();
                int row = Character.getNumericValue(tag.charAt(0));
                int col = Character.getNumericValue(tag.charAt(1));

                if (row >= 0 && row < board.length && col >= 0 && col < board[0].length) {
                    if (board[row][col] == 0) {
                        board[row][col] = 2;
                        button.setText("O");
                        button.setEnabled(false);

                        if (checkWin()) {
                            Toast.makeText(this, "НОЛИКИ ОДЕРЖАЛИ ПОБЕДУ В ЭТОЙ СХВАТКЕ РЭП БАТТЛ МАЙНКРАФТ 1.7.10!", Toast.LENGTH_LONG).show();
                            disable();
                        } else if (isBoardFull()) {
                            Toast.makeText(this, "Ничья!", Toast.LENGTH_LONG).show();
                        } else {
                            playerXTurn = true;
                        }
                    }
                }
            } else {
                throw new RuntimeException("ТЭГ УКРАЛИ");
            }
        }
    }


    public void resetGame(View view) {
        resetBoard();
        enable();
        playerXTurn = true;
    }

    private void resetBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = 0;
            }
        }
    }



    private void botMove() {
        if (!isBoardFull()) {
            int row, col;
            do {
                row = (int) (Math.random() * 3);
                col = (int) (Math.random() * 3);
            } while (board[row][col] != 0);

            board[row][col] = 2;
            Button button = findButtonByTag(row, col);
            button.setText("O");
            button.setEnabled(false);

            if (checkWin()) {
                Toast.makeText(this, "НОЛИКИ ОДЕРЖАЛИ ПОБЕДУ В ЭТОЙ СХВАТКЕ РЭП БАТТЛ МАЙНКРАФТ 1.7.10!", Toast.LENGTH_LONG).show();
                disable();
            } else if (isBoardFull()) {
                Toast.makeText(this, "Ничья!", Toast.LENGTH_LONG).show();
            } else {
                playerXTurn = true;
            }
        }
    }

    private Button findButtonByTag(int row, int col) {
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            if (child instanceof Button) {
                Button button = (Button) child;
                Object tagObject = button.getTag();
                if (tagObject != null) {
                    String tag = tagObject.toString();
                    int buttonRow = Character.getNumericValue(tag.charAt(0));
                    int buttonCol = Character.getNumericValue(tag.charAt(1));
                    if (buttonRow == row && buttonCol == col) {
                        return button;
                    }
                }
            }
        }
        return null;
    }

    private boolean checkWin() {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == board[i][1] && board[i][1] == board[i][2] && board[i][0] != 0) {
                return true;
            }
            if (board[0][i] == board[1][i] && board[1][i] == board[2][i] && board[0][i] != 0) {
                return true;
            }
        }
        if ((board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[0][0] != 0) ||
                (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[0][2] != 0)) {
            return true;
        }
        return false;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == 0) return false;
            }
        }
        return true;
    }

    private void disable() {
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            if (child instanceof Button) {
                child.setEnabled(false);
            }
        }
    }

    private void enable() {
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            if (child instanceof Button) {
                Button button = (Button) child;
                button.setEnabled(true);
                button.setText("");
            }
        }
    }

    private void setCurrentTheme()
    {
        boolean isNightMode = themeSettings.getBoolean("MODE_NIGHT_ON", false);
        AppCompatDelegate.setDefaultNightMode(isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void updateThemeButtonIcon() {
        if (themeSettings.getBoolean("MODE_NIGHT_ON", false)) {
            changeTheme.setImageResource(R.drawable.img_1);
        } else {
            changeTheme.setImageResource(R.drawable.img);
        }
    }

    public void botGame(View view) {
        resetBoard();
        enable();
        playerXTurn = true;
        if(isBotGame) {
            isBotGame = false;
            ((Button) view).setText("БОТА СЮДА");
        }
        else {
            isBotGame = true;
            ((Button) view).setText("С ДРУГОМ СЮДА");
        }
    }
}