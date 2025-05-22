package com.example.winapi;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

public interface WinAdapterInfoApi extends Library {

    WinAdapterInfoApi INSTANCE = Native.load("iphlpapi", WinAdapterInfoApi.class, W32APIOptions.DEFAULT_OPTIONS);

    int GetAdaptersInfo(
            Pointer pAdapterInfo,
            IntByReference pOutBufLen
    );
}
