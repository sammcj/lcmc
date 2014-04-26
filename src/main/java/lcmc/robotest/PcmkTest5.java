/*
 * This file is part of LCMC written by Rasto Levrinc.
 *
 * Copyright (C) 2013, Rastislav Levrinc.
 *
 * The LCMC is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * The LCMC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LCMC; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package lcmc.robotest;

import static lcmc.robotest.RoboTest.CONFIRM_REMOVE;
import static lcmc.robotest.RoboTest.aborted;
import static lcmc.robotest.RoboTest.addConstraint;
import static lcmc.robotest.RoboTest.checkTest;
import static lcmc.robotest.RoboTest.chooseDummy;
import static lcmc.robotest.RoboTest.disableStonith;
import static lcmc.robotest.RoboTest.info;
import static lcmc.robotest.RoboTest.leftClick;
import static lcmc.robotest.RoboTest.moveTo;
import static lcmc.robotest.RoboTest.removeConstraint;
import static lcmc.robotest.RoboTest.removePlaceHolder;
import static lcmc.robotest.RoboTest.removeResource;
import static lcmc.robotest.RoboTest.rightClick;
import static lcmc.robotest.RoboTest.slowFactor;
import static lcmc.robotest.RoboTest.stopEverything;
import lcmc.utilities.Tools;

/**
 * This class is used to test the GUI.
 *
 * @author Rasto Levrinc
 */
final class PcmkTest5 {
    @SuppressWarnings("TooBroadScope")
    static void start(final int count) {
        slowFactor = 0.2f;
        aborted = false;
        
        
        disableStonith();
        /* create 2 dummies */
        checkTest("test5", 1);

        /* placeholders */
        final int ph1X = 380;
        final int ph1Y = 452;
        moveTo(ph1X, ph1Y);
        rightClick();
        moveTo("Placeholder (AND)");
        leftClick();

        final int dummy1X = 235;
        final int dummy1Y = 207;
        chooseDummy(dummy1X, dummy1Y, false, true);
        final int dummy2X = 500;
        final int dummy2Y = 207;
        chooseDummy(dummy2X, dummy2Y, false, true);
        checkTest("test5", 2);

        moveTo(dummy2X, dummy2Y);
        addConstraint(2);
        checkTest("test5", 2);
        moveTo(ph1X, ph1Y);
        addConstraint(1);

        moveTo(ph1X, ph1Y);
        leftClick();
        moveTo(Tools.getString("Browser.ApplyResource"));
        leftClick();
        checkTest("test5", 2.1);

        final int dum1PopX = dummy1X + 80;
        final int dum1PopY = dummy1Y + 60;
        removeConstraint(dum1PopX, dum1PopY);
        checkTest("test5", 2.5);
        /* constraints */
        for (int i = 1; i <= count; i++) {
            moveTo(dummy1X, dummy1Y);
            addConstraint(2);

            checkTest("test5", 3);

            removeConstraint(dum1PopX, dum1PopY);
            checkTest("test5", 2.5);

            moveTo(ph1X, ph1Y);
            addConstraint(1);

            checkTest("test5", 3.5);

            removeConstraint(dum1PopX, dum1PopY);
            checkTest("test5", 2.5);
            info("i: " + i);
        }
        stopEverything();
        checkTest("test5", 3.1);
        removeResource(dummy1X, dummy1Y, CONFIRM_REMOVE);
        removeResource(dummy2X, dummy2Y, CONFIRM_REMOVE);
        removePlaceHolder(ph1X, ph1Y, !CONFIRM_REMOVE);
        checkTest("test5", 1);
    }

    /** Private constructor, cannot be instantiated. */
        private PcmkTest5() {
            /* Cannot be instantiated. */
        }
}
