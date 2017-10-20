package cn.jiguang.imui.messages;


import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.LinkedHashMap;

import cn.jiguang.imui.R;


public class ViewHolderController {

    public static final int PLAY_TYPE_VOICE = 0x1;
    public static final int PLAY_TYPE_TEXT = 0x2;

    private static ViewHolderController mInstance = new ViewHolderController();
    private HashMap<Integer, ImageView> mData = new LinkedHashMap<>();
    private int mLastPlayPosition = -1;
    private int mLastPlayType = -1;

    private ViewHolderController() {

    }

    public static ViewHolderController getInstance() {
        return mInstance;
    }

    public void addView(int position, ImageView view) {
        mData.put(position, view);
    }

    public void setLastPlayPosition(int position) {
        mLastPlayPosition = position;
    }

    public int getLastPlayPosition() {
        return mLastPlayPosition;
    }

    public void setLastPlayType(int mLastPlayType) {
        this.mLastPlayType = mLastPlayType;
    }

    /**
     *
     * TAG 语音消息的 图片有 aurora_anim_send_voice 和 aurora_anim_receive_voice
     * TAG 但是因为只能发语音，所以就直接设置为 aurora_anim_send_voice
     */
    public void notifyAnimStop() {
        ImageView imageView = mData.get(mLastPlayPosition);
        if (imageView != null) {
            if (PLAY_TYPE_VOICE == mLastPlayType) {
                try {
                    AnimationDrawable anim = (AnimationDrawable) imageView.getDrawable();
                    anim.stop();
                    imageView.setImageResource(R.drawable.aurora_sendvoice_send_3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (PLAY_TYPE_TEXT == mLastPlayType) {
                imageView.setBackgroundResource(R.drawable.icon_common_play);
            }
        }

    }

    public void remove(int position) {
        if (mData.size() > 0) {
            mData.remove(position);
        }
    }

    public void release() {
        mData.clear();
        mData = null;
    }

}
