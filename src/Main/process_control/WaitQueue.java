package Main.process_control;

/**
 * Created with IntelliJ IDEA.
 * User: nick
 * Date: 7/2/13
 * Time: 11:53 PM
 * To change this template use File | Settings | File Templates.
 */

import java.util.Vector;
import java.util.Iterator;


public class WaitQueue {


    private static Vector<waitJob>  waitQ = new Vector<waitJob>();


    public WaitQueue(){
        //waitQ = new Vector<waitJob>();
    }

    public static void addItem(int addPID, int io){
        waitJob temp = new waitJob();
        Iterator<waitJob> index = waitQ.iterator();
        //int eltCount = 0;
        boolean added = false;

        temp.jobID = addPID;
        if (io == 1)
        {
            temp.waitTime = 5;
            //temp.isIO = true;
        }
        else if (io == 2)
            temp.waitTime = 1;
        else
            temp.waitTime = 0;


        if (waitQ.isEmpty()){
            waitQ.add(temp);
        }else{

            for (int i = 0; i < (waitQ.size() -1); i++){
                if (temp.waitTime < waitQ.elementAt(i).waitTime ){
                    waitQ.insertElementAt(temp, waitQ.indexOf(index));

                    added = true;
                }
            }

            if (!added){
                waitQ.add(temp);
            }


        }
    }


    public static Integer getItem(){
        int temp = -1;

        Iterator iter = waitQ.iterator();

        if (!waitQ.isEmpty())
        {
            while(iter.hasNext() && temp < 0)
                if (waitQ.get(waitQ.indexOf(iter.next())).waitTime < 0)
                {
                    temp = waitQ.remove(0).jobID;
                }
        }

        if (temp > 0)
        {
            PCB.getInstance().getJob(temp).setJobPriority(2);
        }

        return temp;
    }

    public void saveRegisters(int jobNum, int[] temp)
    {

    }

    public static void countDown()
    {
        Iterator<waitJob> iter = waitQ.iterator();
        waitJob temp = new waitJob();



        while (iter.hasNext())
        {
            temp = iter.next();
            temp.waitTime--;
        }
    }

    public static boolean isEmpty()
    {
        return (waitQ.isEmpty());
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
            //isIO = false;
        }

        public String toString()
        {
            return (jobID + " :: Job - Wait is " + waitTime);
        }
    }

}
