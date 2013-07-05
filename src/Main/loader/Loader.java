package Main.loader;

/**
 * Created with IntelliJ IDEA.
 * User: aV1rus
 * Date: 6/28/13
 * Time: 09:49 AM
 */
import java.io.*;

//import HardDrive.HardDrive;
import Main.memory.HardDrive;
import Main.process_control.PCB;

public class Loader
{
    private static Loader diskLoader;
    private int jobNum;
    private int job_loc;
    private boolean firstWord;
    private boolean firstData;
    private int count;

    private Loader()
    {
        firstWord = false;
        firstData = false;
        job_loc = -999;

        try
        {
            FileReader fr = new FileReader("data2.txt");
            BufferedReader br = new BufferedReader(fr);
            String in = br.readLine();

            while(in != null)
            {
                if(in.contains("JOB")){
                    jobNum = PCB.getInstance().addJob(in);
                    firstWord = true;
                }else if(in.contains("Data")){
                    PCB.getInstance().addData(in, jobNum);
                    firstData = true;;
                    count = 0;

                }else if(in.contains("END")){
                    PCB.getInstance().getJob(jobNum).setData_size(count);
                    jobNum = -1;
                }else{
                    job_loc = HardDrive.getInstance().add(in.substring(2, in.length()));
                    if(firstWord)
                    {
                        PCB.getInstance().getJob(jobNum).setProc_diskStart(job_loc);
                        firstWord = false;
                    }
                    else if(firstData)
                    {
                        PCB.getInstance().getJob(jobNum).setData_diskStart(job_loc);
                        firstData = false;
                    }

                    count++;
                }

                in = br.readLine();
            }
            br.close();
            fr.close();
        }catch(IOException io){
            io.printStackTrace();
        }

    }

    public static synchronized Loader getInstance()
    {
        if(diskLoader == null)
        {
            diskLoader = new Loader();
        }

        return diskLoader;
    }
}

