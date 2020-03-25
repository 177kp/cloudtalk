package com.zhangwuji.im.api.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhangwuji.im.api.common.AES;
import com.zhangwuji.im.api.common.ControllerUtil;
import com.zhangwuji.im.api.entity.IMGroup;
import com.zhangwuji.im.api.entity.IMGroupMember;
import com.zhangwuji.im.api.entity.IMUser;
import com.zhangwuji.im.api.result.ApiResult;
import com.zhangwuji.im.api.service.impl.IMGroupMemberServiceImpl;
import com.zhangwuji.im.api.service.impl.IMGroupMessageServiceImpl;
import com.zhangwuji.im.api.service.impl.IMGroupServiceImpl;
import com.zhangwuji.im.api.service.impl.IMMessageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * IM群消息表 前端控制器
 * </p>
 *
 * @author cloudtalk
 * @since 2019-08-09
 */
@RestController
@RequestMapping("/api/i-mgroup-message")
public class IMGroupMessageController {


    @Resource
    ControllerUtil controllerUtil;

    @Autowired
    AES aes;

    @Autowired
    IMMessageServiceImpl messageService;

    @Autowired
    IMGroupMessageServiceImpl imGroupMessageService;

    @Autowired
    IMGroupServiceImpl imGroupService;

    @Autowired
    IMGroupMemberServiceImpl imGroupMemberService;

    @RequestMapping(value = "disable_send_msg", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult disable_send_msg(HttpServletRequest req, HttpServletResponse rsp) {
        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }

        int groupId= controllerUtil.getIntParameter(req, "groupId", 0);
        int flag= controllerUtil.getIntParameter(req, "flag", 0);
        String uids= controllerUtil.getStringParameter(req, "uids", "");

        IMGroup imGroup=imGroupService.getOne(new QueryWrapper<IMGroup>().eq("id",groupId));
        IMGroupMember imGroupMember=imGroupMemberService.getOne(new QueryWrapper<IMGroupMember>().eq("groupId",groupId).eq("userId",myinfo.getId()).eq("role",1));
        if(imGroup!=null && imGroup.getCreator().equals(myinfo.getId()) || imGroupMember!=null)
        {
            if(uids.equals(""))
            {
                imGroup.setDisableSendMsg(flag);
                imGroupService.updateById(imGroup);
            }
            else
            {
                for(String uid:uids.split(","))
                {
                    IMGroupMember imGroupMember2=imGroupMemberService.getOne(new QueryWrapper<IMGroupMember>().eq("groupId",groupId).eq("userId",uid));
                    imGroupMember2.setDisableSendMsg(flag);
                    imGroupMemberService.updateById(imGroupMember2);
                }
            }

            //发送系统消息
            Map<String, Object> systemMsg = new HashMap<>();
            systemMsg.put("tag","cloudtalk:system");
            systemMsg.put("type","group_dis_msg");
            systemMsg.put("groupId",groupId);
            systemMsg.put("fromId",myinfo.getId());
            systemMsg.put("uids",uids);
            systemMsg.put("flag",flag);
            controllerUtil.sendIMSystemMessage(144,groupId, JSON.toJSONString(systemMsg),2,myinfo.getId());

            returnResult.setCode(ApiResult.SUCCESS);
            returnResult.setData(null);
            returnResult.setMessage("操作成功!");
            return returnResult;
        }
        else
        {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(null);
            returnResult.setMessage("无权限!");
            return returnResult;
        }


    }


}
