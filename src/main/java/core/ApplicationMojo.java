package core;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

@Mojo(name = "start")
public class ApplicationMojo extends AbstractMojo {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationMojo.class);
    @Parameter
    private Properties properties;
    @Override
    public void execute() throws MojoExecutionException {
        try {
            System.setProperties(properties);
            Application.main(new String[0]);
        } catch (IOException e) {
            throw new MojoExecutionException("Error while executing ADCL application", e);
        }
    }
}