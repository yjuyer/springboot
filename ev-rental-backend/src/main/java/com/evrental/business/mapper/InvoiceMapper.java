package com.evrental.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evrental.business.entity.Invoice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InvoiceMapper extends BaseMapper<Invoice> {
}
