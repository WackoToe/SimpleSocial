package Client;
import Server.AcceptThread;
import SimpleSocial.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Paolo on 05/05/2016.
 */
public class SocialClient implements SocialClientInterface
{
    Thread keepAliveThread = null;
    private FileWriter fwUserInfo = null;
    private BufferedWriter bwUserInfo = null;

    private String userName = null;
    private String password = null;
    private String UID = null;
    private Long UIDTime = null;
    public AtomicInteger listeningPort = new AtomicInteger(1);
    private ConcurrentLinkedQueue<String> pendingFriendshipRequest = new ConcurrentLinkedQueue<>();

    private ClientToServerInterface cts = null;
    private ArrayList<String> interestedUser = new ArrayList<>();
    private ConcurrentLinkedQueue<String> pendingMessages = new ConcurrentLinkedQueue<>();

    private ArrayList<Integer> stillIn;

    private ServerToClientInterface lastStc;

    public SocialClient(ArrayList s)
    {
        this.stillIn = s;
    }

    public boolean firstAccess(String code)
    {
        // When the Simplesocial file doesn't exists it's our first access
        File userDataFile = new File("SimpleSocial" + code +".txt");
        return !userDataFile.exists();
    }

    public void incomingConnectionsSetup()
    {
        // Creating incomingConnections Thread, dedicated to handle incoming connections from Social Server
        IncomingConnections incomingObject = new IncomingConnections(pendingFriendshipRequest, listeningPort, this.userName, this.stillIn);
        Thread incomingConnections = new Thread(incomingObject);
        incomingConnections.start();
    }

    public void keepAliveSetup()
    {
        // Creating a thread dedicated to handle keep alive request from Social Server
        KeepAliveClientThread kacThread = new KeepAliveClientThread(this.UID, this.stillIn);
        this.keepAliveThread = new Thread(kacThread);
        this.keepAliveThread.start();
    }

    public void RMISetup()
    {
        // Locating server resource on registry
        try {this.cts = (ClientToServerInterface) LocateRegistry.getRegistry(Const.RMIPORT).lookup(Const.CTSNAME);}
        catch (RemoteException | NotBoundException e) {e.printStackTrace();}
    }

    public void clientSetUp(String code)
    {
        //Creating simple social file
        try {fwUserInfo = new FileWriter("SimpleSocial"+ code +".txt");}
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("An element called SimpleSocial already exists. Delete it to proceed");
        }
        bwUserInfo = new BufferedWriter(fwUserInfo);

        try
        {
            bwUserInfo.write("SimpleSocial file test!");
            bwUserInfo.flush();
        }
        catch (IOException e) {e.printStackTrace();}

        BufferedReader br = null;
        BufferedWriter bw = null;
        boolean regNotOk = true;
        Socket regSocket = null;

        try
        {
            // Start communication with Social server
            regSocket = new Socket(InetAddress.getByName("localhost"), Const.SERVERPORT);
            br = new BufferedReader(new InputStreamReader(regSocket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(regSocket.getOutputStream()));
            String codice = "0\n";
            //Sending operation code
            bw.write(codice);
        }
        catch (IOException e) {e.printStackTrace();}

        String response = null;

        // In this loop, we send our credentials to Social server. Whether
        // user name chosen already exists, we have to insert our
        // credentials again
        while(regNotOk)
        {
            System.out.println("Insert a user name to proceed");
            Scanner keyboard = new Scanner(System.in);
            // Acquiring user name
            this.userName = keyboard.nextLine();
            System.out.println("Insert a password to proceed");
            // Acquiring password
            this.password = keyboard.nextLine();

            try
            {
                // Sending username
                bw.write(userName + "\n");
                // Sending password
                bw.write(password + "\n");
                bw.flush();
                // Waiting for response from Social server
                response = br.readLine();
                System.out.println(response);
            }
            catch (IOException e) {e.printStackTrace();}

            // When user name is correct, we can proceed, otherwise we
            // execute the loop again
            if(response.equals("UserNameOk")) regNotOk = false;
        }

        //Closing Socket
        try {regSocket.close();}
        catch (IOException e) {e.printStackTrace();}
    }

