package Server;

import Exceptions.WrongLoginException;
import Exceptions.WrongUserNameException;
import SimpleSocial.UserInfo;

/**
 * Created by Paolo on 02/06/2016.
 */
public interface OperationsThreadInterface
{
    /**
     *  Register a new user
     *
     *  REQUIRES:   /
     *  MODIFIES:   the registered users list
     *  EFFECTS:    the registered users list is updated
     */
    void registration();

    /**
     *  When some user inserts correct credentials, it's considered online
     *
     *  REQUIRES:   /
     *  MODIFIES:   the online user list
     *  EFFECTS:    the user is now logged in
     */
    void login();

    /**
     *  Social server manages the user's request, sending a friendship request
     *  to an existing user.
     *
     *  REQUIRES:   receiver user must exists
     *  MODIFIES:   receiver's pending friendship request
     *  EFFECTS:    receiver now knows that it has a friendship request
     */
    void friendshipRequest();

    /**
     *  Social server manages a user's pending friendship request from user B
     *  to user A. Whether A decides to accept B's request, from now on, A
     *  and B are friends
     *
     *  REQUIRES:   the pending friendship request must exists
     *  MODIFIES:   A's pending friendship request, A's friends list, B's friends list
     *  EFFECTS:    A and B are now friends
     */
    void handleFriendship();


    /**
     *  Social server sends to a user all his friends
     *
     *  REQUIRES:   /
     *  MODIFIES:   /
     *  EFFECTS:    user's friends list is correctly sent
     */
    void friendsListRequest();


    /**
     *  Social server finds all the userName containing a String sent by
     *  Social Client
     *
     *  REQUIRES:   /
     *  MODIFIES:   /
     *  EFFECTS:    all users found are correctly sent
     */
    void userSearch();


    /**
     *  When the A user publish something, Social server finds all the
     *  users who follows A. Then Social server sends A's publication to
     *  them via callback method. All the followers add the publication
     *  in their pending messages list
     *
     *  REQUIRES:   followers's stub exist
     *  MODIFIES:   /
     *  EFFECTS:    all the follower receive A's publication properly
     */
    void publishContents();


    /**
     *  Provides logout
     *
     *  REQUIRES:   user must be logged
     *  MODIFIES:   /
     *  EFFECTS:    the user is removed from the online lists
     */
    void logout();


    /**
     *  Tells whether a username already exists
     *
     *  REQUIRES:   /
     *  MODIFIES:   /
     *  EFFECTS:    a user name is found
     *  @param:     u: the username we have to check
     */
    void alreadyExists(String u) throws WrongUserNameException;


    /**
     *  Checks whether login data (userName and password) are corrects
     *
     *  REQUIRES:   /
     *  MODIFIES:   /
     *  EFFECTS:    username and password are checked
     *  @param:     u: username we have to check
     *              p: password we have to check
     */
    void checkNameAndPassw(String u, String p) throws WrongLoginException;


    /**
     *  Find a user in a UserInfo list
     *
     *  REQUIRES:   find user != null
     *  MODIFIES:   /
     *  EFFECTS:    the next action is correctly selected
     *  @param:     cal: the UserInfo's list
     *              u: the userName we have to find
     *  @return:    the Userinfo found if exists, null otherwise
     */
    UserInfo findUser(ConcurrentArrayList cal, String u);


    /**
     *  Social server sends a friendship request to dest user
     *
     *  REQUIRES:   user must be logged && source != null && dest != null
     *  MODIFIES:   /
     *  EFFECTS:    friendship request is sent
     *  @param:     source: user who's sending friendship request
     *              dest: user who receive the friendship request
     *  @return:    true when the friendship request is sent properly.
     *              False otherwise
     */
    boolean sendFriendshipRequest(UserInfo source, UserInfo dest);


    /**
     *  Social server finds who sent a request using the UID received
     *
     *  REQUIRES:   user must be logged
     *  MODIFIES:   /
     *  EFFECTS:    source user is found, when exists
     *  @param:     cal: list where we have to find the source user
     *              UID: source's UID
     *  @return:    source user info. null otherwise
     */
    UserInfo sourceRequest(ConcurrentArrayList cal, String UID);


    /**
     *  Removes a pending friendship request
     *
     *  REQUIRES:   user must be logged
     *  MODIFIES:   pending friendship request
     *  EFFECTS:    the pending friendship request is updated
     *  @param:     source: the user who has the list we have to update
     *              u: the name we have to remove from the list
     */
    void removePendingFriendshipReq(UserInfo source,String u);
}
