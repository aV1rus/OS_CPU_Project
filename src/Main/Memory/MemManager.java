package Main.Memory;

/**
 * Created with IntelliJ IDEA.
 * User: aV1rus
 * Date: 7/2/13
 * Time: 09:46 AM
 *
 *
 */
import Main.Constants.Constants;
import Main.ProcessControl.PCB;
import Main.ProcessControl.ReadyQueue;

public class MemManager
{
    private static frame [] mFrameTable;
    private static int mProcessesServed = 1;
    private static int mStartProcess = 1;
    private static int mCurrentProcess = -1;
    private static int mCurrentBuffer = -1;

    public MemManager()
    {
        mFrameTable = new frame [(PCB.getInstance().lastJob()) + 1];


        for (int i = 0; i < (mFrameTable.length); i++)
        {
            mFrameTable[i] = new frame();

        }
    }

    public void makeMMU()
    {
        mCurrentProcess = -1;
        mCurrentBuffer = -1;
        mStartProcess = mProcessesServed;

        for (int i = mStartProcess; i < (ReadyQueue.getInstance().getSize() + 1); i++)
        {
            mProcessesServed++;
            mFrameTable[i].owner = PCB.getInstance().getJob(i).getProc_id();
            mFrameTable [i].number = i;

            for (int j = 0; j < 5; j++)
                PCB.getInstance().getJob(mFrameTable[i].owner).setPageTable(j, 0);


            for (int k = 0; k < 4; k++)
            {
                mFrameTable [i].buffer[0].value[k] = RAM.getInstance().DMAread(PCB.getInstance().getJob(i).getProc_memStart() + k);
                mFrameTable [i].buffer[1].value[k] = RAM.getInstance().DMAread(PCB.getInstance().getJob(i).getData_memStart() + k);
                mFrameTable [i].buffer[2].value[k] = RAM.getInstance().DMAread(PCB.getInstance().getJob(i).getData_memStart() + (PCB.getInstance().getJob(i).getData_count() - (PCB.getInstance().getJob(i).getInputBuffer() + PCB.getInstance().getJob(i).getOutputBuffer())) + PCB.getInstance().getJob(i).getTempBuffer() + k);
                mFrameTable [i].buffer[3].value[k] = RAM.getInstance().DMAread(PCB.getInstance().getJob(i).getData_memStart() + (PCB.getInstance().getJob(i).getData_count() - (PCB.getInstance().getJob(i).getOutputBuffer() + PCB.getInstance().getJob(i).getTempBuffer())) + k);
                mFrameTable [i].buffer[4].value[k] = RAM.getInstance().DMAread(PCB.getInstance().getJob(i).getData_memStart() + (PCB.getInstance().getJob(i).getData_count() - (PCB.getInstance().getJob(i).getTempBuffer())) + k);

            }
        }
    }


    public static String get(int loc, int d, int p)
    {
        findProc(d,p);
        findBuff(mCurrentProcess,d,p);

        if (valid(d))
        {
            return mFrameTable[mCurrentProcess].buffer[mCurrentBuffer].value[p];
        }
        else
        {
            pageFault(d,p);
            return Constants.PAGE_FAULT;
        }
    }

    public static void write(String data, int d, int p)
    {
        findProc(d,p);
        findBuff(mCurrentProcess,d,p);

        if (valid(d))
        {
            RAM.getInstance().DMAwrite(data, (d*4+p));
        }
        else
            pageFault(d,p);
    }

    private static void findProc(int d, int p)
    {

        boolean found = false;
        int count = mStartProcess;

        while (!found)
        {

            if (count > mFrameTable.length)
                found = false;
            else
            {
                mCurrentProcess = mFrameTable[count].owner;
                if (mFrameTable[count].equals(null))
                {
                    found = true;

                }
                if (((PCB.getInstance().getJob(mFrameTable[count].number).getProc_memStart()) <= (d*4+p)) && ((PCB.getInstance().getJob(mFrameTable[count].number).getData_memStart() + PCB.getInstance().getJob(mFrameTable[count].number).getData_count()) > (d*4+p)))
                {
                    found = true;
                }
                else
                    count++;
            }
        }
    }

