package com.vgame.plugins;

import android.os.Bundle;

import com.iap.cm.IAP_CM;
import com.unity3d.player.UnityPlayerActivity;

public class UnityActivity extends UnityPlayerActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		IAP_CM.onInit(this);
	}
}