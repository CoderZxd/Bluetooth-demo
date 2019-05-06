import com.test.zxd.demo.BluetoothService;

/**
 * @author xiaodong.zou
 * @title: Application
 * @projectName bluetootchdemo
 * @description: TODO
 * @date 2019/5/6 16:06
 */
public class Application {
	public static void main(String[] args) {


		BluetoothService bluetoothService = new BluetoothService();

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		bluetoothService.stop();//一直没有设备接入的情况未做处理
	}
}
