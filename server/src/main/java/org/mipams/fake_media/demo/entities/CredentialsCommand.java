package org.mipams.fake_media.demo.entities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.mipams.jumbf.core.util.CoreUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class CredentialsCommand {

    public static final String CREDENTIALS_SCRIPT = "generateCredentials.sh";

    private @Getter String directory;
    private @Getter String username;
    private @Getter String organization;

    public String createCredentials() {

        final String script = CoreUtils.getFullPath(directory, CREDENTIALS_SCRIPT);

        String[] cmd = { "sh", script, directory, username, organization };

        try {
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            return result.toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
