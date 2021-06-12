package cc.binmt.signature;

import bin.util.StreamUtil;
import bin.zip.ZipEntry;
import bin.zip.ZipFile;
import sun.security.pkcs.PKCS7;

import java.io.*;
import java.security.cert.Certificate;
import java.util.*;

public class NKillSignatureTool {

    public static void main(String[] args) throws Exception {
        process();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void process() throws Exception {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config.txt")) {
            properties.load(fis);
        }

        String sSignApk = properties.getProperty("apk.signed");
        String sOutTxt  = properties.getProperty("txt.out");

        File       fSignApk = null;
        FileWriter fOutTxt  = null;

        if (sSignApk == null) {
          System.out.println("apk.signed is undefined");
          System.exit(1);
        }

        fSignApk = new File(sSignApk);
        if (!fSignApk.exists()) {
          System.out.println("apk.signed file path does not exist");
          System.exit(1);
        }

        byte[] bSignatures = getApkSignatureData(fSignApk);
        String sSignatures = Base64.getEncoder().encodeToString(bSignatures);
        String result      = "const-string/jumbo v6, \"" + sSignatures + "\"";

        if (sOutTxt != null) {
          fOutTxt = new FileWriter(sOutTxt);
          fOutTxt.write(result);
          fOutTxt.close();
        }
        else {
          System.out.println(result);
        }
        System.exit(0);
    }

    private static void writeInt(byte[] data, int off, int value) {
        data[off++] = (byte) (value & 0xFF);
        data[off++] = (byte) ((value >>> 8) & 0xFF);
        data[off++] = (byte) ((value >>> 16) & 0xFF);
        data[off] = (byte) ((value >>> 24) & 0xFF);
    }

    private static byte[] getApkSignatureData(File apkFile) throws Exception {
        ZipFile zipFile = new ZipFile(apkFile);
        Enumeration<ZipEntry> entries = zipFile.getEntries();
        while (entries.hasMoreElements()) {
            ZipEntry ze = entries.nextElement();
            String name = ze.getName().toUpperCase();
            if (name.startsWith("META-INF/") && (name.endsWith(".RSA") || name.endsWith(".DSA"))) {
                PKCS7 pkcs7 = new PKCS7(StreamUtil.readBytes(zipFile.getInputStream(ze)));
                Certificate[] certs = pkcs7.getCertificates();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
                dos.write(certs.length);
                for (int i = 0; i < certs.length; i++) {
                    byte[] data = certs[i].getEncoded();
                    System.out.printf("  --SignatureHash[%d]: %08x\n", i, Arrays.hashCode(data));
                    dos.writeInt(data.length);
                    dos.write(data);
                }
                return baos.toByteArray();
            }
        }
        throw new Exception("META-INF/XXX.RSA (DSA) file not found.");
    }

}
