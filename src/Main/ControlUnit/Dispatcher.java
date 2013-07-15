package Main.ControlUnit;

/**
 * Created with IntelliJ IDEA.
 * User: aV1rus
 * Date: 7/4/13
 * Time: 11:59 AM
 * To change this template use File | Settings | File Templates.
 */
import Main.Driver;
import Main.ProcessData.*;

public class Dispatcher
{
    private Dispatcher dispatch;
    protected Main.ProcessData.Process currentProc;
    private int currentInstr;
    private boolean term;

    private int runCPU;

    public Dispatcher(int i)
    {
        currentProc = null;
        currentInstr = -1;
        runCPU = i;
        term = false;
    }

    public void give_proc(int proc_id)
    {
        currentProc = PCB.getInstance().getJob(proc_id);
        PCB.getInstance().getJob(proc_id).setProcState(2);
        currentInstr = currentProc.getNextInstruct();
        term = false;
    }

    public int get()
    {
        if (Driver.contextSwitch && (MultiDispatch.getDispatch(runCPU).currentProc.getProcState() == 4))
        {
            WaitQueue.addItem(MultiDispatch.getDispatch(runCPU).currentProc.getProc_id(), MultiDispatch.getDispatch(runCPU).currentProc.getWaitType());
            return -1;
        }
        else
        {
            if(MultiDispatch.getDispatch(runCPU).currentProc == null)
            {
                throw new IllegalArgumentException();
            }
            else
            {
                int newPC = currentInstr;
                currentInstr++;
                return newPC;
            }
        }
    }

    protected void set(int newInstr)
    {
        int processStart = currentProc.getProc_memStart();
        int processEnd = processStart + currentProc.getProgInstructCount();
        if((newInstr >= processStart) && (newInstr <= processEnd))
            currentInstr = newInstr;
    }


    public boolean getTerminate()
    {
        return term;
    }

    protected void setTerminate()
    {
        term = true;
    }
}

