package uj.wmii.pwj.gvt;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Stream;

class VersionHelper {
//    private long number;
//    private String message;
//    private ArrayList<String> files;

    /**
     * Adds new version with given parameters to .gvt directory
     * @param number
     * @param message
     * @param files
     */
    public static void addVersion(long number, String message, String... files) throws IOException {
        File newVersion = new File(".gvt/"+Long.toString(number));
        try {
            newVersion.createNewFile();
            FileWriter fileWriter = new FileWriter(".gvt/"+Long.toString(number));
            fileWriter.write(message);
            //if(files!=null) {
                fileWriter.write("\n");
                for(int i=0; i<files.length-1; i++){
                    fileWriter.write(files[i]+"\n");
                }
                if(files.length!=0) {
                    fileWriter.write(files[files.length - 1]);
                }
            //}
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setLastVersion(number);
    }

    //public static void addVersion(long number, String message){
    //    addVersion(number,message,null);
    //}

    public static long getLastVersion(){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(".gvt/lastVersion.txt"));
            return Long.parseLong(reader.readLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void setLastVersion(long newVersion) throws IOException {
        FileWriter fileWriter = new FileWriter(".gvt/lastVersion.txt");
        fileWriter.write(Long.toString(newVersion));
        fileWriter.close();
    }
}
