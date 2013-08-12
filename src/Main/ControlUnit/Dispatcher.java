package Main.ControlUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Nick Maiello (aV1rus)
 * Date: 7/4/13
 * Time: 11:59 AM
 * To change this template use File | Settings | File Templates.
 */
import Main.Driver;
import Main.ProcessControl.*;

public class Dispatcher
{
    protected Main.ProcessControl.Process mCurrentProc;
    private int mCurrentInstr;
    private boolean mTerm;

    private int mRunCPU;

    public Dispatcher(int i)
    {
        mCurrentProc = null;
        mCurrentInstr = -1;
        mRunCPU = i;
        mTerm = false;
    }

    public void give_proc(int proc_id)
    {
        mCurrentProc = PCB.getInstance().getJob(proc_id);
        PCB.getInstance().getJob(proc_id).setProcState(2);
        mCurrentInstr = mCurrentProc.getNextInstruct();
        mTerm = false;
    }

    public int get()
    {
        if (Driver.contextSwitch && (Dispatch.getDispatch(mRunCPU).mCurrentProc.getProcState() == 4))
        {
            WaitQueue.addItem(Dispatch.getDispatch(mRunCPU).mCurrentProc.getProc_id(), Dispatch.getDispatch(mRunCPU).mCurrentProc.getWaitType());
            return -1;
        }
        else
        {
            if(Dispatch.getDispatch(mRunCPU).mCurrentProc == null)
            {
                throw new IllegalArgumentException();
            }
            else
            {
                int newPC = mCurrentInstr;
                mCurrentInstr++;
                return newPC;
            }
        }
    }

    protected void set(int newInstr)
    {
        int processStart = mCurrentProc.getProc_memStart();
        int processEnd = processStart + mCurrentProc.getProgInstructCount();
        if((newInstr >= processStart) && (newInstr <= processEnd))
            mCurrentInstr = newInstr;
    }


    public boolean getTerminate()
    {
        return mTerm;
    }

    protected void setTerminate()
    {
        mTerm = true;
    }
}

