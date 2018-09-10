package baqui88.game;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;

import baqui88.widget.FButton;
import baqui88.widget.TouchImageView;
import baqui88.widget.CountDownTimer;

public class MainGameActivity extends AppCompatActivity {
    TextView mission_tv, target_tv, time_tv;
    TouchImageView topImage, bottomImage;
    ImageView top_wrong_tick, bottom_wrong_tick, target_hint_im;
    FButton  hintButton ;

    Bitmap topBitmap, bottomBitmap, layerBitmap;
    Canvas topCanvas, bottomCanvas;
    final Paint paint = new Paint(); // config for canvas

    SingleMission currentMission;
    int timeValue; // run-time for mission
    int target; // number of different points left
    int level = 1;
    CountDownTimer missionTimer, hintTimer;

    // restrict number of missions in each difficulty in a play turn
    int NUMBER_EASY_GAMES = 1;
    int NUMBER_MEDIUM_GAMES = 1;
    int NUMBER_HARD_GAMES = 1;

    public boolean onHintAnimation = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_main);

        mission_tv = (TextView) findViewById(R.id.mission_tv);
        target_tv = (TextView) findViewById(R.id.target_tv);
        time_tv = (TextView) findViewById(R.id.time_tv);
        topImage = (TouchImageView) findViewById(R.id.topImage);
        bottomImage = (TouchImageView) findViewById(R.id.bottomImage);
        top_wrong_tick = (ImageView) findViewById(R.id.top_x);
        bottom_wrong_tick = (ImageView) findViewById(R.id.bot_x);
        target_hint_im = (ImageView) findViewById(R.id.target_hint_im);
        top_wrong_tick.setVisibility(ImageView.INVISIBLE);
        bottom_wrong_tick.setVisibility(ImageView.INVISIBLE);
        target_hint_im.setVisibility(View.INVISIBLE);
        hintButton = (FButton) findViewById(R.id.hint_bt);

        time_tv.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/digital-7.regular.ttf"));
        hintButton.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Sketch_Block.ttf"));
        paint.setAntiAlias(true);
        paint.setColor(0xCC0000FF);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);

        MissionDatabase.getMissionsFromFile("missions.json", this);

        //Now we gonna shuffle the elements of the list so that we will get mission at specific difficulty randomly
        Collections.shuffle(MissionDatabase.EasyMissions);
        Collections.shuffle(MissionDatabase.MediumMissions);
        Collections.shuffle(MissionDatabase.HardMissions);

        //set current (first) mission and states
        updateQueue();

        ///////
        // Each time move or scroll image view, same action will be also applied to other too
        topImage.setOnTouchImageViewListener(new TouchImageView.OnTouchImageViewListener() {
            @Override
            public void before() {
                if (onHintAnimation && topImage.getState() == TouchImageView.State.AFTER_DOUBLE_TAP_ZOOM)
                    target_hint_im.clearAnimation(); // it will call onAnimationEnd()
            }
            @Override
            public void onMove() {
                bottomImage.setZoom(topImage);
            }
        });
        bottomImage.setOnTouchImageViewListener(new TouchImageView.OnTouchImageViewListener() {
            @Override
            public void before() {
                if (onHintAnimation && topImage.getState() == TouchImageView.State.AFTER_DOUBLE_TAP_ZOOM)
                    target_hint_im.clearAnimation(); // it will call onAnimationEnd()
            }
            @Override
            public void onMove() {
                topImage.setZoom(bottomImage);
            }
        });

        ///////
        // Touch image event
        topImage.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return singleTapImageAction(e, top_wrong_tick);
            }
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return false;
            }
            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        });

        bottomImage.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return singleTapImageAction(e, bottom_wrong_tick);
            }
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return false;
            }
            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        });

        ///////////// HINT LOADING /////////////
        hintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                directToHint();
                hintLoading(10*1000,100);
            }
        });
    }

    // move to next scene
    public void updateQueue() {
        if (level <= NUMBER_EASY_GAMES)
            currentMission = MissionDatabase.get(level-1, 1);
        else if (level <= NUMBER_EASY_GAMES + NUMBER_MEDIUM_GAMES)
            currentMission = MissionDatabase.get(level-NUMBER_EASY_GAMES-1, 2);
        else
            currentMission = MissionDatabase.get(level-NUMBER_EASY_GAMES-NUMBER_MEDIUM_GAMES-1,3);

        // set values for TextViews
        target = currentMission.target;
        timeValue = currentMission.time;
        mission_tv.setText(String.valueOf(level));
        target_tv.setText(String.valueOf(target));

        //just set timer for next mission
        missionTimer = new CountDownTimer(timeValue * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                time_tv.setText(mls2str(timeValue));
                //With each iteration decrement the time by 1 sec
                timeValue -= 1;
            }

            //Now user is out of time
            public void onFinish() {
                //We will navigate him to the time up activity using below method
                timeUp();
            }
        }.start();

        //// CANVAS AND BITMAP

        topBitmap = AssetReader.getBitmapFromAssets(this, currentMission.getPathInAssets(), true);
        bottomBitmap = AssetReader.getBitmapFromAssets(this, currentMission.getMirrorPathInAssets(), true);
        layerBitmap = AssetReader.getBitmapFromAssets(this, currentMission.getLayerPathInAssets(), true);
        topCanvas = new Canvas(topBitmap);
        bottomCanvas = new Canvas(bottomBitmap);

        // set background for image view
        topImage.resetZoom();
        bottomImage.resetZoom();
        topImage.setImageBitmap(topBitmap);
        bottomImage.setImageBitmap(bottomBitmap);
    }

    public boolean singleTapImageAction(MotionEvent e, ImageView wrong_tick){
        PointF pixel = topImage.transformCoordTouchToBitmap(e.getX(), e.getY(), true);
        // check wrong touch
        if (layerBitmap.getPixel((int) pixel.x, (int)pixel.y) == Color.TRANSPARENT){
            flashWrongTick(wrong_tick, (int) e.getX(), (int)e.getY(), 200);
            return true;
        }
        Rect block = SingleMission.getBlock(pixel.x, pixel.y, layerBitmap);
        if (block == null)  // pixel was checked
            return false;

        // draw canvas
        // it doesn't result in bitmap, just paint over image via canvas layer
        topCanvas.drawRect(block, paint);
        bottomCanvas.drawRect(block, paint);
        // update view
        topImage.setImageBitmap(topBitmap);
        bottomImage.setImageBitmap(bottomBitmap);

        target -= 1;
        // mark checked block by changing pixel to a unique value
        for (int x = block.left; x <= block.right; x++)
            for (int y = block.top; y <= block.bottom; y++)
                layerBitmap.setPixel(x, y,SingleMission.CHECKED_BLOCK);

        target_tv.setText(String.valueOf(target));
        if (target == 0){
            // HANDLE WHEN MISSION END
            disableImage();
            if (level == NUMBER_EASY_GAMES + NUMBER_MEDIUM_GAMES + NUMBER_HARD_GAMES)
                gameWon();
            else
                nextMissionDialog();
        }
        return true;
    }

    // show and place view center at point (x, y) in parent FrameLayout
    public void placeViewInFrameLayout(View v, int x, int y)
    {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) v.getLayoutParams();
        // v must be measured in layout , unless getWidth() and getHeight() will return 0
        int left = x - v.getWidth()/2;
        int top = y - v.getHeight()/2;
        layoutParams.setMargins(left, top, 0,0);
        v.setLayoutParams(layoutParams);
        v.setVisibility(View.VISIBLE);
    }

    public void flashWrongTick(final ImageView wrong_tick, int x, int y, int duration){
        // place tick at touch point
        placeViewInFrameLayout(wrong_tick, x, y);
        // https://stackoverflow.com/questions/1520887/how-to-pause-sleep-thread-or-process-in-android
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                wrong_tick.setVisibility(ImageView.GONE);
            }
        }, duration);
    }

    public void directToHint(){
        Rect r = SingleMission.hint(layerBitmap);
        if (r == null)
            return;
        PointF focusPixel = new PointF();
        focusPixel.set((r.left + r.right)/2, (r.top+ r.bottom)/2);
        topImage.performZoomAtPixelWithAnimation(focusPixel.x, focusPixel.y);
        // bottomImage.setZoom(topImage); // onMove() did it

        // show target_hint_im and place it at touch point
        PointF touch = topImage.transformCoordBitmapToTouch(focusPixel.x, focusPixel.y);
        placeViewInFrameLayout(target_hint_im, (int)touch.x, (int)touch.y);
        // show scale animation while view is visible
        scaleViewAnimation(target_hint_im, 0.5f, 2f);
    }

    public void scaleViewAnimation(final View v, float startScale, float endScale) {
        onHintAnimation = true;
        if (v.getAnimation() != null) {
            v.getAnimation().start();
            return;
        }
        // SET UP ANIMATION
        Animation anim = new ScaleAnimation(
                startScale, endScale, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
        // anim.setFillAfter(false); // Needed to keep the result of the animation
        anim.setDuration(1000);
        anim.setRepeatCount(2);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.GONE);
                onHintAnimation = false;
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(anim);
    }

    public void hintLoading(final int milis, final int interval){
        final int step = milis / interval;
        final int w = 200;
        final int h = 40;

        hintButton.setEnabled(false);
        final Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(ContextCompat.getColor(this, R.color.clouds));

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(ContextCompat.getColor(getApplicationContext(), R.color.silver));
        paint.setStyle(Paint.Style.FILL);

        hintTimer = new CountDownTimer(milis, interval) {
            int i = 0;
            @Override
            public void onTick(long millisUntilFinished) {
                float start = w * i/step;
                float end = w * (i + 1)/step - 1;
                if (end > w-1) {
                    end = w - 1;
                    if (start > end)
                        return;
                }
                canvas.drawRect(start, 0, end-start+1, h-1, paint);
                hintButton.setBackground(new BitmapDrawable(getResources(), bitmap));
                i++;
            }

            @Override
            public void onFinish() {
                hintButton.setEnabled(true);
                hintButton.setButtonColor(ContextCompat.getColor(getApplicationContext(), R.color.silver));
            }
        }.start();
    }

    //If user press home button and come in the game from memory then this
    //method will continue the timer from the previous time it left
    @Override
    protected void onRestart() {
        super.onRestart();
        missionTimer.resume();
        if (hintTimer != null)
            hintTimer.resume();

    }

    //When activity is destroyed then this will cancel the timer
    @Override
    protected void onStop() {
        super.onStop();
        missionTimer.pause();
        if (hintTimer != null)
            hintTimer.pause();
    }

    //This will pause the timer
    @Override
    protected void onPause() {
        super.onPause();
        missionTimer.pause();
        if (hintTimer != null)
            hintTimer.pause();
    }

    //On BackPressed
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);
        finish();
    }
    //This method will navigate from current activity to GameWon
    public void gameWon() {
        Intent intent = new Intent(this, GameWon.class);
        startActivity(intent);
        finish();
    }

    //This method is called when time is up
    //this method will navigate user to the activity Time_Up
    public void timeUp() {
        Intent intent = new Intent(this, Time_Up.class);
        startActivity(intent);
        finish();
    }

    //This dialog is show when current mission done
    public void nextMissionDialog() {
        final Dialog nextMissionDialog = new Dialog(MainGameActivity.this);
        nextMissionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (nextMissionDialog.getWindow() != null) {
            ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
            nextMissionDialog.getWindow().setBackgroundDrawable(colorDrawable);
        }
        nextMissionDialog.setContentView(R.layout.dialog_next_mission);
        nextMissionDialog.setCancelable(false);
        nextMissionDialog.show();
        //Since the dialog is show to user just pause the timer in background
        onPause();

        TextView doneText = (TextView) nextMissionDialog.findViewById(R.id.well_done_tv);
        FButton buttonNext = (FButton) nextMissionDialog.findViewById(R.id.Next_bt);
        Typeface sb = Typeface.createFromAsset(getAssets(), "fonts/shablagooital.ttf");
        doneText.setTypeface(sb);
        buttonNext.setTypeface(sb);

        //OnCLick listener to go next que
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextMissionDialog.dismiss(); // dismiss dialog
                // set up next level
                level++ ;
                updateQueue();
                enableImage();
                if (hintTimer != null)
                    hintTimer.resume();
            }
        });
    }

    //This method will disable all the option button
    public void disableImage() {
        topImage.setFocusable(false);
        bottomImage.setFocusable(false);
    }

    //This method will all enable the option buttons
    public void enableImage() {
        topImage.setFocusable(true);
        bottomImage.setFocusable(true);
    }

    public String mls2str(int timeValue){
        int minutes = timeValue/60;
        int seconds = timeValue%60;
        String mString, secString;
        if (minutes < 10)
            mString = "0"+ minutes;
        else
            mString = "" + minutes;

        if (seconds < 10)
            secString = "0"+ seconds;
        else
            secString = "" + seconds;

        return mString + ":" + secString;
    }
}
