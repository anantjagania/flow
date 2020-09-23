/**
 * NOTICE: this is an auto-generated file
 *
 * This file has been generated by the `flow:prepare-frontend` maven goal.
 * This file will be overwritten on every run. Any custom changes should be made to webpack.config.js
 */
const fs = require('fs');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const ScriptExtHtmlWebpackPlugin = require('script-ext-html-webpack-plugin');
const CompressionPlugin = require('compression-webpack-plugin');

const path = require('path');
const glob = require('glob');
const camelCase = require('camelCase');

// the folder of app resources:
//  - flow templates for classic Flow
//  - client code with index.html and index.[ts/js] for CCDM
const frontendFolder = '[to-be-generated-by-flow]';
const fileNameOfTheFlowGeneratedMainEntryPoint = '[to-be-generated-by-flow]';
const mavenOutputFolderForFlowBundledFiles = '[to-be-generated-by-flow]';
const useClientSideIndexFileForBootstrapping = '[to-be-generated-by-flow]';
const clientSideIndexHTML = '[to-be-generated-by-flow]';
const clientSideIndexEntryPoint = '[to-be-generated-by-flow]';
const devmodeGizmoJS = '[to-be-generated-by-flow]';
// public path for resources, must match Flow VAADIN_BUILD
const build = 'build';
// public path for resources, must match the request used in flow to get the /build/stats.json file
const config = 'config';
// folder for outputting vaadin-bundle and other fragments
const buildFolder = `${mavenOutputFolderForFlowBundledFiles}/${build}`;
// folder for outputting stats.json
const confFolder = `${mavenOutputFolderForFlowBundledFiles}/${config}`;
// file which is used by flow to read templates for server `@Id` binding
const statsFile = `${confFolder}/stats.json`;
// Folders in the project which can contain static assets.
// FIXME This is missing some entries
const projectStaticAssetsFolders = [
  path.resolve(__dirname, "src", "main", "resources", "META-INF", "resources"),
  path.resolve(__dirname, "src", "main", "resources", "static"),
];
// Folders in the project which can contain application themes
const themeProjectFolders = projectStaticAssetsFolders.map((folder) =>
  path.resolve(folder, "theme")
);
// Special folder inside a theme for component themes that go inside the component shadow root
const themeComponentsFolder = "components";
// The contents of a global CSS file with this name in a theme is always added to the document. E.g. @font-face must be in this
const themeFileAlwaysAddToDocument = "document.css";
// FIXME "target" can be configured in Maven projects
const flowFrontendFolder = path.resolve(__dirname, "target", "flow-frontend");
const themeJarFolder = path.resolve(flowFrontendFolder, "theme");

// make sure that build folder exists before outputting anything
const mkdirp = require('mkdirp');

const devMode = process.argv.find(v => v.indexOf('webpack-dev-server') >= 0);

!devMode && mkdirp(buildFolder);
mkdirp(confFolder);

let stats;

// Open a connection with the Java dev-mode handler in order to finish
// webpack-dev-mode when it exits or crashes.
const watchDogPrefix = '--watchDogPort=';
let watchDogPort = devMode && process.argv.find(v => v.indexOf(watchDogPrefix) >= 0);
if (watchDogPort) {
  watchDogPort = watchDogPort.substr(watchDogPrefix.length);
  const runWatchDog = () => {
    const client = new require('net').Socket();
    client.setEncoding('utf8');
    client.on('error', function () {
      console.log("Watchdog connection error. Terminating webpack process...");
      client.destroy();
      process.exit(0);
    });
    client.on('close', function () {
      client.destroy();
      runWatchDog();
    });

    client.connect(watchDogPort, 'localhost');
  }
  runWatchDog();
}

// Compute the entries that webpack have to visit
const webPackEntries = {};
if (useClientSideIndexFileForBootstrapping) {
  webPackEntries.bundle = clientSideIndexEntryPoint;
  const dirName = path.dirname(fileNameOfTheFlowGeneratedMainEntryPoint);
  const baseName = path.basename(fileNameOfTheFlowGeneratedMainEntryPoint, '.js');
  if (fs.readdirSync(dirName).filter(fileName => !fileName.startsWith(baseName)).length) {
    // if there are vaadin exported views, add a second entry
    webPackEntries.export = fileNameOfTheFlowGeneratedMainEntryPoint;
  }
} else {
  webPackEntries.bundle = fileNameOfTheFlowGeneratedMainEntryPoint;
}

if (devMode) {
  webPackEntries.devmodeGizmo = devmodeGizmoJS;
}

exports = {
  frontendFolder: `${frontendFolder}`,
  buildFolder: `${buildFolder}`,
  confFolder: `${confFolder}`
};

