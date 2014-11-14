import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ServerWorker implements Runnable {
    ServerProtocol protocol;
    Socket socket;

    public ServerWorker(ServerProtocol protocol, Socket socket) {
        this.protocol = protocol;
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("Accepted: " + socket.getInetAddress());

        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            Parser parser = new Parser(in, 1024);

            byte[] code = parser.get(1);

            switch (code[0]) {
                case 0x1:
                    System.out.println("list");
                    out.write(protocol.list());
                    break;
                case 0x2:
                    System.out.println("get");
                    byte[] rawName = parser.getToZero();
                    try {
                        String name = new String(Arrays.copyOf(rawName, rawName.length - 1), "UTF-8");
                        System.out.println("serverside: get " + name);
                        protocol.get(name, out);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case 0x3:
                    System.out.println("put");
                    protocol.parsePut(parser);
            }

            socket.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
