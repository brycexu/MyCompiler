/*
作者:徐贤达
学号:2016060601018
时间:2019/5/8
功能:语法分析器
*/

/*
 A：程序
                    A->B
 B：分程序
                    B->begin C;M end
 C：说明语句表
                    C->DC'
                    C'->; DC'|ε
 D：说明语句
                    D->E|J
 E：变量说明
                    E->integer F
 F：变量
                    F->G
 G：标识符
                    G->HG'
                    G'->HG'|IG'|ε
 H：字母
                    H->a|...|z|A|...|Z
 I：数字
                    I->0|1|...|9
 J：函数说明
                    J->integer function G(K);L
 K：参数
                    K->F
 L：函数体
                    L->begin C;M end
 M：执行语句表
                    M->NM'
                    M'->;NM'|ε
 N：执行语句
                    N->O|P|Q|W
 O：读语句
                    O->read(F)
 P：写语句
                    P->write(F)
 Q：赋值语句
                    Q->F:=R
 R：算术表达式
                    R->SR'
                    R'->-SR'|ε
 S：项
                    S->TS'
                    S'->*TS'|ε
 T：因子
                    T->F|U|Z
 U：常数
                    U->V
 V：无符号整数
                    V->IV'
                    V'->IV'|ε
 W：条件语句
                    W->if X then N else N
 X：条件表达式
                    X->RYR
 Y：关系运算符
                    Y-><|<=|>|>=|=|<>
 Z：函数调用
                    Z->G(R)
 */

import java.util.ArrayList;
import java.io.*;

public class GrammaticalAnalysis {

    // 存放读入的每个单词
    private static ArrayList<String> input;
    // 存放读入的每个单词对应的种别
    private static ArrayList<String> type;
    // 存放当前单词在ArrayList中的index
    private static int currentToken = 0;
    // 存放变量
    private static ArrayList<Variables> var;
    // 指向当前变量
    private static Variables currentVar;
    // 存放过程
    private static ArrayList<Procedures> pro;
    // 指向当前过程
    private static Procedures currentPro;
    // 行号
    private static int line = 1;
    // 是否产生了语法错误
    private static boolean isVirgin = true;
    private static BufferedReader bufferedReader;
    private static BufferedWriter bufferedWriter1;
    private static BufferedWriter bufferedWriter2;
    private static BufferedWriter bufferedWriter3;
    private static BufferedWriter bufferedWriter4;

    private GrammaticalAnalysis() {
        input = new ArrayList<>();
        type = new ArrayList<>();
        var = new ArrayList<>();
        pro = new ArrayList<>();
    }

    public static void main(String[] args) throws Exception{
        // 初始化语法分析器
        GrammaticalAnalysis grammaticalAnalysis = new GrammaticalAnalysis();
        // 读入文件
        File file = new File("src/Files/result.dyd");
        bufferedReader = new BufferedReader(new FileReader(file));
        String str;
        String temp1;
        String temp2;
        while ((str = bufferedReader.readLine()) != null) {
            temp1= str.substring(0,16);
            temp1 = temp1.trim();
            // 单词存入input
            input.add(temp1);
            temp2 = str.substring(17);
            // 种别存入type
            type.add(temp2);
        }
        bufferedReader = new BufferedReader(new FileReader(file));
        File var = new File("src/Files/variables.var");
        bufferedWriter1 = new BufferedWriter(new FileWriter(var));
        File pro = new File("src/Files/procedures.pro");
        bufferedWriter2 = new BufferedWriter(new FileWriter(pro));
        File result = new File("src/Files/result.dys");
        bufferedWriter3 = new BufferedWriter(new FileWriter(result));
        File error = new File("src/Files/error.err");
        bufferedWriter4 = new BufferedWriter(new FileWriter(error));
        // 开始执行语法分析
        grammaticalAnalysis.A();
        // 写变量名表
        grammaticalAnalysis.writeVariables();
        // 写过程名表
        grammaticalAnalysis.writeProcedures();
        // 写输出文件
        grammaticalAnalysis.writeResults();
        bufferedReader.close();
        bufferedWriter1.close();
        bufferedWriter2.close();
        bufferedWriter3.close();
        bufferedWriter4.close();
    }

