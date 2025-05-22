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

    @Override
    public void show() {
        String formatted = formatOutput();
        UiUtils.showNonBlockingWindow("Информация о процессоре", formatted);
    }

    private String formatOutput() {
        WinBase.SYSTEM_INFO info = new WinBase.SYSTEM_INFO();
        Kernel32.INSTANCE.GetSystemInfo(info);

        int archCode = info.processorArchitecture.pi.wProcessorArchitecture.intValue();
        String arch = SystemMapper.mapArchitecture(archCode);
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
                model, arch, processorCount, pageSize, formatOutputWmi()
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

    private String formatOutputWmi() {
        String command = "powershell.exe -Command \"Get-WmiObject Win32_Processor | Select-Object MaxClockSpeed\"";

        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String clock = null;
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.contains("MaxClockSpeed")) continue;
                line = line.trim();
                if (line.matches("\\d+")) {
                    clock = line;
                    break;
                }
            }

            if (clock == null) {
                return "Не удалось извлечь частоту процессора.";
            }

            return String.format("Частота: %s МГц", clock);

        } catch (Exception e) {
            return "Ошибка при получении информации о процессоре через WMI: " + e.getMessage();
        }
    }
}
