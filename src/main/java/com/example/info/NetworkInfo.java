package com.example.info;

import com.example.util.SystemMapper;
import com.example.util.UiUtils;
import com.example.winapi.WinAdapterAddressApi;
import com.example.winapi.WinAdapterAddresses;
import com.example.winapi.WinAdapterInfo;
import com.example.winapi.WinAdapterInfoApi;
import com.example.winapi.WinUnicastAddress;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkInfo implements SystemInfoProvider {

    private static final int GAA_FLAG_INCLUDE_PREFIX = 0x10;
    private static final short AF_INET6 = 23;
    private static final int DIVIDER_LENGTH = 40;

    @Override
    public void show() {
        String formatted = formatOutput();
        UiUtils.showNonBlockingWindow("Сетевые интерфейсы", formatted);
    }

    private String formatOutput() {
        StringBuilder sb = new StringBuilder();
        Map<String, List<String>> ipv6Map = collectIpv6ByMac();

        IntByReference sizeRef = new IntByReference();
        int result = WinAdapterInfoApi.INSTANCE.GetAdaptersInfo(null, sizeRef);
        if (result != 0 && result != 111) {
            return "Ошибка GetAdaptersInfo: " + result;
        }

        Memory buffer = new Memory(sizeRef.getValue());
        result = WinAdapterInfoApi.INSTANCE.GetAdaptersInfo(buffer, sizeRef);
        if (result != 0) {
            return "Ошибка GetAdaptersInfo: " + result;
        }

        Pointer current = buffer;
        while (current != null && Pointer.nativeValue(current) != 0) {
            WinAdapterInfo adapter = new WinAdapterInfo(current);

            String name = new String(adapter.Description).trim().replaceAll("\0", "");
            String mac = SystemMapper.mapMac(adapter.Address, adapter.AddressLength);
            String ip = new String(adapter.IpAddressList.IpAddress).trim().replaceAll("\0", "");
            List<String> ipv6List = ipv6Map.getOrDefault(mac, List.of());

            if ((ip.isEmpty() || ip.equals("0.0.0.0")) && ipv6List.isEmpty()) {
                current = adapter.Next;
                continue;
            }

            sb.append("=".repeat(DIVIDER_LENGTH)).append("\n");
            sb.append("Интерфейс: ").append(name).append("\n");
            sb.append("MAC-адрес: ").append(mac).append("\n");

            if (!ip.isEmpty() && !ip.equals("0.0.0.0")) {
                sb.append("IPv4-адрес: ").append(ip).append("\n");
            }

            if (!ipv6List.isEmpty()) {
                sb.append("IPv6-адреса:\n");
                for (String ipv6 : ipv6List) {
                    sb.append("  - ").append(ipv6).append("\n");
                }
            }

            sb.append("\n");
            current = adapter.Next;
        }

        return sb.toString().trim();
    }

    private Map<String, List<String>> collectIpv6ByMac() {
        Map<String, List<String>> map = new HashMap<>();

        IntByReference sizeRef = new IntByReference(15000);
        Memory buffer = new Memory(sizeRef.getValue());
        int result = WinAdapterAddressApi.INSTANCE.GetAdaptersAddresses(0, GAA_FLAG_INCLUDE_PREFIX, null, buffer, sizeRef);

        if (result != 0) return map;

        WinAdapterAddresses adapter = new WinAdapterAddresses(buffer);

        while (true) {
            String mac = SystemMapper.mapMac(adapter.physicalAddress, adapter.physicalAddressLength);
            List<String> ips = getIpAddresses(adapter);

            if (!ips.isEmpty()) {
                map.put(mac, ips);
            }

            if (adapter.next == null) break;
            adapter = new WinAdapterAddresses(adapter.next);
        }

        return map;
    }

    private List<String> getIpAddresses(WinAdapterAddresses adapter) {
        List<String> result = new ArrayList<>();
        Pointer ptr = adapter.firstUnicastAddress;

        while (ptr != null && Pointer.nativeValue(ptr) != 0) {
            WinUnicastAddress ua;
            try {
                ua = new WinUnicastAddress(ptr);
            } catch (Error e) {
                break;
            }

            Pointer sockaddrPointer = ua.address.lpSockaddr;
            if (sockaddrPointer == null || Pointer.nativeValue(sockaddrPointer) == 0) {
                ptr = ua.next;
                continue;
            }

            short family = sockaddrPointer.getShort(0);
            if (family == AF_INET6) {
                try {
                    byte[] ipv6Bytes = sockaddrPointer.getByteArray(8, 16);
                    String ip = InetAddress.getByAddress(ipv6Bytes).getHostAddress();
                    result.add(ip);
                } catch (Exception ignored) {
                }
            }

            ptr = ua.next;
        }

        return result;
    }
}
