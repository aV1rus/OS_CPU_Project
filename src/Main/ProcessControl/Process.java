package Main.ProcessControl;

/**
 * Created with IntelliJ IDEA.
 * User: aV1rus
 * Date: 7/1/13
 * Time: 11:50 PM
 *
 * Process Class:  This class represents a process within a virtual operating system.
 * This object will be utilized by the PCB (Program Control Block) to hold vital information
 * that will be used to determine efficiency and other metrics for later validation.
 */
import Main.Log.ErrorLog;
import Main.Memory.RAM;


/**
 *
 */
public class Process
{
    /*
    The CPU is supported by a PCB, which may have the following (suggested) structure:

    typedef struct PCB {
        cpuid:				 information the assigned CPU (for multiprocessor system)
        struct state:		// record of environment that is saved on interrupt
                            // including the pc, registers,
                             * buffers, -> Fake buffer containing the copy of what is currently in the buffer.
                             * caches, -> Fake cache representing the copy of what is in the cache.
                             * active
                            // pages/blocks
                             * code-size; (**All variables associated with process)
        struct registers:	// accumulators(CPU String value.. current value), index (Value needed to add to the process ID)
                            // general
        struct sched:		// burst-time(integer::job size) , priority, queue-type(String for scheduler)
                            //, time-slice, remain-time (burst - time = remaining for round robin)
        struct accounts:	// cpu-time, time-limit, time-delays (accumalative), start/end times(from global time units)
                            // io-times (how much time to request and get a service. Kernel time)
        struct memories:	// page-table-base, pages, page-size
                            // base-registers logical/physical map, limit-reg
        struct progeny:		// child-procid, child-code-pointers
        parent: ptr;		// pointer to parent (if this process is spawned, else null)
        status_info:		// pointer to ready-list of active processes or
                            // resource-list on blocked processes
        priority: integer;	// of the process
    }
     */


    private int proc_id;

    //Instruction Disk locations & length
    private int proc_diskStart;

    //Instruction Memory locations & length
    private int proc_memStart;

    //Given job instruction count by mid number in //JOB # # #
    private int proc_iCount;

    //Instruction RAM base-register
    private int proc_baseReg;

    //Data Disk start location.
    private int data_diskStart;

    //Data Memory start location.
    private int data_memStart;

    //Data Disk length
    private int data_size;

    //Given job priorty by last Hexidecimal in //JOB # # #
    private int jobPriority;

    //Number of words in each buffer.
    private int inputBuffer;
    private int outputBuffer;
    private int tempBuffer;

    //Program Status
    private enum Status{ ready, waiting, running, terminated, created };
    private int procState;

    private State state;

    public final static int PROCESS_READY = 0;
    public final static int PROCESS_WAIT = 1;
    public final static int PROCESS_RUN = 2;
    public final static int PROCESS_TERMINATE = 3;
    public final static int PROCESS_HOLD = 4;
    public final static int PROCESS_DEFAULT = 0;

    private int next_instruct;
    private int waitType; // 0 = none, 1 = IO, 2 = pageFault
    private int prog_instruct_count;
    private int ioInstructCount;
    private int waitTime;
    private int executionTime;
    private int faultCount;

    private static final int registerSize = 16;
    private static final int inBuffSize = 30;
    private static final int outBuffSize = 30;

    private int [] registers;
    private String [] inBuff;
    private String [] outBuff;
    private String [] tempBuff;

    private int [][] pageTable;

    /**
     * Default Constructor:: The method sets all object variables to values that
     * are invalid to any process within the scope of this project.
     */
    public Process()
    {
        proc_id = 0;
        proc_diskStart = -1;
        proc_memStart = -1;
        proc_iCount = 0;
        proc_baseReg = -1;
        jobPriority = 0;
        inputBuffer = -1;
        outputBuffer = -1;
        tempBuffer = -1;
        state = new State();
        procState = Process.PROCESS_DEFAULT;
        ioInstructCount = 0;
        executionTime = 0;
        waitTime = 0;
        next_instruct = -1;
        waitType = 0;
        faultCount = 0;

        registers = new int [registerSize];
        inBuff = new String [inBuffSize];
        outBuff = new String [outBuffSize];
        tempBuff = new String [inBuffSize];

        pageTable = new int [5][1];
    }

    public int getProc_id()
    {
        return proc_id;
    }

    public int getProc_diskStart()
    {
        return proc_diskStart;
    }

    public int getProc_memStart()
    {
        return proc_memStart;
    }

    public int getProc_iCount()
    {
        return proc_iCount;
    }

    public int getProc_baseReg()
    {
        return proc_baseReg;
    }

    public int getData_diskStart()
    {
        return data_diskStart;
    }

    public int getData_memStart()
    {
        return data_memStart;
    }

