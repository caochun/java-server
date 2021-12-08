package server.reactor.single;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Handler implements Runnable {
    private static final int MAX_IN = 1024;
    private static final int MAX_OUT = 1024;

    private final SocketChannel socket;
    private final SelectionKey sk;
    private ByteBuffer input = ByteBuffer.allocateDirect(MAX_IN);
    private ByteBuffer output = ByteBuffer.allocateDirect(MAX_OUT);

    public Handler(SocketChannel socket, Selector sel) throws IOException {
        this.socket = socket;
        socket.configureBlocking(false);
        sk = socket.register(sel, SelectionKey.OP_READ, this);
        sel.wakeup();
    }

    private void process() {
        input.flip();
        System.out.println(Thread.currentThread().getName() + "------process------");
        byte[] bytes = new byte[input.limit()];
        input.get(bytes);
        String str = new String(bytes);
        System.out.println(str);
        output.put(("received---" + str).getBytes());
        output.flip();
    }

    @Override
    public void run() {
        try {
            read();
        } catch (IOException ignored) {
        }
    }

    void read() throws IOException {
        socket.read(input);
        process();
        sk.attach(new Sender());
        sk.interestOps(SelectionKey.OP_WRITE);
        sk.selector().wakeup();

    }
    private class Sender implements Runnable {
        @Override
        public void run() {
            try {
                socket.write(output);
            } catch (IOException e) {
                e.printStackTrace();
            }
            sk.cancel();

        }
    }
}
