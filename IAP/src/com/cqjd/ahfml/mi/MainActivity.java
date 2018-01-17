/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

package com.cqjd.ahfml.mi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.u3d.iap.google.util.IabBroadcastReceiver;
import com.u3d.iap.google.util.IabHelper;
import com.u3d.iap.google.util.IabHelper.IabAsyncInProgressException;
import com.u3d.iap.google.util.IabResult;
import com.u3d.iap.google.util.Inventory;
import com.u3d.iap.google.util.Purchase;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Example game using in-app billing version 3.
 *
 * Before attempting to run this sample, please read the README file. It
 * contains important information on how to set up this project.
 *
 * All the game-specific logic is implemented here in MainActivity, while the
 * general-purpose boilerplate that can be reused in any app is provided in the
 * classes in the util/ subdirectory. When implementing your own application,
 * you can copy over util/*.java to make use of those utility classes.
 *
 * This game is a simple "driving" game where the player can buy gas
 * and drive. The car has a tank which stores gas. When the player purchases
 * gas, the tank fills up (1/4 tank at a time). When the player drives, the gas
 * in the tank diminishes (also 1/4 tank at a time).
 *
 * The user can also purchase a "premium upgrade" that gives them a red car
 * instead of the standard blue one (exciting!).
 *
 * The user can also purchase a subscription ("infinite gas") that allows them
 * to drive without using up any gas while that subscription is active.
 *
 * It's important to note the consumption mechanics for each item.
 *
 * PREMIUM: the item is purchased and NEVER consumed. So, after the original
 * purchase, the player will always own that item. The application knows to
 * display the red car instead of the blue one because it queries whether
 * the premium "item" is owned or not.
 *
 * INFINITE GAS: this is a subscription, and subscriptions can't be consumed.
 *
 * GAS: when gas is purchased, the "gas" item is then owned. We consume it
 * when we apply that item's effects to our app's world, which to us means
 * filling up 1/4 of the tank. This happens immediately after purchase!
 * It's at this point (and not when the user drives) that the "gas"
 * item is CONSUMED. Consumption should always happen when your game
 * world was safely updated to apply the effect of the purchase. So,
 * in an example scenario:
 *
 * BEFORE:      tank at 1/2
 * ON PURCHASE: tank at 1/2, "gas" item is owned
 * IMMEDIATELY: "gas" is consumed, tank goes to 3/4
 * AFTER:       tank at 3/4, "gas" item NOT owned any more
 *
 * Another important point to notice is that it may so happen that
 * the application crashed (or anything else happened) after the user
 * purchased the "gas" item, but before it was consumed. That's why,
 * on startup, we check if we own the "gas" item, and, if so,
 * we have to apply its effects to our world and consume it. This
 * is also very important!
 */
public class MainActivity extends UnityPlayerActivity
{
    // Debug tag, for logging
    static final String TAG = "Google IAB";

    // Does the user have the premium upgrade?
    boolean mIsPremium = false;

    // Does the user have an active subscription to the infinite gas plan?
    boolean mSubscribedToInfiniteGas = false;

    // Will the subscription auto-renew?
    boolean mAutoRenewEnabled = false;

    // Tracks the currently owned infinite gas SKU, and the options in the Manage dialog
    String mInfiniteGasSku = "";
    String mFirstChoiceSku = "";
    String mSecondChoiceSku = "";

    // Used to select between purchasing gas on a monthly or yearly basis
    String mSelectedSubscriptionPeriod = "";

    // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
    static final String SKU_PREMIUM = "premium";
    static final String SKU_GAS = "gas";

    // SKU for our subscription (infinite gas)
    static final String SKU_INFINITE_GAS_MONTHLY = "infinite_gas_monthly";
    static final String SKU_INFINITE_GAS_YEARLY = "infinite_gas_yearly";

    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;

    // Graphics for the gas gauge
//    static int[] TANK_RES_IDS = { R.drawable.gas0, R.drawable.gas1, R.drawable.gas2,
//            R.drawable.gas3, R.drawable.gas4 };

    // How many units (1/4 tank is our unit) fill in the tank.
    static final int TANK_MAX = 4;

    // Current amount of gas in tank, in units
    int mTank;

