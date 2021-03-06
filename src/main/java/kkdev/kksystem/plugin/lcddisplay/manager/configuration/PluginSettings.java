/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kkdev.kksystem.plugin.lcddisplay.manager.configuration;

import kkdev.kksystem.base.classes.plugins.simple.SettingsManager;


/**
 *
 * @author blinov_is
 */
public abstract class PluginSettings {

    public static String DISPLAY_CONF;
   public static final String DISPLAY_CONF_FRAMES_DIR="//";
    
    public static LcdDisplayConf MainConfiguration;
    
    public static void InitConfig(String GlobalConfigUID, String MyUID)
    {
        DISPLAY_CONF=GlobalConfigUID+"_"+MyUID + ".json";

        SettingsManager settings = new SettingsManager(DISPLAY_CONF, LcdDisplayConf.class);
        
     //   System.out.println("[LCDDisplay][CONFIG] Load configuration");
        MainConfiguration=(LcdDisplayConf) settings.loadConfig();
        
        if (MainConfiguration==null)
        {
            System.out.println("[LCDDisplay][CONFIG] Error Load configuration, try create default config");
            settings.saveConfig(kk_DefaultConfig.MakeDefaultConfig());
            MainConfiguration=(LcdDisplayConf) settings.loadConfig();
        }
        if (MainConfiguration==null)
        {
            System.out.println("[LCDDisplay][CONFIG] Load configuration, fatal");
            return;
        }
    
    }

}
