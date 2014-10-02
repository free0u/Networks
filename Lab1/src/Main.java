import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;

public class Main {
    private String ipToString(byte[] ip) {
        String res = "";
        for (int i = 0; i < 4; ++i) {
            res += (ip[i] & 0xFF);
            if (i < 3) {
                res += ".";
            }
        }
        return res;
    }

    private String macToString(byte[] mac) {
        String res = "";
        int[] mac2 = new int[6];
        for (int i = 0; i < 6; ++i) {
            mac2[i] = (mac[i] & 0xFF);
        }
        for (int i = 0; i < 6; ++i) {
            String t = Integer.toHexString(mac2[i]);
            if (t.length() == 1) {
                t = "0" + t;
            }
            res += t;

            if (i < 5) {
                res += ".";
            }
        }

        return res;
    }

    private User parseRawData(byte[] message) {
        byte[] ip = new byte[4];
        byte[] mac = new byte[6];
        byte[] byteName = null;

        System.arraycopy(message, 0, ip, 0, 4);
        System.arraycopy(message, 4, mac, 0, 6);

        for (int i = 10; i < message.length; ++i) {
            if (message[i] == 0) {
                byteName = Arrays.copyOfRange(message, 10, i);
                break;
            }
        }

        String sIp = ipToString(ip);
        String sMac = macToString(mac);
        String sName = null;
        try {
            sName = new String(byteName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            sName = "error_name";
        }

        User user = new User(sIp, sMac, sName);

        return user;
    }
    
    Map<User, List<Long>> users = new HashMap<>();
    private void process(byte[] message) {
        User user = parseRawData(message);

        List<Long> timestamps = users.get(user);

        if (timestamps == null) {
            timestamps = new ArrayList<>();
            users.put(user, timestamps);
        }

        long ts = (new Date()).getTime();
        timestamps.add(ts);
    }

    private void runServer() throws IOException {
        Sender sender = new Sender("arch-pc0");
        Thread th = new Thread(sender);
        th.start();

        users = new HashMap<>();
        Log log = new Log(users);
        Thread logThread = new Thread(log);
        logThread.start();

        DatagramSocket serverSocket = new DatagramSocket(7777);
        byte[] receiveData = new byte[1024];
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            byte[] data = receivePacket.getData();
            process(data);
        }
    }

    public static void main(String[] args) throws IOException {
        new Main().runServer();
    }
}
