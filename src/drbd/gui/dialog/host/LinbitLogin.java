/*
 * This file is part of DRBD Management Console by LINBIT HA-Solutions GmbH
 * written by Rasto Levrinc.
 *
 * Copyright (C) 2009, LINBIT HA-Solutions GmbH.
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
package drbd.gui.dialog.host;

import drbd.data.Host;
import drbd.data.ConfigData;
import drbd.utilities.Tools;
import drbd.gui.SpringUtilities;
import drbd.gui.GuiComboBox;
import drbd.gui.dialog.WizardDialog;

import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**.
 * An implementation of a dialog where user can enter the name and password
 * for the linbit website.
 *
 * @author Rasto Levrinc
 * @version $Id$
 *
 */
public class LinbitLogin extends DialogHost {
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Field with user name. */
    private GuiComboBox downloadUserField;
    /** Field with password. */
    private GuiComboBox downloadPasswordField;
    /** Checkbox to save the info. */
    private JCheckBox saveCheckBox;
    /** Width of the check boxes. */
    private static final int CHECKBOX_WIDTH = 120;

    /**
     * Prepares a new <code>LinbitLogin</code> object.
     */
    public LinbitLogin(final WizardDialog previousDialog,
                       final Host host) {
        super(previousDialog, host);
    }

    /**
     * Finishes the dialog and sets the information.
     */
    protected final void finishDialog() {
        Tools.getConfigData().setDownloadLogin(
                                downloadUserField.getStringValue().trim(),
                                downloadPasswordField.getStringValue().trim(),
                                saveCheckBox.isSelected());
    }

    /**
     * Returns the next dialog.
     */
    public WizardDialog nextDialog() {
        return new DrbdLinbitInst(this, getHost());
    }

    /**
     * Check all fields if they are correct.
     * TODO: two checkfields?
     */
    protected final void checkFields() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                boolean v =
                    (downloadUserField.getStringValue().trim().length() > 0);
                v = v & (downloadPasswordField.getStringValue().trim().length()
                         > 0);
                buttonClass(nextButton()).setEnabled(v);
            }
        });
    }

    /**
     * Check all fields if they are correct.
     */
    protected final void checkFields(final GuiComboBox field) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                boolean v =
                    (downloadUserField.getStringValue().trim().length() > 0);
                v = v & (downloadPasswordField.getStringValue().trim().length()
                         > 0);
                buttonClass(nextButton()).setEnabled(v);
            }
        });
    }

    /**
     * Returns the title of the dialog, defined as
     * Dialog.Host.LinbitLogin.Title in TextResources.
     */
    protected final String getHostDialogTitle() {
        return Tools.getString("Dialog.Host.LinbitLogin.Title");
    }

    /**
     * Returns the description of the dialog, defined as
     * Dialog.Host.LinbitLogin.Description in TextResources.
     */
    protected final String getDescription() {
        return Tools.getString("Dialog.Host.LinbitLogin.Description");
    }

    /**
     * Inits the dialog.
     */
    protected final void initDialog() {
        super.initDialog();
        enableComponentsLater(new JComponent[]{buttonClass(nextButton())});
        enableComponents();
        checkFields();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                downloadUserField.requestFocus();
            }
        });
        if (Tools.getConfigData().getAutoOptionHost("drbdinst") != null) {
            Tools.sleep(1000);
            pressNextButton();
        }
    }

    /**
     * Returns the input pane, where user can enter the user name, password and
     * can select a check box to save the info for later.
     */
    protected final JComponent getInputPane() {
        final JPanel inputPane = new JPanel(new SpringLayout());

        /* user */
        final JLabel userLabel = new JLabel(
                      Tools.getString("Dialog.Host.LinbitLogin.EnterUser"));
        inputPane.add(userLabel);
        downloadUserField = new GuiComboBox(
                                        Tools.getConfigData().getDownloadUser(),
                                        null, /* items */
                                        null, /* units */
                                        null, /* type */
                                        "^[,\\w.-]+$",
                                        CHECKBOX_WIDTH,
                                        null, /* abbrv */
                                        ConfigData.AccessType.RO);

        addCheckField(downloadUserField);
        userLabel.setLabelFor(downloadUserField);
        inputPane.add(downloadUserField);

        /* password */
        final JLabel passwordLabel = new JLabel(
                  Tools.getString("Dialog.Host.LinbitLogin.EnterPassword"));

        inputPane.add(passwordLabel);
        downloadPasswordField = new GuiComboBox(
                                Tools.getConfigData().getDownloadPassword(),
                                null, /* items */
                                null, /* units */
                                GuiComboBox.Type.PASSWDFIELD,
                                null, /* type */
                                CHECKBOX_WIDTH,
                                null, /* abbrv */
                                ConfigData.AccessType.RO);

        addCheckField(downloadPasswordField);
        passwordLabel.setLabelFor(downloadPasswordField);
        inputPane.add(downloadPasswordField);

        /* save */
        final JLabel saveLabel = new JLabel("");

        inputPane.add(saveLabel);
        saveCheckBox = new JCheckBox(
                            Tools.getString("Dialog.Host.LinbitLogin.Save"),
                            Tools.getConfigData().getLoginSave());
        saveLabel.setLabelFor(saveCheckBox);
        inputPane.add(saveCheckBox);

        SpringUtilities.makeCompactGrid(inputPane, 3, 2,  // rows, cols
                                                   1, 1,  // initX, initY
                                                   1, 1); // xPad, yPad

        return inputPane;
    }

}
