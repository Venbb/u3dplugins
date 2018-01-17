package com.webservice.soap;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParserException;

import com.unity3d.player.UnityPlayer;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

/**
 * ���� WebService �Ĺ����� http://blog.csdn.net/qq_24867873/article/details/77282260
 * http://blog.csdn.net/da_caoyuan/article/details/52931007
 */
public class WebServiceUtil {
	// �����ռ�
	private static final String NAMESPACE = "http://123game.vn/";
	// WebService ��������ַ
	private static final String ENDPOINT = "http://mapipml.123game.vn/service.asmx?WSDL";

	// һ���Լ���˾����������Ҫ�����֤��
	// �����֤������
	private static final String ID_HEADERNAME = "WebSoapHeader";
	// �����֤ key
	private static final String ID_NAME_PARAM = "UserName";
	// �����֤ value
	private static final String ID_NAME_VALUE = "TId9gIXEJORXkjZvgCqxBjCUUn2uxhLq";
	// �����֤ key
	private static final String ID_PASSWORD_PARAM = "Password";
	// �����֤ value
	private static final String ID_PASSWORD_VALUE = "d9Z8lhTUB8OqeTnFdPN6XTKBmBCPmZCG";

	// ���ʵķ������Ƿ��� dotNet ����
	public static boolean isDotNet = true;

	// �̳߳صĴ�С
	private static int threadSize = 5;
	// ����һ�������ù̶��߳������̳߳أ��Թ�����޽���з�ʽ��������Щ�߳�
	private static ExecutorService threadPool = Executors
			.newFixedThreadPool(threadSize);

	// ������Ӧ��ʾ
	public static final int SUCCESS_FLAG = 0;
	public static final int ERROR_FLAG = 1;

