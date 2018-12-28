package cn.itcast.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by crowndint on 2018/12/16.
 */
@RestController
public class TestController {
    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;
    @RequestMapping("send")
    public void sendCode() {
        // 生成6位随机数
        final String code = (long) (Math.random() * 1000000) + "";
        System.out.println("验证码：" + code);
        HashMap<String,Object> map = new HashMap<String,Object>();

        map.put("mobile", "18707153308");//手机号
        map.put("template_code", "SMS_123672285");//模板编号
        map.put("sign_name", "品优taotao");//签名
        map.put("param", "{number:"+code+"}");
        jmsMessagingTemplate.convertAndSend("sms", map);
    }
}