    public void login()
    {
        Socket logSocket = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        boolean logNotOk = true;
        String response = null;

        try
        {
            //Socket setup
            logSocket = new Socket(InetAddress.getByName("localhost"), Const.SERVERPORT);
            br = new BufferedReader(new InputStreamReader(logSocket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(logSocket.getOutputStream()));
            String code = "1\n";
            //Sending operation code
            bw.write(code);
        }
        catch (IOException e) {e.printStackTrace();}

        // In this loop, we send our credentials to Social server. Whether
        // user name and password doesn't match, we have to insert our
        // credentials again
        while (logNotOk)
        {
            System.out.println("Insert your user name to proceed");
            Scanner keyboard = new Scanner(System.in);
            //Acquiring user name
            this.userName = keyboard.nextLine();
            System.out.println("Insert your password to proceed");
            //Acquiring password
            this.password = keyboard.nextLine();
            String listPort = String.valueOf(listeningPort)+ "\n";

            try
            {
                bw.write(userName + "\n");
                bw.write(password + "\n");
                bw.write(listPort);
                bw.flush();
                response = br.readLine();
            }
            catch (IOException e) {e.printStackTrace();}

            // When we receive "UserNameOk" as response, we can move
            // out from loop
            if(response.equals("UserNameOk")) logNotOk = false;
            else System.out.println("Username or password are not corrects");
        }

        System.out.println("Login data are corrects");

        //Waiting for Social server to send our UID
        try {this.UID = br.readLine();}
        catch (IOException e) {e.printStackTrace();}

        //Setting up keep alive thread
        keepAliveSetup();

        UIDTime = System.currentTimeMillis();
        //Closing log in socket
        try {logSocket.close();}
        catch (IOException e) {e.printStackTrace();}

        // Whether the user have done a previous logout without handling his
        // pending friendship request all that friendship request are
        // restored from "pendingClient_***username***.txt" file
        File currentUserFile = new File("pendingClient_" +this.userName+".txt");
        //Checking whether pending friendship client file exists
        if(currentUserFile.exists())
        {
            FileReader currentUserFR = null;
            try {currentUserFR = new FileReader("pendingClient_" +this.userName+".txt");}
            catch (FileNotFoundException e) {e.printStackTrace();}

            BufferedReader currentUserBR = new BufferedReader(currentUserFR);
            String currentLine = null;

            try
            {
                // Acquiring all friendship request
                while( (currentLine=currentUserBR.readLine()) != null)
                {
                    if(!currentLine.isEmpty()) pendingFriendshipRequest.add(currentLine);
                }
            }
            catch (IOException e) {e.printStackTrace();}
        }

    }

    public void friendshipRequest()
    {
        System.out.println("Insert a username you want to find");
        Scanner keyboard = new Scanner(System.in);
        String userNameToFind = keyboard.nextLine() + "\n";
        Socket friendshipSocket = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        String response = null;

        try
        {
            //Socket setup
            friendshipSocket = new Socket(InetAddress.getByName("localhost"), Const.SERVERPORT);
            br = new BufferedReader(new InputStreamReader(friendshipSocket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(friendshipSocket.getOutputStream()));
            String code = "2\n";
            // Sending operation code
            bw.write(code);
            // Sending UID
            bw.write(this.UID + "\n");
            // Sending user's name we have to find
            bw.write(userNameToFind);
            bw.flush();
            //Waiting for response from Social server
            response = br.readLine();
            System.out.println(response);
            bw.close();
            br.close();
            // Closing socket
            friendshipSocket.close();
        }
        catch (IOException e) {e.printStackTrace();}
    }

    public void handleFriendship()
    {
        String currentUsername = null;

        if(pendingFriendshipRequest.size()==0)
        {
            System.out.println("There isn't any pending friendship request");
            return;
        }

        //Printing all pending friendship request
        System.out.println("This are all your pending friendship requests");
        for(int i=0; i<pendingFriendshipRequest.size(); ++i)
        {
            currentUsername = pendingFriendshipRequest.poll();
            System.out.println(currentUsername);
            pendingFriendshipRequest.add(currentUsername);
        }

        System.out.println("Which friend you want to add/ignore?");
        Scanner keyboard = new Scanner(System.in);
        //Acquiring user's name we want to add/ignore
        String requestName = keyboard.nextLine();
        String userFound = null;

        // Looking for the friend we want to add/ignore. When user's name
        // inserted is wrong, variable userFound is null
        for(int i=0; i<pendingFriendshipRequest.size(); ++i)
        {
            currentUsername = pendingFriendshipRequest.poll();
            if(requestName.equals(currentUsername))
            {
                userFound = currentUsername;
            }
            pendingFriendshipRequest.add(currentUsername);
        }

        if(userFound == null)
        {
            System.out.println("You don't have a pending friendship request for that user!");
            return;
        }

        // Acquiring user decision. Whether stringDecision is different from
        // "y" or "n" user have to insert his response again
        Boolean decisionNotOk = true;
        String stringDecision = null;
        while(decisionNotOk)
        {
            System.out.println("Do you want to accept " + userFound + " friendship request?[y/n]");
            stringDecision = keyboard.nextLine();
            if((stringDecision.equals("y")) || (stringDecision.equals("n"))) decisionNotOk = false;
        }

        Socket handleFriendSocket = null;
        BufferedReader br;
        BufferedWriter bw;
        String response;
        try
        {
            //Socket setup
            handleFriendSocket = new Socket(InetAddress.getByName("localhost"), Const.SERVERPORT);
            br = new BufferedReader(new InputStreamReader(handleFriendSocket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(handleFriendSocket.getOutputStream()));
            String code = "3\n";
            //Sending operation code
            bw.write(code);
            //Sending UID
            bw.write(this.UID + "\n");
            //Sending user we want to add ignore
            bw.write(userFound+ "\n");
            //Sending decision
            bw.write(stringDecision + "\n");
            bw.flush();
            //Waiting for Social Server response
            response = br.readLine();

            System.out.println(response);

            // Removing pending friendship request from
            // pendingFriendshipRequest queue
            for(int i=0; i<pendingFriendshipRequest.size(); ++i)
            {
                currentUsername = pendingFriendshipRequest.poll();
                if(!currentUsername.equals(userFound))
                {
                    pendingFriendshipRequest.add(currentUsername);
                }
            }

            br.close();
            bw.close();
            //Closing socket
            handleFriendSocket.close();

        }
        catch (IOException e) {e.printStackTrace();}
    }

    public void friendsListRequest()
    {
        Socket friendsListSocket = null;
        BufferedWriter bw = null;
        BufferedReader br =null;
        Integer friendsNumber = null;

        try
        {
            //Socket setup
            friendsListSocket = new Socket(InetAddress.getByName("localhost"), Const.SERVERPORT);
            br = new BufferedReader(new InputStreamReader(friendsListSocket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(friendsListSocket.getOutputStream()));
            String code = "4\n";
            //Sending operation code
            bw.write(code);
            //Sending UID
            bw.write(this.UID + "\n");
            bw.flush();
            //Receiving friends number
            friendsNumber = Integer.parseInt(br.readLine());

            if(friendsNumber ==0) System.out.println("You don't have friends");
            else
            {
                //Printing every friend user name
                String currentUsername;
                for(int i=0; i<friendsNumber; ++i)
                {
                    currentUsername = br.readLine();
                    System.out.println(currentUsername);
                }
            }

            bw.close();
            br.close();
            //Closing socket
            friendsListSocket.close();
        }
        catch (IOException e) {e.printStackTrace();}
    }

    public void searchFriend()
    {
        System.out.println("Insert a username you want to find");
        Scanner keyboard = new Scanner(System.in);
        String userNameToFind = keyboard.nextLine();

        Socket searchUserSocket = null;
        BufferedWriter bw = null;
        BufferedReader br =null;
        Integer usersNumber = null;

        try
        {
            //Socket setup
            searchUserSocket = new Socket(InetAddress.getByName("localhost"), Const.SERVERPORT);
            br = new BufferedReader(new InputStreamReader(searchUserSocket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(searchUserSocket.getOutputStream()));

            String code = "5\n";
            //Sending operation code
            bw.write(code);
            //Sending user name we want to find
            bw.write(userNameToFind + "\n");
            bw.flush();
            //Receiving how many users Social server has found
            usersNumber = Integer.parseInt(br.readLine());
            if(usersNumber ==0) System.out.println("0 user found");
            else
            {
                String currentUsername;
                for(int i=0; i<usersNumber; ++i)
                {
                    currentUsername = br.readLine();
                    System.out.println(currentUsername);
                }
            }

            bw.close();
            br.close();
            //Closing Socket
            searchUserSocket.close();
        }
        catch (IOException e) {e.printStackTrace();}
    }

    public void sendContents()
    {
        System.out.println("Insert contents you want to publish");
        Scanner keyboard = new Scanner(System.in);
        String contents = keyboard.nextLine();

        Socket contentsSocket = null;
        BufferedWriter bw = null;
        BufferedReader br =null;
        String serverResponse = null;

        try
        {
            //Socket setup
            contentsSocket = new Socket(InetAddress.getByName("localhost"), Const.SERVERPORT);
            br = new BufferedReader(new InputStreamReader(contentsSocket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(contentsSocket.getOutputStream()));

            String code = "6\n";
            //Sending operation code
            bw.write(code);
            //Sending UID
            bw.write(this.UID + "\n");
            //Sending contents we want to publish
            bw.write(contents + "\n");
            bw.flush();

            //Waiting for Social Server response
            serverResponse = br.readLine();

            bw.close();
            br.close();
            //Closing socket
            contentsSocket.close();
        }
        catch (IOException e) {e.printStackTrace();}
    }

    public void registerInterest()
    {
        System.out.println("Insert a user name you want to follow");
        Scanner keyboard = new Scanner(System.in);
        String userNameToFollow = keyboard.nextLine();

        Socket areFriendsSocket = null;
        BufferedWriter bw = null;
        BufferedReader br =null;
        String serverResponse = null;

        // Establishing whether source user and receiver user are friends
        try
        {
            //Socket setup
            areFriendsSocket = new Socket(InetAddress.getByName("localhost"), Const.SERVERPORT);
            br = new BufferedReader(new InputStreamReader(areFriendsSocket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(areFriendsSocket.getOutputStream()));

            String code = "7\n";
            //Sending operation code
            bw.write(code);
            //Sending UID
            bw.write(this.UID+"\n");
            bw.write(userNameToFollow+"\n");
            bw.flush();

            //Waiting for response
            serverResponse = br.readLine();
            bw.close();
            br.close();
            areFriendsSocket.close();
        }
        catch (IOException e) {e.printStackTrace();}

        //When source user and receiver user are friends, we can mark the
        // interest, otherwise we print an error message
        if(serverResponse.equals("y"))
        {
            interestedUser.add(userNameToFollow);

            //Setting up a new stub that we'll send to Social Server via RMI
            ServerToClientInterface stc = new ServerToClientImpl(this.userName, interestedUser, pendingMessages);

            try
            {
                UnicastRemoteObject.exportObject(stc,0);
                // Sending stub to Social server via RMI
                cts.updatePersonalList(stc);
                this.lastStc = stc;
            }
            catch (RemoteException e) {e.printStackTrace();}
            System.out.println("Interest registered!");
        }
        else System.out.println("You and " + userNameToFollow + " are not friends!");
    }

    public void updateContents()
    {
        String currentString;
        //Printing all pending messages
        for(int i=0; i<pendingMessages.size(); ++i)
        {
            currentString = pendingMessages.poll();
            System.out.println(currentString);
        }
    }

    public void logout()
    {
        // Setting stillIn variable to 0. When others thread will check it,
        // they'll terminate their execution
        this.stillIn.set(0, 0);
        FileWriter pendingFW = null;
        BufferedWriter pendingBW = null;

        // Saving any pending friendship request the user haven't manage,
        // writing it on a file
        try
        {
            //Setting up File writer
            pendingFW = new FileWriter("pendingClient_"+this.userName+".txt");
            //Setting up buffered writer
            pendingBW = new BufferedWriter(pendingFW);

            if(this.pendingFriendshipRequest.size() > 0)
            {
                //Writing all pending request on file
                for(int i=0; i<pendingFriendshipRequest.size(); ++i);
                {
                    pendingBW.write(pendingFriendshipRequest.poll()+"\n");
                }
            }
            pendingBW.close();
            pendingFW.close();
        }
        catch (IOException e) {e.printStackTrace();}

        Socket logoutSocket = null;
        BufferedWriter bw = null;
        BufferedReader br =null;
        String serverResponse = null;

        // Telling Social server we are doing logout
        try
        {
            //Socket setup
            logoutSocket = new Socket(InetAddress.getByName("localhost"), Const.SERVERPORT);
            br = new BufferedReader(new InputStreamReader(logoutSocket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(logoutSocket.getOutputStream()));

            String code = "8\n";
            //Sending operation code
            bw.write(code);
            //Sending UID
            bw.write(this.UID+"\n");
            bw.flush();

            //Waiting for response
            serverResponse = br.readLine();
            bw.close();
            br.close();
            logoutSocket.close();
        }
        catch (IOException e) {e.printStackTrace();}

        //Unexporting RMI object
        if(this.lastStc != null)
        {
            try {UnicastRemoteObject.unexportObject(this.lastStc, true);}
            catch (NoSuchObjectException e) {e.printStackTrace();}
            this.lastStc = null;
        }
    }

    public boolean tokenStillValid()
    {
        Long currentTime = System.currentTimeMillis();
        // Check whether token is older than 24 hours
        // (24 hours = 86400000 milliseconds)
        if ( (currentTime - UIDTime)< 86400000) return true;
        else return false;
    }

    public void actionsMenu()
    {
        // Printing action menu
        System.out.println(" ----------------------------------- ");
        System.out.println("|\tChoose your next action\t\t\t|");
        System.out.println("|\t1-Update token\t\t\t\t\t|");
        System.out.println("|\t2-New friendship request\t\t|");
        System.out.println("|\t3-Handle friendship requests\t|");
        System.out.println("|\t4-Friends list request\t\t\t|");
        System.out.println("|\t5-Search friend\t\t\t\t\t|");
        System.out.println("|\t6-Publish something\t\t\t\t|");
        System.out.println("|\t7-Mark interest\t\t\t\t\t|");
        System.out.println("|\t8-Update contents\t\t\t\t|");
        System.out.println("|\t9-Logout\t\t\t\t\t\t|");
        System.out.println(" ----------------------------------- ");
    }

    public void nextAction(Integer op)
    {
        // Selecting next function we have to call using op parameter
        switch(op)
        {
            case 1:
                break;
            case 2:
                friendshipRequest();
                break;
            case 3:
                handleFriendship();
                break;
            case 4:
                friendsListRequest();
                break;
            case 5:
                searchFriend();
                break;
            case 6:
                sendContents();
                break;
            case 7:
                registerInterest();
                break;
            case 8:
                updateContents();
                break;
            case 9:
                logout();
                break;
        }
    }
}