package com.zhangwuji.im.api.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangwuji.im.api.common.ControllerUtil;
import com.zhangwuji.im.api.entity.IMUser;
import com.zhangwuji.im.api.entity.OrderList;
import com.zhangwuji.im.api.entity.ServerInfoEntity;
import com.zhangwuji.im.api.entity.Userpaylog;
import com.zhangwuji.im.api.result.ApiResult;
import com.zhangwuji.im.api.service.impl.UserpaylogServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author cloudtalk
 * @since 2019-03-23
 */
@RestController
@RequestMapping("/api/userpaylog")
public class UserpaylogController {
    @Resource
    ControllerUtil controllerUtil;

    @Autowired
    UserpaylogServiceImpl userpaylogService;

    @RequestMapping(value = "get_pay_log", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult getPayLog(HttpServletRequest req, HttpServletResponse rsp) {
        ApiResult returnResult = new ApiResult();
        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(null);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }

        int page= controllerUtil.getIntParameter(req, "page", 1);
        int pagesize= controllerUtil.getIntParameter(req, "pagesize", 20);


        List<Userpaylog> alllist =  userpaylogService.getLogList(myinfo.getId(),1,page,pagesize);
        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(alllist);
        returnResult.setMessage("操作成功!");
        return returnResult;
    }
}
