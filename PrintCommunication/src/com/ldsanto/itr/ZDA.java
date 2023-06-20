package com.ldsanto.itr;

import com.zebra.sdk.comm.internal.ZDriverAdapter;

public class ZDA extends ZDriverAdapter{
	public static boolean isDriverLoaded = true;

	static {
		String str1 = System.getProperty("os.name").toLowerCase();
		String str2 = System.getProperty("sun.arch.data.model");
		try {
			if (str1.contains("windows")) {
				if (str2.equals("32")) {
					System.load("C:/ITR/dll/zDriverAdapter.dll");
				} else {
					System.load("C:/ITR/dll/zDriverAdapter64.dll");
				}
				/*if (str2.equals("32")) {
					System.loadLibrary("zDriverAdapter");
				} else {
					System.loadLibrary("zDriverAdapter64");
				} */
			} else if (!str1.contains("mac") && (str1.contains("nix") || str1.contains("nux"))) {

			} 
		} catch (Throwable throwable) {
			isDriverLoaded = false;
		} 
	}
}
