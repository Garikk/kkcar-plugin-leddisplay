/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kkdev.kksystem.plugin.lcddisplay;

import kkdev.kksystem.base.classes.PluginInfo;
import kkdev.kksystem.base.classes.PluginMessage;
import kkdev.kksystem.base.classes.display.DisplayConstants;
import kkdev.kksystem.base.classes.display.PinLedData;
import static kkdev.kksystem.base.constants.PluginConsts.KK_PLUGIN_BASE_LED_DATA;
import kkdev.kksystem.base.interfaces.IPluginBaseInterface;
import kkdev.kksystem.base.interfaces.IPluginKKConnector;
import kkdev.kksystem.plugin.lcddisplay.manager.LedDisplayManager;


/**     
 *
 * @author blinov_is
 */
public final class KKPlugin implements IPluginKKConnector   {
    IPluginBaseInterface Connector;
    
    LedDisplayManager LDisplay;
    String MyUID;
    String DisplayID;
    
    public KKPlugin()
    {
        MyUID=GetPluginInfo().PluginUUID;
        DisplayID=java.util.UUID.randomUUID().toString();
    }
    
    @Override
    public PluginInfo GetPluginInfo() {
         return LEDPluginInfo.GetPluginInfo();
    }

    @Override
    public void PluginInit(IPluginBaseInterface BaseConnector) {
       Connector=BaseConnector;
       LedDisplayManager.Init(this);
    }

    @Override
    public void PluginStart() {
        
    }

    @Override
    public void PluginStop() {
        
    }

    @Override
    public PluginMessage ExecutePin(PluginMessage Pin) {

        LedDisplayManager.ReceivePin(Pin.PinName,Pin.PinData);
        return null;
    }

   
     public void SendPluginMessageData(DisplayConstants.KK_DISPLAY_DATA Command, PinLedData Data)
    {
        PluginMessage Msg=new PluginMessage();
        Msg.SenderUID=MyUID;
        Msg.PinName=KK_PLUGIN_BASE_LED_DATA;
        //
        Msg.PinData=Data;
        //
        Connector.ExecutePinCommand(Msg);
    }

}
