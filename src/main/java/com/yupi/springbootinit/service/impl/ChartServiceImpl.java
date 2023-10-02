package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.model.dto.chart.ChartQueryRequest;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.mapper.ChartMapper;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author Han
* @description 针对表【chart(用户)】的数据库操作Service实现
* @createDate 2023-10-02 17:58:41
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {


    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {

        QueryWrapper<Chart> q = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return q;
        }
        Long id = chartQueryRequest.getId();
        Long userId = chartQueryRequest.getUserId();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        // 设置查询条件
        q.eq(id != null && id > 0   , "id",id);
        q.eq(StringUtils.isNotBlank(goal) , "goal",goal);
        q.eq(StringUtils.isNotBlank(chartType) , "chartType",chartType);
        q.eq(StringUtils.isNotBlank(goal) , "goal",goal);
        q.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        q.eq("isDelete", false);
        q.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return q;
    }
}




