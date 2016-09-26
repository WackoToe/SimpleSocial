package SimpleSocial;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Created by Paolo on 03/06/2016.
 */
public interface ClientToServerInterface extends Remote
{
    void updatePersonalList( ServerToClientInterface stc) throws RemoteException;

    ArrayList<ServerToClientInterface> getUsersList() throws RemoteException;
}
