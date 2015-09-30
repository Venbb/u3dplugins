package com.unity.plugins;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.text.format.Formatter;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

public class MemEx
{
	private static final String TAG = "MemEx";

	public static void log()
	{
		Log.d(TAG, "getAvailMem = " + getAvailMem());
	}

	// 系统可用内存
	public static long getAvailMem()
	{
		MemoryInfo mi = new MemoryInfo();
		// Debug.MemoryInfo dmi = new Debug.MemoryInfo();
		ActivityManager am = (ActivityManager) UnityPlayer.currentActivity.getSystemService(Context.ACTIVITY_SERVICE);
		am.getMemoryInfo(mi);
		long memSize = mi.availMem;
		Log.d(TAG, "可用内存 = " + formateFileSize(memSize) + ";是否在低内存运行 = " + mi.lowMemory + ";低内存阀值 = " + formateFileSize(mi.threshold));
		// mi.lowMemory=mi.availMem<=mi.threshold?true:false;
		// // 获得系统里正在运行的所有进程
		// List<RunningAppProcessInfo> runningAppProcessesList =
		// am.getRunningAppProcesses();
		//
		// for (RunningAppProcessInfo runningAppProcessInfo :
		// runningAppProcessesList)
		// {
		// // 进程ID号
		// int pid = runningAppProcessInfo.pid;
		// // 用户ID
		// int uid = runningAppProcessInfo.uid;
		// // 进程名
		// String processName = runningAppProcessInfo.processName;
		// // 占用的内存
		// int[] pids = new int[]
		// { pid };
		//
		// Debug.MemoryInfo[] memoryInfo = am.getProcessMemoryInfo(pids);
		// int memorySize = memoryInfo[0].dalvikPrivateDirty;
		//
		// Log.d(TAG, "processName=" + processName + ",pid=" + pid + ",uid=" +
		// uid + ",memorySize=" + memorySize / 1024 + "m");
		// }
		return memSize;
	}

	// // 最大内存
	// public static float getMaxMemory()
	// {
	// float maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024;
	// Log.d(TAG, "getMaxMemory = " + maxMemory);
	// return maxMemory;
	// }
	//
	// // 占用内存
	// public static float getTotalMemory()
	// {
	// float totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;
	// Log.d(TAG, "getTotalMemory = " + totalMemory);
	// return totalMemory;
	// }
	//
	// // 剩余内存
	// public static float getFreeMemory()
	// {
	// float freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024;
	// Log.d(TAG, "getFreeMemory = " + freeMemory);
	// return freeMemory;
	// }

	// 调用系统函数，字符串转换 long -String KB/MB
	private static String formateFileSize(long size)
	{
		return Formatter.formatFileSize(UnityPlayer.currentActivity, size);
	}
}
