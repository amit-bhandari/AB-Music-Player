package com.music.player.bhandari.m.activity

import android.app.PendingIntent
import android.content.*
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.vending.billing.IInAppBillingService
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.model.*
import com.music.player.bhandari.m.utils.UtilityFun.restartApp
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Copyright 2017 Amit Bhandari AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class ActivityDonateFunds : AppCompatActivity() {
    var mService: IInAppBillingService? = null
    var mServiceConn: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
        }

        override fun onServiceConnected(
            name: ComponentName,
            service: IBinder
        ) {
            mService = IInAppBillingService.Stub.asInterface(service)

            //check if user has already removed ads
            //in case, he removed ads, and reinstalled app
            val ownedItems: Bundle?
            try {
                ownedItems = mService!!.getPurchases(3, packageName, "inapp", null)
            } catch (e: RemoteException) {
                e.printStackTrace()
                return
            }
            val response: Int = ownedItems.getInt("RESPONSE_CODE")
            if (response == 0) {
                val ownedSkus: ArrayList<String>? =
                    ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST")
                if (ownedSkus != null) {
                    for (i in ownedSkus.indices) {
                        val sku: String = ownedSkus.get(i)
                        if ((sku == getString(R.string.donate_beer))) {
                            startActivity(Intent(this@ActivityDonateFunds,
                                ActivitySettings::class.java))
                            Toast.makeText(applicationContext,
                                "You already have bought me beer!",
                                Toast.LENGTH_LONG).show()
                            finish()
                            return
                        } else if ((sku == getString(R.string.donate_beer_box))) {
                            startActivity(Intent(this@ActivityDonateFunds,
                                ActivitySettings::class.java))
                            Toast.makeText(applicationContext,
                                "You already have bought me beer box!",
                                Toast.LENGTH_LONG).show()
                            finish()
                            return
                        } else if ((sku == getString(R.string.donate_beer))) {
                            startActivity(Intent(this@ActivityDonateFunds,
                                ActivitySettings::class.java))
                            Toast.makeText(applicationContext,
                                "You already have bought me coffee!",
                                Toast.LENGTH_LONG).show()
                            finish()
                            return
                        }
                    }
                }
                // if continuationToken != null, call getPurchases again
                // and pass in the token to retrieve more items
            }

            //start buy procedure
            var product_id: String? = ""
            when (intent.getIntExtra("donate_type", -1)) {
                Constants.DONATE.BEER -> product_id = getString(R.string.donate_beer)
                Constants.DONATE.JD -> product_id = getString(R.string.donate_beer_box)
                Constants.DONATE.COFFEE -> product_id = getString(R.string.donate_coffee)
                else -> {
                    startActivity(Intent(this@ActivityDonateFunds, ActivitySettings::class.java))
                    finish()
                }
            }
            //String product_id = "android.test.purchased";
            var buyIntentBundle: Bundle? = null
            try {
                buyIntentBundle = mService!!.getBuyIntent(3, packageName,
                    product_id, "inapp", "")
            } catch (ignored: Exception) {
            }
            if (buyIntentBundle != null && buyIntentBundle.getInt("RESPONSE_CODE", 1) == 0) {
                val pendingIntent: PendingIntent? = buyIntentBundle.getParcelable("BUY_INTENT")
                try {
                    if (pendingIntent != null) {
                        startIntentSenderForResult(pendingIntent.intentSender,
                            1001, Intent(), 0, 0,
                            0)
                    }
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            val purchaseData: String? = data!!.getStringExtra("INAPP_PURCHASE_DATA")
            if (resultCode == RESULT_OK) {
                try {
                    val jo: JSONObject = JSONObject(purchaseData)
                    val sku: String = jo.getString("productId")
                    Toast.makeText(applicationContext,
                        "Thank you for you contribution towards the app Development!",
                        Toast.LENGTH_LONG).show()
                } catch (e: JSONException) {
                    /* alert("Failed to parse purchase data.");*/
                    Toast.makeText(this, "FAILED!", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(applicationContext, "Transaction failed!", Toast.LENGTH_LONG)
                    .show()
            }
        }
        startActivity(Intent(this@ActivityDonateFunds, ActivitySettings::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //if player service not running, kill the app
        if (MyApp.Companion.getService() == null) {
            restartApp()
        }
        val serviceIntent: Intent = Intent("com.android.vending.billing.InAppBillingService.BIND")
        serviceIntent.setPackage("com.android.vending")
        bindService(serviceIntent, mServiceConn, BIND_AUTO_CREATE)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (mService != null) {
            unbindService(mServiceConn)
        }
    }
}