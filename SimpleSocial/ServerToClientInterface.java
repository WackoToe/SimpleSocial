package SimpleSocial;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Created by Paolo on 03/06/2016.
 */
public interface ServerToClientInterface extends Remote
{
    void sendContent(String s) throws RemoteException;

    String getmyUserName() throws RemoteException;

    ArrayList<String> getMyList() throws RemoteException;
}
