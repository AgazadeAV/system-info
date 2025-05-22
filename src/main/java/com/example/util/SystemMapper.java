package com.example.util;

import com.sun.jna.platform.win32.WinNT;

public class SystemMapper {

    public static final long BYTES_IN_MB = 1024L * 1024;
    public static final long BYTES_IN_GB = 1024L * 1024 * 1024;

    public static String mapArchitecture(int arch) {
        return switch (arch) {
            case 0 -> "x86 (32-бит)";
            case 5 -> "ARM";
            case 6 -> "Intel Itanium (IA64)";
            case 9 -> "x64 (AMD или Intel)";
            case 12 -> "ARM64";
            case 0xFFFF -> "Неизвестно";
            default -> "Не определено (" + arch + ")";
        };
    }

    public static String mapWindowsName(WinNT.OSVERSIONINFOEX info) {
        int major = info.dwMajorVersion.intValue();
        int minor = info.dwMinorVersion.intValue();
        int build = info.dwBuildNumber.intValue();

        if (major == 10 && build >= 22000) return "Windows 11";
        if (major == 10) return "Windows 10";
        if (major == 6 && minor == 3) return "Windows 8.1";
        if (major == 6 && minor == 2) return "Windows 8";
        if (major == 6 && minor == 1) return "Windows 7";
        if (major == 6 && minor == 0) return "Windows Vista";
        if (major == 5 && minor == 1) return "Windows XP";

        return "Windows (Неизвестная версия)";
    }

    public static String mapMac(byte[] mac, int length) {
        if (mac == null || length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02X", mac[i]));
            if (i < length - 1) sb.append("-");
        }
        return sb.toString();
    }

    public static String mapSize(long bytes) {
        if (bytes >= BYTES_IN_GB) {
            double gb = (double) bytes / BYTES_IN_GB;
            return String.format("%.2f GB", gb);
        } else {
            long mb = bytes / BYTES_IN_MB;
            return String.format("%d MB", mb);
        }
    }

    public static String mapMemoryType(String code) {
        return switch (code) {
            case "1" -> "Другое";
            case "2" -> "DRAM";
            case "3" -> "Synchronous DRAM";
            case "4" -> "Cache DRAM";
            case "5" -> "EDO";
            case "6" -> "EDRAM";
            case "7" -> "VRAM";
            case "8" -> "SRAM";
            case "9" -> "RAM";
            case "10" -> "ROM";
            case "11" -> "Flash";
            case "12" -> "EEPROM";
            case "13" -> "FEPROM";
            case "14" -> "EPROM";
            case "15" -> "CDRAM";
            case "16" -> "3DRAM";
            case "17" -> "SDRAM";
            case "18" -> "SGRAM";
            case "19" -> "RDRAM";
            case "20" -> "DDR";
            case "21" -> "DDR2";
            case "22" -> "DDR2 FB-DIMM";
            case "24" -> "DDR3";
            case "25" -> "FBD2";
            case "26" -> "DDR4";
            case "27" -> "LPDDR";
            case "28" -> "LPDDR2";
            case "29" -> "LPDDR3";
            case "30" -> "LPDDR4";
            case "31" -> "Logical non-volatile device";
            case "32" -> "HBM (High Bandwidth Memory)";
            case "33" -> "HBM2";
            case "34" -> "DDR5";
            case "35" -> "LPDDR5";
            default -> "Неизвестно (" + code + ")";
        };
    }
}
