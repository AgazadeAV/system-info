package com.example.info;

import com.example.util.SystemMapper;
import com.example.util.UiUtils;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinReg;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CpuInfo implements SystemInfoProvider {

    private static final String CPU_SPEED_FIELD = "MaxClockSpeed";

    @Override
    public void show() {
        String formatted = formatOutput();
        UiUtils.showNonBlockingWindow("Информация о процессоре", formatted);
    }

    private String formatOutput() {
        WinBase.SYSTEM_INFO info = new WinBase.SYSTEM_INFO();
        Kernel32.INSTANCE.GetSystemInfo(info);

        int archCode = info.processorArchitecture.pi.wProcessorArchitecture.intValue();
        String architecture = SystemMapper.mapArchitecture(archCode);
        String model = getCpuModelFromRegistry();
        int processorCount = info.dwNumberOfProcessors.intValue();
        int pageSize = info.dwPageSize.intValue();

        return String.format("""
                        Модель процессора: %s
                        Архитектура: %s
                        Количество логических ядер: %d
                        Размер страницы памяти: %d байт
                        %s
                        """,
                model, architecture, processorCount, pageSize, fetchCpuSpeedFromWmi()
        );
    }

    private String getCpuModelFromRegistry() {
        try {
            return Advapi32Util.registryGetStringValue(
                    WinReg.HKEY_LOCAL_MACHINE,
                    "HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0",
                    "ProcessorNameString"
            );
        } catch (Exception e) {
            return "Неизвестно";
        }
    }

    private String fetchCpuSpeedFromWmi() {
        String command = "powershell.exe -Command \"Get-WmiObject Win32_Processor | Select-Object " + CPU_SPEED_FIELD + "\"";

        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String clockSpeed = null;
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.contains(CPU_SPEED_FIELD)) continue;
                line = line.trim();
                if (line.matches("\\d+")) {
                    clockSpeed = line;
                    break;
                }
            }

            if (clockSpeed == null) {
                return "Не удалось извлечь частоту процессора.";
            }

            return String.format("Частота: %s МГц", clockSpeed);

        } catch (Exception e) {
            return "Ошибка при получении информации о процессоре через WMI: " + e.getMessage();
        }
    }
}
