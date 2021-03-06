/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.app.servertransaction;

import android.annotation.UnsupportedAppUsage;
import android.app.ClientTransactionHandler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Trace;

import com.android.internal.content.ReferrerIntent;

import java.util.List;
import java.util.Objects;

/**
 * New intent message.
 * @hide
 */
public class NewIntentItem extends ClientTransactionItem {

    @UnsupportedAppUsage
    private List<ReferrerIntent> mIntents;
    private boolean mPause;

    // TODO(lifecycler): Switch new intent handling to this scheme.
    /*@Override
    public int getPostExecutionState() {
        return ON_RESUME;
    }*/

    @Override
    public void execute(ClientTransactionHandler client, IBinder token,
            PendingTransactionActions pendingActions) {
        Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityNewIntent");
        client.handleNewIntent(token, mIntents, mPause);
        Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
    }


    // ObjectPoolItem implementation

    private NewIntentItem() {}

    /** Obtain an instance initialized with provided params. */
    public static NewIntentItem obtain(List<ReferrerIntent> intents, boolean pause) {
        NewIntentItem instance = ObjectPool.obtain(NewIntentItem.class);
        if (instance == null) {
            instance = new NewIntentItem();
        }
        instance.mIntents = intents;
        instance.mPause = pause;

        return instance;
    }

    @Override
    public void recycle() {
        mIntents = null;
        mPause = false;
        ObjectPool.recycle(this);
    }


    // Parcelable implementation

    /** Write to Parcel. */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBoolean(mPause);
        dest.writeTypedList(mIntents, flags);
    }

    /** Read from Parcel. */
    private NewIntentItem(Parcel in) {
        mPause = in.readBoolean();
        mIntents = in.createTypedArrayList(ReferrerIntent.CREATOR);
    }

    public static final Parcelable.Creator<NewIntentItem> CREATOR =
            new Parcelable.Creator<NewIntentItem>() {
        public NewIntentItem createFromParcel(Parcel in) {
            return new NewIntentItem(in);
        }

        public NewIntentItem[] newArray(int size) {
            return new NewIntentItem[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final NewIntentItem other = (NewIntentItem) o;
        return mPause == other.mPause && Objects.equals(mIntents, other.mIntents);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (mPause ? 1 : 0);
        result = 31 * result + mIntents.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "NewIntentItem{pause=" + mPause + ",intents=" + mIntents + "}";
    }
}
