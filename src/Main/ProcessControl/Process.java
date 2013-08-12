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


public class Process
{
    private int mProcessId;
    private int mProcessDiskStart;      //Instruction Disk locations & length
    private int mProcessMemoryStart;    //Instruction Memory locations & length
    private int mProcessCount;          //Given job instruction count by mid number in //JOB # # #
    private int mProcessBaseRegister;   //Instruction RAM base-register
    private int mDataDiskStart;         //Data Disk start location.
    private int mDataMemoryStart;       //Data Memory start location.
    private int mDataSize;              //Data Disk length
    private int mJobPriority;           //Given job priorty by last Hexidecimal in //JOB # # #
    private int mInputBufferWC;           //Number of words in each buffer.
    private int mOutputBufferWC;
    private int mTempBufferWC;
    private int mProcessorState;
    private int mNextInstruction;
    private int mWaitType;               // 0 = none, 1 = IO, 2 = pageFault
    private int mIOInstructCount;
    private int mWaitTime;
    private int mExecutionTime;
    private int mFaultCount;

    public final static int DEFAULT_PROCESS = 0;
    private static final int mRegisterSize = 16;
    private static final int mInputBufferSize = 30;
    private static final int mOutputBufferSize = 30;

    private int [] mRegisters;
    private String [] inBuff;
    private String [] mOutputBuffer;
    private String [] mTempBuffer;

    private int [][] mPageTable;

    /**
     * Default Constructor:: The method sets all object variables to values that
     * are invalid to any process within the scope of this project.
     */
    public Process()
    {
        mProcessId = 0;
        mProcessDiskStart = -1;
        mProcessMemoryStart = -1;
        mProcessCount = 0;
        mProcessBaseRegister = -1;
        mJobPriority = 0;
        mInputBufferWC = -1;
        mOutputBufferWC = -1;
        mTempBufferWC = -1;
        mProcessorState = Process.DEFAULT_PROCESS;
        mIOInstructCount = 0;
        mExecutionTime = 0;
        mWaitTime = 0;
        mNextInstruction = -1;
        mWaitType = 0;
        mFaultCount = 0;

        mRegisters = new int [mRegisterSize];
        inBuff = new String [mInputBufferSize];
        mOutputBuffer = new String [mOutputBufferSize];
        mTempBuffer = new String [mInputBufferSize];

        mPageTable = new int [5][1];
    }

    public int getProc_id()
    {
        return mProcessId;
    }

    public int getProc_diskStart()
    {
        return mProcessDiskStart;
    }

    public int getProc_memStart()
    {
        return mProcessMemoryStart;
    }

    public int getProc_iCount()
    {
        return mProcessCount;
    }

    public int getData_diskStart()
    {
        return mDataDiskStart;
    }

    public int getData_memStart()
    {
        return mDataMemoryStart;
    }

    public int getData_count()
    {
        return mDataSize;
    }

    public int getJobPriority()
    {
        return mJobPriority;
    }

    public int getInputBuffer()
    {
        return mInputBufferWC;
    }

    public int getOutputBuffer()
    {
        return mOutputBufferWC;
    }

    public int getTempBuffer()
    {
        return mTempBufferWC;
    }

    public int [] getRegisters()
    {
        return mRegisters;
    }

    public int getProcState()
    {
        return mProcessorState;
    }

    public int getProgInstructCount()
    {
        return mProcessorState;
    }

    public int getExecTime()
    {
        return mExecutionTime;
    }

    public int getWaitTime()
    {
        return mWaitTime;
    }

    public int getIOCount()
    {
        return mIOInstructCount;
    }

    public int getNextInstruct()
    {
        return mNextInstruction;
    }

    public int getWaitType()
    {
        return mWaitType;
    }

    public int getPageTable(int table)
    {
        return mPageTable[table][0];
    }

    public int getFaultCount()
    {
        return mFaultCount;
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
                "AVERAGE:", (totalWait / totalProcs),
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
        mFaultCount++;
    }

    public void setPageTable(int table, int value)
    {
        mPageTable[table][0] = value;
    }

    public void setProc_id(int id)
    {
        if(id > 0) {
            mProcessId = id;
        }else{
            ErrorLog.getInstance().writeError("Process.setProc_id(int) || >> ID # < 1");
            throw new IllegalArgumentException();
        }
    }

    public void setProc_diskStart(int location)
    {
        if (location >= 0){
            mProcessDiskStart = location;
        }else{
            ErrorLog.getInstance().writeError("Process.setProc_diskStart(int) >> Disk location invalid");
            throw new IllegalArgumentException();
        }
    }

