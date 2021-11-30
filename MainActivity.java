package com.example.musicplayerdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private TextView songName,singerName;
    private ImageView diskImage;
    private TextView lyricPrev, lyricCurrent,lyricNext;
    private SeekBar musicProgress;
    private TextView currentTime,totalTime;
    private ImageButton prevButton,playButton,nextButton;
    private ObjectAnimator animator;

    private MediaPlayer player;
    private int currentPlaying = 0;// as ArrayList down point ,as playing music now
    private ArrayList<Integer> playList = new ArrayList<>();


    private boolean isPausing = false,isPlaying = false;//music is pausing ,music is the first playing then change ture


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        preparePlaylist();
        TimerTask timerTask =new TimerTask() {
            @Override
            public void run() {
                if(isPlaying){
                    updateTimer();
                }
            }
        };
        new Timer().scheduleAtFixedRate(timerTask,0,500);
    }
    void init(){
        songName = findViewById(R.id.tv_song_name);
        singerName = findViewById(R.id.tv_song_singer);
        diskImage = findViewById(R.id.song_disk);
        lyricPrev = findViewById(R.id.song_lyric_previous);
        lyricCurrent = findViewById(R.id.song_lyric_current);
        lyricNext =  findViewById(R.id.song_lyric_next);
        musicProgress = findViewById(R.id.seek_bar_progress);
        currentTime = findViewById(R.id.tv_progress_current);
        totalTime = findViewById(R.id.tv_progress_total);
        prevButton = findViewById(R.id.button_play_perv);
        playButton = findViewById(R.id.button_play_pause);
        nextButton = findViewById(R.id.button_play_next);


        OnClickControl onClick = new OnClickControl();
        prevButton.setOnClickListener(onClick);
        playButton.setOnClickListener(onClick);
        nextButton.setOnClickListener(onClick);


        OnseekBarchangeControl onseekBarchangeControl = new OnseekBarchangeControl();
        musicProgress.setOnSeekBarChangeListener(onseekBarchangeControl);


        animator  = ObjectAnimator.ofFloat(diskImage,"rotation",0,360.0F); // init
        animator.setDuration(10000);//setting  rotation 10s
        animator.setInterpolator(new LinearInterpolator());//time function,have many classics
        animator.setRepeatCount(-1);//cycling always


    }

    private void preparePlaylist(){
        Field[] fields = R.raw.class.getFields();
        for (int count = 0;count < fields.length;count++){
            Log.i("Raw Asset", fields[count].getName());
            try{
                    int resId = fields[count].getInt(fields[count]);
                    playList.add(resId);
                }catch (IllegalAccessException e){
                    e.printStackTrace();
                    }
            }
     }

     private void prepareMedia(){
        if(isPlaying){
            player.start();
            player.reset();
        }
        player = MediaPlayer.create(getApplicationContext(),playList.get(currentPlaying));
        int musicDuration  =player.getDuration();
        musicProgress.setMax(musicDuration);
        int sec = musicDuration/1000;
        int min = sec / 60;
        sec -=  min * 60;
        String musicTime  = String.format("%02d:%02d",min,sec);
        totalTime.setText(musicTime);
        player.start();
     }



     private void updateTimer(){
        runOnUiThread(()->{
            int currentMs  = player.getCurrentPosition();
            int sec = currentMs/1000;
            int min = sec/60;
            sec -= min*60;
            String time  = String.format("%02d:%02d",min,sec);
            musicProgress.setProgress(currentMs);
            currentTime.setText(time);
        });
     }


    private class OnClickControl implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.button_play_perv:
                    Log.i("INFO", "onClick: Replay button having been clicked!");
                    playButton.setImageResource(R.drawable.zanting);//change pause button to start image
                    animator.start();
                    if(!player.isPlaying()){
                        currentPlaying = (--currentPlaying)%playList.size();
                    }
                    //replay music  // forward music
                    prepareMedia();
                    isPausing = false;
                    isPlaying = true;
                    break;
                case R.id.button_play_pause:
                    Log.i("INFO", "onClick: Pause button having been clicked!");
                    //starting pause
                    if(!isPausing && !isPlaying){//pausing  and the first playing
                        playButton.setImageResource(R.drawable.zanting);//change  pause button image
                        animator.start();
                        prepareMedia();
                        isPlaying = true;
                        //start playing music
                    }else if (isPausing && isPlaying){ //pausing and has been played
                        playButton.setImageResource(R.drawable.zanting);//change pause button image
                        animator.resume();
                        player.start();
                        //go along playing music
                    }else{ // playing
                        playButton.setImageResource(R.drawable.jixu);//change pause button to start image
                        animator.pause();
                        player.pause();
                        //pausing music
                    }
                    isPausing = !isPausing;// change pausing  or playing
                    // isPlaying = true;
                    break;
                case R.id.button_play_next:
                    Log.i("INFO", "onClick: Next button having been clicked!");
                    playButton.setImageResource(R.drawable.zanting); // change button to pause
                    currentPlaying = (++currentPlaying)%playList.size();
                    prepareMedia();
                    animator.start();
                    isPausing = false;
                    isPlaying = true;
                    //next music
                    break;
                default:
                    Log.i("INFO", "onClick: Button having been clicked,but have some bugs!");
                    //having some bugs;
            }
        }
    }
    private class OnseekBarchangeControl implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser){
                player.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            player.pause();
            animator.pause();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            player.start();
            if(seekBar.getProgress() < 10){
                animator.start();
            }else{
                animator.resume();
            }
        }
    }
}