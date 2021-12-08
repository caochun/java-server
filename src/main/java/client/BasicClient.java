package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import server.Server;

public class BasicClient {

    public static void main(String[] args) {
        ObjectOutputStream out;
        ObjectInputStream in;

        try (Socket socket = new Socket();) {
            socket.connect(new InetSocketAddress("localhost", Server.SERVER_PORT));
            if (socket.isConnected()) {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                out.writeObject("Hello!");
                out.flush();
                Object s;
                if ((s = in.readObject()) != null) {
                    System.out.print(s);
                }
                out.close();
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
