package Main.Memory;

/**
 * Created with IntelliJ IDEA.
 * User: nick
 * Date: 7/2/13
 * Time: 09:46 AM
 *
 *
 * Memory Management Unit
 */
import Main.ProcessControl.PCB;
import Main.ProcessControl.ReadyQueue;

public class MemManager
{
    private static frame [] frameTable;

    private static int processesServed = 1;
    private static int startProcess = 1;

    private static int currProc = -1;
    private static int currBuff = -1;

    public MemManager()
    {
        frameTable = new frame [(PCB.getInstance().lastJob()) + 1];


        for (int i = 0; i < (frameTable.length); i++)
        {
            frameTable[i] = new frame();

        }
    }

    public void makeMMU()
    {
        currProc = -1;
        currBuff = -1;
        startProcess = processesServed;

        for (int i = startProcess; i < (ReadyQueue.getInstance().getSize() + 1); i++)
        {
            processesServed++;
            frameTable[i].owner = PCB.getInstance().getJob(i).getProc_id();
            frameTable [i].number = i;

            for (int j = 0; j < 5; j++)
                PCB.getInstance().getJob(frameTable[i].owner).setPageTable(j, 0);


            for (int k = 0; k < 4; k++)
            {
                frameTable [i].buffer[0].value[k] = RAM.getInstance().DMAread(PCB.getInstance().getJob(i).getProc_memStart() + k);
                frameTable [i].buffer[1].value[k] = RAM.getInstance().DMAread(PCB.getInstance().getJob(i).getData_memStart() + k);
                frameTable [i].buffer[2].value[k] = RAM.getInstance().DMAread(PCB.getInstance().getJob(i).getData_memStart() + (PCB.getInstance().getJob(i).getData_count() - (PCB.getInstance().getJob(i).getInputBuffer() + PCB.getInstance().getJob(i).getOutputBuffer())) + PCB.getInstance().getJob(i).getTempBuffer() + k);
                frameTable [i].buffer[3].value[k] = RAM.getInstance().DMAread(PCB.getInstance().getJob(i).getData_memStart() + (PCB.getInstance().getJob(i).getData_count() - (PCB.getInstance().getJob(i).getOutputBuffer() + PCB.getInstance().getJob(i).getTempBuffer())) + k);
                frameTable [i].buffer[4].value[k] = RAM.getInstance().DMAread(PCB.getInstance().getJob(i).getData_memStart() + (PCB.getInstance().getJob(i).getData_count() - (PCB.getInstance().getJob(i).getTempBuffer())) + k);

            }
        }
    }


    public static String get(int loc, int d, int p)
    {
        findProc(d,p);
        findBuff(currProc,d,p);

        if (valid(d))
        {
            return frameTable[currProc].buffer[currBuff].value[p];
        }
        else
        {
            pageFault(d,p);
            return "page fault";
        }
    }

    public static void write(String data, int d, int p)
    {
        findProc(d,p);
        findBuff(currProc,d,p);

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
        int count = startProcess;

        while (!found)
        {

            if (count > frameTable.length)
                found = false;
            else
            {
                currProc = frameTable[count].owner;
                if (frameTable[count].equals(null))
                {
                    found = true;

                }
                if (((PCB.getInstance().getJob(frameTable[count].number).getProc_memStart()) <= (d*4+p)) && ((PCB.getInstance().getJob(frameTable[count].number).getData_memStart() + PCB.getInstance().getJob(frameTable[count].number).getData_count()) > (d*4+p)))
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

        if (PCB.getInstance().getJob(currProc).getData_memStart() > (d*4+p))
            currBuff = 0;
        else if ((PCB.getInstance().getJob(currProc).getData_memStart() + (PCB.getInstance().getJob(currProc).getData_count() - PCB.getInstance().getJob(currProc).getInputBuffer() - PCB.getInstance().getJob(currProc).getOutputBuffer() - PCB.getInstance().getJob(currProc).getTempBuffer())) > (d*4+p))
            currBuff = 1;
        else if ((PCB.getInstance().getJob(currProc).getData_memStart() + (PCB.getInstance().getJob(currProc).getData_count() - PCB.getInstance().getJob(currProc).getOutputBuffer() - PCB.getInstance().getJob(currProc).getTempBuffer())) > (d*4+p))
            currBuff = 2;
        else if ((PCB.getInstance().getJob(currProc).getData_memStart() + (PCB.getInstance().getJob(currProc).getData_count() - PCB.getInstance().getJob(currProc).getTempBuffer())) > (d*4+p))
            currBuff = 3;
        else
            currBuff = 4;


    }

    public static boolean valid(int d)
    {
        if ((d) == PCB.getInstance().getJob(currProc).getPageTable(currBuff))
            return true;
        else
            return false;
    }

    public static void pageFault(int d, int p)
    {
        PCB.getInstance().getJob(currProc).setProcState(4);
        PCB.getInstance().getJob(currProc).setFaultCount();
        MemManager.pageSwitch(d);
        PCB.getInstance().getJob(currProc).setPageTable(currBuff, (d));

    }

    public static void pageSwitch(int page)
    {
        int loc = -1;

        if (currBuff == 0)
            loc = PCB.getInstance().getJob(currProc).getProc_memStart();
        else if (currBuff == 1)
            loc = PCB.getInstance().getJob(currProc).getData_memStart();
        else if (currBuff == 2)
            loc = PCB.getInstance().getJob(currProc).getData_memStart() + (PCB.getInstance().getJob(currProc).getData_count() - PCB.getInstance().getJob(currProc).getInputBuffer() - PCB.getInstance().getJob(currProc).getOutputBuffer() - PCB.getInstance().getJob(currProc).getTempBuffer());
        else if (currBuff == 3)
            loc = PCB.getInstance().getJob(currProc).getData_memStart() + (PCB.getInstance().getJob(currProc).getData_count() - PCB.getInstance().getJob(currProc).getOutputBuffer() - PCB.getInstance().getJob(currProc).getTempBuffer());
        else if (currBuff == 4)
            loc = PCB.getInstance().getJob(currProc).getData_memStart() + (PCB.getInstance().getJob(currProc).getData_count() - PCB.getInstance().getJob(currProc).getTempBuffer());

        for (int i = 0; i < 4; i++)
        {
            if (currProc > 1)
                loc = 0;
            frameTable[currProc].buffer[currBuff].value[i] = RAM.getInstance().DMAread(loc + (page * 4)+ i);
        }
    }

    public static void out()
    {
        String returnValue = "";

        for (int i = startProcess; i < processesServed; i++)
            for (int j = 0; j < 5; j++ )
                for (int k = 0; k < 4; k++)
                    returnValue+=("FrameTable: Proc " + i + " : Buff " + j + " : frame " + k + " = " + frameTable[i].buffer[j].value[k] + "\n");

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



