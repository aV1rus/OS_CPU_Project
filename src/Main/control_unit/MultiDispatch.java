package Main.control_unit;

/**
 * Created with IntelliJ IDEA.
 * User: aV1rus
 * Date: 7/4/13
 * Time: 11:59 AM
 */

import Main.driver.Driver;

public class MultiDispatch {

    private static Dispatcher [] multiDisp;


    public MultiDispatch()
    {
        multiDisp = new Dispatcher[Driver.numOfProcessors];
        for (int i = 0; i < Driver.numOfProcessors; i++)
        {
            multiDisp[i] = new Dispatcher(i);
        }
    }

    public static Dispatcher getDispatch(int i)
    {
        return multiDisp[i];
    }
}

