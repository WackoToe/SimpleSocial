package Server;

import Exceptions.WrongLoginException;
import Exceptions.WrongUserNameException;
import SimpleSocial.ClientToServerInterface;
import SimpleSocial.Const;
import SimpleSocial.ServerToClientInterface;
import SimpleSocial.UserInfo;
import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.*;

/**
 * Created by Paolo on 12/05/2016.
 */
public class OperationsThread implements Runnable, OperationsThreadInterface
{
    private Socket currentSocket = null;
    private BufferedReader br;
    private BufferedWriter bw;
    private ConcurrentArrayList allUsers;
    private ConcurrentArrayList onlineUsers;
    private SessionIdentifierGenerator UIDgenerator = new SessionIdentifierGenerator();
    private int listeningPort = -1;
    private ClientToServerInterface cts = null;
    private KeepAliveServerThread kaThread;

    FileWriter fw = null;

    public OperationsThread(Socket s, ConcurrentArrayList au, ConcurrentArrayList ou, ClientToServerInterface c, KeepAliveServerThread k)
    {
        this.currentSocket = s;
        this.allUsers = au;
        this.onlineUsers = ou;
        this.cts = c;
        this.kaThread = k;
    }


    //When run method is called, we already have a socket called
    // "currentSocket". This socket is given by the Accept Thread
    @Override
    public void run()
    {
        Integer currentOp = -1;

        // "userDataFile" contains all the username and password
        try {fw = new FileWriter("userDataFile.txt",true);}
        catch (IOException e) {e.printStackTrace();}

        try
        {
            // Using currentSocket we set up bufferedReader and bufferedWriter
            // to comunicate with Social Client
            br = new BufferedReader(new InputStreamReader(currentSocket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(currentSocket.getOutputStream()));

            // current op will tell us which operation we have to do. Current
            // op is ALWAYS sent by a Social Client, and it must be equal or
            // greater than 0
            currentOp = Integer.parseInt(br.readLine());
        }
        catch (IOException e) {e.printStackTrace();}

        switch (currentOp)
        {
            case -1:
                System.out.println("Some error occurs while receiving login/registration message");
                break;
            case 0:
                System.out.println("Registration case!");
                registration();
                break;
            case 1:
                System.out.println("Login case!");
                login();
                break;
            case 2:
                System.out.println("friendship request case!");
                friendshipRequest();
                break;
            case 3:
                System.out.println("Handle friendship case!");
                handleFriendship();
                break;
            case 4:
                System.out.println("Friends list request case!");
                friendsListRequest();
                break;
            case 5:
                System.out.println("Users search case!");
                userSearch();
                break;
            case 6:
                System.out.println("Contents publish case!");
                publishContents();
                break;
            case 7:
                System.out.println("Are friends? request");
                areFriends();
                break;
            case 8:
                System.out.println("Logout case!");
                logout();
                break;
        }
        try
        {
            br.close();
            bw.close();
            currentSocket.close();
        }
        catch (IOException e) {e.printStackTrace();}
    }

    public void registration()
    {
        String currentUser = null;
        String currentPassword = null;
        boolean regNotOk = true;
        String regResponse = null;

        // When we receive a username and a password we check them. We move
        // outside the loop if and only if username it's not present in
        // allUsers list(we can't have duplicate)
        while(regNotOk)
        {
            try
            {
                //Acquiring userName
                currentUser = br.readLine();
                //Acquiring password
                currentPassword = br.readLine();
            }
            catch (IOException e) {e.printStackTrace();}

            try
            {
                // when the user's name given already exists we throw a
                // WrongUserName Exception
                alreadyExists(currentUser);
                // This line is executed when a WrongUserName IS NOT thrown
                regNotOk = false;
                regResponse = "UserNameOk\n";
            }
            catch (WrongUserNameException e)
            {
                // When a WrongUserNameException is thrown we send a negative response
                e.printStackTrace();
                regResponse = "WrongUserNameException\n";
            }

            try
            {
                //Sending response
                bw.write(regResponse);
                bw.flush();
            }
            catch (IOException e) {e.printStackTrace();}
        }


        try
        {
            //Now that user's name sent is correct, we can add it to allUser list
            allUsers.add(new UserInfo(currentUser, currentPassword, null, -1));

            //We also update the file containing all username and passwords
            fw.write(currentUser);
            fw.write(System.getProperty("line.separator"));
            fw.write(currentPassword);
            fw.write(System.getProperty("line.separator"));
            fw.flush();
            fw.close();
        }
        catch (IOException e) {e.printStackTrace();}
    }

    public void login()
    {
        boolean logNotOk = true;
        String currentUser = null;
        String currentPassword = null;
        String logResponse = null;

        // We stay in the while loop until a correct couple
        // (userName, password) is sent by a Social Client
        while(logNotOk)
        {
            try
            {
                //Reading username
                currentUser = br.readLine();
                //Reading password
                currentPassword = br.readLine();
                listeningPort = Integer.parseInt(br.readLine());
            }
            catch (IOException e) {e.printStackTrace();}

            try
            {
                //When a wrong couple (userName, password) is sent by a social
                // client, a WrongLoginException is thrown
                checkNameAndPassw(currentUser, currentPassword);
                // This line is executed when a WrongLoginException
                // IS NOT thrown
                logNotOk = false;
                logResponse = "UserNameOk\n";
            }
            catch (WrongLoginException e)
            {
                // When a WrongLoginException is thrown we send a
                // negative response
                e.printStackTrace();
                logResponse = "WrongLoginException\n";
            }

            try
            {
                //Sending response
                bw.write(logResponse);
                bw.flush();
            }
            catch (IOException e) {e.printStackTrace();}
        }

        //Generating UID that we'll send to Social client
        String UID = UIDgenerator.nextSessionId();
        UserInfo userToAdd = new UserInfo(currentUser, currentPassword, currentSocket.getInetAddress(), listeningPort);
        userToAdd.setUID(UID);
        onlineUsers.add(userToAdd);

        try
        {
            //Sending UID to Social Client
            bw.write(UID);
            bw.flush();
        }
        catch (IOException e) {e.printStackTrace();}
    }

    public void friendshipRequest()
    {
        String sourceUserUID = null;
        UserInfo sourceUser = null;
        String userNameToFind = null;
        UserInfo userFound = null;
        String reqResponse = null;

        try
        {
            //Getting UID
            sourceUserUID = br.readLine();
            // We identify who sent the request
            sourceUser = sourceRequest(onlineUsers, sourceUserUID);
            //We take its userName
            userNameToFind = br.readLine();
        }
        catch (IOException e) {e.printStackTrace();}

        // We find the user that sourceUser wants to add
        userFound = findUser(onlineUsers, userNameToFind);
        if(userFound == null)
        {
            reqResponse = "The user is not online or doesn't exists";
            System.out.println("The user doesn't exists");
        }
        else
        {
            // We try to send the friendship request. When is sent properly,
            // "sendFriendshipRequest" method returns true, otherwise false.
            if(sendFriendshipRequest(sourceUser, userFound)) {reqResponse = "Friendship request sent";}
            else {reqResponse = "The user is not online";}
        }

        try
        {
            //Sending response to Social client
            bw.write(reqResponse +"\n");
            bw.flush();
        }
        catch (IOException e) {e.printStackTrace();}
    }

    public void handleFriendship()
    {
        String sourceUserUID = null;
        UserInfo sourceUser = null;
        String userNameToFind = null;
        UserInfo userFound = null;
        // friendshipOutcome tells whether the user accept or refuse
        // the friendship request[y/n]
        String friendshipOutcome = null;
        String response = null;

        try
        {
            // Getting the UID
            sourceUserUID = br.readLine();
            // We identify who sent the request
            sourceUser = sourceRequest(onlineUsers, sourceUserUID);
            // We find the user that sourceUser wants to add
            userNameToFind = br.readLine();
            friendshipOutcome = br.readLine();
        }
        catch (IOException e) {e.printStackTrace();}

        userFound = findUser(onlineUsers, userNameToFind);
        if(userFound == null) {response = "That request doesn't exists\n";}
        else
        {
            UserInfo sourceOffline = findUser(allUsers, sourceUser.getUserName());
            UserInfo foundOffline = findUser(allUsers, userNameToFind);
            //When source user decides to accept the friendship request,
            // the friendshipOutcome is "y"
            if(friendshipOutcome.equals("y"))
            {
                //Adding friendship
                sourceOffline.addFriend(userFound.getUserName());
                foundOffline.addFriend(sourceUser.getUserName());
            }
            //Removing pending friendship request
            removePendingFriendshipReq(sourceUser, userFound.getUserName());
            response = "Request handled\n";
        }

        try
        {
            bw.write(response);
            bw.flush();
        }
        catch (IOException e) {e.printStackTrace();}
    }

    public void friendsListRequest()
    {
        String sourceUserUID = null;
        UserInfo sourceUser = null;
        UserInfo sourceUserOffline = null;
        Integer friendsNumber =null;
        ConcurrentLinkedQueue<String> friendsList;
        String currentUsername = null;

        try
        {
            // Getting UID
            sourceUserUID = br.readLine();
            // We identify who sent the request
            sourceUser = sourceRequest(onlineUsers, sourceUserUID);
            sourceUserOffline = findUser(allUsers, sourceUser.getUserName());
        }
        catch (IOException e) {e.printStackTrace();}

        friendsList = sourceUserOffline.getFriendsList();
        friendsNumber = friendsList.size();
        try
        {
            //Sending how many friends sourceUser has
            bw.write(String.valueOf(friendsNumber) + "\n");
            bw.flush();

            for(int i=0; i<friendsNumber; ++i)
            {
                //sending every friend to sourceUser
                currentUsername = friendsList.poll();
                friendsList.add(currentUsername);
                bw.write(currentUsername + "\n");
                bw.flush();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void userSearch()
    {
        String userToFind = null;
        UserInfo currentUser = null;
        ArrayList<String> usersFound = new ArrayList<>();

        //Getting the string we have to find
        try {userToFind = br.readLine();}
        catch (IOException e) {e.printStackTrace();}

        for(int i=0; i<allUsers.size(); ++i)
        {
            currentUser = allUsers.get(i);
            if(currentUser.getUserName().contains(userToFind)) usersFound.add(currentUser.getUserName());
        }

        Integer usersNumber = usersFound.size();
        try
        {
            //Sending how many results we found
            bw.write(String.valueOf(usersNumber) + "\n");
            bw.flush();

            for(int i=0; i<usersNumber; ++i)
            {
                //Sending every result found
                bw.write(usersFound.get(i) + "\n");
                bw.flush();
            }
        }
        catch (IOException e) {e.printStackTrace();}
    }

    public void publishContents()
    {
        String sourceUID = null;
        String contents = null;
        UserInfo sourceUser = null;
        ArrayList<ServerToClientInterface> oul = null;

        try
        {
            //Getting UID
            sourceUID = br.readLine();
            //Getting what sourceUser wrote
            contents = br.readLine();
            // We identify who sent the request
            sourceUser = sourceRequest(onlineUsers, sourceUID);
        }
        catch (IOException e) {e.printStackTrace();}

        try
        {
            //Sending response
            bw.write("Server Response\n");
            bw.flush();
        }
        catch (IOException e) {e.printStackTrace();}

        try
        {
            //Sending what sourceUser wrote to everyone was interested
            // about him
            oul = this.cts.getUsersList();  //oul is the client stub list
            for(int i=0; i<oul.size(); ++i)
            {
                ServerToClientInterface currentUser = oul.get(i);

                //A message cannot be sent to ourselves
                if(!(currentUser.getmyUserName().equals(sourceUser.getUserName())))
                {
                    ArrayList<String> currentMyList = currentUser.getMyList();

                    //When another user X is interested about sourceUser, we
                    // send to X what sourceUser published
                    if(currentMyList.contains(sourceUser.getUserName()))
                    {
                        currentUser.sendContent("User "+ sourceUser.getUserName() +" said: " + contents);
                    }
                }
            }
        }
        catch (RemoteException e) {e.printStackTrace();}
    }

    public void areFriends()
    {
        String sourceUserUID = null;
        String destUsername = null;
        UserInfo sourceUser = null;
        String response = "n";

        try
        {
            //Getting UID
            sourceUserUID = br.readLine();
            destUsername = br.readLine();
            // We identify who sent the request
            sourceUser = sourceRequest(onlineUsers, sourceUserUID);
        }
        catch (IOException e) {e.printStackTrace();}

        //We check whether sourceUser and destUser are friends
        for(int i=0; i<this.allUsers.size(); ++i)
        {
            UserInfo currentUser = allUsers.get(i);
            if(currentUser.getUserName().equals(sourceUser.getUserName()))
            {
                for(int j=0; j<currentUser.getFriendsList().size(); ++j)
                {
                    String currentFriend = currentUser.getFriendsList().poll();
                    currentUser.getFriendsList().add(currentFriend);
                    if(currentFriend.equals(destUsername)) response = "y";
                }
            }
        }

        // When sourceUser and destUser are friends, response is "y",
        // otherwise is "n"
        try
        {
            bw.write(response + "\n");
            bw.flush();
        }
        catch (IOException e) {e.printStackTrace();}
    }

    public void logout()
    {
        String sourceUserUID = null;
        UserInfo sourceUser = null;

        try
        {
            //Getting UID
            sourceUserUID = br.readLine();
            // We identify who sent the request
            sourceUser = sourceRequest(onlineUsers, sourceUserUID);
        }
        catch (IOException e) {e.printStackTrace();}

        //We save all pending friendship request on
        // "pendingServer_**USERNAME**.txt"
        try
        {
            FileWriter pendingFW = new FileWriter("pendingServer_" + sourceUser.getUserName() +".txt");
            BufferedWriter pendingBW = new BufferedWriter(pendingFW);
            ConcurrentLinkedQueue<String> pfr = sourceUser.getPendingFriendshipRequest();
            if(pfr.size() > 0)
            {
                for(int i=0; i<pfr.size(); ++i);
                {
                    pendingBW.write(pfr.poll()+"\n");
                }
            }
            pendingBW.close();
            pendingFW.close();
        }
        catch (IOException e) {e.printStackTrace();}

        //Sending a keep alive to Social client, to unblock his
        // KeepAlive thread
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.schedule(this.kaThread, 0, TimeUnit.MILLISECONDS);
        try
        {
            bw.write("Server Response\n");
            bw.flush();
        }
        catch (IOException e) {e.printStackTrace();}

        // Sending a fictitious friendship request, to unblock his Incoming
        // Connections Thread
        UserInfo logoutUser = new UserInfo("logoutUser", "logoutUser", null, -1);
        sendFriendshipRequest(logoutUser, sourceUser);

        UserInfo currentUser = null;
        for(int i=0; i<onlineUsers.size(); ++i)
        {
            currentUser = onlineUsers.get(i);
            if(currentUser.getUserName().equals(sourceUser.getUserName())) onlineUsers.remove(i);
        }
    }

    public void alreadyExists(String u) throws WrongUserNameException
    {
        int i;
        UserInfo currentUser;
        String currentUsername=null;

        // When the userName sent is "logoutUser" or already exists in
        // allUsers list we throw a WrongUserNameException
        if(u.equals("logoutUser")) throw new WrongUserNameException("The userName " + u + " is not available");
        for(i=0; i<allUsers.size(); ++i)
        {
            currentUser = allUsers.get(i);
            currentUsername = currentUser.getUserName();
            if(currentUsername.equals(u))
            {
                throw new WrongUserNameException("The userName " + u + " already exists");
            }
        }
    }

    public void checkNameAndPassw(String u, String p) throws WrongLoginException
    {
        int i;
        UserInfo currentUser;
        String currentUsername=null;
        String currentPassw = null;

        for(i=0; i<allUsers.size(); ++i)
        {
            currentUser = allUsers.get(i);
            currentUsername = currentUser.getUserName();
            currentPassw = currentUser.getPassword();

            // Checking that username sent exists in the allUsers list
            if(currentUsername.equals(u))
            {
                // Checking whether the password matches
                if (currentPassw.equals(p)) return;
                else throw new WrongLoginException("The password inserted is not correct. The correct was: "+ currentPassw);
            }

        }
        throw new WrongLoginException("The User name inserted is not correct");
    }

    public UserInfo findUser(ConcurrentArrayList cal, String u)
    {
        UserInfo currentUser=null;

        //Looking for a user called "u" in a ConcurrentArrayList
        for(int i=0; i<cal.size(); ++i)
        {
            currentUser = cal.get(i);
            if(currentUser.getUserName().equals(u))
            {
                System.out.println("The target user is " + currentUser.getUserName());
                return currentUser;
            }
        }
        // The user doesn't exists! We return null
        return null;
    }

    public boolean sendFriendshipRequest(UserInfo source, UserInfo dest)
    {
        Socket friendshipSocket = null;
        BufferedWriter bw = null;
        String friendshipMessage = null;

        try
        {
            // Setting up socket
            friendshipSocket = new Socket(dest.getIP(), dest.getListeningPort());
            bw = new BufferedWriter(new OutputStreamWriter(friendshipSocket.getOutputStream()));
            friendshipMessage = source.getUserName();
            //Sending friendship message
            bw.write(friendshipMessage);
            bw.flush();
            //Adding pending friendship request
            dest.addPendingFriendship(source.getUserName());
        }
        catch (ConnectException e)
        {
            //When the user is not online we throw a ConnectException
            System.out.println("Connection refused, the user " + source.getUserName() + " is not online");
            try {bw.close();}
            catch (IOException e1) {e.printStackTrace();}
            // The user is not online, we return false
            return false;
        }
        catch (IOException e) {e.printStackTrace();}

        try
        {
            bw.close();
            friendshipSocket.close();
        }
        catch (IOException e) {e.printStackTrace();}
        // All went good, we can return true
        return true;
    }

    public UserInfo sourceRequest(ConcurrentArrayList cal, String UID)
    {
        UserInfo currentUser = null;

        // Identifying a user having UID "UID" in a ConcurrentArrayList
        for(int i=0; i<cal.size(); ++i)
        {
            currentUser = cal.get(i);
            if(currentUser.getUID().equals(UID))
            {
                return currentUser;
            }
        }
        // We haven't found any user, returning null!
        return null;
    }

    public void removePendingFriendshipReq(UserInfo source,String u)
    {
        UserInfo currentUser = null;
        //Removing pending friendship request
        for(int i=0; i<allUsers.size(); ++i)
        {
            currentUser = allUsers.get(i);

            if(source.equals(currentUser))
            {
                currentUser.removePendingFriendship(u);
            }
        }
    }
}
