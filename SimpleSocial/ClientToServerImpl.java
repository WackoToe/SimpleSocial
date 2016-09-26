package SimpleSocial;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Paolo on 03/06/2016.
 */
public class ClientToServerImpl implements ClientToServerInterface
{
    //HashMap containing all the user stub Social Server received from Clients
    private HashMap<String,ServerToClientInterface> onlineUsersList = new HashMap<>();

    // updatePersonalList method is called by a Social client to communicate to
    // Social Server that he is interested about another user. In fact, stc
    // parameter is a Social client stub that Social server can use to
    // communicate with social client
    @Override
    public synchronized void updatePersonalList(ServerToClientInterface stc)
    {
        try
        {
            String sourceUserName = stc.getmyUserName();
            onlineUsersList.put(sourceUserName,stc);
        }
        catch (RemoteException e) {e.printStackTrace();}
    }

    public synchronized ArrayList<ServerToClientInterface> getUsersList()
    {
        return new ArrayList<>(this.onlineUsersList.values());
    }
}
