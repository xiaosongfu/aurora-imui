package cn.jiguang.imui.messages;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SynthesizerListener;

import cn.jiguang.imui.BuildConfig;
import cn.jiguang.imui.R;
import cn.jiguang.imui.commons.models.IMessage;
import cn.jiguang.imui.utils.HyperLinkParseUtil;
import cn.jiguang.imui.view.CircleImageView;

public class TxtViewHolder<MESSAGE extends IMessage>
        extends BaseMessageViewHolder<MESSAGE>
        implements MsgListAdapter.DefaultMessageViewHolder {

    protected TextView mMsgTv;
    protected TextView mDateTv;
    protected TextView mDisplayNameTv;
    protected CircleImageView mAvatarIv;
    protected ImageButton mResendIb;
    protected ImageView mPlayIb;
    protected ProgressBar mSendingPb;
    protected boolean mIsSender;
    protected ViewHolderController mController;

    public TxtViewHolder(View itemView, boolean isSender) {
        super(itemView);
        this.mIsSender = isSender;
        mMsgTv = (TextView) itemView.findViewById(R.id.aurora_tv_msgitem_message);
        mDateTv = (TextView) itemView.findViewById(R.id.aurora_tv_msgitem_date);
        mAvatarIv = (CircleImageView) itemView.findViewById(R.id.aurora_iv_msgitem_avatar);
        mDisplayNameTv = (TextView) itemView.findViewById(R.id.aurora_tv_msgitem_display_name);
        mResendIb = (ImageButton) itemView.findViewById(R.id.aurora_ib_msgitem_resend);
        mPlayIb = (ImageView) itemView.findViewById(R.id.aurora_ib_msgitem_play);
        mSendingPb = (ProgressBar) itemView.findViewById(R.id.aurora_pb_msgitem_sending);

        mMsgTv.setMovementMethod(LinkMovementMethod.getInstance());

        mController = ViewHolderController.getInstance();
    }

    @Override
    public void onBind(final MESSAGE message) {
        //防止重用 item 时候导致消息错乱
        mMsgTv.setText("");
        //参考：http://blog.csdn.net/qq_24530405/article/details/50506519
        //参考：http://m.blog.csdn.net/article/details?id=51767368
        final String text = message.getText();
        if (text == null || text.length() == 0) {
            return;
        }
        if (IMessage.MessageType.RECEIVE_TEXT == message.getType()) {
//            Object[][] output = HyperLinkParseUtil.parseLinks(text);
//            if (output == null || output.length == 0 || output[0] == null || output[0].length == 0) {
//                mMsgTv.setText(message.getText());
//            } else {
//                int urlCount = output[0].length;
//                Log.i("TxtViewHolder", "一共有" + urlCount + "个url");
//                String remainText = text;
//                int lastStart = 0;//截取到一部分后截掉部分的长度
//                for (int i = 0; i < urlCount; i++) {
//                    final String blueText = (String) output[0][i];//带下划线的文字
//                    final String url = (String) output[1][i];//下划线文字所对应的url连接
//                    int start = (int) output[2][i];//<a>标签在源字符串的起始位置
//                    int end = (int) output[3][i];//<a>标签在源字符串的结束位置
//                    SpannableString spannableString = new SpannableString(blueText);
//                    spannableString.setSpan(new ClickableSpan() {
//                        @Override
//                        public void onClick(View widget) {
//                            if (mMsgLinkClickListener != null) {
//                                mMsgLinkClickListener.onMessageLinkClick(blueText, url);
//                            }
//                        }
//                    }, 0, blueText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    // 字体颜色
//                    spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(itemView.getContext(), R.color.hyper_txt_link_color)), 0, blueText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    int subStart = start - lastStart;
//                    String front = remainText.substring(0, subStart);//截取出一段文字+一段url
//                    remainText = remainText.substring(end - lastStart, remainText.length());//剩下的部分
//                    lastStart = end;
//                    if (front.length() > 0) {
//                        mMsgTv.append(front);
//                    }
//                    mMsgTv.append(spannableString);
//                }
//                if (remainText != null && remainText.length() > 0) {
//                    mMsgTv.append(remainText);
//                }
//            }

            // 是否有 a 或 link 标签
            boolean hasALink = false;
            // step 1:先解析 a 标签
            Object[][] output = HyperLinkParseUtil.parseLinks(text);
            if (output == null || output.length == 0 || output[0] == null || output[0].length == 0) {
                // step 2:没有 a 标签，就再解析 link 标签
                output = HyperLinkParseUtil.parseAHrefLinks(text);
                if (output == null || output.length == 0 || output[0] == null || output[0].length == 0) {
                    hasALink = false;
                    mMsgTv.setText(message.getText());
                } else {
                    hasALink = true;
                }
            } else {
                hasALink = true;
            }
            if (hasALink) {
                int urlCount = output[0].length;
                Log.i("TxtViewHolder", "一共有" + urlCount + "个url");
                String remainText = text;
                int lastStart = 0;//截取到一部分后截掉部分的长度
                for (int i = 0; i < urlCount; i++) {
                    final String blueText = (String) output[0][i];//带下划线的文字
                    final String url = (String) output[1][i];//下划线文字所对应的url连接
                    int start = (int) output[2][i];//<a>标签在源字符串的起始位置
                    int end = (int) output[3][i];//<a>标签在源字符串的结束位置
                    SpannableString spannableString = new SpannableString(blueText);
                    spannableString.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            if (mMsgLinkClickListener != null) {
                                mMsgLinkClickListener.onMessageLinkClick(message.getMsgTag(), blueText, url);
                            }
                        }
                    }, 0, blueText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // 字体颜色
                    spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(itemView.getContext(), R.color.hyper_txt_link_color)), 0, blueText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    int subStart = start - lastStart;
                    String front = remainText.substring(0, subStart);//截取出一段文字+一段url
                    remainText = remainText.substring(end - lastStart, remainText.length());//剩下的部分
                    lastStart = end;
                    if (front.length() > 0) {
                        mMsgTv.append(front);
                    }
                    mMsgTv.append(spannableString);
                }
                if (remainText != null && remainText.length() > 0) {
                    mMsgTv.append(remainText);
                }
            }
        } else {
            mMsgTv.setText(text);
        }
