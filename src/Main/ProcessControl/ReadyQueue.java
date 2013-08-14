package Main.ProcessControl;

/**
 * Created with IntelliJ IDEA.
 * User: Nick Maiello (aV1rus)
 * Date: 7/2/13
 * Time: 11:51 AM
 */
import java.util.*;

import Main.Driver;
import Main.Log.ErrorLog;
import static Main.ConfigFiles.Config.*;

public class ReadyQueue
{
    private static ReadyQueue mReadyQueue;
    private static LinkedList<Integer> mProcessIDs;
    private int mIterator;

    private int mCounter;

    private ReadyQueue(){
        mProcessIDs = new LinkedList<Integer>();
        mIterator = 0;
    }

    public static synchronized ReadyQueue getInstance(){
        if(mReadyQueue == null)
        {
            mReadyQueue = new ReadyQueue();
        }

        return mReadyQueue;
    }

    public int addProcess(int id){
        if(id > 0)
        {
            if(mProcessIDs.add(id)){
                return 0;
            }else{
                ErrorLog.getInstance().writeError("ReadyQueue >> Error adding Process ID");
                return 1;
            }
        }else{
            ErrorLog.getInstance().writeError("ReadQueue.add >> invalid Input ID.");
            throw new IllegalArgumentException();
        }
    }

    public int getProcesses(){
        int getPID;
        if(mIterator < mProcessIDs.size()){
            try{
                getPID = mProcessIDs.get(mIterator);
                mIterator++;
            }catch(java.util.NoSuchElementException e){
                ErrorLog.getInstance().writeError("ReadyQueue Error: " + e.toString());
                return -1;
            }
            return getPID;
        }else{
            PCB.mSecondLoop = false;
            return -1;
        }
    }

    public boolean hasJobsInQueue(){
        if (mIterator < mProcessIDs.size())
            return true;
        else
            return false;
    }

    public int getSize(){
        return mProcessIDs.size();
    }

//    public void resetReadyQueue(){
//        processIDs = new LinkedList<Integer>();
//        iterator = 0;
//    }


    public void prioritize(){
        int [][] temp;
        temp = new int [mProcessIDs.size()+1][2];

        int lastRQ = mProcessIDs.size();

        switch(SORT_TYPE)
        {
            case 1: //priority
            {
                //intialize temporary holder for job list
                for (int i = mCounter; i <= lastRQ; i++)
                {
                    temp[i][0] = 0;
                    temp[i][1] = 0;
                }

                //retrieve the last items from the RQ to be sorted (jobs not assigned)
                for (int i = mCounter; i < lastRQ; i++)
                {
                    temp[i][0] = mProcessIDs.pollLast();
                    temp[i][1] = PCB.getInstance().getJob(temp[i][0]).getJobPriority();
                }

                //bubble sort based on job priority
                for (int i = mCounter; i < (temp.length - 1); i++)
                    for (int j = mCounter; j < (temp.length - 1); j++)
                    {
                        if (temp[i][1] > temp[j][1])
                        {
                            int holder = temp[j][0];
                            int holder2 = temp[j][1];
                            temp[j][0] = temp[i][0];
                            temp[j][1] = temp[i][1];
                            temp[i][0] = holder;
                            temp[i][1] = holder2;
                        }
                    }
                //put the jobs back in the ReadyQueue in sorted order
                for (int i = mCounter; i < (temp.length -1); i++)
                {
                    ReadyQueue.getInstance().addProcess(temp[i][0]);
                }

                //mark the last sorted job
                mCounter = lastRQ;

            }//case Priority

            case 2:  // SJF
            {
                //intialize temporary holder for job list
                for (int i = mCounter; i <= lastRQ; i++)
                {
                    temp[i][0] = 0;
                    temp[i][1] = 0;
                }

                //retrieve the last items from the RQ to be sorted (jobs not assigned)
                for (int i = mCounter; i < lastRQ; i++)
                {
                    temp[i][0] = mProcessIDs.pollLast();
                    temp[i][1] = PCB.getInstance().getJob(temp[i][0]).getProc_iCount();
                }

                //bubble sort based on instruction count
                for (int i = mCounter; i < (temp.length - 1); i++)
                    for (int j = mCounter; j < (temp.length - 1); j++)
                    {
                        if (temp[i][1] > temp[j][1])
                        {
                            int holder = temp[j][0];
                            int holder2 = temp[j][1];
                            temp[j][0] = temp[i][0];
                            temp[j][1] = temp[i][1];
                            temp[i][0] = holder;
                            temp[i][1] = holder2;
                        }
                    }
                //put the jobs back in the ReadyQueue in sorted order
                for (int i = mCounter; i < (temp.length -1); i++)
                {
                    ReadyQueue.getInstance().addProcess(temp[i][0]);
                }

                //mark the last sorted job
                mCounter = lastRQ;

            }//case SJF


            case 0:
            {
                //jobs already in this order
            }
        }
    }



    //print out the ReadyQueue
    public String toString()
    {
        String temp = "Ready Queue List: ";

        for(int i = 0; i < mProcessIDs.size(); i++)
        {
            temp += "\n" + mProcessIDs.get(i);
        }

        return temp;
    }
}

