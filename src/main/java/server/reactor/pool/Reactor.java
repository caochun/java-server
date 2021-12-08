package server.reactor.pool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class Reactor implements Runnable {
    private final Selector selector;
    private final ServerSocketChannel serverSocket;

    public Reactor(int port) throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind( new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        // Multiplexing IO will wakeup this, class acceptor will run.
        serverSocket.register(selector, SelectionKey.OP_ACCEPT, new Acceptor());
    }

    /**
     * Alternatively, use explicit SPI provider:
     * SelectorProvider p = SelectorProvider.provider();
     * selector = p.openSelector();
     * serverSocket = p.openServerSocketChannel();
     */
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
        public void run() {
            try {
                SocketChannel c = serverSocket.accept();
                if (c != null) {
                    new Handler(c, selector);
                }
            } catch (IOException ignored) {

            }
        }
    }
}
