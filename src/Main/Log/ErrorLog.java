package Main.Log;

/**
 * Created with IntelliJ IDEA.
 * User: Nick Maiello (aV1rus)
 * Date: 7/1/13
 * Time: 11:53 AM
 */
import java.io.*;
import java.util.Calendar;

/**
 * ErrorLog class is responsible for creating and modifying a log file called
 * error_log.txt.  This class will create a directory for errors to be logged.
 */
public class ErrorLog
{
    //private class variables
    private File eFile;
    private BufferedWriter bWrite;
    private static ErrorLog eLog;
    private Calendar date;
    private String directory;

    //private default constructor.
    private ErrorLog()

    {
        date = Calendar.getInstance();

        //Create the directory structure.
        File dir = new File("logs");

        try
        {
            if(dir.mkdir())
            {
                directory = dir.getCanonicalPath();
                dir = null;
                dir = new File(directory, "error");
                dir.mkdir();
                directory = dir.getCanonicalPath();
            }
            else
            {
                directory = dir.getCanonicalPath();
                dir = null;
                dir = new File(directory, "error");
                dir.mkdir();
                directory = dir.getCanonicalPath();
            }
        }
        catch(IOException io)
        {
            System.err.println("Error creating logs file...");
            io.printStackTrace();
        }

        //Construct error_log.txt file in local_dir/logs/error/
        eFile = new File(directory, "error_log.txt");
    }

    /**
     * Singleton getPointer method that allows the user access to the object through
     * it's pointer.  This safety feature forces only one object being created.
     *
     * @return synchronized pointed to the object ErrorLog.
     */
    public static synchronized ErrorLog getInstance()
    {
        if(eLog == null)
        {
            eLog = new ErrorLog();
        }

        return eLog;
    }

    /**
     * Mutator Method allows the user to append a string to the end of the file named
     * error_log.txt.
     *
     * @param err String representing the error which the user wishes to record into a
     * file.  This method does not format the string before appending to the file.  This method
     * does append a time stamp to the end of the string for recording purposes.
     *
     * @throws IllegalArgumentException if the string passed to the method is null.
     */
    public synchronized void writeError(String err)
    {
        try
        {
            FileWriter fWrite = new FileWriter(eFile, true);
            bWrite = new BufferedWriter(fWrite);

            if(err == null)
            {
                bWrite.append("ErrorLog::writeError || >> Null input given to writer. -- ");
                bWrite.append(date.getTime().toString());
                bWrite.newLine();
                throw new IllegalArgumentException();

            }
            else
            {
                String temp = err + " -- ";
                bWrite.append(temp);
                bWrite.append(date.getTime().toString());
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


