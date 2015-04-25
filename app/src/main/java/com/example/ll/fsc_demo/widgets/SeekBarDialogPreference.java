package com.example.ll.fsc_demo.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.ll.fsc_demo.R;

public class SeekBarDialogPreference extends DialogPreference {
    private static class SavedState extends BaseSavedState {
        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        int min;
        int max;
        int step;
        int ratio;
        int progress;

        public SavedState(Parcel source) {
            super(source);
            progress = source.readInt();
            min = source.readInt();
            max = source.readInt();
            step = source.readInt();
            ratio = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(progress);
            dest.writeInt(min);
            dest.writeInt(max);
            dest.writeInt(step);
            dest.writeInt(ratio);
        }
    }

    private int mMin = 0;
    private int mMax = 1;
    private int mProgress = 0;
    private int mStep = 1;
    private int mRatio = 1;

    private int mChangedProgress = 0;   /// only used by dialog pass value to parent
    private TextView mSummaryView;

    public SeekBarDialogPreference(Context context) {
        this(context, null);
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.seekBarDialogPreferenceStyle);
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs,
                                   int defStyle) {
        super(context, attrs, defStyle);
        context = getContext();
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.SeekBarDialogPreference, defStyle,
                R.style.Holo_SeekBarDialogPreference);

        setMin(a.getInt(R.styleable.SeekBarDialogPreference_min, mMin));
        setMax(a.getInt(R.styleable.SeekBarDialogPreference_max, mMax));
        setStep(a.getInt(R.styleable.SeekBarDialogPreference_step, mStep));
        setRatio(a.getInt(R.styleable.SeekBarDialogPreference_ratio, mRatio));
        a.recycle();
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mSummaryView = (TextView) view.findViewById(android.R.id.summary);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        final View dialogSummaryView = view.findViewById(android.R.id.summary);
        if (dialogSummaryView != null) {
            ((TextView)dialogSummaryView).setText(getSummary());
        }

        final View dialogSeekbarView = view.findViewById(R.id.seekbar);
        if (dialogSeekbarView instanceof SeekBar) {
            Log.d("FIND SEEKBAR ", " HAHA ");
            final SeekBar skb = ((SeekBar) dialogSeekbarView);
            skb.setMax(toAbsProgress(mMax));
            skb.setProgress(toAbsProgress(mProgress));
            skb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                final private TextView mSummaryView = (TextView)dialogSummaryView;
                private boolean mTouching = false;
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        mSummaryView.setText(getSummary(toRealProgress(progress)));
                        mChangedProgress = toRealProgress(progress);
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mTouching = true;
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mTouching = false;
                }
            });
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            setProgress(mChangedProgress);
        }
    }

    @Override
    protected Integer onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mProgress = myState.progress;
        mMin = myState.min;
        mMax = myState.max;
        mStep = myState.step;
        mRatio = myState.ratio;
        notifyChanged();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        final SavedState myState = new SavedState(superState);
        myState.progress = mProgress;
        myState.max = mMax;
        myState.min = mMin;
        myState.step = mStep;
        myState.ratio = mRatio;
        return myState;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        int def = defaultValue instanceof Integer ? (Integer) defaultValue
                : defaultValue == null ? 0 : Integer.parseInt(defaultValue
                .toString());
        setProgress(restoreValue ? getPersistedInt(def) : def);
    }

    @Override
    public CharSequence getSummary() {
        return getSummary(mProgress);
    }

    private CharSequence getSummary(int progress) {
        CharSequence tmp = super.getSummary();
        if (TextUtils.isEmpty(tmp)) {
            return null;
        }
        return String.format(tmp.toString(), ((float) progress / mRatio));
    }

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
    }

    public void setMax(int max) {
        if (max == mMax) {
            return;
        }

        final boolean wasBlocking = shouldDisableDependents();
        mMax = max;
        if (shouldDisableDependents() != wasBlocking) {
            notifyDependencyChange(!wasBlocking);
        }
        notifyChanged();
    }

    public void setMin(int min) {
        if (min == mMin) {
            return;
        }

        final boolean wasBlocking = shouldDisableDependents();
        mMin = min;
        if (shouldDisableDependents() != wasBlocking) {
            notifyDependencyChange(!wasBlocking);
        }
        notifyChanged();
    }

    public void setStep(int step) {
        if (step == 0) {
            step = 1;
        }

        if (step == mStep) {
            return;
        }

        final boolean wasBlocking = shouldDisableDependents();
        mStep = step;
        if (shouldDisableDependents() != wasBlocking) {
            notifyDependencyChange(!wasBlocking);
        }
        notifyChanged();
    }

    public void setRatio(int ratio) {
        if (ratio == mRatio) {
            return;
        }

        final boolean wasBlocking = shouldDisableDependents();
        mRatio = ratio;
        if (shouldDisableDependents() != wasBlocking) {
            notifyDependencyChange(!wasBlocking);
        }
        notifyChanged();
    }

    public int getRatio() {
        return mRatio;
    }

    public void setProgress(int progress) {
        if (progress > mMax) {
            progress = mMax;
        }
        if (progress < mMin) {
            progress = mMin;
        }

        if (progress == mProgress) {
            return;
        }

        if (callChangeListener(progress)) {

            final boolean wasBlocking = shouldDisableDependents();
            mProgress = progress;
            if (mSummaryView != null) {
                mSummaryView.setText(getSummary());
            }
            persistInt(mProgress);
            if (shouldDisableDependents() != wasBlocking) {
                notifyDependencyChange(!wasBlocking);
            }
            notifyChanged();
        }
    }
    private int toRealProgress(int abs) {
        return abs * mStep + mMin;
    }

    private int toAbsProgress(int real) {
        return (real - mMin) / mStep;
    }
}
