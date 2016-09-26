package Client;

/**
 * Created by Paolo on 16/05/2016.
 */
public interface SocialClientInterface
{
    /**
     *  REQUIRES:   code != null
     *  MODIFIES:   /
     *  EFFECTS:    tries to find "SimpleSocial" file.
     *  @param:     code: TESTING PARAMETER! is used to simulate different
     *              client accessing the service simultaneously
     *  @return:    true when the "SimpleSocial" file is not found,
     *              indicating that is clients's first access
     *              false when the "SimpleSocial" file is found
     */
    boolean firstAccess(String code);


    /**
     *  Creates a new thread, dedicated to incoming connections handling, by
     *  creating an IncomingConnections object.
     *
     *  REQUIRES:   /
     *  MODIFIES:   /
     *  EFFECTS:    the incoming connections thread is created
     *  @param:     t: thread used to handle incoming connections
     */
    void incomingConnectionsSetup();


    /**
     *  Creates a new thread that responds to keep alive server message
     *
     *  REQUIRES:   /
     *  MODIFIES:   /
     *  EFFECTS:    keep alive response thread is created
     *  @param:     t: thread used to set up keep alive
     */
    void keepAliveSetup();


    /**
     *  Locate the server registry and takes the reference to the
     *  ClientToServer object
     *
     *  REQUIRES:   /
     *  MODIFIES:   client To server object
     *  EFFECTS:    the client to server object is ready to use
     */
    void RMISetup();


    /**
     *  Start a communication with the server using TCP on a socket.
     *  Send to the server the user credentials. When the username
     *  sent already exists, ask again new credentials
     *
     *  REQUIRES:   code != null AND previous firstAccess call returned true
     *  MODIFIES:   userName, password
     *  EFFECTS:    the user is registered
     *  @param:     code: used to create the "SimpleSocial" file
     */
    void clientSetUp(String code);


    /**
     *  Provides login services. The user insert his userName and
     *  password. They're both sent to the server. When the username
     *  doesn't exists or the password is incorrect, this method ask
     *  again userName and password. At last, modifies the UID token
     *  and set the UIDTime related to the UID
     *
     *  REQUIRES:   /
     *  MODIFIES:   UID, UIDTime
     *  EFFECTS:    the user is logged in
     */
    void login();


    /**
     *  Send to Simple Social server the necessary data to make a
     *  friendship request. Simple social client doesn't wait for any
     *  positive response. Simple social server just sends negative
     *  response if necessary( like "The user is not online or doesn't
     *  exists")
     *
     *  REQUIRES:   user must be logged
     *  MODIFIES:   /
     *  EFFECTS:    the user friendship request is sent
     */
    void friendshipRequest();


    /**
     *  Shows the user's pending friendship request. Then the user decides
     *  which friend to handle and an outcome. With a positive outcome,
     *  the other user is added as a friend. With a negative outcome the
     *  other user's request is ignored
     *
     *  REQUIRES:   user must be logged
     *  MODIFIES:   pending friendship request list
     *  EFFECTS:    the user's incoming friendship request is handled
     */
    void handleFriendship();


    /**
     *  Asks to simple social server the user's friend list and prints it
     *
     *  REQUIRES:   user must be logged
     *  MODIFIES:   /
     *  EFFECTS:    the user's friends list is printed
     */
    void friendsListRequest();

    /**
     *  Social client sends to Social server a String s. Every username
     *  containing s is sent to Social client and printed
     *
     *  REQUIRES:   user must be logged
     *  MODIFIES:   /
     *  EFFECTS:    all the Username containg s, are printed
     */
    void searchFriend();


    /**
     *  Social client sends a message to social server, containing what Social
     *  Client wants to share with its followers
     *
     *  REQUIRES:   user must be logged
     *  MODIFIES:   /
     *  EFFECTS:    all the Username containg s, are printed
     */
    void sendContents();


    /**
     *  Social clients prints on screen all the messages that were pending
     *
     *  REQUIRES:   user must be logged
     *  MODIFIES:   the pending message list
     *  EFFECTS:    all the pending message are printed
     */
    void updateContents();


    /**
     *  Log out the Social client who calls it, saving the pending friendship
     *  request in a file called "*username*_pending.txt"
     *
     *  REQUIRES:   user must be logged
     *  MODIFIES:   the pending friendship list
     *  EFFECTS:    the user is logged out, and all the pending friendship are saved
     */
    void logout();


    /**
     *  Social client sends a stub to Social server containing all the server
     *  needs to update Social Client about users that Social client follow
     *
     *  REQUIRES:   user must be logged
     *  MODIFIES:   user stub
     *  EFFECTS:    the users's list followed by social client is updated
     */
    void registerInterest();


    /**
     *  Check whether the client's UID is still valid
     *
     *  REQUIRES:   /
     *  MODIFIES:   /
     *  EFFECTS:    the UID is checked
     *  @return:    the boolean returned tells whether the token is still valid
     */
    boolean tokenStillValid();

    /**
     *  Print on screen the user's choice menu, with all the possible actions
     *
     *  REQUIRES:   a previous login was completed successfully
     *  MODIFIES:   /
     *  EFFECTS:    the menu is printed
     */
    void actionsMenu();


    /**
     *  Select the correct function that must be executed next
     *
     *  REQUIRES:   a previous login was completed successfully
     *  MODIFIES:   /
     *  EFFECTS:    the next action is correctly selected
     *  @param:     op: an integer telling the next function to call
     */
    void nextAction(Integer op);
}
