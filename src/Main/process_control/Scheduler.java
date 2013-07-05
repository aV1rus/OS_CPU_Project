package Main.process_control;

/**
 * Created with IntelliJ IDEA.
 * User: nick
 * Date: 7/2/13
 * Time: 05:52 PM
 */
import Main.memory.HardDrive;
import Main.memory.RAM;

//class def
public class Scheduler
{
    //variables used by this class
    public enum Algorithm {FIFO, SJF, PRIORITY};
    public enum Processors {SINGLE, MULTI};
    private static final Algorithm DEFAULT_ALGORITHM = Algorithm.FIFO;
    public static Processors DEFAULT_PROCESSORS = Processors.SINGLE;
    private static final int NULL = 0;
    private final Algorithm alg;
    private Processors proc;
    private Process theJob;
    private int currentJob;
    private static Scheduler sched;
    private int num;


    //constructer to build basic element
    private Scheduler()
    {
            this(DEFAULT_PROCESSORS);

    }
    private Scheduler(Processors processors){
        DEFAULT_PROCESSORS = processors;
        alg = DEFAULT_ALGORITHM;
        proc = DEFAULT_PROCESSORS;
        currentJob = 1;
    }
    //implement a synchronized instance of the scheduler
    public static synchronized Scheduler getInstance()
    {
        //if it does not exist, create; return
        if(sched == null)
        {
            sched = new Scheduler();
        }

        return sched;
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
        switch(alg)
        {
            case FIFO:
            {
                //is the scheduling done? if not...
                theJob = PCB.getInstance().nextProcess();
                if (!(theJob == null) && !(PCB.getInstance().isDone())){
                    //get the next process for the job and add it to the Ready Queue

                    //set the current job by retrieving from PCB
                    currentJob = theJob.getProc_id();
                    //get next available memory location
                    num = RAM.getInstance().get_next_loc();

                    //create temp object for current job
                    Process temp = PCB.getInstance().getJob(currentJob);

                    //while there are still processes, keep moving to RAM
                    boolean keepGoing = ((num + temp.getProgInstructCount() + temp.getData_count() + temp.getInputBuffer() + temp.getOutputBuffer() + temp.getTempBuffer()) < RAM.getInstance().sizeOfRam());

                    while (keepGoing)
                    {
                        //get the next job
                        ReadyQueue.getInstance().addProcess(currentJob);
                        //set the status of this job to ready for dispatch
                        PCB.getInstance().getJob(currentJob).setProcState(1);
                        //read job info (if we use direct calls this will slow the loop)
                        //currentDisk will hold the current position on the disk
                        currentDisk = PCB.getInstance().getJob(currentJob).getProc_diskStart();
                        //proc_start will hold the start location of the job on disk
                        int proc_start = PCB.getInstance().getJob(currentJob).getProc_diskStart();
                        //set the memstart to the first available RAM location
                        //and write the current disk value to that first location

                        memStart = RAM.getInstance().write_next(HardDrive.getInstance().getLoc(currentDisk));

                        //increment the counter since we wrote the first value
                        currentDisk++;
                        //set the memStart location in the PCB
                        PCB.getInstance().getJob(currentJob).setProc_memStart(memStart);
                        if (PCB.getInstance().getJob(currentJob).getNextInstruct() < 0)
                            PCB.getInstance().getJob(currentJob).setNextInstruct(memStart);
                        //determine the length of the instructions
                        programCount = PCB.getInstance().getJob(currentJob).getProc_iCount();

                        //loop through instruction and place in RAM
                        int instProcess = 0;
                        instProcess = (proc_start + programCount);

                        while (currentDisk < instProcess){
                            RAM.getInstance().write_next(HardDrive.getInstance().getLoc(currentDisk));
                            currentDisk++;
                        }

                        //now we read the data and write it (along with buffers)
                        //set start location on the disk for the data
                        currentDisk = PCB.getInstance().getJob(currentJob).getData_diskStart();

                        int data_start = PCB.getInstance().getJob(currentJob).getData_diskStart();
                        //set memStart location in PCB for the data
                        PCB.getInstance().getJob(currentJob).setData_memStart(memStart + programCount);
                        //read data and buffer info (to avoid delay from lookup each loop)
                        dataCount = PCB.getInstance().getJob(currentJob).getData_count();

                        //inBuffCount = PCB.getInstance().getJob(currentJob).getInputBuffer();
                        //outBuffCount = PCB.getInstance().getJob(currentJob).getOutputBuffer();

                        tempBuffCount = PCB.getInstance().getJob(currentJob).getTempBuffer();

                        //loop through to read from disk and write data to RAM + buffers
                        // all of which appear in the data file (** in hex sizes ** )
                        //while (currentDisk < (data_start + dataCount + inBuffCount + outBuffCount))
                        int memLocations = 0;
                        memLocations = dataCount - tempBuffCount;
                        //write the instructions to RAM
                        while (memLocations > 0){
                            //System.out.println("memLoc: " + memLocations);
                            RAM.getInstance().write_next(HardDrive.getInstance().getLoc(currentDisk));
                            currentDisk++;
                            memLocations--;
                        }

                        while ((tempBuffCount > 0) || ((tempBuffCount % 4) == 0)){
                            RAM.getInstance().write_next("00000000");
                            tempBuffCount--;
                        }

                        //if we are done, stop; else get next information

                        theJob = PCB.getInstance().nextProcess();
                        if(!(theJob == null) && !(PCB.getInstance().isDone())){
                            currentJob = theJob.getProc_id();
                            num = RAM.getInstance().get_next_loc();
                            keepGoing = ((num + temp.getProgInstructCount() + temp.getData_count() + temp.getInputBuffer() + temp.getOutputBuffer() + temp.getTempBuffer()) < RAM.getInstance().sizeOfRam());
//
                        }else
                            break;
                    }
                    //if we are done, decrement the incremented iterator
                    if (!PCB.getInstance().done)
                        PCB.getInstance().reduceIter();
                }
                else
                    break;
            }
            case SJF:
            {


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
        //read the id from the ReadyQueue and output
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