    // The helper object
    IabHelper mHelper;

    // Provides purchase notification while this app is running
    IabBroadcastReceiver mBroadcastReceiver;
    String msgHandle;
    /* base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
     * (that you got from the Google Play developer console). This is not your
     * developer public key, it's the *app-specific* public key.
     *
     * Instead of just storing the entire literal string here embedded in the
     * program,  construct the key at runtime from pieces or
     * use bit manipulation (for example, XOR with some other string) to hide
     * the actual key.  The key itself is not secret information, but we don't
     * want to make it easy for an attacker to replace the public key with one
     * of their own and then fake messages from the server.
     */
    String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0RVRj5tpQpGl9GWRUiLeF0oY2n4McdUS/tDXNHVyuj3GsfzymGrdm+hE4vPjqyXbxMfdVJ3f4GXSwNz0RDubxNycenCw5MedEocU/Ma6ZoMKWfoc5m0utokMWL/RyM76+Frpl6OEmSt4t5LdEVvlvTD58Ulthpeaa5wNReTombwI+lX1k2WrRT/EOYaeRqkY90YRQfFa3HxaiCBXUv/2V2tAeL9KOSo3EhSqsB+gw+JGxjGDX46IplLqzGyeAOvbbSLsIkkl1g3zEXSs5QBwH56ZT21sdWEJN5AufymNy4NkoimQDqc0AB0k6TdgwVyb+OcOXMkTubNA4vCIyIPHEQIDAQAB";
    //产品ID
    String[] skus;
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        //StartSetup();
    }
    //初始化
	public void StartSetup(String msgHandle, String skus)
	{
		this.msgHandle = msgHandle;
		this.skus = skus.split(",");
	    // Create the helper, passing it our context and the public key to verify signatures with
	    Log.w(TAG, "Creating IAB helper.");
	    mHelper = new IabHelper(this, base64EncodedPublicKey);	
	    // enable debug logging (for a production application, you should set this to false).
	    mHelper.enableDebugLogging(true);
	
	    // Start setup. This is asynchronous and the specified listener
	    // will be called once setup completes.
	    Log.w(TAG, "Starting setup.");
	    mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() 
	    {
	        public void onIabSetupFinished(IabResult result) 
	        {
	            Log.w(TAG, "Setup finished.");
	
	            if (!result.isSuccess()) 
	            {
	                // Oh noes, there was a problem.
	                complain("Problem setting up in-app billing: " + result);
	                return;
	            }
	
	            // Have we been disposed of in the meantime? If so, quit.
	            if (mHelper == null) return;
	
	            // IAB is fully set up. Now, let's get an inventory of stuff we own.
	            Log.w(TAG, "Setup successful. Querying inventory.");
	            try 
	            {
	                mHelper.queryInventoryAsync(mGotInventoryListener);
	            } 
	            catch (IabAsyncInProgressException e) 
	            {
	                complain("Error querying inventory. Another async operation in progress.");
	            }
	        }
	    });
	}
    //消耗存货
    public void ConsumeInventory()
    {
		Handler mainHandler = new Handler(Looper.getMainLooper());
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				// 已在主线程中，可以更新UI
		        setWaitScreen(true,"");
		        Log.w(TAG, "Querying inventory.");
		        try 
		        {
		            mHelper.queryInventoryAsync(mGotInventoryListener);
		        } 
		        catch (IabAsyncInProgressException e) 
		        {
		            complain("Error querying inventory. Another async operation in progress.");
		        }
			}
		});
    }
    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener()
    {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) 
        {
            Log.w(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure())
            {
                complain("Failed to query inventory: " + result);
                setWaitScreen(false,"Failed to query inventory: " + result);
                return;
            }

            Log.w(TAG, "Query inventory was successful.");
            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */
            if (skus == null) return;
            for(int i = 0; i < skus.length; i++)
            {
                Log.w(TAG, "skus["+i+"]:"+skus[i]);
                Purchase purchase = inventory.getPurchase(skus[i]);
                if(purchase != null && verifyDeveloperPayload(purchase))
                {
                    Log.w(TAG, "We have "+skus[i]+". Consuming it.");
                    try 
                    {
                        mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                    } 
                    catch (IabAsyncInProgressException e)
                    {
                    	complain("Error consuming gas. Another async operation in progress.");
                    }
                }
            }
            setWaitScreen(false,"");
            Log.w(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };
    //购买
	public void Purchase(String sku,String payload)
	{
        // launch the gas purchase UI flow.
        // We will be notified of completion via mPurchaseFinishedListener
        setWaitScreen(true,"");
        Log.w(TAG, "Launching purchase flow for "+sku+".");

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        try
        {
            mHelper.launchPurchaseFlow(this, sku, RC_REQUEST, mPurchaseFinishedListener, payload);
        }
        catch (IabAsyncInProgressException e) 
        {
            complain("Error launching purchase flow. Another async operation in progress.");
            setWaitScreen(false,"Error launching purchase flow. Another async operation in progress.");
        }
	}
    // User clicked the "Upgrade to Premium" button.
    public void onUpgradeAppButtonClicked(View arg0) 
    {
        Log.w(TAG, "Upgrade button clicked; launching purchase flow for upgrade.");
        setWaitScreen(true,"");

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        try
        {
            mHelper.launchPurchaseFlow(this, SKU_PREMIUM, RC_REQUEST, mPurchaseFinishedListener, payload);
        } 
        catch (IabAsyncInProgressException e) 
        {
            complain("Error launching purchase flow. Another async operation in progress.");
            setWaitScreen(false,"Error launching purchase flow. Another async operation in progress.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.w(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data))
        {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else
        {
            Log.w(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p)
    {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener()
    {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {
            Log.w(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure())
            {
                complain("Error purchasing: " + result);
                setWaitScreen(false,"Error purchasing: " + result);
                //如果未消耗,自动消耗掉
                if(result.getResponse()==IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED)
                {
                	ConsumeInventory();
                }
                return;
            }
            if (!verifyDeveloperPayload(purchase))
            {
                complain("Error purchasing. Authenticity verification failed.");
                setWaitScreen(false,"Error purchasing. Authenticity verification failed.");
                return;
            }

            Log.w(TAG, "Purchase successful.");
            Send2Unity("OnPurchaseFinished",purchase,result);
        }
    };

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener()
    {
        public void onConsumeFinished(Purchase purchase, IabResult result)
        {
            Log.w(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess())
            {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.w(TAG, "Consumption successful. Provisioning.");
            }
            else 
            {
                complain("Error while consuming: " + result);
            }
            setWaitScreen(false,"");
            Send2Unity("OnConsumeFinished",purchase,result);
            Log.w(TAG, "End consumption flow.");
        }
    };
    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() 
    {
        super.onDestroy();
        //Dispose();
    }
    
	public void Dispose()
	{
	    // very important:
	    Log.w(TAG, "Destroying helper.");
	    if (mHelper != null) 
	    {
	        mHelper.disposeWhenFinished();
	        mHelper = null;
	    }
	}
    // Enables or disables the "please wait" screen.
    void setWaitScreen(boolean set,String msg)
    {
    	if(msgHandle!=null)
    	{
    		UnityPlayer.UnitySendMessage(msgHandle,set?"ShowWaiting":"HideWaiting",msg);		  		
    	}	
    }

    void complain(String message)
    {
        Log.e(TAG, "**** TrivialDrive Error: " + message);
    }
    //通知Unity
    void Send2Unity(String method,Purchase purchase, IabResult result)
    {
    	if(msgHandle==null)return;
        JSONObject o = new JSONObject();
        try
        {
    		o.put("succ", result.isSuccess());
			o.put("itemType", purchase.getItemType());
            o.put("orderId", purchase.getOrderId());
            o.put("productId", purchase.getSku());
            o.put("purchaseState", purchase.getPurchaseState());
            o.put("developerPayload", purchase.getDeveloperPayload());
            o.put("token", purchase.getToken());
            o.put("autoRenewing", purchase.isAutoRenewing());
            o.put("originalJson", purchase.getOriginalJson());
            o.put("signature", purchase.getSignature());
		} 
        catch (JSONException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    
		UnityPlayer.UnitySendMessage(msgHandle,method,o.toString());	
    }
}
