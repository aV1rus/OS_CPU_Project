package Main.ControlUnit;

/**
 * Created with IntelliJ IDEA.
 * User: aV1rus
 * Date: 7/1/13
 * Time: 11:59 AM
 * To change this template use File | Settings | File Templates.
 */
import Main.Driver;
import Main.Log.ErrorLog;
import Main.Memory.RAM;

import Main.ProcessControl.PCB;

public class CPU
{
    private static CPU cpu;
    int[] reg;
    final static int acc = 0;
    final static int zeroIndex = 1;
    static String PC;
    private boolean busy;

    private int runCPU;
    private int runInstr;
    private int pid;

    private String RAMRead = "";

    public CPU()
    {
        reg = new int[16];
        PC = null;
        busy = false;
    }

    public boolean isBusy()
    {
        return busy;
    }

    public void isBusy(boolean stat)
    {
        busy = stat;
    }




    public void fetch()
    {
        int newAdd = runInstr;
        if((newAdd >= 0) && (newAdd < RAM.getInstance().sizeOfRam()))
        {
            RAMRead = RAM.getInstance().read(newAdd);
            if (RAMRead == "page fault")
            {
                park(1);
                PC = RAMRead;
            }
            else
                PC = RAMRead;
        }
        else
        {
            ErrorLog.getInstance().writeError("CPU::fetch || >> Invalid memory address");
            throw new IllegalArgumentException();
        }
    }


    // accepts an instruction in hex format, decodes it into binary,
    // and creates an Instruction object from the binary representation
    private void execute(String hex)
    {

        String binInstr = parseInstruction(hex);
        Instruction instr = new Instruction(binInstr);
        String format = instr.getFormat();
        String op = instr.getOpcode();

        if(op.equals("13"))
        {
            PCB.getInstance().getJob(pid).setExecTime(1);
            for (int i = PCB.getInstance().getJob(pid).getProc_id(); i < (PCB.getInstance().lastJob() + 1); i++)
            {
                if((PCB.getInstance().getJob(i).getProcState()) < 2)
                    PCB.getInstance().getJob(i).setWaitTime(1);
            }
        }

        else if(format.equals("00")){
            PCB.getInstance().getJob(pid).setExecTime(1);
            for (int i = PCB.getInstance().getJob(pid).getProc_id(); i < (PCB.getInstance().lastJob() + 1); i++)
            {
                if((PCB.getInstance().getJob(i).getProcState()) < 2)
                    PCB.getInstance().getJob(i).setWaitTime(1);
            }

            arithReg(instr);
        }

        else if(format.equals("01")){
            PCB.getInstance().getJob(pid).setExecTime(1);
            for (int i = PCB.getInstance().getJob(pid).getProc_id(); i < (PCB.getInstance().lastJob() + 1); i++)
            {
                if((PCB.getInstance().getJob(i).getProcState()) < 2)
                    PCB.getInstance().getJob(i).setWaitTime(1);
            }

            condImm(instr);
        }

        else if(format.equals("10")){
            PCB.getInstance().getJob(pid).setExecTime(1);
            for (int i = PCB.getInstance().getJob(pid).getProc_id(); i < (PCB.getInstance().lastJob() + 1); i++)
            {
                if((PCB.getInstance().getJob(i).getProcState()) < 2)
                    PCB.getInstance().getJob(i).setWaitTime(1);
            }

            jump(instr);
        }

        else if(format.equals("11")){
            PCB.getInstance().getJob(pid).setExecTime(1);
            PCB.getInstance().getJob(pid).setIOCount(1);
            park(0);

            for (int i = PCB.getInstance().getJob(pid).getProc_id(); i < (PCB.getInstance().lastJob() + 1); i++)
            {
                if((PCB.getInstance().getJob(i).getProcState()) < 2)
                {
                    PCB.getInstance().getJob(i).setWaitTime(5);

                }
            }

            inputOutput(instr, pid);
        }
        else
        {
            ErrorLog.getInstance().writeError("CPU::execute() || >> Invalid instruction");
            throw new IllegalArgumentException();
        }

    }

