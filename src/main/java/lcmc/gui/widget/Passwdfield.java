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

import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import lcmc.data.AccessMode;
import lcmc.data.StringValue;
import lcmc.data.Value;
import lcmc.utilities.MyButton;
import lcmc.utilities.PatternDocument;

/**
 * An implementation of a field where user can enter new value. The
 * field can be Textfield or combo box, depending if there are values
 * too choose from.
 *
 * @author Rasto Levrinc
 * @version $Id$
 *
 */
public final class Passwdfield extends Textfield {
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Prepares a new {@code Passwdfield} object. */
    public Passwdfield(final Value selectedValue,
                       final String regexp,
                       final int width,
                       final AccessMode enableAccessMode,
                       final MyButton fieldButton) {
        super(selectedValue,
              regexp,
              width,
              NO_ABBRV,
              enableAccessMode,
              fieldButton);
        addComponent(getPasswdField(selectedValue, regexp), width);
    }

    /** Returns new JPasswordField with default value. */
    private JComponent getPasswdField(final Value value,
                                      final String regexp) {

        final String valueS;
        if (value == null) {
            valueS = null;
        } else {
            valueS = value.getValueForConfig();
        }

        final JPasswordField pf;
        if (regexp == null) {
            pf = new JPasswordField(valueS);
        } else {
            pf = new JPasswordField(new PatternDocument(regexp), valueS, 0);
        }
        return pf;
    }

    /** Return value, that user have chosen in the field or typed in. */
    @Override
    protected Value getValueInternal() {
        final Value value =
             new StringValue(new String(((JPasswordField) getInternalComponent()).getPassword()));
        if (value.isNothingSelected()) {
            return null;
        }
        return value;
    }

    /** Sets item/value in the component and waits till it is set. */
    @Override
    protected void setValueAndWait0(final Value item) {
        ((JTextComponent) getInternalComponent()).setText(item.getValueForConfig());
    }

    /** Returns document object of the component. */
    @Override
    public Document getDocument() {
        return ((JTextComponent) getInternalComponent()).getDocument();
    }

    /** Requests focus if applicable. */
    @Override
    public void requestFocus() {
        getInternalComponent().requestFocus();
    }

    /** Selects the whole text in the widget if applicable. */
    @Override
    public void selectAll() {
        ((JTextComponent) getInternalComponent()).selectAll();
    }
}
