package com.wms.inbound.dto;

import com.wms.inbound.entity.InboundAsn;
import com.wms.inbound.entity.InboundAsnLine;
import lombok.Data;

import java.util.List;

/**
 * ASN 详情（单头 + 明细行）。
 */
@Data
public class AsnDetailVO {

    private InboundAsn asn;

    private List<InboundAsnLine> lines;
}
