import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by free0u on 10/31/14.
 */
public class Client {
    private static ClientProtocol protocol;
    private static String currentIp;

    private static Socket getConn() {
        try {
            Socket soc = new Socket(currentIp, 7777);
            return soc;
        } catch (IOException e) {
            return null;
        }
    }

    private static void cmdList() throws IOException {
        Socket soc = getConn();
        if (soc == null) {
            System.out.println("Connection failed");
            return;
        }
        DataOutputStream out = new DataOutputStream(soc.getOutputStream());
        out.write(protocol.list());

        InputStream in = soc.getInputStream();
        List<FileEntry> l = protocol.parseList(in);
        for (FileEntry f : l) {
            System.out.println(f.md5 + " " + f.name);
        }
    }

    private static void cmdGet(String name) throws IOException {
        Socket soc = getConn();
        if (soc == null) {
            System.out.println("Connection failed");
            return;
        }
        DataOutputStream out = new DataOutputStream(soc.getOutputStream());
        out.write(protocol.get(name));

        InputStream in = soc.getInputStream();
        boolean result = protocol.parseGet(in, name);
        if (result) {
            System.out.println("get complete");
        }
    }

    private static void cmdPut(String name) throws IOException {
        Socket soc = getConn();
        if (soc == null) {
            System.out.println("Connection failed");
            return;
        }
        DataOutputStream out = new DataOutputStream(soc.getOutputStream());
        protocol.put(out, name);
    }

    private static void clearConsole() {
        final String ANSI_CLS = "\u001b[2J";
        final String ANSI_HOME = "\u001b[H";
        System.out.print(ANSI_CLS + ANSI_HOME);
        System.out.flush();
    }

    private static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    private static void printHosts(Map<Host, List<Long>> hosts) {
        long ts = (new Date()).getTime();

        for (Map.Entry<Host, List<Long>> entry: hosts.entrySet()) {
            Host host = entry.getKey();
            List<Long> timestamps = entry.getValue();
            if (ts - timestamps.get(timestamps.size() - 1) > 4000) { // 4 sec
                continue;
            }

            String name = padRight(host.name, 20);
            String ip = padRight(host.ip, 17);
            String fileCount = padRight(Integer.toString(host.fileCount), 5);
            String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(host.timestamp);
            timeStamp = padRight(timeStamp, 21);

            System.out.print(name);
            System.out.print(ip);
            System.out.print(fileCount);
            System.out.print(timeStamp);
            System.out.println();
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        protocol = new ClientProtocol();
        Map<Host, List<Long>> hosts = new HashMap<>();

        ClientAnnouncer ca = new ClientAnnouncer(protocol, hosts);
        Thread th = new Thread(ca);
        th.start();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("> ");
            String cmd = br.readLine();
            if (cmd.equals("hosts")) {
                printHosts(hosts);
            } else if (cmd.equals("clear")) {
                clearConsole();
            } else if (cmd.startsWith("con")) {
                String[] parts = cmd.split(" ");
                String ip = parts[1];
                currentIp = ip;
                while (true) {
                    System.out.printf("%s > ", ip);
                    String cmd2 = br.readLine();
                    if (cmd2.equals("q")) {
                        break;
                    } else if (cmd2.equals("list")) {
                        cmdList();
                    } else if (cmd2.startsWith("get")) {
                        String name = cmd2.split(" ")[1];
                        cmdGet(name);
                    } else if (cmd2.startsWith("put")) {
                        String name = cmd2.split(" ")[1];
                        cmdPut(name);
                    }
                }
            } else if (cmd.equals("q")) {
                break;
            }
        }
        ca.running = false;
    }
}
