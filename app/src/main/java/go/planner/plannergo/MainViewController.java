/*
 * Copyright 2017 Google Inc. All Rights Reserved.
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

package go.planner.plannergo;

import android.content.SharedPreferences;
import android.util.Log;

import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.Purchase;

import java.util.List;

import go.planner.plannergo.billing.BillingManager;
import go.planner.plannergo.skulist.row.FiveDollarDelegate;
import go.planner.plannergo.skulist.row.OneDollarDelegate;

import static android.content.Context.MODE_PRIVATE;

/**
 * Handles control logic of the FeedbackActivity
 */
public class MainViewController {
    private static final String TAG = "MainViewController";

    private final UpdateListener mUpdateListener;
    private FeedbackActivity mActivity;

    //Tracks if purchased various donation levels
    private boolean isOneDollarDonor;
    private boolean isFiveDollarDonor;

    public MainViewController(FeedbackActivity activity) {
        mUpdateListener = new UpdateListener();
        mActivity = activity;
        loadData();
    }

    public UpdateListener getUpdateListener() {
        return mUpdateListener;
    }

    public boolean isOneDollarDonor() {
        return isOneDollarDonor;
    }

    public boolean isFiveDollarDonor() {
        return isFiveDollarDonor;
    }

    /**
     * Handler to billing updates
     */
    private class UpdateListener implements BillingManager.BillingUpdatesListener {
        @Override
        public void onBillingClientSetupFinished() {
            mActivity.onBillingManagerSetupFinished();
        }

        @Override
        public void onConsumeFinished(String token, @BillingResponse int result) {
            Log.d(TAG, "Consumption finished. Purchase token: " + token + ", result: " + result);

            if (result == BillingResponse.OK) {
                Log.d(TAG, "Saving data...");
                saveData();
            } else {
                mActivity.alert(R.string.alert_error_consuming, result);
            }

            mActivity.showRefreshedUi();
            Log.d(TAG, "End consumption flow.");
        }

        @Override
        public void onPurchasesUpdated(List<Purchase> purchaseList) {

            for (Purchase purchase : purchaseList) {
                switch (purchase.getSku()) {
                    case OneDollarDelegate.SKU_ID:
                        Log.d(TAG, "You donated $1! Thanks so much!");
                        isOneDollarDonor = true;
                        break;
                    case FiveDollarDelegate.SKU_ID:
                        Log.d(TAG, "You donated $5! Thanks you so very much!");
                        isFiveDollarDonor = true;
                        break;
                }
            }

            mActivity.showRefreshedUi();
        }
    }

    /**
     * Save current purchases data to storage using SavedPreferences
     *
     * Not secure, but this isn't important since "stealing" a donation nets you nothing as of now.
     */
    private void saveData() {
        SharedPreferences.Editor spe = mActivity.getPreferences(MODE_PRIVATE).edit();
        spe.putBoolean("isOneDollarDonor", isOneDollarDonor);
        spe.putBoolean("isFiveDollarDonor", isFiveDollarDonor);
        spe.apply();
        Log.d(TAG, "Saved data: isOneDollarDonor = " + String.valueOf(isOneDollarDonor));
    }

    private void loadData() {
        SharedPreferences sp = mActivity.getPreferences(MODE_PRIVATE);
        isOneDollarDonor = sp.getBoolean("isOneDollarDonor", false);
        isFiveDollarDonor = sp.getBoolean("isFiveDollarDonor", false);
        Log.d(TAG, "Loaded data: isOneDollarDonor = " + String.valueOf(isOneDollarDonor));
    }
}