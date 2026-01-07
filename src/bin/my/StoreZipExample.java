package bin.my;


import java.io.*;
import java.util.Collections;
import java.util.zip.*;

/**
 * 演示如何使用STORED方式打包文件（不进行压缩）
 * 作者：AI助手
 * 创建日期：2026-01-07
 */
public class StoreZipExample {

    /**
     * 使用STORED方式将文件添加到ZIP归档中
     * @param srcFile 源文件路径
     * @param zipFile 目标ZIP文件路径
     * @param entryName ZIP条目名称
     * @throws IOException 如果发生IO错误
     */
    public static void addFileAsStored(String srcFile, String zipFile, String entryName) throws IOException {
        File sourceFile = new File(srcFile);

        // 计算文件的CRC32和大小
        CRC32 crc = new CRC32();
        try (InputStream is = new FileInputStream(sourceFile)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = is.read(buffer)) != -1) {
                crc.update(buffer, 0, read);
            }
        }

        long fileSize = sourceFile.length();
        long crcValue = crc.getValue();

        // 创建ZIP输出流
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            // 设置压缩级别为STORED（不压缩）
            ZipEntry entry = new ZipEntry(entryName);
            entry.setMethod(ZipEntry.STORED);
            entry.setSize(fileSize);
            entry.setCompressedSize(fileSize);
            entry.setCrc(crcValue);

            zos.putNextEntry(entry);

            // 写入文件内容
            try (InputStream is = new FileInputStream(sourceFile)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    zos.write(buffer, 0, read);
                }
            }

            zos.closeEntry();
            System.out.println("文件已以STORED方式添加到ZIP: " + entryName);
        }
    }

    /**
     * 修改现有APK中的文件为STORED方式
     * 注意：这是一个简化的示例，实际修改APK需要考虑签名等问题
     * @param apkPath APK文件路径
     * @param entryPath 要修改的条目路径
     * @param outputApkPath 输出APK文件路径
     * @throws IOException 如果发生IO错误
     */
    public static void convertEntryToStored(String apkPath, String entryPath, String outputApkPath) throws IOException {
        // 创建临时目录
        File tempDir = new File("temp_apk");
        tempDir.mkdir();

        try {
            // 1. 解压APK到临时目录
            System.out.println("正在解压APK...");
            try (ZipFile zipFile = new ZipFile(apkPath)) {
                // 解压要修改的文件
                ZipEntry entry = zipFile.getEntry(entryPath);
                if (entry == null) {
                    throw new IOException("未找到条目: " + entryPath);
                }

                File extractedFile = new File(tempDir, entryPath);
                extractedFile.getParentFile().mkdirs();

                try (InputStream is = zipFile.getInputStream(entry);
                     OutputStream os = new FileOutputStream(extractedFile)) {
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                    }
                }

                // 2. 创建新的APK文件
                System.out.println("正在创建新APK...");
                try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputApkPath))) {
                    // 3. 以STORED方式添加要修改的文件
                    // 计算文件的CRC32和大小
                    CRC32 crc = new CRC32();
                    File sourceFile = new File(extractedFile.getPath());
                    try (InputStream is = new FileInputStream(sourceFile)) {
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = is.read(buffer)) != -1) {
                            crc.update(buffer, 0, read);
                        }
                    }
                    long fileSize = sourceFile.length();
                    long crcValue = crc.getValue();

                    // 设置ZIP条目为STORED方式
                    ZipEntry newEntry = new ZipEntry(entryPath);
                    newEntry.setMethod(ZipEntry.STORED);
                    newEntry.setSize(fileSize);
                    newEntry.setCompressedSize(fileSize);
                    newEntry.setCrc(crcValue);

                    zos.putNextEntry(newEntry);

                    // 写入文件内容
                    try (InputStream is = new FileInputStream(sourceFile)) {
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = is.read(buffer)) != -1) {
                            zos.write(buffer, 0, read);
                        }
                    }

                    zos.closeEntry();
                    System.out.println("文件已以STORED方式添加到ZIP: " + entryPath);

                    // 4. 以原方式添加其他文件
                    for (ZipEntry otherEntry : Collections.list(zipFile.entries())) {
                        if (!otherEntry.getName().equals(entryPath)) {
                            // 创建新的ZipEntry对象，避免压缩大小不匹配的问题
                            ZipEntry newOtherEntry = new ZipEntry(otherEntry.getName());
                            // 只复制必要的属性，不复制压缩大小
                            newOtherEntry.setMethod(otherEntry.getMethod());
                            newOtherEntry.setSize(otherEntry.getSize());
                            newOtherEntry.setCrc(otherEntry.getCrc());
                            newOtherEntry.setTime(otherEntry.getTime());
                            newOtherEntry.setComment(otherEntry.getComment());
                            newOtherEntry.setExtra(otherEntry.getExtra());
                            
                            zos.putNextEntry(newOtherEntry);
                            try (InputStream is = zipFile.getInputStream(otherEntry)) {
                                byte[] buffer = new byte[4096];
                                int read;
                                while ((read = is.read(buffer)) != -1) {
                                    zos.write(buffer, 0, read);
                                }
                            }
                            zos.closeEntry();
                        }
                    }
                }

                System.out.println("APK修改完成！");
                System.out.println("原始APK: " + apkPath);
                System.out.println("修改后APK: " + outputApkPath);
                System.out.println("已将条目 " + entryPath + " 改为STORED方式");
            }
        } finally {
            // 清理临时目录
            deleteDirectory(tempDir);
        }
    }

    /**
     * 递归删除目录
     * @param dir 要删除的目录
     */
    private static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("用法: java StoreZipExample <APK文件路径> <要修改的条目路径> <输出APK文件路径>");
            System.out.println("示例: java StoreZipExample app-debug-2.apk assets/SignatureKiller/origin.apk app-debug-2-stored.apk");
            return;
        }

        String apkPath = args[0];
        String entryPath = args[1];
        String outputApkPath = args[2];

        // 执行解包重打包操作，将指定条目转换为STORED方式
        convertEntryToStored(apkPath, entryPath, outputApkPath);
    }
}