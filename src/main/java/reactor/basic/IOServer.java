package reactor.basic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import reactor.Server;

public class IOServer implements Server {

    public static void main(String[] args) throws Exception {

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Work(socket).start();
            }
        }
    }

    public static class Work extends Thread {

        Socket socket;

        public Work(Socket socket) throws IOException {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                Object obj;
                while ((obj = in.readObject()) != null) {
                    System.out.println(obj);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
