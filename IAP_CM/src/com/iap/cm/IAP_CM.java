package com.iap.cm;

import mm.purchasesdk.Purchase;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

public class IAP_CM
{
	private static final String TAG = "IAP_CM";

	static Purchase purchase;
	static Context context;

	private static ProgressDialog mProgressDialog;
	private static IAPListener mListener;

	// // 计费信息
	// // 计费信息 (现网环境)
	private static String APPID = "300009184004"; // 请填写好正确的值
	private static String APPKEY = "2F3CE83A8367CC82CF5F15D1A9269CEA";// 请填写好正确的值

	private static final int PRODUCT_NUM = 1;

	private static String mPaycode;
	private static int mProductNum = PRODUCT_NUM;
	private static String ObjectName = "";
	private static String CkFun = "";
	public static Boolean isInit = false;

	public static Activity getActivity()
	{
		return UnityPlayer.currentActivity;
	}

	public static void init(String appid, String appkey, String _objectName, String _ckFun)
	{
		APPID = appid;
		APPKEY = appkey;
		ObjectName = _objectName;
		CkFun = _ckFun;
		Log.d(TAG, "init  APPID:" + APPID + ";APPKEY:" + APPKEY + ";ObjectName:" + ObjectName + ";CkFun:" + CkFun);
		if (isInit) return;
		new Handler(Looper.getMainLooper()).post(new Runnable()
		{
			@Override
			public void run()
			{
				onInit(getActivity());
			}
		});
	}

	public static void onInit(Context _context)
	{
		context = _context;

		IAPHandler iapHandler = new IAPHandler();
		Log.d(TAG, "step1.实例化PurchaseListener。");
		/**
		 * IAP组件初始化.包括下面3步。
		 */
		/**
		 * step1.实例化PurchaseListener。实例化传入的参数与您实现PurchaseListener接口的对象有关。
		 * 例如，此Demo代码中使用IAPListener继承PurchaseListener，其构造函数需要Context实例。
		 */
		mListener = new IAPListener(context, iapHandler);
		Log.d(TAG, "step2.获取Purchase实例。");
		/**
		 * step2.获取Purchase实例。
		 */
		purchase = Purchase.getInstance();
		Log.d(TAG, "step3.向Purhase传入应用信息。APPID:" + APPID + ";APPKEY:" + APPKEY);
		/**
		 * step3.向Purhase传入应用信息。APPID，APPKEY。 需要传入参数APPID，APPKEY。 APPID，见开发者文档
		 * APPKEY，见开发者文档
		 */
		try
		{
			purchase.setAppInfo(APPID, APPKEY);
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		Log.d(TAG, "step4. IAP组件初始化开始");
		/**
		 * step4. IAP组件初始化开始， 参数PurchaseListener，初始化函数需传入step1时实例化的
		 * PurchaseListener。
		 */
		try
		{
			purchase.init(context, mListener);

		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		showProgressDialog();
	}

	// 购买
	public static void order(String paycode, int paynum)
	{
		Log.d(TAG, "createOrder. mPaycode:" + paycode);
		Log.d(TAG, "createOrder. mProductNum:" + paynum);
		mPaycode = paycode;
		mProductNum = paynum;

		new Handler(Looper.getMainLooper()).post(new Runnable()
		{
			@Override
			public void run()
			{
				onOrder();
			}
		});
	}

	static void onOrder()
	{
		try
		{
			Log.d(TAG, "mPaycode:" + mPaycode + ";mProductNum:" + mProductNum);
			showProgressDialog();
			purchase.order(context, mPaycode, mProductNum, mListener);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// 退订
	public static void unsubscribe(String paycode)
	{
		try
		{
			purchase.unsubscribe(context, paycode, mListener);

		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		showProgressDialog();
	}

	// 查询
	public static void query(String paycode)
	{
		try
		{
			purchase.query(context, paycode, null, mListener);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		showProgressDialog();
	}

	// 清除缓存
	public static void clearCache()
	{
		try
		{
			purchase.clearCache(context);
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
			return;
		}
	}

	// 发送消息给Unity
	public static void SendMessageToU3D(String dataStr)
	{
		UnityPlayer.UnitySendMessage(ObjectName, CkFun, dataStr);
	}

	private static void showProgressDialog()
	{
		if (mProgressDialog == null)
		{
			mProgressDialog = new ProgressDialog(context);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setMessage("请稍候.....");
		}
		if (!mProgressDialog.isShowing())
		{
			mProgressDialog.show();
		}
	}

	public static void dismissProgressDialog()
	{
		if (mProgressDialog != null && mProgressDialog.isShowing())
		{
			mProgressDialog.dismiss();
		}
	}
}
