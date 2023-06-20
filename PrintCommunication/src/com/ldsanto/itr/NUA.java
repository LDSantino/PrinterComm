package com.ldsanto.itr;

public class NUA {
	public static boolean isDriverLoaded = true;

	public static native String[] EnumeratePrinters();

	public native int Open(String paramString);

	public native void Close(int paramInt);

	public native void SetMaxReadTimeout(int paramInt1, int paramInt2);

	public native String GetModel(int paramInt) throws Exception;

	public native String GetManufacturer(int paramInt) throws Exception;

	public native String GetDeviceIDString(int paramInt) throws Exception;

	public native int Read(int paramInt1, byte[] paramArrayOfbyte, int paramInt2) throws Exception;

	public native int Write(int paramInt1, byte[] paramArrayOfbyte, int paramInt2) throws Exception;

	static {
		try {
			String str1 = System.getProperty("os.name").toLowerCase();
			String str2 = System.getProperty("sun.arch.data.model");
			if (str1.contains("windows"))
				/*if (str2.equals("32")) {
					System.loadLibrary("ZebraNativeUsbAdapter_32");
				} else {
					System.loadLibrary("ZebraNativeUsbAdapter_64");
				} */
				if (str2.equals("32")) {
					System.load("C:/ITR/dll/ZebraNativeUsbAdapter_32.dll");
				} else {
					System.load("C:/ITR/dll/ZebraNativeUsbAdapter_64.dll");
				}  
		} catch (Throwable throwable) {
			isDriverLoaded = false;
		} 
	}
}
