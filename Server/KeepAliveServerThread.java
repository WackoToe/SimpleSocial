package Server;

import SimpleSocial.Const;
import SimpleSocial.UserInfo;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Paolo on 30/05/2016.
 */
public class KeepAliveServerThread implements Runnable
{
    private ConcurrentArrayList onlineUsers;
    private DatagramSocket responseSocket = null;

    public KeepAliveServerThread(ConcurrentArrayList ou)
    {
        try {responseSocket = new DatagramSocket(Const.KEEPALIVERESPONSEPORT);}
        catch (SocketException e) {e.printStackTrace();}
        this.onlineUsers = ou;
    }

    // This method is called by a ScheduledExecutorService every Const.KEEPALIVETIMERINTERVAL milliseconds
    @Override
    public void run()
    {
        System.out.println("Nuovo keepalive! yeah!");
        try(MulticastSocket server = new MulticastSocket(Const.MULTICASTPORT))
        {
            //Setting up Keep Alive socket
            server.setTimeToLive(16);
            server.setLoopbackMode(false);
            server.setReuseAddress(true);
            InetAddress multicastGroup = InetAddress.getByName(Const.MULTICASTIP);

            ByteArrayOutputStream byteStream= new ByteArrayOutputStream();
            DataOutputStream out= new DataOutputStream(byteStream);
            out.writeUTF("Keep alive server message");

            byte[] data= byteStream.toByteArray();
            DatagramPacket packet= new DatagramPacket(data, data.length, multicastGroup, Const.MULTICASTPORT);
            server.send(packet);
            byteStream.close();
            out.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        //After we send our keep alive packet, we have to wait for Const.KEEPALIVERESPONSETIME milliseconds
        Long prevTime = System.currentTimeMillis();
        Long currentTime = null;

        //At the beginning remaing time is Const.KEEPALIVERESPONSETIME
        int remainingTime = Const.KEEPALIVERESPONSETIME;
        ConcurrentArrayList updatedOnlineUsers = new ConcurrentArrayList();
        UserInfo currentUser = null;

        // If we still have time, we wait for "remainingTime" milliseconds
        while(remainingTime > 0)
        {
            System.out.println("NEL WHILE: REMAINING TIME IS: " + remainingTime);
            try
            {
                responseSocket.setSoTimeout(remainingTime);
            }
            catch (SocketException e) {e.printStackTrace();}

            byte[] responseBuffer = new byte[50];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
            String responseString = null;

            try
            {
                responseSocket.receive(responsePacket);
                responseString = new String(responsePacket.getData(),0,responsePacket.getLength(),"US-ASCII");
                for(int i=0; i<onlineUsers.size(); ++i)
                {
                    //Every client responding to Keep Alive is insert in the updateOnlineUser list
                    currentUser = onlineUsers.get(i);
                    if(responseString.equals(currentUser.getUID()))
                    {
                        updatedOnlineUsers.add(currentUser);
                    }
                }
            }
            catch (IOException e) {}

            currentTime = System.currentTimeMillis();
            remainingTime -= (int)(currentTime-prevTime);
            prevTime = System.currentTimeMillis();

        }

        // Overwriting old online user list
        onlineUsers = updatedOnlineUsers;
    }
}
