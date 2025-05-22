package com.example.winapi;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.win32.W32APIOptions;

public interface Kernel32DiskApi extends Library {

    Kernel32DiskApi INSTANCE = Native.load("kernel32", Kernel32DiskApi.class, W32APIOptions.UNICODE_OPTIONS);

    boolean GetDiskFreeSpaceExW(
            WString lpDirectoryName,
            LongByReference lpFreeBytesAvailable,
            LongByReference lpTotalNumberOfBytes,
            LongByReference lpTotalNumberOfFreeBytes
    );
}
