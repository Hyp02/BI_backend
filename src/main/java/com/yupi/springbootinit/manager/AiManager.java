package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Han
 * @data 2023/10/6
 * @apiNode
 */
@Service
public class AiManager {
    @Resource
    private YuCongMingClient yuCongMingClient;

    public String doChart(long modelId,String message){
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);
        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);

        if (response == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Ai响应错误");
        }
        return response.getData().getContent();
    }

}
