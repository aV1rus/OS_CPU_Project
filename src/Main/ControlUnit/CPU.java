package Main.ControlUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Nick Maiello (aV1rus)
 * Date: 7/1/13
 * Time: 11:59 AM
 * To change this template use File | Settings | File Templates.
 */
import Main.Constants.Constants;
import Main.Constants.InstructionFormats;
import Main.Constants.InstructionSets;
import Main.Driver;
import Main.Log.ErrorLog;
import Main.Memory.RAM;

import Main.ProcessControl.PCB;

public class CPU
{
    int[] mReg;
    final static int mBeginIndex = 1;
    static String mPageCode;            //Full Page Code
    private String mRAMRead;            //Current RAM read for buff
    private boolean mIsBusy;
    private int mCPUBeingUsed;
    private int mRunInstruction;
    private int mProcessorId;


    public CPU()
    {
        mRAMRead = "";
        mReg = new int[16];
        mPageCode  = null;
        mIsBusy = false;
    }

    public boolean getIsBusy()
    {
        return mIsBusy;
    }

    public void setIsBusy(boolean stat)
    {
        mIsBusy = stat;
    }




    public void fetch()
    {
        int newAdd = mRunInstruction;
        if((newAdd >= 0) && (newAdd < RAM.getInstance().sizeOfRam()))
        {
            mRAMRead = RAM.getInstance().read(newAdd);
            if ( mRAMRead.equals(Constants.PAGE_FAULT) ) park(1);

            mPageCode = mRAMRead;
        }
        else
        {
            ErrorLog.getInstance().writeError("CPU::fetch || >> Invalid memory address");
            throw new IllegalArgumentException();
        }
    }

    public void moveToNext()
    {
        PCB.getInstance().getJob(mProcessorId).setExecTime(1);
        for (int i = PCB.getInstance().getJob(mProcessorId).getProc_id(); i < (PCB.getInstance().lastJob() + 1); i++)
        {
            if((PCB.getInstance().getJob(i).getProcState()) < 2)
                PCB.getInstance().getJob(i).setWaitTime(1);
        }
    }

    // accepts an instruction in hex format, decodes it into binary,
    // and creates an Instruction object from the binary representation
    private void executeCode(String hex)
    {

        String binInstr = parseInstruction(hex);
        Instruction instr = new Instruction(binInstr);
        String format = instr.getFormat();
        String op = instr.getOpcode();


        if(op.equals(InstructionFormats.INSTRUCTION_FORMAT_DO_NOTHING))
            moveToNext();

        else if(format.equals(InstructionFormats.INSTRUCTION_FORMAT_ARITHMETIC)){

            moveToNext();
            arithReg(instr);

        }else if(format.equals(InstructionFormats.INSTRUCTION_FORMAT_CONDITIONAL)){

            moveToNext();
            conditionalImmediate(instr);

        }else if(format.equals(InstructionFormats.INSTRUCTION_FORMAT_UNCONDITIONAL)){

            moveToNext();
            jump(instr);

        }else if(format.equals(InstructionFormats.INSTRUCTION_FORMAT_IO)){

            PCB.getInstance().getJob(mProcessorId).setIOCount(1);
            park(0);
            moveToNext();
            inputOutput(instr, mProcessorId);

        }else{
            ErrorLog.getInstance().writeError("CPU::execute() || >> Invalid instruction");
            throw new IllegalArgumentException();
        }

    }

    private  void inputOutput(Instruction instr, int pid)
    {
        int address = Integer.parseInt(instr.getParams().substring(8, 24), 2); // I/O buffer address
        String op = instr.getOpcode();

        if(op.equals(InstructionSets.INSTRUCTION_SET_READ))
        {
            mRAMRead = (RAM.getInstance().read(PCB.getInstance().getJob(pid).getData_memStart() + (address/4)));
            if (mRAMRead.equals(Constants.PAGE_FAULT) )
                park(1);
            else
                mReg[0] += Integer.parseInt(mRAMRead, 16);
        }
        else if (op.equals(InstructionSets.INSTRUCTION_SET_WRITE))
        {
            String writeValue = Integer.toHexString(mReg[0]);
            RAM.getInstance().write_loc(writeValue, (PCB.getInstance().getJob(pid).getProc_memStart() + (address/4) ));
        }

        else
        {
            ErrorLog.getInstance().writeError("CPU::inputOutput() || >> Invalid instruction");
            throw new IllegalArgumentException();
        }

    }

