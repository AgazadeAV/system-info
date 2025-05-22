package com.example;

import com.example.info.CpuInfo;
import com.example.info.DiskInfo;
import com.example.info.RamInfo;
import com.example.info.NetworkInfo;
import com.example.info.OsInfo;
import com.example.info.SystemInfoProvider;

import java.util.List;

public class MainApp {
    public static void main(String[] args) {
        System.out.println("Сбор информации... Окна откроются через пару секунд.");

        List<SystemInfoProvider> providers = List.of(
                new CpuInfo(),
                new RamInfo(),
                new DiskInfo(),
                new OsInfo(),
                new NetworkInfo()
        );

        for (SystemInfoProvider provider : providers) {
            new Thread(provider::show).start();
        }
    }
}
