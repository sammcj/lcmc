/*
 * This file is part of LCMC written by Rasto Levrinc.
 *
 * Copyright (C) 2014, Rastislav Levrinc.
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

package lcmc.event;

import lcmc.drbd.domain.NetInterface;
import lcmc.host.domain.Host;

import java.util.List;

public class HwNetInterfacesChangedEvent {
    private final Host host;
    private final List<NetInterface> netInterfaces;

    public HwNetInterfacesChangedEvent(final Host host, final List<NetInterface> netInterfaces) {
        this.host = host;
        this.netInterfaces = netInterfaces;
    }

    public Host getHost() {
        return host;
    }

    public List<NetInterface> getNetInterfaces() {
        return netInterfaces;
    }
}
