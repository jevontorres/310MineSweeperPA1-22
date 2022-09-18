package com.example.gridlayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.Intent;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Random;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private int clock = 0;
    public int flags = 4;
    public boolean digging = true;
    private boolean win = false;
    private boolean running = true;
    private static final int COLUMN_COUNT = 8;
    private static final int ROW_COUNT = 10;
    private boolean[][] revealed;
    private ArrayList<ArrayList<Integer>> board;
    private ArrayList<ArrayList<Integer>> adj;
    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    public ArrayList<TextView> cell_tvs;

    public HashSet<TextView> bombSet;
    public HashSet<TextView> revealedSet;
    public HashSet<Pair<TextView, Integer>> reveal;
    public HashSet<TextView> zeroSet;

    private int dpToPixel(int dp) {

        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            clock = savedInstanceState.getInt("clock");
            running = savedInstanceState.getBoolean("running");
        }

        runTimer();
        revealedSet = new HashSet<TextView>();
        zeroSet = new HashSet<TextView>();
        reveal = new HashSet<Pair<TextView, Integer>>();
        revealed = new boolean[10][8];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 7; j++) {
                revealed[i][j] = false;
            }
        }
        cell_tvs = new ArrayList<TextView>();

        //create the grid
        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);
        for (int i = 0; i <= 9; i++) {
            for (int j = 0; j <= 7; j++) {
                TextView tv = new TextView(this);
                tv.setHeight(dpToPixel(40));
                tv.setWidth(dpToPixel(40));
                tv.setTextSize(20);//dpToPixel(32) );
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.GRAY);
                tv.setBackgroundColor(Color.GRAY);
                tv.setOnClickListener(this::onClickTV);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);
                lp.width = 80;
                lp.height = 80;
                lp.leftMargin = 5;
                lp.rightMargin = 5;
                lp.topMargin = 5;
                lp.bottomMargin = 5;
                tv.setGravity(Gravity.CENTER);
                tv.setLayoutParams(lp);
                grid.addView(tv, lp);

                cell_tvs.add(tv);
            }
            Button btnF = (Button)findViewById(R.id.flagging);
            btnF.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    digging = false;
                }
            });
            Button btnD = (Button)findViewById(R.id.digging);
            btnD.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    digging = true;
                }
            });
        }

        setMines();
        board = new ArrayList<ArrayList<Integer>>();
        board = TDMake();
        adj = new ArrayList<ArrayList<Integer>>();
        adj = NAMake();
    }

    public void setMines() {
        int mines = 4;
        int cells = 80;

        bombSet = new HashSet<>();
        for (int i = 0; i < mines; i++) {
            //Get random position for the next bomb
            Random rand = new Random();
            int index = rand.nextInt(cells);
            while (bombSet.contains(cell_tvs.get(index))) {
                //we get new position
                index = rand.nextInt(cells);
            }
            bombSet.add(cell_tvs.get(index));
        }
    }

    private void endGame() {
        running = false;
        Intent intent = new Intent(MainActivity.this, DisplayResults.class);
        intent.putExtra("win", win);
        intent.putExtra("clock", clock);
        startActivity(intent);
    }

    //find mines surrounding a cell
    public int neighbors(int n) {
        int count = 0;
        int y = n / COLUMN_COUNT;
        int x = n % COLUMN_COUNT;
        Log.d("coordinates", String.valueOf(x) + String.valueOf(y));
        Log.d("mine?", String.valueOf(board.get(y).get(x)));
        if (board.get(y).get(x) == 1) {
            return -1;
        }
        if (x > 0) {
            if (board.get(y).get(x - 1) == 1) {
                count++;
                Log.d("hint", "mine to left");
            }
        }
        if (x < 7) {
            if (board.get(y).get(x + 1) == 1) {
                count++;
                Log.d("hint", "mine to right");
            }
        }
        if (y > 0) {
            if (board.get(y - 1).get(x) == 1) {
                count++;
                Log.d("hint", "mine above");
            }
        }
        if (y < 9) {
            if (board.get(y + 1).get(x) == 1) {
                count++;
                Log.d("hint", "mine below");
            }
        }
        if (y < 9 && x < 7) {
            if (board.get(y + 1).get(x + 1) == 1) {
                count++;
                Log.d("hint", "mine to bottom right");
            }
        }
        if (y > 0 && x > 0) {
            if (board.get(y - 1).get(x - 1) == 1) {
                count++;
                Log.d("hint", "mine to top left");
            }
        }
        if (y < 9 && x > 0) {
            if (board.get(y + 1).get(x - 1) == 1) {
                count++;
                Log.d("hint", "mine to bottom left");
            }
        }
        if (y > 0 && x < 7) {
            if (board.get(y - 1).get(x + 1) == 1) {
                count++;
                Log.d("hint", "mine to top right");
            }
        }
        return count;
    }

    //make 2d neighbor array
    public ArrayList<ArrayList<Integer>> NAMake() {
        ArrayList<ArrayList<Integer>> NA = new ArrayList<ArrayList<Integer>>();
        int index = 0;
        for (int i = 0; i < ROW_COUNT; i++) {
            ArrayList<Integer> R = new ArrayList<Integer>();
            for (int j = 0; j < COLUMN_COUNT; j++) {
                R.add(neighbors(index));
                index++;
            }
            NA.add(R);
        }
        for (int i = 0; i < 10; i++) {
            Log.d("neighborArray", String.valueOf(NA.get(i)));
        }
        return NA;
    }

    //make 2d mine or empty array
    public ArrayList<ArrayList<Integer>> TDMake() {
        ArrayList<ArrayList<Integer>> TD = new ArrayList<ArrayList<Integer>>();
        int index = 0;
        for (int i = 0; i < ROW_COUNT; i++) {
            ArrayList<Integer> R = new ArrayList<Integer>();
            for (int j = 0; j < COLUMN_COUNT; j++) {
                if (bombSet.contains(cell_tvs.get(index))) {
                    R.add(1);
                } else {
                    R.add(0);
                }
                index++;
            }
            TD.add(R);
        }
        for (int i = 0; i < 10; i++) {
            Log.d("DEBUG_MESSAGE", String.valueOf(TD.get(i)));
        }
        return TD;
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("clock", clock);
        savedInstanceState.putBoolean("running", running);
    }

    private void updateFlag(int n) {
        final TextView flagView = (TextView) findViewById(R.id.flagCount);
        flags += n;
        flagView.setText(String.valueOf(flags));
    }

    private void runTimer() {
        final TextView timeView = (TextView) findViewById(R.id.timer);
        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                String time = String.format("%02d", clock);
                timeView.setText(time);
                if (running) {
                    clock++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n = 0; n < cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    private void printBool() {
        for (int y = 0; y < 10; y++) {
            String row = "";
            for (int x = 0; x < 8; x++) {
                if (revealed[y][x]) {
                    row += " T ";
                } else {
                    row += " F ";
                }
            }
            Log.d("bool", row);
        }
    }

    private boolean checkBomb(int x, int y) {
        if (board.get(y).get(x) == 1) {
            return true;
        }
        return false;
    }

    private void dfs(int x, int y) {
        if (x < 0 || y < 0 || x > 7 || y > 9) {
            return;
        }
        if (revealed[y][x]) {
            return;
        }
        if (adj.get(y).get(x) > 0) {
            revealed[y][x] = true;
            revealedSet.add(cell_tvs.get(y * 8 + x));
            Pair p = new Pair(cell_tvs.get(y * 8 + x), adj.get(y).get(x));
            reveal.add(p);
            return;
        } else if (adj.get(y).get(x) == 0) {
            revealed[y][x] = true;
            revealedSet.add(cell_tvs.get(y * 8 + x));
            Pair p = new Pair(cell_tvs.get(y * 8 + x), adj.get(y).get(x));
            reveal.add(p);
            zeroSet.add(cell_tvs.get(y * 8 + x));

            dfs(x + 1, y);
            dfs(x - 1, y);
            dfs(x, y + 1);
            dfs(x, y - 1);
            dfs(x + 1, y + 1);
            dfs(x + 1, y - 1);
            dfs(x - 1, y + 1);
            dfs(x - 1, y - 1);

        }
    }

    public void onClickTV(View view) {

        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);
        int y = n / COLUMN_COUNT; //y
        int x = n % COLUMN_COUNT; //x
        if (digging) {
            if (adj.get(y).get(x) == 0) {
                dfs(x, y);
            } else {
                Log.d("neighbor1", "you clicked a neighbor");
                revealed[y][x] = true;
                revealedSet.add(cell_tvs.get(y * 8 + x));
                Pair p = new Pair(cell_tvs.get(y * 8 + x), adj.get(y).get(x));
                reveal.add(p);
                tv.setText(String.valueOf(adj.get(y).get(x)));
            }
            printBool();
            if (bombSet.contains(tv)) {
                Log.d("bomb", String.valueOf(checkBomb(x, y)));
                tv.setTextColor(Color.BLACK);
                tv.setText(R.string.mine);
                tv.setBackgroundColor(Color.RED);
//                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
//                builder1.setMessage("LOSS!");
//                builder1.setCancelable(true);
//                AlertDialog alert11 = builder1.create();
//                alert11.show();
                win = false;
                endGame();
            } else {

                for (Pair p : reveal) {
                    TextView viewer = (TextView) p.first;
                    viewer.setText(String.valueOf(p.second));
                    viewer.setTextColor(Color.GRAY);
                    viewer.setBackgroundColor(Color.LTGRAY);
                }
            }

            for (TextView viewer : zeroSet) {
                viewer.setText("");
                viewer.setTextColor(Color.GRAY);
                viewer.setBackgroundColor(Color.LTGRAY);
            }

            if (tv.getCurrentTextColor() == Color.GRAY) {
                tv.setTextColor(Color.GRAY);
                tv.setBackgroundColor(Color.LTGRAY);
            }
            Log.d("zero count", String.valueOf(zeroSet.size()));
            Log.d("rev count", String.valueOf(revealedSet.size()));
            if (revealedSet.size() == 76) {
                win = true;
                endGame();
            }
        } else {
            String hint = (String) tv.getHint();
            if (flags>0 && hint!="f") {
                tv.setText(R.string.flag);
                tv.setHint("f");
                updateFlag(-1);
            }
            else if(hint=="f"){
                tv.setText("");
                tv.setHint("");
                updateFlag(1);
            }
        }
    }
}