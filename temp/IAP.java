package com.iap;

import mm.purchasesdk.Purchase;
import android.app.ProgressDialog;
import android.content.Context;

public class IAP {
	private static final String APPID = "000000000";
	private static final String APPKEY = "000000000";

	private static final IAP instance = new IAP();

	private IAP() {
		System.out.println("init");
	}

	public static IAP getInstance() {
		return instance;
	}

	private Context context;

	public Context getContext() {
		return context;
	}

	private ProgressDialog progressDialog;
	private Purchase purchase;
	private IAPListener iapListener;
	private Boolean initFinish = false;

	public Boolean getInitFinish() {
		return initFinish;
	}

	private String curPayCode;
	private int curPayNum;

	public void setOrder(String paycode, int paynum) {
		curPayCode = paycode;
		if (paynum > 0) {
			curPayNum = paynum;
		} else {
			curPayNum = 1;
		}
	}

	public static void order(Context _context, String paycode, int paynum) {
		IAP iap = IAP.getInstance();
		iap.setOrder(null, 1);

		if (!iap.initFinish) {
			iap.setOrder(paycode, paynum);
			iap.init(_context);
			return;
		}
		iap.order();
	}

	public void init(Context _context) {
		context = _context;
		showProgressDialog();

		IAPHandler iapHandler = new IAPHandler();
		iapListener = new IAPListener(iapHandler);
		purchase = Purchase.getInstance();
		try {
			purchase.setAppInfo(APPID, APPKEY);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			purchase.init(_context, iapListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String order() {
		String pcode = purchase.order(context, curPayCode, curPayNum,
				iapListener);
		setOrder(null, 1);
		return pcode;
	}

	public void onInitFinish() {
		dismissProgressDialog();
		if (curPayCode != null)
			order();
	}

	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(context);
			progressDialog.setIndeterminate(true);
			progressDialog.setMessage("«Î…‘∫Ú.....");
		}
		if (!progressDialog.isShowing()) {
			progressDialog.show();
		}
	}

	public void dismissProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
	}
}
