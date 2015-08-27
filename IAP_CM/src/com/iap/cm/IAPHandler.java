package com.iap.cm;

import android.os.Handler;
import android.os.Message;

public class IAPHandler extends Handler
{

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
		switch (what)
		{
		case INIT_FINISH:
			// initShow((String) msg.obj);
			onInitFinish((String) msg.obj);
			break;
		case BILL_FINISH:
			// initShow((String) msg.obj);
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
		IAP_CM.isInit = true;
		IAP_CM.dismissProgressDialog();
	}

	// 购买返回结果
	void onBillingFinish(String result)
	{
		IAP_CM.dismissProgressDialog();
		IAP_CM.SendMessageToU3D(result);
	}

	// 查询返回结果
	void onQueryFinish(String result)
	{
		IAP_CM.dismissProgressDialog();
	}

	// 退订返回结果
	void onUnsubscribeFinish(String result)
	{
		IAP_CM.dismissProgressDialog();
	}
	// private void initShow(String msg)
	// {
	// Toast.makeText(IAP_CM.context, "初始化：" + msg, Toast.LENGTH_LONG)
	// .show();
	// }
}
