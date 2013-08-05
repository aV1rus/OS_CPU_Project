package Main;

import Main.ControlUnit.CPU;
import Main.ControlUnit.MultiDispatch;
import Main.Memory.HardDrive;
import Main.Memory.MemManager;
import Main.ProcessControl.PCB;
import Main.ProcessControl.ReadyQueue;
import Main.ProcessControl.Scheduler;
import Main.ProcessControl.WaitQueue;

import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: Nick Maiello (aV1rus)
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

        HardDrive hardDrive = HardDrive.getInstance();
        PCB pcb = PCB.getInstance();
        Loader loader = Loader.getInstance();
        Scheduler scheduler = Scheduler.getInstance();

        new MultiDispatch();

        MemManager memMas= new MemManager();

        CPU[] CPUCore;
        CPUCore = new CPU[numOfProcessors];
        for (int i = 0; i < numOfProcessors; i++)
            CPUCore[i] = new CPU();

        int nextJob = 0;
        int waitQVal = 0;

        int notBusyCount = 0;

        while ( !pcb.isDone() ){
            scheduler.longTerm();
            ReadyQueue.getInstance().prioritize();
            memMas.makeMMU();
            if (!PCB.getInstance().isDone() || ReadyQueue.getInstance().hasJobsInQueue() || (!WaitQueue.isEmpty())){


                boolean firstRun = true;
                while((firstRun) || (nextJob >= 0) || (notBusyCount < numOfProcessors) || (!WaitQueue.isEmpty())){
                    firstRun=false;


                    // Check each Processor

                    for (int i = 0; i < numOfProcessors; i++){

                        if (!CPUCore[i].isBusy()){        //IF Processor CORE is not busy
                            if (contextSwitch){
                                waitQVal = WaitQueue.getItem();
                                if (waitQVal < 1){
                                    nextJob = scheduler.shortTerm();
                                }else{
                                    nextJob = waitQVal;
                                    CPUCore[i].setRegisters(pcb.getJob(nextJob).getRegisters());
                                }
                            }else
                                        nextJob = scheduler.shortTerm();

                            if (nextJob >= 0){
                                MultiDispatch.getDispatch(i).give_proc(nextJob);
                                CPUCore[i].isBusy(true);
                            }else
                                CPUCore[i].isBusy(false);

                        }else{                       //IF Processor CORE IS busy

                            if(!MultiDispatch.getDispatch(i).getTerminate())
                            {
                                int procIndex = MultiDispatch.getDispatch(i).get();
                                if (procIndex == -1)
                                    CPUCore[i].isBusy(false);
                                else
                                    CPUCore[i].run(procIndex, i);
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
        pcb.getJob(1).getFinalData();
    }
}
