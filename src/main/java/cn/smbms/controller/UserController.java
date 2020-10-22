package cn.smbms.controller;

import cn.smbms.pojo.Role;
import cn.smbms.pojo.User;
import cn.smbms.service.role.RoleService;
import cn.smbms.service.user.UserService;
import cn.smbms.tools.Constants;
import cn.smbms.tools.PageSupport;
import com.alibaba.fastjson.JSONArray;
import com.mysql.jdbc.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private RoleService roleService;

    /**
     * 跳转登录页面
     *
     * @return
     */
    @RequestMapping("/login.html")
    public String login() {
        return "login";
    }

    /**
     * 跳转注销页面
     * redirect 重定向
     *
     * @return
     */
    @RequestMapping("/logout.html")
    public String logout(HttpSession session) {
        session.removeAttribute(Constants.USER_SESSION);//清空session
        return "redirect:/user/login.html";
    }


    @ExceptionHandler(value = {RuntimeException.class})
    public String handleException(RuntimeException e, HttpServletRequest request) {
        request.setAttribute("e", e.getMessage());
        return "error";
    }

    /**
     * 处理登录页面
     *
     * @param userCode
     * @param userPassword
     * @param
     * @return
     */
    @RequestMapping(value = "/login.html", method = RequestMethod.POST)
    public String doLogin(String userCode, String userPassword, HttpSession session, HttpServletRequest request) {
        User user = userService.login(userCode, userPassword);
        if (user != null) {
            session.setAttribute(Constants.USER_SESSION, user);
            return "redirect:/user/frame.html";
        } else {
            request.setAttribute("error", "用户名或密码不正确！");
            return "login";
        }

    }

    /**
     * 用户管理页面
     *
     * @param model
     * @param queryname
     * @param userRole
     * @param pageIndex
     * @return
     */
    @RequestMapping("/userlist.html")
    public String main(Model model, String queryname, @RequestParam(value = "queryUserRole", defaultValue = "0") Integer userRole,
                       @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex) {
        //查询用户列表
        List<User> userList = null;
        //设置页面容量
        int pageSize = Constants.pageSize;
        //当前页码
        System.out.println("queryUserName servlet--------" + queryname);
        System.out.println("queryUserRole servlet--------" + userRole);
        System.out.println("query pageIndex--------- > " + pageIndex);
        //总数量（表）
        int totalCount = userService.getUserCount(queryname, userRole);
        //总页数
        PageSupport pages = new PageSupport();
        pages.setCurrentPageNo(pageIndex);
        pages.setPageSize(pageSize);
        pages.setTotalCount(totalCount);
        int totalPageCount = pages.getTotalPageCount();
        //控制首页和尾页
        if (pageIndex < 1) {
            pageIndex = 1;
        } else if (pageIndex > totalPageCount) {
            pageIndex = totalPageCount;
        }
        userList = userService.getUserList(queryname, userRole, pageIndex, pageSize);
        model.addAttribute("userList", userList);
        List<Role> roleList = null;
        roleList = roleService.getRoleList();
        model.addAttribute("roleList", roleList);
        model.addAttribute("queryUserName", queryname);
        model.addAttribute("queryUserRole", userRole);
        model.addAttribute("totalPageCount", totalPageCount);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("currentPageNo", pageIndex);
        return "userlist";
    }

    /*
     * 是否登录，不可直接访问其它页面
     * */
    @RequestMapping("/frame.html")
    public String frame(HttpSession session) {
        User user = (User) session.getAttribute(Constants.USER_SESSION);
        if (user == null) {
            return "login";
        }
        return "frame";
    }

    /**
     * 跳转到添加页面
     *
     * @return
     */
    @RequestMapping("/addUser.html")
    public String addUser(@ModelAttribute User user) {

        return "useradd";
    }

    /**
     * 处理添加的功能页面
     *
     * @return
     */
   /*  @RequestMapping(value = "/addUser.html", method = RequestMethod.POST)
   public String saveUser( User user, HttpSession session) {
        user.setCreationDate(new Date());//创建时间
        user.setCreatedBy(((User) session.getAttribute(Constants.USER_SESSION)).getId());//创建者
        if (userService.add(user)) {
            return "redirect:/user/userlist.html";
        }
        return "user/useradd";
    }*/


    /**
     * 使用JSR 303验证
     */
  /*  @RequestMapping(value = "/addUser.html", method = RequestMethod.POST)
    public String saveUser(@Valid User user, BindingResult bindingResult, HttpSession session) {
      if(bindingResult.hasErrors()){
          return "user/useradd";
      }

        user.setCreationDate(new Date());//创建时间
        user.setCreatedBy(((User) session.getAttribute(Constants.USER_SESSION)).getId());//创建者
        if (userService.add(user)) {
            return "redirect:/user/userlist.html";
        }
        return "user/useradd";
    }*/


    /**
     * 单文件上传的方法
     *
     * @param user
     * @param
     * @param request
     * @param
     * @return
     */
    @RequestMapping(value = "/addUser.html", method = RequestMethod.POST)
    public String saveUser(User user, HttpServletRequest request,
                           @RequestParam(value = "a_idPicpath", required = false) MultipartFile a_idPicpath,
                           @RequestParam(value = "workPic", required = false) MultipartFile workPic) {
        String picPath = uploadFile(request, a_idPicpath);//图片路径
        System.out.println("============>" + a_idPicpath);
        String workPicPath = uploadFile(request, workPic);//工作照

        if (picPath == null || workPicPath == null) {
            return "useradd";
        }
        user.setWorkPicPath(workPicPath);
        user.setIdPicPath(picPath);
        user.setCreationDate(new Date());//创建时间
        User user1 = (User) request.getSession().getAttribute(Constants.USER_SESSION);//创建者
        user.setCreatedBy(user1.getId());
        if (userService.add(user)) {
            return "redirect:/user/userlist.html";
        }
        return "useradd";
    }

    /**
     * 文件上传的方法(单文件和多文件共用的方法)
     *
     * @param request
     * @param attan
     * @return null代表是上传失败
     */
    public String uploadFile(HttpServletRequest request, MultipartFile attan) {
        String picPath = "";
        if (!attan.isEmpty()) { //判断是否有上传文件
            // File.separator系统的分隔符
            String path = request.getServletContext().getRealPath("/statics" + File.separator + "uploadFiles");//文件上传的路径 上传到哪里去
            //System.out.println("uploadFile path ========》" + path);
            String oldFileName = attan.getOriginalFilename();//获取原文件名
            //System.out.println("oldFileName ======》" + oldFileName);
            String suffexs = oldFileName.substring(oldFileName.lastIndexOf("."), oldFileName.length());//原文件名后缀
            //System.out.println("uploadFile prefix ======》" + suffexs);
            if (attan.getSize() > 5000000) { //判断文件大小
                request.setAttribute("error", "文件太小，上传失败！");
                return null;
            }
            List<String> prefix = Arrays.asList(new String[]{".jpg", ".png", ".jpeg", ".pneg", ".gif"});
            if (!prefix.contains(suffexs)) {
                request.setAttribute("error", "文件类型错误，上传失败！");
                return null;
            }
            //文件的重命名 解决重命名问题  解决中文乱码
            String newFileName = System.currentTimeMillis() + "" + new Random().nextInt(1000000) + suffexs;
            File file = new File(path, newFileName);
            if (!file.exists()) {
                file.mkdirs();
            }
            try {
                attan.transferTo(file);
            } catch (IOException e) {
                e.printStackTrace();
                request.setAttribute("error", "其它类型错误，上传失败！");
                return null;
            }
            picPath = newFileName;
        }
        return picPath;
    }


    /**
     * 跳转到修改页面
     *
     * @param uid
     * @param model
     * @return
     */
    @RequestMapping("/modify.html")
    public String modify(String uid, Model model) {
        User user = userService.getUserById(uid);
        model.addAttribute("user", user);
        return "usermodify";
    }

    /**
     * 处理保存修改页面的方法
     *
     * @param
     * @param
     * @return
     */
    @RequestMapping(value = "/modify.html", method = RequestMethod.POST)
    public String saveModify(User user, HttpSession session) {
        System.out.println("----------------");
        user.setModifyDate(new Date());//创建时间
        User user1 = (User) session.getAttribute(Constants.USER_SESSION);//创建者
        user.setModifyBy(user1.getId());
        System.out.println(user);
        if (userService.modify(user)) {
            return "redirect:/user/userlist.html";
        }
        return "usermodify";
    }

    /**
     * 跳转查看信息页面
     *
     * @param id
     * @param model
     * @return
     */
   /* @RequestMapping(value = "/view.html/{id}")
    public String view(@PathVariable String id, Model model) {
        User user = userService.getUserById(id);
      //  String picPath = user.getIdPicPath();
        //user.setIdPicPath(picPath.substring(picPath.lastIndexOf("\\") + 1));
        model.addAttribute("user", user);

        return "userview";
    }*/

    /**
     * 跳转查看信息页面
     *
     * @param id
     * @param model
     * @return
     */
    @RequestMapping("/view")
    @ResponseBody
    public User view(String id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return user;
    }

    @RequestMapping("/rolelist")
    @ResponseBody
    public List<Role> rolelist() {

        return roleService.getRoleList();
    }


    /**
     * 异步请求验证添加
     *
     * @param userCode
     * @return
     */
    @RequestMapping(value = "/isExist")
    @ResponseBody
    public Object isExist(String userCode) {
        User user = userService.selectUserCodeExist(userCode);
        Map<String, Object> map = new HashMap<>();
        if (user != null) {
            map.put("userCode", "exist");
        } else {
            map.put("userCode", "noexist");
        }
        return JSONArray.toJSONString(map);
    }


    /**
     * 跳转到密码修改页面
     *
     * @return
     */
    @RequestMapping("/modifypwd.html")
    public String Modifypwd() {

        return "pwdmodify";
    }

    /**
     * Ajax异步调用的方法
     *
     * @param request
     * @param oldpassword
     * @return
     */
    @RequestMapping("/modifypwdbyName")
    @ResponseBody
    public Map getPwdByUserId(HttpServletRequest request, String oldpassword) {
        User user = (User) request.getSession().getAttribute(Constants.USER_SESSION);
        request.getParameter("oldpassword");
        Map<String, String> resultMap = new HashMap<String, String>();

        if (null == user) {//session过期
            resultMap.put("result", "sessionerror");
        } else if (StringUtils.isNullOrEmpty(oldpassword)) {//旧密码输入为空
            resultMap.put("result", "error");
        } else {
            user.getUserPassword();
            if (oldpassword.equals(user.getUserPassword())) {
                resultMap.put("result", "true");
            } else {//旧密码输入不正确
                resultMap.put("result", "false");
            }
        }
        return resultMap;
    }

    /**
     * 保存修改密码的方法
     *
     * @param newpassword
     * @param request
     * @return
     */
    @RequestMapping(value = "/pwdSave.html", method = RequestMethod.POST)
    public String pwdSave(String newpassword, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(Constants.USER_SESSION);
        boolean flag = false;
        if (user != null && !StringUtils.isNullOrEmpty(newpassword)) {
            flag = userService.updatePwd(user.getId(), newpassword);
            if (flag) {
                request.setAttribute(Constants.SYS_MESSAGE, "修改密码成功,请退出并使用新密码重新登录！");
                request.getSession().removeAttribute(Constants.USER_SESSION);//session注销
                return "redirect:/user/login.html";
            } else {
                request.setAttribute(Constants.SYS_MESSAGE, "修改密码失败！");
            }
        } else {
            request.setAttribute(Constants.SYS_MESSAGE, "修改密码失败！");
        }
        return "pwdmodify";
    }


    @RequestMapping(value = "/deluser.html",method = RequestMethod.POST)
    public Map deluser(HttpServletRequest request, String id) {
        Integer delId = 0;
        try {
            delId = Integer.parseInt(id);
        } catch (Exception e) {
            // TODO: handle exception
            delId = 0;
        }
        HashMap<String, String> resultMap = new HashMap<String, String>();
        if (delId <= 0) {
            resultMap.put("delResult", "notexist");
        } else {
            if (userService.deleteUserById(delId)) {
                resultMap.put("delResult", "true");
            } else {
                resultMap.put("delResult", "false");
            }
        }
        return resultMap;
    }

}
