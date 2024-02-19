package cn.octopusyan.dayzmodtranslator;

/**
 * 启动类
 *
 * @author octopus_yan@foxmail.com
 */
public class AppLuncher {

    public static void main(String[] args) {
//        try {
//            Runtime.getRuntime().exec("Taskkill /IM " + FrpManager.FRPC_CLIENT_FILE_NAME + " /f");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        Application.launch(Application.class, args);
    }
}
