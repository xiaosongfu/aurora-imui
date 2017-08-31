package cn.jiguang.imui.messages;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.jiguang.imui.BuildConfig;
import cn.jiguang.imui.R;
import cn.jiguang.imui.commons.models.IMessage;
import cn.jiguang.imui.view.RoundImageView;

public class FileViewHolder<MESSAGE extends IMessage>
        extends BaseMessageViewHolder<MESSAGE>
        implements MsgListAdapter.DefaultMessageViewHolder {

    protected TextView mMsgTv;
    protected TextView mMsgFileSizeTv;
    protected TextView mDateTv;
    protected TextView mDisplayNameTv;
    protected RoundImageView mAvatarIv;
    protected ImageButton mResendIb;
    protected ProgressBar mSendingPb;
    protected boolean mIsSender;

    public FileViewHolder(View itemView, boolean isSender) {
        super(itemView);
        this.mIsSender = isSender;
        mMsgTv = (TextView) itemView.findViewById(R.id.aurora_tv_msgitem_message);
        mMsgFileSizeTv = (TextView) itemView.findViewById(R.id.aurora_tv_msgitem_filesize);
        mDateTv = (TextView) itemView.findViewById(R.id.aurora_tv_msgitem_date);
        mAvatarIv = (RoundImageView) itemView.findViewById(R.id.aurora_iv_msgitem_avatar);
        mDisplayNameTv = (TextView) itemView.findViewById(R.id.aurora_tv_msgitem_display_name);
        mResendIb = (ImageButton) itemView.findViewById(R.id.aurora_ib_msgitem_resend);
        mSendingPb = (ProgressBar) itemView.findViewById(R.id.aurora_pb_msgitem_sending);
    }

    @Override
    public void onBind(final MESSAGE message) {
//        mMsgTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mMsgTv.setText(message.getMediaFileName());
        if (message.getMediaFileSize() > 0){
            long size = message.getMediaFileSize() / 1024;
            String sizeText = "";
            if (size <= 0) {
                sizeText = message.getMediaFileSize() + "B";
            } else {
                long size2 = size / 1024;
                if (size2 <= 0) {
                    sizeText = size + "KB";
                } else {
                    sizeText = size2 + "MB";
                }
            }
            mMsgFileSizeTv.setText(sizeText);
        }
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
    }

    @Override
    public void applyStyle(MessageListStyle style) {
//        mMsgTv.setMaxWidth((int) (style.getWindowWidth() * style.getBubbleMaxWidth()));
        mMsgTv.setMaxWidth((int) (style.getWindowWidth() * 0.4F));
        if (mIsSender) {
//            mMsgTv.setBackground(style.getSendBubbleDrawable());
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
//            mMsgTv.setBackground(style.getReceiveBubbleDrawable());
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

    public ImageView getAvatar() {
        return mAvatarIv;
    }

}