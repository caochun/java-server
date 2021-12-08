package server.reactor.multiple;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MainReactor implements Runnable {


    private final Selector selector;
    private final ServerSocketChannel serverSocket;
    
    private static final int cores = Runtime.getRuntime().availableProcessors();

    private static ThreadPoolExecutor pool =
            new ThreadPoolExecutor(3, cores, 1, TimeUnit.MINUTES,
                    new SynchronousQueue<>(),
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

    private final Selector[] selectors;
    private int next = 0;

    public MainReactor(int port) throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        // Multiplexing IO will wakeup this, class acceptor will run.
        serverSocket.register(selector, SelectionKey.OP_ACCEPT, new Acceptor());

        selectors = new Selector[cores];
        for (int i = 0; i < cores; i++) {
            selectors[i] = Selector.open();
        }
    }


    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                selector.select();
                // Multiplexing IO events set.
                Set<SelectionKey> selected = selector.selectedKeys();
                for (SelectionKey o : selected) {
                    dispatch(o);
                }
                selected.clear();
            }
        } catch (IOException ignored){

        }
    }

    void dispatch(SelectionKey k) {
        Runnable r = (Runnable) k.attachment();
        if (r != null) {
            r.run();
        }
    }

    private class Acceptor implements Runnable {
        @Override
        public synchronized void run() {
            try {
                SocketChannel c = serverSocket.accept();
                if (c != null) {
                    SubReactor subReactor = new SubReactor(c, selectors[next]);
                    pool.execute(subReactor);
                }
                if (++next == selectors.length) {
                    next = 0;
                }
            } catch (IOException ignored) {

            }
        }
    }
}
