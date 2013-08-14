package Main.Memory;

/**
 * Created with IntelliJ IDEA.
 * User: Nick Maiello (aV1rus)
 * Date: 7/2/13
 * Time: 10:46 PM
 * To change this template use File | Settings | File Templates.
 */
import Main.Driver;
import Main.Log.ErrorLog;
import static Main.ConfigFiles.Config.*;

public class RAM
{
    private String [] mRamArray;
    private int mNextLocation;
    private static RAM mRam;

    private RAM()
    {
        mRamArray = new String[RAM_SIZE];
        mNextLocation = 0;
    }

    public synchronized static RAM getInstance()
    {
        if(mRam == null){
            mRam = new RAM();
        }
        return mRam;
    }

    public String read(int loc)
    {
        if(loc >= 0 && loc < RAM_SIZE)
        {
            if (!CONTEXT_SWITCH)
                return mRamArray[loc];
            else
            {
                int d = (loc / 4);
                int p = (loc % 4);
                return MemManager.get(loc, d, p);
            }
        }
        else
        {
            ErrorLog.getInstance().writeError("RAM::read || >> Invalid parameter given.");
            throw new IllegalArgumentException();
        }
    }

    public String DMAread(int loc)
    {

        if(loc >= 0 && loc < RAM_SIZE)
        {
            return mRamArray[loc];
        }
        else
        {
            ErrorLog.getInstance().writeError("RAM::read || >> Invalid parameter given.");
            throw new IllegalArgumentException();
        }
    }

    public void write_loc(String data, int loc)
    {
        if( data != null)
        {
            if (CONTEXT_SWITCH)
            {
                MemManager.write(data, (loc / 4), (loc % 4));
            }
            else
            {
                if(loc >= 0 && loc < RAM_SIZE)
                {
                    mRamArray[loc] = data;
                }
                else
                {
                    ErrorLog.getInstance().writeError("RAM::write_loc || >> Invalid parameter given.");
                    throw new IllegalArgumentException();
                }
            }
        }
        else
        {
            ErrorLog.getInstance().writeError("RAM::write_loc || >> Invalid");
            ErrorLog.getInstance().writeError("null value\n");
            throw new IllegalArgumentException();
        }
    }

    public void DMAwrite(String data, int loc)
    {
        if( data != null){
            if(loc >= 0 && loc < RAM_SIZE){
                mRamArray[loc] = data;
            }else{
                ErrorLog.getInstance().writeError("RAM::write_loc || >> Invalid parameter given.");
                throw new IllegalArgumentException();
            }

        }else{
            ErrorLog.getInstance().writeError("RAM::write_loc || >> Invalid");
            ErrorLog.getInstance().writeError("null value\n");
            throw new IllegalArgumentException();
        }
    }


    public int write_next(String data)
    {
        int returnLoc = -1;

        if(data != null){
            if(mNextLocation >= 0 && mNextLocation < RAM_SIZE){
                mRamArray[mNextLocation] = data;
                returnLoc = mNextLocation;
                mNextLocation++;
            }else{
                ErrorLog.getInstance().writeError("RAM::write_loc || >> Invalid address.");
                throw new IllegalArgumentException();
            }
        }else{
            ErrorLog.getInstance().writeError("RAM::write_loc || >> Invalid");
            ErrorLog.getInstance().writeError("null value\n");
            throw new IllegalArgumentException();
        }

        return returnLoc;
    }

    public void resetRAM()
    {
        for (int i = 0;i < RAM_SIZE; i++)
            mRamArray[i] = "";
        mNextLocation=0;
    }

//    public String[] getMemDump()
//    {
//        String[] temp = new String[Driver.amtOfRAM];
//
//        for(int i = 0; i < ram.length; i++)
//        {
//            temp[i] = ram[i];
//        }
//
//        return temp;
//    }

    public int get_next_loc()
    {
        return mNextLocation;
    }

    public int sizeOfRam()
    {
        return RAM_SIZE;
    }

    public String toString()
    {
        String temp = "Memory Dump:";

        for(int i = 0; i < mRamArray.length; i++)
        {
            temp += "\n" + mRamArray[i];
        }

        return temp;
    }
}
