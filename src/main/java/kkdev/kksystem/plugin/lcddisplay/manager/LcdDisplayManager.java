/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kkdev.kksystem.plugin.lcddisplay.manager;

import com.sun.xml.internal.fastinfoset.util.StringArray;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import kkdev.kksystem.plugin.lcddisplay.hw.DisplayHW;
import kkdev.kksystem.plugin.lcddisplay.manager.configuration.DisplayPage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import kkdev.kksystem.base.classes.base.PinBaseCommand;
import kkdev.kksystem.base.classes.display.DisplayConstants;
import kkdev.kksystem.base.classes.display.DisplayInfo;
import kkdev.kksystem.base.classes.display.PinLedCommand;
import kkdev.kksystem.base.classes.display.PinLedData;
import kkdev.kksystem.base.classes.plugins.simple.managers.PluginManagerLCD;
import kkdev.kksystem.base.constants.PluginConsts;
import kkdev.kksystem.base.constants.SystemConsts;
import kkdev.kksystem.plugin.lcddisplay.KKPlugin;
import kkdev.kksystem.plugin.lcddisplay.hw.debug.DisplayDebug;
import kkdev.kksystem.plugin.lcddisplay.manager.configuration.PluginSettings;
import kkdev.kksystem.plugin.lcddisplay.hw.rpi.HD44780.DisplayHD44780onRPI;
import kkdev.kksystem.plugin.lcddisplay.hw.DisplayHW.HWDisplayTypes;
import kkdev.kksystem.plugin.lcddisplay.hw.DisplayHW.HWHostTypes;

/**
 *
 * @author blinov_is
 *
 * in now, create and manage only one page "Main", and only one hw display
 *
 */
public class LcdDisplayManager extends PluginManagerLCD {

    static String CurrentFeature;
    static String DefaultDisplay;
    static Map<String, DisplayView> Displays;
    static Map<String, Map<String, List<String>>> SPages;
    static Map<String, String> CurrentPage;              //Feature => PageName
    static Map<String,DisplayPage> DPages;

    public  void Init(KKPlugin Conn) {
        Connector = Conn;

        PluginSettings.InitConfig();
        //
        CurrentFeature = PluginSettings.MainConfiguration.DefaultFeature;
        //
        ConfigAndHWInit();
    }

    private void ConfigAndHWInit() {
        SPages = new HashMap<>();
        Displays = new HashMap<>();
        CurrentPage=new HashMap<>();

        //Add HWDisplays and init
        for (DisplayHW DH : PluginSettings.MainConfiguration.HWDisplays) {
            //Init on RPi Host
            if (DH.HWBoard == HWHostTypes.RaspberryPI_B) {
                if (DH.HWDisplay == HWDisplayTypes.HD44780_4bit) {
                    Displays.put(DH.HWDisplayName, new DisplayView(new DisplayHD44780onRPI()));
                } else {
                    System.out.println("[LCDDisplay][CONFLOADER] Unknown display type in config!! + " + DH.HWBoard);
                }
            } //Debug host
            else if (DH.HWBoard == HWHostTypes.DisplayDebug) {
                Displays.put(DH.HWDisplayName, new DisplayView(new DisplayDebug()));
            } //Config error
            else {
                System.out.println("[LCDDisplay][CONFLOADER] Unknown HW board in config!! + " + DH.HWBoard);
            }

        }
        //Add SPages
        DPages=new HashMap<>();
        for (DisplayPage DP : PluginSettings.MainConfiguration.DisplayPages) {
            DPages.put(DP.PageName, DP);
            DP.InitUIFrames();
            List<String> LS = new ArrayList<>();
            LS.addAll(Arrays.asList(DP.HWDisplays));
            //
            for (String F : DP.Features) {
                if (!SPages.containsKey(F)) {
                    SPages.put(F, new HashMap<>());
                }
                SPages.get(F).put(DP.PageName, LS);
                //
                if (DP.IsDefaultPage)
                    if (!CurrentPage.containsKey(F))
                        CurrentPage.put(F, DP.PageName);
            }
            //
        }

    }


    public void ReceivePin(String FeatureID, String PinName, Object PinData) {

        switch (PinName) {
            case PluginConsts.KK_PLUGIN_BASE_LED_COMMAND:
                PinLedCommand CMD;
                CMD = (PinLedCommand) PinData;
                ProcessCommand(FeatureID, CMD);
                break;
            case PluginConsts.KK_PLUGIN_BASE_LED_DATA:
                PinLedData DAT;
                DAT = (PinLedData) PinData;
                System.out.println("[LCDDisplay][CMD] Received DATA");
                ProcessData(DAT);
                break;
            case PluginConsts.KK_PLUGIN_BASE_PIN_COMMAND:
                PinBaseCommand BaseCMD;
                BaseCMD = (PinBaseCommand) PinData;
                System.out.println("[LCDDisplay][CMD] Received BASE CMD");
                ProcessBaseCommand(BaseCMD);
        }
    }

