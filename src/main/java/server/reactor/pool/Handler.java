package server.reactor.pool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Handler implements Runnable {
    private static final int MAX_IN = 1024;
    private static final int MAX_OUT = 1024;

    private final SocketChannel socket;
    private final SelectionKey sk;
    private ByteBuffer input = ByteBuffer.allocateDirect(MAX_IN);
    private ByteBuffer output = ByteBuffer.allocateDirect(MAX_OUT);

    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(3, 8, 1, TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(1),
            new ThreadFactory() {
                private final AtomicInteger threadNum = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "multi-threads-handler-" + threadNum.getAndIncrement());
                    if (t.isDaemon()) {
                        t.setDaemon(true);
                    }
                    if (t.getPriority() != Thread.NORM_PRIORITY) {
                        t.setPriority(Thread.NORM_PRIORITY);
                    }
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy());

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

    synchronized void read() throws IOException {
        socket.read(input);
        pool.execute(new Processor());

    }

    synchronized void processAndHandOff() {
        process();
        sk.attach(new Sender());
        sk.interestOps(SelectionKey.OP_WRITE);
        sk.selector().wakeup();
    }

    private class Processor implements Runnable {
        @Override
        public void run() {
            processAndHandOff();
        }
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
