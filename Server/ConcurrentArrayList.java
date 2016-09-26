package Server;

import SimpleSocial.UserInfo;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Paolo on 02/06/2016.
 */

// This class was created to grant an efficient, thread safe access to the users Array List
// Every method who reads from the arrayList, takes a readLock.
// Every method who writes in the arrayList, takes a writeLock
// In this way we can have multiple reading access at the same time on ArrayList, and not more
// than one writing access at the same time
public class ConcurrentArrayList
{
    private ArrayList<UserInfo> users;
    private ReentrantReadWriteLock lock;

    public ConcurrentArrayList()
    {
        this.users = new ArrayList<UserInfo>();
        this.lock = new ReentrantReadWriteLock();
    }

    public void add(UserInfo u)
    {
        this.lock.writeLock().lock();
        this.users.add(u);
        this.lock.writeLock().unlock();
    }

    public UserInfo get(int i)
    {
        UserInfo userFound = null;
        this.lock.readLock().lock();
        userFound = this.users.get(i);
        this.lock.readLock().unlock();
        return userFound;
    }

    public int size()
    {
        int arraySize = -1;
        this.lock.readLock().lock();
        arraySize = this.users.size();
        this.lock.readLock().unlock();
        return arraySize;
    }

    public void remove(int i)
    {
        this.users.remove(i);
    }
}
