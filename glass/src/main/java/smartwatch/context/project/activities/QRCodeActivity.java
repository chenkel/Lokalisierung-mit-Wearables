/*
 * Copyright (C) 2013 The Android Open Source Project
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

package smartwatch.context.project.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollView;

import java.util.ArrayList;
import java.util.List;

import smartwatch.context.project.R;
import smartwatch.context.project.card.CardAdapter;

/**
 * Creates a card scroll view with examples of different image layout cards.
 */
public final class QRCodeActivity extends Activity {

    private CardScrollView mCardScroller;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(new CardAdapter(createCards(this)));
        setContentView(mCardScroller);
    }

    /**
     * Creates list of cards that showcase different type of {@link CardBuilder} API.
     */
    private List<CardBuilder> createCards(Context context) {
        ArrayList<CardBuilder> cards = new ArrayList<>();

        cards.add(new CardBuilder(context, CardBuilder.Layout.COLUMNS)
                .setText(getString(R.string.wifi_ble_localization))
                .setIcon(R.drawable.ic_wifi_ble)
                .setFootnote(R.string.wifi_ble_footnote));

        return cards;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        super.onPause();
    }
}
