package com.crash.lib.uuxia.library;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

/**
 * 2014-08-19
 * 
 * @author Eysin
 * 
 */
public class CrashHandler implements UncaughtExceptionHandler {

	/**
	 * 空格字符?
	 */
	public static final String SPACE = " ";
	/**
	 * 句点.
	 */
	/**
	 * 换行?
	 */
	public static final String LINE_BREAK = "\r\n";
	/**
	 * eg: 2013-05-25 15:35:20.
	 */
	public static final String YYYY_MM_DD_KK_MM_SS = "yyyy-MM-dd_kk_mm_ss";

	/**
	 * 日志的扩展名.
	 */
	public static final String LOG_EXTENSION = ".txt";
	/**
	 * TAG.
	 */
	public static final String TAG = CrashHandler.class.getName();
	/**
	 * 上下文
	 */
	private Context mContext;

	/**
	 * 应用崩溃捕获
	 */
	private static CrashHandler sCrashHandler;

	private Object mEmail = null;

	/**
	 * 系统默认捕获
	 */
	private UncaughtExceptionHandler mDefaultHandler;

	/**
	 * 存储卡的路径.
	 */
	public static final String STORAGE_PATH = Environment
			.getExternalStorageDirectory().getPath();

	public void setEmail(Object obj){
		this.mEmail = obj;
	}

	/**
	 * 私有的构造方
	 */
	private CrashHandler() {

	}

	/**
	 * 初始化方
	 * 
	 * @param context
	 *            上下文信
	 */
	public void init(final Context context) {
		mContext = context;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 获取应用崩溃捕获器实
	 * 
	 * @return 应用崩溃捕获器实
	 */
	public static synchronized CrashHandler getInstance() {
		if (CrashHandler.sCrashHandler == null) {
			CrashHandler.sCrashHandler = new CrashHandler();
		}
		return CrashHandler.sCrashHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang
	 * .Thread, java.lang.Throwable)
	 */
	@Override
	public void uncaughtException(final Thread thread, final Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			// 未处理的异常交给系统处理
			mDefaultHandler.uncaughtException(thread, ex);
		}
	}

	/**
	 * 处理异常.
	 * 
	 * @param ex
	 *            异常信息
	 * @return true表示已处理，false表示未处
	 */
	private boolean handleException(final Throwable ex) {
		if (ex == null) {
			return true;
		}
		StringBuffer sbNativeMsg = new StringBuffer();
		/* 打印异常 */
		if (ex != null) {
			sbNativeMsg.append(LINE_BREAK);
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			ex.printStackTrace(printWriter);
			String error = stringWriter.toString();
			error = error.replaceAll("\n\t", LINE_BREAK + "\t");
			sbNativeMsg.append(error);
		}
		String strNativeMsg = sbNativeMsg.toString();
		writeLog(strNativeMsg);
		return false;
	}

