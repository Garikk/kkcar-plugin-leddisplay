/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kkdev.kksystem.plugin.lcddisplay;

import kkdev.kksystem.plugin.lcddisplay.hw.IDisplayConnectorHW;

/**
 *
 * @author blinov_is
 */
public class KKDisplayView {
   public IDisplayConnectorHW Connector;
   public String DisplayID;
   public boolean Enabled;
   public boolean ErrorState;
   
   public KKDisplayView(IDisplayConnectorHW InitConn)
   {
       Connector=InitConn;
       //
       DisplayID=Connector.GetDisplayInfo().DisplayID;
       ((Thread)Connector).start();
//       Connector.InitDisplayHW();
   }
   public void PowerOn()
   {
       Enabled=true;
       Connector.SetPower(Enabled);
               
   }
   public void PowerOff()
   {
       Enabled=false;
       Connector.SetPower(Enabled);
   }
   public void SendText(String Text)
   {
       Connector.DisplayText(Text);
   }
   public void UpdateText(String Text, int Col, int Row)
   {
       Connector.DisplayTextUpdate(Text, Col, Row);
   }
   
}