package Main.Memory;

/**
 * Created with IntelliJ IDEA.
 * User: aV1rus
 * Date: 7/2/13
 * Time: 10:46 PM
 * To change this template use File | Settings | File Templates.
 */
import Main.Driver;
import Main.Log.ErrorLog;

public class RAM
{
    private String [] ram;
    private int next_loc;

    private static RAM ram_obj;

    private RAM()
    {
        ram = new String[Driver.amtOfRAM];
        next_loc = 0;
    }

    public synchronized static RAM getInstance()
    {
        if(ram_obj == null){
            ram_obj = new RAM();
        }
        return ram_obj;
    }

    public String read(int loc)
    {
        if(loc >= 0 && loc < Driver.amtOfRAM)
        {
            if (!Driver.contextSwitch)
                return ram[loc];
            else
            {
                int d = (loc / 4);
                int p = (loc % 4);
                return MemManager.get(loc, d, p);
            }
        }
        else
        {
            ErrorLog.getInstance().writeError("RAM::read || >> Invalid parameter given.");
            throw new IllegalArgumentException();
        }
    }

    public String DMAread(int loc)
    {

        if(loc >= 0 && loc < Driver.amtOfRAM)
        {
            return ram[loc];
        }
        else
        {
            ErrorLog.getInstance().writeError("RAM::read || >> Invalid parameter given.");
            throw new IllegalArgumentException();
        }
    }

    public void write_loc(String data, int loc)
    {
        if( data != null)
        {
            if (Driver.contextSwitch)
            {
                MemManager.write(data, (loc / 4), (loc % 4));
            }
            else
            {
                if(loc >= 0 && loc < Driver.amtOfRAM)
                {
                    ram[loc] = data;
                }
                else
                {
                    ErrorLog.getInstance().writeError("RAM::write_loc || >> Invalid parameter given.");
                    throw new IllegalArgumentException();
                }
            }
        }
        else
        {
            ErrorLog.getInstance().writeError("RAM::write_loc || >> Invalid");
            ErrorLog.getInstance().writeError("null value\n");
            throw new IllegalArgumentException();
        }
    }

    public void DMAwrite(String data, int loc)
    {
        if( data != null){
            if(loc >= 0 && loc < Driver.amtOfRAM){
                ram[loc] = data;
            }else{
                ErrorLog.getInstance().writeError("RAM::write_loc || >> Invalid parameter given.");
                throw new IllegalArgumentException();
            }

        }else{
            ErrorLog.getInstance().writeError("RAM::write_loc || >> Invalid");
            ErrorLog.getInstance().writeError("null value\n");
            throw new IllegalArgumentException();
        }
    }


    public int write_next(String data)
    {
        int returnLoc = -1;

        if(data != null){
            if(next_loc >= 0 && next_loc < Driver.amtOfRAM){
                ram[next_loc] = data;
                returnLoc = next_loc;
                next_loc++;
            }else{
                ErrorLog.getInstance().writeError("RAM::write_loc || >> Invalid address.");
                throw new IllegalArgumentException();
            }
        }else{
            ErrorLog.getInstance().writeError("RAM::write_loc || >> Invalid");
            ErrorLog.getInstance().writeError("null value\n");
            throw new IllegalArgumentException();
        }

        return returnLoc;
    }

    public void resetRAM()
    {
        for (int i = 0;i < Driver.amtOfRAM; i++)
            ram[i] = "";
        next_loc=0;
    }

    public String[] getMemDump()
    {
        String[] temp = new String[Driver.amtOfRAM];

        for(int i = 0; i < ram.length; i++)
        {
            temp[i] = ram[i];
        }

        return temp;
    }

    public int get_next_loc()
    {
        return next_loc;
    }

    public int sizeOfRam()
    {
        return Driver.amtOfRAM;
    }

    public String toString()
    {
        String temp = "Memory Dump:";

        for(int i = 0; i < ram.length; i++)
        {
            temp += "\n" + ram[i];
        }

        return temp;
    }
}
