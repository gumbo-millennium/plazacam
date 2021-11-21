
package nu.gumbo;

import nu.gumbo.Models.WebcamCollection;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.*;

public class ConfigurationParser {
    /**
     * @param filename JSON file to read
     * @return Parsed configuration
     * @throws RuntimeException if the config is invalid or the file is missing
     */
    public static WebcamCollection parse(String filename) throws RuntimeException {
        String fileContents = "";

        File settingsFile = new File(System.getProperty("user.dir") + "/" + filename);

        if (!settingsFile.exists()) {
            try {

                Writer writer = new FileWriter(settingsFile);
                writer.write("[]");
                writer.close();
            } catch (IOException exception) {
                throw new RuntimeException(String.format("Could not create settings file: %s", exception.getLocalizedMessage()));
            }
        }

        System.out.printf("Reading settings file [%s]...\n", settingsFile.getAbsolutePath());

        try {
            BufferedReader in = new BufferedReader(new FileReader(settingsFile.getAbsolutePath()));
            StringBuilder contents = new StringBuilder();

            String lastString;
            while ((lastString = in.readLine()) != null) {
                contents.append(lastString);
            }

            fileContents = contents.toString();
        } catch (IOException readException) {
            throw new RuntimeException(String.format(
                "Failed to read settings file from %s: %s",
                settingsFile.getAbsolutePath(),
                readException.getLocalizedMessage()
            ));
        }

        try {
            return WebcamCollection.fromJson(
                new JSONArray(fileContents)
            );
        } catch (JSONException exception) {
            throw new RuntimeException(String.format("Failed to parse JSON: %s", exception.getLocalizedMessage()));
        }
    }
}