    public void setProc_memStart(int location)
    {
        if( location >= 0){
            mProcessMemoryStart = location;
        }else{
            ErrorLog.getInstance().writeError("Process.setProc_memStart(int) >> Memory location invalid");
            throw new IllegalArgumentException();
        }
    }

    public void setProc_iCount(int count)
    {
        if( count > 0){
            mProcessCount = count;
        }else{
            ErrorLog.getInstance().writeError("Process.setProc_iCount(int) >> Instruction count input < 1");
            throw new IllegalArgumentException();
        }
    }


    public void setData_diskStart(int diskStart)
    {
        if (diskStart >= 0){
            mDataDiskStart = diskStart;
        }else{
            ErrorLog.getInstance().writeError("Process.setData_diskStart(int) >> Disk Start invalid.  diskStart < 0");
            throw new IllegalArgumentException();
        }
    }

    public void setData_memStart(int memStart)
    {
        if(memStart >= 0) {
            mDataMemoryStart = memStart;
        }else {
            ErrorLog.getInstance().writeError("Process.setData_memStart(int) >> Invalid.  memStart < 0" );
            throw new IllegalArgumentException();
        }
    }

    public void setData_size(int size)
    {
        if(size > 0){
            mDataSize = size;
        }else{
            ErrorLog.getInstance().writeError("Process.setData_size >> Size input parameter is invalid");
            throw new IllegalArgumentException();
        }
    }

    public void setJobPriority(int priority)
    {
        if(priority > 0){
            mJobPriority = priority;
        }else{
            ErrorLog.getInstance().writeError("Process.setJobPriority(int) >> priority < 1");
            throw new IllegalArgumentException();
        }
    }

    public void setInputBuffer(int buff)
    {
        if(buff >= 0){
            mInputBufferWC = buff;
        }else{
            ErrorLog.getInstance().writeError("Process.setInputbuffer(int) >> buff < 0");
            throw new IllegalArgumentException();
        }
    }

    public void setInBuff()
    {
        int count = 0;
        System.out.println(mProcessId + " :: Mem:" + getData_memStart() + " Data:" + getData_count() + " iBuff:" + getInputBuffer());
        for (int i = (getData_memStart() + getData_count()); i < (getData_memStart() + getData_count() + getInputBuffer()); i++)
        {
            System.out.println(mProcessId + " :: i:" + i + " count:" + count + " Loc:" + (getData_memStart() - getData_count()));
            inBuff[count] = RAM.getInstance().read(i);
            count++;
        }
    }

    public void setOutputBuffer(int outBuff)
    {
        if(outBuff >= 0){
            mOutputBufferWC = outBuff;
        }else{
            ErrorLog.getInstance().writeError("Process.setOutputBuffer(int) >> outBuff < 0");
            throw new IllegalArgumentException();
        }
    }

    public void setTempBuffer(int tempBuff)
    {
        if(tempBuff >= 0) {
            mTempBufferWC = tempBuff;
        }else{
            ErrorLog.getInstance().writeError("Process.setOutputBuffer(int) || >> tempBuff < 0");
            throw new IllegalArgumentException();
        }
    }

    public void setRegisters(int [] temp)
    {
        mRegisters = temp;
    }

    public void setProcState(int state)
    {
        if(0 <= state && state <= 5)
            mProcessorState = state;
        else{
            ErrorLog.getInstance().writeError("ProcessData.setProcState(int) >> Invalid. 0 <= X <= 4");
            throw new IllegalArgumentException();
        }
    }

    public void setExecTime(int cost)
    {
        mExecutionTime += cost;
    }

    public void setWaitTime(int waitcost)
    {
        if (waitcost == 0)
            mWaitTime = 0;
        else
            mWaitTime += waitcost;
    }

    public void setIOCount(int ioCost)
    {
        mIOInstructCount+= ioCost;
    }

    public void setNextInstruct(int nextInstr)
    {
        mNextInstruction = nextInstr;
    }

    public String toString()
    {
        String temp = "Process Job:";
        temp += "\nProcessId: " + mProcessId;
        temp += "\nProcessDiskStart: " + mProcessDiskStart;
        temp += "\nProcessMemoryStart: " + mProcessMemoryStart;
        temp += "\nProcessCount: " + mProcessCount;
        temp += "\nProcessBaseReg: " + mProcessBaseRegister;
        temp += "\nDataDiskStart: " + mDataDiskStart;
        temp += "\nDataMemoryStart: " + mDataMemoryStart;
        temp += "\nDataSize: " + mDataSize;
        temp += "\nJobPriority: " + mJobPriority;
        temp += "\nInputBuffer: " + mInputBufferWC;
        temp += "\nOutputBuffer: " + mOutputBufferWC;
        temp += "\nTemporaryBuffer: " + mTempBufferWC;

        return temp;
    }
}

