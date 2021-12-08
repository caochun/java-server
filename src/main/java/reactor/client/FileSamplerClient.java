package reactor.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import reactor.Server;

public class FileSamplerClient extends AbstractJavaSamplerClient {

    private Socket socket;
    private ObjectOutputStream out;
    private InputStreamReader in;

    @Override
    public void setupTest(JavaSamplerContext context) {
        super.setupTest(context);
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress("localhost", Server.SERVER_PORT));
            if (socket.isConnected()) {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new InputStreamReader(socket.getInputStream());
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

            int s;
            String str = "";
            while ((s = in.read()) != -1) {
                str.concat((Character.valueOf((char) s).toString()));
            }

            result.setResponseData((Thread.currentThread().getId() + " got resposne:" + str).getBytes());
            result.sampleEnd();
            result.setSuccessful(true);

        } catch (IOException e) {
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