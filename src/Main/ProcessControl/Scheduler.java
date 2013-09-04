package Main.ProcessControl;

/**
 * Created with IntelliJ IDEA.
 * User: Nick Maiello (aV1rus)
 * Date: 7/2/13
 * Time: 05:52 PM
 */
import Main.Memory.HardDrive;
import Main.Memory.RAM;

//class def
public class Scheduler
{
    //variables used by this class
    public enum Algorithm {FIFO, SJF, PRIORITY};
    public enum Processors {SINGLE, MULTI};
    private static final Algorithm DEFAULT_ALGORITHM = Algorithm.FIFO;
    public static Processors DEFAULT_PROCESSORS = Processors.SINGLE;


    private final Algorithm mAlgorithm;
    private Process mCurrentJob;
    private int mCurrentJobId;
    private static Scheduler mScheduler;
    private int mNum;


    //constructer to build basic element
    private Scheduler()
    {
            this(DEFAULT_PROCESSORS);

    }
    private Scheduler(Processors processors){
        DEFAULT_PROCESSORS = processors;
        mAlgorithm = DEFAULT_ALGORITHM;
        mCurrentJobId = 1;
    }
    //implement a synchronized instance of the scheduler
    public static synchronized Scheduler getInstance()
    {
        //if it does not exist, create; return
        if(mScheduler == null)
            mScheduler = new Scheduler();

        return mScheduler;
    }

    //LongTerm Scheduler
    public int longTerm()
    {
        //reset RAM before filling with data
        RAM.getInstance().resetRAM();
        //reset wait time for current processes entering the queues

        //initialize variables
        int memStart = 0,
                currentDisk = 0,
                programCount = 0,
                dataCount = 0,
                //inBuffCount = 0,
                //outBuffCount = 0,
                tempBuffCount = 0;

        //switch on algorithm for prioritization
        switch(mAlgorithm)
        {
            case FIFO:
            {
                //is the scheduling done? if not...
                mCurrentJob = PCB.getInstance().nextProcess();
                if (!(mCurrentJob == null) && !(PCB.getInstance().isDone())){
                    //get the next process for the job and add it to the Ready Queue

                    //set the current job by retrieving from PCB
                    mCurrentJobId = mCurrentJob.getProc_id();
                    //get next available memory location
                    mNum = RAM.getInstance().get_next_loc();

                    //create temp object for current job
                    Process temp = PCB.getInstance().getJob(mCurrentJobId);

                    //while there are still processes, keep moving to RAM
                    boolean doWork = ((mNum + temp.getProgInstructCount() + temp.getData_count() + temp.getInputBuffer() + temp.getOutputBuffer() + temp.getTempBuffer()) < RAM.getInstance().sizeOfRam());

                    while (doWork)
                    {
                        //get the next job
                        ReadyQueue.getInstance().addProcess(mCurrentJobId);
                        //set the status of this job to ready for dispatch
                        PCB.getInstance().getJob(mCurrentJobId).setProcState(1);
                        //read job info (if we use direct calls this will slow the loop)
                        //currentDisk will hold the current position on the disk
                        currentDisk = PCB.getInstance().getJob(mCurrentJobId).getProc_diskStart();
                        //proc_start will hold the start location of the job on disk
                        int proc_start = PCB.getInstance().getJob(mCurrentJobId).getProc_diskStart();
                        //set the memstart to the first available RAM location
                        //and write the current disk value to that first location

                        memStart = RAM.getInstance().write_next(HardDrive.getInstance().getLoc(currentDisk));

                        //increment the counter since we wrote the first value
                        currentDisk++;
                        //set the memStart location in the PCB
                        PCB.getInstance().getJob(mCurrentJobId).setProc_memStart(memStart);
                        if (PCB.getInstance().getJob(mCurrentJobId).getNextInstruct() < 0)
                            PCB.getInstance().getJob(mCurrentJobId).setNextInstruct(memStart);
                        //determine the length of the instructions
                        programCount = PCB.getInstance().getJob(mCurrentJobId).getProc_iCount();

                        //loop through instruction and place in RAM
                        int instProcess = 0;
                        instProcess = (proc_start + programCount);

                        while (currentDisk < instProcess){
                            RAM.getInstance().write_next(HardDrive.getInstance().getLoc(currentDisk));
                            currentDisk++;
                        }

                        //now we read the data and write it (along with buffers)
                        //set start location on the disk for the data
                        currentDisk = PCB.getInstance().getJob(mCurrentJobId).getData_diskStart();

                        int data_start = PCB.getInstance().getJob(mCurrentJobId).getData_diskStart();
                        //set memStart location in PCB for the data
                        PCB.getInstance().getJob(mCurrentJobId).setData_memStart(memStart + programCount);
                        //read data and buffer info (to avoid delay from lookup each loop)
                        dataCount = PCB.getInstance().getJob(mCurrentJobId).getData_count();

                        tempBuffCount = PCB.getInstance().getJob(mCurrentJobId).getTempBuffer();

                        //loop through to read from disk and write data to RAM + buffers
                        // all of which appear in the data file (** in hex sizes ** )
                        int memLocations = dataCount - tempBuffCount;
                        //write the instructions to RAM
                        while (memLocations > 0){
                            RAM.getInstance().write_next(HardDrive.getInstance().getLoc(currentDisk));
                            currentDisk++;
                            memLocations--;
                        }

                        while ((tempBuffCount > 0) || ((tempBuffCount % 4) == 0)){
                            RAM.getInstance().write_next("00000000");
                            tempBuffCount--;
                        }
                        //if we are done, stop; else get next information

                        mCurrentJob = PCB.getInstance().nextProcess();
                        if(!(mCurrentJob == null) && !(PCB.getInstance().isDone())){
                            mCurrentJobId = mCurrentJob.getProc_id();
                            mNum = RAM.getInstance().get_next_loc();
                            doWork = ((mNum + temp.getProgInstructCount() + temp.getData_count() + temp.getInputBuffer() + temp.getOutputBuffer() + temp.getTempBuffer()) < RAM.getInstance().sizeOfRam());
                        }else
                            break;
                    }
                    //if we are done, decrement the incremented iterator
                    if (!PCB.getInstance().mDone)
                        PCB.getInstance().reduceIter();
                }
                else
                    break;
            }
            case SJF:
            {
                  //Shortest Job First


                break;
            }
            case PRIORITY:
            {
                break;
            }
        }

        return currentDisk;

    }//method

    public int shortTerm()
    {
        //read the id from the Queue and output
        int id = ReadyQueue.getInstance().getProcesses();
        //System.out.println(id + " ID gotten ****** ");


        //if we are done...
        if( id == -1)
        {
            //System.out.println(" ****** End of ShortTerm ******* ");//do something
        }
        else
        {
            //Dispatch the job / processes


        }

        return id;
    }
}
