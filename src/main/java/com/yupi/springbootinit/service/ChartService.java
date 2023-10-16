package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.model.dto.chart.*;
import com.yupi.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.vo.BiResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Han
 * @description 针对表【chart(用户)】的数据库操作Service
 * @createDate 2023-10-02 17:58:41
 */
public interface ChartService extends IService<Chart> {

    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);

    BaseResponse<Long> addChart(ChartAddRequest chartAddRequest, HttpServletRequest request);

    BaseResponse<Boolean> deleteChart(DeleteRequest deleteRequest, HttpServletRequest request);

    BaseResponse<Boolean> updateChart(ChartUpdateRequest chartUpdateRequest);

    BaseResponse<Chart> getChartVOById(long id, HttpServletRequest request);

    BaseResponse<Page<Chart>> listChartVOByPage(ChartQueryRequest chartQueryRequest, HttpServletRequest request);

    BaseResponse<Page<Chart>> listMyChartVOByPage(ChartQueryRequest chartQueryRequest, HttpServletRequest request);

    BaseResponse<BiResponse> genChartByAiAsyn(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    BaseResponse<BiResponse> genChartByAi(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    BaseResponse<BiResponse> genChartByAiMq(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    BaseResponse<Boolean> editChart(ChartEditRequest chartEditRequest, HttpServletRequest request);

    void handleCharUpdateError(long charId, String execMessage);

}
