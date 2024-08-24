package easyRPC;
import java.net.ServerSocket;
import java.net.Socket;

import easyRPC.test.TestReplicatedObject;

public abstract class MainApplication {
    
    public static void main(String[] args) throws Exception
    {
        EasyRPC sys = EasyRPC.GetInstance();
        sys.RegisterClass(TestReplicatedObject.class);

        Thread servT = new Thread() {
            @Override
            public void run()
            {
                try {
                    ServerSocket serv = new ServerSocket(5070);
                    Socket cli = serv.accept();
                    sys.Call(cli, "Handle");
                    sys.Call(cli, "WithParam",1L, 5L);
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