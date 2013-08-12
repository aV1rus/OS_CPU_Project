package Main.ProcessControl;

/**
 * Created with IntelliJ IDEA.
 * User: aV1rus
 * Date: 7/2/13
 * Time: 07:50 AM
 */
import java.util.*;
import Main.Log.ErrorLog;

//class def
public class PCB
{
    private List<Process> mProcesses;

    private static PCB mPCB;
    private int mIterator;
    private int mLastJob;
    public static boolean mFirstLoop, mSecondLoop;
    public boolean mDone = false;

    private PCB()
    {
        mProcesses = new ArrayList<Process>();
        mIterator = -1;
        mFirstLoop = true;
        mSecondLoop = true;
        mLastJob = 0;
    }

    public static synchronized PCB getInstance()
    {
        if(mPCB == null) mPCB = new PCB();
        return mPCB;
    }


    /**
     * getJob
     * @param jobNum
     * @return get Job by index
     */
    public Process getJob(int jobNum)
    {
        if((jobNum-1) < mLastJob && (jobNum-1) >= 0){
            return mProcesses.get(jobNum - 1);
        }else{
            ErrorLog.getInstance().writeError("PCB :: getJob >> No job for index");
            throw new IllegalArgumentException();
        }
    }

    /**
     * nextProcess
     * @return    the next Process in the array
     */
    public Process nextProcess()
    {
        if((mIterator+1) >= mLastJob){
            mDone = true;
            return mProcesses.get(mIterator);
        }else{
            mIterator++;
            return mProcesses.get(mIterator);
        }
    }
    public void reduceIter()
    {
        mIterator--;
    }


    public boolean isDone()
    {
        return mDone;
    }

    public int lastJob()
    {
        return mProcesses.size();
    }

    public String toString()
    {
        String temp_str = "PCB Values:";
        for(int i = 0; i < mProcesses.size(); i++){
            temp_str += "\n\n\n\n\n" + mProcesses.get(i);
        }

        return temp_str;
    }

    public List<Process> getPCBArray(){
        return mProcesses;
    }


    /***
     *
     * ADD JOB
     * @param job
     * @return
     *
     * Call this method to add job to PCB
     */
    public int addJob(String job)
    {
        Process temp;

        if(job != null)
        {
            temp = PCB.parse(job);
            mProcesses.add(temp);
            mLastJob++;
            return temp.getProc_id();
        }
        else
        {
            ErrorLog.getInstance().writeError("PCB :: > addJob >> String is null");
            throw new IllegalArgumentException();
        }
    }

    /***
     * addData
     * @param data           Data related to job
     * @param jobNum         JOB of which data is for
     * Call thius function to add job to PCB
     */
    public void addData(String data, int jobNum)
    {
        Process temp;
        Process job;

        if(data != null){
            temp = PCB.parse(data);
            if( 0 < jobNum && (jobNum-1) < mProcesses.size()){
                job = mProcesses.get((jobNum-1));
                if(job.getProc_id() == jobNum){
                    job.setInputBuffer(temp.getInputBuffer());
                    job.setOutputBuffer(temp.getOutputBuffer());
                    job.setTempBuffer(temp.getTempBuffer());
                }else{
                    ErrorLog.getInstance().writeError("PCB :: > addData >> Error");
                    throw new IllegalArgumentException();
                }
            }else{
                ErrorLog.getInstance().writeError("PCB :: > addData >> Out of bounds.");
                throw new IndexOutOfBoundsException();
            }
        } else{
            ErrorLog.getInstance().writeError("PCB :: > addData >> data is null.");
            throw new IllegalArgumentException();
        }
    }


    /***
     * parse
     * @param s
     * @return
     * this will parse the Job is process or
     */
    private static Process parse(String s)
    {
        Process process = new Process();

        String[] data = s.split(" ");

        if(s.contains("JOB")){
            process.setProc_id(Integer.parseInt(data[2], 16));
            process.setJobPriority(Integer.parseInt(data[4], 16));
            process.setProc_iCount(Integer.parseInt(data[3], 16));

            return process;
        }else if(s.contains("Data")){
            process.setInputBuffer(Integer.parseInt(data[2], 16));
            process.setOutputBuffer(Integer.parseInt(data[3], 16));
            process.setTempBuffer(Integer.parseInt(data[4], 16));

            return process;
        }else{
            System.err.println("PCB :: parse > Not a job or data");
            throw new IllegalArgumentException();
        }
    }
}
