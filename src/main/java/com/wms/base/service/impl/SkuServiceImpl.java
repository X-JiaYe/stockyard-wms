package com.wms.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.base.entity.Sku;
import com.wms.base.mapper.SkuMapper;
import com.wms.base.service.SkuService;
import org.springframework.stereotype.Service;

@Service
public class SkuServiceImpl extends ServiceImpl<SkuMapper, Sku> implements SkuService {
}
