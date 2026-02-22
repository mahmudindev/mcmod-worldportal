package com.github.mahmudindev.mcmod.worldportal.base;

import com.github.mahmudindev.mcmod.worldportal.portal.PortalPositions;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalReturns;

public interface IServerLevel {
    PortalReturns worldportal$getPortalReturns();

    PortalPositions worldportal$getPortalPositions();
}
