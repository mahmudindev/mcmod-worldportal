package com.github.mahmudindev.mcmod.worldportal.base;

import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalReturns;

public interface IServerLevel {
    PortalReturns worldportal$getPortalReturns();

    PortalData worldportal$getPortalData();
}
