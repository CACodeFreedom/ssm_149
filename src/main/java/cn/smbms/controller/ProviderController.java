package cn.smbms.controller;

import cn.smbms.pojo.Provider;
import cn.smbms.pojo.User;
import cn.smbms.service.provider.ProviderService;
import cn.smbms.tools.Constants;
import com.mysql.jdbc.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

@Controller

/*@RequestMapping("/provider")*/

public class ProviderController {
    @Resource
    private ProviderService providerService;

    /**
     * 供应商列表信息
     *
     * @param queryProName
     * @param queryProCode
     * @param request
     * @return
     */
    @RequestMapping("/providerlist.html")
    public String providermain(String queryProName, String queryProCode, HttpServletRequest request) {
        List<Provider> providerList = null;
        if (StringUtils.isNullOrEmpty(queryProName)) {
            queryProName = "";
        }
        if (StringUtils.isNullOrEmpty(queryProCode)) {
            queryProCode = "";
        }

        providerList = providerService.getProviderList(queryProName, queryProCode);
        request.setAttribute("providerList", providerList);
        request.setAttribute("queryProName", queryProName);
        request.setAttribute("queryProCode", queryProCode);

        return "providerlist";
    }

    /**
     * 跳转添加页面
     *
     * @return
     */
    @RequestMapping("/addProvider.html")
    public String addProvider() {

        return "provideradd";
    }

    /**
     * 保存供应商添加的方法
     *
     * @param provider
     * @param session
     * @return
     */
    @RequestMapping(value = "/addProvider.html", method = RequestMethod.POST)
    public String savaProvider(Provider provider, HttpSession session) {
        provider.setCreationDate(new Date());
        /* Provider provider1=(Provider)session.getAttribute(Constants.USER_SESSION);*/
        provider.setCreatedBy(provider.getId());
        if (providerService.add(provider)) {
            return "redirect:/providerlist.html";
        }
        return "provideradd";
    }

    /**
     * 跳转到修改页面
     *
     * @return
     */
    @RequestMapping("/modify.html")
    public String modifyProvider(String id, Model model) {
        Provider provider = providerService.getProviderById(id);
        System.out.println("id"+id);
        model.addAttribute("provider", provider);
        System.out.println("-----------------"+provider);
        return "providermodify";
    }


    /**
     * 处理保存修改页面的方法
     *
     * @param
     * @param
     * @return
     */
    @RequestMapping(value = "/modify.html", method = RequestMethod.POST)
    public String saveModify(Provider provider, HttpSession session) {
        System.out.println("----------------");
        provider.setModifyDate(new Date());//创建时间
        User user = (User) session.getAttribute(Constants.USER_SESSION);//创建者
        provider.setModifyBy(user.getId());
        System.out.println(provider);

        if (providerService.modify(provider)) {
            System.out.println("==============");
            return "redirect:/providerlist.html";
        }
        return "providermodify";
    }

    /**
     * 查看
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = "/view.html/{id}")
    public String view(@PathVariable String id, Model model) {
        Provider provider = providerService.getProviderById(id);
        model.addAttribute("provider", provider);
        return "providerview";
    }
}
