/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kkdev.kksystem.plugin.lcddisplay;

import kkdev.kksystem.base.classes.plugins.PluginMessage;
import kkdev.kksystem.base.classes.plugins.simple.KKPluginBase;
import kkdev.kksystem.base.interfaces.IKKControllerUtils;
import kkdev.kksystem.base.interfaces.IPluginBaseInterface;
import kkdev.kksystem.plugin.lcddisplay.manager.LcdDisplayManager;


/**     
 *
 * @author blinov_is
 */
public final class KKPlugin extends KKPluginBase   {
    String DisplayID;
        public IKKControllerUtils SysUtils;
    public KKPlugin()
    { 
        super(new LEDPluginInfo());
        Global.PM=new LcdDisplayManager();
        
        DisplayID=java.util.UUID.randomUUID().toString();
    }

    @Override
    public void pluginInit(IPluginBaseInterface BaseConnector, String GlobalConfUID) {
        super.pluginInit(BaseConnector, GlobalConfUID); //To change body of generated methods, choose Tools | Templates.
         SysUtils = BaseConnector.systemUtilities();
        Global.PM.Init(this);
    }

    @Override
    public PluginMessage executePin(PluginMessage Pin) {
        super.executePin(Pin);
        
        Global.PM.ReceivePin(Pin.FeatureID,Pin.pinName,Pin.getPinData());
        return null;
    }
        public IKKControllerUtils GetUtils() {
        return SysUtils;
    }

}