    private void arithReg(Instruction instr)
    {
        int sReg1 = Integer.parseInt(instr.getParams().substring(0, 4), 2);
        int sReg2 = Integer.parseInt(instr.getParams().substring(4, 8), 2);
        int dReg = Integer.parseInt(instr.getParams().substring(8, 12), 2);

        String op = instr.getOpcode();

        if(op.equals(InstructionSets.INSTRUCTION_SET_MV)) // MV
            mReg[dReg] = mReg[sReg1] + mReg[mBeginIndex];

        else if(op.equals(InstructionSets.INSTRUCTION_SET_ADD)) // ADD
            mReg[dReg] = mReg[sReg1] + mReg[sReg2];

        else if(op.equals(InstructionSets.INSTRUCTION_SET_SUB)) // SUB
            mReg[dReg] = mReg[sReg1] - mReg[sReg2];

        else if(op.equals(InstructionSets.INSTRUCTION_SET_MUL)) // MUL
            mReg[dReg] = mReg[sReg1] * mReg[sReg2];

        else if(op.equals(InstructionSets.INSTRUCTION_SET_DIV)) // DIV
        {
            if ((mReg[sReg2] == 0) || (mReg[sReg1] == 0))
                mReg[dReg] = 0;
            else
                mReg[dReg] = mReg[sReg2] / mReg[sReg1];
        }

        else if(op.equals(InstructionSets.INSTRUCTION_SET_AND)) // bitwise AND
            mReg[dReg] = mReg[sReg1] & mReg[sReg2];

        else if(op.equals(InstructionSets.INSTRUCTION_SET_OR)) // bitwise OR
            mReg[dReg] = mReg[sReg1] | mReg[sReg2];

        else if(op.equals(InstructionSets.INSTRUCTION_SET_SLT)) // SLT
            mReg[dReg] = (mReg[sReg1] > mReg[sReg2])? 0 : 1;

        else
        {
            ErrorLog.getInstance().writeError("CPU::arithReg() || >> Invalid instruction");
            throw new IllegalArgumentException();
        }
    }

    // instruction block for conditional / immediate format
    private void conditionalImmediate(Instruction instr)
    {
        int bReg = Integer.parseInt(instr.getParams().substring(0, 4), 2); // base register
        int dReg = Integer.parseInt(instr.getParams().substring(4, 8), 2); // destination register
        int address = Integer.parseInt(instr.getParams().substring(8), 2)/4; // address / immediate value

        String op = instr.getOpcode();

        if(op.equals(InstructionSets.INSTRUCTION_SET_MOVI))
            mReg[dReg] = mReg[bReg] + mReg[mBeginIndex];

        else if(op.equals(InstructionSets.INSTRUCTION_SET_ADDI))
            mReg[dReg] = mReg[bReg] + address;

        else if(op.equals(InstructionSets.INSTRUCTION_SET_MULI))
            mReg[dReg] = mReg[bReg] * address;

        else if(op.equals(InstructionSets.INSTRUCTION_SET_DIVI))
            mReg[dReg] = mReg[bReg] / address;

        else if(op.equals(InstructionSets.INSTRUCTION_SET_LDI))
            mReg[dReg] = mReg[mBeginIndex] + address;

        else if(op.equals(InstructionSets.INSTRUCTION_SET_SLTI))
            mReg[dReg] = (mReg[bReg] > address)? 0 : 1;

        else if(op.equals(InstructionSets.INSTRUCTION_SET_BEQ))
            if(mReg[bReg] == mReg[dReg])
                Dispatch.getDispatch(mCPUBeingUsed).set(address);

        else if(op.equals(InstructionSets.INSTRUCTION_SET_BNE))
            if(mReg[bReg] != mReg[dReg])
                Dispatch.getDispatch(mCPUBeingUsed).set(address);

        else if(op.equals(InstructionSets.INSTRUCTION_SET_BEZ))
            if(mReg[bReg] == 0)
                Dispatch.getDispatch(mCPUBeingUsed).set(address);

        else if(op.equals(InstructionSets.INSTRUCTION_SET_BNZ))
            if(mReg[bReg] != 0)
                Dispatch.getDispatch(mCPUBeingUsed).set(address);

        else if(op.equals(InstructionSets.INSTRUCTION_SET_BGZ))
            if(mReg[bReg] > 0)
                Dispatch.getDispatch(mCPUBeingUsed).set(address);

        else if(op.equals(InstructionSets.INSTRUCTION_SET_BLZ))
            if(mReg[bReg] < 0)
                Dispatch.getDispatch(mCPUBeingUsed).set(address);
            else{
                ErrorLog.getInstance().writeError("CPU::condImm() || >> Invalid instruction");
                throw new IllegalArgumentException();
            }

    }

