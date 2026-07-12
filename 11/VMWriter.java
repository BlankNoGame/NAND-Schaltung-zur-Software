import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {

    private final BufferedWriter writer;

    public VMWriter(File output) throws IOException {
        writer = new BufferedWriter(new FileWriter((output)));
    }

    public void writePush(Segment seg, int index) throws IOException {
        writer.write("push " + seg.toString() + " " + index);
        writer.newLine();
    }

    public void writePop(Segment seg, int index) throws IOException {
        writer.write("pop " + seg.toString() + " " + index);
        writer.newLine();
    }

    public void writeArithmetic(Command command) throws IOException {
        writer.write(command.toString());
        writer.newLine();
    }

    public void writeLabel(String label) throws IOException {
        writer.write("label " + label);
        writer.newLine();
    }

    public void writeGoto(String label) throws IOException {
        writer.write("goto " + label);
        writer.newLine();
    }

    public void writeIf(String label) throws IOException {
        writer.write("if-goto " + label);
        writer.newLine();
    }

    public void writeCall(String name, int nArgs) throws IOException {
        writer.write("call " + name + " " + nArgs);
        writer.newLine();
    }

    public void writeFunction(String name, int nVars) throws IOException {
        writer.write("function " + name + " " + nVars);
        writer.newLine();
    }

    public void writeReturn() throws IOException {
        writer.write("return");
        writer.newLine();
    }

    public void close() throws IOException {
        writer.close();
    }

}
