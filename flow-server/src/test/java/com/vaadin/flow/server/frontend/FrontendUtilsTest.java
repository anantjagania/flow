/*
 * Copyright 2000-2020 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.server.frontend;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.di.ResourceProvider;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.MockVaadinServletService;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.tests.util.MockDeploymentConfiguration;

import elemental.json.Json;
import elemental.json.JsonException;

import com.vaadin.flow.server.frontend.installer.NodeInstaller;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;
import com.vaadin.flow.server.frontend.scanner.FrontendDependencies;

import elemental.json.JsonObject;
import static com.vaadin.flow.server.Constants.PACKAGE_JSON;
import static com.vaadin.flow.server.Constants.STATISTICS_JSON_DEFAULT;
import static com.vaadin.flow.server.Constants.VAADIN_SERVLET_RESOURCES;
import static com.vaadin.flow.server.InitParameters.SERVLET_PARAMETER_STATISTICS_JSON;
import static com.vaadin.flow.server.frontend.NodeUpdater.DEPENDENCIES;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FrontendUtilsTest {

    private static final String USER_HOME = "user.home";

    private static Class<?> CACHE_KEY;

    static {
        try {
            CACHE_KEY = Class.forName(
                    "com.vaadin.flow.server.frontend.FrontendUtils$Stats");
        } catch (ClassNotFoundException e) {
            Assert.fail("Could not access cache key for stats.json!");
        }
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public final TemporaryFolder tmpDir = new TemporaryFolder();

    @Test
    public void parseValidVersions() {
        FrontendVersion sixPointO = new FrontendVersion(6, 0);

        FrontendVersion requiredVersionTen = new FrontendVersion(10, 0);
        assertFalse(
                FrontendUtils.isVersionAtLeast(sixPointO, requiredVersionTen));
        assertFalse(FrontendUtils.isVersionAtLeast(sixPointO,
                new FrontendVersion(6, 1)));
        assertTrue(FrontendUtils.isVersionAtLeast(new FrontendVersion("10.0.0"),
                requiredVersionTen));
        assertTrue(FrontendUtils.isVersionAtLeast(new FrontendVersion("10.0.2"),
                requiredVersionTen));
        assertTrue(FrontendUtils.isVersionAtLeast(new FrontendVersion("10.2.0"),
                requiredVersionTen));
    }

    @Test
    public void validateLargerThan_passesForNewVersion() {
        FrontendUtils.validateToolVersion("test", new FrontendVersion("10.0.2"),
                new FrontendVersion(10, 0), new FrontendVersion(10, 0));
        FrontendUtils.validateToolVersion("test", new FrontendVersion("10.1.2"),
                new FrontendVersion(10, 0), new FrontendVersion(10, 0));
        FrontendUtils.validateToolVersion("test", new FrontendVersion("11.0.2"),
                new FrontendVersion(10, 0), new FrontendVersion(10, 0));
    }

    @Test
    public void validateLargerThan_passesForSlightlyOldVersion()
            throws UnsupportedEncodingException {
        FrontendUtils.validateToolVersion("test", new FrontendVersion(9, 0, 0),
                new FrontendVersion(10, 0), new FrontendVersion(8, 0));
    }

    @Test
    public void validateLargerThan_throwsForOldVersion() {
        try {
            FrontendUtils.validateToolVersion("test",
                    new FrontendVersion(7, 5, 0), new FrontendVersion(10, 0),
                    new FrontendVersion(8, 0));
            Assert.fail("No exception was thrown for old version");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains(
                    "Your installed 'test' version (7.5.0) is too old. Supported versions are 10.0+"));
        }
    }

    @Test
    public void parseValidToolVersions() throws IOException {
        Assert.assertEquals("10.11.12",
                FrontendUtils.parseVersionString("v10.11.12"));
        Assert.assertEquals("8.0.0",
                FrontendUtils.parseVersionString("v8.0.0"));
        Assert.assertEquals("8.0.0", FrontendUtils.parseVersionString("8.0.0"));
        Assert.assertEquals("6.9.0", FrontendUtils.parseVersionString(
                "Aktive Codepage: 1252\n" + "6.9.0\n" + ""));
    }

    @Test(expected = IOException.class)
    public void parseEmptyToolVersions() throws IOException {
        FrontendUtils.parseVersionString(" \n");
    }

    @Test
    public void assetsByChunkIsCorrectlyParsedFromStats()
            throws IOException, ServiceException {
        VaadinService service = setupStatsAssetMocks("ValidStats.json");

        String statsAssetsByChunkName = FrontendUtils
                .getStatsAssetsByChunkName(service);

        Assert.assertEquals("{" + "\"index\": \"build/index-1111.cache.js\","
                + "\"index.es5\": \"build/index.es5-2222.cache.js\"" + "}",
                statsAssetsByChunkName);
    }

    @Test
    public void formattingError_assetsByChunkIsCorrectlyParsedFromStats()
            throws IOException, ServiceException {
        VaadinService service = setupStatsAssetMocks("MissFormatStats.json");

        String statsAssetsByChunkName = FrontendUtils
                .getStatsAssetsByChunkName(service);

        Assert.assertEquals("{" + "\"index\": \"build/index-1111.cache.js\","
                + "\"index.es5\": \"build/index.es5-2222.cache.js\"" + "}",
                statsAssetsByChunkName);
    }

    @Test
    public void noStatsFile_assetsByChunkReturnsNull()
            throws IOException, ServiceException {
        VaadinService service = getServiceWithResource(null);

        String statsAssetsByChunkName = FrontendUtils
                .getStatsAssetsByChunkName(service);

        Assert.assertNull(statsAssetsByChunkName);
    }

    @Test
    public void faultyStatsFileReturnsNull()
            throws IOException, ServiceException {
        VaadinService service = setupStatsAssetMocks("InvalidStats.json");

        String statsAssetsByChunkName = FrontendUtils
                .getStatsAssetsByChunkName(service);

        Assert.assertNull(statsAssetsByChunkName);
    }

    @Test
    public synchronized void getVaadinHomeDirectory_noVaadinFolder_folderIsCreated()
            throws IOException {
        String originalHome = System.getProperty(USER_HOME);
        File home = tmpDir.newFolder();
        System.setProperty(USER_HOME, home.getPath());
        try {
            File vaadinDir = new File(home, ".vaadin");
            if (vaadinDir.exists()) {
                FileUtils.deleteDirectory(vaadinDir);
            }
            File vaadinHomeDirectory = FrontendUtils.getVaadinHomeDirectory();
            Assert.assertTrue(vaadinHomeDirectory.exists());
            Assert.assertTrue(vaadinHomeDirectory.isDirectory());

            // access it one more time
            vaadinHomeDirectory = FrontendUtils.getVaadinHomeDirectory();
            Assert.assertEquals(".vaadin", vaadinDir.getName());
        } finally {
            System.setProperty(USER_HOME, originalHome);
        }
    }

    @Test(expected = IllegalStateException.class)
    public synchronized void getVaadinHomeDirectory_vaadinFolderIsAFile_throws()
            throws IOException {
        String originalHome = System.getProperty(USER_HOME);
        File home = tmpDir.newFolder();
        System.setProperty(USER_HOME, home.getPath());
        try {
            File vaadinDir = new File(home, ".vaadin");
            if (vaadinDir.exists()) {
                FileUtils.deleteDirectory(vaadinDir);
            }
            vaadinDir.createNewFile();
            FrontendUtils.getVaadinHomeDirectory();
        } finally {
            System.setProperty(USER_HOME, originalHome);
        }
    }

    @Test
    public void commandToString_longCommand_resultIsWrapped() {
        List<String> command = Arrays.asList("./node/node",
                "./node_modules/webpack-dev-server/bin/webpack-dev-server.js",
                "--config", "./webpack.config.js", "--port 57799",
                "--env watchDogPort=57798", "-d", "--inline=false", "--progress",
                "--colors");
        String wrappedCommand = FrontendUtils.commandToString(".", command);
        Assert.assertEquals("\n" + "./node/node \\ \n"
                + "    ./node_modules/webpack-dev-server/bin/webpack-dev-server.js \\ \n"
                + "    --config ./webpack.config.js --port 57799 -d \\ \n"
                + "    --env watchDogPort=57798 --inline=false --progress --colors \n", wrappedCommand);
    }

    @Test
    public void commandToString_commandContainsBaseDir_baseDirIsReplaced() {
        List<String> command = Arrays.asList("./node/node",
                "/somewhere/not/disclosable/node_modules/webpack-dev-server/bin/webpack-dev-server.js");
        String wrappedCommand = FrontendUtils
                .commandToString("/somewhere/not/disclosable", command);
        Assert.assertEquals(
                "\n" + "./node/node \\ \n"
                        + "    ./node_modules/webpack-dev-server/bin/webpack-dev-server.js \n",
                wrappedCommand);
    }

    @Test
    public void getStatsContent_getStatsFromClassPath_delegateToGetApplicationResource()
            throws IOException {
        VaadinService service = Mockito.mock(VaadinService.class);
        VaadinContext context = Mockito.mock(VaadinContext.class);
        Mockito.when(service.getContext()).thenReturn(context);

        ResourceProvider provider = mockResourceProvider(service, context);

        FrontendUtils.getStatsContent(service);

        Mockito.verify(provider).getApplicationResource(context, "foo");
    }

    @Test
    public void getStatsAssetsByChunkName_getStatsFromClassPath_delegateToGetApplicationResource()
            throws IOException {
        VaadinService service = Mockito.mock(VaadinService.class);
        VaadinContext context = Mockito.mock(VaadinContext.class);
        Mockito.when(service.getContext()).thenReturn(context);

        ResourceProvider provider = mockResourceProvider(service, context);

        FrontendUtils.getStatsAssetsByChunkName(service);

        Mockito.verify(provider).getApplicationResource(context, "foo");
    }

    @Test
    public void getStatsContent_getStatsFromClassPath_populatesStatsCache()
            throws IOException, ServiceException {
        VaadinService service = setupStatsAssetMocks("ValidStats.json");

        assertNull("Stats cache should not be present",
                service.getContext().getAttribute(CACHE_KEY));

        // Populates cache
        FrontendUtils.getStatsContent(service);

        assertNotNull("Stats cache should be created",
                service.getContext().getAttribute(CACHE_KEY));
    }

    @Test
    public void getStatsAssetsByChunkName_getStatsFromClassPath_populatesStatsCache()
            throws IOException, ServiceException {
        VaadinService service = setupStatsAssetMocks("ValidStats.json");

        assertNull("Stats cache should not be present",
                service.getContext().getAttribute(CACHE_KEY));

        // Populates cache
        FrontendUtils.getStatsAssetsByChunkName(service);

        assertNotNull("Stats cache should be created",
                service.getContext().getAttribute(CACHE_KEY));
    }

    @Test // #10893
    public void newLineCharacterInJsonStats_readStreamIsJsonParsable()
            throws IOException, ServiceException {
        VaadinService service = setupStatsAssetMocks(
                "specialCharacterValue.json");

        assertNull("Stats cache should not be present",
                service.getContext().getAttribute(CACHE_KEY));

        // Load file from classpath and populate cache
        String statsContent = FrontendUtils.getStatsContent(service);

        try {
            // Json parsing should not throw for loaded stats.
            Json.parse(statsContent);
        } catch (JsonException jsonException) {
            Assert.fail("Json loaded from class path was not parsable json");
        }

        assertNotNull("Stats cache should be created",
                service.getContext().getAttribute(CACHE_KEY));

        // Load cached stats.
        statsContent = FrontendUtils.getStatsContent(service);

        try {
            // Json parsing should not throw for cached stats.
            Json.parse(statsContent);
        } catch (JsonException jsonException) {
            Assert.fail("Json loaded from cache was not parsable json");
        }
    }

    @Test
    public void clearCachedStatsContent_clearsCache()
            throws IOException, ServiceException {
        VaadinService service = setupStatsAssetMocks("ValidStats.json");

        assertNull("Stats cache should not be present",
                service.getContext().getAttribute(CACHE_KEY));
        // Can be invoked without cache - throws no exception
        FrontendUtils.clearCachedStatsContent(service);

        // Populates cache
        FrontendUtils.getStatsContent(service);

        // Clears cache
        FrontendUtils.clearCachedStatsContent(service);

        assertNull("Stats cache should not be present",
                service.getContext().getAttribute(CACHE_KEY));
    }

    @Test
    public void deleteNodeModules_nopIfNotExists() throws IOException {
        File nodeModules = new File(tmpDir.getRoot(), "node_modules");
        FrontendUtils.deleteNodeModules(nodeModules);
    }

    @Test(expected = IOException.class)
    public void deleteNodeModules_throwsIfNotNamedNodeModules()
            throws IOException {
        File myModules = new File(tmpDir.getRoot(), "my_modules");
        myModules.mkdirs();
        FrontendUtils.deleteNodeModules(myModules);
    }

    @Test
    public void deleteNodeModules_canDeleteSymlinksAndNotFollowThem()
            throws IOException {

        // Test fails on Windows due to UAC FileSystemException
        Assume.assumeFalse(FrontendUtils.isWindows());

        File externalDir = new File(tmpDir.getRoot(), "external");
        File externalLicense = new File(externalDir, "LICENSE");

        externalLicense.getParentFile().mkdirs();
        externalLicense.createNewFile();

        File nodeModules = new File(tmpDir.getRoot(), "node_modules");
        File containing = new File(nodeModules, ".pnpm/a/node_modules/dep");
        containing.mkdirs();
        File license = new File(containing, "LICENSE");
        license.createNewFile();

        File linking = new File(nodeModules, ".pnpm/b/node_modules/dep");
        linking.getParentFile().mkdirs();
        Files.createSymbolicLink(linking.toPath(),
                new File("../../a/node_modules/dep").toPath());

        File linkingExternal = new File(nodeModules,
                ".pnpm/b/node_modules/external");
        Files.createSymbolicLink(linkingExternal.toPath(),
                new File("../../../../external").toPath());

        Assert.assertTrue(nodeModules.exists());
        Assert.assertTrue(linking.exists());
        Assert.assertTrue(new File(linking, "LICENSE").exists());
        Assert.assertTrue(new File(linkingExternal, "LICENSE").exists());

        FrontendUtils.deleteNodeModules(nodeModules);

        Assert.assertFalse(nodeModules.exists());
        Assert.assertTrue(externalLicense.exists());
    }

    @Test
    public void symlinkByNpm_deleteDirectory_doesNotDeleteSymlinkFolderFiles()
            throws IOException, ExecutionFailedException {
        File npmFolder = tmpDir.newFolder();

        File generatedPath = new File(npmFolder, "generated");
        generatedPath.mkdir();

        File symbolic = new File(npmFolder, "symbolic");
        symbolic.mkdir();
        File linkFolderFile = new File(symbolic, "symbol.txt");
        linkFolderFile.createNewFile();

        final JsonObject packageJson = Json.createObject();
        packageJson.put(DEPENDENCIES, Json.createObject());

        packageJson.getObject(DEPENDENCIES).put("@symbolic/link",
                "./" + symbolic.getName());

        FileUtils.writeStringToFile(new File(npmFolder, PACKAGE_JSON),
                packageJson.toJson(), StandardCharsets.UTF_8);

        ClassFinder finder = Mockito.mock(ClassFinder.class);

        Logger logger = Mockito.spy(LoggerFactory.getLogger(NodeUpdater.class));

        NodeUpdater nodeUpdater = new NodeUpdater(finder,
                Mockito.mock(FrontendDependencies.class), npmFolder,
                generatedPath) {

            @Override
            public void execute() {
            }

            @Override
            Logger log() {
                return logger;
            }

        };

        new TaskRunNpmInstall(finder, nodeUpdater, false, true, "v16.16.0",
                URI.create(
                        NodeInstaller.DEFAULT_NODEJS_DOWNLOAD_ROOT)).execute();

        FrontendUtils.deleteNodeModules(new File(npmFolder, "node_modules"));

        Assert.assertTrue("Linked folder contents should not be removed.",
                linkFolderFile.exists());
    }


    private ResourceProvider mockResourceProvider(VaadinService service,
            VaadinContext context) {
        DeploymentConfiguration config = Mockito
                .mock(DeploymentConfiguration.class);

        Lookup lookup = Mockito.mock(Lookup.class);
        Mockito.when(context.getAttribute(Lookup.class)).thenReturn(lookup);

        ResourceProvider provider = Mockito.mock(ResourceProvider.class);
        Mockito.when(lookup.lookup(ResourceProvider.class))
                .thenReturn(provider);

        Mockito.when(service.getDeploymentConfiguration()).thenReturn(config);
        Mockito.when(service.getContext()).thenReturn(context);

        Mockito.when(config.isProductionMode()).thenReturn(true);

        Mockito.when(config.getStringProperty(SERVLET_PARAMETER_STATISTICS_JSON,
                VAADIN_SERVLET_RESOURCES + STATISTICS_JSON_DEFAULT))
                .thenReturn("foo");
        return provider;
    }

    private VaadinService setupStatsAssetMocks(String statsFile)
            throws IOException, ServiceException {
        String stats = IOUtils.toString(FrontendUtilsTest.class.getClassLoader()
                .getResourceAsStream(statsFile), StandardCharsets.UTF_8);

        return getServiceWithResource(stats);
    }

    private VaadinService getServiceWithResource(String content)
            throws ServiceException, IOException {
        MockDeploymentConfiguration configuration = new MockDeploymentConfiguration();
        configuration.setProductionMode(true);
        VaadinService service = new MockVaadinServletService(configuration);
        service.init();

        VaadinContext context = service.getContext();
        Lookup lookup = context.getAttribute(Lookup.class);
        ResourceProvider provider = lookup.lookup(ResourceProvider.class);

        if (content != null) {
            File tmpFile = tmpDir.newFile();
            try (FileOutputStream outputStream = new FileOutputStream(
                    tmpFile)) {
                IOUtils.write(content, outputStream, StandardCharsets.UTF_8);
            }
            Mockito.when(provider.getApplicationResource(context,
                    VAADIN_SERVLET_RESOURCES + STATISTICS_JSON_DEFAULT))
                    .thenReturn(tmpFile.toURI().toURL());
        }

        return service;
    }

}
