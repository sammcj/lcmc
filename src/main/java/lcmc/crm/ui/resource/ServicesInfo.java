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

package lcmc.crm.ui.resource;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

import com.google.common.base.Optional;
import lcmc.common.domain.AccessMode;
import lcmc.common.ui.GUIData;
import lcmc.common.domain.Application;
import lcmc.common.ui.treemenu.TreeMenuController;
import lcmc.common.ui.utils.Dialogs;
import lcmc.crm.domain.CrmXml;
import lcmc.crm.domain.ClusterStatus;
import lcmc.host.domain.Host;
import lcmc.crm.domain.PtestData;
import lcmc.crm.domain.ResourceAgent;
import lcmc.common.domain.StringValue;
import lcmc.common.domain.Value;
import lcmc.common.domain.Resource;
import lcmc.common.ui.Browser;
import lcmc.crm.ui.CrmGraph;
import lcmc.cluster.ui.ClusterBrowser;
import lcmc.common.ui.EditableInfo;
import lcmc.common.ui.Info;
import lcmc.cluster.ui.widget.Check;
import lcmc.cluster.ui.widget.Widget;
import lcmc.common.ui.utils.ButtonCallback;
import lcmc.crm.service.CRM;
import lcmc.common.ui.utils.ComponentWithTest;
import lcmc.logger.Logger;
import lcmc.logger.LoggerFactory;
import lcmc.common.domain.util.Tools;
import lcmc.common.ui.utils.UpdatableItem;

/**
 * This class holds info data for services view and global heartbeat
 * config.
 */
@Named
public class ServicesInfo extends EditableInfo {
    private static final Logger LOG = LoggerFactory.getLogger(ServicesInfo.class);
    static final ImageIcon CLUSTER_ICON = Tools.createImageIcon(Tools.getDefault("ClustersPanel.ClusterIcon"));
    /** Cache for the info panel. */
    private JComponent infoPanel = null;

    @Inject
    private ServicesMenu servicesMenu;
    @Inject
    private GUIData guiData;
    @Inject
    private Provider<ConstraintPHInfo> constraintPHInfoProvider;
    @Inject
    private Provider<PcmkRscSetsInfo> pcmkRscSetsInfoProvider;
    @Inject
    private Application application;
    @Inject
    private TreeMenuController treeMenuController;
    @Inject
    private CrmServiceFactory crmServiceFactory;
    @Inject
    private Dialogs dialogs;

    @Override
    public void init(final String name, final Browser browser) {
        super.init(name, browser);
        setResource(new Resource(name));
    }

    @Override
    public ClusterBrowser getBrowser() {
        return (ClusterBrowser) super.getBrowser();
    }

    void setInfoPanel(final JComponent infoPanel) {
        this.infoPanel = infoPanel;
    }

    /** Returns names of all global parameters. */
    @Override
    public String[] getParametersFromXML() {
        final CrmXml crmXml = getBrowser().getCrmXml();
        if (crmXml == null) {
            return null;
        }
        return crmXml.getGlobalParameters();
    }

    /**
     * Returns long description of the global parameter, that is used for
     * tool tips.
     */
    @Override
    protected String getParamLongDesc(final String param) {
        return getBrowser().getCrmXml().getGlobalLongDesc(param);
    }

    /**
     * Returns short description of the global parameter, that is used as
     * label.
     */
    @Override
    protected String getParamShortDesc(final String param) {
        return getBrowser().getCrmXml().getGlobalShortDesc(param);
    }

    @Override
    protected Value getParamDefault(final String param) {
        return getBrowser().getCrmXml().getGlobalParamDefault(param);
    }

    @Override
    protected Value getParamPreferred(final String param) {
        return getBrowser().getCrmXml().getGlobalPreferredValue(param);
    }

    @Override
    protected Value[] getParamPossibleChoices(final String param) {
        return getBrowser().getCrmXml().getGlobalComboBoxChoices(param);
    }

    @Override
    protected boolean checkParam(final String param, final Value newValue) {
        return getBrowser().getCrmXml().checkGlobalParam(param, newValue);
    }

    @Override
    protected boolean isInteger(final String param) {
        return getBrowser().getCrmXml().isGlobalInteger(param);
    }
    @Override
    protected boolean isLabel(final String param) {
        return getBrowser().getCrmXml().isGlobalLabel(param);
    }

    @Override
    protected boolean isTimeType(final String param) {
        return getBrowser().getCrmXml().isGlobalTimeType(param);
    }

    @Override
    protected boolean isAdvanced(final String param) {
        return Tools.areEqual(getParamDefault(param), getParamSaved(param))
               && getBrowser().getCrmXml().isGlobalParamAdvanced(param);
    }

    @Override
    protected AccessMode.Type getAccessType(final String param) {
        return getBrowser().getCrmXml().getGlobalParamAccessType(param);
    }

    @Override
    protected String isEnabled(final String param) {
        return null;
    }

    @Override
    protected AccessMode.Mode isEnabledOnlyInAdvancedMode(final String param) {
        return AccessMode.NORMAL;
    }

    @Override
    protected boolean isRequired(final String param) {
        return getBrowser().getCrmXml().isGlobalRequired(param);
    }

    /**
     * Returns whether the global parameter is of boolean type and
     * requires a checkbox.
     */
    @Override
    protected boolean isCheckBox(final String param) {
        return getBrowser().getCrmXml().isGlobalBoolean(param);
    }

    @Override
    protected String getParamType(final String param) {
        return getBrowser().getCrmXml().getGlobalType(param);
    }

