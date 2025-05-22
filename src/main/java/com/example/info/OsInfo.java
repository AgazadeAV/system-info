package com.example.info;

import com.example.util.SystemMapper;
import com.example.util.UiUtils;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase.SYSTEM_INFO;
import com.sun.jna.platform.win32.WinNT.OSVERSIONINFOEX;

public class OsInfo implements SystemInfoProvider {

    @Override
    public void show() {
        String formatted = formatOutput();
        UiUtils.showNonBlockingWindow("Информация об ОС", formatted);
    }

    private String formatOutput() {
        OSVERSIONINFOEX versionInfo = new OSVERSIONINFOEX();
        boolean success = Kernel32.INSTANCE.GetVersionEx(versionInfo);
        if (!success) {
            return "Не удалось получить информацию об ОС через WinAPI.";
        }

        SYSTEM_INFO sysInfo = new SYSTEM_INFO();
        Kernel32.INSTANCE.GetNativeSystemInfo(sysInfo);
        sysInfo.processorArchitecture.read();
        int arch = sysInfo.processorArchitecture.pi.wProcessorArchitecture.intValue();

        String architecture = SystemMapper.mapArchitecture(arch);
        String osName = SystemMapper.mapWindowsName(versionInfo);

        return String.format("""
                        Операционная система: %s
                        Версия: %d.%d (сборка %d)
                        Архитектура: %s
                        """,
                osName,
                versionInfo.dwMajorVersion.intValue(),
                versionInfo.dwMinorVersion.intValue(),
                versionInfo.dwBuildNumber.intValue(),
                architecture
        );
    }
}
