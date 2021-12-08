package server.basic;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import server.Server;

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
        ObjectInputStream in;
        ObjectOutputStream out;

        public Work(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());

                Object obj;
                while ((obj = in.readObject()) != null) {
                    System.out.println(obj);
                    Thread.sleep(1000);
                    out.writeObject("OK");
                    out.flush();
                }
            } catch (EOFException e) {
                try {
                    out.writeObject("DONE");
                    out.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
