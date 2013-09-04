package Main.ConfigFiles;

/**
 * Created with IntelliJ IDEA.
 * User: Nick Maiello
 * Date: 8/13/13
 * Time: 10:00 AM
 */
public class Config {

    public static final int SORT_TYPE = 1;          // 0=FCFS, 1=Job Priority, 2=SJF   ONLY 1 is available
    public static int NUM_OF_PROCESSOR = 10;          // 1 / 4 / 8 / 16 / 32 / etc
    public static int RAM_SIZE = 2048;              // RAM is multiplied by num of processor
    public static final int HARD_DRIVE_SPACE = 2048;
    public static final boolean CONTEXT_SWITCH = true;

}
