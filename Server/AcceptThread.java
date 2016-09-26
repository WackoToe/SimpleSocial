package Server;

import SimpleSocial.ClientToServerInterface;
import SimpleSocial.Const;
import SimpleSocial.UserInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Paolo on 12/05/2016.
 */
public class AcceptThread implements Runnable
{
    private ConcurrentArrayList allUsers;
    private ConcurrentArrayList onlineUser;
    private ConcurrentArrayList pendingFriendshipRequests;
    private ClientToServerInterface cts = null;
    private KeepAliveServerThread kaThread = null;

    public AcceptThread(ConcurrentArrayList au, ConcurrentArrayList ou, ConcurrentArrayList pfr, ClientToServerInterface c, KeepAliveServerThread k)
    {
        this.allUsers = au;
        this.onlineUser = ou;
        this.pendingFriendshipRequests = pfr;
        this.cts = c;
        this.kaThread = k;
    }

    @Override
    public void run()
    {
        ServerSocket currentServerSocket = null;
        ExecutorService executor = Executors.newFixedThreadPool(8);

        while(true)
        {
            try
            {
                //Thread is waiting for connections
                System.out.println("Waiting for connections " + Thread.currentThread().getName());
                currentServerSocket = new ServerSocket(Const.SERVERPORT);
                Socket socketForWorker = currentServerSocket.accept();

                //When it receives a request, creates a new task for the Social
                // Server Thread Pool
                System.out.println("Request received!");
                Runnable worker = new OperationsThread(socketForWorker, allUsers, onlineUser, this.cts, kaThread);
                executor.execute(worker);

                currentServerSocket.close();
            }
            catch (IOException e) {e.printStackTrace();}
        }
    }
}
