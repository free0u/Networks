import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by free0u on 10/31/14.
 */
public class ServerAnnouncer implements Runnable {

    ServerProtocol protocol;

    public ServerAnnouncer(ServerProtocol protocol) {
        this.protocol = protocol;
    }

    public void run() {
        try {
            while (true) {
                DatagramSocket clientSocket = new DatagramSocket();
                InetAddress IPAddress = InetAddress.getByName("255.255.255.255");

                byte[] ann = protocol.announce();
                DatagramPacket sendPacket = new DatagramPacket(ann, ann.length, IPAddress, 7777);
                clientSocket.send(sendPacket);

                Thread.sleep(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
