package com.legenty.utils.utils;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.legenty.utils.R;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class VersionUtils {
	
	private final String appVersion; 
	private final String appName;
	private final static String FREE_VERSION_NAME = "com.fikraplus.terredct";
	private final static String FREE_DEV_VERSION_NAME = "com.fikraplus.terredct.dev";
	private final static String FREE_CUSTOM_VERSION_NAME = "com.fikraplus.terredct.freecustom";
	


	
	
	private VersionUtils(Context ctx) {
		String appVersion = "";
		int versionCode = -1;
		try {
			PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
			appVersion = packageInfo.versionName;  //Version suffix  ctx.getString(R.string.app_version_suffix)  already appended in build.gradle
			versionCode = packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		this.appVersion = appVersion;
		appName = ctx.getString(R.string.app_name);
	}

	private static VersionUtils ver = null;
	private static VersionUtils getVersion(Context ctx){
		if (ver == null) {
			ver = new VersionUtils(ctx);
		}
		return ver;
	}
	
	public static String getFullVersion(Context ctx){
		VersionUtils v = getVersion(ctx);
		return v.appName + " " + v.appVersion;
	}
	
	public static String getAppVersion(Context ctx){
		VersionUtils v = getVersion(ctx);
		return v.appVersion;
	}

	public static String getAppName(Context ctx){
		VersionUtils v = getVersion(ctx);
		return v.appName;
	}
	
	public static boolean isProductionVersion(Context ctx){
		VersionUtils v = getVersion(ctx);
		return !v.appVersion.contains("#");
	}

	public static String getVersionAsURLParam(Context ctx) {
		try {
			return "osmandver=" + URLEncoder.encode(getVersionForTracker(ctx), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public static boolean isFreeVersion(Context ctx){
		return ctx.getPackageName().equals(FREE_VERSION_NAME) || 
				ctx.getPackageName().equals(FREE_DEV_VERSION_NAME) ||
				ctx.getPackageName().equals(FREE_CUSTOM_VERSION_NAME)
				;
	}

	public static boolean isDeveloperVersion(Context ctx){
		return getAppName(ctx).contains("~") || ctx.getPackageName().equals(FREE_DEV_VERSION_NAME);
	}

	public static boolean isDeveloperBuild(Context ctx){
		return getAppName(ctx).contains("~");
	}

	public static String getVersionForTracker(Context ctx) {
		String v = VersionUtils.getAppName(ctx);
		if(VersionUtils.isProductionVersion(ctx)){
			v = VersionUtils.getFullVersion(ctx);
		} else {
			v +=" test";
		}
		return v;
	}

}
