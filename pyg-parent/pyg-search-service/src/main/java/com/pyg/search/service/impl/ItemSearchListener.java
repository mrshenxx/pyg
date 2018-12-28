package com.pyg.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

@Component
public class ItemSearchListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {

        TextMessage textMessage = (TextMessage) message;
        try {
            String text = textMessage.getText();
            System.out.println("监听接收到消息..." + text);
            List<TbItem> list = JSON.parseArray(text, TbItem.class);
            for (TbItem item : list) {
                System.out.println(item.getId() + " " + item.getTitle());
                Map specMap = JSON.parseObject(item.getSpec());//将 spec 字段中的 json字符串转换为 map
                item.setSpecMap(specMap);//给带注解的字段赋值
            }
            itemSearchService.importList(list);//导入
            System.out.println("成功导入到索引库");
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
