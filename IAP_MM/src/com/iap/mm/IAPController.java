package com.iap.mm;

import mm.purchasesdk.OnPurchaseListener;
import mm.purchasesdk.Purchase;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

public class IAPController
{
	private static final String TAG = "IAP_MM";

	public static Purchase purchase;
	public static Context context;

	private static ProgressDialog mProgressDialog;
	private IAPListener mListener;

	// // 计费信息
	// // 计费信息 (现网环境)
	private static final String APPID = "300009184004"; // 请填写好正确的值
	private static final String APPKEY = "2F3CE83A8367CC82CF5F15D1A9269CEA";// 请填写好正确的值

	// 计费点信息
	private static final String LEASE_PAYCODE = "00000000000000";// 请填写好正确的值。运行时通过菜单可以修改

	private static final int PRODUCT_NUM = 1;

	private String mPaycode;
	private int mProductNum = PRODUCT_NUM;
	private static String ObjectName = "";
	private static String CkFun = "";

	private static final IAPController instance = new IAPController();

	public static IAPController getInstance()
	{
		return instance;
	}

	public static Activity getActivity()
	{
		return UnityPlayer.currentActivity;
	}

	public void init(Context _context)
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
	}

	public static void SetListener(String _objectName, String _ckFun)
	{
		Log.d(TAG, "SetListener  ObjectName:" + _objectName + ";CkFun:"
				+ _ckFun);
		ObjectName = _objectName;
		CkFun = _ckFun;
	}

	public void createOrder(String paycode, int paynum)
	{
		Log.d(TAG, "order. mPaycode:" + paycode);
		Log.d(TAG, "order. mProductNum:" + paynum);
		mPaycode = "30000918400401";// paycode;
		mProductNum = 1;
		order(context, mListener);
	}

	void order(Context context, OnPurchaseListener listener)
	{
		try
		{
			purchase.order(context, mPaycode, mProductNum, listener);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void onBillingFinish(String result)
	{
		Log.d(TAG, "onBillingFinish:" + result);
		UnityPlayer.UnitySendMessage(ObjectName, CkFun, result);
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
