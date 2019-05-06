package com.test.zxd.demo;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class BluetoothService implements Runnable {
	private Boolean stopFlag = false;

	//本机蓝牙设备
	private LocalDevice localDevice = null;

	// 流连接
	private StreamConnection streamConnection = null;

	// 接受数据的字节流
	private byte[] acceptdByteArray = new byte[1024];

	// 输入流
	private DataInputStream inputStream;

	//接入通知
	private StreamConnectionNotifier streamConnectionNotifier;

	//线程池
	private  final static ExecutorService serviceExcuters = Executors.newCachedThreadPool();
	

	public BluetoothService() {
		try {
			BluCatUtil.doctorDevice(); 					// 驱动检查
			RemoteDeviceDiscovery.runDiscovery();		// 搜索附近所有的蓝牙设备
			 System.out.println("$$$$$$$$$$$$$$$$$devicesDiscoveredSet:"+RemoteDeviceDiscovery.getDevices());
		} catch (IOException | InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			localDevice = LocalDevice.getLocalDevice();
			if(!localDevice.setDiscoverable(DiscoveryAgent.GIAC)) {
				System.out.println("请将蓝牙设置为可被发现");
			}else{
				System.out.println(localDevice.getFriendlyName()+"蓝牙已设置为可被发现!");
				System.out.println(localDevice.getBluetoothAddress());
				System.out.println(localDevice.getDeviceClass().getClass().getName());
			}
			/*Set<RemoteDevice> devicesDiscoveredSet = RemoteDeviceDiscovery.getDevices();		//附近所有的蓝牙设备，必须先执行 runDiscovery
			if (devicesDiscoveredSet.iterator().hasNext()) {									//连接
				RemoteDevice first = devicesDiscoveredSet.iterator().next();
				streamConnection = (StreamConnection) Connector.open("btspp://" + first.getBluetoothAddress() + ":1");
			}*/
			/**
			 * 作为服务端，被请求
			 */
//			String url = "btspp://localhost:0000100000001000800000805f9b34fb;name=RemoteBluetooth";
			String url = "btspp://localhost:"+localDevice.getBluetoothAddress()+";name=RemoteBluetooth";
            streamConnectionNotifier = (StreamConnectionNotifier)Connector.open(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		serviceExcuters.submit(this);
	}

	@Override
	public void run() {
		try {
			System.out.println("waiting for connection...");
			String inStr = null;
			streamConnection = streamConnectionNotifier.acceptAndOpen();				//阻塞的，等待设备连接,这里就没有作中断处理了，如果没有连接该线程就无法关闭
			System.out.println("*********************************设备连接成功*************************************");
			inputStream = streamConnection.openDataInputStream();
			int length;
			while (true) {
				if ((inputStream.available()) <= 0) {					//不阻塞线程
					if (stopFlag) {                                        //UI停止后，关闭
						break;
					}
					Thread.sleep(800);									//数据间隔比较长，手动堵塞线程
				} else {
					length = inputStream.read(acceptdByteArray);
					if(length>0) {
						inStr = new String(acceptdByteArray,0,length);
						System.out.println(inStr);
					}

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			try {
				if(inputStream != null) {
					inputStream.close();
				}
				if(streamConnection != null) {
					streamConnection.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public synchronized void stop() {
		System.out.println("#######stop##############");
		stopFlag = true;
		serviceExcuters.shutdown();
	}
}