module.exports = {
  mode: 'production',
  context: frontendFolder,
  entry: webPackEntries,

  output: {
    filename: `${build}/vaadin-[name]-[contenthash].cache.js`,
    path: mavenOutputFolderForFlowBundledFiles,
    publicPath: 'VAADIN/',
  },

  resolve: {
    // Search for import "x/y" inside these folders, used at least for importing an application theme
    modules: [
      "node_modules",
      flowFrontendFolder,
      ...projectStaticAssetsFolders,
    ],
    extensions: ['.ts', '.js'],
    alias: {
      Frontend: frontendFolder
    }
  },

  devServer: {
    // webpack-dev-server serves ./ ,  webpack-generated,  and java webapp
    contentBase: [mavenOutputFolderForFlowBundledFiles, 'src/main/webapp'],
    after: function(app, server) {
      app.get(`/stats.json`, function(req, res) {
        res.json(stats);
      });
      app.get(`/stats.hash`, function(req, res) {
        res.json(stats.hash.toString());
      });
      app.get(`/assetsByChunkName`, function(req, res) {
        res.json(stats.assetsByChunkName);
      });
      app.get(`/stop`, function(req, res) {
        // eslint-disable-next-line no-console
        console.log("Stopped 'webpack-dev-server'");
        process.exit(0);
      });
    }
  },

  module: {
    rules: [
      {
        test: /\.ts$/,
        use: [
          'awesome-typescript-loader'
        ]
      },
      {
        test: /\.css$/i,
        use: ['lit-css-loader', 'extract-loader', 'css-loader']
      }
    ]
  },
  performance: {
    maxEntrypointSize: 2097152, // 2MB
    maxAssetSize: 2097152 // 2MB
  },
  plugins: [
    // Generate compressed bundles when not devMode
    !devMode && new CompressionPlugin(),

    // Generates the stats file for flow `@Id` binding.
    function (compiler) {
      compiler.hooks.afterEmit.tapAsync("FlowIdPlugin", (compilation, done) => {
        let statsJson = compilation.getStats().toJson();
        // Get bundles as accepted keys
        let acceptedKeys = statsJson.assets.filter(asset => asset.chunks.length > 0)
          .map(asset => asset.chunks).reduce((acc, val) => acc.concat(val), []);

        // Collect all modules for the given keys
        const modules = collectModules(statsJson, acceptedKeys);

        // Collect accepted chunks and their modules
        const chunks = collectChunks(statsJson, acceptedKeys);

        let customStats = {
          hash: statsJson.hash,
          assetsByChunkName: statsJson.assetsByChunkName,
          chunks: chunks,
          modules: modules
        };

        if (!devMode) {
          // eslint-disable-next-line no-console
          console.log("         Emitted " + statsFile);
          fs.writeFile(statsFile, JSON.stringify(customStats, null, 1), done);
        } else {
          // eslint-disable-next-line no-console
          console.log("         Serving the 'stats.json' file dynamically.");

          stats = customStats;
          done();
        }
      });
    },

    // Includes JS output bundles into "index.html"
    useClientSideIndexFileForBootstrapping && new HtmlWebpackPlugin({
      template: clientSideIndexHTML,
      inject: 'head',
      chunks: ['bundle', ...(devMode ? ['devmodeGizmo'] : [])]
    }),
    useClientSideIndexFileForBootstrapping && new ScriptExtHtmlWebpackPlugin({
      defaultAttribute: 'defer'
    }),
  ].filter(Boolean)
};

/**
 * Collect chunk data for accepted chunk ids.
 * @param statsJson full stats.json content
 * @param acceptedKeys chunk ids that are accepted
 * @returns slimmed down chunks
 */
function collectChunks(statsJson, acceptedChunks) {
  const chunks = [];
  // only handle chunks if they exist for stats
  if (statsJson.chunks) {
    statsJson.chunks.forEach(function (chunk) {
      // Acc chunk if chunk id is in accepted chunks
      if (acceptedChunks.includes(chunk.id)) {
        const modules = [];
        // Add all modules for chunk as slimmed down modules
        chunk.modules.forEach(function (module) {
          const slimModule = {
            id: module.id,
            name: module.name,
            source: module.source
          };
          if(module.modules) {
            slimModule.modules = collectSubModules(module);
          }
          modules.push(slimModule);
        });
        const slimChunk = {
          id: chunk.id,
          names: chunk.names,
          files: chunk.files,
          hash: chunk.hash,
          modules: modules
        }
        chunks.push(slimChunk);
      }
    });
  }
  return chunks;
}

/**
 * Collect all modules that are for a chunk in  acceptedChunks.
 * @param statsJson full stats.json
 * @param acceptedChunks chunk names that are accepted for modules
 * @returns slimmed down modules
 */
