package Server;

import java.io.IOException;

/**
 * Created by Paolo on 06/05/2016.
 */
public class ServerMain
{
    public static void main(String[] args)
    {
        SocialServer s1 = new SocialServer();

        //Server setup
        try {s1.serverSetUp();}
        catch (IOException e) {e.printStackTrace();}

        //Setting up the keep alive thread
        s1.keepAliveServerThreadSetup();
        // Setting up RMI
        s1.RMISetup();
        // Setting up threads for incoming connections
        s1.threadPoolSetup();
    }
}
