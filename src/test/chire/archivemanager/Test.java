package chire.archivemanager;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Test {
    public static void main(String[] args) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("SHA-256");
//
//        md5.update((Files.readAllBytes(Paths.get("Z:\\奇怪的\\test2.mp4"))));
//        md5.update("ss".getBytes());
//
//        byte[] file1Hash = md5.digest();

        byte[] file1Hash;

        Path path = Paths.get("Z:\\奇怪的\\test2.mp4");
        int bufferSize = 8192; // 8KB buffer
        byte[] buffer = new byte[bufferSize];
        int bytesRead;

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path.toFile()))) {
            while ((bytesRead = bis.read(buffer)) != -1) {
                // 处理已读取的字节块，例如更新哈希函数
                if (bytesRead == buffer.length) {
                    // 如果读取了完整的缓冲区大小，则直接处理
                    md5.update(buffer);
                } else {
                    // 如果是最后一块，可能是不完整的，所以复制到新数组
                    byte[] lastChunk = new byte[bytesRead];
                    System.arraycopy(buffer, 0, lastChunk, 0, bytesRead);
                    md5.update(lastChunk);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        file1Hash = md5.digest();

        //aca2c444c81d9fe1210f67cf43a4823bf4187de16f6de028c74350d7f719a4bc
        //

        StringBuilder sb = new StringBuilder();
        for (byte b : file1Hash) {
            sb.append(String.format("%02x", b));
        }

        System.out.println((sb.toString()));
    }

    public static String shortenHash(String hash) {
        // 使用Base62编码作为字符集
        final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        // 将原始哈希值转换为一个大的整数
        BigInteger bigIntHash = new BigInteger(hash, 16);

        // 缩写长度
        int length = 18;

        StringBuilder shortened = new StringBuilder();

        // 循环生成缩写
        for (int i = 0; i < length; i++) {
            // 取模运算得到当前位的索引
            int index = bigIntHash.mod(BigInteger.valueOf(chars.length())).intValue();
            shortened.append(chars.charAt(index));

            // 更新bigIntHash为除以字符集长度后的商
            bigIntHash = bigIntHash.divide(BigInteger.valueOf(chars.length()));
        }

        return shortened.toString();
    }
}
