package com.example.winapi;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

@FieldOrder({
        "Next", "ComboIndex", "AdapterName", "Description", "AddressLength", "Address",
        "Index", "Type", "DhcpEnabled", "CurrentIpAddress", "IpAddressList", "GatewayList",
        "DhcpServer", "HaveWins", "PrimaryWinsServer", "SecondaryWinsServer", "LeaseObtained", "LeaseExpires"
})
public class WinAdapterInfo extends Structure {

    public Pointer Next;
    public int ComboIndex;
    public byte[] AdapterName = new byte[260];
    public byte[] Description = new byte[132];
    public int AddressLength;
    public byte[] Address = new byte[8];
    public int Index;
    public int Type;
    public int DhcpEnabled;
    public Pointer CurrentIpAddress; // not used
    public WinIpAddressString IpAddressList;
    public WinIpAddressString GatewayList;
    public WinIpAddressString DhcpServer;
    public boolean HaveWins;
    public WinIpAddressString PrimaryWinsServer;
    public WinIpAddressString SecondaryWinsServer;
    public int LeaseObtained;
    public int LeaseExpires;

    public WinAdapterInfo() {}

    public WinAdapterInfo(Pointer p) {
        super(p);
        read();
    }

    @FieldOrder({ "Next", "IpAddress", "IpMask", "Context" })
    public static class WinIpAddressString extends Structure {
        public Pointer Next;
        public byte[] IpAddress = new byte[16];
        public byte[] IpMask = new byte[16];
        public int Context;

        public WinIpAddressString() {}

        public WinIpAddressString(Pointer p) {
            super(p);
            read();
        }
    }
}
