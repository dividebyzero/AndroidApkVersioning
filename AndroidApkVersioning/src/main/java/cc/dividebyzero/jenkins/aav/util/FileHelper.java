package cc.dividebyzero.jenkins.aav.util;

import hudson.FilePath;

import java.io.*;

/**
 * some File stuff
 */
public class FileHelper {


    /**
     *
     * @param path IN filepath object
     * @param versionNumber OUT the parsed version number
     * @return true if parsing successfull, else false
     */
    public static final boolean readVersionNumberFromFile(final FilePath path, long versionNumber){
        //yes, this is using pointers. deal with it. :-P
        String line = null;
        try{

            BufferedReader bis = new BufferedReader(new InputStreamReader(path.read()));
            line = bis.readLine();

            bis.close();

        }catch (IOException ioe){

        }
        if(line != null ){
            versionNumber= Long.valueOf(line);
            return true;
        }
        versionNumber = 0;
        return false;
    }

    public static boolean writeVersionNumberToFile(final long number,final FilePath path, final boolean create){
        if(! checkFile(path)  && ! create){
            return false;
        }

        try{

            path.write(String.valueOf(number),null);


        }catch (IOException ioe){
            return false;
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    public static boolean checkFile(FilePath filePath) {
        if(filePath == null)
            return false;

        try {
            return filePath.exists();
        } catch (IOException e) {
        } catch (InterruptedException e) {
        }
        return false;
    }













}













