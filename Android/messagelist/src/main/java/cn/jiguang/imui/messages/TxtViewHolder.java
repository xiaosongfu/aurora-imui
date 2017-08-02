package cn.jiguang.imui.messages;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.jiguang.imui.BuildConfig;
import cn.jiguang.imui.R;
import cn.jiguang.imui.commons.models.IMessage;
import cn.jiguang.imui.utils.LinkUtil;
import cn.jiguang.imui.view.CircleImageView;

public class TxtViewHolder<MESSAGE extends IMessage>
        extends BaseMessageViewHolder<MESSAGE>
        implements MsgListAdapter.DefaultMessageViewHolder {

    protected TextView mMsgTv;
    protected TextView mDateTv;
    protected TextView mDisplayNameTv;
    protected CircleImageView mAvatarIv;
    protected ImageButton mResendIb;
    protected ProgressBar mSendingPb;
    protected boolean mIsSender;

    public TxtViewHolder(View itemView, boolean isSender) {
        super(itemView);
        this.mIsSender = isSender;
        mMsgTv = (TextView) itemView.findViewById(R.id.aurora_tv_msgitem_message);
        mDateTv = (TextView) itemView.findViewById(R.id.aurora_tv_msgitem_date);
        mAvatarIv = (CircleImageView) itemView.findViewById(R.id.aurora_iv_msgitem_avatar);
        mDisplayNameTv = (TextView) itemView.findViewById(R.id.aurora_tv_msgitem_display_name);
        mResendIb = (ImageButton) itemView.findViewById(R.id.aurora_ib_msgitem_resend);
        mSendingPb = (ProgressBar) itemView.findViewById(R.id.aurora_pb_msgitem_sending);

        mMsgTv.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onBind(final MESSAGE message) {
        //TAG !! 对于文本消息，因为 RecycleView 的 item 会重用，
        //为了避免消息错乱，需要先设置为 ""
        mMsgTv.setText("");

        //参考：http://blog.csdn.net/qq_24530405/article/details/50506519
        //参考：http://m.blog.csdn.net/article/details?id=51767368
        final String text = message.getText();
//        System.out.println("onBind: "+text);
        if (text == null || text.length() == 0) {
            return;
        }
//        //只关心接收到的文本消息
        //20170802 下午，fix bug #388
        //调解员登录 app 的时候，他在 web 推送的法律法规就不再是 IMessage.MessageType.RECEIVE_TEXT 这个类型，
        //所以这里不能限定在 IMessage.MessageType.RECEIVE_TEXT 这个类型
//        if(IMessage.MessageType.RECEIVE_TEXT == message.getType()) {
            Object[][] output = LinkUtil.parseLinks(text);
            if (output == null || output.length == 0 || output[0] == null || output[0].length == 0) {
                mMsgTv.setText(message.getText());
            } else {
                //有可能只有文本而没有url，所以必须以文本数量为循环次数的标准
                //而对于url，也得先判断有没有才能取值
                int blueTextCount = output[0].length;
                int urlCount = output[1].length;
                String remainText = text;
                int lastStart = 0;//截取到一部分后截掉部分的长度
                for (int i = 0; i < blueTextCount; i++) {
                    final String blueText = (String) output[0][i];//带下划线的文字
                    final String url = i < urlCount ? (String) output[1][i] : null;
                    int start = (int) output[2][i];//<a>标签在源字符串的起始位置
                    int end = (int) output[3][i];//<a>标签在源字符串的结束位置
                    SpannableString spannableString = new SpannableString(blueText);
                    spannableString.setSpan(new ClickableSpan() {
                        //在这里定义点击下划线文字的点击事件，不一定非要打开浏览器
                        @Override
                        public void onClick(View widget) {
                            if (mMsgLinkClickListener != null) {
                                if (null != url && !TextUtils.isEmpty(url) && url.startsWith("http")) {
                                    mMsgLinkClickListener.onMessageLinkClick(true, blueText, url);
                                } else {
                                    mMsgLinkClickListener.onMessageLinkClick(false, blueText, url);
                                }
                            }
                        }
                    }, 0, blueText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    int subStart = start - lastStart;
                    String front = remainText.substring(0, subStart);//截取出一段文字+一段url
                    //Log.i("TxtViewHolder", "起始位置：" + (end - lastStart));
                    remainText = remainText.substring(end - lastStart, remainText.length());//剩下的部分
                    lastStart = end;
                    //Log.i("TxtViewHolder", "front是：" + front);
                    //Log.i("TxtViewHolder", "spann是：" + spannableString);
                    //Log.i("TxtViewHolder", "remain是：" + remainText);
                    if (front.length() > 0) {
//                        System.out.println("onBind--front: "+text);
                        mMsgTv.append(front);
                    }
//                    System.out.println("onBind--spannableString: "+spannableString);
                    mMsgTv.append(spannableString);
                }
                if (remainText != null && remainText.length() > 0) {
//                    System.out.println("onBind--remainText: "+remainText);
                    mMsgTv.append(remainText);
                }
            }
//        }else {
//            mMsgTv.setText(text);
//        }
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