    private static void findBuff( int proc, int d, int p)
    {

        if (PCB.getInstance().getJob(mCurrentProcess).getData_memStart() > (d*4+p))
            mCurrentBuffer = 0;
        else if ((PCB.getInstance().getJob(mCurrentProcess).getData_memStart() + (PCB.getInstance().getJob(mCurrentProcess).getData_count() - PCB.getInstance().getJob(mCurrentProcess).getInputBuffer() - PCB.getInstance().getJob(mCurrentProcess).getOutputBuffer() - PCB.getInstance().getJob(mCurrentProcess).getTempBuffer())) > (d*4+p))
            mCurrentBuffer = 1;
        else if ((PCB.getInstance().getJob(mCurrentProcess).getData_memStart() + (PCB.getInstance().getJob(mCurrentProcess).getData_count() - PCB.getInstance().getJob(mCurrentProcess).getOutputBuffer() - PCB.getInstance().getJob(mCurrentProcess).getTempBuffer())) > (d*4+p))
            mCurrentBuffer = 2;
        else if ((PCB.getInstance().getJob(mCurrentProcess).getData_memStart() + (PCB.getInstance().getJob(mCurrentProcess).getData_count() - PCB.getInstance().getJob(mCurrentProcess).getTempBuffer())) > (d*4+p))
            mCurrentBuffer = 3;
        else
            mCurrentBuffer = 4;


    }

    public static boolean valid(int d)
    {
        if ((d) == PCB.getInstance().getJob(mCurrentProcess).getPageTable(mCurrentBuffer))
            return true;
        else
            return false;
    }

    public static void pageFault(int d, int p)
    {
        PCB.getInstance().getJob(mCurrentProcess).setProcState(4);
        PCB.getInstance().getJob(mCurrentProcess).setFaultCount();
        MemManager.pageSwitch(d);
        PCB.getInstance().getJob(mCurrentProcess).setPageTable(mCurrentBuffer, (d));

    }

    public static void pageSwitch(int page)
    {
        int loc = -1;

        if (mCurrentBuffer == 0)
            loc = PCB.getInstance().getJob(mCurrentProcess).getProc_memStart();
        else if (mCurrentBuffer == 1)
            loc = PCB.getInstance().getJob(mCurrentProcess).getData_memStart();
        else if (mCurrentBuffer == 2)
            loc = PCB.getInstance().getJob(mCurrentProcess).getData_memStart() + (PCB.getInstance().getJob(mCurrentProcess).getData_count() - PCB.getInstance().getJob(mCurrentProcess).getInputBuffer() - PCB.getInstance().getJob(mCurrentProcess).getOutputBuffer() - PCB.getInstance().getJob(mCurrentProcess).getTempBuffer());
        else if (mCurrentBuffer == 3)
            loc = PCB.getInstance().getJob(mCurrentProcess).getData_memStart() + (PCB.getInstance().getJob(mCurrentProcess).getData_count() - PCB.getInstance().getJob(mCurrentProcess).getOutputBuffer() - PCB.getInstance().getJob(mCurrentProcess).getTempBuffer());
        else if (mCurrentBuffer == 4)
            loc = PCB.getInstance().getJob(mCurrentProcess).getData_memStart() + (PCB.getInstance().getJob(mCurrentProcess).getData_count() - PCB.getInstance().getJob(mCurrentProcess).getTempBuffer());

        for (int i = 0; i < 4; i++)
        {
            if (mCurrentProcess > 1)
                loc = 0;
            mFrameTable[mCurrentProcess].buffer[mCurrentBuffer].value[i] = RAM.getInstance().DMAread(loc + (page * 4)+ i);
        }
    }

    public static void out()
    {
        String returnValue = "";

        for (int i = mStartProcess; i < mProcessesServed; i++)
            for (int j = 0; j < 5; j++ )
                for (int k = 0; k < 4; k++)
                    returnValue+=("FrameTable: Proc " + i + " : Buff " + j + " : frame " + k + " = " + mFrameTable[i].buffer[j].value[k] + "\n");

        System.out.println(returnValue);
    }

    private static class frame
    {
        int owner;
        int number;
        dataFields [] buffer;

        private frame()
        {
            owner = -1;
            number = -1;
            buffer = new dataFields [5];

            for (int i = 0; i < 5; i++)
            {
                buffer[i] = new dataFields();
            }
        }
    }

    private static class dataFields
    {
        String [] value;

        private dataFields()
        {
            value = new String [4];
        }
    }
}



