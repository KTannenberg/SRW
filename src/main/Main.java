package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import loader.Table;

import org.yaml.snakeyaml.Yaml;

import runtime.Scheduler;
import runtime.SchedulerException;

public class Main {

    public final static String help = 
            "Available commands:" +
            " 'help' prints this message" +
            " 'cadd' adds a channel" +
            " 'crem <channel_number>' removes a channel" +
            " 'wadd' adds a worker" +
            " 'wrem <worker_number>' removes a worker" +
            " 'inpd <channel_number> <data_name> <data_string> [<event_name>]' inputs a data to channel" +
            " 'inpe <channel_number> <event_name>' adds event to channel" +
            " 'stop' stops scheduler and all workers"
            ;
    
    public static void main(String[] args) throws IOException {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        int state = -1;
        
        String module = null;
        String table = null;
        String mainTable = null;
        String mainState = null;
        int workers = -1;
        int channels = -1;
        
        
  conf: for(String line = ""; line != null && !line.equalsIgnoreCase("$exit"); line = console.readLine()) {
            switch(state) {
                case -1:
                    sopln("Hi, lets configure server, before we start it, get ready to input information about location of modules and tables");
                    sopnl("Please, input path to .class directory, containing modules > ");
                    state = 0; 
                    break;
                case 0:
                    File moduleDir = new File(line);
                    if(moduleDir.isDirectory()) {
                        String[] files = moduleDir.list(new FilenameFilter() {
                            public boolean accept(File dir, String name) {
                                return name.matches("\\w+.class");
                            }
                        });
                        if(files.length > 0) {
                            sopln("You have specified directory, that contains " + files.length + " .class files");
                            sopln(Arrays.toString(files));
                            sopnl("Do you accept? (Y/N) > ");
                            module = line;
                            state = 1;
                        } else {
                            sopnl("You have specified directory, that doesn't contain .class files, try again > ");
                        }
                    } else {
                        sopnl("You have specified path, that isn't a directory, try again > ");
                    }
                    break;
                case 1:
                    if(line.equalsIgnoreCase("Y")) {
                        sopln("Server will use modules from '" + module + "' directory");
                        sopnl("Now specify path to .table directory, containing tables > ");
                        state = 2;
                    } else if(line.equalsIgnoreCase("N")) {
                        sopnl("Please, specify other directory > ");
                        state = 0;
                    } else {
                        sopnl("Only accept 'Y' or 'N', try again > ");
                    }
                    break;
                case 2:
                    File tableDir = new File(line);
                    if(tableDir.isDirectory()) {
                        String[] files = tableDir.list(new FilenameFilter() {
                            public boolean accept(File dir, String name) {
                                return name.matches("\\w+.table");
                            }
                        });
                        if(files.length > 0) {
                            sopln("You have specified directory, that contains " + files.length + " .table files");
                            sopln(Arrays.toString(files));
                            sopnl("Do you accept? (Y/N) > ");
                            table = line;
                            state = 3;
                        } else {
                            sopnl("You have specified directory, that doesn't contain .table files, try again > ");
                        }
                    } else {
                        sopnl("You have specified path, that isn't a directory, try again > ");
                    }
                    break;
                case 3:
                    if(line.equalsIgnoreCase("Y")) {
                        sopln("Server will use tables from '" + table + "' directory");
                        sopnl("Now specify quantity of channels and workers (e.g. '10 4') > ");
                        state = 4;
                    } else if(line.equalsIgnoreCase("N")) {
                        sopnl("Please, specify other directory > ");
                        state = 2;
                    } else {
                        sopnl("Only accept 'Y' or 'N', try again > ");
                    }
                    break;
                case 4:
                    if(line.matches("\\d+ +\\d+")) {
                        String[] numbers = line.split(" +");
                        channels = Integer.parseInt(numbers[0]);
                        workers = Integer.parseInt(numbers[1]);
                        sopln("Server will work with " + channels + " channels and " + workers + " workers");
                        sopnl("Do you accept? (Y/N) > ");
                        state = 5;
                    } else {
                        sopnl("Wrong number format (e.g. '10 4'), try again > ");
                    }
                    break;
                case 5:
                    if(line.equalsIgnoreCase("Y")) {
                        sopln("You have accepted configuration with " + channels + " channels and " + workers + " workersServer will use tables from '" + table + "' directory");
                        sopln("\nServer configuration has been completed");
                        sopln("Curent server configuration is:");
                        sopln("    modules:  " + module);
                        sopln("    tables:   " + table);
                        sopln("    workers:  " + workers);
                        sopln("    channels: " + channels);
                        sopnl("Do you accept? this config (Y/N) > ");
                        state = 6;
                    } else if(line.equalsIgnoreCase("N")) {
                        sopnl("Please, specify other quantity of channels and workers > ");
                        state = 4;
                    } else {
                        sopnl("Only accept 'Y' or 'N', try again > ");
                    }
                    break;
                case 6:
                    if(line.equalsIgnoreCase("Y")) {
                        sopln("You have almost finished");
                        sopnl("Enter please name of main table and main state in it (e.g. 'tbl_1 stt_1') > ");
                        state = 7;
                    } else if(line.equalsIgnoreCase("N")) {
                        sopln("Here we go from a scratch\n");
                        state = -1;
                    } else {
                        sopnl("Only accept 'Y' or 'N', try again > ");
                    }
                    break;
                case 7:
                    if(line.matches("\\w+ +\\w+")) {
                        String[] info = line.split(" +");
                        mainTable = info[0];
                        mainState = info[1];
                        sopln("Main table is '" + mainTable + "', main state is '" + mainState + "'");
                        sopnl("This data can only be checked in runtime, do you sure you enter right? (Y/N) > ");
                        state = 8;
                    } else {
                        sopnl("Try again (e.g. 'tbl_1 stt_1') > ");
                    }
                    break;
                case 8:
                    if(line.equalsIgnoreCase("Y")) {
                        sopln("You have finished configuration");
                        break conf;
                    } else if(line.equalsIgnoreCase("N")) {
                        sopnl("Enter other main table and main state > ");
                        state = 7;
                    } else {
                        sopnl("Only accept 'Y' or 'N', try again > ");
                    }
                    break;
            }
        }
        
        Scheduler scheduler = new Scheduler(workers, channels, table, module, mainTable, mainState);
        new Thread(scheduler).start();
        sopln(help);
        sopnl("Input > ");
 input: for(String line = console.readLine(); line != null && !line.equalsIgnoreCase("$exit"); line = console.readLine()) {
            String[] parts = line.split(" +");
            switch(parts[0].hashCode()) {
                case 3198785:
                    sopln(help); break;
                case 3045726:
                    sopln("MAIN: Creating a channel");
                    try {
                        scheduler.createChannel();
                    } catch (SchedulerException e) {
                        sopln(e.getMessage());
                    } break;
                case 3641546:
                    sopln("MAIN: Creating a worker");
                    try {
                        scheduler.createWorker();
                    } catch (SchedulerException e) {
                        sopln(e.getMessage());
                    } break;
                case 3540994:
                    sopln("MAIN: Stopping scheduler");
                    scheduler.stop(); break input;
                case 3062103:
                    sopln("MAIN: Removing a channel");
                    if(parts.length == 2 && parts[1].matches("\\d+")) {
                        int cnum = Integer.parseInt(parts[1]);
                        sopln("MAIN: Removing a channel ID" + cnum);
                        try {
                            scheduler.removeChannel(cnum);
                        } catch (SchedulerException e) {
                            sopln(e.getMessage());
                        }
                    } else {
                        sopln("MAIN: Wrong number format or not enough arguments");
                    }
                    break;
                case 3657923:
                    sopln("MAIN: Removing a worker");
                    if(parts.length == 2 && parts[1].matches("\\d+")) {
                        int wnum = Integer.parseInt(parts[1]);
                        sopln("MAIN: Removing a worker ID" + wnum);
                        try {
                            scheduler.removeWorker(wnum);
                        } catch (SchedulerException e) {
                            sopln(e.getMessage());
                        }
                    } else {
                        sopln("MAIN: Wrong number format or not enough arguments");
                    }
                    break;
                case 3237338:
                    //event
                    sopln("MAIN: Creating an event");
                    if(parts.length == 3 && parts[1].matches("\\d+")) {
                        int cnum = Integer.parseInt(parts[1]);
                        sopln("MAIN: Creating an event in channel ID" + cnum);
                        try {
                            scheduler.createEvent(cnum, parts[2]);
                        } catch (SchedulerException e) {
                            sopln(e.getMessage());
                        }
                    } else {
                        sopln("MAIN: Wrong number format or not enough arguments");
                    }
                    break;
                case 3237337:
                    //data
                    sopln("MAIN: Inputing a data");
                    if(parts.length > 3 && parts[1].matches("\\d+")) {
                        int cnum = Integer.parseInt(parts[1]);
                        sopln("MAIN: Inputing a data to channel ID" + cnum);
                        try {
                            scheduler.inputData(cnum, parts[2], parts[3], parts.length == 5 ? parts[4] : null);
                        } catch (SchedulerException e) {
                            sopln(e.getMessage());
                        }
                    } else {
                        sopln("MAIN: Wrong number format or not enough arguments");
                    }
                    break;
                case 109757585:
                    sopln("MAIN: State of scheduler");
                    sopln(scheduler.getState()); break;
                default:
                    sopln("Unknown command"); break;
            }
            sopnl("Input > ");
        }
        
        System.out.println("the craken has been realeased");
    }

    private static void sopnl(String input) {
        System.out.print(input);
    }
    private static void sopln(String input) {
        System.out.println(input);
    }
}
