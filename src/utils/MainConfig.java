package utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import utils.CryptoStuff.ArrConv;
import utils.CryptoStuff.Pbkdf2Stuff;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import static utils.CryptoStuff.HashStuff.HashInputToStr;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MainConfig {
    public String secretFile;
    public List<String> inputPaths;
    public String outputPath;
    public List<Byte> randomSalt;
    public boolean syncDeletions;
    public boolean logs;
    public int neededGb;

    @Override
    public String toString() {
        return "MainConfig{" +
                "secretFile='" + secretFile + '\'' +
                ", inputPaths=" + inputPaths +
                ", outputPath='" + outputPath + '\'' +
                ", randomSalt=" + randomSalt +
                ", syncDeletions=" + syncDeletions +
                ", logs=" + logs +
                ", neededGb=" + neededGb +
                '}';
    }

    public static MainConfig glob;
    public static String encPassword;

    public static byte[] GetRandomBytesPwAndExtra(String extra) {
        try {
            return Pbkdf2Stuff.getRandomFromPassword(HashInputToStr(encPassword) + HashInputToStr(extra), ArrConv.ByteCListToBytePArr(glob.randomSalt));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
