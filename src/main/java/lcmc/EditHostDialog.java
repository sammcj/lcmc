/*
 * This file is part of DRBD Management Console by LINBIT HA-Solutions GmbH
 * written by Rasto Levrinc.
 *
 * Copyright (C) 2009, LINBIT HA-Solutions GmbH.
 * Copyright (C) 2011-2012, Rastislav Levrinc.
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


package lcmc;

import lcmc.data.Host;
import lcmc.gui.dialog.host.DialogHost;
import lcmc.gui.dialog.host.SSH;
import lcmc.utilities.Logger;
import lcmc.utilities.LoggerFactory;
import lcmc.utilities.Tools;

/**
 * EditHostDialog.
 *
 * Show step by step dialogs that configure a host.
 *
 * @author Rasto Levrinc
 * @version $Id$
 */
public final class EditHostDialog {
    /** Logger. */
    private static final Logger LOG =
                              LoggerFactory.getLogger(EditHostDialog.class);
    /** The host object. */
    private final Host host;

    /** Prepares a new {@code EditHostDialog} object. */
    public EditHostDialog(final Host host) {
        this.host = host;
    }

    /** Shows step by step dialogs that configure a host. */
    public void showDialogs() {
        DialogHost dialog = new SSH(null, host);
        final boolean expanded = Tools.getGUIData().isTerminalPanelExpanded();
        Tools.getGUIData().expandTerminalSplitPane(0);
        while (true) {
            LOG.debug1("showDialogs: dialog: " + dialog.getClass().getName());
            final DialogHost newdialog = (DialogHost) dialog.showDialog();
            if (dialog.isPressedCancelButton()) {
                if (!expanded) {
                    Tools.getGUIData().expandTerminalSplitPane(1);
                }
                if (newdialog == null) {
                    LOG.debug1("showDialogs: dialog: "
                               + dialog.getClass().getName() + " canceled");
                    return;
                }
            } else if (dialog.isPressedFinishButton()) {
                LOG.debug1("showDialogs: dialog: "
                           + dialog.getClass().getName() + " finished");
                break;
            }
            dialog = newdialog;
        }
        if (!expanded) {
            Tools.getGUIData().expandTerminalSplitPane(1);
        }
    }
}
