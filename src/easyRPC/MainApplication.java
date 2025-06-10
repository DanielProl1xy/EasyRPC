package easyRPC;
import java.net.ServerSocket;
import java.net.Socket;

import easyRPC.core.EasySerializator;
import easyRPC.test.TestReplicatedObject;

public abstract class MainApplication {
    
    public static void main(String[] args) throws Exception
    {
        EasyRPC sys = EasyRPC.Start(new EasySerializator());
        TestReplicatedObject object = new TestReplicatedObject();
        sys.RegisterObject(object);

        Thread servT = new Thread() {
            @Override
            public void run()
            {
                try {
                    ServerSocket serv = new ServerSocket(5070);
                    Socket cli = serv.accept();
                    sys.Call(cli, TestReplicatedObject.class, "WithParam",
                                                    35.5f, "boolean value is: ", true);
                    sys.Receive(cli);
                    serv.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        
        Thread clientT = new Thread() {
            @Override
            public void run()
            {
                try {
                    Socket sock = new Socket("127.0.0.1", 5070);
                    sys.Call(sock, TestReplicatedObject.class, "Handle");
                    sys.Receive(sock);
                    sys.Receive(sock);
                    sock.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        servT.start();
        clientT.start();
        try {
            servT.join();
            clientT.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}