package com.iap.cm;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class IAPHandler extends Handler
{
	static final String TAG = "IAPHandler";

	public static final int INIT_FINISH = 10000;
	public static final int BILL_FINISH = 10001;
	public static final int QUERY_FINISH = 10002;
	public static final int UNSUB_FINISH = 10003;

	public IAPHandler()
	{

	}

	@Override
	public void handleMessage(Message msg)
	{
		super.handleMessage(msg);
		int what = msg.what;
		Log.d(TAG, "what:" + what);
		switch (what)
		{
		case INIT_FINISH:
			onInitFinish((String) msg.obj);
			break;
		case BILL_FINISH:
			onBillingFinish((String) msg.obj);
			break;
		case QUERY_FINISH:
			onQueryFinish((String) msg.obj);
			break;
		case UNSUB_FINISH:
			onUnsubscribeFinish((String) msg.obj);
			break;
		default:
			break;
		}
	}

	// 初始化返回结果
	void onInitFinish(String result)
	{
		Log.d(TAG, "onInitFinish:" + result);
		CM_IAPController.isInit = true;
		CM_IAPController.SendMessage(result);
	}

	// 购买返回结果
	void onBillingFinish(String result)
	{
		Log.d(TAG, "onBillingFinish:" + result);
		CM_IAPController.SendMessage(result);
	}

	// 查询返回结果
	void onQueryFinish(String result)
	{
		Log.d(TAG, "onQueryFinish:" + result);
	}

	// 退订返回结果
	void onUnsubscribeFinish(String result)
	{
		Log.d(TAG, "onUnsubscribeFinish:" + result);
	}
}