    /**
     * 输出错误
     * @param errorNumber:错误种别
     * @param errorInfo:错误的单词名称
     * @throws Exception
     */
    private void printError(int errorNumber, String errorInfo) throws Exception{
        isVirgin = false;
        switch (errorNumber) {
            // 缺少符号错
            case 1:
                System.out.println("ERROR! LINE:" + line + "  缺少符号错:" + errorInfo + "\n");
                bufferedWriter4.write("ERROR! LINE:" + line + "  缺少符号错:" + "'" + errorInfo + "'" + "\n");
                break;
            // 符号匹配错
            case 2:
                System.out.println("ERROR! LINE:" + line + "  符号匹配错:" + errorInfo + "\n");
                bufferedWriter4.write("ERROR! LINE:" + line + "  符号匹配错:" + "'" + errorInfo + "'" + "\n");
                break;
            // 符号无定义
            case 3:
                System.out.println("ERROR! LINE:" + line + "  符号无定义:" + errorInfo + "\n");
                bufferedWriter4.write("ERROR! LINE:" + line + "  符号无定义:" + "'" + errorInfo + "'" + "\n");
                break;
            // 符号重复定义
            case 4:
                System.out.println("ERROR! LINE:" + line + "  符号重复定义:" + errorInfo + "\n");
                bufferedWriter4.write("ERROR! LINE:" + line + "  符号重复定义:" + "'" + errorInfo + "'" + "\n");
                break;
            default:
                break;
        }
    }

    /**
     * 获取下一个单词
     */
    private void nextToken() {
        currentToken++;
        while (input.get(currentToken).equals("EOLN")) {
            currentToken++;
            line++;
        }
    }

    /**
     * 获取下一个单词在input中的index
     * @return 下一个单词在input中的index
     */
    private int getNextToken() {
        int nextToken = currentToken + 1;
        while (input.get(nextToken).equals("EOLN"))
            nextToken++;
        return nextToken;
    }

    /**
     * 跳到下一行(符号匹配错下)
     */
    private void jumpToNextLine() {
        while (!input.get(currentToken).equals("EOLN"))
            currentToken++;
        currentToken++;
    }

    /**
     * 判断变量名是否合法
     * @param vname:变量名
     * @param vproc:变量所属过程名称
     * @param vkind:变量种别
     * @return 是和否
     */
    private boolean isVarIllegal(String vname, String vproc, int vkind) {
        if (var.size() == 0 || pro.size() == 0)
            return false;
        for (int i = 0; i < pro.size(); i++) {
            if (vname.equals(pro.get(i).pname))
                return true;
        }
        for (int i = 0; i < var.size(); i++) {
            if (vname.equals(var.get(i).vname) && vproc.equals(var.get(i).vproc) && vkind == var.get(i).vkind)
                return true;
        }
        return false;
    }

    /**
     * 判断过程名是否合法
     * @param pname:过程名
     * @return 是和否
     */
    private boolean isProIllegal(String pname) {
        if (var.size() == 0 || pro.size() == 0)
            return false;
        for (int i = 0; i < pro.size(); i++) {
            if (pname.equals(pro.get(i).pname))
                return true;
        }
        for (int i = 0; i < var.size(); i++) {
            if (pname.equals(var.get(i).vname))
                return true;
        }
        return false;
    }

