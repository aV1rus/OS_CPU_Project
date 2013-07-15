package Main.ProcessData;

/**
 * Created with IntelliJ IDEA.
 * User: aV1rus
 * Date: 7/2/13
 * Time: 11:51 AM
 */
import java.util.*;

import Main.Driver;
import Main.Log.ErrorLog;

public class ReadyQueue
{
    private static ReadyQueue readyQueue;
    private static LinkedList<Integer> processIDs;
    private int iterator;

    private int rqCounter;

    private ReadyQueue(){
        processIDs = new LinkedList<Integer>();
        iterator = 0;
    }

    public static synchronized ReadyQueue getInstance(){
        if(readyQueue == null)
        {
            readyQueue = new ReadyQueue();
        }

        return readyQueue;
    }

    public int addProcess(int id){
        if(id > 0)
        {
            if(processIDs.add(id)){
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
        if(iterator < processIDs.size()){
            try{
                getPID = processIDs.get(iterator);
                iterator++;
            }catch(java.util.NoSuchElementException e){
                ErrorLog.getInstance().writeError("ReadyQueue Error: " + e.toString());
                return -1;
            }
            return getPID;
        }else{
            PCB.loop2 = false;
            return -1;
        }
    }

    public boolean hasJobsInQueue(){
        if (iterator < processIDs.size())
            return true;
        else
            return false;
    }

    public int getSize(){
        return processIDs.size();
    }

    public void resetReadyQueue(){
        processIDs = new LinkedList<Integer>();
        iterator = 0;
    }


    public void prioritize(){
        int [][] temp;
        temp = new int [processIDs.size()+1][2];

        int lastRQ = processIDs.size();

        switch(Driver.sort)
        {
            case 1: //priority
            {
                //intialize temporary holder for job list
                for (int i = rqCounter; i <= lastRQ; i++)
                {
                    temp[i][0] = 0;
                    temp[i][1] = 0;
                }

                //retrieve the last items from the RQ to be sorted (jobs not assigned)
                for (int i = rqCounter; i < lastRQ; i++)
                {
                    temp[i][0] = processIDs.pollLast();
                    temp[i][1] = PCB.getInstance().getJob(temp[i][0]).getJobPriority();
                }

                //bubble sort based on job priority
                for (int i = rqCounter; i < (temp.length - 1); i++)
                    for (int j = rqCounter; j < (temp.length - 1); j++)
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
                for (int i = rqCounter; i < (temp.length -1); i++)
                {
                    ReadyQueue.getInstance().addProcess(temp[i][0]);
                }

                //mark the last sorted job
                rqCounter = lastRQ;

            }//case Priority

            case 2:  // SJF
            {
                //intialize temporary holder for job list
                for (int i = rqCounter; i <= lastRQ; i++)
                {
                    temp[i][0] = 0;
                    temp[i][1] = 0;
                }

                //retrieve the last items from the RQ to be sorted (jobs not assigned)
                for (int i = rqCounter; i < lastRQ; i++)
                {
                    temp[i][0] = processIDs.pollLast();
                    temp[i][1] = PCB.getInstance().getJob(temp[i][0]).getProc_iCount();
                }

                //bubble sort based on instruction count
                for (int i = rqCounter; i < (temp.length - 1); i++)
                    for (int j = rqCounter; j < (temp.length - 1); j++)
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
                for (int i = rqCounter; i < (temp.length -1); i++)
                {
                    ReadyQueue.getInstance().addProcess(temp[i][0]);
                }

                //mark the last sorted job
                rqCounter = lastRQ;

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

        for(int i = 0; i < processIDs.size(); i++)
        {
            temp += "\n" + processIDs.get(i);
        }

        return temp;
    }
}

