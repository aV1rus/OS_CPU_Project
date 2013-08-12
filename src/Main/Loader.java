package Main;

/**
 * Created with IntelliJ IDEA.
 * User: Nick Maiello (aV1rus)
 * Date: 6/27/13
 * Time: 09:49 AM
 */
import java.io.*;

//import HardDrive.HardDrive;
import Main.Constants.Constants;
import Main.Memory.HardDrive;
import Main.ProcessControl.PCB;

public class Loader
{
    private static Loader loader;
    private int jobNum;
    private int job_loc;
    private int currentType;
    private int count;

    public static synchronized Loader getInstance(){
        if(loader == null) loader = new Loader();
        return loader;
    }

    private Loader()
    {
        currentType = -1;
        job_loc = -999;

        try
        {
            FileReader fr = new FileReader("data.txt");
            BufferedReader br = new BufferedReader(fr);
            String in = br.readLine();

            while(in != null)
            {
                if(in.contains(Constants.LOADER_JOB)){
                    jobNum = PCB.getInstance().addJob(in);
                    currentType = 0;
                    //System.out.println("LOADER :: JOB > "+in);
                }else if(in.contains(Constants.LOADER_DATA)){
                    PCB.getInstance().addData(in, jobNum);
                    //System.out.println("LOADER :: DATA > "+in);
                    currentType = 1;
                    count = 0;

                }else if(in.contains(Constants.LOADER_END)){
                    PCB.getInstance().getJob(jobNum).setData_size(count);
                    //System.out.println("LOADER :: END > "+in);
                    jobNum = -1;
                }else{
                    job_loc = HardDrive.getInstance().add(in.substring(2, in.length()));
                    if(currentType == 0){
                        //System.out.println("LOADER :: FIRST JOB > "+in);
                        PCB.getInstance().getJob(jobNum).setProc_diskStart(job_loc);
                        currentType = -1;
                    }else if(currentType == 1){
                        //System.out.println("LOADER :: FIRST DATA > "+in);
                        PCB.getInstance().getJob(jobNum).setData_diskStart(job_loc);
                        currentType = -1;
                    }

                    count++;
                }

                in = br.readLine();
            }
            br.close();
            fr.close();


            System.out.println("");
            System.out.println("LOADER :: > Finished Loading successfully");
        }catch(IOException io){
            io.printStackTrace();
        }

    }


}

