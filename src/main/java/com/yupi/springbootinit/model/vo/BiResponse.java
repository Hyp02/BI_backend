package com.yupi.springbootinit.model.vo;

import lombok.Data;

/**
 * @author Han
 * @data 2023/10/6
 * @apiNode
 */
@Data
public class BiResponse {
    /**
     * 生成的图表数据
     */
    private String genChart;
    /**
     * 生成的分析结论
     */
    private String genResult;

}
