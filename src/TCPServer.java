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
 * ʵ��TCP����������
 * ��ʾ������������� java.net.ServerSocket
 * ���췽����
 *         ServerSocket(int port) ���ݶ˿ں�
 * 
 * ����Ҫ�����飺����Ҫ��ÿͻ��˵��׽��ֶ���Socket
 *     Socket accept()
 */
public class TCPServer {

	// �����˿�
	private static final int PORT = 8003;

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null;
		Socket socket = null;
		try {
			// ������������Socket�����趨һ�������Ķ˿�PORT
			serverSocket = new ServerSocket(PORT);
			// ������Ҫ����ѭ����������˻�ȡ��Ϣ�Ĳ���Ӧ����һ��while��ѭ����
			while (true) {
				try {
					// �������ͻ��˵�����
					socket = serverSocket.accept();
				} catch (Exception e) {
					System.out.println("������ͻ��˵����ӳ����쳣");
					e.printStackTrace();
				}
				ServerThread thread = new ServerThread(socket);
				thread.start();
			}
		} catch (Exception e) {
			System.out.println("�˿ڱ�ռ��");
			e.printStackTrace();
		} finally {
			serverSocket.close();
		}
	}
}

// ������߳���
// �̳�Thread��Ļ���������дrun��������run�����ж�����Ҫִ�е�����
class ServerThread extends Thread {
	private Socket socket;
	InputStream inputStream;
	OutputStream outputStream;

	/**
	 * Э��汾��
	 */
	private int PV;
	/**
	 * ������
	 */
	private byte bFunctionCode;

	private byte functionCode;
	/**
	 * �豸����
	 */
	private int deviceType;
	/**
	 * ��ƷSN
	 */
	private long SN;
	/**
	 * �ն�ID
	 */
	private int ID;

	/**
	 * ��Ϣ���
	 */
	private int serial;
	/**
	 * ���ݳ���
	 */
	private int dataLength;

	private int mid = 0;
	/**
	 * RTCʱ��
	 */
	private long rtcTime;
	/**
	 * ����ID
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

				// ��װ�����������տͻ��˵�����
				BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
				DataInputStream dis = new DataInputStream(bis);
				byte[] bytess = new byte[1]; // һ�ζ�ȡһ��byte

				byte[] dataBytes = InputStream2ByteArray("D:\\guoleiming\\CS-iTVP-07.bin");
				byte[][] result = splitBytes(dataBytes, 512);

				// ���տͻ��˵���Ϣ����ӡ
				inputStream = socket.getInputStream();
				byte[] bytesd = new byte[54];
				inputStream.read(bytesd);
				String string = new String(bytesToHexString(bytesd));
				System.out.println(string);

				byte[] bytesds = hexStringToByteArray(string);
				// �����ǲ��Խ������ݣ�ȫ����ReportProcess.java��
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
					// �����ǲ��Խ������ݣ�ȫ����ReportProcess.java��
					PV = bytes[0];

					functionCode = bytes[1];
					imageId = bytes[16];

					packageId = Utilty.getInstance().bytes2Int(bytes, 18, 2);

					bFunctionCode = bytes[17];

					System.out.println(imageId + "--������");

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

					if (dis.available() == 0) { // һ������

						// System.out.println(socket.getRemoteSocketAddress() + ":" + ret);
						// //
						// byte[] bytes = hexStringToByteArray(ret);
						// // �����ǲ��Խ������ݣ�ȫ����ReportProcess.java��
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
						// ����������
						// if (bFunctionCode == 0x0E) {
						// outputStream = socket.getOutputStream();
						// byte[] dataBytes = InputStream2ByteArray("E:\\glm\\�½��ļ���\\CS-iTVP-07.bin");
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

				// ��ͻ��˷�����Ϣ
				// outputStream = socket.getOutputStream();
				// outputStream.write("OK".getBytes());
				// System.out.println("OK");

			}
		} catch (Exception e) {
			System.out.println("�ͻ��������Ͽ�������");
			// e.printStackTrace();
		}
		// �����������ر�socket
		try {
			socket.close();
		} catch (IOException e) {
			System.out.println("�ر����ӳ����쳣");
			e.printStackTrace();
		}
	}

	/**
	 * byte[]����ת��Ϊ16���Ƶ��ַ���
	 *
	 * @param bytes
	 *            Ҫת�����ֽ�����
	 * @return ת����Ľ��
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
	 * ������������
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

		// �����CRCЧ��
		byte[] dataBytes = InputStream2ByteArray("D:\\guoleiming\\CS-iTVP-07.bin");
		byte[] bytes2NoCRC = Arrays.copyOf(dataBytes, dataBytes.length);
		byte[] bytes2CRC = Utilty.getInstance().CRC16(bytes2NoCRC);

		bytesRead[10] = bytes2CRC[1];
		bytesRead[11] = bytes2CRC[0];

		// CRCУ��
		byte[] bytesNoCRC = Arrays.copyOf(bytesRead, bytesRead.length - 2);
		byte[] bytesCRC = Utilty.getInstance().CRC16(bytesNoCRC);

		bytesRead[12] = bytesCRC[1];
		bytesRead[13] = bytesCRC[0];

		return bytesRead;

	}

	/**
	 * ��������
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

		// �����������ݸ�����������
		System.arraycopy(databyte, 0, bytesRead, 12, databyte.length);

		// CRCУ��
		byte[] bytesNoCRC = Arrays.copyOf(bytesRead, bytesRead.length - 2);
		byte[] bytesCRC = Utilty.getInstance().CRC16(bytesNoCRC);
		bytesRead[524] = bytesCRC[1];
		bytesRead[525] = bytesCRC[0];

		return bytesRead;
	}

	/**
	 * �������ļ�ת��Ϊbyte����
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
	 * ���byte����
	 * 
	 * @param bytes
	 *            Ҫ��ֵ�����
	 * @param size
	 *            Ҫ���������һ��
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