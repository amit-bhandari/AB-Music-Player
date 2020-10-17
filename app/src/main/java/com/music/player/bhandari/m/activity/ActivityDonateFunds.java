package com.music.player.bhandari.m.activity;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.MyApp;
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

public class ActivityDonateFunds extends AppCompatActivity {

    IInAppBillingService mService;

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
                        if (sku.equals(getString(R.string.donate_beer))) {
                            startActivity(new Intent(ActivityDonateFunds.this,ActivitySettings.class));
                            Toast.makeText(getApplicationContext(), "You already have bought me beer!",Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }else if(sku.equals(getString(R.string.donate_beer_box))){
                            startActivity(new Intent(ActivityDonateFunds.this,ActivitySettings.class));
                            Toast.makeText(getApplicationContext(), "You already have bought me beer box!",Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }else if(sku.equals(getString(R.string.donate_beer))){
                            startActivity(new Intent(ActivityDonateFunds.this,ActivitySettings.class));
                            Toast.makeText(getApplicationContext(), "You already have bought me coffee!",Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                }
                }
                // if continuationToken != null, call getPurchases again
                // and pass in the token to retrieve more items
            }

            //start buy procedure
            String product_id = "";
            switch (getIntent().getIntExtra("donate_type",-1)){
                case Constants.DONATE.BEER:
                    product_id = getString(R.string.donate_beer);
                    break;

                case Constants.DONATE.JD:
                    product_id = getString(R.string.donate_beer_box);
                    break;

                case Constants.DONATE.COFFEE:
                    product_id = getString(R.string.donate_coffee);
                    break;

                default:
                    startActivity(new Intent(ActivityDonateFunds.this,ActivitySettings.class));
                    finish();
            }
            //String product_id = "android.test.purchased";
            Bundle buyIntentBundle = null;
            try {
                buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                        product_id , "inapp", "");
            }catch (Exception ignored){

            }
            if (buyIntentBundle != null && buyIntentBundle.getInt("RESPONSE_CODE", 1) == 0) {
                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                try {
                    if (pendingIntent != null) {
                        startIntentSenderForResult(pendingIntent.getIntentSender(),
                                1001, new Intent(), 0, 0,
                                0);
                    }
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }


        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    Toast.makeText(getApplicationContext(), "Thank you for you contribution towards the app Development!"
                            ,Toast.LENGTH_LONG).show();
                }
                catch (JSONException e) {
                   /* alert("Failed to parse purchase data.");*/
                    Toast.makeText(this,"FAILED!",Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }else {
                Toast.makeText(getApplicationContext(),"Transaction failed!",Toast.LENGTH_LONG).show();

            }
        }
        startActivity(new Intent(ActivityDonateFunds.this,ActivitySettings.class));
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //if player service not running, kill the app
        if(MyApp.getService()==null){
            UtilityFun.restartApp();
        }


        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
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
