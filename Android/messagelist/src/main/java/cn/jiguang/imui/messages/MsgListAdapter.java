package cn.jiguang.imui.messages;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import cn.jiguang.imui.R;
import cn.jiguang.imui.commons.ImageLoader;
import cn.jiguang.imui.commons.ViewHolder;
import cn.jiguang.imui.commons.models.IMessage;

public class MsgListAdapter<MESSAGE extends IMessage> extends RecyclerView.Adapter<ViewHolder>
        implements ScrollMoreListener.OnLoadMoreListener {

    // Text message
    private final int TYPE_RECEIVE_TXT = 0;
    private final int TYPE_SEND_TXT = 1;

    // Photo message
    private final int TYPE_SEND_IMAGE = 2;
    private final int TYPE_RECEIVER_IMAGE = 3;

    // Location message
    private final int TYPE_SEND_LOCATION = 4;
    private final int TYPE_RECEIVER_LOCATION = 5;

    // Voice message
    private final int TYPE_SEND_VOICE = 6;
    private final int TYPE_RECEIVER_VOICE = 7;

    // Video message
    private final int TYPE_SEND_VIDEO = 8;
    private final int TYPE_RECEIVE_VIDEO = 9;

    // Group change message
    private final int TYPE_EVENT = 10;

    // Custom message
    private final int TYPE_CUSTOM_SEND_MSG = 11;
    private final int TYPE_CUSTOM_RECEIVE_MSG = 12;

    // File message
    private final int TYPE_SEND_FILE = 20;
    private final int TYPE_RECEIVE_FILE = 21;

    // Voice meeting message
    private final int TYPE_SEND_VOICE_MEETING = 60;
    private final int TYPE_RECEIVE_VOICE_MEETING = 61;

    // Video meeting message
    private final int TYPE_SEND_VIDEO_MEETING = 70;
    private final int TYPE_RECEIVE_VIDEO_MEETING = 71;

    private Context mContext;
    private String mSenderId;
    private HoldersConfig mHolders;
    private OnLoadMoreListener mListener;

    private ImageLoader mImageLoader;
    private boolean mIsSelectedMode;
    private OnMsgClickListener<MESSAGE> mMsgClickListener;
    private OnMsgLongClickListener<MESSAGE> mMsgLongClickListener;
    private OnAvatarClickListener<MESSAGE> mAvatarClickListener;
    private OnMsgResendListener<MESSAGE> mMsgResendListener;
    private SelectionListener mSelectionListener;
    private int mSelectedItemCount;
    private LinearLayoutManager mLayoutManager;
    private MessageListStyle mStyle;
    private MediaPlayer mMediaPlayer;
    private boolean mIsEarPhoneOn;
    private List<Wrapper> mItems;
    private boolean mScroll;

    public MsgListAdapter(String senderId, ImageLoader imageLoader) {
        this(senderId, new HoldersConfig(), imageLoader);
    }

    public MsgListAdapter(String senderId, HoldersConfig holders, ImageLoader imageLoader) {
        mMediaPlayer = new MediaPlayer();
        mSenderId = senderId;
        mHolders = holders;
        mImageLoader = imageLoader;
        mItems = new ArrayList<>();
    }

    public void setScrolling(boolean scroll) {
        this.mScroll = scroll;
    }

    public boolean getScrolling() {
        return this.mScroll;
    }

    public void setAudioPlayByEarPhone(int state) {
        AudioManager audioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        int currVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        if (state == 0) {
            mIsEarPhoneOn = false;
            audioManager.setSpeakerphoneOn(true);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                    AudioManager.STREAM_VOICE_CALL);
        } else {
            mIsEarPhoneOn = true;
            audioManager.setSpeakerphoneOn(false);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currVolume,
                    AudioManager.STREAM_VOICE_CALL);
        }
    }

    public void pauseVoice() {
        try {
            mMediaPlayer.pause();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_SEND_TXT:
                return getHolder(parent, mHolders.mSendTxtLayout, mHolders.mSendTxtHolder, true);
            case TYPE_RECEIVE_TXT:
                return getHolder(parent, mHolders.mReceiveTxtLayout, mHolders.mReceiveTxtHolder, false);
            case TYPE_SEND_LOCATION:
                return getHolder(parent, mHolders.mSendLocationLayout, mHolders.mSendLocationHolder, true);
            case TYPE_RECEIVER_LOCATION:
                return getHolder(parent, mHolders.mReceiveLocationLayout, mHolders.mReceiveLocationHolder, false);
            case TYPE_SEND_VOICE:
                return getHolder(parent, mHolders.mSendVoiceLayout, mHolders.mSendVoiceHolder, true);
            case TYPE_RECEIVER_VOICE:
                return getHolder(parent, mHolders.mReceiveVoiceLayout, mHolders.mReceiveVoiceHolder, false);
            case TYPE_SEND_IMAGE:
                return getHolder(parent, mHolders.mSendPhotoLayout, mHolders.mSendPhotoHolder, true);
            case TYPE_RECEIVER_IMAGE:
                return getHolder(parent, mHolders.mReceivePhotoLayout, mHolders.mReceivePhotoHolder, false);
            case TYPE_SEND_VIDEO:
                return getHolder(parent, mHolders.mSendVideoLayout, mHolders.mSendVideoHolder, true);
            case TYPE_RECEIVE_VIDEO:
                return getHolder(parent, mHolders.mReceiveVideoLayout, mHolders.mReceiveVideoHolder, false);
            case TYPE_EVENT:
                return getHolder(parent, mHolders.mEventLayout, mHolders.mEventMsgHolder, true);
            case TYPE_CUSTOM_SEND_MSG:
                return getHolder(parent, mHolders.mCustomSendMsgLayout, mHolders.mCustomSendMsgHolder, true);
            case TYPE_SEND_FILE:
                return getHolder(parent, mHolders.mSendFileLayout, mHolders.mSendFileHolder, true);
            case TYPE_RECEIVE_FILE:
                return getHolder(parent, mHolders.mReceiveFileLayout, mHolders.mReceiveFileHolder, false);

            case TYPE_SEND_VOICE_MEETING:
                return getHolder(parent, mHolders.mSendVoiceMeetingLayout, mHolders.mSendVoiceMeetingHolder, true);
            case TYPE_RECEIVE_VOICE_MEETING:
                return getHolder(parent, mHolders.mReceiveVoiceMeetingLayout, mHolders.mReceiveVoiceMeetingHolder, false);
            case TYPE_SEND_VIDEO_MEETING:
                return getHolder(parent, mHolders.mSendVideoMeetingLayout, mHolders.mSendVideoMeetingHolder, true);
            case TYPE_RECEIVE_VIDEO_MEETING:
                return getHolder(parent, mHolders.mReceiveVideoMeetingLayout, mHolders.mReceiveVideoMeetingHolder, false);


            default:
                return getHolder(parent, mHolders.mCustomReceiveMsgLayout, mHolders.mCustomReceiveMsgHolder, false);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Wrapper wrapper = mItems.get(position);
        if (wrapper.item instanceof IMessage) {
            IMessage message = (IMessage) wrapper.item;
            switch (message.getType()) {
                case SEND_TEXT:
                    return TYPE_SEND_TXT;
                case RECEIVE_TEXT:
                    return TYPE_RECEIVE_TXT;
                case SEND_LOCATION:
                    return TYPE_SEND_LOCATION;
                case RECEIVE_LOCATION:
                    return TYPE_RECEIVER_LOCATION;
                case SEND_VOICE:
                    return TYPE_SEND_VOICE;
                case RECEIVE_VOICE:
                    return TYPE_RECEIVER_VOICE;
                case SEND_IMAGE:
                    return TYPE_SEND_IMAGE;
                case RECEIVE_IMAGE:
                    return TYPE_RECEIVER_IMAGE;
                case SEND_VIDEO:
                    return TYPE_SEND_VIDEO;
                case RECEIVE_VIDEO:
                    return TYPE_RECEIVE_VIDEO;
                case EVENT:
                    return TYPE_EVENT;
                case SEND_CUSTOM:
                    return TYPE_CUSTOM_SEND_MSG;
                case SEND_FILE:
                    return TYPE_SEND_FILE;
                case RECEIVE_FILE:
                    return TYPE_RECEIVE_FILE;

                case SEND_VOICE_MEETING:
                    return TYPE_SEND_VOICE_MEETING;
                case RECEIVE_VOICE_MEETING:
                    return TYPE_RECEIVE_VOICE_MEETING;
                case SEND_VIDEO_MEETING:
                    return TYPE_SEND_VIDEO_MEETING;
                case RECEIVE_VIDEO_MEETING:
                    return TYPE_RECEIVE_VIDEO_MEETING;

                default:
                    return TYPE_CUSTOM_RECEIVE_MSG;
            }
        }
        return TYPE_CUSTOM_SEND_MSG;
    }

    private <HOLDER extends ViewHolder> ViewHolder getHolder(ViewGroup parent, @LayoutRes int layout,
                                                             Class<HOLDER> holderClass, boolean isSender) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        try {
            Constructor<HOLDER> constructor = holderClass.getDeclaredConstructor(View.class, boolean.class);
            constructor.setAccessible(true);
            HOLDER holder = constructor.newInstance(v, isSender);
            if (holder instanceof DefaultMessageViewHolder) {
                ((DefaultMessageViewHolder) holder).applyStyle(mStyle);
            }
            return holder;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Wrapper wrapper = mItems.get(holder.getAdapterPosition());
        if (wrapper.item instanceof IMessage) {
            ((BaseMessageViewHolder) holder).mPosition = holder.getAdapterPosition();
            ((BaseMessageViewHolder) holder).mContext = this.mContext;
            DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
            ((BaseMessageViewHolder) holder).mDensity = dm.density;
            ((BaseMessageViewHolder) holder).mIsSelected = wrapper.isSelected;
            ((BaseMessageViewHolder) holder).mImageLoader = this.mImageLoader;
            ((BaseMessageViewHolder) holder).mMsgLongClickListener = this.mMsgLongClickListener;
            ((BaseMessageViewHolder) holder).mMsgClickListener = this.mMsgClickListener;
            ((BaseMessageViewHolder) holder).mMsgLinkClickListener = this.mMsgLinkClickListener;
            ((BaseMessageViewHolder) holder).mAvatarClickListener = this.mAvatarClickListener;
            ((BaseMessageViewHolder) holder).mMsgResendListener = this.mMsgResendListener;
            ((BaseMessageViewHolder) holder).mMediaPlayer = this.mMediaPlayer;
            ((BaseMessageViewHolder) holder).mScroll = this.mScroll;
            ((BaseMessageViewHolder) holder).mIsEarPhoneOn = this.mIsEarPhoneOn;
        }
        holder.onBind(wrapper.item);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private class Wrapper<DATA> {
        private DATA item;
        boolean isSelected;

        Wrapper(DATA item) {
            this.item = item;
        }
    }

    /**
     * Add message to bottom of list
     *
     * @param message        message to be add
     * @param scrollToBottom if true scroll list to bottom
     */
    public void addToStart(MESSAGE message, boolean scrollToBottom) {
        Wrapper<MESSAGE> element = new Wrapper<>(message);
        mItems.add(0, element);
        notifyItemRangeInserted(0, 1);
        if (mLayoutManager != null && scrollToBottom) {
            mLayoutManager.scrollToPosition(0);
        }
    }

    /**
     * Add messages chronologically, to load last page of messages from history, use this method.
     *
     * @param messages Last page of messages.
     */
    public void addToEnd(List<MESSAGE> messages) {
        int oldSize = mItems.size();
        for (int i = 0; i < messages.size(); i++) {
            MESSAGE message = messages.get(i);
            mItems.add(new Wrapper<>(message));
        }
        notifyItemRangeInserted(oldSize, mItems.size() - oldSize);
    }

    @SuppressWarnings("unchecked")
    private int getMessagePositionById(String id) {
        for (int i = 0; i < mItems.size(); i++) {
            Wrapper wrapper = mItems.get(i);
            if (wrapper.item instanceof IMessage) {
                MESSAGE message = (MESSAGE) wrapper.item;
                if (message.getMsgId().contentEquals(id)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public List<MESSAGE> getMessageList() {
        List<MESSAGE> msgList = new ArrayList<>();
        for (Wrapper wrapper : mItems) {
            if (wrapper.item instanceof IMessage) {
                msgList.add((MESSAGE) wrapper.item);
            }
        }
        return msgList;
    }

    /**
     * Update message by its id.
     *
     * @param message message to be updated.
     */
    public void updateMessage(MESSAGE message) {
        updateMessage(message.getMsgId(), message);
    }

    /**
     * Updates message by old identifier.
     *
     * @param oldId      message id to be updated
     * @param newMessage message to be updated
     */
    public void updateMessage(String oldId, MESSAGE newMessage) {
        int position = getMessagePositionById(oldId);
        if (position >= 0) {
            Wrapper<MESSAGE> element = new Wrapper<>(newMessage);
            mItems.set(position, element);
            notifyItemChanged(position);
        }
    }

    /**
     * Delete message.
     *
     * @param message message to be deleted.
     */
    public void delete(MESSAGE message) {
        deleteById(message.getMsgId());
    }

    /**
     * Delete message by identifier.
     *
     * @param id identifier of message.
     */
    public void deleteById(String id) {
        int index = getMessagePositionById(id);
        if (index >= 0) {
            mItems.remove(index);
            notifyItemRemoved(index);
        }
    }

    /**
     * Delete messages.
     *
     * @param messages messages list to be deleted.
     */
    public void delete(List<MESSAGE> messages) {
        for (MESSAGE message : messages) {
            int index = getMessagePositionById(message.getMsgId());
            if (index >= 0) {
                mItems.remove(index);
                notifyItemRemoved(index);
            }
        }
    }

    /**
     * Delete messages by identifiers.
     *
     * @param ids ids array of identifiers of messages to be deleted.
     */
    public void deleteByIds(String[] ids) {
        for (String id : ids) {
            int index = getMessagePositionById(id);
            if (index >= 0) {
                mItems.remove(index);
                notifyItemRemoved(index);
            }
        }
    }

    /**
     * Clear messages list.
     */
    public void clear() {
        mItems.clear();
    }

    /**
     * Enable selection mode.
     *
     * @param listener SelectionListener. To get selected messages use {@link #getSelectedMessages()}.
     */
    public void enableSelectionMode(SelectionListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("SelectionListener must not be null.");
        } else {
            mSelectionListener = listener;
        }
    }

    /**
     * Disable selection mode, and deselect all items.
     */
    public void disableSelectionMode() {
        mSelectionListener = null;
        deselectAllItems();
    }

    /**
     * Get selected messages.
     *
     * @return ArrayList with selected messages.
     */
    @SuppressWarnings("unchecked")
    public ArrayList<MESSAGE> getSelectedMessages() {
        ArrayList<MESSAGE> list = new ArrayList<>();
        for (Wrapper wrapper : mItems) {
            if (wrapper.item instanceof IMessage && wrapper.isSelected) {
                list.add((MESSAGE) wrapper.item);
            }
        }
        return list;
    }

    /**
     * Delete all selected messages
     */
    public void deleteSelectedMessages() {
        List<MESSAGE> selectedMessages = getSelectedMessages();
        delete(selectedMessages);
        deselectAllItems();
    }

    /**
     * Deselect all items.
     */
    public void deselectAllItems() {
        for (int i = 0; i < mItems.size(); i++) {
            Wrapper wrapper = mItems.get(i);
            if (wrapper.isSelected) {
                wrapper.isSelected = false;
                notifyItemChanged(i);
            }
        }
        mIsSelectedMode = false;
        mSelectedItemCount = 0;
        notifySelectionChanged();
    }

    private void notifySelectionChanged() {
        if (mSelectionListener != null) {
            mSelectionListener.onSelectionChanged(mSelectedItemCount);
        }
    }

    /**
     * Set onMsgClickListener, fires onClick event only if list is not in selection mode.
     *
     * @param listener OnMsgClickListener
     */
    public void setOnMsgClickListener(OnMsgClickListener<MESSAGE> listener) {
        mMsgClickListener = listener;
    }

    private View.OnClickListener getMsgClickListener(final Wrapper<MESSAGE> wrapper) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSelectionListener != null && mIsSelectedMode) {
                    wrapper.isSelected = !wrapper.isSelected;
                    if (wrapper.isSelected) {
                        incrementSelectedItemsCount();
                    } else {
                        decrementSelectedItemsCount();
                    }

                    MESSAGE message = (wrapper.item);
                    notifyItemChanged(getMessagePositionById(message.getMsgId()));
                } else {
                    notifyMessageClicked(wrapper.item);
                }
            }
        };
    }

    private void incrementSelectedItemsCount() {
        mSelectedItemCount++;
        notifySelectionChanged();
    }

    private void decrementSelectedItemsCount() {
        mSelectedItemCount--;
        mIsSelectedMode = mSelectedItemCount > 0;
        notifySelectionChanged();
    }

    private void notifyMessageClicked(MESSAGE message) {
        if (mMsgClickListener != null) {
            mMsgClickListener.onMessageClick(message);
        }
    }

    /**
     * Set long click listener for item, fires only if selection mode is disabled.
     *
     * @param listener onMsgLongClickListener
     */
    public void setMsgLongClickListener(OnMsgLongClickListener<MESSAGE> listener) {
        mMsgLongClickListener = listener;
    }

    private View.OnLongClickListener getMessageLongClickListener(final Wrapper<MESSAGE> wrapper) {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mSelectionListener == null) {
                    notifyMessageLongClicked(wrapper.item);
                    return true;
                } else {
                    mIsSelectedMode = true;
                    view.callOnClick();
                    return true;
                }
            }
        };
    }

    private void notifyMessageLongClicked(MESSAGE message) {
        if (mMsgLongClickListener != null) {
            mMsgLongClickListener.onMessageLongClick(message);
        }
    }

    public void setOnAvatarClickListener(OnAvatarClickListener<MESSAGE> listener) {
        mAvatarClickListener = listener;
    }

    public void setLayoutManager(LinearLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    public void setStyle(Context context, MessageListStyle style) {
        mContext = context;
        mStyle = style;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        mListener = listener;
    }

    @Override
    public void onLoadMore(int page, int total) {
        if (null != mListener) {
            mListener.onLoadMore(page, total);
        }
    }

    public interface DefaultMessageViewHolder {
        void applyStyle(MessageListStyle style);
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int page, int totalCount);
    }

    public interface SelectionListener {
        void onSelectionChanged(int count);
    }

    /**
     * Callback will invoked when message item is clicked
     *
     * @param <MESSAGE>
     */
    public interface OnMsgClickListener<MESSAGE extends IMessage> {
        void onMessageClick(MESSAGE message);
    }

    /**
     * Callback will invoked when message item is long clicked
     *
     * @param <MESSAGE>
     */
    public interface OnMsgLongClickListener<MESSAGE extends IMessage> {
        void onMessageLongClick(MESSAGE message);
    }

    public void setOnMsgLinkClickListener(OnMsgLinkClickListener listener) {
        mMsgLinkClickListener = listener;
    }

    /**
     * Callback will invoked when the link content is clicked
     */
    public interface OnMsgLinkClickListener {
        /**
         * 富文本消息的文本点击回调
         * @param isNeededOpenInWebView 是否需要使用浏览器打开，
         *                              如果是相关法律法规就需要，
         *                              如果是其他的就是在群里@小商
         * @param linkContent 在群里@小商的内容
         * @param url 使用浏览器打开的连接
         */
        void onMessageLinkClick(boolean isNeededOpenInWebView, String linkContent, String url);
    }

    public interface OnAvatarClickListener<MESSAGE extends IMessage> {
        void onAvatarClick(MESSAGE message);
    }

    public void setMsgResendListener(OnMsgResendListener<MESSAGE> listener) {
        this.mMsgResendListener = listener;
    }

    public interface OnMsgResendListener<MESSAGE extends IMessage> {
        void onMessageResend(MESSAGE message);
    }

    /**
     * Holders Config
     * Config your custom layouts and view holders into adapter.
     * You need instantiate HoldersConfig, otherwise will use default MessageListStyle.
     */
    public static class HoldersConfig {

        private Class<? extends BaseMessageViewHolder<? extends IMessage>> mSendTxtHolder;
        private Class<? extends BaseMessageViewHolder<? extends IMessage>> mReceiveTxtHolder;

        private Class<? extends BaseMessageViewHolder<? extends IMessage>> mSendLocationHolder;
        private Class<? extends BaseMessageViewHolder<? extends IMessage>> mReceiveLocationHolder;

        private Class<? extends BaseMessageViewHolder<? extends IMessage>> mSendVoiceHolder;
        private Class<? extends BaseMessageViewHolder<? extends IMessage>> mReceiveVoiceHolder;

        private Class<? extends BaseMessageViewHolder<? extends IMessage>> mSendPhotoHolder;
        private Class<? extends BaseMessageViewHolder<? extends IMessage>> mReceivePhotoHolder;

        private Class<? extends BaseMessageViewHolder<? extends IMessage>> mSendVideoHolder;
        private Class<? extends BaseMessageViewHolder<? extends IMessage>> mReceiveVideoHolder;

        private Class<? extends BaseMessageViewHolder<? extends IMessage>> mCustomSendMsgHolder;
        private Class<? extends BaseMessageViewHolder<? extends IMessage>> mCustomReceiveMsgHolder;

        private Class<? extends BaseMessageViewHolder<? extends IMessage>> mEventMsgHolder;

        private Class<? extends BaseMessageViewHolder<? extends IMessage>> mSendFileHolder;
        private Class<? extends BaseMessageViewHolder<? extends IMessage>> mReceiveFileHolder;


        private Class<? extends BaseMessageViewHolder<? extends IMessage>> mSendVoiceMeetingHolder;
        private Class<? extends BaseMessageViewHolder<? extends IMessage>> mReceiveVoiceMeetingHolder;
        private Class<? extends BaseMessageViewHolder<? extends IMessage>> mSendVideoMeetingHolder;
        private Class<? extends BaseMessageViewHolder<? extends IMessage>> mReceiveVideoMeetingHolder;


        private int mSendTxtLayout;
        private int mReceiveTxtLayout;

        private int mSendLocationLayout;
        private int mReceiveLocationLayout;

        private int mSendVoiceLayout;
        private int mReceiveVoiceLayout;

        private int mSendPhotoLayout;
        private int mReceivePhotoLayout;

        private int mSendVideoLayout;
        private int mReceiveVideoLayout;

        private int mCustomSendMsgLayout;
        private int mCustomReceiveMsgLayout;

        private int mEventLayout;

        private int mSendFileLayout;
        private int mReceiveFileLayout;


        private int mSendVoiceMeetingLayout;
        private int mReceiveVoiceMeetingLayout;
        private int mSendVideoMeetingLayout;
        private int mReceiveVideoMeetingLayout;


        public HoldersConfig() {
            mSendTxtHolder = DefaultTxtViewHolder.class;
            mReceiveTxtHolder = DefaultTxtViewHolder.class;

            mSendVoiceHolder = DefaultVoiceViewHolder.class;
            mReceiveVoiceHolder = DefaultVoiceViewHolder.class;

            mSendPhotoHolder = DefaultPhotoViewHolder.class;
            mReceivePhotoHolder = DefaultPhotoViewHolder.class;

            mSendVideoHolder = DefaultVideoViewHolder.class;
            mReceiveVideoHolder = DefaultVideoViewHolder.class;

            mSendFileHolder = DefaultFileViewHolder.class;
            mReceiveFileHolder = DefaultFileViewHolder.class;



            mSendVoiceMeetingHolder = DefaultVoiceMeetingViewHolder.class;
            mReceiveVoiceMeetingHolder = DefaultVoiceMeetingViewHolder.class;
            mSendVideoMeetingHolder = DefaultVideoMeetingViewHolder.class;
            mReceiveVideoMeetingHolder = DefaultVideoMeetingViewHolder.class;



            mSendTxtLayout = R.layout.item_send_text;
            mReceiveTxtLayout = R.layout.item_receive_txt;

            mSendVoiceLayout = R.layout.item_send_voice;
            mReceiveVoiceLayout = R.layout.item_receive_voice;

            mSendPhotoLayout = R.layout.item_send_photo;
            mReceivePhotoLayout = R.layout.item_receive_photo;

            mSendVideoLayout = R.layout.item_send_video;
            mReceiveVideoLayout = R.layout.item_receive_video;

            mEventMsgHolder = DefaultEventMsgViewHolder.class;
            mEventLayout = R.layout.item_event_message;

            mSendFileLayout = R.layout.item_send_file;
            mReceiveFileLayout = R.layout.item_receive_file;


            mSendVoiceMeetingLayout = R.layout.item_send_voice_meeting;
            mReceiveVoiceMeetingLayout = R.layout.item_receive_voice_meeting;
            mSendVideoMeetingLayout = R.layout.item_send_video_meeting;
            mReceiveVideoMeetingLayout = R.layout.item_receive_video_meeting;

        }

        /**
         * In place of default send text message style by passing custom view holder and layout.
         *
         * @param holder Custom view holder that extends BaseMessageViewHolder.
         * @param layout Custom send text message layout.
         */
        public void setSenderTxtMsg(Class<? extends BaseMessageViewHolder<? extends IMessage>> holder,
                                    @LayoutRes int layout) {
            this.mSendTxtHolder = holder;
            this.mSendTxtLayout = layout;
        }

        /**
         * In place of default receive text message style by passing custom view holder and layout.
         *
         * @param holder Custom view holder that extends BaseMessageViewHolder.
         * @param layout Custom receive text message layout.
         */
        public void setReceiverTxtMsg(Class<? extends BaseMessageViewHolder<? extends IMessage>> holder,
                                      @LayoutRes int layout) {
            this.mReceiveTxtHolder = holder;
            this.mReceiveTxtLayout = layout;
        }

        /**
         * Customize send text message layout.
         *
         * @param layout Custom send text message layout.
         */
        public void setSenderLayout(@LayoutRes int layout) {
            this.mSendTxtLayout = layout;
        }

        /**
         * Customize receive text message layout.
         *
         * @param layout Custom receive text message layout.
         */
        public void setReceiverLayout(@LayoutRes int layout) {
            this.mReceiveTxtLayout = layout;
        }

        /**
         * In place of default send voice message style by passing custom view holder and layout.
         *
         * @param holder Custom view holder that extends BaseMessageViewHolder.
         * @param layout Custom send voice message layout.
         */
        public void setSenderVoiceMsg(Class<? extends BaseMessageViewHolder<? extends IMessage>> holder,
                                      @LayoutRes int layout) {
            this.mSendVoiceHolder = holder;
            this.mSendVoiceLayout = layout;
        }

        /**
         * Customize send voice message layout.
         *
         * @param layout Custom send voice message layout.
         */
        public void setSendVoiceLayout(@LayoutRes int layout) {
            this.mSendVoiceLayout = layout;
        }

        /**
         * In place of default receive voice message style by passing custom view holder and layout.
         *
         * @param holder Custom view holder that extends BaseMessageViewHolder.
         * @param layout Custom receive voice message layout.
         */
        public void setReceiverVoiceMsg(Class<? extends BaseMessageViewHolder<? extends IMessage>> holder,
                                        @LayoutRes int layout) {
            this.mReceiveVoiceHolder = holder;
            this.mReceiveVoiceLayout = layout;
        }

        /**
         * Customize receive voice message layout.
         *
         * @param layout Custom receive voice message layout.
         */
        public void setReceiveVoiceLayout(@LayoutRes int layout) {
            this.mReceiveVoiceLayout = layout;
        }

        /**
         * In place of default send photo message style by passing custom view holder and layout.
         *
         * @param holder Custom view holder that extends BaseMessageViewHolder.
         * @param layout Custom send photo message layout
         */
        public void setSendPhotoMsg(Class<? extends BaseMessageViewHolder<? extends IMessage>> holder,
                                    @LayoutRes int layout) {
            this.mSendPhotoHolder = holder;
            this.mSendPhotoLayout = layout;
        }

        /**
         * Customize send voice message layout.
         *
         * @param layout Custom send photo message layout.
         */
        public void setSendPhotoLayout(@LayoutRes int layout) {
            this.mSendPhotoLayout = layout;
        }

        /**
         * In place of default receive photo message style by passing custom view holder and layout.
         *
         * @param holder Custom view holder that extends BaseMessageViewHolder.
         * @param layout Custom receive photo message layout
         */
        public void setReceivePhotoMsg(Class<? extends BaseMessageViewHolder<? extends IMessage>> holder,
                                       @LayoutRes int layout) {
            this.mReceivePhotoHolder = holder;
            this.mReceivePhotoLayout = layout;
        }

        /**
         * Customize receive voice message layout.
         *
         * @param layout Custom receive photo message layout.
         */
        public void setReceivePhotoLayout(@LayoutRes int layout) {
            this.mReceivePhotoLayout = layout;
        }

        /**
         * In place of default send video message style by passing custom view holder and layout.
         *
         * @param holder Custom view holder that extends BaseMessageViewHolder.
         * @param layout custom send video message layout
         */
        public void setSendVideoMsg(Class<? extends BaseMessageViewHolder<? extends IMessage>> holder,
                                    @LayoutRes int layout) {
            this.mSendVideoHolder = holder;
            this.mSendVideoLayout = layout;
        }

        /**
         * Customize send voice message layout.
         *
         * @param layout Custom send Video message layout.
         */
        public void setSendVideoLayout(@LayoutRes int layout) {
            this.mSendVideoLayout = layout;
        }

        /**
         * In place of default receive video message style by passing custom view holder and layout.
         *
         * @param holder Custom view holder that extends BaseMessageViewHolder.
         * @param layout Custom receive video message layout
         */
        public void setReceiveVideoMsg(Class<? extends BaseMessageViewHolder<? extends IMessage>> holder,
                                       @LayoutRes int layout) {
            this.mReceiveVideoHolder = holder;
            this.mReceiveVideoLayout = layout;
        }

        /**
         * Customize receive video message layout.
         *
         * @param layout Custom receive video message layout.
         */
        public void setReceiveVideoLayout(@LayoutRes int layout) {
            this.mReceiveVideoLayout = layout;
        }

        public void setSendCustomMsg(Class<? extends BaseMessageViewHolder<? extends IMessage>> holder,
                                     @LayoutRes int layout) {
            this.mCustomSendMsgHolder = holder;
            this.mCustomSendMsgLayout = layout;
        }

        public void setReceiveCustomMsg(Class<? extends BaseMessageViewHolder<? extends IMessage>> holder,
                                        @LayoutRes int layout) {
            this.mCustomReceiveMsgHolder = holder;
            this.mCustomReceiveMsgLayout = layout;
        }

        public void setSendLocationMsg(Class<? extends BaseMessageViewHolder<? extends IMessage>> holder,
                                       @LayoutRes int layout) {
            this.mSendLocationHolder = holder;
            this.mSendLocationLayout = layout;
        }

        public void setReceiveLocationMsg(Class<? extends BaseMessageViewHolder<? extends IMessage>> holder,
                                          @LayoutRes int layout) {
            this.mReceiveLocationHolder = holder;
            this.mReceiveLocationLayout = layout;
        }


        public void setEventMessage(Class<? extends BaseMessageViewHolder<? extends IMessage>> holder,
                                    @LayoutRes int layout) {
            this.mEventMsgHolder = holder;
            this.mEventLayout = layout;
        }

    }

    private static class DefaultTxtViewHolder extends TxtViewHolder<IMessage> {

        public DefaultTxtViewHolder(View itemView, boolean isSender) {
            super(itemView, isSender);

        }
    }

    private static class DefaultVoiceViewHolder extends VoiceViewHolder<IMessage> {

        public DefaultVoiceViewHolder(View itemView, boolean isSender) {
            super(itemView, isSender);
        }
    }

    private static class DefaultPhotoViewHolder extends PhotoViewHolder<IMessage> {

        public DefaultPhotoViewHolder(View itemView, boolean isSender) {
            super(itemView, isSender);
        }
    }

    private static class DefaultVideoViewHolder extends VideoViewHolder<IMessage> {

        public DefaultVideoViewHolder(View itemView, boolean isSender) {
            super(itemView, isSender);
        }
    }

    private static class DefaultEventMsgViewHolder extends EventViewHolder<IMessage> {
        public DefaultEventMsgViewHolder(View itemView, boolean isSender) {
            super(itemView, isSender);
        }
    }

    private static class DefaultFileViewHolder extends FileViewHolder<IMessage> {

        public DefaultFileViewHolder(View itemView, boolean isSender) {
            super(itemView, isSender);
        }
    }



    private static class DefaultVoiceMeetingViewHolder extends VoiceMeetingViewHolder<IMessage> {
        public DefaultVoiceMeetingViewHolder(View itemView, boolean isSender) {
            super(itemView, isSender);
        }
    }
    private static class DefaultVideoMeetingViewHolder extends VideoMeetingViewHolder<IMessage> {
        public DefaultVideoMeetingViewHolder(View itemView, boolean isSender) {
            super(itemView, isSender);
        }
    }




    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        ViewHolderController.getInstance().remove(holder.getAdapterPosition());
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        ViewHolderController.getInstance().release();
    }
}
