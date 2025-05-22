package com.example.info;

import com.example.util.SystemMapper;
import com.example.util.UiUtils;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RamInfo implements SystemInfoProvider {

    @Override
    public void show() {
        String formatted = formatOutput();
        UiUtils.showNonBlockingWindow("Информация об оперативной памяти", formatted);
    }

    private String formatOutput() {
        WinBase.MEMORYSTATUSEX memStatus = new WinBase.MEMORYSTATUSEX();
        Kernel32.INSTANCE.GlobalMemoryStatusEx(memStatus);

        long totalBytes = memStatus.ullTotalPhys.longValue();
        long availableBytes = memStatus.ullAvailPhys.longValue();
        long usedBytes = totalBytes - availableBytes;

        return String.format("""
                        Общий объём ОЗУ: %s
                        Используется: %s
                        Свободно: %s
                        %s
                        """,
                SystemMapper.mapSize(totalBytes),
                SystemMapper.mapSize(usedBytes),
                SystemMapper.mapSize(availableBytes),
                formatOutputWmi()
        );
    }

    private String formatOutputWmi() {
        String command = "powershell.exe -Command \"Get-WmiObject Win32_PhysicalMemory | Select-Object Manufacturer, SMBIOSMemoryType, Speed\"";

        class RamModule {
            final String manufacturer;
            final String type;
            final String speed;

            public RamModule(String manufacturer, String type, String speed) {
                this.manufacturer = manufacturer;
                this.type = type;
                this.speed = speed;
            }

            String groupKey() {
                return manufacturer + "|" + type + "|" + speed;
            }
        }

        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            Map<String, List<RamModule>> grouped = new LinkedHashMap<>();
            String line;
            boolean dataStarted = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.matches("[-\\s]+") || line.contains("Manufacturer")) {
                    dataStarted = true;
                    continue;
                }

                if (dataStarted) {
                    line = line.replaceAll("\\s{2,}", " ");
                    String[] parts = line.split(" ");
                    if (parts.length >= 3) {
                        String manufacturer = parts[0];
                        String memoryTypeCode = parts[1];
                        String speed = parts[2];
                        try {
                            String type = SystemMapper.mapMemoryType(memoryTypeCode);
                            RamModule module = new RamModule(manufacturer, type, speed);

                            grouped.computeIfAbsent(module.groupKey(), k -> new ArrayList<>()).add(module);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }

            if (grouped.isEmpty()) {
                return "Не удалось извлечь информацию об ОЗУ через WMI.";
            }

            StringBuilder sb = new StringBuilder();
            for (List<RamModule> group : grouped.values()) {
                RamModule sample = group.get(0);
                int count = group.size();

                sb.append(String.format("""
                    Модулей: %d
                    Производитель: %s
                    Тип: %s
                    Скорость: %s МГц
                    """, count, sample.manufacturer, sample.type, sample.speed)).append("\n");
            }

            return sb.toString().trim();

        } catch (Exception e) {
            return "Ошибка при получении информации об ОЗУ через WMI: " + e.getMessage();
        }
    }
}