    private void jump(Instruction instr)
    {
        //set starting address (factor in word size)
        int address = (Integer.parseInt(instr.getParams())/4);
        String op = instr.getOpcode();
        if(op.equals(InstructionSets.INSTRUCTION_SET_HLT)) // HLT
        {
            //display job information on completion

            String waitTimeVal =  (Dispatch.getDispatch(mCPUBeingUsed).mCurrentProc.getWaitTime() > 0) ?
                    "" + (Dispatch.getDispatch(mCPUBeingUsed).mCurrentProc.getWaitTime() - Dispatch.getDispatch(mCPUBeingUsed).mCurrentProc.getExecTime()) :
                    "0";
            String processVal = ""+mProcessorId;
            String executionTimeVal = "" + Dispatch.getDispatch(mCPUBeingUsed).mCurrentProc.getExecTime();
            String instructionsVal = "" + Dispatch.getDispatch(mCPUBeingUsed).mCurrentProc.getProc_iCount();
            String IOinstructionsVal ="" + Dispatch.getDispatch(mCPUBeingUsed).mCurrentProc.getIOCount();
            String faultsVal = ""+ Dispatch.getDispatch(mCPUBeingUsed).mCurrentProc.getFaultCount();






            System.out.format(Constants.Output_Table_Format,
                    Constants.PROCESS_LABEL,
                    processVal + "\t",
                    Constants.WAIT_TIME_LABEL + "\t",
                    waitTimeVal + "\t",
                    Constants.EXECUTION_TIME_LABEL,
                    executionTimeVal + "\t",
                    Constants.INSTRUCTIONS_LABEL,
                    instructionsVal + "\t",
                    Constants.IO_INSTRUCTIONS_LABEL,
                    IOinstructionsVal + "\t",
                    Constants.FAULTS_LABEL,
                    faultsVal + "\t");
            System.out.print(Constants.EndLine);


            Dispatch.getDispatch(mCPUBeingUsed).mCurrentProc.setProcState(3);
            Dispatch.getDispatch(mCPUBeingUsed).setTerminate();
            mIsBusy = false;
            mReg = new int[16];
        }

        else if(op.equals(InstructionSets.INSTRUCTION_SET_JMP))
            Dispatch.getDispatch(mCPUBeingUsed).set(address);

        else
        {
            ErrorLog.getInstance().writeError("CPU::jump() || >> Invalid instruction");
            throw new IllegalArgumentException();
        }
    }

    public void setRegisters(int[] temp)
    {
        mReg = temp;
    }



    public void run(int instruct, int CPUNum)
    {
        mCPUBeingUsed = CPUNum;
        mRunInstruction = instruct;
        mProcessorId = Dispatch.getDispatch(mCPUBeingUsed).mCurrentProc.getProc_id();
        fetch();
        if ( !mPageCode.equals(Constants.PAGE_FAULT) )
            executeCode(mPageCode);

    }
    private String hexToBinary(char hexChar)
    {
        int value;
        String bin;

        if((hexChar >= '0') && (hexChar <= '9'))
        {
            value = hexChar - '0';
            bin = Integer.toBinaryString(value);
        }
        else if ((hexChar >= 'A') && (hexChar <= 'F'))
        {
            value = (hexChar - 'A') + 10;
            bin = Integer.toBinaryString(value);
        }
        else
            bin = null;

        if(bin != null)
        {
            int zeros = 4 - bin.length();
            for(int i = 0; i < zeros; i++)
                bin = "0" + bin;
        }

        return bin;
    }

    public String parseInstruction(String hexString)
    {
        String bin = "";

        for(int i = 0; i < hexString.length(); i++)
            bin = bin + hexToBinary(hexString.charAt(i));

        return bin;

    }
    private void park(int type)
    {
        PCB.getInstance().getJob(mProcessorId).setProcState(4);
        if (Driver.contextSwitch)
        {
            if (type == 0) //io interrupt
                PCB.getInstance().getJob(mProcessorId).setNextInstruct(mRunInstruction + 1);
            else if (type == 1) //pagefault
                PCB.getInstance().getJob(mProcessorId).setNextInstruct(mRunInstruction);
            PCB.getInstance().getJob(mProcessorId).setRegisters(mReg);
        }
    }

    private static class Instruction
    {
        String format, opcode, params;


        private Instruction(String instr)
        {
            format = instr.substring(0, 2);
            opcode = "00" + instr.substring(2, 8);
            params = instr.substring(8, instr.length());
        }

        private String getFormat()
        {
            return format;
        }

        private String getOpcode()
        {
            return opcode;
        }

        private String getParams()
        {
            return params;
        }

    }

}


