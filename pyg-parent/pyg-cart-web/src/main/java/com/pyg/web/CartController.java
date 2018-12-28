package com.pyg.web;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pyg.cart.service.CartService;
import com.pyg.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference(timeout = 10000)
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    /**
     * 购物车列表
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        //得到登陆人账号,判断当前是否有人登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人:" + username);

        //从cookie提取购物车
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString == null || cartListString.equals("")) {
            cartListString = "[]";
        }
        List<Cart> cartList_cookies = JSON.parseArray(cartListString, Cart.class);
        if (username.equals("anonymousUser")) {//如果未登录
            System.out.println("从cookie中提取购物车");

            return cartList_cookies;
        } else {//如果已登录
            //从redis 中提取购物车
            List<Cart> cartListFromRedis = cartService.findCartListFromRedis(username);
            if (cartList_cookies.size() > 0) {//如果本地存在购物车
                //得到合并购物车
                List<Cart> cartList = cartService.mergeCartList(cartList_cookies, cartListFromRedis);
                //清除本地 cookie 的数据
                CookieUtil.deleteCookie(request, response, "cartList");
                //将合并后的数据存入 redis
                cartService.saveCartListToRedis(username, cartList);
                System.out.println("执行了合并购物车的逻辑");
                return cartList;
            }
            return cartListFromRedis;
        }
    }

    /**
     * 添加商品到购物车
     *
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        //可以访问的域
        //response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        //如果要操作cookie 必须加上这句话
        //response.setHeader("Access-Control-Allow-Credentials", "true");

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录用户:" + username);
        try {
            List<Cart> cartList = findCartList();//从cookie获取购物车列表
            //调用服务方法操作购物车
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);

            if (username.equals("anonymousUser")) {//如果是未登录，保存到 cookie
                //将新的购物车存入cookie
                String cartListString = JSON.toJSONString(cartList);
                util.CookieUtil.setCookie(request, response, "cartList", cartListString, 3600 * 24, "UTF-8");
                System.out.println("向cookie存入数据");

            } else {//如果是已登录，保存到 redis
                cartService.saveCartListToRedis(username, cartList);
            }
            return new Result(true, "添加到购物车成功");

        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加到购物车失败");
        }
    }
}
