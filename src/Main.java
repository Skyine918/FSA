import java.io.*;

public class Main {

    public static void main(String[] args) {

        FSA_new fsa = new FSA_new("fsa.txt", "result.txt");

        FileWriter fw = null;
        BufferedWriter bw = null;

        try {

            fw = new FileWriter("result.txt", true);
            bw = new BufferedWriter(fw);

            if (fsa.isValid) {
                String str = fsa.kleeneAlgorithm();
                if (str.equals("{}")) {
                    bw.write(str);
                } else {
                    bw.write(str.substring(0, fsa.kleeneAlgorithm().length() - 1));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}


