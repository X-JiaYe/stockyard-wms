package com.wms.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.base.entity.Zone;
import com.wms.base.mapper.ZoneMapper;
import com.wms.base.service.ZoneService;
import org.springframework.stereotype.Service;

@Service
public class ZoneServiceImpl extends ServiceImpl<ZoneMapper, Zone> implements ZoneService {
}
