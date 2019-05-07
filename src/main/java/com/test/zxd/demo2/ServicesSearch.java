package com.test.zxd.demo2;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.bluetooth.*;
/**
 * @author xiaodong.zou
 * @title: ServicesSearch
 * @projectName bluetootchdemo
 * @description: TODO
 * @date 2019/5/7 9:26
 */
public class ServicesSearch {

	static final UUID OBEX_FILE_TRANSFER = new UUID(0x1106);

	public static final Vector<String> serviceUrlFound = new Vector();

	public static void main(String[] args) throws IOException, InterruptedException {

		// First run RemoteDeviceDiscovery and use discoved device
		RemoteDeviceDiscovery.main(null);

		serviceUrlFound.clear();

//		UUID serviceUUID = OBEX_OBJECT_PUSH;
		UUID serviceUUID = OBEX_FILE_TRANSFER;
		if ((args != null) && (args.length > 0)) {
			serviceUUID = new UUID(args[0], false);
		}

		final Object serviceSearchCompletedEvent = new Object();

		DiscoveryListener listener = new DiscoveryListener() {

			@Override
			public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
				System.out.println("111111111111111111111111111111111111111111");
			}
			@Override
			public void inquiryCompleted(int discType) {
				System.out.println("22222222222222222222222222222222222222222");
			}
			@Override
			public void servicesDiscovered(int transID, ServiceRecord[] serviceRecords) {
				for (ServiceRecord serviceRecord:serviceRecords) {
					System.out.println("serviceRecord if trust device:"+serviceRecord.getHostDevice().isTrustedDevice());
					int[] ids = serviceRecord.getAttributeIDs();
					for(int id:ids){
						System.out.println("attribute====>"+id+":"+serviceRecord.getAttributeValue(id));
					}
					String url = serviceRecord.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
					if (url == null) {
						continue;
					}
					serviceUrlFound.add(url);
					DataElement serviceName = serviceRecord.getAttributeValue(0x0100);
					if (serviceName != null) {
						System.out.println("service " + serviceName.getValue() + " found " + url);
					} else {
						System.out.println("service found " + url);
					}
				}
			}
			@Override
			public void serviceSearchCompleted(int transID, int respCode) {
				System.out.println("service search completed!");
				synchronized(serviceSearchCompletedEvent){
					serviceSearchCompletedEvent.notifyAll();
				}
			}

		};

		UUID[] searchUuidSet = new UUID[] { serviceUUID };
		int[] attrIDs =  new int[] {
				0x0100 // Service name
		};

		for(Enumeration en = RemoteDeviceDiscovery.devicesDiscovered.elements(); en.hasMoreElements(); ) {
			RemoteDevice btDevice = (RemoteDevice)en.nextElement();

			synchronized(serviceSearchCompletedEvent) {
				System.out.println("search services on " + btDevice.getBluetoothAddress() + " " + btDevice.getFriendlyName(false));
				LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, btDevice, listener);
				serviceSearchCompletedEvent.wait();
			}
		}

	}
}
