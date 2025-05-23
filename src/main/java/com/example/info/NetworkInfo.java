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

    private static final int ERROR_BUFFER_OVERFLOW = 111;
    private static final int DEFAULT_IPV6_BUFFER_SIZE = 15_000;
    private static final int SOCKADDR_FAMILY_OFFSET = 0;
    private static final int IPV6_ADDRESS_OFFSET = 8;
    private static final int IPV6_ADDRESS_LENGTH = 16;

    @Override
    public void show() {
        String formatted = formatOutput();
        UiUtils.showNonBlockingWindow("Сетевые интерфейсы", formatted);
    }

    private String formatOutput() {
        Map<String, List<String>> ipv6Map = collectIpv6ByMac();
        List<WinAdapterInfo> adapters = getAdaptersInfo();
        return formatAdapters(adapters, ipv6Map);
    }

    private List<WinAdapterInfo> getAdaptersInfo() {
        List<WinAdapterInfo> adapters = new ArrayList<>();
        IntByReference sizeRef = new IntByReference();
        int result = WinAdapterInfoApi.INSTANCE.GetAdaptersInfo(null, sizeRef);
        if (result != 0 && result != ERROR_BUFFER_OVERFLOW) return adapters;

        Memory buffer = new Memory(sizeRef.getValue());
        result = WinAdapterInfoApi.INSTANCE.GetAdaptersInfo(buffer, sizeRef);
        if (result != 0) return adapters;

        Pointer current = buffer;
        while (current != null && Pointer.nativeValue(current) != 0) {
            WinAdapterInfo adapter = new WinAdapterInfo(current);
            adapters.add(adapter);
            current = adapter.Next;
        }

        return adapters;
    }

    private String formatAdapters(List<WinAdapterInfo> adapters, Map<String, List<String>> ipv6Map) {
        StringBuilder sb = new StringBuilder();
        for (WinAdapterInfo adapter : adapters) {
            String name = SystemMapper.cleanCString(adapter.Description);
            String mac = SystemMapper.mapMac(adapter.Address, adapter.AddressLength);
            String ip = new String(adapter.IpAddressList.IpAddress).trim().replaceAll("\0", "");
            List<String> ipv6List = ipv6Map.getOrDefault(mac, List.of());

            if ((ip.isEmpty() || ip.equals("0.0.0.0")) && ipv6List.isEmpty()) continue;

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
        }

        return sb.toString().trim();
    }

    private Map<String, List<String>> collectIpv6ByMac() {
        Map<String, List<String>> ipv6Map = new HashMap<>();

        IntByReference sizeRef = new IntByReference(DEFAULT_IPV6_BUFFER_SIZE);
        Memory buffer = new Memory(sizeRef.getValue());
        int result = WinAdapterAddressApi.INSTANCE.GetAdaptersAddresses(0, GAA_FLAG_INCLUDE_PREFIX, null, buffer, sizeRef);

        if (result != 0) return ipv6Map;

        WinAdapterAddresses adapter = new WinAdapterAddresses(buffer);

        while (true) {
            String mac = SystemMapper.mapMac(adapter.physicalAddress, adapter.physicalAddressLength);
            List<String> ips = getIpAddresses(adapter);

            if (!ips.isEmpty()) {
                ipv6Map.put(mac, ips);
            }

            if (adapter.next == null) break;
            adapter = new WinAdapterAddresses(adapter.next);
        }

        return ipv6Map;
    }

    private List<String> getIpAddresses(WinAdapterAddresses adapter) {
        List<String> ipv6Addresses = new ArrayList<>();
        Pointer currentPtr = adapter.firstUnicastAddress;

        while (currentPtr != null && Pointer.nativeValue(currentPtr) != 0) {
            WinUnicastAddress winUnicastAddress;
            try {
                winUnicastAddress = new WinUnicastAddress(currentPtr);
            } catch (Error e) {
                break;
            }

            Pointer sockaddrPointer = winUnicastAddress.address.lpSockaddr;
            if (sockaddrPointer == null || Pointer.nativeValue(sockaddrPointer) == 0) {
                currentPtr = winUnicastAddress.next;
                continue;
            }

            short family = sockaddrPointer.getShort(SOCKADDR_FAMILY_OFFSET);
            if (family == AF_INET6) {
                try {
                    byte[] ipv6Bytes = sockaddrPointer.getByteArray(IPV6_ADDRESS_OFFSET, IPV6_ADDRESS_LENGTH);
                    String ip = InetAddress.getByAddress(ipv6Bytes).getHostAddress();
                    ipv6Addresses.add(ip);
                } catch (Exception ignored) {
                }
            }

            currentPtr = winUnicastAddress.next;
        }

        return ipv6Addresses;
    }
}
