package io.agora.vlive.ui.actionsheets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.agora.vlive.utils.Global;
import io.agora.vlive.R;

public class LiveRoomSettingActionSheet extends AbstractActionSheet implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    public interface LiveRoomSettingActionSheetListener extends AbsActionSheetListener {
        void onResolutionSelected(int index);
        void onFrameRateSelected(int index);
        void onBitrateSelected(int bitrate);
        void onSettingBackPressed();
    }

    private static final int PAGE_MAIN = 0;
    private static final int PAGE_RESOLUTION = 1;
    private static final int PAGE_FRAME_RATE = 2;

    private int mCurPage;
    private View mMain;
    private View mBackIcon;
    private TextView mTitle;
    private LayoutInflater mInflater;
    private LinearLayout mContainer;

    private AppCompatTextView mMainResolutionText;
    private AppCompatTextView mMainFrameRateText;
    private AppCompatTextView mMainBitrateText;
    private String mMainBitrateTextFormat;
    private SeekBar mBitrateSeekBar;

    private RecyclerView mResolutionRecycler;
    private RecyclerView mFrameRateRecycler;

    private int mPaddingHorizontal;
    private int mDividerHeight;

    // Whether the sheet can fallback to previous action sheets
    private boolean mCanFallback;

    private LiveRoomSettingActionSheetListener mListener;

    public LiveRoomSettingActionSheet(Context context) {
        super(context);
        init();
    }

    public LiveRoomSettingActionSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LiveRoomSettingActionSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaddingHorizontal = getResources().getDimensionPixelSize(
                R.dimen.live_room_action_sheet_margin);
        mDividerHeight = getResources().getDimensionPixelSize(
                R.dimen.live_room_action_sheet_item_divider_height);
        mInflater = LayoutInflater.from(getContext());
        View layout = mInflater.inflate(
                R.layout.action_room_settings, this, false);
        addView(layout);

        mContainer = findViewById(R.id.live_room_setting_container);
        mMain = mInflater.inflate(R.layout.action_room_settings_main, this, false);
        mBackIcon = layout.findViewById(R.id.live_room_setting_back);
        mBackIcon.setOnClickListener(this);
        mTitle = layout.findViewById(R.id.live_room_action_sheet_bg_music_title);
        mMain.findViewById(R.id.live_room_setting_resolution).setOnClickListener(this);
        mMain.findViewById(R.id.live_room_setting_framerate).setOnClickListener(this);

        gotoMainPage();
    }

    public void setFallback(boolean canFallback) {
        mCanFallback = canFallback;
        mBackIcon.setVisibility(mCanFallback ? VISIBLE : GONE);
    }

    @Override
    public void setActionSheetListener(AbsActionSheetListener listener) {
        if (listener instanceof LiveRoomSettingActionSheetListener) {
            mListener = (LiveRoomSettingActionSheetListener) listener;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar == mBitrateSeekBar) {
            int progress = seekBar.getProgress();
            if (progress < Global.Constants.VIDEO_MIN_BITRATE) {
                progress = Global.Constants.VIDEO_MIN_BITRATE;
            }

            // round up all values to the multiples of 50
            if (progress % 50 != 0) {
                progress = progress / 50 * 50;
            }

            application().states().setVideoBitrate(progress);
            mMainBitrateText.setText(String.format(
                    mMainBitrateTextFormat, progress));
            if (mListener != null) mListener.onBitrateSelected(progress);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.live_room_setting_back:
                if (mCurPage != PAGE_MAIN) {
                    mCurPage = PAGE_MAIN;
                    gotoPage(PAGE_MAIN);
                } else {
                    if (mListener != null) mListener.onSettingBackPressed();
                }

                break;
            case R.id.live_room_setting_resolution:
                mCurPage = PAGE_RESOLUTION;
                gotoPage(PAGE_RESOLUTION);
                break;
            case R.id.live_room_setting_framerate:
                mCurPage = PAGE_FRAME_RATE;
                gotoPage(PAGE_FRAME_RATE);
                break;
        }
    }

    private void gotoPage(int page) {
        switch (page) {
            case PAGE_MAIN:
                gotoMainPage();
                break;
            case PAGE_RESOLUTION:
                gotoResolutionPage();
                break;
            case PAGE_FRAME_RATE:
                gotoFrameRatePage();
                break;
        }
    }

    private void gotoMainPage() {
        mBackIcon.setVisibility(mCanFallback ? VISIBLE : GONE);
        mTitle.setText(R.string.live_room_setting_action_sheet_title);
        mContainer.removeAllViews();
        mContainer.addView(mMain);

        mMainResolutionText = findViewById(R.id.live_room_setting_main_resolution_text);
        mMainFrameRateText = findViewById(R.id.live_room_setting_main_framerate_text);
        mBitrateSeekBar = findViewById(R.id.live_room_setting_bitrate_progress_bar);
        mBitrateSeekBar.setMax(Global.Constants.VIDEO_MAX_BITRATE);
        mBitrateSeekBar.setOnSeekBarChangeListener(this);
        mMainBitrateText = findViewById(R.id.live_room_setting_bitrate_value_text);
        mMainBitrateTextFormat = getResources().getString(R.string.live_room_setting_bitrate_value_format);
        setMainPageText();
    }

    private void setMainPageText() {
        mMainResolutionText.setText(Global.Constants.RESOLUTIONS[
                application().states().resolutionIndex()]);
        mMainFrameRateText.setText(Global.Constants.FRAME_RATES[
                application().states().frameRateIndex()]);
        mBitrateSeekBar.setProgress(application().states().videoBitrate());
        mMainBitrateText.setText(String.format(mMainBitrateTextFormat,
                application().states().videoBitrate()));
    }

    private void gotoResolutionPage() {
        mBackIcon.setVisibility(View.VISIBLE);
        mTitle.setText(R.string.live_room_setting_title_resolution);
        mResolutionRecycler = new RecyclerView(getContext());
        LinearLayoutManager manager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        mResolutionRecycler.setLayoutManager(manager);
        mResolutionRecycler.setAdapter(new ResolutionAdapter());
        mResolutionRecycler.addItemDecoration(new LineDecorator());
        mContainer.removeAllViews();
        mContainer.addView(mResolutionRecycler);
    }

    private void gotoFrameRatePage() {
        mBackIcon.setVisibility(View.VISIBLE);
        mTitle.setText(R.string.live_room_setting_title_framerate);
        mFrameRateRecycler = new RecyclerView(getContext());
        LinearLayoutManager manager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        mFrameRateRecycler.setLayoutManager(manager);
        mFrameRateRecycler.setAdapter(new FrameRateAdapter());
        mFrameRateRecycler.addItemDecoration(new LineDecorator());
        mContainer.removeAllViews();
        mContainer.addView(mFrameRateRecycler);
    }

    private class ResolutionAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ResolutionViewHolder(mInflater.inflate(
                    R.layout.live_room_setting_list_item_text_only,
                    parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ResolutionViewHolder resolutionHolder = (ResolutionViewHolder) holder;
            resolutionHolder.textView.setText(Global.Constants.RESOLUTIONS[position]);
            holder.itemView.setActivated(position == application().states().resolutionIndex());
            resolutionHolder.setPosition(position);
        }

        @Override
        public int getItemCount() {
            return Global.Constants.RESOLUTIONS.length;
        }
    }

    private class ResolutionViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView textView;
        int position;
        ResolutionViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.live_room_setting_item_text);
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    application().states().setResolutionIndex(position);
                    mResolutionRecycler.getAdapter().notifyDataSetChanged();
                    if (mListener != null) mListener.onResolutionSelected(position);
                }
            });
        }

        void setPosition(int position) {
            this.position = position;
        }
    }

    private class FrameRateAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new FrameRateViewHolder(mInflater.inflate(
                    R.layout.live_room_setting_list_item_text_only,
                    parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            FrameRateViewHolder frameRateHolder = (FrameRateViewHolder) holder;
            frameRateHolder.textView.setText(Global.Constants.FRAME_RATES[position]);
            frameRateHolder.setPosition(position);
            holder.itemView.setActivated(
                    position == application().states().frameRateIndex());
        }

        @Override
        public int getItemCount() {
            return Global.Constants.FRAME_RATES.length;
        }
    }

    private class FrameRateViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView textView;
        int position;
        FrameRateViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.live_room_setting_item_text);
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    application().states().setFrameRateIndex(position);
                    mFrameRateRecycler.getAdapter().notifyDataSetChanged();
                    if (mListener != null) mListener.onFrameRateSelected(position);
                }
            });
        }

        void setPosition(int position) {
            this.position = position;
        }
    }

    private class LineDecorator extends RecyclerView.ItemDecoration {
        @Override
        public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            Rect rect = new Rect();
            Paint paint = new Paint();
            paint.setColor(Global.Constants.DIVIDER_COLOR);

            int count = parent.getChildCount();
            for (int i = 0; i < count - 1; i++) {
                if (parent == mResolutionRecycler &&
                    application().states().resolutionIndex() == i + 1) {
                    continue;
                } else if (parent == mFrameRateRecycler &&
                    application().states().frameRateIndex() == i + 1) {
                    continue;
                }

                View child = parent.getChildAt(i);
                child.getDrawingRect(rect);
                int startX = rect.left + mPaddingHorizontal;
                int width = rect.right - rect.left - startX * 2;
                int height = rect.bottom - rect.top;
                int startY = height * (i + 1);
                c.drawRect(new Rect(startX, startY,
                        startX + width, startY + mDividerHeight), paint);
            }
        }
    }
}