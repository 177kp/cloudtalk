package com.zhangwuji.im.api.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangwuji.im.api.common.AES;
import com.zhangwuji.im.api.common.ControllerUtil;
import com.zhangwuji.im.api.entity.IMMessage;
import com.zhangwuji.im.api.entity.IMUser;
import com.zhangwuji.im.api.entity.Userpaylog;
import com.zhangwuji.im.api.result.ApiResult;
import com.zhangwuji.im.api.service.impl.IMGroupMessageServiceImpl;
import com.zhangwuji.im.api.service.impl.IMMessageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author cloudtalk
 * @since 2019-08-09
 */
@RestController
@RequestMapping("/api/message")
public class IMMessageController {

    @Resource
    ControllerUtil controllerUtil;

    @Autowired
    AES aes;

    @Autowired
    IMMessageServiceImpl messageService;

    @Autowired
    IMGroupMessageServiceImpl imGroupMessageService;

    @RequestMapping(value = "send_im_message", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult send_im_message (HttpServletRequest req, HttpServletResponse rsp) {
        ApiResult returnResult = new ApiResult();
        int fromid= controllerUtil.getIntParameter(req, "fromid", 0);
        int toid= controllerUtil.getIntParameter(req, "toid", 0);
        int sessionType= controllerUtil.getIntParameter(req, "sessionType", 1);
        String msgcontent = controllerUtil.getStringParameter(req, "msgcontent", "");

        controllerUtil.sendIMSystemMessage(1,toid,msgcontent,sessionType,fromid);

        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setMessage("操作成功!");
        return returnResult;
    }

    @RequestMapping(value = "send_message", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult send_message (HttpServletRequest req, HttpServletResponse rsp) {
        ApiResult returnResult = new ApiResult();
        int fromid= controllerUtil.getIntParameter(req, "fromid", 0);
        int toid= controllerUtil.getIntParameter(req, "toid", 0);
        int sessionType= controllerUtil.getIntParameter(req, "sessionType", 1);
        String msgcontent = controllerUtil.getStringParameter(req, "msgcontent", "");

        int relateId=fromid+toid;
        int msgId=0;
        Map<String, Object> msgobj=messageService.get_user_max_msgid(0,relateId);
        if(msgobj!=null)
        {
            msgId= Integer.parseInt(msgobj.get("msgId").toString());
        }

        IMMessage imMessage=new IMMessage();
        imMessage.setContent(msgcontent);
        imMessage.setFlag(0);
        imMessage.setFromId(fromid);
        imMessage.setToId(toid);
        imMessage.setType(1);
        imMessage.setRelateId(relateId);
        imMessage.setMsgId(msgId+1);
        imMessage.setStatus(0);
        imMessage.setCreated(System.currentTimeMillis()/1000);
        imMessage.setUpdated(imMessage.getCreated());
        messageService.save(imMessage);

        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setMessage("操作成功!");
        return returnResult;
    }

    @RequestMapping(value = "get_message_log", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult get_message_log(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }

        int touid= controllerUtil.getIntParameter(req, "id", 0);
        int page= controllerUtil.getIntParameter(req, "page", 1);
        int pagesize= controllerUtil.getIntParameter(req, "pagesize", 20);
        int sessionType=controllerUtil.getIntParameter(req, "sessionType", 1);

        Page<Map<String, Object>> pagelist=new  Page<Map<String, Object>>();
        if(sessionType==1)
        {
            pagelist = messageService.getMessageList(new Page<>(page, pagesize), myinfo.getId(),touid);
        }
        else if(sessionType==2)
        {
            pagelist = imGroupMessageService.getMessageList(new Page<>(page, pagesize),touid);
        }

        List<Map<String, Object>> allmessages=new LinkedList<>();
//        for (Map<String, Object> map:pagelist.getRecords())
//        {
//            String content=map.get("content").toString();
//            if(content!=null && !content.equals(""))
//            {
//                content=aes.decrypt(content);
//            }
//            map.put("content",content);
//            allmessages.add(map);
//        }

        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(pagelist.getRecords());
        returnResult.setMessage("操作成功!");
        return returnResult;
    }


    @RequestMapping(value = "del_message", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult del_message(HttpServletRequest req, HttpServletResponse rsp) {
        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }

        int toId = controllerUtil.getIntParameter(req, "toId", 0);
        int fromId = controllerUtil.getIntParameter(req, "fromId", 0);
        int msgId = controllerUtil.getIntParameter(req, "msgId", 0);
        int sessionType = controllerUtil.getIntParameter(req, "sessionType", 1);

        if(sessionType==1)
        {
            messageService.deleteMessage(fromId,toId,msgId);
        }
        else
        {
            imGroupMessageService.del_group_message(toId,msgId);
        }

        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setMessage("操作成功!");
        return returnResult;
    }

}
