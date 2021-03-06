package com.frederon;

import org.apache.commons.io.output.TeeOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

class Logger {

    private static DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy@HH-mm-ss");
    private static FileOutputStream stream;
    private static TeeOutputStream out;
    private static PrintStream ps;

    public void setupLogger() throws IOException{
        File file = new File("results-" + dateFormat.format(Calendar.getInstance().getTime()) + ".log");
        try {
            stream = new FileOutputStream(file);
            out = new TeeOutputStream(System.out, stream);
            ps = new PrintStream(out, true); //true - auto-flush after println
            System.setOut(ps);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeLogger() {
        ps.close();
    }
}
