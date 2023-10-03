package com.yupi.springbootinit.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import io.netty.util.internal.ResourcesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * excel相关工具类
 *
 * @author Han
 * @data 2023/10/3
 * @apiNode
 */
@Slf4j
public class ExcelUtils {
    /**
     * Excel转CSV
     * @param multipartFile
     * @return
     */
    public static String excelToCsv(MultipartFile multipartFile) {
        //File file = null;
        //try {
        //    file = ResourceUtils.getFile("classpath:网站数据.xlsx");
        //} catch (FileNotFoundException e) {
        //    e.printStackTrace();
        //}
        List<Map<Integer,String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("表格处理错误",e);
            e.printStackTrace();
        }
        if (CollUtil.isEmpty(list)){
            return "";
        }
        // 拼接结果的StringBuild对象
        StringBuilder result = new StringBuilder();
        // 转换为CSV
        // 取出表头
        LinkedHashMap<Integer, String> headerMap = (LinkedHashMap) list.get(0);
        // 过滤为null值
        List<String> notNullHeadData = headerMap.values()
                .stream()
                .filter(ObjectUtil::isNotNull)
                .collect(Collectors.toList());
        // 拼接表头
        result.append(StrUtil.join(",", notNullHeadData)).append("\n");
        for (int i = 1; i < headerMap.size(); i++) {
            // 数据
            LinkedHashMap<Integer, String> dataMap = (LinkedHashMap)list.get(i);
            // 过滤null值
            List<String> notNullData = dataMap.values()
                    .stream()
                    .filter(ObjectUtil::isNotNull)
                    .collect(Collectors.toList());
            // 拼接数据
            result.append(StrUtil.join(",", notNullData)).append("\n");
        }
        log.info(result.toString());
        return result.toString();
    }

    public static void main(String[] args) {
        excelToCsv(null);
    }
}
