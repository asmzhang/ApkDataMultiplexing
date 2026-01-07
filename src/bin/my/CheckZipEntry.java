package bin.my;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CheckZipEntry {
    public static void main(String[] args) throws Exception {
//        String apkPath = "test.apk";
//        String entryPath = "assets/base.apk";

//        String apkPath = "app-debug.apk";
//        String entryPath = "assets/origin817.apk";

        String apkPath = "app-debug-2.apk";
        String entryPath = "assets/origin817.apk";

        // 解析命令行参数
        if (args.length >= 1) {
            apkPath = args[0];
        }
        if (args.length >= 2) {
            entryPath = args[1];
        }
        
        try (ZipFile zipFile = new ZipFile(apkPath)) {
            ZipEntry entry = zipFile.getEntry(entryPath);
            if (entry == null) {
                System.out.println("Entry not found: " + entryPath);
                return;
            }
            
            System.out.println("Entry: " + entryPath);
            System.out.println("Size: " + entry.getSize() + " bytes");
            System.out.println("Compressed Size: " + entry.getCompressedSize() + " bytes");
            System.out.println("Method: " + getCompressionMethodName(entry.getMethod()));
            System.out.println("Is STORED: " + (entry.getMethod() == ZipEntry.STORED));
            System.out.println("Is DEFLATED: " + (entry.getMethod() == ZipEntry.DEFLATED));
        }
    }
    
    private static String getCompressionMethodName(int method) {
        switch (method) {
            case ZipEntry.STORED: return "STORED (0)";
            case ZipEntry.DEFLATED: return "DEFLATED (8)";
            default: return "Unknown (" + method + ")";
        }
    }
}