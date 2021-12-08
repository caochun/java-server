package server.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import server.Server;

public class NIOServer {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(Server.SERVER_PORT));

        while (true) {
            SocketChannel socketChannel = serverSocket.accept();
            new Work(socketChannel).start();
        }
    }

    public static class Work extends Thread {

        SocketChannel socketChannel;

        public Work(SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;
        }

        @Override
        public void run() {

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            try {
                if (socketChannel.read(buffer) != -1) {
                    buffer.clear();
                    buffer.put("ok".getBytes());
                    buffer.flip();
                    socketChannel.write(buffer);
                    socketChannel.socket().close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
