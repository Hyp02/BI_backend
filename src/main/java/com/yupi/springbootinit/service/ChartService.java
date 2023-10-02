package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.springbootinit.model.dto.chart.ChartQueryRequest;
import com.yupi.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Han
* @description 针对表【chart(用户)】的数据库操作Service
* @createDate 2023-10-02 17:58:41
*/
public interface ChartService extends IService<Chart> {

    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);
}
