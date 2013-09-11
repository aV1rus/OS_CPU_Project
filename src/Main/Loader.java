package Main;

/**
 * Created with IntelliJ IDEA.
 * User: Nick Maiello (aV1rus)
 * Date: 6/27/13
 * Time: 09:49 AM
 */
import java.io.*;

//import HardDrive.HardDrive;
import Main.ConfigFiles.Constants;
import Main.Log.ErrorLog;
import Main.Memory.HardDrive;
import Main.ProcessControl.PCB;

import static Main.ConfigFiles.Config.DATASHEET_FILE;

public class Loader
{
    private static Loader mLoader;
    private int mJobNum;
    private int mJobLocation;
    private int mCurrentType;
    private int mCount;

    public static synchronized Loader getInstance(){
        if(mLoader == null) mLoader = new Loader();
        return mLoader;
    }

    private Loader()
    {
        mCurrentType = -1;
        mJobLocation = -999;

        try
        {
            FileReader fr = new FileReader(DATASHEET_FILE);
            BufferedReader br = new BufferedReader(fr);
            String in = br.readLine();

            while(in != null)
            {
                if(in.contains(Constants.LOADER_JOB)){
                    mJobNum = PCB.getInstance().addJob(in);
                    mCurrentType = 0;
                }else if(in.contains(Constants.LOADER_DATA)){
                    PCB.getInstance().addData(in, mJobNum);
                    mCurrentType = 1;
                    mCount = 0;

                }else if(in.contains(Constants.LOADER_END)){
                    PCB.getInstance().getJob(mJobNum).setData_size(mCount);
                    mJobNum = -1;
                }else{
                    mJobLocation = HardDrive.getInstance().add(in.substring(2, in.length()));

                    switch(mCurrentType)
                    {
                        case 0:
                            PCB.getInstance().getJob(mJobNum).setProc_diskStart(mJobLocation);
                            mCurrentType = -1;
                            break;
                        case 1:
                            PCB.getInstance().getJob(mJobNum).setData_diskStart(mJobLocation);
                            mCurrentType = -1;
                            break;
                        default:
                            break;
                    }


                    mCount++;
                }

                in = br.readLine();
            }
            br.close();
            fr.close();


            System.out.println("");
            System.out.println("LOADER :: > Done loading JOBS");



        }catch(IOException io){

            ErrorLog.getInstance().writeError("IO Exception in Loader class");


        }

    }


}

