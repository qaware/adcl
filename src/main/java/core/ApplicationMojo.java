package core;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.IOException;

@Mojo(name = "start")
public class ApplicationMojo extends AbstractMojo {
    @Override
    public void execute() throws MojoExecutionException {
        try {
            Application.main(new String[0]);
        } catch (IOException e) {
            throw new MojoExecutionException("Error while executing ADCL maven goal 'start'", e);
        }
    }
}
