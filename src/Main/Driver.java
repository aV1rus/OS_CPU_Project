package Main;

import Main.ControlUnit.CPU;
import Main.ControlUnit.MultiDispatch;
import Main.Memory.HardDrive;
import Main.Memory.MemManager;
import Main.ProcessData.PCB;
import Main.ProcessData.ReadyQueue;
import Main.ProcessData.Scheduler;
import Main.ProcessData.WaitQueue;

import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: aV1rus
 * Date: 6/28/13
 * Time: 11:49 AM
 */

public class Driver
{
    // 0=FCFS, 1=Job Priority, 2=SJF
    public static final int sort = 1;
    // choose 1, 4, 8, 32 or other
    public static int numOfProcessors = 4;
    // RAM divided by 4 (256 entered here = 1024 RAM)
    public static int amtOfRAM = 1024;
    public static final int hardDriveSpace = 2048;
    public static final boolean contextSwitch = true;

    public static Scheduler.Processors getMultiSing(){
        Scanner input=new Scanner(System.in);
        String val = "";

        System.out.println("Would you like this to be a Single core or Multi Core demo? S == One CPU || M == 4 CPU");
        val=input.next(); // Get what the user types.

        if(val.toLowerCase().equals("s")) return  Scheduler.Processors.SINGLE;
        if(val.toLowerCase().equals("m")) return Scheduler.Processors.MULTI;

        System.out.println("Please Only use letters S or M... (Not Case Sensitive)");

        return getMultiSing();
    }

    public static int getNumProcs(){
        Scanner input=new Scanner(System.in); // Decl. & init. a Scanner.
        String val = "";

        System.out.println("How many CPU's would you like to have?");
        val=input.next(); // Get what the user types.

        try{
            if(Integer.parseInt(val) % 1 == 0) return  Integer.parseInt(val);
        }catch(Exception e){}

        System.out.println("Please Only use whole numbers");

        return getNumProcs();
    }
    public static int getAmtRam(){
        Scanner input=new Scanner(System.in); // Decl. & init. a Scanner.
        String val = "";

        System.out.println("How much ram would you like to have? Only use multiple's of 1024... (or number to multiply by 1024)");
        val=input.next(); // Get what the user types.
        try{
            if(Integer.parseInt(val) % 1024 == 0) return  Integer.parseInt(val);
            if(Integer.parseInt(val) < 15){
                System.out.println("Received multiply by "+val + " = "+Integer.parseInt(val)*1024);
                return  Integer.parseInt(val)*1024;    }
        }catch(Exception e){
        }

        System.out.println("Please Only use multiples of 1024...");

        return getAmtRam();
    }

    public static void main(String [] args)
    {
        amtOfRAM = 2048;//getAmtRam();
        numOfProcessors = 8;//getNumProcs();

        HardDrive.getInstance();
        PCB.getInstance();
        Loader.getInstance();
        Scheduler.getInstance();

        MultiDispatch bigDispatch = new MultiDispatch();

        MemManager memMaster = new MemManager();

        CPU[] CPUCore;
        CPUCore = new CPU[numOfProcessors];
        for (int i = 0; i < numOfProcessors; i++)
            CPUCore[i] = new CPU();

        int nextJob = 0;
        int waitQVal = 0;

        int notBusyCount = 0;
        int x = 0;

        while ( !PCB.getInstance().isDone() ){
            Scheduler.getInstance().longTerm();
            ReadyQueue.getInstance().prioritize();
            memMaster.makeMMU();
            if (!PCB.getInstance().isDone() || ReadyQueue.getInstance().hasJobsInQueue() || (!WaitQueue.isEmpty())){
                boolean firstRun = true;
                while((firstRun) || (nextJob >= 0) || (notBusyCount < numOfProcessors) || (!WaitQueue.isEmpty())){
                    firstRun=false;
                    for (int i = 0; i < numOfProcessors; i++){
                        if (!CPUCore[i].isBusy()){
                            if (contextSwitch){
                                waitQVal = WaitQueue.getItem();
                                if (waitQVal < 1){
                                    nextJob = Scheduler.getInstance().shortTerm();
                                }else{
                                    nextJob = waitQVal;
                                    CPUCore[i].setRegisters(PCB.getInstance().getJob(nextJob).getRegisters());
                                }
                            }else
                                        nextJob = Scheduler.getInstance().shortTerm();

                            if (nextJob >= 0){
                                MultiDispatch.getDispatch(i).give_proc(nextJob);
                                CPUCore[i].isBusy(true);
                            }else
                                CPUCore[i].isBusy(false);
                        }else{
                            if(!MultiDispatch.getDispatch(i).getTerminate())
                            {

                                x = MultiDispatch.getDispatch(i).get();
                                if (x == -1)
                                    CPUCore[i].isBusy(false);
                                else
                                    CPUCore[i].run(x,i);
                            }
                            else
                                CPUCore[i].isBusy(false);
                        }
                        notBusyCount = 0;
                        for (int j = 0; j < numOfProcessors; j++)
                        {
                            if (!CPUCore[j].isBusy())
                                notBusyCount++;
                        }

                        WaitQueue.countDown();

                    }

                }

            }
        }
        PCB.getInstance().getJob(1).getFinalData();
    }
}
