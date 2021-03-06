/*
 * Copyright (c) 2016-2019 VMware, Inc. All Rights Reserved.
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices
 * and license terms. Your use of these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package com.vmware.mangle.unittest.services.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.plugin.PluginDetails;
import com.vmware.mangle.services.FileStorageService;
import com.vmware.mangle.services.PluginDetailsService;
import com.vmware.mangle.services.PluginService;
import com.vmware.mangle.services.mockdata.CustomFaultMockData;
import com.vmware.mangle.services.repository.PluginDetailsRepository;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Unit Test cases for PluginDetailsService.
 *
 * @author kumargautam
 */
@PrepareForTest({ Files.class })
@PowerMockIgnore("javax.management.*")
public class PluginDetailsServiceTest extends PowerMockTestCase {

    @Mock
    private PluginDetailsRepository pluginDetailsRepository;
    @Mock
    private PluginService pluginService;
    @Mock
    private SpringPluginManager pluginManager;
    @Mock
    private FileStorageService storageService;
    @InjectMocks
    private PluginDetailsService pluginDetailsService;
    private CustomFaultMockData customFaultMockData;
    private String pluginId = "plugin-test";

    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Files.class);
        this.customFaultMockData = new CustomFaultMockData();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginDetailsService#syncPlugins(java.util.List)}.
     *
     * @throws IOException
     */
    @Test
    public void testSyncPluginsListOfPluginWrapper() throws IOException {
        PluginWrapper pluginWrapper = mock(PluginWrapper.class);
        List<PluginWrapper> lPluginWrappers = new ArrayList<>();
        lPluginWrappers.add(pluginWrapper);
        PluginDetails pluginDetails = customFaultMockData.getPluginDetails();
        PluginDetails pluginDetails1 = customFaultMockData.getPluginDetails();
        pluginDetails1.setPluginId(pluginId);
        List<PluginDetails> list = new ArrayList<>();
        list.add(pluginDetails);
        list.add(pluginDetails1);
        when(pluginDetailsRepository.findAll()).thenReturn(list);
        when(pluginWrapper.getPluginId()).thenReturn(pluginId);
        File file = new File(pluginDetails.getPluginPath());
        PowerMockito.mockStatic(Files.class);
        PowerMockito.when(Files.write(any(Path.class), any(byte[].class))).thenReturn(file.toPath());
        when(pluginManager.loadPlugin(any(Path.class))).thenReturn(pluginDetails.getPluginId());
        when(pluginManager.startPlugin(anyString())).thenReturn(PluginState.STARTED);
        when(storageService.getFileStorageLocation()).thenReturn(file.toPath());
        pluginDetailsService.syncPlugins(lPluginWrappers);
        verify(pluginDetailsRepository, times(1)).findAll();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginDetailsService#syncPlugins(java.util.List)}.
     *
     */
    @Test
    public void testSyncPluginsListOfPluginWrapperForUnload() {
        PluginWrapper pluginWrapper = mock(PluginWrapper.class);
        List<PluginWrapper> lPluginWrappers = new ArrayList<>();
        lPluginWrappers.add(pluginWrapper);
        PluginDetails pluginDetails = customFaultMockData.getPluginDetails();
        PluginDetails pluginDetails1 = customFaultMockData.getPluginDetails();
        pluginDetails1.setIsActive(false);
        pluginDetails1.setIsLoaded(false);
        pluginDetails1.setPluginId(pluginId);
        List<PluginDetails> list = new ArrayList<>();
        list.add(pluginDetails);
        list.add(pluginDetails1);
        when(pluginDetailsRepository.findAll()).thenReturn(list);
        when(pluginWrapper.getPluginId()).thenReturn(pluginId);
        when(pluginManager.unloadPlugin(anyString())).thenReturn(true);
        pluginDetailsService.syncPlugins(lPluginWrappers);
        verify(pluginDetailsRepository, times(1)).findAll();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginDetailsService#syncPlugins(java.util.List)}.
     *
     */
    @Test
    public void testSyncPluginsListOfPluginWrapperForDisable() {
        PluginWrapper pluginWrapper = mock(PluginWrapper.class);
        List<PluginWrapper> lPluginWrappers = new ArrayList<>();
        lPluginWrappers.add(pluginWrapper);
        PluginDetails pluginDetails = customFaultMockData.getPluginDetails();
        PluginDetails pluginDetails1 = customFaultMockData.getPluginDetails();
        pluginDetails1.setIsActive(false);
        pluginDetails1.setPluginId(pluginId);
        List<PluginDetails> list = new ArrayList<>();
        list.add(pluginDetails);
        list.add(pluginDetails1);
        when(pluginDetailsRepository.findAll()).thenReturn(list);
        when(pluginWrapper.getPluginId()).thenReturn(pluginId);
        when(pluginManager.disablePlugin(anyString())).thenReturn(true);
        pluginDetailsService.syncPlugins(lPluginWrappers);
        verify(pluginDetailsRepository, times(1)).findAll();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginDetailsService#createPluginDetails(com.vmware.mangle.cassandra.model.plugin.PluginDetails)}.
     */
    @Test
    public void testCreatePluginDetails() {
        PluginDetails pluginDetails = customFaultMockData.getPluginDetails();
        when(pluginDetailsRepository.save(any(PluginDetails.class))).thenReturn(pluginDetails);
        assertEquals(pluginDetailsService.createPluginDetails(pluginDetails), pluginDetails);
        verify(pluginDetailsRepository, times(1)).save(any(PluginDetails.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginDetailsService#createPluginDetails(com.vmware.mangle.cassandra.model.plugin.PluginDetails)}.
     */
    @Test
    public void testCreatePluginDetailsMangleRuntimeException() {
        PluginDetails pluginDetails = customFaultMockData.getPluginDetails();
        when(pluginDetailsRepository.save(any(PluginDetails.class))).thenReturn(pluginDetails);
        try {
            pluginDetailsService.createPluginDetails(null);
        } catch (MangleRuntimeException e) {
            assertEquals(ErrorCode.FIELD_VALUE_EMPTY, e.getErrorCode());
        }
    }

    @Test
    public void testMultiNodeResyncForDisable() {
        PluginDetails pluginDetails = customFaultMockData.getPluginDetails();
        pluginDetails.setIsActive(false);
        Optional<PluginDetails> optional = Optional.of(pluginDetails);
        when(pluginDetailsRepository.findByPluginId(pluginDetails.getPluginId())).thenReturn(optional);

        when(pluginService.disablePlugin(pluginDetails.getPluginId())).thenReturn(true);

        pluginDetailsService.resync(pluginDetails.getPluginId());

        verify(pluginDetailsRepository, times(1)).findByPluginId(pluginDetails.getPluginId());
        verify(pluginService, times(1)).disablePlugin(pluginDetails.getPluginId());
    }

    @Test
    public void testMultiNodeResyncForUnload() {
        PluginDetails pluginDetails = customFaultMockData.getPluginDetails();
        pluginDetails.setIsActive(false);
        pluginDetails.setIsLoaded(false);
        Optional<PluginDetails> optional = Optional.of(pluginDetails);
        when(pluginDetailsRepository.findByPluginId(pluginDetails.getPluginId())).thenReturn(optional);

        when(pluginService.disablePlugin(pluginDetails.getPluginId())).thenReturn(true);

        pluginDetailsService.resync(pluginDetails.getPluginId());

        verify(pluginDetailsRepository, times(1)).findByPluginId(pluginDetails.getPluginId());
        verify(pluginService, times(1)).unloadPlugin(pluginDetails.getPluginId());
    }

    @Test
    public void testMultiNodeResyncForDelete() {
        PluginDetails pluginDetails = customFaultMockData.getPluginDetails();
        Optional<PluginDetails> optional = Optional.empty();
        when(pluginDetailsRepository.findByPluginId(pluginDetails.getPluginId())).thenReturn(optional);

        when(pluginService.disablePlugin(pluginDetails.getPluginId())).thenReturn(true);

        pluginDetailsService.resync(pluginDetails.getPluginId());

        verify(pluginDetailsRepository, times(1)).findByPluginId(pluginDetails.getPluginId());
        verify(pluginService, times(1)).deletePlugin(pluginDetails.getPluginId());
    }
}