package reactor.basic;

import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import reactor.Server;

public class FileServer implements Server {

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
        OutputStreamWriter out;

        public Work(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new ObjectInputStream(socket.getInputStream());
                out = new OutputStreamWriter(socket.getOutputStream());

                if (in.readObject() != null) {
                    System.out.println("sending file");
                    FileReader fr = new FileReader("char_table.txt");
                    int i;
                    while ((i = fr.read()) != -1) {
                        out.write((char) i);
                    }
                    fr.close();

                }

                out.flush();

            } catch (EOFException e) {
                try {
                    out.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
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
