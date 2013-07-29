package cc.dividebyzero.jenkins.aav.manifest;

import hudson.FilePath;

/**
 * Created with IntelliJ IDEA.
 * User: zero
 * Date: 7/29/13
 * Time: 8:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidManifestProcessor {


    private final FilePath manifestPath;

    public AndroidManifestProcessor(final FilePath manifestPath){
        this.manifestPath=manifestPath;
    }



    public boolean updateVersionNumber(final long versionNumber){

        return false;
    }


    public boolean updateVersionName(final String versionName){
        return false;
    }
}
