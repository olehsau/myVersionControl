package uj.wmii.pwj.gvt;

import java.io.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class Gvt {

    private final ExitHandler exitHandler;

    public Gvt(ExitHandler exitHandler) {
        this.exitHandler = exitHandler;
    }

    public static void main(String... args) {
        Gvt gvt = new Gvt(new ExitHandler());
        gvt.mainInternal(args);
    }

    void mainInternal(String... args) {
        if (args.length == 0) {
            exitHandler.exit(1, "Please specify command.");
        }
        else if (args[0].equals("init")) {
            if ((new File(".gvt")).mkdir()) {
                File curVersion = new File(".gvt/curVersion.txt");
                File lastVersion = new File(".gvt/lastVersion.txt");
                File stage = new File(".gvt/stage.txt");  // files go there after add command
                File filesFolder = new File(".gvt/filesFolder");
                try {
                    curVersion.createNewFile();
                    lastVersion.createNewFile();
                    stage.createNewFile();
                    filesFolder.mkdir();
                    FileWriter fileWriter = new FileWriter(".gvt/curVersion.txt");
                    fileWriter.write("0");
                    fileWriter.close();
                    fileWriter = new FileWriter(".gvt/lastVersion.txt");
                    fileWriter.write("0");
                    fileWriter.close();
                    VersionHelper.addVersion(0, "GVT initialized.");
                    System.out.println("Current directory initialized successfully.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                exitHandler.exit(10, "Current directory is already initialized.");
            }
        }
        else if (new File(".gvt").exists() == false) {
            exitHandler.exit(-2, "Current directory is not initialized. Please use init command to initialize.");
        }
        else if (args[0].equals("add")) {
            if (args.length >= 2) {
                String fileName = args[1];
                if (new File(fileName).exists() == false) {  // if there is no such file
                    exitHandler.exit(21, "File not found. File: " + fileName);
                }
                long newVersion = VersionHelper.getLastVersion() + 1;
                if (args.length >= 3) {
                    if (args[2].equals("-m")) {
                        if (args.length == 4) {
                            //if(args[3].charAt(0)=='"' && args[3].charAt(args[3].length()-1)=='"'){
                            try {
                                VersionHelper.addVersion(newVersion, "Added file: " + fileName + " " + args[3], fileName+"-"+newVersion);
                                BufferedReader reader = new BufferedReader(new FileReader(".gvt/stage.txt"));
                                String fileFromStage;
                                boolean alreadyAdded = false;
                                while ((fileFromStage = reader.readLine()) != null) {
                                    if (fileFromStage.strip().equals(fileName)) {
                                        alreadyAdded = true;
                                        break;
                                    }
                                }
                                if (alreadyAdded == true) {
                                    exitHandler.exit(-111, "File already added. File: " + fileName);
                                } else {
                                    FileWriter fileWriter = new FileWriter(".gvt/stage.txt", true);
                                    fileWriter.write(fileName + "\n");
                                    fileWriter.close();
                                    File fileToCopyToFilesFolder = new File(fileName);
                                    File copyOfThatFile = new File(".gvt/filesFolder/" + fileName+"-"+newVersion);
                                    copyOfThatFile.createNewFile();
                                    copyFileUsingStream(fileToCopyToFilesFolder, copyOfThatFile);
                                    VersionHelper.addVersion(newVersion, "Added file: " + fileName, fileName);
                                    System.out.println("File added successfully. File: " + fileName);
                                }
                            } catch (IOException e) {
                                System.out.println("File cannot be added. See ERR for details. File: " + fileName);
                                e.printStackTrace();
                                exitHandler.exitOperation(22);
                            }
                            //}else {exitHandler.exit(1,"Message text should be inside \"\" (quotation marks)");}
                        } else {
                            exitHandler.exit(1, "Bad command");
                        }
                    } else {
                        exitHandler.exit(1, "Bad command");
                    }
                } else {
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(".gvt/stage.txt"));
                        String fileFromStage;
                        boolean alreadyAdded = false;
                        while ((fileFromStage = reader.readLine()) != null) {
                            if (fileFromStage.strip().equals(fileName)) {
                                alreadyAdded = true;
                                break;
                            }
                        }
                        if (alreadyAdded == true) {
                            exitHandler.exit(-111, "File already added. File: " + fileName);
                        } else {
                            FileWriter fileWriter = new FileWriter(".gvt/stage.txt", true);
                            fileWriter.write(fileName + "\n");
                            fileWriter.close();
                            File fileToCopyToFilesFolder = new File(fileName);
                            File copyOfThatFile = new File(".gvt/filesFolder/" + fileName+"-"+newVersion);
                            copyOfThatFile.createNewFile();
                            copyFileUsingStream(fileToCopyToFilesFolder, copyOfThatFile);
                            VersionHelper.addVersion(newVersion, "Added file: " + fileName, fileName+"-"+newVersion);
                            System.out.println("File added successfully. File: " + fileName);
                        }
                    } catch (IOException e) {
                        System.out.println("File cannot be added. See ERR for details. File: " + fileName);
                        e.printStackTrace();
                        exitHandler.exitOperation(22);
                    }
                }
            } else {
                exitHandler.exit(20, "Please specify file to add.");
            }
        } else if (args[0].equals("commit")) {
            if (args.length < 2) {
                exitHandler.exit(50, "Please specify file to commit.");
            } else{
                String extraMessage = "";
            if (args.length == 4 && args[2].equals("-m")) {
                extraMessage = args[3];
            }
            String fileName = args[1];
            try {
                if (fileIsInStage(fileName) && new File(fileName).exists()) {
                    VersionHelper.addVersion(VersionHelper.getLastVersion() + 1, "Committed file: " + fileName + " " + extraMessage,
                            fileName + "-" + Long.toString(VersionHelper.getLastVersion() + 1));
                    //addFileToStage(fileName); maybe don't need this
                    copyFileUsingStream(new File(fileName), new File(".gvt/filesFolder/" + fileName
                            + "-" + VersionHelper.getLastVersion()));
                    System.out.println("File committed successfully. File: " + fileName);
                } else if (fileIsInStage(fileName) == false) {
                    System.out.println("File is not added to gvt. File: " + fileName);
                } else if (new File(fileName).exists() == false) {
                    exitHandler.exit(51, "File not found. File: " + fileName);
                }
            } catch (IOException e) {
                System.out.println("File cannot be committed, see ERR for details. File: " + fileName);
                e.printStackTrace();
                exitHandler.exitOperation(52);
            }
        }
    }

        else if(args[0].equals("detach")){
            if(args.length<2){
                exitHandler.exit(30,"Please specify file to detach.");
            }
            else{
                String fileName = args[1];
                try {
                    if(fileIsInStage(fileName)==false){
                        System.out.println("File is not added to gvt. File: "+fileName);
                    }
                    else {
                        removeFileFromStage(fileName);
//                        File filesFolder = new File(".gvt/filesFolder");
//                        for (File file : filesFolder.listFiles()){
//                            if(file.getName().split("-")[0].equals(fileName)){
//                                file.delete();
//                            }
//                        }
                        String message="";
                        if(args.length==4 && args[2].equals("-m")){message=args[3];}
                        VersionHelper.addVersion(VersionHelper.getLastVersion()+1,
                                "File detached successfully. File: "+fileName+" "+message,fileName);
                        System.out.println("File detached successfully. File: "+fileName);
                    }
                } catch (IOException e) {
                    System.out.println("File cannot be detached, see ERR for details. File: "+fileName);
                    e.printStackTrace();
                    exitHandler.exitOperation(31);
                }
            }
        }

        else if(args[0].equals("checkout")){
            if(args.length<2){
                exitHandler.exit(-111,"where is version???");
            }
            else {
                String version = args[1];
                try {
                    if (Long.parseLong(args[1]) < 0 || Long.parseLong(args[1]) > VersionHelper.getLastVersion()) {
                        exitHandler.exit(40, "Invalid version number: " + version);
                    }
                    else{
                        // delete all files
                        BufferedReader reader = new BufferedReader(new FileReader(".gvt/stage.txt"));
                        String line;
                        while ((line = reader.readLine())!=null){
                            File file = new File(line);
                            file.delete();
                        }
                        // moving through versions from 1 up to specified version
                        long versionLong = Long.parseLong(version);
                        for(long i=1; i<=versionLong; i++){
                            BufferedReader versionReader = new BufferedReader(new FileReader(".gvt/"+i));
                            String command = versionReader.readLine().split(" ")[0];
                            String fileName = versionReader.readLine();
                            switch (command){
                                case "Added","Committed":
                                    File fileMain = new File(fileName.split("-")[0]);
                                    if(fileMain.exists()==false){fileMain.createNewFile();}
                                    File fileFromFolder = new File(".gvt/filesFolder/"+fileName);
                                    copyFileUsingStream(fileFromFolder, fileMain);
                            }
                        }
                        System.out.println("Version "+version+" checked out successfully.");
                    }
                }catch (NumberFormatException e){
                    exitHandler.exit(40, "Invalid version number: " + version);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    exitHandler.exitOperation(-123);
                } catch (IOException e) {
                    e.printStackTrace();
                    exitHandler.exitOperation(-124);
                }
            }
        }
        else if(args[0].equals("history")){
            long n = VersionHelper.getLastVersion(); // number of versions to show (latest-n, latest-n+1,...,latest)
            if(args.length>1){
                if(args[1].equals("-last")){
                    n = Long.parseLong(args[2])-1;
                }
            }

            for(long i=VersionHelper.getLastVersion()-n; i<=VersionHelper.getLastVersion(); i++){
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(".gvt/"+i));
                    System.out.println(i+": "+reader.readLine());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else if(args[0].equals("version")){
            long numberOfVersion = VersionHelper.getLastVersion();
            if(args.length>1){
                try {
                    numberOfVersion = Long.parseLong(args[1]);
                }catch (NumberFormatException e){
                    exitHandler.exit(60,"Invalid version number: "+args[1]+".");
                }
                if(new File(".gvt/"+numberOfVersion).exists()==false){
                    exitHandler.exit(60,"Invalid version number: "+args[1]+".");
                }
            }
            System.out.println("Version: "+numberOfVersion);
            try {
                BufferedReader reader = new BufferedReader(new FileReader(".gvt/"+numberOfVersion));
                System.out.println(reader.readLine());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        else{
            exitHandler.exit(1,"Unknown command "+args[0]+".");
        }
    }

    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    private static boolean fileIsInStage(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(".gvt/stage.txt"));
        String line;
        while ((line = reader.readLine())!=null){
            if(line.equals(fileName)){
                return true;
            }
        }
        return false;
    }

    private static void addFileToStage(String fileName) throws IOException {
        FileWriter writer = new FileWriter(".gvt/stage.txt",true);
        writer.write(fileName+"\n");
        writer.close();
    }

    private static void removeFileFromStage(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(".gvt/stage.txt"));
        File temp = new File(".gvt/stageTemp.txt");
        temp.createNewFile();
        FileWriter writer = new FileWriter(".gvt/stageTemp.txt",true);
        String line;
        while ((line = reader.readLine())!=null){
            if(line.equals(fileName)==false){
                writer.write(line+"\n");
            }
        }
        writer.close();
        copyFileUsingStream(temp,new File(".gvt/stage.txt"));
        temp.delete();
    }

}