    /**
     * 写变量名表
     * 左对齐
     * @throws Exception
     */
    private void writeVariables() throws Exception {
        bufferedWriter1.write(String.format("%-16s", "vname"));
        bufferedWriter1.write(String.format("%-16s", "vproc"));
        bufferedWriter1.write(String.format("%-8s", "vkind"));
        bufferedWriter1.write(String.format("%-16s", "vtype"));
        bufferedWriter1.write(String.format("%-8s", "vlev"));
        bufferedWriter1.write(String.format("%-8s", "vadr"));
        bufferedWriter1.write("\n");
        System.out.println("Variables");
        System.out.print(String.format("%-16s", "vname"));
        System.out.print(String.format("%-16s", "vproc"));
        System.out.print(String.format("%-8s", "vkind"));
        System.out.print(String.format("%-16s", "vtype"));
        System.out.print(String.format("%-8s", "vlev"));
        System.out.print(String.format("%-8s", "vadr"));
        System.out.print("\n");
        for (int i = 0; i < var.size(); i++) {
            bufferedWriter1.write(String.format("%-16s", var.get(i).vname));
            bufferedWriter1.write(String.format("%-16s", var.get(i).vproc));
            bufferedWriter1.write(String.format("%-8s", var.get(i).vkind));
            bufferedWriter1.write(String.format("%-16s", var.get(i).vtype.toString()));
            bufferedWriter1.write(String.format("%-8s", var.get(i).vlev));
            bufferedWriter1.write(String.format("%-8s", var.get(i).vadr));
            bufferedWriter1.write("\n");
            System.out.print(String.format("%-16s", var.get(i).vname));
            System.out.print(String.format("%-16s", var.get(i).vproc));
            System.out.print(String.format("%-8s", var.get(i).vkind));
            System.out.print(String.format("%-16s", var.get(i).vtype.toString()));
            System.out.print(String.format("%-8s", var.get(i).vlev));
            System.out.print(String.format("%-8s", var.get(i).vadr));
            System.out.print("\n");
        }
        System.out.println();
    }

    /**
     * 写过程名表
     * 左对齐
     * @throws Exception
     */
    private void writeProcedures() throws Exception {
        bufferedWriter2.write(String.format("%-16s", "pname"));
        bufferedWriter2.write(String.format("%-16s", "ptype"));
        bufferedWriter2.write(String.format("%-8s", "plev"));
        bufferedWriter2.write(String.format("%-8s", "fadr"));
        bufferedWriter2.write(String.format("%-8s", "ladr"));
        bufferedWriter2.write("\n");
        System.out.println("Procedures");
        System.out.print(String.format("%-16s", "pname"));
        System.out.print(String.format("%-16s", "ptype"));
        System.out.print(String.format("%-8s", "plev"));
        System.out.print(String.format("%-8s", "fadr"));
        System.out.print(String.format("%-8s", "ladr"));
        System.out.print("\n");
        for (int i = 0; i < pro.size(); i++) {
            bufferedWriter2.write(String.format("%-16s", pro.get(i).pname));
            bufferedWriter2.write(String.format("%-16s", pro.get(i).ptype.toString()));
            bufferedWriter2.write(String.format("%-8s", pro.get(i).plev));
            bufferedWriter2.write(String.format("%-8s", pro.get(i).fadr));
            bufferedWriter2.write(String.format("%-8s", pro.get(i).ladr));
            bufferedWriter2.write("\n");
            System.out.print(String.format("%-16s", pro.get(i).pname));
            System.out.print(String.format("%-16s", pro.get(i).ptype.toString()));
            System.out.print(String.format("%-8s", pro.get(i).plev));
            System.out.print(String.format("%-8s", pro.get(i).fadr));
            System.out.print(String.format("%-8s", pro.get(i).ladr));
            System.out.print("\n");
        }
        System.out.println();
    }

