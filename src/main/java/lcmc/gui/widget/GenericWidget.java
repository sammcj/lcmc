/*
 * This file is part of LCMC
 *
 * Copyright (C) 2012, Rastislav Levrinc.
 *
 * LCMC is free software; you can redistribute it and/or
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

package lcmc.gui.widget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import lcmc.data.AccessMode;
import lcmc.data.Application;
import lcmc.data.Value;
import lcmc.gui.SpringUtilities;
import lcmc.utilities.Logger;
import lcmc.utilities.LoggerFactory;
import lcmc.utilities.MyButton;
import lcmc.utilities.Tools;
import lcmc.utilities.WidgetListener;

/**
 * An implementation of a field where user can enter new value. The
 * field can be Textfield or combo box, depending if there are values
 * too choose from.
 *
 * @author Rasto Levrinc
 * @version $Id$
 *
 */
public abstract class GenericWidget<T extends JComponent>
extends JPanel
implements Widget {
    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(GenericWidget.class);
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Component of this widget. */
    private T component;
    /** Whether the field is editable. */
    private boolean editable = false;
    /** Whether the field should be always editable. */
    private boolean alwaysEditable = false;
    /** File chooser button or some other button. */
    private final MyButton fieldButton;
    /** Component part of field with button. */
    private T componentPart = null;
    /** Label of this component. */
    private JLabel label = null;
    /** Whether the component should be enabled. */
    private boolean enablePredicate = true;
    /** Whether the extra text field button should be enabled. */
    private boolean tfButtonEnabled = true;
    /** Access Type for this component to become enabled. */
    private AccessMode enableAccessMode = new AccessMode(
                                                    Application.AccessType.RO,
                                                    false);
    /** Tooltip if element is enabled. */
    private String toolTipText = null;
    /** Tooltip for label if it is enabled. */
    private String labelToolTipText = null;
    /** getValue setValue lock. */
    private final ReadWriteLock mValueLock = new ReentrantReadWriteLock();
    private final Lock mValueReadLock = mValueLock.readLock();
    private final Lock mValueWriteLock = mValueLock.writeLock();
    /** Regexp that this field must match. */
    private final String regexp;
    /** Reason why it is disabled. */
    private String disabledReason = null;
    /** List of widget listeners. */
    private final Collection<WidgetListener> widgetListeners =
                                              new ArrayList<WidgetListener>();
    /** Whether the combobox was never set. */
    private boolean newFlag = true;

    /** Prepares a new {@code GenericWidget} object. */
    public GenericWidget(final String regexp,
                  final AccessMode enableAccessMode) {
        this(regexp,
             enableAccessMode,
             NO_BUTTON); /* without button */
    }

    /** Prepares a new {@code GenericWidget} object. */
    public GenericWidget(final String regexp,
                  final AccessMode enableAccessMode,
                  final MyButton fieldButton) {
        super();
        this.enableAccessMode = enableAccessMode;
        this.fieldButton = fieldButton;
        setLayout(new BorderLayout(0, 0));
        if (regexp != null && regexp.indexOf("@NOTHING_SELECTED@") > -1) {
            this.regexp =
             regexp.replaceAll("@NOTHING_SELECTED@", NOTHING_SELECTED_DISPLAY);
        } else {
            this.regexp = regexp;
        }
    }


    protected final void addComponent(final T newComp, final int width) {
        if (fieldButton == null) {
            component = newComp;
        } else {
            componentPart = newComp;
            component = (T) new JPanel();
            component.setLayout(new SpringLayout());

            component.add(newComp);
            component.add(fieldButton);
            /** add button */
            SpringUtilities.makeCompactGrid(component, 1, 2,
                                                       0, 0,
                                                       0, 0);
        }
        component.setPreferredSize(new Dimension(width, WIDGET_HEIGHT));
        if (componentPart != null) {
            componentPart.setPreferredSize(new Dimension(width, WIDGET_HEIGHT));
        }
        setPreferredSize(new Dimension(width, WIDGET_COMPONENT_HEIGHT));
        if (width != 0) {
            component.setMaximumSize(new Dimension(width, WIDGET_HEIGHT));
            setMaximumSize(new Dimension(width, WIDGET_COMPONENT_HEIGHT));
        }

        add(Box.createRigidArea(new Dimension(0, 1)), BorderLayout.PAGE_START);
        add(component, BorderLayout.CENTER);
        add(Box.createRigidArea(new Dimension(0, 1)), BorderLayout.PAGE_END);
        processAccessMode();
    }

    @Override
    public void reloadComboBox(final Value selectedValue,
                               final Value[] items) {
    }

    /** Sets the tooltip text. */
    @Override
    public void setToolTipText(String text) {
        toolTipText = text;
        final String disabledReason0 = disabledReason;
        if (disabledReason0 != null) {
            text = text + "<br>" + disabledReason0;
        }
        if (enableAccessMode.getAccessType() != Application.AccessType.NEVER) {
            final boolean accessible =
                     Tools.getApplication().isAccessible(enableAccessMode);
            if (!accessible) {
                text = text + "<br>" + getDisabledTooltip();
            }
        }
        component.setToolTipText("<html>" + text + "</html>");
        if (fieldButton != null) {
            componentPart.setToolTipText("<html>" + text + "</html>");
            fieldButton.setToolTipText("<html>" + text + "</html>");
        }
    }

    /** Sets label tooltip text. */
    private void setLabelToolTipText(String text) {
        labelToolTipText = text;
        if (text == null || label == null) {
            return;
        }
        String disabledTooltip = null;
        if (enableAccessMode.getAccessType() != Application.AccessType.NEVER) {
            final boolean accessible =
                     Tools.getApplication().isAccessible(enableAccessMode);
            if (!accessible) {
                disabledTooltip = getDisabledTooltip();
            }
        }
        final String disabledReason0 = disabledReason;
        if (disabledReason0 != null || disabledTooltip != null) {
            final StringBuilder tt = new StringBuilder(40);
            if (disabledReason0 != null) {
                tt.append(disabledReason0);
                tt.append("<br>");
            }
            if (disabledTooltip != null) {
                tt.append(disabledTooltip);
            }
            if (text.length() > 6 && "<html>".equals(text.substring(0, 6))) {
                text = "<html>" + tt.toString() + "<br>" + "<br>"
                       + text.substring(6);
            } else {
                text = Tools.html(text + "<br>" + tt.toString());
            }
        }
        label.setToolTipText(text);
    }

    /** Returns tooltip for disabled element. */
    private String getDisabledTooltip() {
        String advanced = "";
        if (enableAccessMode.isAdvancedMode()) {
            advanced = "Advanced ";
        }
        final StringBuilder sb = new StringBuilder(100);
        sb.append("editable in \"");
        sb.append(advanced);
        sb.append(
                Application.OP_MODES_MAP.get(enableAccessMode.getAccessType()));
        sb.append("\" mode");

        if (disabledReason != null) {
            /* yet another reason */
            sb.append(' ');
            sb.append(disabledReason);
        }

        return sb.toString();
    }

    /** Sets the field editable. */
    @Override
    public final void setEditable() {
        setEditable(editable);
    }

    /** Sets combo box editable. */
    @Override
    public void setEditable(final boolean editable) {
        this.editable = editable;
    }

    /**
     * Returns string value. If object value is null, returns empty string (not
     * null).
     */
    @Override
    public abstract String getStringValue();

    @Override
    public final Value getValue() {
        mValueReadLock.lock();
        try {
            return getValueInternal();
        } finally {
            mValueReadLock.unlock();
        }
    }

    /** Return value, that user have chosen in the field or typed in. */
    abstract Value getValueInternal();

    /** Clears the combo box. */
    @Override
    public void clear() {
    }

    /** Sets component visible or invisible and remembers this state. */
    @Override
    public final void setVisible(final boolean visible) {
        setComponentsVisible(visible);
    }

    /** Sets component visible or invisible. */
    protected void setComponentsVisible(final boolean visible) {
        final JComponent c;
        if (fieldButton == null) {
            c = component;
        } else {
            c = componentPart;
        }
        final JComponent comp = c;
        super.setVisible(visible);
        Tools.invokeLater(!Tools.CHECK_SWING_THREAD, new Runnable() {
            @Override
            public void run() {
                if (label != null) {
                    label.setVisible(visible);
                }
                comp.setVisible(visible);
                if (visible) {
                    setHeight(WIDGET_COMPONENT_HEIGHT);
                } else {
                    setHeight(0);
                }
                repaint();
            }
        });
    }


    /** Sets component enabled or disabled and remembers this state. */
    @Override
    public final void setEnabled(final boolean enabled) {
        enablePredicate = enabled;
        setComponentsEnabled(
                   enablePredicate
                   && Tools.getApplication().isAccessible(enableAccessMode));
    }

    /** Sets extra button enabled. */
    @Override
    public final void setTFButtonEnabled(final boolean tfButtonEnabled) {
        this.tfButtonEnabled = tfButtonEnabled;
        fieldButton.setEnabled(tfButtonEnabled);
    }

    /** Sets component enabled or disabled. */
    protected void setComponentsEnabled(final boolean enabled) {
        component.setEnabled(enabled);
        if (fieldButton != null) {
            componentPart.setEnabled(enabled);
            fieldButton.setEnabled(enabled && tfButtonEnabled);
        }
    }

    /**
     * Enables/Disables component in a group of components identified by
     * specified string. This works only with RADIOGROUP at the moment.
     */
    @Override
    public void setEnabled(final String s, final boolean enabled) {
    }

    /** Returns whether component is editable or not. */
    @Override
    public abstract boolean isEditable();

    /** Sets item/value in the component and waits till it is set. */
    @Override
    public final void setValueAndWait(final Value item) {
        newFlag = false;
        if (Tools.areEqual(item, getValue())) {
            return;
        }
        mValueWriteLock.lock();
        try {
            setValueAndWait0(item);
        } finally {
            mValueWriteLock.unlock();
        }
    }

    /** Sets item/value in the component and waits till it is set. */
    protected abstract void setValueAndWait0(final Value item);

    /** Sets item/value in the component, disable listeners. */
    @Override
    public final void setValueNoListeners(final Value item) {
        newFlag = false;
        if (Tools.areEqual(item, getValue())) {
            return;
        }
        for (final WidgetListener wl : widgetListeners) {
            wl.setEnabled(false);
        }
        mValueWriteLock.lock();
        try {
            setValueAndWait0(item);
        } finally {
            mValueWriteLock.unlock();
        }
        for (final WidgetListener wl : widgetListeners) {
            wl.setEnabled(true);
        }
    }

    /** Sets item/value in the component. */
    @Override
    public final void setValue(final Value item) {
        newFlag = false;
        if (Tools.areEqual(item, getValue())) {
            return;
        }
        Tools.invokeLater(!Tools.CHECK_SWING_THREAD, new Runnable() {
            @Override
            public void run() {
                mValueWriteLock.lock();
                try {
                    setValueAndWait0(item);
                } finally {
                    mValueWriteLock.unlock();
                }
            }
        });
    }

    /** Sets selected index. */
    @Override
    public void setSelectedIndex(final int index) {
    }

    /** Returns document object of the component. */
    @Override
    public abstract Document getDocument();

    /** Selects part after first '*' in the ip. */
    @Override
    public void selectSubnet() {
    }

    protected final void addDocumentListener(final Document doc,
                                             final WidgetListener wl) {
        doc.addDocumentListener(
                new DocumentListener() {
                    private void check(final DocumentEvent e) {
                        if (wl.isEnabled()) {
                            try {
                                final String text =
                                   e.getDocument().getText(0, doc.getLength());

                                final Thread t = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        wl.checkText(text);
                                    }
                                });
                                t.start();
                            } catch (final BadLocationException ble) {
                                LOG.appWarning("check: document listener error");
                            }
                        }
                    }

                    @Override
                    public void insertUpdate(final DocumentEvent e) {
                        check(e);
                    }

                    @Override
                    public void removeUpdate(final DocumentEvent e) {
                        check(e);
                    }

                    @Override
                    public void changedUpdate(final DocumentEvent e) {
                        check(e);
                    }
                }
            );

    }

    protected ItemListener getItemListener(final WidgetListener wl) {
        return new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                if (e.getItem() instanceof String) {
                    /* handled by document listener */
                    return;
                }
                setEditable();
                if (wl.isEnabled()
                    && e.getStateChange() == ItemEvent.SELECTED) {
                    final Value value = (Value) e.getItem();
                    final Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            wl.check(value);
                        }
                    });
                    t.start();
                }
            }
        };
    }

    /** Adds item listener to the component. */
    @Override
    public void addListeners(final WidgetListener widgetListener) {
        widgetListeners.add(widgetListener);
    }

    /**
     * Sets the background for the component which value is incorrect (failed).
     */
    @Override
    public final void wrongValue() {
        setBackgroundColor(ERROR_VALUE_BACKGROUND);
        if (label != null) {
            label.setForeground(Color.RED);
        }
    }

    /** Sets background without considering the label. */
    @Override
    public final void setBackground(final Value defaultValue,
                                    final Value savedValue,
                                    final boolean required) {
        setBackground(null, defaultValue, null, savedValue, required);
    }

    /**
     * Sets background of the component depending if the value is the same
     * as its default value and if it is a required argument.
     * Must be called after combo box was already added to some panel.
     *
     * It also disables, hides the component depending on the access type.
     * TODO: rename the function
     */
    @Override
    public final void setBackground(final String defaultLabel,
                                    final Value defaultValue,
                                    final String savedLabel,
                                    final Value savedValue,
                                    final boolean required) {
        if (getParent() == null) {
            return;
        }
        final JComponent comp;
        if (fieldButton == null) {
            comp = component;
        } else {
            comp = componentPart;
        }
        final Value value = getValue();
        String labelText = null;
        if (savedLabel != null) {
            labelText = label.getText();
        }

        final Color backgroundColor = getParent().getBackground();
        if (!Tools.areEqual(value, savedValue)
            || (savedLabel != null && !Tools.areEqual(labelText, savedLabel))) {
            if (label != null) {
                LOG.debug2("setBackground: changed label: " + labelText + " != " + savedLabel);
                LOG.debug2("setBackground: changed: " + value + " != " + savedValue);
                /*
                   Tools.printStackTrace(labelText + ": changed: "
                                         + value + " != " + savedValue);
                 */
                label.setForeground(CHANGED_VALUE_COLOR);
            }
        } else if (Tools.areEqual(value, defaultValue)
                   && (savedLabel == null
                       || Tools.areEqual(labelText, defaultLabel))) {
            if (label != null) {
                label.setForeground(DEFAULT_VALUE_COLOR);
            }
        } else {
            if (label != null) {
                label.setForeground(SAVED_VALUE_COLOR);
            }
        }
        setBackground(backgroundColor);
        final Color compColor = Color.WHITE;
        setComponentBackground(backgroundColor, compColor);
        processAccessMode();
    }

    protected void setComponentBackground(final Color backgroundColor,
                                          final Color compColor) {
    }

    /** Sets flag that determines whether the combo box is always editable. */
    @Override
    public final void setAlwaysEditable(final boolean alwaysEditable) {
        this.alwaysEditable = alwaysEditable;
        setEditable(alwaysEditable);
    }

    protected final boolean isAlwaysEditable() {
        return alwaysEditable;
    }

    /** Requests focus if applicable. */
    @Override
    public void requestFocus() {
    }

    /** Selects the whole text in the widget if applicable. */
    @Override
    public void selectAll() {
    }

    /** Sets the width of the widget. */
    @Override
    public final void setWidth(final int newWidth) {
        final JComponent c;
        if (fieldButton == null) {
            c = component;
        } else {
            c = componentPart;
        }
        c.setMinimumSize(new Dimension(newWidth,
                                       (int) c.getMinimumSize().getHeight()));
        c.setPreferredSize(
                       new Dimension(newWidth,
                                     (int) c.getPreferredSize().getHeight()));
        c.setMaximumSize(new Dimension(newWidth,
                                       (int) c.getMaximumSize().getHeight()));
        setMinimumSize(new Dimension(newWidth,
                                     (int) getMinimumSize().getHeight()));
        setPreferredSize(new Dimension(newWidth,
                                       (int) getPreferredSize().getHeight()));
        setMaximumSize(new Dimension(newWidth,
                                     (int) getMaximumSize().getHeight()));
        final Container p = getParent();
        if (p != null) {
            p.validate();
            p.repaint();
        }
    }

    /** Sets the height of the widget. */
    public final void setHeight(final int newHeight) {
        final JComponent c;
        if (fieldButton == null) {
            c = component;
        } else {
            c = componentPart;
        }
        c.setMinimumSize(new Dimension((int) c.getMinimumSize().getWidth(),
                                       newHeight));
        c.setPreferredSize(new Dimension((int) c.getPreferredSize().getWidth(),
                                         newHeight));
        c.setMaximumSize(new Dimension((int) c.getMaximumSize().getWidth(),
                                       newHeight));
        setMinimumSize(new Dimension((int) getMinimumSize().getWidth(),
                                     newHeight));
        setPreferredSize(new Dimension((int) getPreferredSize().getWidth(),
                                       newHeight));
        setMaximumSize(new Dimension((int) getMaximumSize().getWidth(),
                                     newHeight));
        if (label != null) {
            label.setMinimumSize(
                        new Dimension((int) label.getMinimumSize().getWidth(),
                                      newHeight));
            label.setPreferredSize(
                        new Dimension((int) label.getPreferredSize().getWidth(),
                                      newHeight));
            label.setMaximumSize(
                        new Dimension((int) label.getMaximumSize().getWidth(),
                                      newHeight));
        }
        final Container p = getParent();
        if (p != null) {
            p.validate();
            p.repaint();
        }
    }

    /** Returns its component. */
    final JComponent getJComponent() {
        return component;
    }

    /** Sets background color. */
    @Override
    public void setBackgroundColor(final Color bg) {
        Tools.invokeLater(new Runnable() {
            @Override
            public void run() {
                setBackground(bg);
            }
        });
    }

    /** Sets label for this component. */
    @Override
    public final void setLabel(final JLabel label,
                               final String labelToolTipText) {
        this.label = label;
        this.labelToolTipText = labelToolTipText;
    }

    /** Returns label for this component. */
    @Override
    public final JLabel getLabel() {
        return label;
    }

    /** Sets this item enabled and visible according to its access type. */
    @Override
    public final void processAccessMode() {
        final boolean accessible =
                       Tools.getApplication().isAccessible(enableAccessMode);
        setComponentsEnabled(enablePredicate && accessible);
        if (toolTipText != null) {
            setToolTipText(toolTipText);
        }
        if (label != null) {
            if (labelToolTipText != null) {
                setLabelToolTipText(labelToolTipText);
            }
            label.setEnabled(enablePredicate && accessible);
        }
    }

    /** Returns item at the specified index. */
    //Value getItemAt(final int i) {
    //    return null;
    //}

    /** Cleanup whatever would cause a leak. */
    @Override
    public void cleanup() {
        widgetListeners.clear();
    }

    /** Returns regexp of this field. */
    @Override
    public final String getRegexp() {
        return regexp;
    }

    /** Sets reason why it is disabled. */
    @Override
    public final void setDisabledReason(final String disabledReason) {
        this.disabledReason = disabledReason;
    }

    /** Returns component. */
    protected final T getInternalComponent() {
        if (fieldButton == null) {
            return component;
        } else {
            return componentPart;
        }
    }

    /** Returns this widget, so that the interface Widget can be used in other
     *  components. */
    @Override
    public final Component getComponent() {
        return this;
    }

    /** Returns widget listeners. */
    protected final Collection<WidgetListener> getWidgetListeners() {
        return widgetListeners;
    }

    /** Return enable predicate. */
    protected final boolean isEnablePredicate() {
        return enablePredicate;
    }

    /** Return access mode at which this component is enabled. */
    protected final AccessMode getEnableAccessMode() {
        return enableAccessMode;
    }

    

    /** Return whether this widget was never set. */
    @Override
    public final boolean isNew() {
        return newFlag;
    }

    /** Select the text component. */
    @Override
    public void select(final int selectionStart, final int selectionEnd) {
        LOG.appWarning("select: not implemented");
    }

    @Override
    public void setText(final String text) {
    }

    /** Workaround for jcombobox so that it works with default button. */
    static class ActivateDefaultButtonListener<E> extends KeyAdapter
                                               implements ActionListener {
        /** Combobox, that should work with default button. */
        private final JComboBox<E> box;

        /** Creates new ActivateDefaultButtonListener. */
        ActivateDefaultButtonListener(final JComboBox<E> box) {
            super();
            this.box = box;
        }

        /** Is called when a key was pressed. */
        @Override
        public void keyPressed(final KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                /* Simulte click on default button. */
                doClick(e);
            }
        }

        /** Is called when an action was performed. */
        @Override
        public void actionPerformed(final ActionEvent e) {
            doClick(e);
        }

        /** Do click. */
        private void doClick(final EventObject e) {
            final Component c = (Component) e.getSource();

            final JRootPane rootPane = SwingUtilities.getRootPane(c);

            if (rootPane != null) {
                final JButton defaultButton = rootPane.getDefaultButton();

                if (defaultButton != null && !box.isPopupVisible()) {
                    final Object selection = box.getEditor().getItem();
                    box.setSelectedItem(selection);
                    defaultButton.doClick();
                }
            }
        }
    }

    /**
     * TextField that selects all when focused.
     */
    public static final class MTextField extends JTextField {
        /** Serial Version UID. */
        private static final long serialVersionUID = 1L;
        /** To select all only once. */
        private volatile boolean selected = false;

        /** Creates a new MTextField object. */
        public MTextField(final String text) {
            super(text);
        }

        /** Creates a new MTextField object. */
        public MTextField(final String text,
                          final int columns) {
            super(text, columns);
        }

        /** Creates a new MTextField object. */
        MTextField(final Document doc,
                   final String text,
                   final int columns) {
            super(doc, text, columns);
        }

        /** Focus event. */
        @Override
        protected void processFocusEvent(final FocusEvent e) {
            super.processFocusEvent(e);
            if (!selected) {
                selected = true;
                if (e.getID() == FocusEvent.FOCUS_GAINED) {
                    selectAll();
                }
            }
        }
    }
}
