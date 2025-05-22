package com.example.winapi;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

public interface WinAdapterAddressApi extends StdCallLibrary {

    WinAdapterAddressApi INSTANCE = Native.load("iphlpapi", WinAdapterAddressApi.class);

    int GetAdaptersAddresses(
            int family,
            int flags,
            Pointer reserved,
            Pointer adapterAddresses,
            IntByReference sizePointer
    );
}
