package com.wms.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.base.entity.Location;
import com.wms.base.mapper.LocationMapper;
import com.wms.base.service.LocationService;
import org.springframework.stereotype.Service;

@Service
public class LocationServiceImpl extends ServiceImpl<LocationMapper, Location> implements LocationService {
}
