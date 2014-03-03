package org.dasein.cloud.cloudstack.compute;

import org.dasein.cloud.AbstractCapabilities;
import org.dasein.cloud.Capabilities;
import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.ProviderContext;
import org.dasein.cloud.Requirement;
import org.dasein.cloud.cloudstack.CSServiceProvider;
import org.dasein.cloud.cloudstack.CSVersion;
import org.dasein.cloud.compute.Architecture;
import org.dasein.cloud.compute.ImageClass;
import org.dasein.cloud.compute.Platform;
import org.dasein.cloud.compute.VMScalingCapabilities;
import org.dasein.cloud.compute.VirtualMachineCapabilities;
import org.dasein.cloud.compute.VmState;
import org.dasein.cloud.cloudstack.CSCloud;
import org.dasein.cloud.util.APITrace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

/**
 * Describes the capabilities of Cloudstack with respect to Dasein virtual machine operations.
 * <p>Created by Danielle Mayne: 3/03/14 12:51 PM</p>
 * @author Danielle Mayne
 * @version 2014.03 initial version
 * @since 2014.03
 */

public class VMCapabilities extends AbstractCapabilities<CSCloud> implements VirtualMachineCapabilities {


    public VMCapabilities(@Nonnull CSCloud cloud) { super(cloud); }

    @Override
    public boolean canAlter(@Nonnull VmState fromState) throws CloudException, InternalException {
        return fromState.equals(VmState.STOPPED);
    }

    @Override
    public boolean canClone(@Nonnull VmState fromState) throws CloudException, InternalException {
        return false;
    }

    @Override
    public boolean canPause(@Nonnull VmState fromState) throws CloudException, InternalException {
        return false;
    }

    @Override
    public boolean canReboot(@Nonnull VmState fromState) throws CloudException, InternalException {
        return true;
    }

    @Override
    public boolean canResume(@Nonnull VmState fromState) throws CloudException, InternalException {
        return false;
    }

    @Override
    public boolean canStart(@Nonnull VmState fromState) throws CloudException, InternalException {
        return !fromState.equals(VmState.RUNNING);
    }

    @Override
    public boolean canStop(@Nonnull VmState fromState) throws CloudException, InternalException {
        return !fromState.equals(VmState.STOPPED);
    }

    @Override
    public boolean canSuspend(@Nonnull VmState fromState) throws CloudException, InternalException {
        return false;
    }

    @Override
    public boolean canTerminate(@Nonnull VmState fromState) throws CloudException, InternalException {
        return !fromState.equals(VmState.TERMINATED);
    }

    @Override
    public boolean canUnpause(@Nonnull VmState fromState) throws CloudException, InternalException {
        return false;
    }

    @Override
    public int getMaximumVirtualMachineCount() throws CloudException, InternalException {
        return Capabilities.LIMIT_UNKNOWN;
    }

    @Override
    public int getCostFactor(@Nonnull VmState state) throws CloudException, InternalException {
        return 100;
    }

    @Nonnull
    @Override
    public String getProviderTermForVirtualMachine(@Nonnull Locale locale) throws CloudException, InternalException {
        return "virtual machine";
    }

    @Nullable
    @Override
    public VMScalingCapabilities getVerticalScalingCapabilities() throws CloudException, InternalException {
        return VMScalingCapabilities.getInstance(false,true,Requirement.NONE,Requirement.NONE);
    }

    @Nonnull
    @Override
    public Requirement identifyImageRequirement(@Nonnull ImageClass cls) throws CloudException, InternalException {
        return (cls.equals(ImageClass.MACHINE) ? Requirement.REQUIRED : Requirement.NONE);
    }

    @Nonnull
    @Override
    public Requirement identifyPasswordRequirement(Platform platform) throws CloudException, InternalException {
        return Requirement.NONE;
    }

    @Nonnull
    @Override
    public Requirement identifyRootVolumeRequirement() throws CloudException, InternalException {
        return Requirement.NONE;
    }

    @Nonnull
    @Override
    public Requirement identifyShellKeyRequirement(Platform platform) throws CloudException, InternalException {
        return Requirement.NONE;
    }

    @Nonnull
    @Override
    public Requirement identifyStaticIPRequirement() throws CloudException, InternalException {
        return Requirement.NONE;
    }

    @Nonnull
    @Override
    public Requirement identifyVlanRequirement() throws CloudException, InternalException {
        APITrace.begin(getProvider(), "VM.identifyVlanRequirement");
        try {
            if( getProvider().getServiceProvider().equals(CSServiceProvider.DATAPIPE) ) {
                return Requirement.NONE;
            }
            if( getProvider().getVersion().greaterThan(CSVersion.CS21) ) {
                ProviderContext ctx = getProvider().getContext();

                if( ctx == null ) {
                    throw new CloudException("No context was set for this request");
                }
                String regionId = ctx.getRegionId();

                if( regionId == null ) {
                    throw new CloudException("No region was set for this request");
                }
                return (getProvider().getDataCenterServices().requiresNetwork(regionId) ? Requirement.REQUIRED : Requirement.OPTIONAL);
            }
            return Requirement.OPTIONAL;
        }
        finally {
            APITrace.end();
        }
    }

    @Override
    public boolean isAPITerminationPreventable() throws CloudException, InternalException {
        return false;
    }

    @Override
    public boolean isBasicAnalyticsSupported() throws CloudException, InternalException {
        return false;
    }

    @Override
    public boolean isExtendedAnalyticsSupported() throws CloudException, InternalException {
        return false;
    }

    @Override
    public boolean isUserDataSupported() throws CloudException, InternalException {
        return true;
    }

    private transient Collection<Architecture> architectures;
    @Nonnull
    @Override
    public Iterable<Architecture> listSupportedArchitectures() throws InternalException, CloudException {
        if( architectures == null ) {
            ArrayList<Architecture> a = new ArrayList<Architecture>();

            a.add(Architecture.I32);
            a.add(Architecture.I64);
            architectures = Collections.unmodifiableList(a);
        }
        return architectures;
    }
}