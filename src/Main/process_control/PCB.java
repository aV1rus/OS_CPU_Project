package Main.process_control;

/**
 * Created with IntelliJ IDEA.
 * User: aV1rus
 * Date: 7/2/13
 * Time: 07:50 AM
 */
import java.util.*;
import Main.log_files.ErrorLog;

//class def
public class PCB
{
    private List<Process> pcb;
    private static PCB database;
    private int iterator;
    private int lastJob;
    public static boolean loop, loop2;
    public boolean done = false;

    private PCB()
    {
        pcb = new ArrayList<Process>();
        iterator = -1;
        loop = true;
        loop2 = true;
        lastJob = 0;
    }

    public static synchronized PCB getInstance()
    {
        if(database == null)
        {
            database = new PCB();
        }

        return database;
    }

    public int addJob(String job)
    {
        Process temp = new Process();

        if(job != null)
        {
            temp = PCB.parse(job);
            pcb.add(temp);
            lastJob++;
            return temp.getProc_id();
        }
        else
        {
            ErrorLog.getInstance().writeError("PCB::addJob || >> Input parameter is null.");
            throw new IllegalArgumentException();
        }
    }

    public void addData(String data, int jobNum)
    {
        Process temp = new Process();
        Process job;

        if(data != null)
        {
            temp = PCB.parse(data);
            if( 0 < jobNum && (jobNum-1) < pcb.size())
            {
                job = pcb.get((jobNum-1));
                if(job.getProc_id() == jobNum)
                {
                    job.setInputBuffer(temp.getInputBuffer());
                    job.setOutputBuffer(temp.getOutputBuffer());
                    job.setTempBuffer(temp.getTempBuffer());
                }
                else
                {
                    ErrorLog.getInstance().writeError("PCB::addData || >> Jobs mis-represented.");
                    throw new IllegalArgumentException();
                }
            }
            else
            {
                ErrorLog.getInstance().writeError("PCB::addData || >> Parameter job num out of bounds.");
                throw new IndexOutOfBoundsException();
            }
        }
        else
        {
            ErrorLog.getInstance().writeError("PCB::addData || >> data parameter = null.");
            throw new IllegalArgumentException();
        }
    }

    private static Process parse(String s)
    {
        int index = 0;
        String initialStr = s;
        String temp = "";
        String hexStr = "";
        Process process = new Process();
        int pid, jobPriority, size;
        int inputBuff, outputBuff, tempBuff;
        List<Character> hexList = new ArrayList<Character>();

        if(initialStr.contains("JOB"))
        {
            index = initialStr.indexOf('B');

            temp = initialStr.substring(index + 2);
            index = temp.indexOf(' ');

            String job_num = temp.substring(0, index);
            pid = Integer.parseInt(temp.substring(0, index), 16);
            temp = temp.substring(index + 1);
            index = temp.indexOf(' ');
            hexStr = temp.substring(0, index);

            size = Integer.parseInt(hexStr, 16);

            temp = temp.substring(index + 1);
            jobPriority = Integer.parseInt(temp, 16);

            process.setProc_id(pid);
            process.setJobPriority(jobPriority);
            process.setProc_iCount(size);

            return process;
        }
        else if(initialStr.contains("Data"))
        {
            index = initialStr.indexOf('t') + 2;
            temp = initialStr.substring(index + 1);
            index = temp.indexOf(' ');

            String input_buff = temp.substring(0, index);
            process.setInputBuffer(Integer.parseInt(input_buff, 16));
            temp = temp.substring(index + 1);
            index = temp.indexOf(' ');
            String output_buff = temp.substring(0, index);
            process.setOutputBuffer(Integer.parseInt(output_buff, 16));
            temp = temp.substring(index + 1);
            process.setTempBuffer(Integer.parseInt(temp, 16));

            return process;
        }
        else
        {
            System.err.println("parse::PCB||Invalid input parameter; non-PCB element entered.");
            throw new IllegalArgumentException();
        }
    }

    public Process getJob(int jobNum)
    {
        if((jobNum-1) < lastJob && (jobNum-1) >= 0){
            return pcb.get(jobNum - 1);
        }else{
            ErrorLog.getInstance().writeError("PCB::getJob || >> Invalid job number.");
            throw new IllegalArgumentException();
        }
    }

    public Process nextProcess()
    {
        if((iterator+1) >= lastJob){
            done = true;
            return pcb.get(iterator);
        }else{
            iterator++;
            return pcb.get(iterator);
        }
    }
    public int nextProcessIndex()
    {
        if((iterator+1) >= lastJob){
            done = true;
            return iterator;
        }else{
            iterator++;
            return iterator;
        }
    }


    public void reduceIter()
    {
        iterator--;
    }


    public boolean isDone()
    {
        return done;
    }

    public int lastJob()
    {
        return pcb.size();
    }

    public String toString()
    {
        String temp_str = "PCB Values:";
        for(int i = 0; i < pcb.size(); i++){
            temp_str += "\n\n\n\n\n" + pcb.get(i);
        }

        return temp_str;
    }

    public List<Process> getPCBArray(){
        return pcb;
    }
}
