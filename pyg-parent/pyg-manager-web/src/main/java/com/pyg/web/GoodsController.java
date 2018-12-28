package com.pyg.web;

import java.util.Arrays;
import java.util.List;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pyg.pojo.TbItem;
import com.pyg.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.pyg.pojo.TbGoods;
import com.pyg.service.GoodsService;

import entity.PageResult;
import entity.Result;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * controller
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

//    @Reference(timeout = 100000)
//    private ItemSearchService itemSearchService;

    //@Reference(timeout = 50000)
    //private ItemPageService itemPageService;



    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return goodsService.findPage(page, rows);
    }


    /**
     * 修改
     *
     * @param goods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Goods goods) {
        try {
            goodsService.update(goods);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Goods findOne(Long id) {
        return goodsService.findOne(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @Autowired
    private Destination queueSolrDeleteDestination;//用户在索引库中删除记录

    @Autowired
    private Destination topicPageDeleteDestination;//用于删除静态网页的消息


    @RequestMapping("/delete")
    public Result delete(final Long[] ids) {
        try {
            goodsService.delete(ids);
           // itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
            jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });

            //删除页面
            jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 查询+分页
     *
     * @param goods
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        return goodsService.findPage(goods, page, rows);
    }

    @Autowired
    private Destination queueSolrDestination;//用于发送 solr 导入的消息

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Destination topicPageDestination;//用于生成商品详细页的消息目标
    /**
     * 更新状态
     *
     * @param ids
     * @param status
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status) {
        try {
            goodsService.updateStatus(ids, status);
          // System.out.println(ids);
            //按照 SPU ID 查询 SKU 列表(状态为 1)
            if (status.equals("2")) {//审核通过
                List<TbItem> itemList = goodsService.findItemListByGoodsIdandStatus(ids, status);
              //  System.out.println(itemList);
                    //调用搜索接口实现数据批量导入
                    if (itemList.size() > 0) {

                        final String jsonString = JSON.toJSONString(itemList);
                        jmsTemplate.send(queueSolrDestination, new MessageCreator() {
                            @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(jsonString);
                        }
                    });

                    } else {
                        System.out.println("没有明细数据");
                    }

                //生成商品详情页
                for (final Long goodsId : ids) {
                   // itemPageService.genItemHtml(goodsId);
                   jmsTemplate.send(topicPageDestination, new MessageCreator() {
                       @Override
                       public Message createMessage(Session session) throws JMSException {
                           return session.createTextMessage(goodsId+"");
                       }
                   });
                }

            }
            return new Result(true, "修改状态成功");
        } catch (Exception e) {

            return new Result(false, "修改状态失败");
        }
    }

    /**
     * 生成静态页（测试）
     *
     * @param goodsId
     */
    /*@RequestMapping("/genHtml")
    public void genHtml(Long goodsId) {

        itemPageService.genItemHtml(goodsId);
    }*/
}
