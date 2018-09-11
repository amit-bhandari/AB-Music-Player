package com.music.player.bhandari.m.activity;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.vending.billing.IInAppBillingService;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.UIElementHelper.MyDialogBuilder;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.utils.UtilityFun;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 Copyright 2017 Amit Bhandari AB

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

public class ActivityRemoveAds extends AppCompatActivity {

    IInAppBillingService mService;
    boolean dontDismiss = false;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);

            //check if user has already removed ads
            //in case, he removed ads, and reinstalled app
            Bundle ownedItems = null;
            try {
                ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
            } catch (RemoteException e) {

                e.printStackTrace();
                return;
            }

            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> ownedSkus =
                        ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");

                if (ownedSkus != null) {
                    for (int i = 0; i < ownedSkus.size(); ++i) {
                        String sku = ownedSkus.get(i);
                        if (sku.equals(getString(R.string.remove_ads))) {
                            startActivity(new Intent(ActivityRemoveAds.this,ActivityMain.class));
                            Toast.makeText(getApplicationContext(), getString(R.string.ads_already_removed),Toast.LENGTH_LONG).show();
                            MyApp.getPref().edit().putBoolean(getString(R.string.pref_remove_ads_after_payment),true).apply();
                            finish();
                            return;
                        }
                }
                }
                // if continuationToken != null, call getPurchases again
                // and pass in the token to retrieve more items
            }

            //start buy procedure
            String product_id = getString(R.string.remove_ads);
            //String product_id = "android.test.purchased";
            Bundle buyIntentBundle = null;
            try {
                buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                        product_id , "inapp", "");
                if(buyIntentBundle.getInt("RESPONSE_CODE", 1)==0) {
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                    try {
                        startIntentSenderForResult(pendingIntent.getIntentSender(),
                                1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                                Integer.valueOf(0));
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception ignored){

            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data!=null && requestCode == 1001 ) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    Toast.makeText(getApplicationContext(), getString(R.string.ads_removed),Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), getString(R.string.ads_still_showing),Toast.LENGTH_LONG).show();
                    MyApp.getPref().edit().putBoolean(getString(R.string.pref_remove_ads_after_payment),true).apply();
                    /*alert("You have bought the " + sku + ". Excellent choice,
                            adventurer!");*/
                }
                catch (JSONException e) {
                   /* alert("Failed to parse purchase data.");*/
                    Toast.makeText(this,getString(R.string.error_something_wrong),Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }else {
                Toast.makeText(getApplicationContext(),getString(R.string.error_trans_failed),Toast.LENGTH_LONG).show();

            }
        }
        startActivity(new Intent(ActivityRemoveAds.this,ActivityMain.class));
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //if player service not running, kill the app
        if(MyApp.getService()==null){
           UtilityFun.restartApp();
        }


        MaterialDialog dialog = new MyDialogBuilder(this)
                .title(getString(R.string.nav_remove_ads))
                .content(R.string.remove_ads_content)
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if(!dontDismiss) {
                            startActivity(new Intent(ActivityRemoveAds.this, ActivityMain.class));
                            finish();
                        }
                    }
                })
                .positiveText(getString(R.string.remove_ads_permanently))
                .negativeText(getString(R.string.cancel))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dontDismiss = false;
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dontDismiss = true;

                        Intent serviceIntent =
                                new Intent("com.android.vending.billing.InAppBillingService.BIND");
                        serviceIntent.setPackage("com.android.vending");
                        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

                        try {
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "remove_ads_launched");
                            UtilityFun.logEvent(bundle);
                        }catch (Exception ignored){
                        }

                        if(getIntent().getExtras()!=null && getIntent().getExtras().getBoolean("from_notif")){
                            try {
                                Bundle bundle = new Bundle();
                                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "notification_clicked");
                                UtilityFun.logEvent(bundle);
                            }catch (Exception ignored){
                            }
                        }
                    }
                })
                .build();

        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

        dialog.show();
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

}
