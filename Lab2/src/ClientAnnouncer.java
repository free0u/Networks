import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

/**
 * Created by free0u on 10/31/14.
 */
public class ClientAnnouncer implements Runnable {
    Map<Host, List<Long>> hosts;
    ClientProtocol protocol;
    boolean running;

    private void process(byte[] data) throws IOException {
        Host host = protocol.parseHost(data);
        List<Long> timestamps = hosts.get(host);
        if (timestamps == null) {
            timestamps = new ArrayList<>();
            hosts.put(host, timestamps);
        }

        long ts = (new Date()).getTime();
        timestamps.add(ts);

        while (timestamps.size() > 0) {
            long delta = ts - timestamps.get(0);
            if (delta > 10000) { // 10 sec
                timestamps.remove(0);
            } else {
                break;
            }
        }
    }

    public ClientAnnouncer(ClientProtocol protocol, Map<Host, List<Long>> hosts) {
        this.protocol = protocol;
        this.hosts = hosts;
        this.running = true;
    }

    @Override
    public void run() {
        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(7777);
            byte[] receiveData = new byte[1024];
            while (running) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                byte[] data = receivePacket.getData();
                process(data);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
