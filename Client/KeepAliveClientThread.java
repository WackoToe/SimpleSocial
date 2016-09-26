package Client;

import SimpleSocial.Const;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Paolo on 30/05/2016.
 */
public class KeepAliveClientThread implements Runnable {
    private String UID;
    private ArrayList<Integer> stillIn = null;
    MulticastSocket client = null;
    InetAddress multicastGroup = null;

    public KeepAliveClientThread(String id, ArrayList<Integer> s)
    {
        this.UID = id;
        this.stillIn = s;
        try
        {
            this.client = new MulticastSocket(Const.MULTICASTPORT);
            this.multicastGroup=InetAddress.getByName(Const.MULTICASTIP);
            client.joinGroup(multicastGroup);
        }
        catch (IOException e) {e.printStackTrace();}
    }

    @Override
    public void run()
    {
        try
        {
            while(this.stillIn.get(0).equals(1))
            {
                //Receiving Keep Alive from server
                DatagramPacket packet= new DatagramPacket(new byte[512], 512);
                client.receive(packet);
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(packet.getData(),packet.getOffset(),packet.getLength()));

                //Sending response to server, creating a new Datagram Socket
                String myUID = this.UID;
                DatagramSocket responseSocket = new DatagramSocket();
                byte[] bufferSent =myUID.getBytes("US-ASCII");
                DatagramPacket responsePacket = new DatagramPacket(bufferSent, bufferSent.length, packet.getAddress(), Const.KEEPALIVERESPONSEPORT);
                responseSocket.send(responsePacket);

                in.close();
                responseSocket.close();
            }
            client.close();
        }
        catch (IOException e) {e.printStackTrace();}
    }
}
