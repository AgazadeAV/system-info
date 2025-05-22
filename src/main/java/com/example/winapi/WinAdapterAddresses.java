package com.example.winapi;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.ULONG;

import java.util.Arrays;
import java.util.List;

public class WinAdapterAddresses extends Structure {

    public ULONG length;
    public int ifIndex;
    public Pointer next;
    public Pointer adapterName;
    public Pointer firstUnicastAddress;
    public Pointer firstAnycastAddress;
    public Pointer firstMulticastAddress;
    public Pointer firstDnsServerAddress;
    public Pointer dnsSuffix;
    public Pointer description;
    public Pointer friendlyName;
    public byte[] physicalAddress = new byte[8];
    public int physicalAddressLength;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(
                "length", "ifIndex", "next", "adapterName",
                "firstUnicastAddress", "firstAnycastAddress", "firstMulticastAddress", "firstDnsServerAddress",
                "dnsSuffix", "description", "friendlyName",
                "physicalAddress", "physicalAddressLength"
        );
    }

    public WinAdapterAddresses() {}

    public WinAdapterAddresses(Pointer p) {
        super(p);
        read();
    }

    public String getFriendlyName() {
        return friendlyName == null ? "" : friendlyName.getWideString(0);
    }
}