function collectModules(statsJson, acceptedChunks) {
  let modules = [];
  // skip if no modules defined
  if (statsJson.modules) {
    statsJson.modules.forEach(function (module) {
      // Add module if module chunks contain an accepted chunk and the module is generated-flow-imports.js module
      if (module.chunks.filter(key => acceptedChunks.includes(key)).length > 0
        && (module.name.includes("generated-flow-imports.js") || module.name.includes("generated-flow-imports-fallback.js"))) {
        const slimModule = {
          id: module.id,
          name: module.name,
          source: module.source
        };
        if(module.modules) {
          slimModule.modules = collectSubModules(module);
        }
        modules.push(slimModule);
      }
    });
  }
  return modules;
}

/**
 * Collect any modules under a module (aka. submodules);
 *
 * @param module module to get submodules for
 */
function collectSubModules(module) {
  let modules = [];
  module.modules.forEach(function (submodule) {
    if (submodule.source) {
      const slimModule = {
        name: submodule.name,
        source: submodule.source,
      };
      if(submodule.id) {
        slimModule.id = submodule.id;
      }
      modules.push(slimModule);
    }
  });
  return modules;
}

// Generate theme file(s)

const getThemeProperties = (themeFolder, themeName) => {
  const themePropertyFile = path.resolve(themeFolder, "theme.json");
  if (!fs.existsSync(themePropertyFile)) {
    return {};
  }
  return JSON.parse(fs.readFileSync(themePropertyFile));
};

const generateThemeFile = (themeFolder, themeName, themeProperties) => {
  const globalFiles = glob.sync("*.css", {
    cwd: themeFolder,
    nodir: true,
  });
  const componentsFiles = glob.sync("*.css", {
    cwd: path.resolve(themeFolder, themeComponentsFolder),
    nodir: true,
  });

  let themeFile = `
import { css, unsafeCSS, registerStyles } from "@vaadin/vaadin-themable-mixin/register-styles";
`;

  if (themeProperties.parent) {
    themeFile += `import {applyTheme as applyBaseTheme} from 'theme/${themeProperties.parent}/${themeProperties.parent}.js';
`;
  }

  themeFile += `
// target: Document | ShadowRoot
export const injectGlobalCss = (css, target) => {
    // FIXME: not all browsers support constructable stylesheets
  const sheet = new CSSStyleSheet();
  sheet.replaceSync(css);
  target.adoptedStyleSheets = [...target.adoptedStyleSheets, sheet];
};
`;
  const imports = [];
  const globalCssCode = [];
  const componentCssCode = [];
  const parentTheme = themeProperties.parent ? "applyBaseTheme(target);\n" : "";
  globalFiles.forEach((global) => {
    const filename = path.basename(global);
    const variable = camelCase(filename);
    imports.push(`import ${variable} from "!!raw-loader!./${filename}";\n`);
    if (filename == themeFileAlwaysAddToDocument) {
      globalCssCode.push(`injectGlobalCss(${variable}, document);\n`);
    } else {
      globalCssCode.push(`injectGlobalCss(${variable}, target);\n`);
    }
  });

  componentsFiles.forEach((componentCss) => {
    const filename = path.basename(componentCss);
    const tag = filename.replace(".css", "");
    const variable = camelCase(filename);
    imports.push(
      `import ${variable} from "!!raw-loader!./${themeComponentsFolder}/${filename}";\n`
    );
    componentCssCode.push(`registerStyles(
  "${tag}",
  css\`
    \${unsafeCSS(${variable})}
  \`
);
`);
  });

  const themeIdentifier = "_vaadinds_" + themeName + "_";
  const globalCssFlag = themeIdentifier + "globalCss";
  const componentCssFlag = themeIdentifier + "componentCss";

  themeFile += imports.join("");
  themeFile += `export const applyTheme = (target) => {
    ${parentTheme}
    if (!target['${globalCssFlag}']) {
      ${globalCssCode.join("")}
      target['${globalCssFlag}'] = true;
    }
    if (!document['${componentCssFlag}']) {
      ${componentCssCode.join("")}
      document['${componentCssFlag}'] = true;
     }
}
`;

  return themeFile;
};
const handleThemes = (themesFolder) => {
  const dir = fs.opendirSync(themesFolder);
  while ((dirent = dir.readSync())) {
    if (!dirent.isDirectory()) {
      continue;
    }
    const themeName = dirent.name;
    const themeFolder = path.resolve(themesFolder, themeName);
    const themeProperties = getThemeProperties(themeFolder, themeName);
    const themeFile = generateThemeFile(
      themeFolder,
      themeName,
      themeProperties
    );
    fs.writeFileSync(path.resolve(themeFolder, themeName + ".js"), themeFile);
  }
};

function generateThemeFiles() {
  if (fs.existsSync(themeJarFolder)) {
    handleThemes(themeJarFolder);
  } else {
    console.warn("Theme JAR folder not found", themeJarFolder);
  }

  themeProjectFolders.forEach((themeProjectFolder) => {
    if (fs.existsSync(themeProjectFolder)) {
      handleThemes(themeProjectFolder);
    }
  });
}

// FIXME Verify if this should be triggered here or at some other point in time
generateThemeFiles();
