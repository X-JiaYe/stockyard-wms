package com.wms.inbound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.inbound.entity.InboundAsnLine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InboundAsnLineMapper extends BaseMapper<InboundAsnLine> {

    /** 行锁读取明细行，保证收货/上架对同一行的并发串行化。 */
    @Select("SELECT id, asn_id, sku_id, expected_qty, received_qty, qualified_qty, putaway_qty, lot_no, " +
            "created_at, updated_at, deleted FROM inbound_asn_line WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    InboundAsnLine selectForUpdateById(@Param("id") Long id);
}
