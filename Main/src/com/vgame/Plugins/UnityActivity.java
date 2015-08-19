package com.vgame.Plugins;

import android.os.Bundle;

import com.iap.mm.IAPController;
import com.unity3d.player.UnityPlayerActivity;

public class UnityActivity extends UnityPlayerActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		IAPController.getInstance().init(this);
	}

	public void SetListener(String _objectName, String _ckFun)
	{
		IAPController.SetListener(_objectName, _ckFun);
	}

	public void Order(String paycode, int paynum)
	{
		IAPController.getInstance().createOrder(paycode, paynum);
	}
}