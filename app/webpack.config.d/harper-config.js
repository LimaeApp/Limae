const path = require('path');

const projectRoot = path.resolve(__dirname, '../../../../');

const harperPkgPath = path.resolve(projectRoot, 'harper-binding/pkg');

console.log("Custom Webpack Config: Harper Binding");
console.log("Linking 'harper-binding' to: " + harperPkgPath);

config.resolve.alias = {
    ...config.resolve.alias,
    'harper-binding': harperPkgPath
};

config.experiments = {
    ...config.experiments,
    asyncWebAssembly: true,
    syncWebAssembly: true
};