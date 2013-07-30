package cc.dividebyzero.jenkins.aav;
import cc.dividebyzero.jenkins.aav.manifest.AndroidManifestProcessor;
import cc.dividebyzero.jenkins.aav.util.FileHelper;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link AndroidApkVersioning} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class AndroidApkVersioning extends Builder {

    private final String name;
    private final String versionNumberEnvironmentVariable;
    private String versionNumberStr ="0.0.1";
    private String versionName = "unknown";
    private long versionNumber=0;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public AndroidApkVersioning(String name, String versionNumberEnvironmentVariable) {
        this.name = name;
        this.versionNumberEnvironmentVariable = versionNumberEnvironmentVariable;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {

        // This also shows how you can consult the global configuration of the builder

        debugDescribeConfig(listener.getLogger());


        FilePath versionFile = null;
        if (getDescriptor().isReadVersionFromFileEnabled()) {
           versionFile = computeFilePath(build, getDescriptor().getFilePath());

            if (FileHelper.checkFile(versionFile)) {

                if (FileHelper.readVersionNumberFromFile(versionFile, versionNumber) && readVersionNameFromEnv(build)) {
                    listener.getLogger().println("versionNumber>>" + versionNumber + "<< versionName>>" + versionName + "<<");
                } else {
                    listener.getLogger().println(" failed reading versionNumber or versionName");
                    //fail build
                    return false;
                }

            }
        } else {


            if (readVersionNumberFromEnv(build) && readVersionNameFromEnv(build)) {

                versionNumber = Long.valueOf(versionNumberStr);
                listener.getLogger().println("versionNumber>>" + versionNumberStr + "<< versionName>>" + versionName + "<<");
            } else {
                listener.getLogger().println(" failed reading versionNumber or versionName from Env Vars");
                //fail build
                return false;
            }


        }


        AndroidManifestProcessor amp = new AndroidManifestProcessor(computeFilePath(build, getDescriptor().getManifestPath()));

        if(versionNumber!=0){
           if(!amp.updateVersionNumber(versionNumber)){
               listener.getLogger().println(" failed updating versionNumber");
               return false;
           }
        }

        if(versionName!=null && versionName.length()>0){
            if(! amp.updateVersionName(versionName)){
                listener.getLogger().println(" failed updating versionNumber");
                return false;
            }
        }

        if(getDescriptor().isAutoIncrementEnabled()){
            versionNumber++;
            if( ! FileHelper.writeVersionNumberToFile(versionNumber,versionFile,getDescriptor().isReadVersionFromFileEnabled())){
                listener.getLogger().println(" failed incrementing versionNumber");
                return false;
            }
        }
        listener.getLogger().println("android apk versioning SUCCESS");
        return true;
    }

    private void debugDescribeConfig(java.io.PrintStream logger) {

        logger.println("*********AndroidApkVersioning - CONFIG ******");
        logger.println("env var to read build number from >>"+getDescriptor().getVersionNumberEnvironmentVariable()+"<<");
        logger.println("env var to read build name from >>"+getDescriptor().getVersionNameEnvironmentVariable()+"<<");
        logger.println("read number from file ?>>"+getDescriptor().isReadVersionFromFileEnabled()+"<<");
        logger.println("auto-inc number from file ?>>"+getDescriptor().isAutoIncrementEnabled()+"<<");
        logger.println("file path>>"+getDescriptor().getFilePath()+"<<");
        logger.println("AndroidManifest file path>>"+getDescriptor().getManifestPath()+"<<");
        logger.println("********* CONFIG END ******");



    }

    private FilePath computeFilePath(AbstractBuild build, final String filePath){

        try{
            return  build.getWorkspace().withSuffix(File.separator+filePath);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private boolean readVersionNumberFromEnv(final AbstractBuild build){
        try{
            final String envarName = getDescriptor().getVersionNumberEnvironmentVariable();
            if(envarName!=null){
                versionNumberStr = build.getEnvironment().get(envarName);
                return true;
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return false;
    }

    private boolean readVersionNameFromEnv(final AbstractBuild build){
        try{
            final String envarName = getDescriptor().getVersionNameEnvironmentVariable();
            if(envarName!=null){
                versionName = build.getEnvironment().get(envarName);
                return true;
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return false;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link AndroidApkVersioning}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private boolean readVersionFromFileEnabled;

        private boolean autoIncrementEnabled;

        private String filePath;

        private String versionNumberEnvironmentVariable;

        private String versionNameEnvironmentVariable;



        private String manifestPath = "AndroidManifest.xml";

        /**
         * Performs on-the-fly validation of the form field 'manifestPath'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckManifestPath(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Path to AndroidManifest.xml can not be empty");
            if (value.length() < 14)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Android Apk Versioning";
        }

        @java.lang.Override
        public Builder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            configure(req,formData);
            return super.newInstance(req, formData);
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {


            // To persist global configuration information,
            // set that to properties and call save().


            readVersionFromFileEnabled = formData.getBoolean("readVersionFromFileEnabled");
            filePath = formData.getString("filePath");

            autoIncrementEnabled = formData.getBoolean("autoIncrementEnabled");

            versionNumberEnvironmentVariable = formData.getString("versionNumberEnvironmentVariable");
            versionNameEnvironmentVariable = formData.getString("versionNameEnvironmentVariable");

            manifestPath = formData.getString("manifestPath");

            save();
            return super.configure(req,formData);
        }


        public boolean isReadVersionFromFileEnabled() {
            return readVersionFromFileEnabled;
        }

        public String getFilePath() {
            return filePath;
        }

        public boolean isAutoIncrementEnabled() {
            return autoIncrementEnabled;
        }

        public String getVersionNumberEnvironmentVariable() {
            return versionNumberEnvironmentVariable;
        }

        public String getVersionNameEnvironmentVariable() {
            return versionNameEnvironmentVariable;
        }

        public String getManifestPath() {
            return manifestPath;
        }
    }
}

