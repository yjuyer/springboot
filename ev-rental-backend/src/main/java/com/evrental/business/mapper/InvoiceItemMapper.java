package com.evrental.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evrental.business.entity.InvoiceItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InvoiceItemMapper extends BaseMapper<InvoiceItem> {
}
