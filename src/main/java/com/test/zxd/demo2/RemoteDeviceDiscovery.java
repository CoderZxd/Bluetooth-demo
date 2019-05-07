package com.test.zxd.demo2;

import java.io.IOException;
import java.util.Vector;
import javax.bluetooth.*;

/**
 * @author xiaodong.zou
 * @title: RemoteDeviceDiscovery
 * @projectName bluetootchdemo
 * @description: TODO
 * @date 2019/5/7 9:25
 */
public class RemoteDeviceDiscovery {
	public static final Vector<RemoteDevice> devicesDiscovered = new Vector();

	public static void main(String[] args) throws IOException, InterruptedException {

		final Object inquiryCompletedEvent = new Object();

		devicesDiscovered.clear();

		DiscoveryListener listener = new DiscoveryListener() {

			@Override
			public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
				System.out.print("Device " + btDevice.getBluetoothAddress() + " found,");
				devicesDiscovered.addElement(btDevice);
				try {
					System.out.println("name " + btDevice.getFriendlyName(false));
				} catch (IOException cantGetDeviceName) {
				}
			}

			@Override
			public void inquiryCompleted(int discType) {
				System.out.println("Device Inquiry completed!");
				synchronized(inquiryCompletedEvent){
					inquiryCompletedEvent.notifyAll();
				}
			}

			@Override
			public void serviceSearchCompleted(int transID, int respCode) {
				System.out.println("#" + "serviceSearchCompleted");
			}

			@Override
			public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
				System.out.println("#" + "servicesDiscovered"+",transID:"+transID);
			}
		};

		synchronized(inquiryCompletedEvent) {
			LocalDevice localDevice = LocalDevice.getLocalDevice();
			System.out.println("#本机蓝牙名称:" + localDevice.getFriendlyName()+",蓝牙地址为:"+localDevice.getBluetoothAddress());
			boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
			if (started) {
				System.out.println("wait for device inquiry to complete...");
				inquiryCompletedEvent.wait();
				System.out.println(devicesDiscovered.size() +  " device(s) found");
			}
		}
	}
}
