package Main.ControlUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Nick Maiello (aV1rus)
 * Date: 7/10/13
 * Time: 11:59 AM
 *
 *
 * Enable MultiCore Processing
 *
 *
 *
 */

import Main.Driver;
import static Main.ConfigFiles.Config.*;

public class Dispatch {

    private static Dispatcher [] mDispatch;


    public Dispatch()
    {
        mDispatch = new Dispatcher[NUM_OF_PROCESSOR];
        for (int i = 0; i < NUM_OF_PROCESSOR; i++)
        {
            mDispatch[i] = new Dispatcher(i);
        }
    }

    public static Dispatcher getDispatch(int i)
    {
        return mDispatch[i];
    }
}

