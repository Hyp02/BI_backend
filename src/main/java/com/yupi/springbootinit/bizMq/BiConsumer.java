package com.yupi.springbootinit.bizMq;
import java.util.Date;

import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.utils.ExcelUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Han
 * @data 2023/10/16
 * @apiNode
 */
@Component
@Slf4j
public class BiConsumer {
    @Resource
    private ChartService chartService;
    @Resource
    private AiManager aiManager;
    @SneakyThrows
    // 监听code_queue这个队列中的消息，
    @RabbitListener(queues = MqConstant.BI_QUEUE_NAME,ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG)  long deliverTag){
        if (StringUtils.isBlank(message)) {
            // 拒绝服务
            channel.basicNack(deliverTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"参数为空");
        }
        Chart chart = chartService.getById(Long.parseLong(message));
        if (chart == null) {
            channel.basicNack(deliverTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"图标为空");
        }
        // 开始执行,修改数据库中的状态
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus("running");
        boolean b = chartService.updateById(updateChart);
        if (!b) {
            channel.basicNack(deliverTag,false,false);

            chartService.handleCharUpdateError(chart.getId(),"更新图表执行中状态失败");
        }
        // 调用AI服务
        String resultByAi = aiManager.doChart(CommonConstant.BI_MODEL_ID, userInput(chart));
        log.info("AI生成内容：{}",resultByAi);
        String[] split = resultByAi.split("【【【【【");
        if (split.length < 3) {
            channel.basicNack(deliverTag,false,false);
            chartService.handleCharUpdateError(chart.getId(),"AI生成失败");
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
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {
            channel.basicNack(deliverTag,false,false);
            chartService.handleCharUpdateError(chart.getId(),"更新图标成功状态失败");
            return;

        }
        log.info("接收到消息= {}",message);
        channel.basicAck(deliverTag,false);

    }

    /**
     * 封装用户输入
     * @param chart
     * @return
     */
    private String userInput(Chart chart){

        String goal = chart.getGoal();
        String chartData = chart.getChartData();
        String chartType = chart.getChartType();
        StringBuilder userInput = new StringBuilder();
        //分析需求：
        userInput.append("分析需求:").append("\n").append(goal).append("\n");
        if (StringUtils.isNotBlank(chartType)) {
            userInput.append("请使用" + chartType).append("\n");
        }
        //分析网站用户的增长情况
        //原始数据：
        userInput.append("原始数据:").append("\n").append(chartData).append("\n");
        //日期,用户数
        //1号,10
        //2号,20
        //3号,30
        log.info("用户诉求：{}", userInput);
        return userInput.toString();

    }

}
