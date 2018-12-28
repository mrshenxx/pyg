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
            System.out.println("�������յ���Ϣ..." + text);
            List<TbItem> list = JSON.parseArray(text, TbItem.class);
            for (TbItem item : list) {
                System.out.println(item.getId() + " " + item.getTitle());
                Map specMap = JSON.parseObject(item.getSpec());//�� spec �ֶ��е� json�ַ���ת��Ϊ map
                item.setSpecMap(specMap);//����ע����ֶθ�ֵ
            }
            itemSearchService.importList(list);//����
            System.out.println("�ɹ����뵽������");
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
