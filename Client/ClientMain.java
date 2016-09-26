package Client;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Paolo on 05/05/2016.
 */
public class ClientMain
{
    public static void main(String[] args)
    {
        ArrayList<Integer> stillIn = new ArrayList<>();
        stillIn.add(1);
        SocialClient c1 = new SocialClient(stillIn);
        Integer nextCode = -1;

        System.setProperty("java.net.preferIPv4Stack" , "true");
        System.out.println("Insert your code");
        Scanner keyboard = new Scanner(System.in);
        //Acquiring code
        String stringCode = keyboard.nextLine();

        //Setting up incoming connections thread
        c1.incomingConnectionsSetup();
        //Setting up RMI
        c1.RMISetup();

        //Checking whether is user first access
        if(c1.firstAccess(stringCode)) c1.clientSetUp(stringCode);
        else System.out.println("File already exists");

        System.out.println("Now you have to log in");
        c1.login();

        // While stillIn value is equals to 1, we ask to the user to
        // make a new action.When user wants to log out, stillIn
        // value is changed to 0 by logout function contained in
        // SocialClient class
        while(stillIn.get(0).equals(1))
        {
            //Checking whether our token is still valid, otherwise we have to
            // log in again!
            if(!c1.tokenStillValid())
            {
                System.out.println("Token time expired");
                c1.login();
            }
            //Prints all the possible actions
            c1.actionsMenu();
            //User inserts his next action
            nextCode = keyboard.nextInt();
            //Selecting next action
            c1.nextAction(nextCode);
        }
    }
}