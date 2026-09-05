package com.wms.inbound.service;

import com.wms.common.result.PageResult;
import com.wms.inbound.dto.AsnCreateRequest;
import com.wms.inbound.dto.AsnDetailVO;
import com.wms.inbound.dto.PutawayRequest;
import com.wms.inbound.dto.ReceiveRequest;
import com.wms.inbound.entity.InboundAsn;
import com.wms.inbound.entity.InboundAsnLine;

/**
 * 入库 Service：ASN → 收货 → 上架。
 */
public interface InboundService {

    InboundAsn createAsn(AsnCreateRequest req);

    PageResult<InboundAsn> queryAsns(Long warehouseId, Integer status, long pageNum, long pageSize);

    AsnDetailVO getAsn(Long id);

    InboundAsnLine receive(ReceiveRequest req);

    InboundAsnLine putaway(PutawayRequest req);
}
