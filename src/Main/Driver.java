package Main;

import Main.ConfigFiles.Constants;
import Main.ControlUnit.CPU;
import Main.ControlUnit.Dispatch;
import Main.Memory.HardDrive;
import Main.Memory.MemManager;
import Main.ProcessControl.PCB;
import Main.ProcessControl.ReadyQueue;
import Main.ProcessControl.Scheduler;
import Main.ProcessControl.WaitQueue;
import static Main.ConfigFiles.Config.*;
/**
 * Created with IntelliJ IDEA.
 * User: Nick Maiello (aV1rus)
 * Date: 6/28/13
 * Time: 11:49 AM
 */

public class Driver
{


    public static void main(String [] args)
    {
        HardDrive hardDrive = HardDrive.getInstance();
        PCB pcb = PCB.getInstance();
        Loader loader = Loader.getInstance();
        Scheduler scheduler = Scheduler.getInstance();

        new Dispatch();

        MemManager memMas= new MemManager();

        CPU[] CPUCore;
        CPUCore = new CPU[NUM_OF_PROCESSOR];
        for (int i = 0; i < NUM_OF_PROCESSOR; i++)
            CPUCore[i] = new CPU();

        int nextJob = 0;
        int waitQVal = 0;

        int notBusyCount = 0;

        printCurrentDataTitles();
        while ( !pcb.isDone() ){
            scheduler.longTerm();
            ReadyQueue.getInstance().prioritize();
            memMas.makeMMU();
            if (!PCB.getInstance().isDone() || ReadyQueue.getInstance().hasJobsInQueue() || (!WaitQueue.isEmpty())){


                boolean firstRun = true;
                while((firstRun) || (nextJob >= 0) || (notBusyCount < NUM_OF_PROCESSOR) || (!WaitQueue.isEmpty())){
                    firstRun=false;


                    // Check each Processor

                    for (int i = 0; i < NUM_OF_PROCESSOR; i++){

                        if (!CPUCore[i].getIsBusy()){        //IF Processor CORE is not busy
                            if (CONTEXT_SWITCH){
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
                                Dispatch.getDispatch(i).give_proc(nextJob);
                                CPUCore[i].setIsBusy(true);
                            }else
                                CPUCore[i].setIsBusy(false);

                        }else{                       //IF Processor CORE IS busy

                            if(!Dispatch.getDispatch(i).getTerminate())
                            {
                                int procIndex = Dispatch.getDispatch(i).get();
                                if (procIndex == -1)
                                    CPUCore[i].setIsBusy(false);
                                else
                                    CPUCore[i].run(procIndex, i);
                            }
                            else
                                CPUCore[i].setIsBusy(false);
                        }
                        notBusyCount = 0;
                        for (int j = 0; j < NUM_OF_PROCESSOR; j++)
                        {
                            if (!CPUCore[j].getIsBusy())
                                notBusyCount++;
                        }

                        WaitQueue.countDown();



                    }

                }

            }
        }
        pcb.getJob(1).getFinalData();
    }
    public static void printCurrentDataTitles()
    {
        System.out.print("\n\n");
        System.out.format(Constants.OUTPUT_TABLE_FORMAT,
                Constants.PROCESS_LABEL + "\t",
                "\t",
                Constants.WAIT_TIME_LABEL + "\t",
                "\t",
                Constants.EXECUTION_TIME_LABEL + "\t",
                "\t",
                Constants.INSTRUCTIONS_LABEL + "\t",
                "\t",
                Constants.IO_INSTRUCTIONS_LABEL + "\t",
                "\t",
                Constants.FAULTS_LABEL + "\t",
                "\t");
        System.out.print(Constants.TABLE_LINE_BREAK);
    }
}
