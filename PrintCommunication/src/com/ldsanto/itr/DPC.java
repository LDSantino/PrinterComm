package com.ldsanto.itr;

import com.zebra.sdk.comm.ConnectionA;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.ConnectionReestablisher;
import com.zebra.sdk.comm.internal.ConnectionInfo;
import com.zebra.sdk.comm.internal.DriverConnectionReestablisher;
import com.zebra.sdk.comm.internal.NotMyConnectionDataException;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterDriver;
import com.zebra.sdk.util.internal.RegexUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class DPC extends ConnectionA {
	private String printerName;

	private ByteArrayOutputStream bOut;

	protected DPC(ConnectionInfo paramConnectionInfo) throws NotMyConnectionDataException {
		String str1 = System.getProperty("os.name").toLowerCase();
		if (!str1.contains("windows"))
			throw new NotMyConnectionDataException("Invalid OS"); 
		String str2 = paramConnectionInfo.getMyData();
		String str3 = "^\\s*((?i)USB:)?([^:]+)\\s*$";
		List<String> list = RegexUtil.getMatches(str3, str2);
		if (list.isEmpty())
			throw new NotMyConnectionDataException("USB Connection doesn't understand " + str2); 
		this.printerName = list.get(2);
		this.maxTimeoutForRead = 5000;
		this.timeToWaitForMoreData = 500;
		new ByteArrayInputStream(new byte[0]);
		this.bOut = new ByteArrayOutputStream();
	}

	public DPC(String paramString) throws ConnectionException {
		this(paramString, 5000, 500);
	}

	public DPC(String paramString, int paramInt1, int paramInt2) throws ConnectionException {
		String str = System.getProperty("os.name").toLowerCase();
		if (!str.contains("windows"))
			throw new ConnectionException("Invalid OS"); 
		this.maxTimeoutForRead = paramInt1;
		this.timeToWaitForMoreData = paramInt2;
		this.printerName = paramString;
		new ByteArrayInputStream(new byte[0]);
		this.bOut = new ByteArrayOutputStream();
	}

	private String getConnectionBuilderPrefix() {
		return "USB";
	}

	public String getPrinterName() {
		return this.printerName;
	}

	public void open() throws ConnectionException {
		if (!ZDA.isDriverLoaded)
			throw new ConnectionException("The native code dll is not loaded."); 
		if (this.isConnected)
			return; 
		boolean bool = false;
		DiscoveredPrinterDriver[] arrayOfDiscoveredPrinterDriver = UsbDisc.getZebraDriverPrinters();
		for (DiscoveredPrinterDriver discoveredPrinterDriver : arrayOfDiscoveredPrinterDriver) {
			if (discoveredPrinterDriver.printerName.equalsIgnoreCase(this.printerName)) {
				bool = true;
				break;
			} 
		} 
		if (!bool)
			throw new ConnectionException("No installed printer named " + this.printerName); 
		if (!ZDA.OpenPrinter(this.printerName))
			throw new ConnectionException("Failed to open printer " + this.printerName); 
		this.isConnected = true;
	}

	public void close() throws ConnectionException {
		if (!ZDA.isDriverLoaded)
			throw new ConnectionException("The native code dll is not loaded."); 
		if (this.isConnected) {
			if (!ZDA.ClosePrinter(this.printerName))
				throw new ConnectionException("Failed to close printer " + this.printerName); 
			this.isConnected = false;
		} 
	}

	public byte[] read() throws ConnectionException {
		if (this.isConnected) {
			byte[] arrayOfByte1 = (this.bOut.size() > 0) ? this.bOut.toByteArray() : new byte[0];
			byte[] arrayOfByte2 = readFromPrinter();
			byte[] arrayOfByte3 = new byte[arrayOfByte2.length + arrayOfByte1.length];
			System.arraycopy(arrayOfByte1, 0, arrayOfByte3, 0, arrayOfByte1.length);
			System.arraycopy(arrayOfByte2, 0, arrayOfByte3, arrayOfByte1.length, arrayOfByte2.length);
			this.bOut = new ByteArrayOutputStream();
			return arrayOfByte3;
		} 
		return null;
	}

	public int readChar() throws ConnectionException {
		if (this.isConnected) {
			byte[] arrayOfByte;
			if (this.bOut.size() == 0) {
				arrayOfByte = readFromPrinter();
			} else {
				arrayOfByte = this.bOut.toByteArray();
			} 
			this.bOut = new ByteArrayOutputStream();
			this.bOut.write(arrayOfByte, 1, arrayOfByte.length - 1);
			return arrayOfByte[0] & 0xFF;
		} 
		return -1;
	}

	public void write(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) throws ConnectionException {
		if (!ZDA.isDriverLoaded)
			throw new ConnectionException("The native code dll is not loaded."); 
		if (!isConnected())
			throw new ConnectionException("The connection is not open"); 
		File file = null;
		try {
			file = File.createTempFile("_zsdk_", "_zsdk_");
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(paramArrayOfbyte, paramInt1, paramInt2);
			fileOutputStream.flush();
			fileOutputStream.close();
			if (!ZDA.SendFileToPrinter(this.printerName, file.getAbsolutePath()))
				throw new ConnectionException("Error writing to connection: " + this.printerName); 
			writeToLogStream(paramArrayOfbyte, paramInt1, paramInt2);
			file.delete();
		} catch (IOException iOException) {
			throw new ConnectionException("Error preparing data for writing to connection");
		} finally {
			if (file != null && file.exists())
				file.delete(); 
		} 
	}

	public int bytesAvailable() throws ConnectionException {
		int i = 0;
		if (this.isConnected && this.bOut.size() == 0) {
			byte[] arrayOfByte = readFromPrinter();
			if (arrayOfByte.length < 0)
				throw new ConnectionException("Failed to read from the USB port: " + this.printerName); 
			if (arrayOfByte.length > 0) {
				try {
					this.bOut.write(arrayOfByte);
				} catch (IOException iOException) {
					throw new ConnectionException("Failed to read from the USB port: " + this.printerName);
				} 
				i += arrayOfByte.length;
			} 
		} else {
			i = this.bOut.size();
		} 
		return i;
	}

	protected byte[] readFromPrinter() {
		byte[] arrayOfByte = null;
		if (ZDA.isDriverLoaded == true)
			arrayOfByte = ZDA.GetBinaryDataFromPrinter(this.printerName); 
		return arrayOfByte;
	}

	public String toString() {
		return getConnectionBuilderPrefix() + ":" + getPrinterName();
	}

	public String getSimpleConnectionName() {
		return getPrinterName();
	}

	public ConnectionReestablisher getConnectionReestablisher(long paramLong) throws ConnectionException {
		return (ConnectionReestablisher)new DriverConnectionReestablisher(this, paramLong);
	}
}
