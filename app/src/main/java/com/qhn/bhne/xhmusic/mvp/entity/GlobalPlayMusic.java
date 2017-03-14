package com.qhn.bhne.xhmusic.mvp.entity;


import com.qhn.bhne.xhmusic.common.MusicConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qhn
 * on 2017/2/23 0023.
 */

public class GlobalPlayMusic {


    //音乐播放列表
    private List<Songs> currentPlaySongList = new ArrayList<>();
    //播放位置
    private int playPosition = 0;
    //播放状态
   // private MusicConstants.MusicStatus musicStatus = MusicConstants.MusicStatus.STOP;
    //播放进度
    private int progress = 0;
    //当前的歌曲品质
    @MusicConstants.MusicQuality
    private int musicQuality = MusicConstants.MUSIC_QUALITY_STANDARD;

    private Boolean isPlay=false;

    public Boolean getIsPlay() {
        return isPlay;
    }

    public void setIsPlay(Boolean play) {
        isPlay = play;
    }

    public List<Songs> getCurrentPlaySongList() {
        return currentPlaySongList;
    }

    public void setCurrentPlaySongList(List<Songs> currentPlaySongList) {
        this.currentPlaySongList = currentPlaySongList;
    }

    public int getPlayPosition() {
        return playPosition;
    }

    public void setPlayPosition(int playPosition) {
        this.playPosition = playPosition;
    }

   /* public MusicConstants.MusicStatus getMusicStatus() {
        return musicStatus;
    }

    public void setMusicStatus(MusicConstants.MusicStatus musicStatus) {
        this.musicStatus = musicStatus;
    }*/

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @MusicConstants.MusicQuality
    public int getMusicQuality() {
        return musicQuality;
    }

    public void setMusicQuality(@MusicConstants.MusicQuality int musicQuality) {
        this.musicQuality = musicQuality;
    }


}