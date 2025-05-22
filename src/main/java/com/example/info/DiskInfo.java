package com.example.info;

import com.example.util.SystemMapper;
import com.example.util.UiUtils;
import com.example.winapi.Kernel32DiskApi;
import com.sun.jna.WString;
import com.sun.jna.ptr.LongByReference;

public class DiskInfo implements SystemInfoProvider {

    @Override
    public void show() {
        String formatted = formatOutput();
        UiUtils.showNonBlockingWindow("Информация о дисках", formatted);
    }

    private String formatOutput() {
        StringBuilder sb = new StringBuilder();

        for (char drive = 'C'; drive <= 'Z'; drive++) {
            String root = drive + ":\\";

            LongByReference freeUser = new LongByReference();
            LongByReference total = new LongByReference();
            LongByReference freeTotal = new LongByReference();

            boolean success = Kernel32DiskApi.INSTANCE.GetDiskFreeSpaceExW(
                    new WString(root), freeUser, total, freeTotal
            );

            if (success && total.getValue() > 0) {
                String totalStr = SystemMapper.mapSize(total.getValue());
                String freeStr = SystemMapper.mapSize(freeUser.getValue());

                sb.append(String.format("""
                        Диск: %s
                        Общий объём: %s
                        Свободно: %s
                        """, root, totalStr, freeStr)
                );
            }
        }

        return sb.toString().trim();
    }
}
