package com.yupi.springbootinit.service.impl;
import java.util.Date;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.manager.RedissonLimiterManager;
import com.yupi.springbootinit.model.dto.chart.*;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.mapper.ChartMapper;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.BiResponse;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.ExcelUtils;
import com.yupi.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Han
 * @description 针对表【chart(用户)】的数据库操作Service实现
 * @createDate 2023-10-02 17:58:41
 */
@Service
@Slf4j
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
        implements ChartService {
    @Resource
    private UserService userService;
    @Resource
    private AiManager aiManager;
    @Resource
    private RedissonLimiterManager redissonLimiterManager;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;


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
        String name = chartQueryRequest.getName();
        Long userId = chartQueryRequest.getUserId();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        // 设置查询条件
        q.eq(id != null && id > 0, "id", id);
        q.like(StringUtils.isNotBlank(name), "name", name);
        q.eq(StringUtils.isNotBlank(goal), "goal", goal);
        q.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        q.eq(StringUtils.isNotBlank(goal), "goal", goal);
        q.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        q.eq("isDelete", false);
        q.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return q;
    }

    @Override
    public BaseResponse<Long> addChart(ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = this.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    @Override
    public BaseResponse<Boolean> deleteChart(DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = this.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = this.removeById(id);
        return ResultUtils.success(b);
    }

    @Override
    public BaseResponse<Boolean> updateChart(ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = this.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = this.updateById(chart);
        return ResultUtils.success(result);
    }

    @Override
    public BaseResponse<Chart> getChartVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = this.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    @Override
    public BaseResponse<Page<Chart>> listChartVOByPage(ChartQueryRequest chartQueryRequest, HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = this.page(new Page<>(current, size),
                this.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    @Override
    public BaseResponse<Page<Chart>> listMyChartVOByPage(ChartQueryRequest chartQueryRequest, HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // todo 使用redis存储图表信息
        //Page<Chart> chartPage1 = new Page<>(current, size);
        //chartPage1.setRecords(new ArrayList<>());
        //this.page(chartPage1, this.getQueryWrapper(chartQueryRequest))

        Page<Chart> chartPage = this.page(new Page<>(current, size),
                this.getQueryWrapper(chartQueryRequest));
        String s = JSONUtil.toJsonStr(chartPage);

        return ResultUtils.success(chartPage);
    }


    /**
     * 使用Ai生成图表
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @Override
    public BaseResponse<BiResponse> genChartByAiAsyn(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest,
                                                     HttpServletRequest request) {
        // 是否登录
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 获取用户输入的信息
        String name = genChartByAiRequest.getName();
        String chartType = genChartByAiRequest.getChartType();
        String goal = genChartByAiRequest.getGoal();
        StringBuilder userInput = new StringBuilder();
        // 校验并抛出异常
        ThrowUtils.throwIf(StringUtils.isBlank(goal),
                ErrorCode.PARAMS_ERROR,
                "请输入要分析的目标");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100,
                ErrorCode.PARAMS_ERROR,
                "名称过长");
        // 校验上传的数据
        long size = multipartFile.getSize();
        final long XIAN_ZHI_SIZE = 10*1024*1024L;

        ThrowUtils.throwIf(size>XIAN_ZHI_SIZE,ErrorCode.OPERATION_ERROR,
                "文件大小超出限制");
        String filename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(filename);
        List<String> fileSupport = Arrays.asList("xlsx", "xls");

        ThrowUtils.throwIf(!fileSupport.contains(suffix),ErrorCode.OPERATION_ERROR,
                "当前系统暂不支持xlsx、xls以外类型的数据分析");
        // 用户限流
        redissonLimiterManager.doRateLimit("genChartByAi_"+loginUser.getId());
        // 原始数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        //分析需求：
        userInput.append("分析需求:").append("\n").append(goal).append("\n");
        if (StringUtils.isNotBlank(chartType)) {
            userInput.append("请使用" + chartType).append("\n");
        }
        //分析网站用户的增长情况
        //原始数据：
        userInput.append("原始数据:").append("\n").append(csvData).append("\n");
        //日期,用户数
        //1号,10
        //2号,20
        //3号,30
        log.info("用户诉求：{}", userInput);
        // 保存到数据库
        Chart chart = new Chart();
        chart.setUserId(loginUser.getId());
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        boolean save = this.save(chart);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "图表保存失败");
        // BI模型id
        long BI_MODEL_ID = 1659171950288818178L;
        // 异步调用
        CompletableFuture.runAsync(()->{
            // 开始执行,修改数据库中的状态
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("running");
            boolean b = this.updateById(updateChart);
            if (!b) {
                this.handleCharUpdateError(chart.getId(),"更新图表执行中状态失败");
            }
            // 调用AI服务
            String resultByAi = aiManager.doChart(BI_MODEL_ID, userInput.toString());
            String[] split = resultByAi.split("【【【【【");
            if (split.length < 3) {
                this.handleCharUpdateError(chart.getId(),"AI生成失败");
            }
            // 取出Ai生成的结果
            String genChart = split[1].trim();
            String fenXiResult = split[2].trim();
            // 修改AI生成结论后的chart信息
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setStatus("succeed");
            updateChartResult.setGenResult(fenXiResult);
            // 保存修改后的数据到数据库
            boolean updateResult = this.updateById(updateChartResult);
            if (!updateResult) {
                this.handleCharUpdateError(chart.getId(),"更新图标成功状态失败");
                return;

            }
        },threadPoolExecutor);
        BiResponse biResponse = new BiResponse();

        biResponse.setChartId(chart.getId());
        // todo redis优化
        // 保存到redis中，每次保存，更新redis
        // 返回结果
        return ResultUtils.success(biResponse);
    }

    /**
     * 更新状态信息处理
     * @param charId
     * @param execMessage
     */
    private void handleCharUpdateError(long charId,String execMessage){
        Chart updateChartResult = new Chart();
        updateChartResult.setId(charId);
        updateChartResult.setStatus("failed");
        updateChartResult.setExecMessage(execMessage);
        // 保存修改后的数据到数据库
        boolean updateResult = this.updateById(updateChartResult);
        if (!updateResult) {
            log.error("更新图表状态失败_"+charId+","+execMessage);
        }
    }

   @Override
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        // 校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        // 校验文件后缀 aaa.png
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        User loginUser = userService.getLoginUser(request);
        // 限流判断，每个用户一个限流器
        redissonLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
        // BI模型id
        long BI_MODEL_ID = 1659171950288818178L;
        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        String result = aiManager.doChart(BI_MODEL_ID, userInput.toString());
        String[] splits = result.split("【【【【【");
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
        }
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }


    //private void saveChartToDBAndCache(BiResponse biResponse, User loginUser, String status,String execMessage,String name,
    //                                   String chartType, String goal, String csvData, String chartCode, String fenXiResult) {
    //    // 保存数据
    //    Chart chart = new Chart();
    //    chart.setUserId(loginUser.getId());
    //    chart.setGoal(goal);
    //    chart.setName(name);
    //    chart.setChartData(csvData);
    //    chart.setChartType(chartType);
    //    chart.setGenChart(chartCode);
    //    chart.setGenResult(fenXiResult);
    //    chart.setStatus("");
    //    biResponse.setChartId(chart.getId());
    //    boolean save = this.save(chart);
    //    // todo 保存到缓存
    //    ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "图表保存失败");
    //}

    @Override
    public BaseResponse<Boolean> editChart(ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = this.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = this.updateById(chart);
        return ResultUtils.success(result);
    }
}




