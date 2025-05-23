package com.example.info;

import com.example.util.SystemMapper;
import com.example.util.UiUtils;
import com.example.winapi.Kernel32DiskApi;
import com.sun.jna.WString;
import com.sun.jna.ptr.LongByReference;

public class DiskInfo implements SystemInfoProvider {

    private static final char DRIVE_START = 'C';
    private static final char DRIVE_END = 'Z';

    @Override
    public void show() {
        String formatted = formatOutput();
        UiUtils.showNonBlockingWindow("Информация о дисках", formatted);
    }

    private String formatOutput() {
        StringBuilder sb = new StringBuilder();

        for (char drive = DRIVE_START; drive <= DRIVE_END; drive++) {
            String root = drive + ":\\";

            LongByReference freeForUserBytes = new LongByReference();
            LongByReference totalBytes = new LongByReference();
            LongByReference freeTotalBytes = new LongByReference();

            boolean success = Kernel32DiskApi.INSTANCE.GetDiskFreeSpaceExW(
                    new WString(root), freeForUserBytes, totalBytes, freeTotalBytes
            );

            if (success && totalBytes.getValue() > 0) {
                String total = SystemMapper.mapSize(totalBytes.getValue());
                String free = SystemMapper.mapSize(freeForUserBytes.getValue());
                String used = SystemMapper.mapSize(totalBytes.getValue() - freeForUserBytes.getValue());

                sb.append(String.format("""
                                Диск: %s
                                Общий объём: %s
                                Свободно: %s
                                Используется: %s
                                """,
                        root, total, free, used)
                );
            }
        }

        return sb.toString().trim();
    }
}
