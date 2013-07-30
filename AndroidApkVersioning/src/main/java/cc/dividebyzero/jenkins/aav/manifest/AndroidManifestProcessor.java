package cc.dividebyzero.jenkins.aav.manifest;

import hudson.FilePath;
import org.jdom2.Document;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.IOException;


/**
 *
 */
public class AndroidManifestProcessor {


    private final Namespace nsAndroid;

    public class ProcessingException extends RuntimeException{
        public ProcessingException(Throwable cause) {
            super(cause);
        }
    }

    private final FilePath manifestPath;
    private Document document;

    public AndroidManifestProcessor(final FilePath manifestPath){
        this.manifestPath=manifestPath;
        nsAndroid = Namespace.getNamespace("android","http://schemas.android.com/apk/res/android");
        readDocument();
    }


    public void readDocument(){
        SAXBuilder saxBuilder = new SAXBuilder();
        try {
            document = saxBuilder.build(manifestPath.read());

        } catch (Exception e) {
          throw new ProcessingException(e);
        }

    }


    public void writeDocument(){
        try {
            XMLOutputter lecrivain = new XMLOutputter(Format.getPrettyFormat());
            lecrivain.output(document,manifestPath.write());
        } catch (Exception e) {
            throw new ProcessingException(e);
        }
    }

    public boolean updateVersionNumber(final long versionNumber){
        document.getRootElement().getAttribute("versionCode",nsAndroid)
                .setValue(String.valueOf(versionNumber));
        writeDocument();
        return true;
    }


    public boolean updateVersionName(final String versionName){
        document.getRootElement().getAttribute("versionName",nsAndroid)
                .setValue(String.valueOf(versionName));
        writeDocument();
        return true;
    }



}
