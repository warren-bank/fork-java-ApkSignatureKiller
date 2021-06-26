package cc.binmt.signature;

import bin.util.StreamUtil;
import sun.security.pkcs.PKCS7;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.cert.Certificate;
import java.util.Base64;

public class NKillSignatureTool {

    public static void main(String[] args) {
        if (args.length < 1) {
            die();
        }

        try {
            process(args[0]);
        }
        catch(Exception e) {
            die();
        }
    }

    private static void die() {
        die(null);
    }

    private static void die(String msg) {
        die(msg, 0);
    }

    private static void die(String msg, int exitcode) {
        if (msg != null) {
            System.out.print(msg);
        }
        System.exit(exitcode);
    }

    private static void process(String certFilePath) throws Exception {
        File certFile = new File(certFilePath);

        if (!certFile.exists()) {
            die();
        }

        byte[] bSignatures = getApkSignatureData(certFile);
        String sSignatures = Base64.getEncoder().encodeToString(bSignatures);

        die(sSignatures);
    }

    private static void writeInt(byte[] data, int off, int value) {
        data[off++] = (byte) (value & 0xFF);
        data[off++] = (byte) ((value >>> 8) & 0xFF);
        data[off++] = (byte) ((value >>> 16) & 0xFF);
        data[off] = (byte) ((value >>> 24) & 0xFF);
    }

    private static byte[] getApkSignatureData(File certFile) throws Exception {
        PKCS7 pkcs7 = new PKCS7(StreamUtil.readBytes(new FileInputStream(certFile)));
        Certificate[] certs = pkcs7.getCertificates();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.write(certs.length);
        for (int i = 0; i < certs.length; i++) {
            byte[] data = certs[i].getEncoded();
            dos.writeInt(data.length);
            dos.write(data);
        }
        return baos.toByteArray();
    }

}
