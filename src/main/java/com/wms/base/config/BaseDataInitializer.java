package com.wms.base.config;

import com.wms.base.entity.Location;
import com.wms.base.entity.Sku;
import com.wms.base.entity.Warehouse;
import com.wms.base.entity.Zone;
import com.wms.base.service.LocationService;
import com.wms.base.service.SkuService;
import com.wms.base.service.WarehouseService;
import com.wms.base.service.ZoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 启动时初始化基础数据示例（幂等）：贴合计算机电子产品仓库场景。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BaseDataInitializer implements CommandLineRunner {

    private final WarehouseService warehouseService;
    private final ZoneService zoneService;
    private final LocationService locationService;
    private final SkuService skuService;

    @Override
    public void run(String... args) {
        if (warehouseService.count() > 0) {
            return;
        }
        // 1. 仓库
        Warehouse wh = new Warehouse();
        wh.setCode("WH-HD01");
        wh.setName("华东电子仓");
        wh.setStatus(1);
        warehouseService.save(wh);

        // 2. 库区（存储/暂存/退货/质检）
        Zone storage = zone(wh.getId(), "ZN-ST-01", "存储区", 1);
        Zone staging = zone(wh.getId(), "ZN-TS-01", "暂存区", 2);
        Zone returns = zone(wh.getId(), "ZN-RT-01", "退货区", 3);
        Zone qc = zone(wh.getId(), "ZN-QC-01", "质检区", 4);
        zoneService.saveBatch(java.util.List.of(storage, staging, returns, qc));

        // 3. 货位（存储位 / 拣货位 / 质检位 / 暂存位）
        Location l1 = location(wh.getId(), storage.getId(), "ST-A01-01", 2, 1);
        Location l2 = location(wh.getId(), storage.getId(), "ST-A01-02", 2, 1);
        Location l3 = location(wh.getId(), storage.getId(), "ST-A02-01", 2, 1);
        Location p1 = location(wh.getId(), storage.getId(), "PK-01-01", 1, 1);
        Location p2 = location(wh.getId(), storage.getId(), "PK-01-02", 1, 1);
        Location q1 = location(wh.getId(), qc.getId(), "QC-01-01", 3, 1);
        Location t1 = location(wh.getId(), staging.getId(), "TS-01-01", 3, 1);
        Location r1 = location(wh.getId(), returns.getId(), "RT-01-01", 3, 1);
        locationService.saveBatch(java.util.List.of(l1, l2, l3, p1, p2, q1, t1, r1));

        // 4. 物料（计算机电子产品）
        skuService.saveBatch(java.util.List.of(
                sku("CPU-INTEL-14600K", "Intel 酷睿 i5-14600K 处理器", "14核20线程/3.5GHz/LGA1700", "颗", "6933225710011", 0, 1),
                sku("CPU-INTEL-14700K", "Intel 酷睿 i7-14700K 处理器", "20核28线程/3.4GHz/LGA1700", "颗", "6933225710028", 0, 1),
                sku("CPU-AMD-7800X3D", "AMD 锐龙 7 7800X3D 处理器", "8核16线程/4.2GHz/AM5", "颗", "6933225710035", 0, 1),
                sku("RAM-DDR5-16G", "金士顿 Fury DDR5 16GB 5600MHz 内存", "16GB/5600MHz/CL36", "条", "6933225710042", 1, 0),
                sku("RAM-DDR5-32G", "金士顿 Fury DDR5 32GB 6000MHz 内存", "32GB/6000MHz/CL36", "条", "6933225710059", 1, 0),
                sku("SSD-NVME-1TB", "三星 990 PRO NVMe SSD 1TB 固态硬盘", "1TB/PCIe4.0/7450MB/s", "块", "6933225710066", 1, 1),
                sku("SSD-NVME-2TB", "三星 990 PRO NVMe SSD 2TB 固态硬盘", "2TB/PCIe4.0/7450MB/s", "块", "6933225710073", 1, 1),
                sku("HDD-SATA-4TB", "希捷 酷鱼 4TB SATA 机械硬盘", "4TB/5400RPM/SATA3", "块", "6933225710080", 1, 0),
                sku("GPU-RTX4070", "NVIDIA RTX 4070 12GB 显卡", "12GB GDDR6X/PCIe4.0", "张", "6933225710097", 0, 1),
                sku("GPU-RTX4080S", "NVIDIA RTX 4080 Super 16GB 显卡", "16GB GDDR6X/PCIe4.0", "张", "6933225710103", 0, 1),
                sku("MB-B760", "华硕 TUF GAMING B760 主板", "LGA1700/DDR5/ATX", "块", "6933225710110", 0, 0),
                sku("MB-Z790", "微星 MAG Z790 主板", "LGA1700/DDR5/ATX", "块", "6933225710127", 0, 0),
                sku("PSU-750W", "海盗船 RM750x 750W 电源", "750W/金牌全模组/ATX3.0", "个", "6933225710134", 0, 0),
                sku("CASE-ATX", "联力 216 中塔机箱", "ATX/侧透/自带3风扇", "个", "6933225710141", 0, 0),
                sku("MON-27-2K", "戴尔 S2725DS 27英寸 2K 显示器", "27英寸/2560x1440/IPS", "台", "6933225710158", 0, 1)
        ));

        log.info("已初始化基础数据示例：1 仓库 / 4 库区 / 8 货位 / 15 物料");
    }

    private Zone zone(Long warehouseId, String code, String name, int zoneType) {
        Zone z = new Zone();
        z.setWarehouseId(warehouseId);
        z.setCode(code);
        z.setName(name);
        z.setZoneType(zoneType);
        return z;
    }

    private Location location(Long warehouseId, Long zoneId, String code, int locType, int status) {
        Location l = new Location();
        l.setWarehouseId(warehouseId);
        l.setZoneId(zoneId);
        l.setCode(code);
        l.setLocType(locType);
        l.setStatus(status);
        return l;
    }

    private Sku sku(String code, String name, String spec, String unit, String barcode, int trackLot, int trackSerial) {
        Sku s = new Sku();
        s.setCode(code);
        s.setName(name);
        s.setSpec(spec);
        s.setUnit(unit);
        s.setBarcode(barcode);
        s.setTrackLot(trackLot);
        s.setTrackSerial(trackSerial);
        s.setStatus(1);
        return s;
    }
}
