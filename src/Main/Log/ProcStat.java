package Main.Log;

/**
 * Created with IntelliJ IDEA.
 * User: aV1rus
 * Date: 7/1/13
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */
import java.io.*;

/**
 * ProcStat class is responsible for creating and modifying a log file called
 * process_stats.txt.  This class creates a directory under the local directory of
 * the executable called "/logs/stats/".  If this directory exists then it moves
 * to that directory to see if the file exists.  If not, it is created and any information
 * added by the writeStat() method is appended to the end of that file.  Each entry
 * is ended by a newLine() statement for easier reading.
 */
public class ProcStat
{
    //Private Class Variables
    private File sFile;
    private BufferedWriter bWrite;
    private static ProcStat pStat;
    private String directory;

    //Private Default Constructor
    private ProcStat()
    {
        //Creates the directory structure.
        File dir = new File("logs");

        try
        {
            if(dir.mkdir())
            {
                directory = dir.getCanonicalPath();
                dir = null;
                dir = new File(directory, "stats");
                dir.mkdir();
                directory = dir.getCanonicalPath();
            }
            else //If directory "logs" already exists.
            {
                directory = dir.getCanonicalPath();
                dir = null;
                dir = new File(directory, "stats");
                dir.mkdir();
                directory = dir.getCanonicalPath();
            }
        }
        catch(IOException io)
        {
            System.err.println("Error creating stats files");
            io.printStackTrace();
        }

        //Create the .txt file being utilized by class.
        sFile = new File(directory, "process_stats.txt");
    }

    /**
     * Singleton getPointer method that allows the user access to the object through
     * it's pointer.  This safety feature forces only one object being created.
     *
     * @return synchronized pointed to the object ProcStat.
     */
    public static synchronized ProcStat getInstance()
    {
        if(pStat == null)
        {
            pStat = new ProcStat();
        }

        return pStat;
    }

    /**
     * Mutator Method allows the user to append a string to the end of the file named
     * process_stats.txt.
     *
     * @param stats String representing the statistical information the user wishes to
     * append to the back of the file.  This method does not format this string in any way.
     * It is up to the user to format the string in the manner they wish to store the information.
     *
     * @throws IllegalArgumentException if the string passed to the method is null.
     */
    public synchronized void writeStat(String stats)
    {
        try
        {
            FileWriter fWrite = new FileWriter(sFile, true);
            bWrite= new BufferedWriter(fWrite);

            if(stats == null)
            {
                //Writes the error to the log file.
                ErrorLog.getInstance().writeError("ProcStat::writeStat || >> null parameter passed. -- ");
                throw new IllegalArgumentException();
            }
            else
            {
                String temp = stats;
                bWrite.append(temp);
                bWrite.newLine();
            }

            bWrite.close();
        }
        catch(IOException io)
        {
            System.err.println("Internal Error::ErrorLog Object >> writeError");
            io.printStackTrace();
        }
    }
}

