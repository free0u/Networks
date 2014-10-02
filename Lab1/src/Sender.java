import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;

class Sender implements Runnable {
    private String name;

    Sender(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        byte[] message = null;
        try {
            // ip
            InetAddress localHost = InetAddress.getLocalHost();
            byte[] ip = localHost.getAddress();

            // mac address
            byte[] mac = NetworkInterface.getByInetAddress(localHost).getHardwareAddress();

            // name
            byte[] byteName = name.getBytes("UTF-8");

            message = new byte[ip.length + mac.length + byteName.length + 1];

            int ind = 0;
            for (byte i : ip) {
                message[ind++] = i;
            }
            for (byte i : mac) {
                message[ind++] = i;
            }
            for (byte i : byteName) {
                message[ind++] = i;
            }
            message[ind] = 0;


            while (true) {
                DatagramSocket clientSocket = new DatagramSocket();
                InetAddress IPAddress = InetAddress.getByName("255.255.255.255");

                DatagramPacket sendPacket = new DatagramPacket(message, message.length, IPAddress, 7777);
                clientSocket.send(sendPacket);

                Thread.sleep(2000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}