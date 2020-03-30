import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Array;
import java.util.Arrays;

/*
 * 实现TCP服务器程序
 * 表示服务器程序的类 java.net.ServerSocket
 * 构造方法：
 *         ServerSocket(int port) 传递端口号
 * 
 * 很重要的事情：必须要获得客户端的套接字对象Socket
 *     Socket accept()
 */
public class TCPServer {

	// 监听端口
	private static final int PORT = 8003;

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null;
		Socket socket = null;
		try {
			// 建立服务器的Socket，并设定一个监听的端口PORT
			serverSocket = new ServerSocket(PORT);
			// 由于需要进行循环监听，因此获取消息的操作应放在一个while大循环中
			while (true) {
				try {
					// 建立跟客户端的连接
					socket = serverSocket.accept();
				} catch (Exception e) {
					System.out.println("建立与客户端的连接出现异常");
					e.printStackTrace();
				}
				ServerThread thread = new ServerThread(socket);
				thread.start();
			}
		} catch (Exception e) {
			System.out.println("端口被占用");
			e.printStackTrace();
		} finally {
			serverSocket.close();
		}
	}
}

// 服务端线程类
// 继承Thread类的话，必须重写run方法，在run方法中定义需要执行的任务。
class ServerThread extends Thread {
	private Socket socket;
	InputStream inputStream;
	OutputStream outputStream;

	/**
	 * 协议版本号
	 */
	private int PV;
	/**
	 * 功能码
	 */
	private byte bFunctionCode;

	private byte functionCode;
	/**
	 * 设备类型
	 */
	private int deviceType;
	/**
	 * 产品SN
	 */
	private long SN;
	/**
	 * 终端ID
	 */
	private int ID;

	/**
	 * 消息序号
	 */
	private int serial;
	/**
	 * 数据长度
	 */
	private int dataLength;

	private int mid = 0;
	/**
	 * RTC时间
	 */
	private long rtcTime;
	/**
	 * 镜像ID
	 */
	private int imageId;

	private int packageId;

	private int i = 0, j = 0, k = 0;

	private int index = 0;

	public ServerThread(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		try {
			while (true) {

				// 封装输入流（接收客户端的流）
				BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
				DataInputStream dis = new DataInputStream(bis);
				byte[] bytess = new byte[1]; // 一次读取一个byte

				byte[] dataBytes = InputStream2ByteArray("D:\\guoleiming\\CS-iTVP-07.bin");
				byte[][] result = splitBytes(dataBytes, 512);

				// 接收客户端的消息并打印
				inputStream = socket.getInputStream();
				byte[] bytesd = new byte[54];
				inputStream.read(bytesd);
				String string = new String(bytesToHexString(bytesd));
				System.out.println(string);

				byte[] bytesds = hexStringToByteArray(string);
				// 以下是测试解析数据，全部在ReportProcess.java中
				bFunctionCode = bytesds[1];

				if (bFunctionCode == 0x01) {
					outputStream = socket.getOutputStream();
					byte[] data1 = UpgradeBoot(result.length);
					outputStream.write(data1);
				}

				while (dis.read(bytess) != -1) {

					inputStream = socket.getInputStream();
					byte[] bytesdsd = new byte[26];
					inputStream.read(bytesdsd);
					String strings = new String(bytesToHexString(bytesdsd));
					System.out.println(strings);

					byte[] bytes = hexStringToByteArray(strings);
					// 以下是测试解析数据，全部在ReportProcess.java中
					PV = bytes[0];

					functionCode = bytes[1];
					imageId = bytes[16];

					packageId = Utilty.getInstance().bytes2Int(bytes, 18, 2);

					bFunctionCode = bytes[17];

					System.out.println(imageId + "--错误码");

					if (functionCode == (byte) 0xAA && j < result.length
							&& (bFunctionCode == (byte) 0x10 || bFunctionCode == (byte) 0x0f)) {
						j = packageId + 1;
						byte[] data2 = Upgrade(j, result[j]);
						outputStream.write(data2);
						System.out.println(packageId + "--" + result.length);
					} else if ((functionCode == (byte) 0x0E && j < result.length)) {
						index = Utilty.getInstance().bytes2Int(bytes, 20, 2);
						j = index;
						byte[] data2 = Upgrade(j, result[j]);
						outputStream.write(data2);
						System.out.println(index + "--" + result.length);
					}

					if (j == result.length) {
						outputStream.close();
					}

					System.out.println("B:" + j);

					if (dis.available() == 0) { // 一个请求

						// System.out.println(socket.getRemoteSocketAddress() + ":" + ret);
						// //
						// byte[] bytes = hexStringToByteArray(ret);
						// // 以下是测试解析数据，全部在ReportProcess.java中
						// PV = bytes[0];
						// bFunctionCode = bytes[1];
						// deviceType = Utilty.getInstance().bytes2Int(bytes, 2, 1);
						// SN = Utilty.getInstance().bytesToLong(bytes, 4, 4, true);
						// ID = Utilty.getInstance().bytes2Int(bytes, 8, 2);
						// mid = Utilty.getInstance().bytes2Int(bytes, 12, 2);
						// dataLength = Utilty.getInstance().bytes2Int(bytes, 14, 2);
						// rtcTime = Utilty.getInstance().bytesToLong(bytes, 16, 4, true);
						// imageId = Utilty.getInstance().bytes2Int(bytes, 20, 2);
						//
						// System.out.println(
						// "bFunctionCode:" + bFunctionCode + ";SN:" + SN + ";serial:" + serial +
						// ";mid:" + mid);
						//
						// ret="";
						// 升级请求报文
						// if (bFunctionCode == 0x0E) {
						// outputStream = socket.getOutputStream();
						// byte[] dataBytes = InputStream2ByteArray("E:\\glm\\新建文件夹\\CS-iTVP-07.bin");
						// byte[][] result = splitBytes(dataBytes, 512);
						//
						// byte[] data1 = UpgradeBoot(result.length);
						// outputStream.write(data1);
						//
						// String rett="";
						// rett +=bytesToHexString(bytess) + " ";
						// System.out.println(socket.getRemoteSocketAddress() + ":" + ret);

						// outputStream.close();
						// for (int i = 0; i < result.length; i++) {
						// byte[] data2=Upgrade(result[i]);
						// outputStream.write(data2);
						// }
						// }
					}
				}

				// 向客户端发送消息
				// outputStream = socket.getOutputStream();
				// outputStream.write("OK".getBytes());
				// System.out.println("OK");

			}
		} catch (Exception e) {
			System.out.println("客户端主动断开连接了");
			// e.printStackTrace();
		}
		// 操作结束，关闭socket
		try {
			socket.close();
		} catch (IOException e) {
			System.out.println("关闭连接出现异常");
			e.printStackTrace();
		}
	}

