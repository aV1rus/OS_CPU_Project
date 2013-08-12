package Main.Constants;

/**
 * Created with IntelliJ IDEA.
 * User: nick
 * Date: 8/12/13
 * Time: 5:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class InstructionSets {
    public static final String INSTRUCTION_SET_READ = "00000000";
    public static final String INSTRUCTION_SET_WRITE = "00000001";

    public static final String INSTRUCTION_SET_MV = "00000100";
    public static final String INSTRUCTION_SET_ADD = "00000101";
    public static final String INSTRUCTION_SET_SUB = "00000110";
    public static final String INSTRUCTION_SET_MUL = "00000111";
    public static final String INSTRUCTION_SET_DIV = "00001000";


    public static final String INSTRUCTION_SET_AND = "00001001";
    public static final String INSTRUCTION_SET_OR = "00001010";
    public static final String INSTRUCTION_SET_SLT = "00010000";


    public static final String INSTRUCTION_SET_MOVI = "00001011";
    public static final String INSTRUCTION_SET_ADDI = "00001100";
    public static final String INSTRUCTION_SET_MULI = "00001101";
    public static final String INSTRUCTION_SET_DIVI = "00001110";
    public static final String INSTRUCTION_SET_LDI = "00001111";
    public static final String INSTRUCTION_SET_SLTI = "00010001";
    public static final String INSTRUCTION_SET_BEQ = "00010101";
    public static final String INSTRUCTION_SET_BNE = "00010110";
    public static final String INSTRUCTION_SET_BEZ = "00010111";
    public static final String INSTRUCTION_SET_BNZ = "00011000";
    public static final String INSTRUCTION_SET_BGZ = "00011001";
    public static final String INSTRUCTION_SET_BLZ = "00011010";

    public static final String INSTRUCTION_SET_HLT = "00010010";

    public static final String INSTRUCTION_SET_JMP = "00010100";
}
