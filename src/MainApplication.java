import java.net.ServerSocket;
import java.net.Socket;


// Provided as a simple example of usage

public abstract class MainApplication {

    public static RPCHandler testHandler;
    
    public static void main(String[] args)
    {
        
        testHandler = new RPCHandler("test", new IRPCH() {
            @Override
            public void Handle() { System.out.println("Hello, world!"); }
            @Override 
            public void CallBack() { System.out.println("Call back!"); }
        }, true);
        
        EasyRPC sys = EasyRPC.GetInstance();
        sys.RegisterHandler(testHandler);

        Thread serv_t = new Thread() {
            @Override
            public void run()
            {
                try {
                    ServerSocket serv = new ServerSocket(6969);
                    Socket cli = serv.accept();
                    sys.Call(testHandler, cli);
                    sys.Recieve(cli);
                    serv.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        
        Thread cli_t = new Thread() {
            @Override
            public void run()
            {
                try {
                    Socket sock = new Socket("127.0.0.1", 6969);
                    sys.Recieve(sock);
                    sock.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        serv_t.start();
        cli_t.start();
        try {
            serv_t.join();
            cli_t.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}