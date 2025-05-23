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
        if (!Kernel32.INSTANCE.GetVersionEx(versionInfo)) {
            return "Не удалось получить информацию об ОС через WinAPI.";
        }

        int major = versionInfo.dwMajorVersion.intValue();
        int minor = versionInfo.dwMinorVersion.intValue();
        int build = versionInfo.dwBuildNumber.intValue();

        String osName = SystemMapper.mapWindowsName(versionInfo);
        String architecture = getArchitecture();

        return String.format("""
            Операционная система: %s
            Версия: %d.%d (сборка %d)
            Архитектура: %s
            """, osName, major, minor, build, architecture);
    }

    private String getArchitecture() {
        SYSTEM_INFO sysInfo = new SYSTEM_INFO();
        Kernel32.INSTANCE.GetNativeSystemInfo(sysInfo);
        sysInfo.processorArchitecture.read();
        int archCode = sysInfo.processorArchitecture.pi.wProcessorArchitecture.intValue();
        return SystemMapper.mapArchitecture(archCode);
    }
}
