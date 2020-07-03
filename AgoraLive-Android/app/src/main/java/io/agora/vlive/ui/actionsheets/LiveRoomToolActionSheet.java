package io.agora.vlive.ui.actionsheets;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.agora.vlive.R;

public class LiveRoomToolActionSheet extends AbstractActionSheet {
    public interface LiveRoomToolActionSheetListener extends AbsActionSheetListener {
        void onActionSheetRealDataClicked();
        void onActionSheetSettingClicked();
        void onActionSheetRotateClicked();
        void onActionSheetVideoClicked(boolean muted);
        void onActionSheetSpeakerClicked(boolean muted);
        boolean onActionSheetEarMonitoringClicked(boolean monitor);
    }

    private static final int GRID_SPAN = 4;
    private static final int FUNC_COUNT_AUDIENCE = 1;
    private static final int FUNC_COUNT_VIRTUAL_IMAGE = 3;

    private static final int DATA_INDEX = 0;
    private static final int SPEAKER_INDEX = 1;
    private static final int EAR_MONITOR = 2;
    private static final int SETTING_INDEX = 3;
    private static final int ROTATE_INDEX = 4;
    private static final int VIDEO_INDEX = 5;

    private static final int[] ICON_RES = {
            R.drawable.icon_data,
            R.drawable.action_sheet_tool_speaker,
            R.drawable.action_sheet_tool_ear_monitor,
            R.drawable.icon_setting,
            R.drawable.icon_rotate,
            R.drawable.action_sheet_tool_video
    };

    private RecyclerView mRecycler;
    private String[] mToolNames;
    private boolean mIsHost;
    private boolean mIsVirtualImage;
    private int mItemPadding;
    private boolean mMuteVideo;
    private boolean mMuteVoice;
    private boolean mEarMonitoring;
    private LiveRoomToolActionSheetListener mListener;

    public LiveRoomToolActionSheet(Context context) {
        super(context);
        init();
    }

    private void init() {
        mToolNames = getResources().getStringArray(R.array.live_room_action_sheet_tool_names);
        mItemPadding = getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);

        LayoutInflater.from(getContext()).inflate(R.layout.action_tool, this, true);
        mRecycler = findViewById(R.id.live_room_action_sheet_tool_recycler);
        mRecycler.setLayoutManager(new GridLayoutManager(getContext(), GRID_SPAN));
        mRecycler.setAdapter(new ToolAdapter());
        mRecycler.addItemDecoration(new PaddingDecoration());
        mMuteVoice = application().config().isAudioMuted();
        mMuteVideo = application().config().isVideoMuted();
    }

    public void setHost(boolean isHost) {
        mIsHost = isHost;
        if (mRecycler != null) {
            mRecycler.getAdapter().notifyDataSetChanged();
        }
    }

    public void setVirtualImage(boolean virtualImage) {
        mIsVirtualImage = virtualImage;
        if (mRecycler != null) {
            mRecycler.getAdapter().notifyDataSetChanged();
        }
    }

    public void setEnableInEarMonitoring(boolean enabled) {
        mEarMonitoring = enabled;
        mRecycler.getAdapter().notifyDataSetChanged();
    }

    private class ToolAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ToolViewHolder(LayoutInflater.from(
                    getContext()).inflate(R.layout.action_tool_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ToolViewHolder toolViewHolder = (ToolViewHolder) holder;
            toolViewHolder.setPosition(position);
            toolViewHolder.name.setText(mToolNames[position]);
            toolViewHolder.icon.setImageResource(ICON_RES[position]);

            if (position == VIDEO_INDEX) {
                holder.itemView.setActivated(!mMuteVideo);
            } else if (position == SPEAKER_INDEX) {
                holder.itemView.setActivated(!mMuteVoice);
            } else if (position == EAR_MONITOR) {
                holder.itemView.setActivated(mEarMonitoring);
            }
        }

        @Override
        public int getItemCount() {
            if (!mIsHost) {
                return FUNC_COUNT_AUDIENCE;
            } else if (mIsVirtualImage) {
                return FUNC_COUNT_VIRTUAL_IMAGE;
            } else {
                return ICON_RES.length;
            }
        }
    }

    private class ToolViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView icon;
        AppCompatTextView name;
        int position;
        ToolViewHolder(@NonNull final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.live_room_action_sheet_tool_item_name);
            icon = itemView.findViewById(R.id.live_room_action_sheet_tool_item_icon);
            icon.setOnClickListener(view ->  {
                if (position == VIDEO_INDEX) {
                    mMuteVideo = !mMuteVideo;
                    itemView.setActivated(!mMuteVideo);
                } else if (position == SPEAKER_INDEX) {
                    mMuteVoice = !mMuteVoice;
                    itemView.setActivated(!mMuteVoice);
                }
                handleItemClicked(view, position);
            });
        }

        void setPosition(int position) {
            this.position = position;
        }
    }

    private void handleItemClicked(View view, int position) {
        if (mListener == null) return;

        switch (position) {
            case DATA_INDEX:
                mListener.onActionSheetRealDataClicked();
                break;
            case SETTING_INDEX:
                mListener.onActionSheetSettingClicked();
                break;
            case ROTATE_INDEX:
                mListener.onActionSheetRotateClicked();
                break;
            case VIDEO_INDEX:
                mListener.onActionSheetVideoClicked(mMuteVideo);
                break;
            case SPEAKER_INDEX:
                mListener.onActionSheetSpeakerClicked(mMuteVoice);
                break;
            case EAR_MONITOR:
                boolean allowed = mListener.onActionSheetEarMonitoringClicked(!mEarMonitoring);
                if (allowed) {
                    mEarMonitoring = !mEarMonitoring;
                    view.setActivated(mEarMonitoring);
                }
                break;
        }
    }

    private class PaddingDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.top = mItemPadding;
            outRect.bottom = mItemPadding;
        }
    }

    @Override
    public void setActionSheetListener(AbsActionSheetListener listener) {
        if (listener instanceof LiveRoomToolActionSheetListener) {
            mListener = (LiveRoomToolActionSheetListener) listener;
        }
    }
}
