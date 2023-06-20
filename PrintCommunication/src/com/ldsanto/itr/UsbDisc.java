package com.ldsanto.itr;

import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.internal.ZDriverAdapter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterDriver;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterFilter;
import com.zebra.sdk.printer.discovery.DiscoveredUsbPrinter;
import com.zebra.sdk.printer.discovery.internal.UsbDiscoverWindows;
import com.zebra.sdk.printer.discovery.internal.UsbDiscovererLibUsb;
import com.zebra.sdk.printer.discovery.internal.UsbDiscoveryI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "unchecked", "rawtypes"})
public class UsbDisc {
	private static final String driverNameTag = "&&&DRIVERNAME:";

	private static final String portNameTag = "&&&PORTNAMES:";

	private static final String printerNameTag = "&&&PRINTERNAME:";

	private static final int ZEBRA_USB_VID = 2655;

	private static final List<String> SUPPORTED_MFG_NAMES = Arrays.asList(new String[] { "zebra", "zebra technologies", "zebra card printer", "zebra digital card printer" });

	public static DiscoveredPrinterDriver[] getZebraDriverPrinters() throws ConnectionException {
		String str = System.getProperty("os.name").toLowerCase();
		if (!str.contains("windows"))
			throw new ConnectionException("Invalid OS"); 
		if (!NUA.isDriverLoaded)
			throw new ConnectionException("The native code dll is not loaded."); 
		String[] arrayOfString = (String[])ZDriverAdapter.GetZebraPrintersWithDriverAndPortNames();
		ArrayList<DiscoveredPrinterDriver> arrayList = new ArrayList();
		for (String str1 : arrayOfString) {
			DiscoveredPrinterDriver discoveredPrinterDriver = createDiscoveredPrinterDriver(str1);
			if (printerIsOnUsbPort(discoveredPrinterDriver))
				arrayList.add(discoveredPrinterDriver); 
		} 
		DiscoveredPrinterDriver[] arrayOfDiscoveredPrinterDriver = new DiscoveredPrinterDriver[arrayList.size()];
		arrayList.toArray(arrayOfDiscoveredPrinterDriver);
		return arrayOfDiscoveredPrinterDriver;
	}

	private static boolean printerIsOnUsbPort(DiscoveredPrinterDriver paramDiscoveredPrinterDriver) {
		return paramDiscoveredPrinterDriver.portNames[0].toLowerCase().contains("usb");
	}

	private static DiscoveredPrinterDriver createDiscoveredPrinterDriver(String paramString) {
		int i = paramString.indexOf("&&&DRIVERNAME:") + "&&&DRIVERNAME:".length();
		int j = paramString.indexOf("&&&PORTNAMES:");
		int k = j + "&&&PORTNAMES:".length();
		int m = paramString.indexOf("&&&PRINTERNAME:");
		int n = m + "&&&PRINTERNAME:".length();
		String str1 = paramString.substring(i, j);
		String str2 = paramString.substring(k, m);
		String str3 = paramString.substring(n);
		String[] arrayOfString = str2.split(",");
		return new DiscoveredPrinterDriver(str3, str1, arrayOfString);
	}

	public static DiscoveredUsbPrinter[] getZebraUsbPrinters() throws ConnectionException {
		UsbDiscovererLibUsb usbDiscovererLibUsb = null;
		UsbDiscoverWindows usbDiscoverWindows = null;
		String str = System.getProperty("os.name").toLowerCase();
		if (str.contains("windows")) {
			usbDiscoverWindows = new UsbDiscoverWindows();
		} else {
			usbDiscovererLibUsb = new UsbDiscovererLibUsb();
		} 
		return storePrintersIntoArray((UsbDiscoveryI)usbDiscovererLibUsb, null);
	}

	public static DiscoveredUsbPrinter[] getZebraUsbPrinters(DiscoveredPrinterFilter paramDiscoveredPrinterFilter) throws ConnectionException {
		UsbDiscovererLibUsb usbDiscovererLibUsb = null;
		UsbDiscoverWindows usbDiscoverWindows = null;
		String str = System.getProperty("os.name").toLowerCase();
		if (str.contains("windows")) {
			usbDiscoverWindows = new UsbDiscoverWindows();
		} else {
			usbDiscovererLibUsb = new UsbDiscovererLibUsb();
		} 
		return storePrintersIntoArray((UsbDiscoveryI)usbDiscovererLibUsb, paramDiscoveredPrinterFilter);
	}

	private static DiscoveredUsbPrinter[] storePrintersIntoArray(UsbDiscoveryI paramUsbDiscoveryI, DiscoveredPrinterFilter paramDiscoveredPrinterFilter) throws ConnectionException {
		List list = null;
		list = paramUsbDiscoveryI.getPrinters(paramDiscoveredPrinterFilter);
		DiscoveredUsbPrinter[] arrayOfDiscoveredUsbPrinter = null;
		if (list != null) {
			arrayOfDiscoveredUsbPrinter = new DiscoveredUsbPrinter[list.size()];
			list.toArray((Object[])arrayOfDiscoveredUsbPrinter);
		} 
		return arrayOfDiscoveredUsbPrinter;
	}

	protected void loadZebraPrintersIntoArray(ArrayList<DiscoveredUsbPrinter> paramArrayList, DiscoveredUsbPrinter paramDiscoveredUsbPrinter, DiscoveredPrinterFilter paramDiscoveredPrinterFilter) {
		if (paramDiscoveredPrinterFilter == null)
			paramDiscoveredPrinterFilter = new DeviceFilterImpl(); 
		Map<String, String> map = paramDiscoveredUsbPrinter.getDiscoveryDataMap();
		if (map.containsKey("vid")) {
			String str = map.get("vid");
			if (str != null && !str.isEmpty() && Integer.parseInt(str, 16) == 2655 && paramDiscoveredPrinterFilter != null && paramDiscoveredPrinterFilter.shouldAddPrinter(paramDiscoveredUsbPrinter))
				paramArrayList.add(paramDiscoveredUsbPrinter); 
		} else if (map.containsKey("MFG")) {
			String str = ((String)map.get("MFG")).trim().toLowerCase();
			if (SUPPORTED_MFG_NAMES.contains(str) && paramDiscoveredPrinterFilter != null && paramDiscoveredPrinterFilter.shouldAddPrinter(paramDiscoveredUsbPrinter))
				paramArrayList.add(paramDiscoveredUsbPrinter); 
		} 
	}

	private class DeviceFilterImpl implements DiscoveredPrinterFilter {
		private DeviceFilterImpl() {}

		public boolean shouldAddPrinter(DiscoveredPrinter param1DiscoveredPrinter) {
			return true;
		}
	}
}