	private void sendEmail(final String error,final String path) {
		if (mEmail == null) {
			System.out.println("如想发送邮件通知，请添加邮件插件...");
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				String title = "【" + getAppName() + "】 手机厂商:"+Build.MANUFACTURER + "-手机型号:" + Build.MODEL 	+ ",错误报告.";
				Class clazz = mEmail.getClass();
				try {
					Method subJect = clazz.getDeclaredMethod(MethondName.setSubject, String.class);
					Method conTent = clazz.getDeclaredMethod(MethondName.setContent, String.class);
					Method attachFile = clazz.getDeclaredMethod(MethondName.setAttachFile, String.class);
					Method sendtextMail = clazz.getDeclaredMethod(MethondName.sendTextMail);
					subJect.invoke(mEmail, title);
					conTent.invoke(mEmail, error);
					attachFile.invoke(mEmail, path);
					sendtextMail.invoke(mEmail);
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private String getAppName() {
		ApplicationInfo android = this.mContext.getApplicationInfo();
		return this.mContext.getString(android.labelRes);
	}

	/**
	 * 写日?
	 * 
	 *            上下文信?
	 *            输出日志的等?
	 *            日志标签
	 *            void 日志内容
	 */
	private void writeLog(final String strMsg) {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			/* 未检测到存储? */
			return;
		}
		/* 写入日志的消? */
		StringBuffer sbMsg = new StringBuffer();
		sbMsg.append(SPACE);
		sbMsg.append(getCurUtcDateTimeString());
		sbMsg.append(SPACE);
		sbMsg.append(strMsg);
		sbMsg.append(LINE_BREAK);

		/* 日志的文件名 */
		StringBuffer sbFileName = new StringBuffer();
		sbFileName.append(getCurUtcDateTimeString());
		sbFileName.append(LOG_EXTENSION);

		if (mContext != null) {
			String strLogDirName = null;
			strLogDirName = STORAGE_PATH + File.separator
					+ mContext.getPackageName() + File.separator; // 公共日志目录
			createDir(strLogDirName);
			File file = new File(strLogDirName, sbFileName.toString());
			if (!file.exists()) {
				String strSysInfo = genInfo(); // 在日志文件的?写入系统信息
				strSysInfo += sbMsg.toString();
				saveFile(strLogDirName, sbFileName.toString(),
						strSysInfo.getBytes(Charset.defaultCharset()), false);
			} else {
				saveFile(strLogDirName, sbFileName.toString(), sbMsg.toString().getBytes(Charset.defaultCharset()), false);
			}
		}
	}

	public static boolean isNull(final String strSource) {
		return strSource == null || "".equals(strSource.trim());
	}

	/**
	 * "yyyy-MM-dd kk_mm_ss"
	 * 
	 * @return
	 */
	public static String getCurUtcDateTimeString() {
		return getCurUtcString(YYYY_MM_DD_KK_MM_SS);
	}

	/**
	 * 获取UTC当前时间的描述.
	 * 
	 * @param strInFmt
	 *            时间格式
	 * @return UTC当前时间的描述
	 */
	public static String getCurUtcString(final String strInFmt) {
		if (isNull(strInFmt)) {
			throw new NullPointerException("参数strInFmt不能为空");
		}
		return format(getCurUtcMillis(), strInFmt);
	}

	/**
	 * 格式化时间.
	 * 
	 * @param lMillis
	 *            时间参数
	 * @param strInFmt
	 *            时间格式
	 * @return 对应的时间字符串
	 */
	public static String format(final long lMillis, final String strInFmt) {
		if (isNull(strInFmt)) {
			throw new NullPointerException("参数strInFmt不能为空");
		}
		return (String) DateFormat.format(strInFmt, lMillis);
	}

	/**
	 * 获取UTC当前时间距离1970-01-01的毫秒数，UTC当前时间忽略毫秒值.
	 * 
	 * @return UTC当前时间距离1970-01-01的毫秒数.
	 */
	public static long getCurUtcMillis() {
		Time time = new Time();
		time.setToNow();
		long lCurMillis = time.toMillis(false);
		long lOffset = time.gmtoff * DateUtils.SECOND_IN_MILLIS;
		long lUtcMillis = lCurMillis - lOffset;
		return lUtcMillis;
	}

	/**
	 * 创建目录，若目录已存则不做操作.
	 * 
	 * @param strDir
	 *            目录的路径
	 */
	public static void createDir(final String strDir) {
		if (strDir == null) {
			Log.e(TAG, "参数strDir不能为空");
			return;
		}
		File file = new File(strDir);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	/**
	 * 保存文件.
	 * 
	 * @param strFileDir
	 *            文件目录，若不存在则创建
	 * @param strFileName
	 *            文件名
	 * @param btContents
	 *            准备写入文件的字节数组
	 * @param bOverride
	 *            写入模式，true表示覆盖，false表示追加
	 */
	public void saveFile(final String strFileDir,
			final String strFileName, final byte[] btContents,
			final boolean bOverride) {
		if (isNull(strFileDir)) {
			Log.e(TAG, "参数strFileDir不能为空");
			return;
		} else if (isNull(strFileName)) {
			Log.e(TAG, "参数strFileName不能为空");
			return;
		} else if (btContents == null) {
			Log.e(TAG, "参数btContents不能为空");
			return;
		}
		/* 拼接文件的路径 */
		StringBuffer sbFilePath = new StringBuffer();
		sbFilePath.append(strFileDir);
		sbFilePath.append(File.separator);
		sbFilePath.append(strFileName);
		FileOutputStream fos = null;
		try {
			createDir(strFileDir);
			File file = new File(sbFilePath.toString());
			Boolean bIsExist = file.exists();
			if (!bIsExist || bOverride) {
				/* 文件不存在或覆盖模式 */
				fos = new FileOutputStream(file);
			} else if (!bOverride && bIsExist) {
				/* 文件存在且追加模式 */
				fos = new FileOutputStream(file, true);
			}
			fos.write(btContents);
		} catch (IOException e) {
			/* 无法使用文件的I/O流 */
			Log.e(TAG, "无法使用文件的I/O流", e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					/* 无法关闭文件的I/O流 */
					Log.e(TAG, "无法关闭文件的I/O流", e);
				}
			}
		}

		try {
			sendEmail(new String(btContents, "ISO-8859-1"),sbFilePath.toString());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 生成系统相关的信息.
	 * 
	 *            上下文信息
	 * @return 系统相关的信息
	 */
	public String genInfo() {
		StringBuffer sbInfo = new StringBuffer();
		sbInfo.append("【品牌信息】：" + getBrandName());
		sbInfo.append(LINE_BREAK);
		sbInfo.append("【产品信息】：" + getProductName());
		sbInfo.append(LINE_BREAK);
		sbInfo.append("【设备信息】：" + getModelName());
		sbInfo.append(LINE_BREAK);
		sbInfo.append("【制造商信息】：" + getManufacturerName());
		sbInfo.append(LINE_BREAK);
		sbInfo.append("【操作系统版本号】：" + getOSVersionCode());
		sbInfo.append(LINE_BREAK);
		sbInfo.append("【操作系统版本名】：" + getOSVersionName());
		sbInfo.append(LINE_BREAK);
		sbInfo.append("【操作系统版本显示名】：" + getOSVersionDisplayName());
		sbInfo.append(LINE_BREAK);
		sbInfo.append("【App版本号】：" + getAppVersionCode());
		sbInfo.append(LINE_BREAK);
		sbInfo.append("【App版本名】：" + getAppVersionName());
		sbInfo.append(LINE_BREAK);
		sbInfo.append("【主机地址】：" + getHost());
		sbInfo.append(LINE_BREAK);
		sbInfo.append(LINE_BREAK);
		return sbInfo.toString();
	}

	/**
	 * 获取App版本号.
	 * 
	 *            上下文信息
	 * @return App版本号
	 */
	public int getAppVersionCode() {
		int iAppVersionCode = 0;
		try {
			PackageManager manager = mContext.getPackageManager();
			PackageInfo info = manager.getPackageInfo(
					mContext.getPackageName(), 0);
			iAppVersionCode = info.versionCode;
		} catch (NameNotFoundException e) {
			System.err.println("未找到包名为\"" + mContext.getPackageName()
					+ "\"对应的包信息");
		}
		return iAppVersionCode;
	}

	/**
	 * 获取App版本名.
	 * 
	 *            上下文信息
	 * @return App版本名
	 */
	public String getAppVersionName() {
		String strAppVersionName = "未知的版本名";
		try {
			PackageManager manager = mContext.getPackageManager();
			PackageInfo info = manager.getPackageInfo(
					mContext.getPackageName(), 0);
			strAppVersionName = info.versionName;
		} catch (NameNotFoundException e) {
			System.err.println("未找到包名为\"" + mContext.getPackageName()
					+ "\"对应的包信息");
		}
		return strAppVersionName;
	}

	/**
	 * 获取设备制造商名称.
	 * 
	 * @return 设备制造商名称
	 */
	public static String getManufacturerName() {
		return Build.MANUFACTURER;
	}

	/**
	 * 获取设备名称.
	 * 
	 * @return 设备名称
	 */
	public static String getModelName() {
		return Build.MODEL;
	}

	/**
	 * 获取产品名称.
	 * 
	 * @return 产品名称
	 */
	public static String getProductName() {
		return Build.PRODUCT;
	}

	/**
	 * 获取品牌名称.
	 * 
	 * @return 品牌名称
	 */
	public static String getBrandName() {
		return Build.BRAND;
	}

	/**
	 * 获取操作系统版本号.
	 * 
	 * @return 操作系统版本号
	 */
	public static int getOSVersionCode() {
		return Build.VERSION.SDK_INT;
	}

	/**
	 * 获取操作系统版本名.
	 * 
	 * @return 操作系统版本名
	 */
	public static String getOSVersionName() {
		return Build.VERSION.RELEASE;
	}

	/**
	 * 获取操作系统版本显示名.
	 * 
	 * @return 操作系统版本显示名
	 */
	public static String getOSVersionDisplayName() {
		return Build.DISPLAY;
	}

	/**
	 * 获取主机地址.
	 * 
	 * @return 主机地址
	 */
	public static String getHost() {
		return Build.HOST;
	}

	static class MethondName{
		public static String setSubject = "setSubject";
		public static String setContent = "setContent";
		public static String setAttachFile = "setAttachFile";
		public static String sendTextMail = "sendTextMail";
	}
}
