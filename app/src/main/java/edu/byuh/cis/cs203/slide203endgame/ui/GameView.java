package edu.byuh.cis.cs203.slide203endgame.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.byuh.cis.cs203.slide203endgame.R;
import edu.byuh.cis.cs203.slide203endgame.logic.GameBoard;
import edu.byuh.cis.cs203.slide203endgame.logic.GameMode;
import edu.byuh.cis.cs203.slide203endgame.logic.Player;
import edu.byuh.cis.cs203.slide203endgame.util.TickListener;
import edu.byuh.cis.cs203.slide203endgame.util.Timer;

/**
 * The GameView class runs the user interface of our app.
 * All drawing is initiated here; all touch events are
 * handled here.
 */
public class GameView extends ImageView implements TickListener {

    private Grid grid;
    private boolean firstRun;
    private GuiButton currentButton;
    private GuiButton[] buttons;
    private List<GuiToken> tokens;
    private GameBoard engine;
    private Timer timer;
    private int xScore, oScore;
    private Paint xScorePaint, oScorePaint;
    public GameMode gameMode;
    private GuiButton k;
    private MediaPlayer mp;
    static int[] backgrounds = new int[] {0,1,2};


    public void pauseMusic() {
        mp.pause();
    }

    public void restartMusic() {
        mp.start();
    }

    public void unloadMusic() {
        mp.release();
    }



    /**
     * Basic constructor. Initializes all fields that don't
     * directly rely on the screen resolution being known.
     * @param context The Activity class that created the View
     */
    public GameView(MainActivity context, GameMode gM) {
        super(context);
        setImageResource(R.drawable.game);
        setScaleType(ScaleType.FIT_XY);
        backgrounds[0] = R.drawable.game;
        backgrounds[1] = R.drawable.game1;
        backgrounds[2] = R.drawable.game2;
        firstRun = true;
        buttons = new GuiButton[10];
        tokens = new ArrayList<GuiToken>();
        engine = new GameBoard();
        timer = Timer.kk();
        xScore = oScore = 0;
        xScorePaint = new Paint();
        xScorePaint.setColor(Color.RED);
        oScorePaint = new Paint(xScorePaint);
        xScorePaint.setTextAlign(Paint.Align.LEFT);
        oScorePaint.setTextAlign(Paint.Align.RIGHT);
        gameMode = gM;
        mp = MediaPlayer.create(context, R.raw.zhaytee_microcomposer_1);
        mp.setLooping(true);
        mp.start();

//        startMessage();

    }

    /**
     * Draws the grid, buttons, and tokens.
     * @param c The Canvas object, supplied by the system.
     */
    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
//        c.drawColor(Color.WHITE);
        if (firstRun) {
            init();
            firstRun = false;
        }

        grid.draw(c);

        for (GuiToken t : tokens) {
            t.draw(c);
        }
        for (GuiButton b : buttons) {
            b.draw(c);
        }

