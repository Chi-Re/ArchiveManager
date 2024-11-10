package chire.archivemanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Test {
    public static void main(String[] args) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        byte[] file1Hash = md5.digest(Files.readAllBytes(Paths.get("Z:\\奇怪的\\test2.mp4")));
        System.out.println(Arrays.toString(file1Hash));
    }
}
