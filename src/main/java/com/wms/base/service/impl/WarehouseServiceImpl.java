package com.wms.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.base.entity.Warehouse;
import com.wms.base.mapper.WarehouseMapper;
import com.wms.base.service.WarehouseService;
import org.springframework.stereotype.Service;

@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {
}
