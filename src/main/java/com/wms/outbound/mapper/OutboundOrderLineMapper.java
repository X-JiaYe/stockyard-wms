package com.wms.outbound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.outbound.entity.OutboundOrderLine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OutboundOrderLineMapper extends BaseMapper<OutboundOrderLine> {

    /** 行锁读取明细行，保证拣货对同一行的并发串行化。 */
    @Select("SELECT id, order_id, sku_id, order_qty, picked_qty, lot_no, created_at, updated_at, deleted " +
            "FROM outbound_order_line WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    OutboundOrderLine selectForUpdateById(@Param("id") Long id);
}
