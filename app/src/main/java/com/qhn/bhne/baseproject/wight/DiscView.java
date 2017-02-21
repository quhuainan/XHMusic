package com.qhn.bhne.baseproject.wight;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.util.LogTime;
import com.qhn.bhne.baseproject.R;
import com.qhn.bhne.baseproject.mvp.entity.MusicData;
import com.qhn.bhne.baseproject.utils.DisplayUtil;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.log.LoggerDefault;

/**
 * Created by AchillesL on 2016/11/15.
 */

public class DiscView extends RelativeLayout {
    //盘针
    private ImageView mIvNeedle;
    //页面切换viewpager
    private ViewPager mVpContain;
    //页面切换viewpager的适配器
    private ViewPagerAdapter mViewPagerAdapter;
    //盘针动画
    private ObjectAnimator mNeedleAnimator;
    //唱盘中唱片的数据源
    private List<View> mDiscLayouts = new ArrayList<>();

    private List<MusicData> mMusicDatas = new ArrayList<>();

    //动画集合
    private List<ObjectAnimator> mDiscAnimators = new ArrayList<>();

    /*标记ViewPager是否处于偏移的状态*/
    private boolean mViewPagerIsOffset = false;

    /*标记唱针复位后，是否需要重新偏移到唱片处*/
    private boolean mIsNeed2StartPlayAnimator = false;

    //音乐默认状态
    private MusicStatus musicStatus = MusicStatus.STOP;

    //唱针默认动画时长
    public static final int DURATION_NEEDLE_ANIMATOR = 500;

    //唱针动画状态
    private NeedleAnimatorStatus needleAnimatorStatus = NeedleAnimatorStatus.IN_FAR_END;

    //对外部暴露的接口，用于动态改变唱片的信息
    private IPlayInfo mIPlayInfo;

    //屏幕的宽和高
    private int mScreenWidth, mScreenHeight;

    /*唱针当前所处的状态*/
    private enum NeedleAnimatorStatus {
        /*移动时：从唱盘往远处移动*/
        TO_FAR_END,
        /*移动时：从远处往唱盘移动*/
        TO_NEAR_END,
        /*静止时：离开唱盘*/
        IN_FAR_END,
        /*静止时：贴近唱盘*/
        IN_NEAR_END
    }

    /*音乐当前的状态：只有播放、暂停、停止三种*/
    public enum MusicStatus {
        PLAY, PAUSE, STOP
    }

    /*DiscView需要触发的音乐切换状态：播放、暂停、上/下一首、停止*/
    public enum MusicChangedStatus {
        PLAY, PAUSE, NEXT, LAST, STOP
    }

    public interface IPlayInfo {
        /*用于更新标题栏变化*/
        void onMusicInfoChanged(String musicName, String musicAuthor);

        /*用于更新背景图片*/
        void onMusicPicChanged(int musicPicRes);

        /*用于更新音乐播放状态*/
        void onMusicChanged(MusicChangedStatus musicChangedStatus);
    }

    private static final String TAG = "DiscView";
    public DiscView(Context context) {
        this(context, null);
    }

    public DiscView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DiscView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScreenWidth = DisplayUtil.getScreenWidth(context);
        mScreenHeight = DisplayUtil.getScreenHeight(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initDiscBackground();
        initViewPager();
        initNeedle();
        initObjectAnimator();
    }

    //初始化底盘
    private void initDiscBackground() {
        ImageView mDiscBackground = (ImageView) findViewById(R.id.ivDiscBlackgound);
        mDiscBackground.setImageDrawable(getDiscBackgroundDrawable());
        //动态获取背景图片的对应的位置
        int marginTop = (int) (DisplayUtil.SCALE_DISC_MARGIN_TOP * mScreenHeight);
        LayoutParams layoutParams = (LayoutParams) mDiscBackground
                .getLayoutParams();
        layoutParams.setMargins(0, marginTop, 0, 0);

        mDiscBackground.setLayoutParams(layoutParams);
    }