	public static void callMethod(final String msgHandle, final String method,
			String jsonData) {
		// Log.e("Soap", jsonData);
		final SimpleArrayMap<String, Object> map = new SimpleArrayMap<String, Object>();
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jsonData);
			Iterator<String> readers = jsonObject.keys();
			String key = null;
			while (readers.hasNext()) {
				key = readers.next();
				map.put(key, jsonObject.get(key));
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Log.e("Soap", map.toString());
		Handler mainHandler = new Handler(Looper.getMainLooper());
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				// �������߳��У����Ը���UI
				WebServiceUtil.call(method, map,
						new WebServiceUtil.ResponseCallBack() {
							@Override
							public void onSuccess(String result) {
								// Log.e("Soap", "hhhhhhhhhhhh:" + result);
								UnityPlayer.UnitySendMessage(msgHandle,
										"onSuccess", result);
							}

							@Override
							public void onError(Exception e) {
								// Log.e("Soap", e.getMessage());
								UnityPlayer.UnitySendMessage(msgHandle,
										"onError", e.getMessage());
							}
						});
			}
		});
	}

	/**
	 * ���� WebService �ӿ�
	 * 
	 * @param methodName
	 *            WebService �ĵ��÷�����
	 * @param mapParams
	 *            WebService �Ĳ������ϣ�����Ϊ null
	 * @param reponseCallBack
	 *            ��������Ӧ�ӿ�
	 */
	public static void call(final String methodName,
			SimpleArrayMap<String, Object> mapParams,
			final ResponseCallBack reponseCallBack) {
		// Log.e("Soap", mapParams.toString());
		// ���� HttpTransportSE ���󣬴��� WebService ��������ַ
		final HttpTransportSE transport = new HttpTransportSE(ENDPOINT);
		transport.debug = true;

		// �����֤�������Ҫ�Ļ���
		Element[] header = new Element[1];
		// ���������ռ�����֤�ķ�����
		header[0] = new Element().createElement(NAMESPACE, ID_HEADERNAME);
		// �������� 1
		Element userName = new Element()
				.createElement(NAMESPACE, ID_NAME_PARAM);
		userName.addChild(Node.TEXT, ID_NAME_VALUE);
		header[0].addChild(Node.ELEMENT, userName);
		// �������� 2
		Element password = new Element().createElement(NAMESPACE,
				ID_PASSWORD_PARAM);
		password.addChild(Node.TEXT, ID_PASSWORD_VALUE);
		header[0].addChild(Node.ELEMENT, password);

		// ���� SoapObject �������ڴ����������
		final SoapObject soapObject = new SoapObject(NAMESPACE, methodName);
		// ��Ӳ���
		if (mapParams != null) {
			for (int index = 0; index < mapParams.size(); index++) {
				String key = mapParams.keyAt(index);
				// ��������£����ݵĲ�����Ϊ String ���ͣ�������������»��� boolean ���ͣ������� Object ����
				Object value = mapParams.get(key);
				soapObject.addProperty(key, value);
			}
		}

		// ʵ���� SoapSerializationEnvelope������ WebService �� SOAP Э��İ汾��
		// ������ VER10 VER11 VER12 ���ְ汾�������Լ���Ҫ��д
		final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.headerOut = header; // �����֤�������Ҫ�Ļ���
		envelope.dotNet = isDotNet; // �����Ƿ���õ��� .Net ������ WebService
		envelope.bodyOut = soapObject; // ���ݲ���
		// envelope.setOutputSoapObject(soapObject);// ����һ��ȼ�

		// ���������߳�ͨ�ŵ� Handler
		final Handler responseHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				// ������Ϣ�� arg1 ֵ�жϵ����ĸ��ӿ�
				if (msg.arg1 == SUCCESS_FLAG) {
					Log.e("Soap", "onSuccess:" + (String) msg.obj);
					reponseCallBack.onSuccess((String) msg.obj);
				} else {
					Log.e("Soap",
							"onError:" + ((Exception) msg.obj).getMessage());
					reponseCallBack.onError((Exception) msg.obj);
				}
			}

		};

		// �ύһ�����̵߳��̳߳ز��ڴ������ڵ��� WebService
		if (threadPool == null || threadPool.isShutdown()) {
			threadPool = Executors.newFixedThreadPool(threadSize);
		}
		threadPool.submit(new Runnable() {

			@Override
			public void run() {
				String result = null;
				try {
					// ��� EOFException
					System.setProperty("http.keepAlive", "false");
					// ���ӷ��������еķ�����ܲ���Ҫ���� NAMESPACE + methodName����һ���������� null
					transport.call(null, envelope);
					if (envelope.getResponse() != null) {
						// ��ȡ��������Ӧ���ص� SoapObject
						SoapObject object = (SoapObject) envelope.bodyIn;
						result = object.getProperty(0).toString();
					}
				} catch (IOException e) {
					// �� call �����ĵ�һ������Ϊ null ʱ����һ���ĸ����� IO �쳣
					// �����Ҫ��Ҫ��׽���쳣���������ռ�ӷ�������Ϊ������������
					// e.printStackTrace();
					try {
						transport.call(NAMESPACE + methodName, envelope);
						if (envelope.getResponse() != null) {
							// ��ȡ��������Ӧ���ص� SoapObject
							SoapObject object = (SoapObject) envelope.bodyIn;
							result = object.getProperty(0).toString();
						}
					} catch (Exception e1) {
						// e1.printStackTrace();
						responseHandler.sendMessage(responseHandler
								.obtainMessage(0, ERROR_FLAG, 0, e1));
					}
				} catch (XmlPullParserException e) {
					// e.printStackTrace();
					responseHandler.sendMessage(responseHandler.obtainMessage(
							0, ERROR_FLAG, 0, e));
				} finally {
					// ����ȡ����Ϣ���� Handler ���͵����߳�
					responseHandler.sendMessage(responseHandler.obtainMessage(
							0, SUCCESS_FLAG, 0, result));
				}
			}
		});
	}

	/**
	 * �����̳߳صĴ�С
	 * 
	 * @param threadSize
	 */
	public static void setThreadSize(int threadSize) {
		WebServiceUtil.threadSize = threadSize;
		threadPool.shutdownNow();
		threadPool = Executors.newFixedThreadPool(WebServiceUtil.threadSize);
	}

	/**
	 * ��������Ӧ�ӿڣ�����Ӧ����Ҫ�ص��˽ӿ�
	 */
	public interface ResponseCallBack {

		void onSuccess(String result);

		void onError(Exception e);
	}

	public static String getIP() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& (inetAddress instanceof Inet4Address)) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}