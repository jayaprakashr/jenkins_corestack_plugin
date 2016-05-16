package org.jenkinsci.plugins.corestack;
import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;


public class CorestackNotifier extends Notifier {

	private final String jobId;
	private String ProjectId;
	
    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public CorestackNotifier(String jobId, String ProjectId) {
        this.jobId = jobId;
        this.ProjectId = ProjectId;
    }

    public String getProjectId() {
		return ProjectId;
	}

    public String getJobId() {
        return jobId;
    }

    
	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		// TODO Auto-generated method stub
		return BuildStepMonitor.NONE;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {
				
		String url = this.getDescriptor().getURL();
		String username = this.getDescriptor().getUsername();
		String password = this.getDescriptor().getPassword();
		String token = CorestackNotifier.authToken(url, username, password);
	    if (token == null){
	    	return false;
	    }
		
		try {
            Client restClient = Client.create();
            WebResource webResource = null;
            
            HashMap<String, String> requestData = new HashMap<>();
            
            // This also shows how you can consult the global configuration of the builder
            if (build.getResult() == Result.SUCCESS) {
                requestData.put("status", "CREATE_COMPLETED"); 
                requestData.put("status_reason", "Jenkins job triggered successfully");
            }
            else{
                requestData.put("status", "CREATE_FAILED"); 
                requestData.put("status_reason", "Jenkins job triggered failed");
            }
            
            JSONObject jsonData = new JSONObject();
            jsonData.putAll(requestData);
            String strjsonData = jsonData.toString();
            
            String authURL = url + "/" + getProjectId() + "/jobs/" + getJobId();
            webResource = restClient.resource(authURL);
            ClientResponse tokenObj = webResource.type("application/json")
            									 .header("X-Auth-User", username)
            									 .header("X-Auth-Token", token)            									 
            									 .post(ClientResponse.class, strjsonData);
            if(tokenObj.getStatus() != 404){
            	return false;
            }
            
        } catch (Exception e) {
            return false;
        }
        return true;
	}

	@Override
	public boolean prebuild(AbstractBuild<?, ?> arg0, BuildListener arg1) {
		// TODO Auto-generated method stub
		return true;
	}
	 
	
	public static String authToken(String url, String username, String password){
        try {            
            Client restClient = Client.create();
            WebResource webResource = null;
            
            HashMap<String, String> requestData = new HashMap<>();
            requestData.put("username", username); 
            requestData.put("password", password);
            
            JSONObject jsonData = new JSONObject();
            jsonData.putAll(requestData);
            String strjsonData = jsonData.toString();
            
            String authURL = url + "/auth/tokens";
            webResource = restClient.resource(authURL);
            ClientResponse tokenObj = webResource.type("application/json")
            									 .post(ClientResponse.class, strjsonData);
            if(tokenObj.getStatus() != 200){
            	return null;
            }
			@SuppressWarnings("static-access")
			JSONObject jsonOutput = new JSONObject().fromObject(tokenObj.getEntity(String.class));
            String token = jsonOutput.getJSONObject("data").getJSONObject("token").getString("key");
            return token;
            
        } catch (Exception e) {
        	return null;
        }		
	}
	

	@Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link CorestackNotifier}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/corestack/CorestackNotifier/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		/**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private String url;
        private String username;
        private String password;
        
        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }
        
        public String getURL() {
            return url;
        }

    	public String getUsername() {
    		return username;
    	}

    	public String getPassword() {
    		return password;
    	}
    	
       public FormValidation doTestConnection(@QueryParameter("corestack.url") final String url,
                @QueryParameter("corestack.username") final String username,
                @QueryParameter("corestack.password") final String password) {

    	    String token = CorestackNotifier.authToken(url, username, password);
    	    if (token == null){
    	    	return FormValidation.error("Corestack credentials are not valid");
    	    }
    	    return FormValidation.ok("Corestack server connection successfull");
        }
        
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Corestack Notification";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            url = formData.getString("url");
            username = formData.getString("username");
            password = formData.getString("password");
            save();
            return super.configure(req,formData);
        }
        
        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			return new CorestackNotifier(formData.getString("jobId"), formData.getString("projectId"));
        	
        }
        
    }

}


