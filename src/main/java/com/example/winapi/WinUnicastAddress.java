package com.example.winapi;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.List;

public class WinUnicastAddress extends Structure {

    public Pointer next;
    public int length;
    public int flags;
    public SOCKET_ADDRESS address;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("next", "length", "flags", "address");
    }

    public WinUnicastAddress() {}

    public WinUnicastAddress(Pointer p) {
        super(p);
        read();
    }

    public static class SOCKET_ADDRESS extends Structure {
        public Pointer lpSockaddr;
        public int iSockaddrLength;

        @Override
        protected List<String> getFieldOrder() {
            return List.of("lpSockaddr", "iSockaddrLength");
        }
    }
}