    public int getData_count()
    {
        return data_size;
    }

    public int getJobPriority()
    {
        return jobPriority;
    }

    public int getInputBuffer()
    {
        return inputBuffer;
    }

    public void getInBuff()
    {
        for (int i = 0; i < (getProc_memStart() - getProc_iCount()); i++)
        {
            RAM.getInstance().write_loc(inBuff[i],(getData_memStart() + getData_count() + i));
        }
    }

    public int getOutputBuffer()
    {
        return outputBuffer;
    }

    public void getOutBuff()
    {
        for (int i = 0; i < (getProc_memStart() - getProc_iCount()); i++)
        {
            RAM.getInstance().write_loc(outBuff[i],(getData_memStart() + getData_count() + getData_count() + i));
        }
    }

    public int getTempBuffer()
    {
        return tempBuffer;
    }

    public String [] getTempBuff()
    {
        return tempBuff;
    }

    public int [] getRegisters()
    {
        return registers;
    }

    public int getProcState()
    {
        return procState;
    }

    public int getProgInstructCount()
    {
        return prog_instruct_count;
    }

    public int getExecTime()
    {
        return executionTime;
    }

    public int getWaitTime()
    {
        return waitTime;
    }

    public int getIOCount()
    {
        return ioInstructCount;
    }

    public int getNextInstruct()
    {
        return next_instruct;
    }

    public int getWaitType()
    {
        return waitType;
    }

    public int getPageTable(int table)
    {
        return pageTable[table][0];
    }

    public int getFaultCount()
    {
        return faultCount;
    }

    public void getFinalData()
    {
        int totalProcs = 0;
        int totalWait = 0;
        int totalExecute = 0;
        int totalInstructions = 0;
        int totalIO = 0;
        int totalFault = 0;

        for (int i = 1; i < PCB.getInstance().lastJob(); i++)
        {
            totalProcs++;
            totalWait+=PCB.getInstance().getJob(i).getWaitTime();
            totalExecute+=PCB.getInstance().getJob(i).getExecTime();
            totalInstructions+=PCB.getInstance().getJob(i).getProc_iCount();
            totalIO+=PCB.getInstance().getJob(i).getIOCount();
            totalFault+=PCB.getInstance().getJob(i).getFaultCount();
        }
        System.out.format("%15s%5s%15s%5s%15s%5s%15s%5s%15s%5s%15s%5s",
                "- - - - -",
                "",
                "AVERAGE:",
                (totalWait / totalProcs),
                "",
                ""+(totalExecute / totalProcs),
                "",
                ""+(totalInstructions / totalProcs),
                "",
                ""+(totalIO / totalProcs),
                "",
                ""+(totalFault / totalProcs));

    }

    public void setFaultCount()
    {
        faultCount++;
    }

    public void setPageTable(int table, int value)
    {
        pageTable[table][0] = value;
    }

    public void setProc_id(int id)
    {
        if(id > 0)
        {
            proc_id = id;
        }
        else
        {
            ErrorLog.getInstance().writeError("Process.setProc_id(int) || >> ID # < 1");
            throw new IllegalArgumentException();
        }
    }

    public void setProc_diskStart(int location)
    {
        if (location >= 0)
        {
            proc_diskStart = location;
        }
        else
        {
            ErrorLog.getInstance().writeError("Process.setProc_diskStart(int) >> Disk location invalid");
            throw new IllegalArgumentException();
        }
    }

    public void setProc_memStart(int location)
    {
        if( location >= 0)
        {
            proc_memStart = location;
        }
        else
        {
            ErrorLog.getInstance().writeError("Process.setProc_memStart(int) >> Memory location invalid");
            throw new IllegalArgumentException();
        }
    }

    public void setProc_iCount(int count)
    {
        if( count > 0)
        {
            proc_iCount = count;
        }
        else
        {
            ErrorLog.getInstance().writeError("Process.setProc_iCount(int) >> Instruction count input < 1");
            throw new IllegalArgumentException();
        }
    }

    public void setProc_baseReg(int reg)
    {
        if(reg >= 0)
        {
            proc_baseReg = reg;
        }
        else
        {
            ErrorLog.getInstance().writeError("Process.setProc_baseReg(int) >> Base Register number invalid.  reg < 0" );
            throw new IllegalArgumentException();
        }
    }

    public void setData_diskStart(int diskStart)
    {
        if (diskStart >= 0)
        {
            data_diskStart = diskStart;
        }
        else
        {
            ErrorLog.getInstance().writeError("Process.setData_diskStart(int) >> Disk Start invalid.  diskStart < 0");
            throw new IllegalArgumentException();
        }
    }

    public void setData_memStart(int memStart)
    {
        if(memStart >= 0)
        {
            data_memStart = memStart;
        }
        else
        {
            ErrorLog.getInstance().writeError("Process.setData_memStart(int) >> Invalid.  memStart < 0" );
            throw new IllegalArgumentException();
        }
    }

