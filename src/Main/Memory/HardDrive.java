package Main.Memory;

/**
 * Created with IntelliJ IDEA.
 * User: Nick Maiello (aV1rus)
 * Date: 7/3/13
 * Time: 12:45 AM
 * To change this template use File | Settings | File Templates.
 *
 *
 * HardDrive Module
 */

import Main.Driver;
import Main.Log.ErrorLog;

//class def
public class HardDrive
{
    //method variables
    private String [] mHardDrive;
    private int mNextLocation;
    private int mLastLocation;

    private static HardDrive disk;

    //constructor
    private HardDrive()
    {
        mHardDrive = new String[Driver.hardDriveSpace];
        mNextLocation = 0;
    }

    //synchronized object for instance
    public synchronized static HardDrive getInstance()
    {
        if(disk == null)
        {
            disk = new HardDrive();
        }

        return disk;
    }

    //add information to disk
    public int add(String word)
    {
        //valid address and information?
        if(mNextLocation >= 0 && word != null)
        {
            //write to next location and increment
            mHardDrive[mNextLocation] = word;
            int temp = mNextLocation;
            mLastLocation = mNextLocation;
            mNextLocation++;

            //end of drive space? Set flag
            if(mNextLocation == Driver.hardDriveSpace)
            {
                mNextLocation = -1;
            }

            return temp;
        }
        else if(mNextLocation == -1)
        {
            ErrorLog.getInstance().writeError("HardDisk::add || >> Disk is full.");
            throw new IllegalArgumentException();
        }
        else
        {
            ErrorLog.getInstance().writeError("HardDisk::add || >> Input param is null.");
            throw new IllegalArgumentException();
        }
    }

    //get data from a specific point on the drive
    public String getLoc(int loc)
    {
        if(loc < mHardDrive.length && loc >= 0)
        {
            return mHardDrive[loc];
        }
        else
        {
            ErrorLog.getInstance().writeError("HardDisk::getLoc || >> Parameter is incorrect.");
            throw new IllegalArgumentException();
        }
    }

    //write data to a specific location on the drive
//    public void writeLoc(String hex, int loc)
//    {
//        if(hex != null && loc < h_drive.length)
//        {
//            h_drive[loc] = hex;
//        }
//        else
//        {
//            ErrorLog.getInstance().writeError("HardDrive::writeLoc || >> Invalid inputs.");
//            throw new IllegalArgumentException();
//        }
//    }

    //get the last disk location for comparisons
//    public int maxDiskLoc()
//    {
//        return lastLoc;
//    }

    //print contents of disk
    public String toString()
    {
        String str = "Memory Dump";
        for(int i = 0; i < mHardDrive.length; i++)
        {
            str +="\nmem_loc " + i + " :: " + mHardDrive[i];
        }

        return str;
    }
}

