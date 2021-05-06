package edu.byuh.cis.cs203.slide203endgame.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import edu.byuh.cis.cs203.slide203endgame.R;

public class SplashActivity extends Activity {
    private ImageView sv;

    @Override
    protected void onCreate(Bundle b){
        super.onCreate(b);
        sv = new ImageView(this);
        sv.setImageResource(R.drawable.splash);
        sv.setScaleType(ImageView.ScaleType.FIT_XY);
        setContentView(sv);
    }
    /**
     * 
    This part is splash screen
    You can choose one, two player mode, info, setting buttons

     */

    @Override
    public boolean onTouchEvent(MotionEvent m){
        if (m.getAction() == MotionEvent.ACTION_UP){
            float w = sv.getWidth();
            float h = sv.getHeight();
            RectF OnePlayerMode = new RectF(0, h*(5f/6f), w*(3f/5f), h*1.1f);
            RectF TwoPlayerMode = new RectF(w*(3f/5f), h*(5f/6f), w, h*1.1f);
            RectF prefsButton = new RectF(w*(226f/300f), 0, w, h*(64f/512f));
            RectF aboutButton = new RectF(0,0,w*(200f/500f),h*(150f/512f));
            float x = m.getX();
            float y = m.getY();
            if (OnePlayerMode.contains(x,y)){
                Intent game = new Intent(this,MainActivity.class);
                game.putExtra("playermode","one");
                startActivity(game);
            }else if(TwoPlayerMode.contains(x,y)){
                Intent game = new Intent(this,MainActivity.class);
                game.putExtra("playermode","two");
                startActivity(game);
            }else if(aboutButton.contains(x,y)){
                AlertDialog.Builder ab = new AlertDialog.Builder(this);
                ab.setTitle("Info")
                        .setNeutralButton("Play this cool game", (dialog, which) -> {
                            Log.d("CIS","Cool");});
                AlertDialog box = ab.create();
                box.show();

            }
        }
        return true;
    }



}