    /**
     * 写输出结果
     * @throws Exception
     */
    private void writeResults() throws Exception {
        if (isVirgin) {
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                bufferedWriter3.write(str + "\n");
            }
        } else {
            bufferedWriter3.write("Unable to pass the grammatical analysis!");
        }
    }

    /**
     * A:程序
     * A->B
     * @throws Exception
     */
    private void A() throws Exception {
        currentPro = new Procedures();
        B();
    }

    /**
     * B:分程序
     * B->begin C;M end
     * @throws Exception
     */
    private void B() throws Exception {
        if (input.get(currentToken).equals("begin")) {
            nextToken();
        } else {
            // 出错处理:跳到C
            printError(1, "begin");
            if (!input.get(currentToken).equals("integer"))
                nextToken();
        }
        C();
        if (input.get(currentToken).equals(";")) {
            nextToken();
        } else {
            // 出错处理:跳到M
            printError(1, ";");
            if ((!input.get(currentToken).equals("integer")) &&
                    (!input.get(currentToken).equals("read")) &&
                    (!input.get(currentToken).equals("write")) &&
                    (!type.get(currentToken).equals("10"))) {
                nextToken();
            }
        }
        M();
        if (input.get(currentToken).equals("end")) {
            nextToken();
        } else {
            printError(1, "end");
        }
    }

    /**
     * C:说明语句表(左递归)
     * C->DC'
     * @throws Exception
     */
    private void C() throws Exception {
        D();
        C_();
    }

    /**
     * C'
     * C'->;DC'|ε
     * @throws Exception
     */
    private void C_() throws Exception {
        int nextToken = getNextToken();
        if (input.get(currentToken).equals(";") && input.get(nextToken).equals("integer")) {
            nextToken();
            D();
            C_();
        }
        else if (input.get(currentToken).equals(";") && type.get(nextToken).equals("10")) {
            nextToken();
            // 出错处理:跳过一行
            printError(2, input.get(currentToken));
            jumpToNextLine();
            D();
            C_();
        } else {
            if (input.get(currentToken).equals("integer")) {
                // 出错处理:跳到D
                printError(1, ";");
                D();
                C_();
            }
        }
    }

    /**
     * D:说明语句
     * D->E|J
     * @throws Exception
     */
    private void D() throws Exception {
        if (input.get(getNextToken()).equals("function"))
            J();
        else
            E();
    }

    /**
     * E:变量说明
     * E->integer F
     * @throws Exception
     */
    private void E() throws Exception {
        if (input.get(currentToken).equals("integer"))
            nextToken();
        else {
            printError(1, "integer");
            nextToken();
        }
        // 创建新变量(实参)
        currentVar = new Variables();
        currentVar.vname = input.get(currentToken);
        currentVar.vproc = currentPro.pname;
        currentVar.vkind = 0;
        currentVar.vtype = Types.integer;
        currentVar.vlev = currentPro.plev;
        currentVar.vadr = var.size();
        if (isVarIllegal(currentVar.vname, currentVar.vproc, currentVar.vkind))
            printError(4, currentVar.vname);
        else {
            if (currentPro.varNum == 0)
                currentPro.fadr = currentVar.vadr;
            currentPro.ladr = currentVar.vadr;
            currentPro.varNum++;
            var.add(currentVar);
        }
        F();
    }

    /**
     * F:变量
     * F->G
     * @throws Exception
     */
    private void F() throws Exception {
        G();
    }

    /**
     * G:标识符(左递归)
     * G->HG'
     * @throws Exception
     */
    private void G() throws Exception {
        if (type.get(currentToken).equals("10"))
            nextToken();
    }

    /**
     * G'
     * G'->HG'|IG'|ε
     * @throws Exception
     */
    private void G_() throws Exception {}

    /**
     * H:字母
     * H->a|...|z|A|...|Z
     * @throws Exception
     */
    private void H() throws Exception {}

    /**
     * I:数字
     * I->0|1|...|9
     * @throws Exception
     */
    private void I() throws Exception {}

    /**
     * J:函数说明
     * J->integer function G(K);L
     * @throws Exception
     */
    private void J() throws Exception {
        // 创建新进程
        Procedures temp = new Procedures();
        temp.pname = currentPro.pname;
        temp.ptype = currentPro.ptype;
        temp.plev = currentPro.plev;
        temp.fadr = currentPro.fadr;
        temp.ladr = currentPro.ladr;
        temp.varNum = currentPro.varNum;
        if (input.get(currentToken).equals("integer"))
            nextToken();
        else {
            // 出错处理:跳到:"function"
            printError(1, "integer");
            if (!input.get(currentToken).equals("function"))
                nextToken();
        }
        if (input.get(currentToken).equals("function"))
            nextToken();
        else {
            // 出错处理:跳到G
            printError(1, "function");
            if (!type.get(currentToken).equals("10"))
                nextToken();
        }
        currentPro.pname = input.get(currentToken);
        currentPro.ptype = Types.integer;
        currentPro.plev++;
        currentPro.varNum = 0;
        if (isProIllegal(input.get(currentToken)))
            printError(4, currentPro.pname);
        G();
        if (input.get(currentToken).equals("("))
            nextToken();
        else {
            // 出错处理:跳到K
            printError(1, "(");
            if (!type.get(currentToken).equals("10"))
                nextToken();
        }
        K();
        if (input.get(currentToken).equals(")"))
            nextToken();
        else {
            // 出错处理:跳到";"
            printError(1, ")");
            if (!input.get(currentToken).equals(";"))
                nextToken();
        }
        if (input.get(currentToken).equals(";"))
            nextToken();
        else {
            // 出错处理:跳到L
            printError(1, ";");
            if (!input.get(currentToken).equals("begin"))
                nextToken();
        }
        L();
        currentPro = temp;
    }

    /**
     * K:参数
     * K->F
     * @throws Exception
     */
    private void K() throws Exception {
        // 创建新变量(形参)
        currentVar = new Variables();
        currentVar.vname = input.get(currentToken);
        currentVar.vproc = currentPro.pname;
        currentVar.vkind = 1;
        currentVar.vtype = Types.integer;
        currentVar.vlev = currentPro.plev;
        currentVar.vadr = var.size();
        if (isVarIllegal(currentVar.vname, currentVar.vproc, currentVar.vkind))
            printError(4, currentVar.vname);
        else {
            if (currentPro.varNum == 0)
                currentPro.fadr = currentVar.vadr;
            currentPro.ladr = currentVar.vadr;
            currentPro.varNum++;
            var.add(currentVar);
        }
        F();
    }

    /**
     * L:函数体
     * L->begin C;M end
     * @throws Exception
     */
    private void L() throws Exception {
        if (input.get(currentToken).equals("begin"))
            nextToken();
        else {
            // 出错处理:跳到C
            printError(1, "begin");
            if (!input.get(currentToken).equals("integer"))
                nextToken();
        }
        C();
        pro.add(currentPro);
        if (input.get(currentToken).equals(";"))
            nextToken();
        else {
            // 出错处理:跳到M
            printError(1, ";");
            if ((!input.get(currentToken).equals("integer")) &&
                    (!input.get(currentToken).equals("read")) &&
                    (!input.get(currentToken).equals("write")) &&
                    (!type.get(currentToken).equals("10"))) {
                nextToken();
            }
        }
        M();
        if (input.get(currentToken).equals("end"))
            nextToken();
        else {
            // 出错处理:跳到"end"or";"
            printError(1, "end");
            if ((!input.get(currentToken).equals(";")) &&
                    (!input.get(currentToken).equals("end")))
                nextToken();
        }
    }

    /**
     * M:执行语句表(左递归)
     * M->NM'
     * @throws Exception
     */
    private void M() throws Exception {
        N();
        M_();
    }

    /**
     * M'
     * M'->;NM'|ε
     * @throws Exception
     */
    private void M_() throws Exception {
        if (input.get(currentToken).equals(";")) {
            nextToken();
            N();
            M_();
        } else {
            if (!input.get(currentToken).equals("end") &&
                    (!input.get(currentToken).equals("EOF"))) {
                // 出错处理:跳到"end"
                printError(1, ";");
                N();
                M_();
            }
        }
    }

    /**
     * N:执行语句
     * N->O|P|Q|W
     * @throws Exception
     */
    private void N() throws Exception {
        if (input.get(currentToken).equals("read"))
            O();
        else if (input.get(currentToken).equals("write"))
            P();
        else if (input.get(currentToken).equals("if"))
            W();
        else if (type.get(currentToken).equals("10"))
            Q();
        else {
            printError(2, input.get(currentToken));
            nextToken();
        }
    }

    /**
     * O:读语句
     * O->read(F)
     * @throws Exception
     */
    private void O() throws Exception {
        if (input.get(currentToken).equals("read"))
            nextToken();
        else {
            // 出错处理:跳到"("
            printError(1, "read");
            if (!input.get(currentToken).equals("("))
                nextToken();
        }
        if (input.get(currentToken).equals("("))
            nextToken();
        else {
            // 出错处理:跳到F
            printError(1, "(");
            if (!type.get(currentToken).equals("10"))
                nextToken();
        }
        if (!isVarIllegal(input.get(currentToken), currentPro.pname, 0) &&
                !isVarIllegal(input.get(currentToken), currentPro.pname, 1))
            printError(3, input.get(currentToken));
        F();
        if (input.get(currentToken).equals(")"))
            nextToken();
        else {
            // 出错处理:跳到";"or"end"
            printError(1, ")");
            if ((!input.get(currentToken).equals(";")) &&
                    !input.get(currentToken).equals("end"))
                nextToken();
        }
    }

    /**
     * P:写语句
     * P->write(F)
     * @throws Exception
     */
    private void P() throws Exception {
        if (input.get(currentToken).equals("write"))
            nextToken();
        else {
            // 出错处理:跳到"("
            printError(1, "write");
            if (!input.get(currentToken).equals("("))
                nextToken();
        }
        if (input.get(currentToken).equals("("))
            nextToken();
        else {
            // 出错处理:跳到F
            printError(1, "(");
            if (!type.get(currentToken).equals("10"))
                nextToken();
        }
        if ((!isVarIllegal(input.get(currentToken), currentPro.pname, 0)) &&
                (!isVarIllegal(input.get(currentToken), currentPro.pname, 1)))
            printError(3, input.get(currentToken));
        F();
        if (input.get(currentToken).equals(")"))
            nextToken();
        else {
            // 出错处理:跳到"end"
            printError(1, ")");
            if ((!input.get(currentToken).equals(";")) &&
                    (!input.get(currentToken).equals("end")))
                nextToken();
        }
    }

    /**
     * Q:赋值语句
     * Q->F:=R
     * @throws Exception
     */
    private void Q() throws Exception {
        if ((!isVarIllegal(input.get(currentToken), currentPro.pname, 0)) &&
                (!isVarIllegal(input.get(currentToken), currentPro.pname, 1))) {
            printError(3, input.get(currentToken));
        }
        F();
        if (input.get(currentToken).equals(":="))
            nextToken();
        else {
            // 出错处理:跳到R
            printError(1, ":=");
            if ((!type.get(currentToken).equals("10")) &&
                    (!type.get(currentToken).equals("11")))
                nextToken();
        }
        R();
    }

    /**
     * R:算术表达式(左递归)
     * R->SR'
     * @throws Exception
     */
    private void R() throws Exception {
        S();
        R_();
    }

    /**
     * R'
     * R'->-SR'|ε
     * @throws Exception
     */
    private void R_() throws Exception {
        if (input.get(currentToken).equals("-")) {
            nextToken();
            S();
            R_();
        } else {
            if (type.get(currentToken).equals("10") || type.get(currentToken).equals("11")) {
                S();
                R_();
            }
        }
    }

    /**
     * S:项(左递归)
     * S->TS'
     * @throws Exception
     */
    private void S() throws Exception {
        T();
        S_();
    }

    /**
     * S'
     * S'->*TS'|ε
     * @throws Exception
     */
    private void S_() throws Exception {
        if (input.get(currentToken).equals("*")) {
            nextToken();
            T();
            S_();
        } else {
            if (type.get(currentToken).equals("10") || type.get(currentToken).equals("11")) {
                T();
                S_();
            }
        }
    }

    /**
     * T:因子
     * T->F|U|Z
     * @throws Exception
     */
    private void T() throws Exception {
        if (type.get(currentToken).equals("11"))
            U();
        else if (input.get(getNextToken()).equals("("))
            Z();
        else {
            if ((!isVarIllegal(input.get(currentToken), currentPro.pname, 0)) &&
                    (!isVarIllegal(input.get(currentToken), currentPro.pname, 1)))
                printError(3, input.get(currentToken));
            F();
        }
    }

    /**
     * U:常数
     * U->V
     * @throws Exception
     */
    private void U() throws Exception {
        if (type.get(currentToken).equals("11"))
            nextToken();
    }

    /**
     * V:无符号整数(左递归)
     * V->IV'
     * @throws Exception
     */
    private void V() throws Exception {}

    /**
     * V'
     * V'->IV'|ε
     * @throws Exception
     */
    private void V_() throws Exception {}

    private void W() throws Exception {
        if (input.get(currentToken).equals("if"))
            nextToken();
        else {
            // 出错处理:跳到X
            printError(1, "if");
            if ((!type.get(currentToken).equals("10")) &&
                    !(type.get(currentToken).equals("11")))
                nextToken();
        }
        X();
        if (input.get(currentToken).equals("then"))
            nextToken();
        else {
            printError(1, "then"); // N
            if ((!input.get(currentToken).equals("integer")) &&
                    (!input.get(currentToken).equals("read")) &&
                    (!input.get(currentToken).equals("write")) &&
                    (!type.get(currentToken).equals("10"))) {
                nextToken();
            }
        }
        N();
        if (input.get(currentToken).equals("else"))
            nextToken();
        else {
            printError(1, "else"); // N
            if ((!input.get(currentToken).equals("integer")) &&
                    (!input.get(currentToken).equals("read")) &&
                    (!input.get(currentToken).equals("write")) &&
                    (!type.get(currentToken).equals("10"))) {
                nextToken();
            }
        }
        N();
    }

    /**
     * X:条件表达式
     * X->RYR
     * @throws Exception
     */
    private void X() throws Exception {
        R();
        Y();
        R();
    }

    /**
     * Y:关系运算符
     * Y-><|<=|>|>=|=|<>
     * @throws Exception
     */
    private void Y() throws Exception {
        if (input.get(currentToken).equals("<") ||
                input.get(currentToken).equals("<=") ||
                input.get(currentToken).equals(">") ||
                input.get(currentToken).equals(">=") ||
                input.get(currentToken).equals("<>") ||
                input.get(currentToken).equals("="))
            nextToken();
        else {
            // 出错处理:跳到R
            printError(1, "运算符");
            if ((!type.get(currentToken).equals("10")) &&
                    (!type.get(currentToken).equals("11")))
                nextToken();
        }
    }

    /**
     * Z:函数调用
     * Z->G(R)
     * @throws Exception
     */
    private void Z() throws Exception {
        if (!isProIllegal(input.get(currentToken)))
            printError(3, input.get(currentToken));
        G();
        if (input.get(currentToken).equals("("))
            nextToken();
        else {
            // 出错处理:跳到R
            printError(1, "(");
            if ((!type.get(currentToken).equals("10")) &&
                    (!type.get(currentToken).equals("11")))
                nextToken();
        }
        R();
        if (input.get(currentToken).equals(")"))
            nextToken();
        else {
            // 出错处理:跳到R
            printError(1, ")");
            if ((!input.get(currentToken).equals("-")) &&
                    (!input.get(currentToken).equals("*")) &&
                    (!input.get(currentToken).equals(";")) &&
                    (!input.get(currentToken).equals("end")))
                nextToken();
        }
    }
}
