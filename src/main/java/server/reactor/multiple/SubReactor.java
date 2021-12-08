package server.reactor.multiple;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class SubReactor implements Runnable {
    private final SocketChannel socket;
    private final Selector selector;

    public SubReactor(SocketChannel socket, Selector sel) throws IOException {
        this.socket = socket;
        this.selector = sel;
        socket.configureBlocking(false);
        // register read io event and interested.
        socket.register(sel, SelectionKey.OP_READ, new Handler(this.socket, sel));
        // wakeup all threads which interesting this io event.
        // sel.wakeup();
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                selector.select();
                // Multiplexing IO events set.
                Set<SelectionKey> selected = selector.selectedKeys();
                for (SelectionKey key : selected) {
                    Runnable handler = (Runnable) key.attachment();
                    if (handler != null) {
                        handler.run();
                    }
                }
                selected.clear();
            }
        } catch (IOException ignored){

        }
    }
}