    ///////////////////
    ///////////////////
    private void ProcessCommand(String FeatureID, PinLedCommand Command) {

        switch (Command.Command) {
            case DISPLAY_KKSYS_PAGE_INIT:
                System.out.println("[LCDDisplay][CMD] Received CMD INIT");
                break;
            case DISPLAY_KKSYS_PAGE_ACTIVATE:
                System.out.println("[LCDDisplay][CMD] Received CMD ACTIVATE");
                SetPageToActive(FeatureID, Command.PageID);
                break;
            case DISPLAY_KKSYS_GETINFO:
                System.out.println("[LCDDisplay][CMD] Received CMD GETINFO");
                AnswerDisplayInfo();
                break;

        }
    }

    private void ProcessData(PinLedData Data) {

        switch (Data.DataType) {
            case DISPLAY_KKSYS_TEXT_SIMPLE_OUT:
                SendTextToPage(Data.FeatureUID, Data.TargetPage, Data.Direct_DisplayText);
                break;
            case DISPLAY_KKSYS_TEXT_UPDATE_DIRECT:

                break;
            case DISPLAY_KKSYS_TEXT_UPDATE_FRAME:
                UpdatePageUIFrames(Data.FeatureUID, Data.TargetPage, Data.OnFrame_DataKeys, Data.OnFrame_DataValues);
                break;
        }
    }

    private void ProcessBaseCommand(PinBaseCommand Command) {
        switch (Command.BaseCommand) {
            case CHANGE_FEATURE:
                System.out.println("[LCDDisplay][MANAGER] Feature changed >> " + CurrentFeature + " >> " + Command.ChangeFeatureID);
                ChangeFeature(CurrentFeature);
                break;
            case PLUGIN:
                break;
        }
    }

    //////////////////
    ///////////////////

    private void AnswerDisplayInfo() {
        PinLedData Ret;
        DisplayInfo[] DI = new DisplayInfo[Displays.values().size()];
        //
        int cnt = 0;
        //
        for (DisplayView DV : Displays.values()) {
            DI[cnt] = DV.Connector.GetDisplayInfo();
            cnt++;
        }
        //     
        Ret = new PinLedData();
        Ret.DisplayState = DI;
        Ret.DataType=DisplayConstants.KK_DISPLAY_DATA.DISPLAY_KKSYS_DISPLAY_STATE;
        //
        DISPLAY_SendPluginMessageData(CurrentFeature, Ret);
        //
    }
    
    //////////////////
    ///////////////////

    private void SendTextToPage(String FeatureID, String PageID, String[] Text) {
        for (String TL : Text) {
            SendTextToPage(FeatureID, PageID, TL);
        }
    }
    private void SendTextToPage(String FeatureID, String PageID, String Text) {
        //Redirect unknown pages to main
        if (!SPages.get(FeatureID).containsKey(PageID)) {
            PageID = "MAIN";
        }
        //
        List<String> DisplayToView;
        DisplayToView = SPages.get(FeatureID).get(PageID);

        DisplayView DV;
        for (String D : DisplayToView) {
            DV = Displays.get(D);
            if (DV != null) {
                if (DV.Enabled & !DV.ErrorState) {
                    DV.SendText(Text);
                }
            }
        }
    }
    private void UpdateTextOnPage(String FeatureID, String PageID, String[] Text, int[] PositionsCol, int[] PositionRow) {
        //Redirect unknown pages to main
        if (!SPages.get(FeatureID).containsKey(PageID)) {
            PageID = "MAIN";
        }
        //
        List<String> DisplayToView;
        DisplayToView = SPages.get(FeatureID).get(PageID);

        for (int i = 0; i <= Text.length; i++) {
            DisplayView DV;
            for (String D : DisplayToView) {
                DV = Displays.get(D);
                if (DV != null) {
                    if (DV.Enabled & !DV.ErrorState) {
                        DV.UpdateText(Text[i], PositionsCol[i], PositionRow[i]);
                    }
                }
            }
        }

    }
    private void UpdatePageUIFrames(String FeatureID, String PageID, String[] Keys, String[] Values)
    {
    
    
    }
    private void SetPageToActive(String FeatureID, String PageID) {
         
        if (CurrentFeature.equals(FeatureID))
        {
           SetPageToInactive(FeatureID,CurrentPage.get(FeatureID));
        }
        //
        CurrentPage.put(FeatureID, PageID);
        //
        DisplayView DV;
        for (String DIS:SPages.get(FeatureID).get(PageID))
        {
            DV=Displays.get(DIS);
            DV.UIFrames=DPages.get(PageID).UIFramesData;
            Displays.get(DIS).SetDisplayState(true);
        }
    }

    private void SetPageToInactive(String FeatureID, String PageID) {
        for (String DIS:SPages.get(FeatureID).get(PageID))
        {
            Displays.get(DIS).SetDisplayState(false);
        }
    }

    private void ChangeFeature(String FeatureID) {
        if (CurrentFeature.equals(FeatureID)) {
            return;
        }
        // Set last selected feature displays to inactive
        SetPageToInactive(FeatureID, CurrentPage.get(FeatureID));
        // Set Current page of feature to Active
        SetPageToActive(FeatureID, CurrentPage.get(CurrentFeature));
         //
        //
        CurrentFeature = FeatureID;
        //
    }
}
