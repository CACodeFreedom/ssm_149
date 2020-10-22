package cn.smbms.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ProperManager {
/*    private  static  ProperManager properManager;*/
    private static  ProperManager properManager=new ProperManager();
    Properties params;
    /**
     * 私有构造器--读取数据库配置文件
     */
    private ProperManager(){
         params=new Properties();
        String configFile = "database.properties";
        InputStream is=ProperManager.class.getClassLoader().getResourceAsStream(configFile);
        try {
            params.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 自己创建
     * @return
     */
    private ProperManager getProperManager(){
        return new ProperManager();
    }

    /**
     * 全局访问点公共使用
     * @return
     */
   /* public synchronized static ProperManager getInstance(){
        if(properManager==null){
            properManager=new ProperManager();
        }
        return properManager;
    }*/
    public  static ProperManager getInstance(){

        return properManager;
    }

    /**
     * 静态内部类
     */
    public static class ProperManager_{
        public static ProperManager properManager(){
            return new ProperManager();
        }
    }

    /**
     * 实例方法
     * @param key
     * @return
     */
    public String getValueByKey(String key){
        return params.getProperty(key);
    }
}