        c.drawText("Pineapple: " + xScore, 0, xScorePaint.descent()*3, xScorePaint);
        c.drawText("Banana: " + oScore, getWidth(), oScorePaint.descent()*3, oScorePaint);

    }

    private void init() {
        float w = getWidth();
        float h = getHeight();
        float unit = w/16f;
        float gridX = unit * 2.5f;
        float cellSize = unit * 2.3f;
        float gridY = unit * 9;
        float buttonTop = gridY - cellSize;
        float buttonLeft = gridX - cellSize;
        grid = new Grid(gridX, gridY, cellSize);
        buttons[0] = new GuiButton('1', this, buttonLeft + cellSize*1, buttonTop, cellSize);
        buttons[1] = new GuiButton('2', this, buttonLeft + cellSize*2, buttonTop, cellSize);
        buttons[2] = new GuiButton('3', this, buttonLeft + cellSize*3, buttonTop, cellSize);
        buttons[3] = new GuiButton('4', this, buttonLeft + cellSize*4, buttonTop, cellSize);
        buttons[4] = new GuiButton('5', this, buttonLeft + cellSize*5, buttonTop, cellSize);
        buttons[5] = new GuiButton('A', this, buttonLeft, buttonTop + cellSize*1, cellSize);
        buttons[6] = new GuiButton('B', this, buttonLeft, buttonTop + cellSize*2, cellSize);
        buttons[7] = new GuiButton('C', this, buttonLeft, buttonTop + cellSize*3, cellSize);
        buttons[8] = new GuiButton('D', this, buttonLeft, buttonTop + cellSize*4, cellSize);
        buttons[9] = new GuiButton('E', this, buttonLeft, buttonTop + cellSize*5, cellSize);
        float scoreFontSize = MainActivity.findThePerfectFontSize(h/25f);
        xScorePaint.setTextSize(scoreFontSize);
        oScorePaint.setTextSize(scoreFontSize);

        timer.subscribe(this);
    }

    /**
     * Handles all touch events.
     * @param m an object that contains the (x,y) coordinates
     *          of the touch event (among other things)
     * @return true, always. (Just like the Church!)
     */
    @Override
    public boolean onTouchEvent(MotionEvent m) {

        //ignore touch events if the View is not fully initialized
        if (grid == null || firstRun) return true;

        //ignore touch events if there are any "moving" tokens on-screen.
        if (GuiToken.anyMoving()) return true;

        float x = m.getX();
        float y = m.getY();
        if (m.getAction() == MotionEvent.ACTION_DOWN) {
            //Main.say("finger down!");
            currentButton = null;
            for (GuiButton b : buttons) {
                if (b.contains(x, y)) {
                    currentButton = b;
                    b.press();
                    break;
                }
            }

            //show a helpful hint if the user taps inside the grid
            if (currentButton == null) {
                Toast t = Toast.makeText(getContext(),
                        "To play, touch one of the buttons next to the grid.",
                        Toast.LENGTH_LONG);
                t.setGravity(Gravity.TOP, 0, 0);
                t.show();
            }

        } else if (m.getAction() == MotionEvent.ACTION_MOVE) {
            boolean touchingAButton = false;
            for (GuiButton b : buttons) {
                if (b.contains(x, y)) {
                    touchingAButton = true;
                    if (currentButton != null && b != currentButton) {
                        currentButton.release();
                        currentButton = null;
                        break;
                    }
                }
            }
            if (!touchingAButton) {
                unselectAllButtons();
            }
        } else if (m.getAction() == MotionEvent.ACTION_UP) {
            for (GuiButton b : buttons) {
                if (b.contains(x, y)) {
                    if (b == currentButton) {
                        k = b;
                        currentButton.release();
                        cleanupFallenTokens();
                        handleButtonPress();
                    }
                }
            }
            currentButton = null;
        }
        return true;
    }

    private GuiToken getTokenAt(char row, char col) {
        for (GuiToken gt : tokens) {
            if (gt.matches(row, col)) {
                return gt;
            }
        }
        return null;
    }

    private void unselectAllButtons() {
        for (GuiButton b : buttons) {
            b.release();
        }
    }

    @Override
    public void onTick() {
        if (!GuiToken.anyMoving()) {
            Player winner = engine.checkForWin();
            if (winner != Player.BLANK) {
                timer.pause();
                String message;
                if (winner == Player.X) {
                    message = "Banana wins!";
                    xScore++;
                } else if (winner == Player.O) {
                    message = "Pineapple wins!";
                    oScore++;
                } else {
                    message = "It's a tie!";
                }
                AlertDialog.Builder ab = new AlertDialog.Builder(getContext());
                ab.setMessage(message)
                        .setTitle("GAME OVER!")
                        .setCancelable(false)
                        .setPositiveButton("Play again", (dialog,  which) -> reset())
                        .setNegativeButton("Quit", (dialog, which) -> ((Activity)getContext()).finish());
                AlertDialog box = ab.create();
                box.show();
            }else{
                if (gameMode == GameMode.ONE_PLAYER && engine.getCurrentPlayer() == Player.X){
                    computerRun();
                }
            }
        }
        invalidate();
    }

    private void reset() {
        engine.clear();
        tokens.clear();
        timer.deregisterAll();
        timer.subscribe(this);
        timer.restart();

        setImageResource(backgrounds[(int) (Math.random() * 3)]);
        setScaleType(ScaleType.FIT_XY);
        invalidate();
    }


    private void cleanupFallenTokens() {
        List<GuiToken> doomed = new ArrayList<GuiToken>();
        for (GuiToken t : tokens) {
            if (t.isInvisible(getHeight())) {
                doomed.add(t);
            }
        }
        for (GuiToken d : doomed) {
            tokens.remove(d);
            timer.unsubscribe(d);
        }
    }

    private void computerRun(){
        class comTask extends AsyncTask<Void, Void, Void>{
            @Override
            protected Void doInBackground(Void... args){
                return null;

            }

            @Override
            protected void onPostExecute(Void rien){
                int max = 9;
                int min = 1;
                int range = max - min + 1;
                int rand = (int)(Math.random() * range) + min;
                if (k == null) {
                    currentButton = buttons[rand];
                }else{
                    currentButton = k;
                }

                handleButtonPress();

            }
        }
        new comTask().execute();
    }

    private void handleButtonPress(){
        char label = currentButton.getLabel();
        engine.submitMove(label);
        GuiToken tok = new GuiToken(engine.getCurrentPlayer(), currentButton, getResources());
        tokens.add(tok);
        timer.subscribe(tok);

        List<GuiToken> neighbors = new ArrayList<GuiToken>();
        neighbors.add(tok);
        if (currentButton.isTopButton()) {
            //we're moving down
            for (char row = 'A'; row <= 'E'; row++) {
                GuiToken tokenInColumn = getTokenAt(row, label);
                if (tokenInColumn != null) {
                    neighbors.add(tokenInColumn);
                } else {
                    break;
                }
            }
            for (GuiToken t : neighbors) {
                t.moveDown();
            }
        }
        else {
            //we're moving right
            for (char col = '1'; col <= '5'; col++) {
                GuiToken tokenInRow = getTokenAt(label, col);
                if (tokenInRow != null) {
                    neighbors.add(tokenInRow);
                } else {
                    break;
                }
            }
            for (GuiToken t : neighbors) {
                t.moveRight();
            }
        }
    }

//    private void startMessage(){
//        AlertDialog.Builder ab = new AlertDialog.Builder(getContext());
//        ab.setMessage("Choose")
//                .setTitle("Play Mode Selection")
//                .setCancelable(false)
//                .setPositiveButton("One Player", (dialog,  which) -> gameMode = GameMode.ONE_PLAYER)
//                .setNegativeButton("Two Player", (dialog, which) -> gameMode = GameMode.TWO_PLAYER);
//        AlertDialog box = ab.create();
//        box.show();
//    }


}

