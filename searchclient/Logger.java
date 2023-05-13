package searchclient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

    private static Logger instance;
    private static File logFile;
    private static FileWriter logWriter;
 
    private Logger() {
        logFile = new File("logs4.txt");
        try {
            logWriter = new FileWriter(logFile, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    public static Logger getInstance() {
        if(instance == null) {
            instance = new Logger();
        }
        return instance;
    }
 
    public void log(String message) {
        try {
            logWriter.write(message + "\n");
            logWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}