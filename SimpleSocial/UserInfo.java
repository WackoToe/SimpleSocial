package SimpleSocial;

import java.net.InetAddress;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Paolo on 06/05/2016.
 */
public class UserInfo
{
    private String userName = null;
    private String password = null;
    private InetAddress remoteIP = null;
    private String UID = null;
    private int listeningPort;
    private ConcurrentLinkedQueue<String> pendingFriendshipRequest = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<String> friendsList = new ConcurrentLinkedQueue<>();

    public UserInfo(String u, String p, InetAddress i, int lp)
    {
        this.userName = u;
        this.password = p;
        this.remoteIP = i;
        this.listeningPort = lp;
    }

    // Getters method
    public String getUserName() {return this.userName;}

    public String getPassword() {return this.password;}

    public InetAddress getIP() {return this.remoteIP;}

    public String getUID(){return  this.UID;}

    public void setUID(String s) {this.UID = s;}

    public int getListeningPort() {return this.listeningPort;}

    public ConcurrentLinkedQueue<String> getPendingFriendshipRequest() {return  this.pendingFriendshipRequest;}

    public ConcurrentLinkedQueue<String> getFriendsList() {return this.friendsList;}


    //Setters method
    public void addPendingFriendship(String s) {this.pendingFriendshipRequest.add(s);}

    public void removePendingFriendship(String u)
    {
        String currentUsername = null;
        for(int i=0; i<pendingFriendshipRequest.size(); ++i)
        {
            currentUsername = pendingFriendshipRequest.poll();
            if(!currentUsername.equals(u))
            {
                pendingFriendshipRequest.add(currentUsername);
                return;
            }
        }
        System.out.println("RIMOZIONE PENDING FRIENDSHIP ERRATA!");
    }

    public void addFriend(String s) {this.friendsList.add(s);}
}
