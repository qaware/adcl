package core;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.util.Properties;

/**
 * ApplicationMojo is the entry point for maven goal {@code adcl:start}.
 * Goal is to start the Application with the arguments given via Maven in the configuration tag.
 */
@Mojo(name = "start")
public class ApplicationMojo extends AbstractMojo {

    @Parameter
    private Properties properties;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            properties.forEach((key, value) -> System.setProperty("adcl." + key.toString(), value.toString()));
            Application.main(new String[0]);
        } catch (IOException e) {
            throw new MojoExecutionException("Error while executing ADCL application", e);
        }
    }
}