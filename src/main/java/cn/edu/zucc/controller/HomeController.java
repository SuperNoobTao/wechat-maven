package cn.edu.zucc.controller;

import aes.AesException;
import aes.WXBizMsgCrypt;
import cn.edu.zucc.service.CoreService;
import cn.edu.zucc.util.SignUtil;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Map;


/**
 * Created by vito on 2016/7/26.
 */
@Controller
@RequestMapping("/welcome")
public class HomeController {

    private String token = "weixinCourse";
    private String encodingAESKey = "5MeUKsTBjZStOa9VvyB7FqAFSbdlaoF3Z17yAVDFbof";
    private String appId = "wx8bc2ceff5caf27b6";

    @RequestMapping(value="/api",method = RequestMethod.GET)
    @ResponseBody
    public void api( HttpServletResponse response, HttpServletRequest request) throws IOException {

        // 微信加密签名
        String signature = request.getParameter("signature");
        System.out.println("微信加密签名"+signature);
        // 时间戳
        String timestamp = request.getParameter("timestamp");
        System.out.println("时间戳"+timestamp);
        // 随机数
        String nonce = request.getParameter("nonce");
        System.out.println("随机数"+nonce);
        // 随机字符串
        String echostr = request.getParameter("echostr");
        PrintWriter out = response.getWriter();
        // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
        if (SignUtil.checkSignature(signature, timestamp, nonce)) {
            out.print(echostr);
        }
        System.out.println(echostr);
        out.close();
        out = null;

    }


    @RequestMapping(value="/api",method = RequestMethod.POST)
    @ResponseBody
    public void  getWeiXinMessage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 将请求、响应的编码均设置为UTF-8（防止中文乱码）
        request.setCharacterEncoding("UTF-8");  //微信服务器POST消息时用的是UTF-8编码，在接收时也要用同样的编码，否则中文会乱码；
        response.setCharacterEncoding("UTF-8"); //在响应消息（回复消息给用户）时，也将编码方式设置为UTF-8，原理同上；
        //初始化配置文件

        // 微信加密签名
        String msg_signature = request.getParameter("msg_signature");
        // 时间戳
        String timestamp = request.getParameter("timestamp");
        // 随机数
        String nonce = request.getParameter("nonce");


        //从请求中读取整个post数据
        InputStream inputStream = request.getInputStream();
        String postData = IOUtils.toString(inputStream, "UTF-8");
        System.out.println(postData);

        String msg = "";
        WXBizMsgCrypt wxcpt = null;
        try {
            wxcpt = new WXBizMsgCrypt(token, encodingAESKey, appId);
            //解密消息
            msg = wxcpt.decryptMsg(msg_signature, timestamp, nonce, postData);
        } catch (AesException e) {
            e.printStackTrace();
        }
        System.out.println("msg=" + msg);
        // 调用核心业务类接收消息、处理消息
        String respMessage = CoreService.processRequest(msg);
        System.out.println("respMessage=" + respMessage);

        String encryptMsg = "";
        try {
            //加密回复消息
            encryptMsg = wxcpt.encryptMsg(respMessage, timestamp, nonce);
        } catch (AesException e) {
            e.printStackTrace();
        }

        // 响应消息
        PrintWriter out = response.getWriter();
        out.print(encryptMsg);
        out.close();


    }


}
