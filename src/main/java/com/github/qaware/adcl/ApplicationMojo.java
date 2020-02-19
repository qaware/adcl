package com.github.qaware.adcl;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.Properties;

/**
 * ApplicationMojo is the entry point for maven goal {@code adcl:start}.
 * Goal is to start the application with the arguments given via Maven in the configuration tag.
 */
@Mojo(name = "start")
public class ApplicationMojo extends AbstractMojo {

    @Parameter
    private Properties properties;

    /**
     * Launches the application
     *
     * @throws MojoExecutionException if the application does not exit gracefully
     */
    @Override
    public void execute() throws MojoExecutionException {
        Properties propertiesBackup = new Properties(System.getProperties());
        try {
            if (properties != null) {
                properties.forEach((key, value) -> System.setProperty("adcl." + key.toString(), value.toString()));
            }
            int exitCode = Application.launch();
            if (exitCode != 0) throw new MojoExecutionException("Application terminated with exit code " + exitCode);
        } finally {
            System.setProperties(propertiesBackup);
        }
    }
}