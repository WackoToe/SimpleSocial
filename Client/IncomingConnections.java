package Client;

import Server.OperationsThread;
import SimpleSocial.Const;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Paolo on 19/05/2016.
 */
public class IncomingConnections implements Runnable
{
    private ConcurrentLinkedQueue<String> pendingFriendshipRequest;
    private AtomicInteger listeningPort;
    private String username;
    private ArrayList<Integer> stillIn = null;

    public IncomingConnections(ConcurrentLinkedQueue<String> pfr, AtomicInteger lp, String u, ArrayList<Integer> s)
    {
        this.pendingFriendshipRequest = pfr;
        this.listeningPort = lp;
        this.username = u;
        this.stillIn = s;
    }

    @Override
    public void run()
    {
        ServerSocket incomingConnectionsServerSocket = null;
        Socket incomingConnectionsSocket = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        String message = null;

        while(stillIn.get(0).equals(1))
        {
            try
            {
                try
                {
                    if(listeningPort.intValue() == 1)
                    {
                        incomingConnectionsServerSocket = new ServerSocket(0);
                        listeningPort.set(incomingConnectionsServerSocket.getLocalPort());
                    }
                    else
                    {
                        incomingConnectionsServerSocket = new ServerSocket(listeningPort.intValue());
                    }
                }
                catch (BindException e1)
                {
                    e1.printStackTrace();
                    System.out.println("ALREADY BOUNDED!");
                }

                //Waiting for an incoming connection
                incomingConnectionsSocket = incomingConnectionsServerSocket.accept();
                br = new BufferedReader(new InputStreamReader(incomingConnectionsSocket.getInputStream()));
                bw = new BufferedWriter(new OutputStreamWriter(incomingConnectionsSocket.getOutputStream()));

                //Reading user's name who is sending friendship request
                message = br.readLine();
                // When we receive a request from "logoutUser" we ignore it, because tell us
                // that we requested a log out
                if(!(message.equals("logoutUser")))
                {
                    System.out.println("Received request from "+message+" user");
                    pendingFriendshipRequest.add(message);
                }
                br.close();
                bw.close();
                incomingConnectionsSocket.close();
                incomingConnectionsServerSocket.close();
            }
            catch (IOException e) {e.printStackTrace();}
        }
    }
}
