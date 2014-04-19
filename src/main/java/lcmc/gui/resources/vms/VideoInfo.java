/*
 * This file is part of DRBD Management Console by LINBIT HA-Solutions GmbH
 * written by Rasto Levrinc.
 *
 * Copyright (C) 2009-2010, LINBIT HA-Solutions GmbH.
 * Copyright (C) 2009-2010, Rasto Levrinc
 *
 * DRBD Management Console is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * DRBD Management Console is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with drbd; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package lcmc.gui.resources.vms;

import lcmc.data.*;
import lcmc.gui.Browser;
import lcmc.gui.widget.Widget;
import lcmc.data.VMSXML.VideoData;
import lcmc.utilities.Tools;
import lcmc.utilities.MyButton;

import javax.swing.JPanel;
import javax.swing.JComponent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * This class holds info about virtual video device.
 */
final class VideoInfo extends HardwareInfo {
    /** Parameters. */
    private static final String[] PARAMETERS = {VideoData.MODEL_TYPE,
                                                VideoData.MODEL_VRAM,
                                                VideoData.MODEL_HEADS};

    /** Whether the parameter is editable only in advanced mode. */
    private static final Collection<String> IS_ENABLED_ONLY_IN_ADVANCED =
        new HashSet<String>(Arrays.asList(new String[]{
                                                VideoData.MODEL_VRAM,
                                                VideoData.MODEL_HEADS}));

    /** Short name. */
    private static final Map<String, String> SHORTNAME_MAP =
                                                 new HashMap<String, String>();

    /** Long name. */
    private static final Map<String, String> LONGNAME_MAP =
                                                 new HashMap<String, String>();

    /** Whether the parameter is required. */
    private static final Collection<String> IS_REQUIRED =
        new HashSet<String>(Arrays.asList(new String[]{VideoData.MODEL_TYPE}));

    /** Possible values. */
    private static final Map<String, Value[]> POSSIBLE_VALUES =
                                               new HashMap<String, Value[]>();
    static {
        SHORTNAME_MAP.put(VideoData.MODEL_TYPE,
                          Tools.getString("VideoInfo.ModelType"));
        SHORTNAME_MAP.put(VideoData.MODEL_VRAM,
                          Tools.getString("VideoInfo.ModelVRAM"));
        SHORTNAME_MAP.put(VideoData.MODEL_HEADS,
                          Tools.getString("VideoInfo.ModelHeads"));
    }
    static {
        LONGNAME_MAP.put(VideoData.MODEL_VRAM,
                         Tools.getString("VideoInfo.ModelVRAM.ToolTip"));
        LONGNAME_MAP.put(VideoData.MODEL_HEADS,
                         Tools.getString("VideoInfo.ModelHeads.ToolTip"));
    }
    static {
        POSSIBLE_VALUES.put(VideoData.MODEL_TYPE,
                            new Value[]{new StringValue("cirrus"),
                                        new StringValue("vga"),
                                        new StringValue("vmvga"),
                                        new StringValue("xen")});
    }