    private  void inputOutput(Instruction instr, int pid)
    {

        int reg1 = Integer.parseInt(instr.getParams().substring(0, 4), 2);
        int reg2 = Integer.parseInt(instr.getParams().substring(4, 8), 2); // I/O buffer offset
        int address = Integer.parseInt(instr.getParams().substring(8, 24), 2); // I/O buffer address
        String op = instr.getOpcode();

        if(op.equals("00000000"))
        {
            RAMRead = (RAM.getInstance().read(PCB.getInstance().getJob(pid).getData_memStart() + (address/4)));
            if (RAMRead == "page fault")
            {
                //System.out.println("CPU: parked at RAMRead");
                park(1);
            }
            else
                reg[acc] += Integer.parseInt(RAMRead, 16);
        }
        else if (op.equals("00000001"))
        {
            String writeValue = Integer.toHexString(reg[acc]);
            RAM.getInstance().write_loc(writeValue, (PCB.getInstance().getJob(pid).getProc_memStart() + (address/4) ));// + reg[reg2]));
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

        if(op.equals("00000100")) // MV
            reg[dReg] = reg[sReg1] + reg[zeroIndex];

        else if(op.equals("00000101")) // ADD
            reg[dReg] = reg[sReg1] + reg[sReg2];

        else if(op.equals("00000110")) // SUB
            reg[dReg] = reg[sReg1] - reg[sReg2];

        else if(op.equals("00000111")) // MUL
            reg[dReg] = reg[sReg1] * reg[sReg2];

        else if(op.equals("00001000")) // DIV
        {
            if ((reg[sReg2] == 0) || (reg[sReg1] == 0))
                reg[dReg] = 0;
            else
                reg[dReg] = reg[sReg2] / reg[sReg1];
        }

        else if(op.equals("00001001")) // bitwise AND
            reg[dReg] = reg[sReg1] & reg[sReg2];

        else if(op.equals("00001010")) // bitwise OR
            reg[dReg] = reg[sReg1] | reg[sReg2];

        else if(op.equals("00010000")) // SLT
            reg[dReg] = (reg[sReg1] > reg[sReg2])? 0 : 1;

        else
        {
            ErrorLog.getInstance().writeError("CPU::arithReg() || >> Invalid instruction");
            throw new IllegalArgumentException();
        }

        //System.out.println("D Reg Final: " + dReg + " :: " + reg[dReg]);
    }

    // instruction block for conditional / immediate format
    private void condImm(Instruction instr)
    {
        int bReg = Integer.parseInt(instr.getParams().substring(0, 4), 2); // base register
        int dReg = Integer.parseInt(instr.getParams().substring(4, 8), 2); // destination register
        int address = Integer.parseInt(instr.getParams().substring(8), 2)/4; // address / immediate value

        String op = instr.getOpcode();

        if(op.equals("00001011")) // MOVI
            reg[dReg] = reg[bReg] + reg[zeroIndex];

        else if(op.equals("00001100")) // ADDI
            reg[dReg] = reg[bReg] + address;

        else if(op.equals("00001101")) // MULI
            reg[dReg] = reg[bReg] * address;

        else if(op.equals("00001110")) // DIVI
            reg[dReg] = reg[bReg] / address;

        else if(op.equals("00001111")) // LDI
            reg[dReg] = reg[zeroIndex] + address;

        else if(op.equals("00010001")) // SLTI
            reg[dReg] = (reg[bReg] > address)? 0 : 1;

        else if(op.equals("00010101")) // BEQ
            if(reg[bReg] == reg[dReg])
                MultiDispatch.getDispatch(runCPU).set(address);

            else if(op.equals("00010110")) // BEQ
                if(reg[bReg] != reg[dReg])
                    MultiDispatch.getDispatch(runCPU).set(address);

                else if(op.equals("00010111")) // BEZ
                    if(reg[bReg] == 0)
                        MultiDispatch.getDispatch(runCPU).set(address);

                    else if(op.equals("00011000")) // BNZ
                        if(reg[bReg] != 0)
                            MultiDispatch.getDispatch(runCPU).set(address);

                        else if(op.equals("00011001")) // BGZ
                            if(reg[bReg] > 0)
                                MultiDispatch.getDispatch(runCPU).set(address);

                            else if(op.equals("00011010")) // BLZ
                                if(reg[bReg] < 0)
                                    MultiDispatch.getDispatch(runCPU).set(address);
                                else
                                {
                                    ErrorLog.getInstance().writeError("CPU::condImm() || >> Invalid instruction");
                                    throw new IllegalArgumentException();
                                }

        //System.out.println("dReg Final: " + dReg + " :: " + reg[dReg]);
    }

    private void jump(Instruction instr)
    {
        //set starting address (factor in word size)
        int address = (Integer.parseInt(instr.getParams())/4);
        String op = instr.getOpcode(); //parseInstruction(instr.getOpcode());
        //System.out.println(" in jump inst : " + instr.getOpcode());

        //System.out.println(" Jump: Op: " + op);

        if(op.equals("00010010")) // HLT
        {
            //display job information on completion

            String waitTimeLabel = "Wait Time:";
            String processLabel = "Process:";
            String executionTimeLabel = "Execution Time:";
            String instructionsLabel = "Instructions:";
            String IOinstructionsLabel ="IO Instructions:";
            String faultsLabel = "Faults:";



            String waitTimeVal =  (MultiDispatch.getDispatch(runCPU).currentProc.getWaitTime() > 0) ?
                    "" + (MultiDispatch.getDispatch(runCPU).currentProc.getWaitTime() - MultiDispatch.getDispatch(runCPU).currentProc.getExecTime()) :
                    "0";
            String processVal = ""+pid;
            String executionTimeVal = "" + MultiDispatch.getDispatch(runCPU).currentProc.getExecTime();
            String instructionsVal = "" + MultiDispatch.getDispatch(runCPU).currentProc.getProc_iCount();
            String IOinstructionsVal ="" + MultiDispatch.getDispatch(runCPU).currentProc.getIOCount();
            String faultsVal = ""+ MultiDispatch.getDispatch(runCPU).currentProc.getFaultCount();






            System.out.format("%15s%5s%15s%5s%15s%5s%15s%5s%15s%5s%15s%5s",
                                     processLabel,
                                                    processVal+"\t",
                                     waitTimeLabel+"\t",
                                                    waitTimeVal+"\t",
                                     executionTimeLabel,
                                                    executionTimeVal+"\t",
                                     instructionsLabel,
                                                    instructionsVal+"\t",
                                     IOinstructionsLabel,
                                                    IOinstructionsVal+"\t",
                                     faultsLabel,
                                                    faultsVal+"\t");
            System.out.print("\n----------------------------------------------------------------------------------------------------------------------------------------------\n");


            MultiDispatch.getDispatch(runCPU).currentProc.setProcState(3);
            MultiDispatch.getDispatch(runCPU).setTerminate();
            busy = false;
            reg = new int[16];
        }

        else if(op.equals("00010100"))
            MultiDispatch.getDispatch(runCPU).set(address);

        else
        {
            ErrorLog.getInstance().writeError("CPU::jump() || >> Invalid instruction");
            throw new IllegalArgumentException();
        }
    }

    public void setRegisters(int[] temp)
    {
        reg = temp;
    }



    public void run(int instruct, int CPUNum)
    {
        runCPU = CPUNum;
        runInstr = instruct;
        pid = MultiDispatch.getDispatch(runCPU).currentProc.getProc_id();
        fetch();
        if (PC != "page fault")
            execute(PC);

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
        PCB.getInstance().getJob(pid).setProcState(4);
        if (Driver.contextSwitch)
        {
            if (type == 0) //io interrupt
                PCB.getInstance().getJob(pid).setNextInstruct(runInstr + 1);
            else if (type == 1) //pagefault
                PCB.getInstance().getJob(pid).setNextInstruct(runInstr);
            PCB.getInstance().getJob(pid).setRegisters(reg);
        }
    }

    private static class Instruction
    {
        String format, opcode, params;

        private Instruction()
        {
            format = null;
            opcode = null;
            params = null;
        }

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