    //将底盘变成圆形
    private Drawable getDiscBackgroundDrawable() {

        //根据当前屏幕的大小获取对应比例的底盘尺寸
        int discSize = (int) (mScreenWidth * DisplayUtil.SCALE_DISC_SIZE);

        //获取圆盘背景的bitmap对象
        Bitmap bitmapDisc = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R
                .drawable.ic_disc_blackground), discSize, discSize, false);

        //将Bitmap转化成圆形drawable对象
        RoundedBitmapDrawable roundDiscDrawable = RoundedBitmapDrawableFactory.create
                (getResources(), bitmapDisc);
        return roundDiscDrawable;
    }

    //初始化viewpager
    private void initViewPager() {
        mViewPagerAdapter = new ViewPagerAdapter();
        mVpContain = (ViewPager) findViewById(R.id.vpDiscContain);
        //取消滑动结束时出现颜色渐变
        mVpContain.setOverScrollMode(View.OVER_SCROLL_NEVER);

        mVpContain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int lastPositionOffsetPixels = 0;
            int currentItem = 0;

            //当页面滚动时回调 position当前页面所处的位置，positionOffset 偏移的比例，positionOffsetPixels 偏移的像素点
            @Override
            public void onPageScrolled(int position, float positionOffset, int
                    positionOffsetPixels) {
                //手指向左滑
                if (lastPositionOffsetPixels > positionOffsetPixels) {
                    if (positionOffset < 0.5) {//滑动幅度小于
                        notifyMusicInfoChanged(position);
                    } else {
                        notifyMusicInfoChanged(mVpContain.getCurrentItem());
                    }
                }
                //右滑
                else if (lastPositionOffsetPixels < positionOffsetPixels) {
                    if (positionOffset > 0.5) {
                        notifyMusicInfoChanged(position + 1);
                    } else {
                        notifyMusicInfoChanged(position);
                    }
                }
                //记录上一次滑动时的所处的像素点，多次滑动时有用
                lastPositionOffsetPixels = positionOffsetPixels;
            }

            //当滑动到新页面时回调
            @Override
            public void onPageSelected(int position) {
                resetOtherDiscAnimation(position);
                notifyMusicPicChanged(position);
                if (position > currentItem) {
                    notifyMusicStatusChanged(MusicChangedStatus.NEXT);
                } else {
                    notifyMusicStatusChanged(MusicChangedStatus.LAST);
                }
                currentItem = position;
            }

            //当滑动状态改变时回调
            @Override
            public void onPageScrollStateChanged(int state) {
                doWithAnimatorOnPageScroll(state);
            }
        });
        mVpContain.setAdapter(mViewPagerAdapter);

        //动态设置viewpager的参数
        LayoutParams layoutParams = (LayoutParams) mVpContain.getLayoutParams();
        int marginTop = (int) (DisplayUtil.SCALE_DISC_MARGIN_TOP * mScreenHeight);
        layoutParams.setMargins(0, marginTop, 0, 0);
        mVpContain.setLayoutParams(layoutParams);
    }

    //取消其他页面上的动画，并将图片旋转角度复原
    private void resetOtherDiscAnimation(int position) {
        for (int i = 0; i < mDiscLayouts.size(); i++) {
            if (position == i) continue;
            mDiscAnimators.get(position).cancel();
            ImageView imageView = (ImageView) mDiscLayouts.get(i).findViewById(R.id.ivDisc);
            imageView.setRotation(0);
        }
    }

    //跟随滑动状态来改变toolbar上的信息
    public void notifyMusicInfoChanged(int position) {
        if (mIPlayInfo != null) {
            //需要变动的地方
            MusicData musicData = mMusicDatas.get(position);
            mIPlayInfo.onMusicInfoChanged(musicData.getMusicName(), musicData.getMusicAuthor());
        }
    }

    //动态设置指针的位置以及偏移角度
    private void initNeedle() {
        mIvNeedle = (ImageView) findViewById(R.id.ivNeedle);
        //根据屏幕的宽高来确定指针的宽高
        int needleWidth = (int) (DisplayUtil.SCALE_NEEDLE_WIDTH * mScreenWidth);

        int needleHeight = (int) (DisplayUtil.SCALE_NEEDLE_HEIGHT * mScreenHeight);

        //设置手柄的外边距为负数，让其隐藏一部分，将marginTop设置为负值即可使唱针
        int marginTop = (int) (DisplayUtil.SCALE_NEEDLE_MARGIN_TOP * mScreenHeight) * -1;
        int marginLeft = (int) (DisplayUtil.SCALE_NEEDLE_MARGIN_LEFT * mScreenWidth);

        //重新定义唱针的大小
        Bitmap originBitmap = BitmapFactory.decodeResource(getResources(), R.drawable
                .ic_needle);
        Bitmap bitmap = Bitmap.createScaledBitmap(originBitmap, needleWidth, needleHeight, false);

        LayoutParams layoutParams = (LayoutParams) mIvNeedle.getLayoutParams();
        layoutParams.setMargins(marginLeft, marginTop, 0, 0);

        int pivotX = (int) (DisplayUtil.SCALE_NEEDLE_PIVOT_X * mScreenWidth);
        int pivotY = (int) (DisplayUtil.SCALE_NEEDLE_PIVOT_Y * mScreenWidth);

        mIvNeedle.setPivotX(pivotX);
        mIvNeedle.setPivotY(pivotY);
        mIvNeedle.setRotation(DisplayUtil.ROTATION_INIT_NEEDLE);
        mIvNeedle.setImageBitmap(bitmap);
        mIvNeedle.setLayoutParams(layoutParams);
    }

    //初始化唱针动画
    private void initObjectAnimator() {
        mNeedleAnimator = ObjectAnimator.ofFloat(mIvNeedle, View.ROTATION, DisplayUtil
                .ROTATION_INIT_NEEDLE, 0);
        mNeedleAnimator.setDuration(DURATION_NEEDLE_ANIMATOR);
        mNeedleAnimator.setInterpolator(new AccelerateInterpolator());
        mNeedleAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                //根据动画开始前NeedleAnimatorStatus的状态，即可得出动画进行时NeedleAnimatorStatus的状态

                //当指针在远处静止时，那么其动画状态就应该是移动到近处
                if (needleAnimatorStatus == NeedleAnimatorStatus.IN_FAR_END) {
                    needleAnimatorStatus = NeedleAnimatorStatus.TO_NEAR_END;
                } else if (needleAnimatorStatus == NeedleAnimatorStatus.IN_NEAR_END) {
                    needleAnimatorStatus = NeedleAnimatorStatus.TO_FAR_END;
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //指针动画结束后，重新定义指针动画状态属性并开始播放/暂停唱盘动画
                if (needleAnimatorStatus == NeedleAnimatorStatus.TO_NEAR_END) {
                    needleAnimatorStatus = NeedleAnimatorStatus.IN_NEAR_END;//唱针停在唱盘上
                    int index = mVpContain.getCurrentItem();//获取当前vp显示的位置
                    playDiscAnimator(index);//播放唱盘的动画
                    musicStatus = MusicStatus.PLAY;//音乐状态为播放
                } else if (needleAnimatorStatus == NeedleAnimatorStatus.TO_FAR_END) {
                    needleAnimatorStatus = NeedleAnimatorStatus.IN_FAR_END;
                    if (musicStatus == MusicStatus.STOP) {
                        mIsNeed2StartPlayAnimator = true;
                    }
                }

                if (mIsNeed2StartPlayAnimator) {
                    mIsNeed2StartPlayAnimator = false;

                    // 只有在ViewPager不处于偏移状态时，才开始唱盘旋转动画

                    if (!mViewPagerIsOffset) {
                        /*延时500ms*/
                        DiscView.this.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                playAnimator();
                            }
                        }, 50);
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    //播放唱盘动画
    private void playDiscAnimator(int index) {
        ObjectAnimator objectAnimator = mDiscAnimators.get(index);
        if (objectAnimator.isPaused()) {
            objectAnimator.resume();
        } else {
            objectAnimator.start();
        }

        //唱盘动画可能执行多次，只有不是音乐不在播放状态，在回调执行播放
        if (musicStatus != MusicStatus.PLAY) {
            notifyMusicStatusChanged(MusicChangedStatus.PLAY);
        }
    }

    /*暂停唱盘动画*/
    private void pauseDiscAnimator(int index) {
        ObjectAnimator objectAnimator = mDiscAnimators.get(index);
        objectAnimator.pause();
        mNeedleAnimator.reverse();
    }

    private void doWithAnimatorOnPageScroll(int state) {
        switch (state) {
            case ViewPager.SCROLL_STATE_IDLE://viewPage处于闲置状态
            case ViewPager.SCROLL_STATE_SETTLING: {//viewPage处于滑动状态但是这种滑动状态是自己控制的
                Log.d(TAG, "doWithAnimatorOnPageScroll: 滑动---自己控制");
                mViewPagerIsOffset = false;
                if (musicStatus == MusicStatus.PLAY) {
                    playAnimator();
                }
                break;
            }
            case ViewPager.SCROLL_STATE_DRAGGING: {//viewPage处于滑动状态但是这种滑动状态是外界控制的
                Log.d(TAG, "doWithAnimatorOnPageScroll: 滑动---不属于自己控制");
                mViewPagerIsOffset = true;
                pauseAnimator();
                break;
            }
        }
    }

    //注入回调接口
    public void setPlayInfoListener(IPlayInfo listener) {
        this.mIPlayInfo = listener;
    }


     // 得到唱盘图片唱盘图片由空心圆盘及音乐专辑图片“合成”得到
    private Drawable getDiscDrawable(int musicPicRes) {
        int discSize = (int) (mScreenWidth * DisplayUtil.SCALE_DISC_SIZE);
        int musicPicSize = (int) (mScreenWidth * DisplayUtil.SCALE_MUSIC_PIC_SIZE);
        //根据屏幕尺寸来设置唱片和唱盘的大小

        //唱盘
        Bitmap bitmapDisc = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R
                .mipmap.ic_disc), discSize, discSize, false);
        //唱片变成圆形
        Bitmap bitmapMusicPic = getMusicPicBitmap(musicPicSize, musicPicRes);

        BitmapDrawable discDrawable = new BitmapDrawable(bitmapDisc);

        RoundedBitmapDrawable roundMusicDrawable = RoundedBitmapDrawableFactory.create
                (getResources(), bitmapMusicPic);

        //抗锯齿
        discDrawable.setAntiAlias(true);
        roundMusicDrawable.setAntiAlias(true);

        //唱盘，唱片合体
        Drawable[] drawables = new Drawable[2];
        drawables[0] = roundMusicDrawable;
        drawables[1] = discDrawable;

        LayerDrawable layerDrawable = new LayerDrawable(drawables);
        int musicPicMargin = (int) ((DisplayUtil.SCALE_DISC_SIZE - DisplayUtil
                .SCALE_MUSIC_PIC_SIZE) * mScreenWidth / 2);
        //调整专辑图片的四周边距，让其显示在正中
        layerDrawable.setLayerInset(0, musicPicMargin, musicPicMargin, musicPicMargin,
                musicPicMargin);

        return layerDrawable;
    }

    //压缩图片资源，防止OOM
    private Bitmap getMusicPicBitmap(int musicPicSize, int musicPicRes) {

        BitmapFactory.Options options = new BitmapFactory.Options();

        //则通过BitmapFactory.decodeResource();方法获取的bitmap对象只包含其边界信息而不给其分配内存空间
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), musicPicRes, options);

        //计算采样率 注意：采样率大于1时图片才会被压缩
        int imageWidth = options.outWidth;
        int sample = imageWidth / musicPicSize;
        int dstSample = 1;
        if (sample > dstSample) {
            dstSample = sample;
        }

        options.inJustDecodeBounds = false;
        //设置图片采样率
        options.inSampleSize = dstSample;
        //设置图片解码格式
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                musicPicRes, options), musicPicSize, musicPicSize, true);
    }

    //注入数据源，初始化数据
    public void setMusicDataList(List<MusicData> musicDataList) {
        if (musicDataList.isEmpty()) return;

        mDiscLayouts.clear();
        mMusicDatas.clear();
        mDiscAnimators.clear();
        mMusicDatas.addAll(musicDataList);
        //更改数据
        for (MusicData musicData : mMusicDatas) {
            View discLayout = LayoutInflater.from(getContext()).inflate(R.layout.layout_disc,
                    mVpContain, false);

            ImageView disc = (ImageView) discLayout.findViewById(R.id.ivDisc);
            disc.setImageDrawable(getDiscDrawable(musicData.getMusicPicRes()));

            mDiscAnimators.add(getDiscObjectAnimator(disc));
            mDiscLayouts.add(discLayout);
        }
        mViewPagerAdapter.notifyDataSetChanged();

        MusicData musicData = mMusicDatas.get(0);
        if (mIPlayInfo != null) {
            mIPlayInfo.onMusicInfoChanged(musicData.getMusicName(), musicData.getMusicAuthor());
            mIPlayInfo.onMusicPicChanged(musicData.getMusicPicRes());
        }
    }

    //定义唱盘动画
    private ObjectAnimator getDiscObjectAnimator(ImageView disc) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(disc, View.ROTATION, 0, 360);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setDuration(20 * 1000);
        objectAnimator.setInterpolator(new LinearInterpolator());
        return objectAnimator;
    }

    //activity结束时需要调用的方法
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    /*播放动画*/
    private void playAnimator() {
        /*唱针处于远端时，直接播放动画*/
        if (needleAnimatorStatus == NeedleAnimatorStatus.IN_FAR_END) {
            mNeedleAnimator.start();
        }
        /*唱针处于往远端移动时，设置标记，等动画结束后再播放动画*/
        else if (needleAnimatorStatus == NeedleAnimatorStatus.TO_FAR_END) {
            mIsNeed2StartPlayAnimator = true;
        }
    }

    /*暂停动画*/
    private void pauseAnimator() {
        /*播放时暂停动画*/
        if (needleAnimatorStatus == NeedleAnimatorStatus.IN_NEAR_END) {
            int index = mVpContain.getCurrentItem();
            pauseDiscAnimator(index);
        }
        /*唱针往唱盘移动时暂停动画*/
        else if (needleAnimatorStatus == NeedleAnimatorStatus.TO_NEAR_END) {
            mNeedleAnimator.reverse();
            /**
             * 若动画在没结束时执行reverse方法，则不会执行监听器的onStart方法，此时需要手动设置
             * */
            needleAnimatorStatus = NeedleAnimatorStatus.TO_FAR_END;
        }
        /**
         * 动画可能执行多次，只有音乐处于停止 / 暂停状态时，才执行暂停命令
         * */
        if (musicStatus == MusicStatus.STOP) {
            notifyMusicStatusChanged(MusicChangedStatus.STOP);
        } else if (musicStatus == MusicStatus.PAUSE) {
            notifyMusicStatusChanged(MusicChangedStatus.PAUSE);
        }
    }


    public void notifyMusicPicChanged(int position) {
        if (mIPlayInfo != null) {
            MusicData musicData = mMusicDatas.get(position);
            mIPlayInfo.onMusicPicChanged(musicData.getMusicPicRes());
        }
    }

    public void notifyMusicStatusChanged(MusicChangedStatus musicChangedStatus) {
        if (mIPlayInfo != null) {
            mIPlayInfo.onMusicChanged(musicChangedStatus);
        }
    }

    private void play() {playAnimator();
    }


    private void pause() {
        musicStatus = MusicStatus.PAUSE;
        pauseAnimator();
    }

    public void stop() {
        musicStatus = MusicStatus.STOP;
        pauseAnimator();
    }

    public void playOrPause() {
        if (musicStatus == MusicStatus.PLAY) {
            Toast.makeText(getContext(), "暂停音乐", Toast.LENGTH_SHORT).show();

            pause();
        } else {
            play();

            Toast.makeText(getContext(), "播放音乐", Toast.LENGTH_SHORT).show();
        }
    }


    public void next() {
        int currentItem = mVpContain.getCurrentItem();
        if (currentItem == mMusicDatas.size() - 1) {
            Toast.makeText(getContext(), "已经到达最后一首", Toast.LENGTH_SHORT).show();
        } else {
            selectMusicWithButton();
            mVpContain.setCurrentItem(currentItem + 1, true);
        }
    }

    public void last() {
        int currentItem = mVpContain.getCurrentItem();
        if (currentItem == 0) {
            Toast.makeText(getContext(), "已经到达第一首", Toast.LENGTH_SHORT).show();
        } else {
            selectMusicWithButton();
            mVpContain.setCurrentItem(currentItem - 1, true);
        }
    }

    public boolean isPlaying() {
        return musicStatus == MusicStatus.PLAY;
    }

    private void selectMusicWithButton() {
        if (musicStatus == MusicStatus.PLAY) {
            mIsNeed2StartPlayAnimator = true;
            pauseAnimator();
        } else if (musicStatus == MusicStatus.PAUSE) {
            play();
        }
    }

    class ViewPagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View discLayout = mDiscLayouts.get(position);
            container.addView(discLayout);
            return discLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mDiscLayouts.get(position));
        }

        @Override
        public int getCount() {
            return mDiscLayouts.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
