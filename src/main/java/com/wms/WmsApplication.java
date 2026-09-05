package com.wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * WMS 启动入口。
 *
 * <p>架构：单模块 + 包级分层（模块化单体），业务域按包划分：
 * {@code com.wms.base}（基础数据）、{@code com.wms.stock}（库存中枢）、
 * {@code com.wms.inbound}（入库）、{@code com.wms.outbound}（出库）、
 * {@code com.wms.system}（系统管理）、{@code com.wms.report}（报表）、
 * {@code com.wms.common}（公共）。边界由包名约定，后续可用 ArchUnit 兜底。
 */
@SpringBootApplication
public class WmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmsApplication.class, args);
    }
}
