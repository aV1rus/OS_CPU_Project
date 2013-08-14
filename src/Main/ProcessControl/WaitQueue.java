package Main.ProcessControl;

/**
 * Created with IntelliJ IDEA.
 * User: Nick Maiello (aV1rus)
 * Date: 7/2/13
 * Time: 11:53 PM
 * To change this template use File | Settings | File Templates.
 */

import java.util.Vector;
import java.util.Iterator;


public class WaitQueue {


    private static Vector<waitJob>  mQueue = new Vector<waitJob>();


    public WaitQueue(){
        //waitQ = new Vector<waitJob>();
    }

    public static void addItem(int addPID, int io){
        waitJob temp = new waitJob();
        Iterator<waitJob> index = mQueue.iterator();
        boolean added = false;

        temp.jobID = addPID;
        if (io == 1)
        {
            temp.waitTime = 5;
        }
        else if (io == 2)
            temp.waitTime = 1;
        else
            temp.waitTime = 0;


        if (mQueue.isEmpty()){
            mQueue.add(temp);
        }else{

            for (int i = 0; i < (mQueue.size() -1); i++){
                if (temp.waitTime < mQueue.elementAt(i).waitTime ){
                    mQueue.insertElementAt(temp, mQueue.indexOf(index));

                    added = true;
                }
            }

            if (!added){
                mQueue.add(temp);
            }


        }
    }


    public static Integer getItem(){
        int temp = -1;

        Iterator iter = mQueue.iterator();

        if (!mQueue.isEmpty())
        {
            while(iter.hasNext() && temp < 0)
                if (mQueue.get(mQueue.indexOf(iter.next())).waitTime < 0)
                {
                    temp = mQueue.remove(0).jobID;
                }
        }

        if (temp > 0)
        {
            PCB.getInstance().getJob(temp).setJobPriority(2);
        }

        return temp;
    }


    public static void countDown()
    {
        Iterator<waitJob> iter = mQueue.iterator();
        waitJob temp = new waitJob();



        while (iter.hasNext())
        {
            temp = iter.next();
            temp.waitTime--;
        }
    }

    public static boolean isEmpty()
    {
        return (mQueue.isEmpty());
    }


    private static class waitJob
    {
        private int jobID;
        private int waitTime;
        //private boolean isIO;

        private waitJob()
        {
            jobID = -1;
            waitTime = 0;
        }

        public String toString()
        {
            return (jobID + " :: Job - Wait is " + waitTime);
        }
    }

}