//        mMsgTv.setText(message.getText());
        if (message.getTimeString() != null && !TextUtils.isEmpty(message.getTimeString())) {
            mDateTv.setVisibility(View.VISIBLE);
            mDateTv.setText(message.getTimeString());
        } else {
            mDateTv.setVisibility(View.GONE);
        }
        boolean isAvatarExists = message.getFromUser().getAvatarFilePath() != null
                && !message.getFromUser().getAvatarFilePath().isEmpty();
        if (isAvatarExists && mImageLoader != null) {
            mImageLoader.loadAvatarImage(mAvatarIv, message.getFromUser().getAvatarFilePath());
        } else if (mImageLoader == null) {
            mAvatarIv.setVisibility(View.GONE);
        }
        if (!mIsSender) {
            if (mDisplayNameTv.getVisibility() == View.VISIBLE) {
                mDisplayNameTv.setText(message.getFromUser().getDisplayName());
            }
        } else {
            switch (message.getMessageStatus()) {
                case SEND_GOING:
                    mSendingPb.setVisibility(View.VISIBLE);
                    mResendIb.setVisibility(View.GONE);
                    Log.i("TxtViewHolder", "sending message");
                    break;
                case SEND_FAILED:
                    mSendingPb.setVisibility(View.GONE);
                    Log.i("TxtViewHolder", "send message failed");
                    mResendIb.setVisibility(View.VISIBLE);
                    mResendIb.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mMsgResendListener != null) {
                                mMsgResendListener.onMessageResend(message);
                            }
                        }
                    });
                    break;
                case SEND_SUCCEED:
                    mSendingPb.setVisibility(View.GONE);
                    mResendIb.setVisibility(View.GONE);
                    Log.i("TxtViewHolder", "send message succeed");
                    break;
            }
        }

        mMsgTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMsgClickListener != null) {
                    mMsgClickListener.onMessageClick(message);
                }
            }
        });

        mMsgTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mMsgLongClickListener != null) {
                    mMsgLongClickListener.onMessageLongClick(message);
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.w("MsgListAdapter", "Didn't set long click listener! Drop event.");
                    }
                }
                return true;
            }
        });

        mAvatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAvatarClickListener != null) {
                    mAvatarClickListener.onAvatarClick(message);
                }
            }
        });

        if (!mIsSender && null != mPlayIb) {
            mPlayIb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mController.notifyAnimStop();
                    mController.addView(getAdapterPosition(), mPlayIb);
                    if (mController.getLastPlayPosition() == getAdapterPosition()) {
                        if (null != mSpeechSynthesizer) {
                            mSpeechSynthesizer.stopSpeaking();

                            mController.setLastPlayPosition(-1);
                            mController.setLastPlayType(-1);

                            mPlayIb.setBackgroundResource(R.drawable.icon_common_play);
                        }
                    } else {
                        startPlayTextMessage(getAdapterPosition(), mMsgTv.getText().toString().trim());
                    }
                }
            });
        }
    }

    private void startPlayTextMessage(int position, String text) {
        mController.setLastPlayPosition(position);
        mController.setLastPlayType(ViewHolderController.PLAY_TYPE_TEXT);

        mPlayIb.setBackgroundResource(R.drawable.icon_common_pause);

        // 如果在播放语音消息，则要停止播放
        if (null != mMediaPlayer) {
            mMediaPlayer.pause();
        }

        if (null != mSpeechSynthesizer) {
            mSpeechSynthesizer.startSpeaking(text, new SynthesizerListener() {
                @Override
                public void onSpeakBegin() {
                }

                @Override
                public void onBufferProgress(int i, int i1, int i2, String s) {
                }

                @Override
                public void onSpeakPaused() {
                }

                @Override
                public void onSpeakResumed() {
                }

                @Override
                public void onSpeakProgress(int i, int i1, int i2) {
                }

                @Override
                public void onCompleted(SpeechError speechError) {
                    mController.setLastPlayPosition(-1);
                    mController.setLastPlayType(-1);

                    mPlayIb.setBackgroundResource(R.drawable.icon_common_play);
                }

                @Override
                public void onEvent(int i, int i1, int i2, Bundle bundle) {
                }
            });
        }
    }

    @Override
    public void applyStyle(MessageListStyle style) {
        mMsgTv.setMaxWidth((int) (style.getWindowWidth() * style.getBubbleMaxWidth()));
        if (mIsSender) {
            mMsgTv.setBackground(style.getSendBubbleDrawable());
            mMsgTv.setTextColor(style.getSendBubbleTextColor());
            mMsgTv.setTextSize(style.getSendBubbleTextSize());
            mMsgTv.setPadding(style.getSendBubblePaddingLeft(),
                    style.getSendBubblePaddingTop(),
                    style.getSendBubblePaddingRight(),
                    style.getSendBubblePaddingBottom());
            if (style.getSendingProgressDrawable() != null) {
                mSendingPb.setProgressDrawable(style.getSendingProgressDrawable());
            }
            if (style.getSendingIndeterminateDrawable() != null) {
                mSendingPb.setIndeterminateDrawable(style.getSendingIndeterminateDrawable());
            }
        } else {
            mMsgTv.setBackground(style.getReceiveBubbleDrawable());
            mMsgTv.setTextColor(style.getReceiveBubbleTextColor());
            mMsgTv.setTextSize(style.getReceiveBubbleTextSize());
            mMsgTv.setPadding(style.getReceiveBubblePaddingLeft(),
                    style.getReceiveBubblePaddingTop(),
                    style.getReceiveBubblePaddingRight(),
                    style.getReceiveBubblePaddingBottom());
            if (style.getShowDisplayName() == 1) {
                mDisplayNameTv.setVisibility(View.VISIBLE);
            }
        }
        mDateTv.setTextSize(style.getDateTextSize());
        mDateTv.setTextColor(style.getDateTextColor());

        android.view.ViewGroup.LayoutParams layoutParams = mAvatarIv.getLayoutParams();
        layoutParams.width = style.getAvatarWidth();
        layoutParams.height = style.getAvatarHeight();
        mAvatarIv.setLayoutParams(layoutParams);
    }

    public TextView getMsgTextView() {
        return mMsgTv;
    }

    public CircleImageView getAvatar() {
        return mAvatarIv;
    }

}