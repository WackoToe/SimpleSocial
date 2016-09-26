package SimpleSocial;

import java.net.Inet4Address;
import java.net.InetAddress;

/**
 * Created by Paolo on 06/05/2016.
 */
public class Const {

    public final static int SERVERPORT = 9001;
    public final static String IP = "127.0.0.1";

    public final static int MULTICASTPORT = 9002;
    public static final String MULTICASTIP ="225.1.1.1";
    public static final int KEEPALIVERESPONSEPORT = 9003;

    public static final int KEEPALIVETIMERINTERVAL = 10000;
    public static final int KEEPALIVERESPONSETIME = 8000;

    public static final int RMIPORT = 9004;
    public static final String CTSNAME = "clientToServer";

}
