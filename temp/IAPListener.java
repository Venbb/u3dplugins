package com.iap;

import java.util.HashMap;

import android.os.Message;
import android.util.Log;

import mm.purchasesdk.OnPurchaseListener;
import mm.purchasesdk.Purchase;
import mm.purchasesdk.PurchaseCode;

public class IAPListener implements OnPurchaseListener {
	
	private final String TAG = "IAPListener";
	private IAPHandler iapHandler;
	public IAPListener(IAPHandler _iapHandler)
	{
		iapHandler=_iapHandler;
	}
	@Override
	public void onAfterApply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAfterDownload() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBeforeApply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBeforeDownload() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBillingFinish(int code, HashMap arg1) 
	{
		Log.d(TAG, "billing finish, status code = " + code);
		String result = "��������������ɹ�";
		Message message = iapHandler.obtainMessage(IAPHandler.BILL_FINISH);
		// �˴ζ�����orderID
		String orderID = null;
		// ��Ʒ��paycode
		String paycode = null;
		// ��Ʒ����Ч��(������������Ʒ��Ч)
		String leftday = null;
		// ��Ʒ�Ľ��� ID���û����Ը����������ID����ѯ��Ʒ�Ƿ��Ѿ�����
		String tradeID = null;
		
		String ordertype = null;
		if (code == PurchaseCode.ORDER_OK || (code == PurchaseCode.AUTH_OK)) {
			/**
			 * ��Ʒ����ɹ������Ѿ����� ��ʱ�᷵����Ʒ��paycode��orderID,�Լ�ʣ��ʱ��(����������Ʒ)
			 */
			if (arg1 != null) {
				leftday = (String) arg1.get(OnPurchaseListener.LEFTDAY);
				if (leftday != null && leftday.trim().length() != 0) {
					result = result + ",ʣ��ʱ�� �� " + leftday;
				}
				orderID = (String) arg1.get(OnPurchaseListener.ORDERID);
				if (orderID != null && orderID.trim().length() != 0) {
					result = result + ",OrderID �� " + orderID;
				}
				paycode = (String) arg1.get(OnPurchaseListener.PAYCODE);
				if (paycode != null && paycode.trim().length() != 0) {
					result = result + ",Paycode:" + paycode;
				}
				tradeID = (String) arg1.get(OnPurchaseListener.TRADEID);
				if (tradeID != null && tradeID.trim().length() != 0) {
					result = result + ",tradeID:" + tradeID;
				}
				ordertype = (String) arg1.get(OnPurchaseListener.ORDERTYPE);
				if (tradeID != null && tradeID.trim().length() != 0) {
					result = result + ",ORDERTYPE:" + ordertype;
				}
			}
		} else {
			result = "���������" + Purchase.getReason(code);
		}
		message.obj = result;
		message.sendToTarget();
		System.out.println(result);
	}

	@Override
	public void onInitFinish(int code)
	{
		Log.d(TAG, "Init finish, status code = " + code);
		Message message = iapHandler.obtainMessage(IAPHandler.INIT_FINISH);
		String result = "��ʼ�������" + Purchase.getReason(code);
		message.obj = result;
		message.sendToTarget();
	}

	@Override
	public void onQueryFinish(int arg0, HashMap arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnsubscribeFinish(int arg0) {
		// TODO Auto-generated method stub
		
	}

}
