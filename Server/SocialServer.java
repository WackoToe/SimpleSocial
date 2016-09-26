package Server;

import SimpleSocial.ClientToServerImpl;
import SimpleSocial.ClientToServerInterface;
import SimpleSocial.Const;
import SimpleSocial.UserInfo;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.*;

/**
 * Created by Paolo on 06/05/2016.
 */
public class SocialServer
{
    private FileReader frUserInfoFile = null;
    private BufferedReader brUserInfoFile = null;
    private FileWriter fwUserInfoFile = null;
    private BufferedWriter bwUserInfoFile = null;

    protected static ConcurrentArrayList allUsers = new ConcurrentArrayList();
    protected static ConcurrentArrayList onlineUser = new ConcurrentArrayList();
    protected static ConcurrentArrayList pendingFriendshipRequests = new ConcurrentArrayList();
    ClientToServerInterface cts = null;
    private KeepAliveServerThread kaThread = null;

    public SocialServer()
    {
    }


    public void serverSetUp() throws IOException
    {
        boolean userInfoFileExists;

        File userDataFile = new File("userDataFile.txt");
        userInfoFileExists = userDataFile.exists();
        System.setProperty("java.net.preferIPv4Stack" , "true");

        // When the User info file doesn't exists we must create it
        if(!userInfoFileExists)
        {
            System.out.println("User info file doesn't exists");
            this.fwUserInfoFile = new FileWriter("userDataFile.txt");
            this.bwUserInfoFile = new BufferedWriter(fwUserInfoFile);
            System.out.println("User info file created");
        }
        else
        {
            this.fwUserInfoFile = new FileWriter("userDataFile.txt", true);
            this.bwUserInfoFile = new BufferedWriter(fwUserInfoFile);
        }

        this.frUserInfoFile = new FileReader("userDataFile.txt");
        this.brUserInfoFile = new BufferedReader(frUserInfoFile);

        /*  Setting up users vector:
        *   userDataFile.txt contains data about users.
        *   odd lines contains userName
        *   even lines contains password
        */
        String currentLine;
        String currentUserName;
        String currentPassword;

        //Acquiring all the users from file
        while((currentLine=brUserInfoFile.readLine()) != null)
        {
            currentUserName = currentLine;
            currentLine=brUserInfoFile.readLine();
            currentPassword = currentLine;

            allUsers.add(new UserInfo(currentUserName, currentPassword, null, -1));
        }

        //Restoring all pending friendship
        for(int i=0; i<allUsers.size(); ++i)
        {
            File currentUserFile = new File("pendingServer_" +allUsers.get(i).getUserName()+".txt");
            if(currentUserFile.exists())
            {
                FileReader currentUserFR = new FileReader("pendingServer_" +allUsers.get(i).getUserName()+".txt");
                BufferedReader currentUserBR = new BufferedReader(currentUserFR);

                while( (currentLine=currentUserBR.readLine()) != null)
                {
                    if(!currentLine.isEmpty()) allUsers.get(i).addPendingFriendship(currentLine);
                }
            }
        }
    }

    //Setting up thread pool
    public void threadPoolSetup()
    {
        AcceptThread acpObject = new AcceptThread(allUsers, onlineUser, pendingFriendshipRequests, this.cts, kaThread);
        Thread acpThread = new Thread(acpObject);
        acpThread.start();
    }

    //Setting up keepAlive
    public void keepAliveServerThreadSetup()
    {
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        kaThread = new KeepAliveServerThread(onlineUser);

        // Every Const.KEEPALIVETIMERINTERVAL millisecond we launch the run
        // method contained in the KeepAliveServerThread class
        ses.scheduleAtFixedRate(kaThread, 10000, Const.KEEPALIVETIMERINTERVAL, TimeUnit.MILLISECONDS);
    }

    //Setting up RMI
    public void RMISetup()
    {
        try{
            cts = (ClientToServerInterface) UnicastRemoteObject.exportObject(new ClientToServerImpl(),0);
            LocateRegistry.createRegistry(Const.RMIPORT);
            Registry registry= LocateRegistry.getRegistry(Const.RMIPORT);
            registry.rebind(Const.CTSNAME, cts);
            System.out.println("Object registered");
        }
        catch(RemoteException e){System.out.println("Server error:" +e.getMessage());}
    }
}
