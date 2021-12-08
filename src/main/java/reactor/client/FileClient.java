package reactor.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import reactor.Server;

public class FileClient {

    public static void main(String[] args) {
        ObjectOutputStream out;
        InputStreamReader in;

        try (Socket socket = new Socket();) {
            socket.connect(new InetSocketAddress("localhost", Server.SERVER_PORT));
            if (socket.isConnected()) {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new InputStreamReader(socket.getInputStream());

                out.writeObject("Hello!");
                out.flush();
                int s;
                while ((s = in.read()) != -1) {
                    System.out.print((char) s);
                }
                out.close();
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
