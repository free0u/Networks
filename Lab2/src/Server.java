import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerProtocol p = new ServerProtocol();
        Thread th = new Thread(new ServerAnnouncer(p));
        th.start();

        ServerSocket serverSocket = new ServerSocket(7777);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new ServerWorker(p, clientSocket)).start();
        }
    }
}
