package com.tutoringsys.common.util;

import org.springframework.stereotype.Component;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class BackupUtil {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final String BACKUP_DIR = "backup";
    private static final int MAX_BACKUP_DAYS = 30;

    /**
     * 执行数据库备份
     */
    public void backupDatabase() {
        executorService.execute(() -> {
            try {
                // 创建备份目录
                File backupDir = new File(BACKUP_DIR);
                if (!backupDir.exists()) {
                    backupDir.mkdirs();
                }

                // 生成备份文件名
                String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String backupFile = BACKUP_DIR + "/tutoringsys_db_" + dateStr + ".sql";

                // 执行备份命令
                String command = String.format(
                        "mysqldump -u root -p66776677 --databases tutoringsys_db > %s",
                        backupFile
                );

                Process process = Runtime.getRuntime().exec(command);
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    System.out.println("数据库备份成功: " + backupFile);
                    // 清理过期备份
                    cleanupOldBackups();
                } else {
                    System.out.println("数据库备份失败");
                }
            } catch (Exception e) {
                System.err.println("备份过程中发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * 清理过期备份
     */
    private void cleanupOldBackups() {
        File backupDir = new File(BACKUP_DIR);
        File[] files = backupDir.listFiles();
        if (files != null) {
            LocalDate now = LocalDate.now();
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".sql")) {
                    String fileName = file.getName();
                    try {
                        String dateStr = fileName.substring(13, 21); // 提取日期部分
                        LocalDate backupDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
                        if (now.minusDays(MAX_BACKUP_DAYS).isAfter(backupDate)) {
                            if (file.delete()) {
                                System.out.println("删除过期备份: " + fileName);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("处理备份文件时发生错误: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 双写机制：将数据写入文件
     * @param data 数据
     * @param filename 文件名
     */
    public void writeToFile(Object data, String filename) {
        executorService.execute(() -> {
            try {
                // 创建日志目录
                File logDir = new File("logs");
                if (!logDir.exists()) {
                    logDir.mkdirs();
                }

                // 写入文件
                File file = new File(logDir, filename);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                    writer.write(data.toString());
                    writer.newLine();
                }
            } catch (Exception e) {
                System.err.println("写入文件时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}