package Main.ControlUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Nick Maiello (aV1rus)
 * Date: 7/4/13
 * Time: 11:59 AM
 */

import Main.Driver;

public class Dispatch {

    private static Dispatcher [] mDispatch;


    public Dispatch()
    {
        mDispatch = new Dispatcher[Driver.numOfProcessors];
        for (int i = 0; i < Driver.numOfProcessors; i++)
        {
            mDispatch[i] = new Dispatcher(i);
        }
    }

    public static Dispatcher getDispatch(int i)
    {
        return mDispatch[i];
    }
}

