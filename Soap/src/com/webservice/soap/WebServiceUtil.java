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
 * 访问 WebService 的工具类 http://blog.csdn.net/qq_24867873/article/details/77282260
 * http://blog.csdn.net/da_caoyuan/article/details/52931007
 */
public class WebServiceUtil {
	// 命名空间
	private static final String NAMESPACE = "http://123game.vn/";
	// WebService 服务器地址
	private static final String ENDPOINT = "http://mapipml.123game.vn/service.asmx?WSDL";

	// 一般自己公司开发都是需要身份验证的
	// 身份验证方法名
	private static final String ID_HEADERNAME = "WebSoapHeader";
	// 身份验证 key
	private static final String ID_NAME_PARAM = "UserName";
	// 身份验证 value
	private static final String ID_NAME_VALUE = "TId9gIXEJORXkjZvgCqxBjCUUn2uxhLq";
	// 身份验证 key
	private static final String ID_PASSWORD_PARAM = "Password";
	// 身份验证 value
	private static final String ID_PASSWORD_VALUE = "d9Z8lhTUB8OqeTnFdPN6XTKBmBCPmZCG";

	// 访问的服务器是否由 dotNet 开发
	public static boolean isDotNet = true;

	// 线程池的大小
	private static int threadSize = 5;
	// 创建一个可重用固定线程数的线程池，以共享的无界队列方式来运行这些线程
	private static ExecutorService threadPool = Executors
			.newFixedThreadPool(threadSize);

	// 连接响应标示
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
				// 已在主线程中，可以更新UI
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
	 * 调用 WebService 接口
	 * 
	 * @param methodName
	 *            WebService 的调用方法名
	 * @param mapParams
	 *            WebService 的参数集合，可以为 null
	 * @param reponseCallBack
	 *            服务器响应接口
	 */
	public static void call(final String methodName,
			SimpleArrayMap<String, Object> mapParams,
			final ResponseCallBack reponseCallBack) {
		// Log.e("Soap", mapParams.toString());
		// 创建 HttpTransportSE 对象，传递 WebService 服务器地址
		final HttpTransportSE transport = new HttpTransportSE(ENDPOINT);
		transport.debug = true;

		// 身份验证（如果需要的话）
		Element[] header = new Element[1];
		// 传入命名空间与验证的方法名
		header[0] = new Element().createElement(NAMESPACE, ID_HEADERNAME);
		// 创建参数 1
		Element userName = new Element()
				.createElement(NAMESPACE, ID_NAME_PARAM);
		userName.addChild(Node.TEXT, ID_NAME_VALUE);
		header[0].addChild(Node.ELEMENT, userName);
		// 创建参数 2
		Element password = new Element().createElement(NAMESPACE,
				ID_PASSWORD_PARAM);
		password.addChild(Node.TEXT, ID_PASSWORD_VALUE);
		header[0].addChild(Node.ELEMENT, password);

		// 创建 SoapObject 对象用于传递请求参数
		final SoapObject soapObject = new SoapObject(NAMESPACE, methodName);
		// 添加参数
		if (mapParams != null) {
			for (int index = 0; index < mapParams.size(); index++) {
				String key = mapParams.keyAt(index);
				// 多数情况下，传递的参数都为 String 类型，不过少数情况下会有 boolean 类型，所以用 Object 代替
				Object value = mapParams.get(key);
				soapObject.addProperty(key, value);
			}
		}

		// 实例化 SoapSerializationEnvelope，传入 WebService 的 SOAP 协议的版本号
		// 这里有 VER10 VER11 VER12 三种版本，根据自己需要填写
		final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.headerOut = header; // 身份验证（如果需要的话）
		envelope.dotNet = isDotNet; // 设置是否调用的是 .Net 开发的 WebService
		envelope.bodyOut = soapObject; // 传递参数
		// envelope.setOutputSoapObject(soapObject);// 与上一句等价

		// 用于与主线程通信的 Handler
		final Handler responseHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				// 根据消息的 arg1 值判断调用哪个接口
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

		// 提交一个子线程到线程池并在此线种内调用 WebService
		if (threadPool == null || threadPool.isShutdown()) {
			threadPool = Executors.newFixedThreadPool(threadSize);
		}
		threadPool.submit(new Runnable() {

			@Override
			public void run() {
				String result = null;
				try {
					// 解决 EOFException
					System.setProperty("http.keepAlive", "false");
					// 连接服务器，有的服务可能不需要传递 NAMESPACE + methodName，第一个参数传递 null
					transport.call(null, envelope);
					if (envelope.getResponse() != null) {
						// 获取服务器响应返回的 SoapObject
						SoapObject object = (SoapObject) envelope.bodyIn;
						result = object.getProperty(0).toString();
					}
				} catch (IOException e) {
					// 当 call 方法的第一个参数为 null 时会有一定的概念抛 IO 异常
					// 因此需要需要捕捉此异常后用命名空间加方法名作为参数重新连接
					// e.printStackTrace();
					try {
						transport.call(NAMESPACE + methodName, envelope);
						if (envelope.getResponse() != null) {
							// 获取服务器响应返回的 SoapObject
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
					// 将获取的消息利用 Handler 发送到主线程
					responseHandler.sendMessage(responseHandler.obtainMessage(
							0, SUCCESS_FLAG, 0, result));
				}
			}
		});
	}

	/**
	 * 设置线程池的大小
	 * 
	 * @param threadSize
	 */
	public static void setThreadSize(int threadSize) {
		WebServiceUtil.threadSize = threadSize;
		threadPool.shutdownNow();
		threadPool = Executors.newFixedThreadPool(WebServiceUtil.threadSize);
	}

	/**
	 * 服务器响应接口，在响应后需要回调此接口
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