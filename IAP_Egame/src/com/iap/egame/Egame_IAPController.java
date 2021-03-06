package com.iap.egame;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import cn.egame.terminal.paysdk.EgamePay;
import cn.egame.terminal.paysdk.EgamePayListener;
import cn.egame.terminal.sdk.log.EgameAgent;

import com.unity3d.player.UnityPlayer;

public class Egame_IAPController
{
	static final String TAG = "Egame_IAPController";

	static String ObjectName = "";
	static String CkFun = "";

	static Activity getActivity()
	{
		return UnityPlayer.currentActivity;
	}

	static boolean isInit = false;

	public static boolean IsInit()
	{
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable()
		{
			@Override
			public void run()
			{
				isInit = EgamePay.sInitStatus < 0 ? false : true;
			}
		});
		return isInit;
	}

	// 初始化
	public static void Init(String _objectName, String _ckFun)
	{
		SetListener(_objectName, _ckFun);
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable()
		{
			@Override
			public void run()
			{
				Log.d(TAG, "EgamePay.sInitStatus0:" + EgamePay.sInitStatus);
				EgamePay.init(getActivity());
				Log.d(TAG, "EgamePay.sInitStatus1:" + EgamePay.sInitStatus);
			}
		});
	}

	// 支付
	public static void Order(String altas, String name)
	{
		Log.d(TAG, "EgamePay.sInitStatus2:" + EgamePay.sInitStatus);
		final HashMap<String, String> payParams = new HashMap<String, String>();
		payParams.put(EgamePay.PAY_PARAMS_KEY_TOOLS_ALIAS, altas);
		payParams.put(EgamePay.PAY_PARAMS_KEY_TOOLS_NAME, name);

		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable()
		{
			@Override
			public void run()
			{
				Log.d(TAG, "Order:" + payParams);
				Pay(payParams);
			}
		});
	}

	// 支付
	private static void Pay(HashMap<String, String> payParams)
	{
		EgamePay.pay(getActivity(), payParams, new EgamePayListener()
		{

			@Override
			public void paySuccess(Map<String, String> params)
			{
				String result = "{\"result\":0,\"altas\":\"" + params.get(EgamePay.PAY_PARAMS_KEY_TOOLS_ALIAS) + "\",\"name\":\"" + params.get(EgamePay.PAY_PARAMS_KEY_TOOLS_NAME) + "\",\"cp_order_id\":\"" + params.get(EgamePay.PAY_PARAMS_KEY_CP_PARAMS) + "\"}";
				SendMessage(result);
			}

			@Override
			public void payFailed(Map<String, String> params, int erroInt)
			{
				String result = "{\"result\":1,\"altas\":\"" + params.get(EgamePay.PAY_PARAMS_KEY_TOOLS_ALIAS) + "\",\"name\":\"" + params.get(EgamePay.PAY_PARAMS_KEY_TOOLS_NAME) + "\",\"erroInt\":" + erroInt + "}";
				SendMessage(result);
			}

			@Override
			public void payCancel(Map<String, String> params)
			{
				String result = "{\"result\":2,\"altas\":\"" + params.get(EgamePay.PAY_PARAMS_KEY_TOOLS_ALIAS) + "\",\"name\":\"" + params.get(EgamePay.PAY_PARAMS_KEY_TOOLS_NAME) + "\"}";
				SendMessage(result);
			}
		});
	}

	// 暂停
	public static void OnPause()
	{
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable()
		{
			@Override
			public void run()
			{
				Log.d(TAG, "OnPause:" + getActivity());
				EgameAgent.onPause(getActivity());
			}
		});
	}

	// 恢复
	public static void OnResume()
	{
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable()
		{
			@Override
			public void run()
			{
				Log.d(TAG, "OnResume:" + getActivity());
				EgameAgent.onResume(getActivity());
			}
		});
	}

	public static void SetListener(String _objectName, String _ckFun)
	{
		ObjectName = _objectName;
		CkFun = _ckFun;
		Log.d(TAG, "SetListener ObjectName:" + ObjectName + ";CkFun:" + CkFun);
	}

	// 发送消息给Unity
	public static void SendMessage(String dataStr)
	{
		Log.d(TAG, "支付结果:" + dataStr);
		if (!ObjectName.isEmpty() && !CkFun.isEmpty())
		{
			UnityPlayer.UnitySendMessage(ObjectName, CkFun, dataStr);
		}
	}
}
