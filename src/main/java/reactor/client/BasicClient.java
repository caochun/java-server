package reactor.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import reactor.Server;

public class BasicClient extends AbstractJavaSamplerClient {

    private Socket socket;
    private ObjectOutputStream out;

    @Override
    public void setupTest(JavaSamplerContext context) {
        super.setupTest(context);
        try {
            socket = new Socket();
            socket.setKeepAlive(true);
            socket.connect(new InetSocketAddress("localhost", Server.SERVER_PORT));
            if (socket.isConnected()) {
                out = new ObjectOutputStream(socket.getOutputStream());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SampleResult runTest(JavaSamplerContext arg0) {
        SampleResult result = getSampleResult();
        result.sampleStart();

        try {
            out.writeObject(Thread.currentThread().getId() + ":Hello JavaSamplerClient!");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        result.setResponseData((Thread.currentThread().getId() + ":success!").getBytes());
        result.sampleEnd();
        result.setSuccessful(true);
        return result;
    }

    public SampleResult getSampleResult() {
        SampleResult result = new SampleResult();
        result.setSampleLabel(getLabel());
        return result;
    }

    public String getLabel() {
        return "TCPSampler" + Thread.currentThread().getId();
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        try {
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("teardownTest:" + Thread.currentThread().getId());

        super.teardownTest(context);
    }

    

}