    public void setData_size(int size)
    {
        if(size > 0)
        {
            data_size = size;
        }
        else
        {
            ErrorLog.getInstance().writeError("Process.setData_size >> Size input parameter is invalid");
            throw new IllegalArgumentException();
        }
    }

    public void setJobPriority(int priority)
    {
        if(priority > 0)
        {
            jobPriority = priority;
        }
        else
        {
            ErrorLog.getInstance().writeError("Process.setJobPriority(int) >> priority < 1");
            throw new IllegalArgumentException();
        }
    }

    public void setInputBuffer(int buff)
    {
        if(buff >= 0)
        {
            inputBuffer = buff;
        }
        else
        {
            ErrorLog.getInstance().writeError("Process.setInputbuffer(int) >> buff < 0");
            throw new IllegalArgumentException();
        }
    }

    public void setInBuff()
    {
        int count = 0;
        System.out.println(proc_id + " :: Mem:" + getData_memStart() + " Data:" + getData_count() + " iBuff:" + getInputBuffer());
        for (int i = (getData_memStart() + getData_count()); i < (getData_memStart() + getData_count() + getInputBuffer()); i++)
        {
            System.out.println(proc_id + " :: i:" + i + " count:" + count + " Loc:" + (getData_memStart() - getData_count()));
            inBuff[count] = RAM.getInstance().read(i);
            count++;
        }
    }

    public void setOutputBuffer(int outBuff)
    {
        if(outBuff >= 0)
        {
            outputBuffer = outBuff;
        }
        else
        {
            ErrorLog.getInstance().writeError("Process.setOutputBuffer(int) >> outBuff < 0");
            throw new IllegalArgumentException();
        }
    }

    public void setOutBuff()
    {
        for (int i = 0; i < (getProc_memStart() - getProc_iCount()); i++)
        {
            outBuff[i] = RAM.getInstance().read(getData_memStart() + getData_count() + getData_count() + i);
        }
    }

    public void setTempBuffer(int tempBuff)
    {
        if(tempBuff >= 0)
        {
            tempBuffer = tempBuff;
        }
        else
        {
            ErrorLog.getInstance().writeError("Process.setOutputBuffer(int) || >> tempBuff < 0");
            throw new IllegalArgumentException();
        }
    }

    public void setTempBuff(String[] temp)
    {
        tempBuff = temp;
    }

    public void setRegisters(int [] temp)
    {
        registers = temp;
    }

    public void setProcState(int state)
    {
        if(0 <= state && state <= 5)
        {
            //System.out.println("Proc State change: " + state);
            procState = state;
        }
        else
        {
            ErrorLog.getInstance().writeError("ProcessData.setProcState(int) >> Invalid. 0 <= X <= 4");
            throw new IllegalArgumentException();
        }
    }

    public int setProgInstructCount(int i)
    {
        if(i > 0)
        {
            prog_instruct_count = i;
            return prog_instruct_count;
        }
        else
        {
            ErrorLog.getInstance().writeError("Process.setProgInstructCount >> Invalid");
            throw new IllegalArgumentException();
        }
    }

    public State getState()
    {
        return state;
    }

    public String getProcessState()
    {
        switch(procState)
        {
            case(0):
                return "Ready";
            case(1):
                return "Waiting";
            case(2):
                return "Running";
            case(3):
                return "Terminated";
            case(4):
                return "Undefined Process";
            default:
                return null;
        }
    }

    public void setWaitType(int wait)
    {
        waitType = wait;
    }

    public void setExecTime(int cost)
    {
        executionTime += cost;
    }

    public void setWaitTime(int waitcost)
    {
        if (waitcost == 0)
            waitTime = 0;
        else
            waitTime += waitcost;
    }

    public void setIOCount(int ioCost)
    {
        ioInstructCount+= ioCost;
    }

    public void setNextInstruct(int nextInstr)
    {
        next_instruct = nextInstr;
    }

    public String toString()
    {
        String temp = "Process Job:";
        temp += "\nPid: " + proc_id;
        temp += "\nP_diskStart: " + proc_diskStart;
        temp += "\nP_memStart: " + proc_memStart;
        temp += "\nP_iCount: " + proc_iCount;
        temp += "\nP_baseReg: " + proc_baseReg;
        temp += "\nd_diskStart: " + data_diskStart;
        temp += "\nd_memStart: " + data_memStart;
        temp += "\nd_size: " + data_size;
        temp += "\nJob_Priority: " + jobPriority;
        temp += "\nInput_buff: " + inputBuffer;
        temp += "\nOutput_Buff: " + outputBuffer;
        temp += "\ntemp_buff: " + tempBuffer;

        return temp;
    }
}

