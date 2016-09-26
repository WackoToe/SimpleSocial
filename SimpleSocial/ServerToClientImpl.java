package SimpleSocial;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Paolo on 03/06/2016.
 */
public class ServerToClientImpl implements ServerToClientInterface
{
    //Social client username
    private String myUserName;
    //Friends social client is interested about
    private ArrayList<String> myList;
    //Pending messages list
    private ConcurrentLinkedQueue<String> pendingMessages;

    public ServerToClientImpl(String myun, ArrayList<String> al, ConcurrentLinkedQueue<String> pm)
    {
        this.myUserName = myun;
        this.myList = al;
        this.pendingMessages = pm;
    }

    //Method used by social server to send a message to social client
    public void sendContent(String s)
    {
        pendingMessages.add(s);
    }

    public String getmyUserName() {return this.myUserName;}

    public ArrayList<String> getMyList(){return this.myList;}
}