    /** Returns "add new" button. */
    static MyButton getNewBtn(final DomainInfo vdi) {
        final MyButton newBtn = new MyButton("Add Video Device");
        newBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        vdi.addVideosPanel();
                    }
                });
                t.start();
            }
        });
        return newBtn;
    }
    /** Table panel. */
    private JComponent tablePanel = null;
    /** Creates the VideoInfo object. */
    VideoInfo(final String name, final Browser browser,
                 final DomainInfo vmsVirtualDomainInfo) {
        super(name, browser, vmsVirtualDomainInfo);
    }

    /** Adds disk table with only this disk to the main panel. */
    @Override
    protected void addHardwareTable(final JPanel mainPanel) {
        tablePanel = getTablePanel("Video Devices",
                                   DomainInfo.VIDEO_TABLE,
                                   getNewBtn(getVMSVirtualDomainInfo()));
        if (getResource().isNew()) {
            Tools.invokeLater(new Runnable() {
                @Override
                public void run() {
                    tablePanel.setVisible(false);
                }
            });
        }
        mainPanel.add(tablePanel);
    }

    /** Returns long description of the specified parameter. */
    @Override
    protected String getParamLongDesc(final String param) {
        final String name = LONGNAME_MAP.get(param);
        if (name == null) {
            return getParamShortDesc(param);
        }
        return name;
    }

    /** Returns short description of the specified parameter. */
    @Override
    protected String getParamShortDesc(final String param) {
        final String name = SHORTNAME_MAP.get(param);
        if (name == null) {
            return param;
        }
        return name;
    }

    /** Returns preferred value for specified parameter. */
    @Override
    protected Value getParamPreferred(final String param) {
        return null;
    }

    /** Returns default value for specified parameter. */
    @Override
    protected Value getParamDefault(final String param) {
        return null;
    }

    /** Returns parameters. */
    @Override
    public String[] getParametersFromXML() {
        return PARAMETERS.clone();
    }

    /** Returns possible choices for drop down lists. */
    @Override
    protected Value[] getParamPossibleChoices(final String param) {
        return POSSIBLE_VALUES.get(param);
    }

    /** Returns section to which the specified parameter belongs. */
    @Override
    protected String getSection(final String param) {
        return "Video Device Options";
    }

    /** Returns true if the specified parameter is required. */
    @Override
    protected boolean isRequired(final String param) {
        return IS_REQUIRED.contains(param);
    }

    /** Returns true if the specified parameter is integer. */
    @Override
    protected boolean isInteger(final String param) {
        return false;
    }

    /** Returns true if the specified parameter is label. */
    @Override
    protected boolean isLabel(final String param) {
        return false;
    }

    /** Returns true if the specified parameter is of time type. */
    @Override
    protected boolean isTimeType(final String param) {
        return false;
    }

    /** Returns whether parameter is checkbox. */
    @Override
    protected boolean isCheckBox(final String param) {
        return false;
    }

    /** Returns the type of the parameter. */
    @Override
    protected String getParamType(final String param) {
        return "undef"; // TODO:
    }

    /** Returns the regexp of the parameter. */
    @Override
    protected String getParamRegexp(final String param) {
        return null;
    }

    /** Applies the changes. */
    @Override
    void apply(final Application.RunMode runMode) {
        if (Application.isTest(runMode)) {
            return;
        }
        Tools.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                getApplyButton().setEnabled(false);
                getRevertButton().setEnabled(false);
                getInfoPanel();
            }
        });
        waitForInfoPanel();
        final Map<String, String> parameters =
                                    getHWParameters(getResource().isNew());
        for (final Host h : getVMSVirtualDomainInfo().getDefinedOnHosts()) {
            final VMSXML vmsxml = getBrowser().getVMSXML(h);
            if (vmsxml != null) {
                parameters.put(VideoData.SAVED_MODEL_TYPE,
                               getParamSaved(VideoData.MODEL_TYPE).getValueForConfig());
                final String domainName =
                                getVMSVirtualDomainInfo().getDomainName();
                final Node domainNode = vmsxml.getDomainNode(domainName);
                modifyXML(vmsxml, domainNode, domainName, parameters);
                final String virshOptions =
                                   getVMSVirtualDomainInfo().getVirshOptions();
                vmsxml.saveAndDefine(domainNode, domainName, virshOptions);
            }
            getResource().setNew(false);
        }
        getBrowser().reload(getNode(), false);
        getBrowser().periodicalVMSUpdate(
                                getVMSVirtualDomainInfo().getDefinedOnHosts());
        Tools.invokeLater(new Runnable() {
            @Override
            public void run() {
                tablePanel.setVisible(true);
            }
        });
        final String[] params = getParametersFromXML();
        if (Application.isLive(runMode)) {
            storeComboBoxValues(params);
        }
        checkResourceFields(null, params);
    }

    /** Returns device parameters. */
    @Override
    protected Map<String, String> getHWParameters(final boolean allParams) {
        final Map<String, String> params = super.getHWParameters(allParams);
        setName(getParamSavedForConfig(VideoData.MODEL_TYPE));
        return params;
    }

    /** Returns data for the table. */
    @Override
    protected Object[][] getTableData(final String tableName) {
        if (DomainInfo.HEADER_TABLE.equals(tableName)) {
            return getVMSVirtualDomainInfo().getMainTableData();
        } else if (DomainInfo.VIDEO_TABLE.equals(tableName)) {
            if (getResource().isNew()) {
                return new Object[][]{};
            }
            return new Object[][]{getVMSVirtualDomainInfo().getVideoDataRow(
                                getName(),
                                null,
                                getVMSVirtualDomainInfo().getVideos(),
                                true)};
        }
        return new Object[][]{};
    }

    /** Returns whether this parameter is advanced. */
    @Override
    protected boolean isAdvanced(final String param) {
        return false;
    }

    /** Whether the parameter should be enabled. */
    @Override
    protected String isEnabled(final String param) {
        if (getResource().isNew() || !VideoData.MODEL_TYPE.equals(param)) {
            return null;
        } else {
            return "";
        }
    }

    /** Whether the parameter should be enabled only in advanced mode. */
    @Override
    protected boolean isEnabledOnlyInAdvancedMode(final String param) {
         return IS_ENABLED_ONLY_IN_ADVANCED.contains(param);
    }

    /** Returns access type of this parameter. */
    @Override
    protected Application.AccessType getAccessType(final String param) {
        return Application.AccessType.ADMIN;
    }

    /** Returns true if the value of the parameter is ok. */
    @Override
    protected boolean checkParam(final String param, final Value newValue) {
        return !isRequired(param)
               || (newValue != null && !newValue.isNothingSelected());
    }

    /** Updates parameters. */
    @Override
    void updateParameters() {
        final Map<String, VideoData> videos =
                              getVMSVirtualDomainInfo().getVideos();
        if (videos != null) {
            final VideoData videoData = videos.get(getName());
            if (videoData != null) {
                for (final String param : getParametersFromXML()) {
                    final Value oldValue = getParamSaved(param);
                    Value value = getParamSaved(param);
                    final Widget wi = getWidget(param, null);
                    for (final Host h
                            : getVMSVirtualDomainInfo().getDefinedOnHosts()) {
                        final VMSXML vmsxml = getBrowser().getVMSXML(h);
                        if (vmsxml != null) {
                            final Value savedValue =
                                                  videoData.getValue(param);
                            if (savedValue != null) {
                                value = savedValue;
                            }
                        }
                    }
                    if (!Tools.areEqual(value, oldValue)) {
                        getResource().setValue(param, value);
                        if (wi != null) {
                            /* only if it is not changed by user. */
                            wi.setValue(value);
                        }
                    }
                }
            }
        }
        updateTable(DomainInfo.HEADER_TABLE);
        updateTable(DomainInfo.VIDEO_TABLE);
        checkResourceFields(null, getParametersFromXML());
    }

    /** Returns string representation. */
    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder(30);
        final Value type = getParamSaved(VideoData.MODEL_TYPE);
        if (type == null) {
            s.append("new video device...");
        } else {
            s.append(type.getValueForConfig());
        }
        return s.toString();
    }

    /** Removes this video device without confirmation dialog. */
    @Override
    protected void removeMyselfNoConfirm(final Application.RunMode runMode) {
        if (Application.isTest(runMode)) {
            return;
        }
        final String virshOptions = getVMSVirtualDomainInfo().getVirshOptions();
        for (final Host h : getVMSVirtualDomainInfo().getDefinedOnHosts()) {
            final VMSXML vmsxml = getBrowser().getVMSXML(h);
            if (vmsxml != null) {
                final Map<String, String> parameters =
                                                new HashMap<String, String>();
                parameters.put(VideoData.SAVED_MODEL_TYPE,
                               getParamSaved(VideoData.MODEL_TYPE).getValueForConfig());
                vmsxml.removeVideoXML(getVMSVirtualDomainInfo().getDomainName(),
                                      parameters,
                                      virshOptions);
            }
        }
        getBrowser().periodicalVMSUpdate(
                                getVMSVirtualDomainInfo().getDefinedOnHosts());
        removeNode();
    }

    /**
     * Returns whether this item is removeable (null), or string why it isn't.
     */
    @Override
    protected String isRemoveable() {
        return null;
    }


    /** Modify device xml. */
    @Override
    protected void modifyXML(final VMSXML vmsxml, final Node node, final String domainName, final Map<String, String> params) {
        if (vmsxml != null) {
            vmsxml.modifyVideoXML(node, domainName, params);
        }
    }
}
