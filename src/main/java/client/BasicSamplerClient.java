package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import server.Server;

public class BasicSamplerClient extends AbstractJavaSamplerClient {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    @Override
    public void setupTest(JavaSamplerContext context) {
        super.setupTest(context);
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress("localhost", Server.SERVER_PORT));
            if (socket.isConnected()) {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
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
            out.writeObject("Hello from " + Thread.currentThread().getId());
            out.flush();
            Object s;
            if ((s = in.readObject()) != null) {
                result.setResponseData((Thread.currentThread().getId() + " got resposne:" + s.toString()).getBytes());
                result.sampleEnd();
                result.setSuccessful(true);
            }
        } catch (IOException | ClassNotFoundException e) {
            result.sampleEnd();
            result.setSuccessful(false);
        }

        return result;
    }

    public SampleResult getSampleResult() {
        SampleResult result = new SampleResult();
        result.setSampleLabel(getLabel());
        return result;
    }

    public String getLabel() {
        return "BasicSamplerClient" + Thread.currentThread().getId();
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