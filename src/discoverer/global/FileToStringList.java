package discoverer.global;

import java.util.*;
import java.io.*;

/**
 * Ugly java6 filetostring
 */
public class FileToStringList {

    /**
     * converts a given string into a string array line by line
     *
     * @param p
     * @param maxline
     * @param maxLineLength
     * @return
     */
    public static String[] convert(String p, int maxline) {
        if (p == null) {
            return null;
        }
        List<String> lines = new ArrayList<String>();
        BufferedReader buffReader = null;
        try {
            buffReader = new BufferedReader(new FileReader(p));
            String line = null;
            while ((line = buffReader.readLine()) != null) {
                if (line.length() != 0 && line.length() < maxline) {
                    lines.add(line);
                }
            }

        } catch (Exception ioe) {
            Glogger.out(ioe.getMessage() + "- file not found");
        } finally {
            try {
                buffReader.close();
            } catch (Exception ioe1) {
                //Glogger.err(ioe1.getMessage());
            }
        }

        return ListToArray(lines);
    }

    public static String[] convertMultiline(String p, int maxline) {
        if (p == null) {
            return null;
        }

        List<String> lines = new ArrayList<String>();
        BufferedReader buffReader = null;
        try {
            buffReader = new BufferedReader(new FileReader(p));
            String line = null;
            StringBuilder ex = new StringBuilder();
            while ((line = buffReader.readLine()) != null) {
                if (!line.equals("")) {
                    ex.append(line);
                } else {
                    lines.add(ex.toString());
                    ex = new StringBuilder();
                }
            }
            if (ex.length() != 0){
                lines.add(ex.toString());
            }

        } catch (Exception ioe) {
            Glogger.out(ioe.getMessage() + "- file not found");
        } finally {
            try {
                buffReader.close();
            } catch (Exception ioe1) {
                //Glogger.err(ioe1.getMessage());
            }
        }

        return ListToArray(lines);
    }

    /*
     converts a given list into array
     */
    private static String[] ListToArray(List<String> list) {
        String[] ret = new String[list.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = list.get(i);
        }

        return ret;
    }
}