	/**
	 * byte[]数组转换为16进制的字符串
	 *
	 * @param bytes
	 *            要转换的字节数组
	 * @return 转换后的结果
	 */
	public static String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * 升级启动报文
	 * 
	 * @throws IOException
	 */
	private byte[] UpgradeBoot(int length) throws IOException {
		byte[] bytesRead = new byte[14];
		bytesRead[0] = 0x02;
		bytesRead[1] = 0x0F;
		bytesRead[2] = (byte) (ID & 0xFF);
		bytesRead[3] = (byte) (ID >> 8);
		byte[] bytesmid = Utilty.getInstance().int2Bytes(this.mid, 2);
		bytesRead[4] = bytesmid[1];
		bytesRead[5] = bytesmid[0];
		byte[] bytesDataLength = Utilty.getInstance().int2Bytes(4, 2);
		bytesRead[6] = bytesDataLength[1];
		bytesRead[7] = bytesDataLength[0];
		byte[] bytesLength = Utilty.getInstance().int2Bytes(length, 2);
		bytesRead[8] = bytesLength[1];
		bytesRead[9] = bytesLength[0];

		// 镜像包CRC效验
		byte[] dataBytes = InputStream2ByteArray("D:\\guoleiming\\CS-iTVP-07.bin");
		byte[] bytes2NoCRC = Arrays.copyOf(dataBytes, dataBytes.length);
		byte[] bytes2CRC = Utilty.getInstance().CRC16(bytes2NoCRC);

		bytesRead[10] = bytes2CRC[1];
		bytesRead[11] = bytes2CRC[0];

		// CRC校验
		byte[] bytesNoCRC = Arrays.copyOf(bytesRead, bytesRead.length - 2);
		byte[] bytesCRC = Utilty.getInstance().CRC16(bytesNoCRC);

		bytesRead[12] = bytesCRC[1];
		bytesRead[13] = bytesCRC[0];

		return bytesRead;

	}

	/**
	 * 升级报文
	 */
	private byte[] Upgrade(int j, byte[] databyte) {
		byte[] bytesRead = new byte[526];
		bytesRead[0] = 0x02;
		bytesRead[1] = 0x10;
		bytesRead[2] = (byte) (ID & 0xFF);
		bytesRead[3] = (byte) (ID >> 8);
		byte[] bytesmid = Utilty.getInstance().int2Bytes(this.mid, 2);
		bytesRead[4] = bytesmid[1];
		bytesRead[5] = bytesmid[0];
		byte[] bytesDataLength = Utilty.getInstance().int2Bytes(516, 2);
		bytesRead[6] = bytesDataLength[1];
		bytesRead[7] = bytesDataLength[0];

		byte[] bytesDataId = Utilty.getInstance().int2Bytes(j, 2);
		bytesRead[8] = bytesDataId[1];
		bytesRead[9] = bytesDataId[0];

		bytesRead[10] = 0X00;
		bytesRead[11] = 0X00;

		// 将升级包数据复制在数组中
		System.arraycopy(databyte, 0, bytesRead, 12, databyte.length);

		// CRC校验
		byte[] bytesNoCRC = Arrays.copyOf(bytesRead, bytesRead.length - 2);
		byte[] bytesCRC = Utilty.getInstance().CRC16(bytesNoCRC);
		bytesRead[524] = bytesCRC[1];
		bytesRead[525] = bytesCRC[0];

		return bytesRead;
	}

	/**
	 * 将本地文件转化为byte数组
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	private byte[] InputStream2ByteArray(String filePath) throws IOException {
		InputStream in = new FileInputStream(filePath);
		byte[] data = toByteArray(in);
		in.close();
		return data;
	}

	private byte[] toByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024 * 4];
		int n = 0;
		while ((n = in.read(buffer)) != -1) {
			out.write(buffer, 0, n);
		}
		return out.toByteArray();
	}

	/**
	 * 拆分byte数组
	 * 
	 * @param bytes
	 *            要拆分的数组
	 * @param size
	 *            要按几个组成一份
	 * @return
	 */
	public byte[][] splitBytes(byte[] bytes, int size) {
		double splitLength = Double.parseDouble(size + "");
		int arrayLength = (int) Math.ceil(bytes.length / splitLength);
		byte[][] result = new byte[arrayLength][];
		int from, to;
		for (int i = 0; i < arrayLength; i++) {

			from = (int) (i * splitLength);
			to = (int) (from + splitLength);
			if (to > bytes.length)
				to = bytes.length;
			result[i] = Arrays.copyOfRange(bytes, from, to);
		}
		return result;
	}

}