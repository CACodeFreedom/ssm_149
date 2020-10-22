package cn.smbms.controller;

import cn.smbms.pojo.Bill;
import cn.smbms.pojo.Provider;
import cn.smbms.pojo.User;
import cn.smbms.service.bill.BillService;
import cn.smbms.service.provider.ProviderService;
import cn.smbms.tools.Constants;
import com.mysql.jdbc.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

@Controller
public class BillController {
    @Resource
    private BillService billService;
    @Resource
    private ProviderService providerService;

    /**
     * 订单列表
     *
     * @param bill
     * @param queryProductName
     * @param queryProviderId
     * @param queryIsPayment
     * @param request
     * @return
     */
    @RequestMapping("/billlist.html")
    public String billList(Bill bill, String queryProductName, String queryProviderId, String queryIsPayment, HttpServletRequest request) {
        List<Provider> providerList = null;
        List<Bill> billList = null;
        providerList = providerService.getProviderList("", "");
        request.setAttribute("providerList", providerList);
        request.getParameter("queryProductName");
        request.getParameter("queryProviderId");
        request.getParameter("queryIsPayment");
        if (StringUtils.isNullOrEmpty(queryProductName)) {
            queryProductName = "";
        }
        if (StringUtils.isNullOrEmpty(queryIsPayment)) {
            bill.setIsPayment(0);
        } else {
            bill.setIsPayment(Integer.parseInt(queryIsPayment));
        }
        if (StringUtils.isNullOrEmpty(queryProviderId)) {
            bill.setProviderId(0);
        } else {
            bill.setProviderId(Integer.parseInt(queryProviderId));
        }
        bill.setProductName(queryProductName);
        billList = billService.getBillList(bill);
        request.setAttribute("billList", billList);
        request.setAttribute("queryProductName", queryProductName);
        request.setAttribute("queryProviderId", queryProviderId);
        request.setAttribute("queryIsPayment", queryIsPayment);

        return "billlist";
    }

    /**
     * 跳转到添加页面
     *
     * @return
     */
    @RequestMapping(value = "/addBill.html")
    public String addBill() {
        return "billadd";

    }

    /***
     * 保存添加的方法
     * @param bill
     * @param session
     * @return
     */
    @RequestMapping(value = "/savaBill.html", method = RequestMethod.POST)
    public String savaBill(Bill bill, HttpSession session) {
        System.out.println("-------------------------");
        bill.setCreationDate(new Date());
        bill.setCreatedBy(((User) session.getAttribute(Constants.USER_SESSION)).getId());
        System.out.println(bill);
        if (billService.add(bill)) {
            System.out.println("====================" + bill.getProductName());
            return "redirect:/billlist.html";
        }
        return "billadd";
    }

    /**
     * 异步请求供应商名称
     *
     * @return
     */
    @RequestMapping("/providerlist")
    //异步获取数据时使用
    @ResponseBody
    public List<Provider> providerlist() {

        return providerService.getProviderList(null, null);
    }

    /**
     * 查看
     * @param id
     * @param model
     * @return
     */

    @RequestMapping("/billview")

    public String view(String id,Model model){
        Bill bill=billService.getBillById(id);
        model.addAttribute("bill",bill);
        return "billview";
    }



    /**
     * 跳转到修改页面
     *
     * @param id
     * @param model
     * @return
     */
    @RequestMapping("/modifyBill.html")
    public String modifyBill(String id, Model model) {
        Bill bill = billService.getBillById(id);
        System.out.println("id" + id);
        model.addAttribute("bill", bill);
        System.out.println("-----------------" + bill);
        return "billmodify";
    }

    /**
     * 保存修改的方法
     * @param bill
     * @param session
     * @return
     */

    @RequestMapping(value = "/savebill.html",method = RequestMethod.POST)
    public String saveBill(Bill bill,HttpSession session){
        bill.setModifyDate(new Date());//创建时间
        User user = (User) session.getAttribute(Constants.USER_SESSION);//创建者
        bill.setModifyBy(user.getId());

        if (billService.modify(bill)) {
            System.out.println("==============");
            return "redirect:/billlist.html";
        }
        return "billmodify";

    }
}