    @Override
    protected String getSection(final String param) {
        return getBrowser().getCrmXml().getGlobalSectionForDisplay(param);
    }

    void apply(final Host dcHost, final Application.RunMode runMode) {
        LOG.debug1("apply: start: test: " + runMode);
        final String[] params = getParametersFromXML();
        if (Application.isLive(runMode)) {
            application.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    getApplyButton().setEnabled(false);
                    getRevertButton().setEnabled(false);
                    getApplyButton().setToolTipText("");
                }
            });
        }
        getInfoPanel();
        waitForInfoPanel();

        /* update pacemaker */
        final Map<String, String> args = new HashMap<String, String>();
        for (final String param : params) {
            final Value value = getComboBoxValue(param);
            if (Tools.areEqual(value, getParamDefault(param))) {
                continue;
            }

            if (value == null || value.isNothingSelected()) {
                continue;
            }
            args.put(param, value.getValueForConfig());
        }
        final RscDefaultsInfo rdi = getBrowser().getRscDefaultsInfo();
        final String[] rdiParams = rdi.getParametersFromXML();
        final Map<String, String> rdiMetaArgs = new LinkedHashMap<String, String>();
        for (final String param : rdiParams) {
            final Value value = rdi.getComboBoxValue(param);
            if (Tools.areEqual(value, rdi.getParamDefault(param))) {
                continue;
            }
            if (value != null && !value.isNothingSelected()) {
                rdiMetaArgs.put(param, value.getValueForConfig());
            }
        }
        final String rscDefaultsId = getBrowser().getClusterStatus().getRscDefaultsId(runMode);
        CRM.setGlobalParameters(dcHost, args, rdiMetaArgs, rscDefaultsId, runMode);
        if (Application.isLive(runMode)) {
            storeComboBoxValues(params);
            rdi.storeComboBoxValues(rdiParams);
        }
        for (final ServiceInfo si : getBrowser().getExistingServiceList(null)) {
            final Check check = si.checkResourceFields(null, si.getParametersFromXML(), true, false, false);
            if (check.isCorrect() && check.isChanged()) {
                si.apply(dcHost, runMode);
            }
        }
        if (Application.isLive(runMode)) {
            setApplyButtons(null, params);
        }
        LOG.debug1("apply: end: test: " + runMode);
    }

    /** Sets heartbeat global parameters after they were obtained. */
    public void setGlobalConfig(final ClusterStatus clStatus) {
        final String[] params = getParametersFromXML();
        for (final String param : params) {
            final String valueS = clStatus.getGlobalParam(param);
            if (valueS == null) {
                continue;
            }
            final Value value = new StringValue(valueS);
            final Value oldValue = getParamSaved(param);
            if (!Tools.areEqual(value, oldValue)) {
                getResource().setValue(param, value);
                final Widget wi = getWidget(param, null);
                if (wi != null) {
                    wi.setValue(value);
                }
            }
        }
        if (infoPanel == null) {
            application.invokeLater(new Runnable() {
                @Override
                public void run() {
                    getInfoPanel();
                }
            });
        }
    }

    /**
     * Check if this connection is filesystem with drbd ra and if so, set it.
     */
    private void setFilesystemWithDrbd(final ServiceInfo siP, final ServiceInfo si) {
        if (siP.getResourceAgent().isLinbitDrbd()) {
            /* linbit::drbd -> Filesystem */
            ((FilesystemRaInfo) si).setLinbitDrbdInfo((LinbitDrbdInfo) siP);
        } else {
            /* drbddisk -> Filesystem */
            ((FilesystemRaInfo) si).setDrbddiskInfo((DrbddiskInfo) siP);
        }
    }

    /** Sets clone info object. */
    private CloneInfo setCreateCloneInfo(final String cloneId,
                                         final ClusterStatus clStatus,
                                         final Application.RunMode runMode) {
        CloneInfo newCi = (CloneInfo) getBrowser().getServiceInfoFromCRMId(cloneId);
        final CrmGraph hg = getBrowser().getCrmGraph();
        if (newCi == null) {
            final Point2D p = null;
            newCi =
               (CloneInfo) addServicePanel(getBrowser().getCrmXml().getCloneResourceAgent(),
                                           p,
                                           false,
                                           cloneId,
                                           null,
                                           runMode);
            getBrowser().addToHeartbeatIdList(newCi);
            final Map<String, String> resourceNode = clStatus.getParamValuePairs(newCi.getHeartbeatId(runMode));
            newCi.setParameters(resourceNode);
        } else {
            final Map<String, String> resourceNode = clStatus.getParamValuePairs(newCi.getHeartbeatId(runMode));
            newCi.setParameters(resourceNode);
            if (Application.isLive(runMode)) {
                newCi.setUpdated(false);
                hg.repaint();
            }
        }
        newCi.getService().setNew(false);
        return newCi;
    }

    private GroupInfo setCreateGroupInfo(final String group,
                                         final CloneInfo newCi,
                                         final ClusterStatus clStatus,
                                         final Application.RunMode runMode) {
        GroupInfo newGi = (GroupInfo) getBrowser().getServiceInfoFromCRMId(group);
        final CrmGraph hg = getBrowser().getCrmGraph();
        if (newGi == null) {
            final Point2D p = null;
            newGi =
              (GroupInfo) addServicePanel(getBrowser().getCrmXml().getGroupResourceAgent(),
                                          p,
                                          false,
                                          group,
                                          newCi,
                                          runMode);
            final Map<String, String> resourceNode = clStatus.getParamValuePairs(newGi.getHeartbeatId(runMode));
            newGi.setParameters(resourceNode);
            if (newCi != null) {
                newCi.addCloneServicePanel(newGi);
            }
        } else {
            final Map<String, String> resourceNode = clStatus.getParamValuePairs(newGi.getHeartbeatId(runMode));
            newGi.setParameters(resourceNode);
            if (Application.isLive(runMode)) {
                newGi.setUpdated(false);
                hg.repaint();
            }
        }
        newGi.getService().setNew(false);
        return newGi;
    }

    private void setGroupResources(final Set<String> allGroupsAndClones,
                                   final String grpOrCloneId,
                                   final GroupInfo newGi,
                                   final CloneInfo newCi,
                                   final List<ServiceInfo> serviceIsPresent,
                                   final List<ServiceInfo> groupServiceIsPresent,
                                   final ClusterStatus clStatus,
                                   final Application.RunMode runMode) {
        final Map<ServiceInfo, Map<String, String>> setParametersHash = new HashMap<ServiceInfo, Map<String, String>>();
        if (newCi != null) {
            setParametersHash.put(newCi, clStatus.getParamValuePairs(grpOrCloneId));
        } else if (newGi != null) {
            setParametersHash.put(newGi, clStatus.getParamValuePairs(grpOrCloneId));
        }
        final CrmGraph hg = getBrowser().getCrmGraph();
        final List<String> gs = clStatus.getGroupResources(grpOrCloneId, runMode);
        if (gs == null) {
            return;
        }
        boolean newService = false;
        int pos = 0;
        for (final String hbId : gs) {
            if (clStatus.isOrphaned(hbId) && application.isHideLRM()) {
                continue;
            }
            ServiceInfo newServiceInfo;
            if (allGroupsAndClones.contains(hbId)) {
                if (newGi != null) {
                    LOG.appWarning("setGroupResources: group in group not implemented");
                    continue;
                }
                /* clone group */
                final GroupInfo gi = setCreateGroupInfo(hbId, newCi, clStatus, runMode);
                setGroupResources(allGroupsAndClones,
                                  hbId,
                                  gi,
                                  null,
                                  serviceIsPresent,
                                  groupServiceIsPresent,
                                  clStatus,
                                  runMode);
                newServiceInfo = gi;
            } else {
                final ResourceAgent newResourceAgent = clStatus.getResourceType(hbId);
                if (newResourceAgent == null) {
                    /* This is bad. There is a service but we do not have
                     * the heartbeat script of this service or the we look
                     * in the wrong places.
                     */
                    LOG.appWarning("setGroupResources: " + hbId + ": could not find resource agent");
                }
                /* continue of creating/updating of the
                 * service in the gui.
                 */
                newServiceInfo = getBrowser().getServiceInfoFromCRMId(hbId);
                final Map<String, String> resourceNode = clStatus.getParamValuePairs(hbId);
                if (newServiceInfo == null) {
                    newService = true;
                    newServiceInfo = crmServiceFactory.createServiceWithParameters(
                            hbId,
                            newResourceAgent,
                            resourceNode,
                            getBrowser());
                    newServiceInfo.getService().setCrmId(hbId);
                    getBrowser().addToHeartbeatIdList(newServiceInfo);
                    if (newGi != null) {
                        newGi.addGroupServicePanel(newServiceInfo, false);
                    } else if (newCi != null) {
                        newCi.addCloneServicePanel(newServiceInfo);
                    } else {
                        final Point2D p = null;
                        addServicePanel(newServiceInfo, p, false, false, runMode);
                    }
                } else {
                    getBrowser().addNameToServiceInfoHash(newServiceInfo);
                    setParametersHash.put(newServiceInfo, resourceNode);
                }
                newServiceInfo.getService().setNew(false);
                serviceIsPresent.add(newServiceInfo);
                if (newGi != null || newCi != null) {
                    groupServiceIsPresent.add(newServiceInfo);
                }
            }
            final DefaultMutableTreeNode node = newServiceInfo.getNode();
            if (node != null) {
                treeMenuController.moveNodeToPosition(node, pos);
                pos++;
            }
        }

        for (final Map.Entry<ServiceInfo, Map<String, String>> setEntry : setParametersHash.entrySet()) {
            setEntry.getKey().setParameters(setEntry.getValue());
            if (Application.isLive(runMode)) {
                setEntry.getKey().setUpdated(false);
            }
        }
        if (newService) {
            treeMenuController.reloadNode(getBrowser().getServicesNode(), false);
        }
        hg.repaint();
    }

    /**
     * This functions goes through all services, constrains etc. in
     * clusterStatus and updates the internal structures and graph.
     */
    public void setAllResources(final ClusterStatus clStatus, final Application.RunMode runMode) {
        if (clStatus == null) {
            return;
        }
        final Set<String> allGroupsAndClones = clStatus.getAllGroups();
        final CrmGraph hg = getBrowser().getCrmGraph();
        final List<ServiceInfo> groupServiceIsPresent = new ArrayList<ServiceInfo>();
        final List<ServiceInfo> serviceIsPresent = new ArrayList<ServiceInfo>();
        for (final String groupOrClone : allGroupsAndClones) {
            CloneInfo newCi = null;
            GroupInfo newGi = null;
            if (clStatus.isClone(groupOrClone)) {
                /* clone */
                newCi = setCreateCloneInfo(groupOrClone, clStatus, runMode);
                serviceIsPresent.add(newCi);
            } else if (!"none".equals(groupOrClone)) {
                /* group */
                final GroupInfo gi = (GroupInfo) getBrowser().getServiceInfoFromCRMId(groupOrClone);
                if (gi != null && gi.getCloneInfo() != null) {
                    /* cloned group is already done */
                    groupServiceIsPresent.add(gi);
                    continue;
                }
                newGi = setCreateGroupInfo(groupOrClone, newCi, clStatus, runMode);
                serviceIsPresent.add(newGi);
            }
            setGroupResources(allGroupsAndClones,
                              groupOrClone,
                              newGi,
                              newCi,
                              serviceIsPresent,
                              groupServiceIsPresent,
                              clStatus,
                              runMode);
        }

        hg.clearKeepColocationList();
        hg.clearKeepOrderList();
        /* resource sets */
        final List<CrmXml.RscSetConnectionData> rscSetConnections = clStatus.getRscSetConnections();
        if (rscSetConnections != null) {
            final Map<CrmXml.RscSetConnectionData, ConstraintPHInfo> rdataToCphi =
                                                     new LinkedHashMap<CrmXml.RscSetConnectionData, ConstraintPHInfo>();
            getBrowser().lockNameToServiceInfo();
            final Map<String, ServiceInfo> idToInfoHash = getBrowser().getNameToServiceInfoHash(ConstraintPHInfo.NAME);
            final List<ConstraintPHInfo> preNewCphis = new ArrayList<ConstraintPHInfo>();
            if (idToInfoHash != null) {
                for (final Map.Entry<String, ServiceInfo> infoEntry : idToInfoHash.entrySet()) {
                    final ConstraintPHInfo cphi = (ConstraintPHInfo) infoEntry.getValue();
                    final CrmXml.RscSetConnectionData rdataOrd = cphi.getRscSetConnectionDataOrder();
                    final CrmXml.RscSetConnectionData rdataCol = cphi.getRscSetConnectionDataColocation();
                    if (cphi.getService().isNew()) {
                        preNewCphis.add(cphi);
                    }
                    if (rdataOrd != null && !rdataOrd.isEmpty()) {
                        rdataToCphi.put(rdataOrd, cphi);
                    }
                    if (rdataCol != null && !rdataCol.isEmpty()) {
                        rdataToCphi.put(rdataCol, cphi);
                    }
                }
            }
            getBrowser().unlockNameToServiceInfo();
            final Collection<ConstraintPHInfo> newCphis = new ArrayList<ConstraintPHInfo>();
            for (final CrmXml.RscSetConnectionData rdata : rscSetConnections) {
                ConstraintPHInfo constraintPHInfo = null;

                for (final Map.Entry<CrmXml.RscSetConnectionData, ConstraintPHInfo> phEntry : rdataToCphi.entrySet()) {
                    if (phEntry.getKey() == rdata) {
                        continue;
                    }
                    if (rdata.equals(phEntry.getKey()) || rdata.equalsAlthoughReversed(phEntry.getKey())) {
                        constraintPHInfo = phEntry.getValue();
                        constraintPHInfo.setRscSetConnectionData(rdata);
                        break;
                    }
                }
                PcmkRscSetsInfo rscSetsInfo = null;
                if (constraintPHInfo == null) {
                    for (final Map.Entry<CrmXml.RscSetConnectionData, ConstraintPHInfo> phEntry
                            : rdataToCphi.entrySet()) {
                        if (phEntry.getKey() == rdata) {
                            constraintPHInfo = phEntry.getValue();
                            break;
                        }
                        if (phEntry.getValue().sameConstraintId(rdata)) {
                            /* use the same rsc set info object */
                            rscSetsInfo = phEntry.getValue().getPcmkRscSetsInfo();
                        }
                        if (phEntry.getValue().getService().isNew()
                            || (rdata.canUseSamePlaceholder(phEntry.getKey())
                                && phEntry.getValue().sameConstraintId(rdata))) {
                            constraintPHInfo = phEntry.getValue();
                            constraintPHInfo.setRscSetConnectionData(rdata);
                            rscSetsInfo = constraintPHInfo.getPcmkRscSetsInfo();
                            if (rscSetsInfo != null) {
                                if (rdata.isColocation()) {
                                    rscSetsInfo.addColocation(rdata.getConstraintId(), constraintPHInfo);
                                } else {
                                    rscSetsInfo.addOrder(rdata.getConstraintId(), constraintPHInfo);
                                }
                            }
                            break;
                        }
                    }
                }
                if (constraintPHInfo == null && !preNewCphis.isEmpty()) {
                    /* placeholder */
                    constraintPHInfo = preNewCphis.remove(0);
                    rdataToCphi.put(rdata, constraintPHInfo);
                    constraintPHInfo.setRscSetConnectionData(rdata);
                }
                if (constraintPHInfo == null) {
                    constraintPHInfo = constraintPHInfoProvider.get();
                    constraintPHInfo.init(getBrowser(), rdata, ConstraintPHInfo.Preference.AND);
                    if (rscSetsInfo == null) {
                        rscSetsInfo = pcmkRscSetsInfoProvider.get();
                        rscSetsInfo.init(getBrowser());
                    }
                    if (rdata.isColocation()) {
                        rscSetsInfo.addColocation(rdata.getConstraintId(), constraintPHInfo);
                    } else {
                        rscSetsInfo.addOrder(rdata.getConstraintId(), constraintPHInfo);
                    }
                    constraintPHInfo.setPcmkRscSetsInfo(rscSetsInfo);
                    getBrowser().addNameToServiceInfoHash(constraintPHInfo);
                    newCphis.add(constraintPHInfo); /* have to add it later,
                                           so that ids are correct. */
                    rdataToCphi.put(rdata, constraintPHInfo);
                }
                serviceIsPresent.add(constraintPHInfo);

                final CrmXml.RscSet rscSet1 = rdata.getRscSet1();
                final CrmXml.RscSet rscSet2 = rdata.getRscSet2();
                if (rdata.isColocation()) {
                    /* colocation */
                    if (rscSet1 != null) {
                        for (final String rscId : rscSet1.getRscIds()) {
                            final ServiceInfo si =
                               getBrowser().getServiceInfoFromCRMId(rscId);
                            hg.addColocation(rdata.getConstraintId(), constraintPHInfo, si);
                        }
                    }
                    if (rscSet2 != null) {
                        for (final String rscId : rscSet2.getRscIds()) {
                            final ServiceInfo si = getBrowser().getServiceInfoFromCRMId( rscId);
                            hg.addColocation(rdata.getConstraintId(), si, constraintPHInfo);
                        }
                    }
                } else {
                    /* order */
                    if (rscSet1 != null) {
                        for (final String rscId : rscSet1.getRscIds()) {
                            final ServiceInfo si = getBrowser().getServiceInfoFromCRMId( rscId);
                            hg.addOrder(rdata.getConstraintId(), si, constraintPHInfo);
                        }
                    }
                    if (rscSet2 != null) {
                        for (final String rscId : rscSet2.getRscIds()) {
                            final ServiceInfo si = getBrowser().getServiceInfoFromCRMId(rscId);
                            hg.addOrder(rdata.getConstraintId(), constraintPHInfo, si);
                        }
                    }
                }
                if (Application.isLive(runMode)) {
                    constraintPHInfo.setUpdated(false);
                    constraintPHInfo.getService().setNew(false);
                }
            }

            for (final ConstraintPHInfo cphi : newCphis) {
                hg.addConstraintPlaceholder(cphi,
                                            null, /* pos */
                                            Application.RunMode.LIVE);
            }
        }

        /* colocations */
        final Map<String, List<CrmXml.ColocationData>> colocationMap = clStatus.getColocationRscMap();
        for (final Map.Entry<String, List<CrmXml.ColocationData>> colocationEntry : colocationMap.entrySet()) {
            final List<CrmXml.ColocationData> withs = colocationEntry.getValue();
            for (final CrmXml.ColocationData data : withs) {
                final String withRscId = data.getWithRsc();
                final ServiceInfo withSi = getBrowser().getServiceInfoFromCRMId(withRscId);
                final ServiceInfo siP = getBrowser().getServiceInfoFromCRMId(colocationEntry.getKey());
                hg.addColocation(data.getId(), siP, withSi);
            }
        }

        /* orders */
        final Map<String, List<CrmXml.OrderData>> orderMap = clStatus.getOrderRscMap();
        for (final Map.Entry<String, List<CrmXml.OrderData>> orderEntry : orderMap.entrySet()) {
            for (final CrmXml.OrderData data : orderEntry.getValue()) {
                final String rscThenId = data.getRscThen();
                final ServiceInfo si = getBrowser().getServiceInfoFromCRMId(rscThenId);
                if (si != null) { /* not yet complete */
                    final ServiceInfo siP = getBrowser().getServiceInfoFromCRMId(orderEntry.getKey());
                    if (siP != null && siP.getResourceAgent() != null) {
                        /* dangling orders and colocations */
                        if ((siP.getResourceAgent().isDrbddisk() || siP.getResourceAgent().isLinbitDrbd())
                            && "Filesystem".equals(si.getName())) {
                            final List<CrmXml.ColocationData> cds = clStatus.getColocationDatas(orderEntry.getKey());
                            if (cds != null) {
                                for (final CrmXml.ColocationData cd : cds) {
                                    if (cd.getWithRsc().equals(rscThenId)) {
                                        setFilesystemWithDrbd(siP, si);
                                    }
                                }
                            }
                        }
                        hg.addOrder(data.getId(), siP, si);
                    }
                }
            }
        }

        for (final Object info : treeMenuController.nodesToInfos(getNode().children())) {
            final ServiceInfo serviceInfo = (ServiceInfo) info;
            for (final ServiceInfo subService : serviceInfo.getSubServices()) {
                if (!groupServiceIsPresent.contains(subService) && !subService.getService().isNew()) {
                    /* remove the group service from the menu
                       that does not exist anymore. */
                    subService.removeInfo();
                }
            }
        }
        hg.setServiceIsPresentList(serviceIsPresent);
        /** Set placeholders to "new", if they have no connections. */
        application.invokeInEdt(new Runnable() {
            @Override
            public void run() {
                hg.killRemovedEdges();
                final Map<String, ServiceInfo> idToInfoHash =
                                                   getBrowser().getNameToServiceInfoHash(ConstraintPHInfo.NAME);
                if (idToInfoHash != null) {
                    for (final Map.Entry<String, ServiceInfo> serviceEntry : idToInfoHash.entrySet()) {
                        final ConstraintPHInfo cphi = (ConstraintPHInfo) serviceEntry.getValue();
                        if (!cphi.getService().isNew() && cphi.isEmpty()) {
                            cphi.getService().setNew(true);
                        }
                    }
                }
                hg.killRemovedVertices();
                hg.scale();
            }
        });
    }

    /** Clears the info panel cache, forcing it to reload. */
    @Override
    public boolean selectAutomaticallyInTreeMenu() {
        return infoPanel == null;
    }

    /** Returns type of the info text. text/plain or text/html. */
    @Override
    protected String getInfoMimeType() {
        return GUIData.MIME_TYPE_TEXT_HTML;
    }

    /**
     * Returns info for info panel, that hb status failed or null, in which
     * case the getInfoPanel() function will show.
     */
    @Override
    public String getInfo() {
        if (getBrowser().crmStatusFailed()) {
            return Tools.getString("ClusterBrowser.ClStatusFailed");
        }
        return null;
    }

    /** Creates rsc_defaults panel. */
    private void addRscDefaultsPanel(final JPanel optionsPanel, final int leftWidth, final int rightWidth) {
        final RscDefaultsInfo rdi = getBrowser().getRscDefaultsInfo();
        rdi.widgetClear();
        final String[] params = rdi.getParametersFromXML();
        rdi.addParams(optionsPanel, params, leftWidth, rightWidth, null);
    }

    /** Returns editable info panel for global crm config. */
    @Override
    public JComponent getInfoPanel() {
        /* if don't have hb status we don't have all the info we need here.
         * TODO: OR we need to get hb status only once
         */
        if (getBrowser().crmStatusFailed()) {
            return super.getInfoPanel();
        }
        final CrmGraph hg = getBrowser().getCrmGraph();
        if (infoPanel != null) {
            hg.pickBackground();
            return infoPanel;
        }
        final JPanel newPanel = new JPanel();
        newPanel.setBackground(ClusterBrowser.PANEL_BACKGROUND);
        newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.PAGE_AXIS));
        if (getBrowser().getCrmXml() == null || getBrowser().getClusterStatus() == null) {
            return newPanel;
        }
        final ButtonCallback buttonCallback = new ButtonCallback() {
            private volatile boolean mouseStillOver = false;

            /**
             * Whether the whole thing should be enabled.
             */
            @Override
            public boolean isEnabled() {
                final Host dcHost = getBrowser().getDCHost();
                return dcHost != null && !Tools.versionBeforePacemaker(dcHost);
            }

            @Override
            public void mouseOut(final ComponentWithTest component) {
                if (!isEnabled()) {
                    return;
                }
                mouseStillOver = false;
                hg.stopTestAnimation((JComponent) component);
                component.setToolTipText("");
            }

            @Override
            public void mouseOver(final ComponentWithTest component) {
                if (!isEnabled()) {
                    return;
                }
                mouseStillOver = true;
                component.setToolTipText(ClusterBrowser.STARTING_PTEST_TOOLTIP);
                component.setToolTipBackground(Tools.getDefaultColor("ClusterBrowser.Test.Tooltip.Background"));
                Tools.sleep(250);
                if (!mouseStillOver) {
                    return;
                }
                mouseStillOver = false;
                final CountDownLatch startTestLatch = new CountDownLatch(1);
                hg.startTestAnimation((JComponent) component, startTestLatch);
                final Host dcHost = getBrowser().getDCHost();
                getBrowser().ptestLockAcquire();
                try {
                    final ClusterStatus clStatus = getBrowser().getClusterStatus();
                    clStatus.setPtestResult(null);
                    apply(dcHost, Application.RunMode.TEST);
                    final PtestData ptestData = new PtestData(CRM.getPtest(dcHost));
                    component.setToolTipText(ptestData.getToolTip());
                    clStatus.setPtestResult(ptestData);
                } finally {
                    getBrowser().ptestLockRelease();
                }
                startTestLatch.countDown();
            }
        };
        initCommitButton(buttonCallback);
        getBrowser().getRscDefaultsInfo().setApplyButton(getApplyButton());
        getBrowser().getRscDefaultsInfo().setRevertButton(getRevertButton());
        final JPanel mainPanel = new JPanel();
        mainPanel.setBackground(ClusterBrowser.PANEL_BACKGROUND);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        final JPanel optionsPanel = new JPanel();
        optionsPanel.setBackground(ClusterBrowser.PANEL_BACKGROUND);
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));
        optionsPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        final JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(ClusterBrowser.BUTTON_PANEL_BACKGROUND);
        buttonPanel.setMinimumSize(new Dimension(0, 50));
        buttonPanel.setPreferredSize(new Dimension(0, 50));
        buttonPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
        buttonPanel.add(getActionsButton(), BorderLayout.LINE_END);

        newPanel.add(buttonPanel);

        final String[] params = getParametersFromXML();
        addParams(optionsPanel,
                  params,
                  application.getDefaultSize("ClusterBrowser.DrbdResLabelWidth"),
                  application.getDefaultSize("ClusterBrowser.DrbdResFieldWidth"),
                  null);

        addRscDefaultsPanel(optionsPanel,
                            application.getDefaultSize("ClusterBrowser.DrbdResLabelWidth"),
                            application.getDefaultSize("ClusterBrowser.DrbdResFieldWidth"));
        getApplyButton().addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    LOG.debug1("actionPerformed: BUTTON: apply");
                    final Thread thread = new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                getBrowser().clStatusLock();
                                apply(getBrowser().getDCHost(), Application.RunMode.LIVE);
                                getBrowser().clStatusUnlock();
                            }
                        }
                    );
                    thread.start();
                }
            }
        );
        getRevertButton().addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    LOG.debug1("actionPerformed: BUTTON: revert");
                    final Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getBrowser().clStatusLock();
                            revert();
                            getBrowser().clStatusUnlock();
                        }
                    });
                    thread.start();
                }
            }
        );

        /* apply button */
        addApplyButton(buttonPanel);
        addRevertButton(buttonPanel);
        application.invokeLater(new Runnable() {
            @Override
            public void run() {
                setApplyButtons(null, params);
            }
        });

        mainPanel.add(optionsPanel);

        newPanel.add(getMoreOptionsPanel(application.getServiceLabelWidth() + application.getServiceFieldWidth() + 4));
        newPanel.add(new JScrollPane(mainPanel));

        hg.pickBackground();
        infoPanel = newPanel;
        infoPanelDone();
        return infoPanel;
    }

    /** Returns heartbeat graph. */
    @Override
    public JPanel getGraphicalView() {
        return getBrowser().getCrmGraph().getGraphPanel();
    }

    /**
     * Adds service to the list of services.
     * TODO: are they both used?
     */
    public ServiceInfo addServicePanel(final ResourceAgent newResourceAgent,
                                       final Point2D pos,
                                       final boolean reloadNode,
                                       final String heartbeatId,
                                       final CloneInfo newCi,
                                       final Application.RunMode runMode) {
        final ServiceInfo newServiceInfo = crmServiceFactory.createFromResourceAgent(
                newResourceAgent,
                getBrowser().getClusterStatus().isMaster(heartbeatId),
                getBrowser());
        if (heartbeatId != null) {
            newServiceInfo.getService().setCrmId(heartbeatId);
            getBrowser().addToHeartbeatIdList(newServiceInfo);
        }
        if (newCi == null) {
            addServicePanel(newServiceInfo, pos, reloadNode, true, runMode);
        }
        return newServiceInfo;
    }

    /**
     * Adds new service to the specified position. If position is null, it
     * will be computed later. reloadNode specifies if the node in
     * the menu should be reloaded and get uptodate.
     */
    void addServicePanel(final ServiceInfo newServiceInfo,
                         final Point2D pos,
                         final boolean reloadNode,
                         final boolean interactive,
                         final Application.RunMode runMode) {
        application.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                newServiceInfo.getService().setResourceClass(newServiceInfo.getResourceAgent().getResourceClass());
                final CrmGraph hg = getBrowser().getCrmGraph();
                getBrowser().addNameToServiceInfoHash(newServiceInfo);
                if (!hg.addResource(newServiceInfo,
                                    null,
                                    pos,
                                    false, /* colocation only */
                                    false, /* order only */
                                    runMode)) {
                    final DefaultMutableTreeNode newServiceNode = treeMenuController.createMenuItem(
                            getBrowser().getServicesNode(),
                            newServiceInfo);
                    if (interactive) {
                        if (newServiceInfo.getResourceAgent().isProbablyMasterSlave()) {
                            /* only if it was added manually. */
                            newServiceInfo.changeType(ServiceInfo.MASTER_SLAVE_TYPE_STRING);
                        } else if (newServiceInfo.getResourceAgent().isProbablyClone()) {
                            newServiceInfo.changeType(ServiceInfo.CLONE_TYPE_STRING);
                        }
                    }
                    if (reloadNode) {
                        /* show it */
                        treeMenuController.reloadNode(getBrowser().getServicesNode(), false);
                        treeMenuController.reloadNode(newServiceNode, true);
                    }
                    getBrowser().reloadAllComboBoxes(newServiceInfo);
                    hg.scale();
                }
                hg.reloadServiceMenus();
            }
        });
    }

    /** Returns 'add service' list for graph popup menu. */
    List<ResourceAgent> getAddServiceList(final String cl) {
        return getBrowser().globalGetAddServiceList(cl);
    }

    /**
     * Returns background popup. Click on background represents cluster as
     * whole.
     */
    @Override
    public List<UpdatableItem> createPopup() {
        return servicesMenu.getPulldownMenu(this);
    }
    /**
     * Returns whether all the parameters are correct. If param is null,
     * all parameters will be checked, otherwise only the param, but other
     * parameters will be checked only in the cache. This is good if only
     * one value is changed and we don't want to check everything.
     */
    @Override
    public Check checkResourceFields(final String param, final String[] params) {
        final RscDefaultsInfo rdi = getBrowser().getRscDefaultsInfo();
        final Check check = new Check(new ArrayList<String>(), new ArrayList<String>());
        check.addCheck(rdi.checkResourceFields(param, rdi.getParametersFromXML(), true));
        check.addCheck(super.checkResourceFields(param, params));
        for (final ServiceInfo si : getBrowser().getExistingServiceList(null)) {
            check.addCheck(si.checkResourceFields(null, si.getParametersFromXML(), true, false, false));
        }
        return check;
    }

    /** Revert all values. */
    @Override
    public void revert() {
        super.revert();
        final RscDefaultsInfo rdi = getBrowser().getRscDefaultsInfo();
        rdi.revert();
        for (final ServiceInfo si : getBrowser().getExistingServiceList(null)) {
            if (si.checkResourceFields(null, si.getParametersFromXML(), true, false, false).isChanged()) {
                si.revert();
            }
        }
        //TODO: should remove new resources and constraints
    }

    /**
     * Copy/paste field from one field to another.
     */
    private void copyPasteField(final Widget oldWi, final Widget newWi) {
        if (newWi == null || oldWi == null) {
            return;
        }
        final Value oldValue = oldWi.getValue();
        application.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (oldValue == null || oldValue.isNothingSelected()) {
                    newWi.setValueNoListeners(null);
                } else {
                    newWi.setValueNoListeners(oldValue);
                }
            }
        });
    }

    private void copyPasteFields(final ServiceInfo oldSi, final ServiceInfo newSi) {
        /* parameters */
        for (final String param : oldSi.getParametersFromXML()) {
            if (ServiceInfo.GUI_ID.equals(param) || ServiceInfo.PCMK_ID.equals(param)) {
                if (getBrowser().isCrmId(oldSi.getService().getCrmId())) {
                    continue;
                }
            }
            copyPasteField(oldSi.getWidget(param, null), newSi.getWidget(param, null));
        }

        /* operations */
        copyPasteField(oldSi.getSameAsOperationsWi(), newSi.getSameAsOperationsWi());

        for (final String op : oldSi.getResourceAgent().getOperationNames()) {
            for (final String param : getBrowser().getCrmOperationParams(op)) {
                copyPasteField(oldSi.getOperationsComboBox(op, param), newSi.getOperationsComboBox(op, param));
            }
        }

        /* locations */
        for (final Host host : getBrowser().getClusterHosts()) {
            final HostInfo hi = host.getBrowser().getHostInfo();
            copyPasteField(oldSi.getScoreComboBoxHash().get(hi), newSi.getScoreComboBoxHash().get(hi));
        }
        /* ping */
        copyPasteField(oldSi.getPingComboBox(), newSi.getPingComboBox());
    }

    public void pasteServices(final List<Info> oldInfos) {
        if (oldInfos.isEmpty()) {
            return;
        }
        final String cn = getBrowser().getCluster().getName();
        guiData.startProgressIndicator(cn, "paste");
        final ClusterBrowser otherBrowser = (ClusterBrowser) oldInfos.get(0).getBrowser();
        getBrowser().getClusterViewPanel().setDisabledDuringLoad(true);
        otherBrowser.getClusterViewPanel().setDisabledDuringLoad(true);
        for (Info oldI : oldInfos) {
            CloneInfo oci = null;
            if (oldI instanceof CloneInfo) {
                oci = (CloneInfo) oldI;
                oldI = oci.getContainedService();
            }
            final CloneInfo oldCi = oci;
            if (oldI instanceof ServiceInfo) {
                final ServiceInfo oldSi = (ServiceInfo) oldI;
                final ServiceInfo newSi =
                    addServicePanel(oldSi.getResourceAgent(),
                                    null, /* pos */
                                    true,
                                    null, /* clone id */
                                    null,
                                    Application.RunMode.LIVE);
                application.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!(newSi instanceof CloneInfo)) {
                            oldSi.getInfoPanel();
                            newSi.getInfoPanel();
                            oldSi.waitForInfoPanel();
                            newSi.waitForInfoPanel();
                        }
                        if (oldCi != null) {
                            final Value v = newSi.getTypeRadioGroup().getValue();
                            if (oldCi.getService().isMaster()) {
                                if (!ServiceInfo.MASTER_SLAVE_TYPE_STRING.equals(v)) {
                                    newSi.getTypeRadioGroup().setValue(ServiceInfo.MASTER_SLAVE_TYPE_STRING);
                                }
                            } else {
                                if (!ServiceInfo.CLONE_TYPE_STRING.equals(v)) {
                                    newSi.getTypeRadioGroup().setValue(ServiceInfo.CLONE_TYPE_STRING);
                                }
                            }
                        }
                        copyPasteFields(oldSi, newSi);
                    }
                });

                /* clone parameters */
                final CloneInfo newCi = newSi.getCloneInfo();
                if (newCi != null) {
                    for (final String param : oldCi.getParametersFromXML()) {
                        if (ServiceInfo.GUI_ID.equals(param) || ServiceInfo.PCMK_ID.equals(param)) {
                            if (getBrowser().isCrmId(oldCi.getService().getCrmId())) {
                                continue;
                            }
                        }
                        application.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                copyPasteField(oldCi.getWidget(param, null), newCi.getWidget(param, null));
                            }
                        });
                    }
                }
                if (oldI instanceof GroupInfo) {
                    final GroupInfo oldGi = (GroupInfo) oldI;
                    final GroupInfo newGi = (GroupInfo) newSi;

                    application.invokeInEdt(new Runnable() {
                        @Override
                        public void run() {
                            for (final ServiceInfo oldChild : oldGi.getSubServices()) {
                                oldChild.getInfoPanel();
                                final ServiceInfo newChild =
                                        newGi.addGroupServicePanel(oldChild.getResourceAgent(), false);
                                newChild.getInfoPanel();
                                copyPasteFields(oldChild, newChild);
                            }
                            treeMenuController.reloadNode(newGi.getNode(), false);
                        }
                    });
                }
            }
        }
        guiData.stopProgressIndicator(cn, "paste");
        otherBrowser.getClusterViewPanel().setDisabledDuringLoad(false);
        getBrowser().getClusterViewPanel().setDisabledDuringLoad(false);
    }

    public void exportGraphAsPng() {
        final Optional<String> savePath = dialogs.getFileName("lcmc-pcmk");
        if (savePath.isPresent()) {
            new Thread() {
                public void run() {
                    BufferedImage image = getBrowser().getCrmGraph().createImage();
                    Tools.writeImage(savePath.get(), image, "PNG");
                }
            }.start();
        }
    }
}
