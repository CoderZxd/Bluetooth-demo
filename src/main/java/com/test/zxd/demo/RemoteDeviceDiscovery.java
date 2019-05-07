package com.test.zxd.demo;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.microedition.io.Connector;

import com.intel.bluetooth.RemoteDeviceHelper;

public class RemoteDeviceDiscovery {

	public final static Set<RemoteDevice> devicesDiscoveredSet = new HashSet<RemoteDevice>();

	public static void runDiscovery() throws IOException, InterruptedException {
		findDevices();
	}

	private static void findDevices() throws IOException, InterruptedException {
		final Object inquiryCompletedEvent = new Object();
		devicesDiscoveredSet.clear();
		DiscoveryListener listener = new DiscoveryListener() {
			@Override
			public void inquiryCompleted(int discType) {
				System.out.println("#" + "搜索完成");
				synchronized (inquiryCompletedEvent) {
					inquiryCompletedEvent.notifyAll();
				}
			}

			@Override
			public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
				devicesDiscoveredSet.add(remoteDevice);
				try {
					System.out.println("#发现设备:" + remoteDevice.getFriendlyName(false)+" "+remoteDevice.getBluetoothAddress());
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			@Override
			public void servicesDiscovered(int arg0, ServiceRecord[] arg1) {
				System.out.println("#" + "servicesDiscovered");
			}

			@Override
			public void serviceSearchCompleted(int arg0, int arg1) {
				System.out.println("#" + "serviceSearchCompleted");
			}
		};

		synchronized (inquiryCompletedEvent) {
			LocalDevice localDevice = LocalDevice.getLocalDevice();
			System.out.println("#本机蓝牙名称:" + localDevice.getFriendlyName()+",蓝牙地址为:"+localDevice.getBluetoothAddress());
			boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC,listener);
			if (started) {
				System.out.println("#" + "等待搜索完成...");
				inquiryCompletedEvent.wait();
				LocalDevice.getLocalDevice().getDiscoveryAgent().cancelInquiry(listener);
				System.out.println("#发现设备数量：" + devicesDiscoveredSet.size());
			}
		}

	}

	public static Set<RemoteDevice> getDevices() {
		return devicesDiscoveredSet;
	}

	public static String deviceName(RemoteDevice remoteDevice) {

		String address = remoteDevice.getBluetoothAddress();

		String name = "";
		try {
			name = remoteDevice.getFriendlyName(false);
		} catch (IOException e) {
			System.out.println("#Error: " + e.getMessage());
			try {
				name = remoteDevice.getFriendlyName(false);
			} catch (IOException e2) {
				System.out.println("#Error: " + e2.getMessage());
			}

		}

		String rssi = "NA";

		String toret = "";

		if(BlucatState.csv) {
			toret += (new Date()).getTime() + ", ";
		}
		toret += BluCatUtil.clean(address) + ", " + "\"" + BluCatUtil.clean(name) + "\", " + "Trusted:"
				+ remoteDevice.isTrustedDevice() + ", " + "Encrypted:" + remoteDevice.isEncrypted();

		if (BlucatState.rssi) {
			try {
				rssi = String.valueOf(RemoteDeviceHelper.readRSSI(remoteDevice));
			} catch (Throwable e) {

				String url = "btl2cap://" + remoteDevice.getBluetoothAddress() + ":1";

				try {
					BlucatState.connection = Connector.open(url, Connector.READ_WRITE, true);
					rssi = String.valueOf(RemoteDeviceHelper.readRSSI(remoteDevice));
					BlucatState.connection.close();

				} catch (IOException e1) {
				}
			}
			toret += ", " + rssi;
		}
		return toret;

	}

}
