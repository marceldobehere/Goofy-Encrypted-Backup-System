package utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MainConfig {
    public String secretFile;
    public List<String> inputPaths;
    public String outputPath;
    public List<Byte> randomSalt;

    @Override
    public String toString() {
        return "MainConfig{" +
                "secretFile='" + secretFile + '\'' +
                ", inputPath='" + inputPaths + '\'' +
                ", outputPath='" + outputPath + '\'' +
                ", randomSalt=" + randomSalt +
                '}';
    }

    public static MainConfig glob;
    public static String encPassword;
}
