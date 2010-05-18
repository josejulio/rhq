/*
 * RHQ Management Platform
 * Copyright (C) 2010 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.rhq.bundle.ant.task;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.rhq.bundle.ant.DeployPropertyNames;
import org.rhq.bundle.ant.DeploymentPhase;
import org.rhq.bundle.ant.type.DeploymentType;
import org.rhq.bundle.ant.type.InputPropertyType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The rhq:bundle task defines the metadata needed to deploy, redeploy, or undeploy an RHQ bundle.
 *
 * @author Ian Springer
 */
public class BundleTask extends AbstractBundleTask {        
    private String name;
    private String version;
    private String description;
    private Map<String, DeploymentType> deployments = new HashMap<String, DeploymentType>();
    private Set<InputPropertyType> inputProperties = new HashSet<InputPropertyType>();
    
    @Override
    public void maybeConfigure() throws BuildException {
        // The below call will init the attribute fields.
        super.maybeConfigure();

        validateAttributes();
        //validateTypes();
        
        getProject().setBundleName(this.name);
        getProject().setBundleVersion(this.version);
        getProject().setBundleDescription(this.description);
    }

    /**
     * The RHQ Ant launcher will ensure that the following Ant project properties are defined prior to this method being
     * invoked:
     *
     *   rhq.deploy.name  - the {@link org.rhq.core.domain.bundle.BundleDeployment deployment} name
     *                      (e.g. "appserver")
     *   rhq.deploy.dir   - the {@link org.rhq.core.domain.bundle.BundleDeployment deployment} install dir
     *                      (e.g. "/opt/jbossas-petstore")
     *   rhq.deploy.phase - the {@link org.rhq.bundle.ant.DeploymentPhase deployment phase}
     *
     * If the bundle recipe is being executed from the command line, the user must supply these properties, along
     * with any input properties required by the bundle recipe.
     *
     * @throws BuildException
     */
    @Override
    public void execute() throws BuildException {        
        Hashtable projectProps = getProject().getProperties();

        // Make sure the requires System properties are defined and valid.
        String deployDir = (String) projectProps.get(DeployPropertyNames.DEPLOY_DIR);
        if (deployDir == null) {
            throw new BuildException("Required property [" + DeployPropertyNames.DEPLOY_DIR + "] was not specified.");
        }
        File deployDirFile = new File(deployDir);
        if (!deployDirFile.isAbsolute()) {
            throw new BuildException("Value of property [" + DeployPropertyNames.DEPLOY_DIR + "] (" + deployDirFile
                + ") is not an absolute path.");
        }
        getProject().setDeployDir(deployDirFile);
        log(DeployPropertyNames.DEPLOY_DIR + "=\"" + deployDir + "\"", Project.MSG_DEBUG);

        String deploymentIdStr = (String) projectProps.get(DeployPropertyNames.DEPLOY_ID);
        if (deploymentIdStr == null) {
            throw new BuildException("Required property [" + DeployPropertyNames.DEPLOY_ID + "] was not specified.");
        }
        int deploymentId;
        try {
            deploymentId = Integer.parseInt(deploymentIdStr);
        } catch (Exception e) {
            throw new BuildException("Value of property [" + DeployPropertyNames.DEPLOY_ID + "] (" + deploymentIdStr
                + ") is not valid.", e);
        }
        getProject().setDeploymentId(deploymentId);
        log(DeployPropertyNames.DEPLOY_ID + "=\"" + deploymentId + "\"", Project.MSG_DEBUG);

        String deploymentName = (String) projectProps.get(DeployPropertyNames.DEPLOY_NAME);
        if (deploymentName == null) {
            throw new BuildException("Required property [" + DeployPropertyNames.DEPLOY_NAME + "] was not specified.");
        }
        getProject().setDeploymentName(deploymentName);
        DeploymentType deployment = this.deployments.get(deploymentName);
        if (deployment == null) {
            throw new BuildException("There is no deployment element defined with name '" + deploymentName + "'.");
        }

        String deploymentPhaseName = (String) projectProps.get(DeployPropertyNames.DEPLOY_PHASE);
        if (deploymentPhaseName == null) {
            throw new BuildException("Required property [" + DeployPropertyNames.DEPLOY_PHASE + "] was not specified.");
        }
        DeploymentPhase deploymentPhase;
        try {
            deploymentPhase = DeploymentPhase.valueOf(deploymentPhaseName.toUpperCase());
        } catch (IllegalArgumentException e) {
            DeploymentPhase[] phases = DeploymentPhase.values();
            List<String> validPhaseNames = new ArrayList<String>(phases.length);
            for (DeploymentPhase phase : phases) {
                validPhaseNames.add(phase.name().toLowerCase());
            }
            throw new BuildException("Value of property '" + DeployPropertyNames.DEPLOY_PHASE
                    + "' (" + deploymentPhaseName + ") is not a valid deployment phase - the valid phases are "
                    + validPhaseNames + ".");
        }
        getProject().setDeploymentPhase(deploymentPhase);

        // Initialize the deployment configuration.
        for (InputPropertyType inputProperty : this.inputProperties) {
            inputProperty.execute();
        }
        log("Executing '" + deploymentPhase + "' phase for deployment '" + deploymentName + "' from bundle '"
                + this.name + "' version " + this.version + " using config " + getProject().getConfiguration() + "...");
        switch (deploymentPhase) {
            case INSTALL:
                deployment.install();

                break;
            case START:
                deployment.start();
                break;
            case STOP:
                deployment.stop();
                break;
            case UPGRADE:
                deployment.upgrade();
                break;
            case UNINSTALL:
                deployment.uninstall();
                break;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void addConfigured(InputPropertyType inputProperty) {
        this.inputProperties.add(inputProperty);
        inputProperty.init();
    }

    public void addConfigured(DeploymentType deployment) {
        this.deployments.put(deployment.getName(), deployment);
    }

    public Map<String, DeploymentType> getDeployments() {
        return deployments;
    }

    /**
     * Ensure we have a consistent and legal set of attributes, and set
     * any internal flags necessary based on different combinations
     * of attributes.
     *
     * @throws BuildException if an error occurs
     */
    protected void validateAttributes() throws BuildException {
        if (this.name == null) {
            throw new BuildException("The 'name' attribute is required.");
        }
        if (this.name.length() == 0) {
            throw new BuildException("The 'name' attribute must have a non-empty value.");
        }
        if (this.version == null) {
            throw new BuildException("The 'version' attribute is required.");
        }
        if (this.version.length() == 0) {
            throw new BuildException("The 'version' attribute must have a non-empty value.");
        }
    }

    /**
     * Ensure we have a legal set of types.
     *
     * @throws BuildException if an error occurs
     */
    protected void validateTypes() throws BuildException {
        if (this.deployments.isEmpty()) {
            throw new BuildException("At least one 'deployment' child element must be specified.");
        }
    }
}