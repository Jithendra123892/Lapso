import { injectGlobalWebcomponentCss } from 'Frontend/generated/jar-resources/theme-util.js';

import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import '@vaadin/polymer-legacy-adapter/style-modules.js';
import 'Frontend/generated/jar-resources/vaadin-grid-flow-selection-column.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-column.js';
import '@vaadin/text-field/theme/lumo/vaadin-text-field.js';
import '@vaadin/icons/vaadin-iconset.js';
import '@vaadin/vertical-layout/theme/lumo/vaadin-vertical-layout.js';
import '@vaadin/app-layout/theme/lumo/vaadin-app-layout.js';
import '@vaadin/tooltip/theme/lumo/vaadin-tooltip.js';
import '@vaadin/app-layout/theme/lumo/vaadin-drawer-toggle.js';
import '@vaadin/icon/theme/lumo/vaadin-icon.js';
import '@vaadin/context-menu/theme/lumo/vaadin-context-menu.js';
import 'Frontend/generated/jar-resources/contextMenuConnector.js';
import 'Frontend/generated/jar-resources/contextMenuTargetConnector.js';
import '@vaadin/horizontal-layout/theme/lumo/vaadin-horizontal-layout.js';
import '@vaadin/grid/theme/lumo/vaadin-grid.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-sorter.js';
import '@vaadin/checkbox/theme/lumo/vaadin-checkbox.js';
import 'Frontend/generated/jar-resources/gridConnector.ts';
import '@vaadin/button/theme/lumo/vaadin-button.js';
import 'Frontend/generated/jar-resources/disableOnClickFunctions.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-column-group.js';
import 'Frontend/generated/jar-resources/lit-renderer.ts';
import '@vaadin/notification/theme/lumo/vaadin-notification.js';
import '@vaadin/login/theme/lumo/vaadin-login-form.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';
import 'Frontend/generated/jar-resources/ReactRouterOutletElement.tsx';

const loadOnDemand = (key) => {
  const pending = [];
  if (key === '067b641571542daad6fdaa902d693fd53d81c709bdfd19738c97c9253cd9a35d') {
    pending.push(import('./chunks/chunk-066d52826c6c9aeb34ef99d10f2fbeb6081a4545a07ec73d28921f59e24a78f9.js'));
  }
  if (key === '0f0c952ea94603dca8bdf19271eb1458b1f004f8d66191b0f53e00932489fc3f') {
    pending.push(import('./chunks/chunk-433420a67c4206124affcc5df10d5c358f24c23aa2527562c95e6e201d8c083d.js'));
  }
  if (key === '65bf22861a7b440743eb18c84df9095a056572e0523298fbddc1f492ec3caa5e') {
    pending.push(import('./chunks/chunk-a7eaa8fa446e0ee87de66bb556a3f91c356cc810389ab431b8ff5aa2966cf94c.js'));
  }
  if (key === '7df02438a3021b79ebdefafccbb9d500df834d17bf484a2d0d3f3134a53544d1') {
    pending.push(import('./chunks/chunk-ae484fd52fb9398ea461ce130735a163e02918a77be6d4781fb9f1e8b3d62a8c.js'));
  }
  if (key === 'c7a8b0a50586aba3851877cf47644b1bc1ee4bad6fbf08c15f300c21e6c498a8') {
    pending.push(import('./chunks/chunk-50804ba7ee8153ef056cadff5a323fd4cc1555a9b1c07104e58f5730a66a6956.js'));
  }
  if (key === '901fd04c7fa93389e63741eb56e115901749e51ffe2a47e475e3b5de68194591') {
    pending.push(import('./chunks/chunk-a7eaa8fa446e0ee87de66bb556a3f91c356cc810389ab431b8ff5aa2966cf94c.js'));
  }
  if (key === 'e772c7809e7ca08e5331266092db34755cbfcd3ee5588df03d4f2cb6cb3055e6') {
    pending.push(import('./chunks/chunk-72966c58a9fd143968254c992ffb62d7260bc58520ff8d239f4ad2c1008829f5.js'));
  }
  if (key === '63bc87b6658f562fa67412c364ebbfb0a618b19b6f92f6da47563e8944bdf423') {
    pending.push(import('./chunks/chunk-75d24d39bc49d588c9d8425e4efa4fd0164306c408db6015f743e1dc48b65fb4.js'));
  }
  if (key === 'a0928018232ebc28f4ca1c86facecec30501ce18340d10d859015308b151bc6e') {
    pending.push(import('./chunks/chunk-50804ba7ee8153ef056cadff5a323fd4cc1555a9b1c07104e58f5730a66a6956.js'));
  }
  if (key === '63e4f2f9d53dbf87ee2abe2ec54a3cbafbe4e217ad353365cf7d354518c6ed2f') {
    pending.push(import('./chunks/chunk-a7eaa8fa446e0ee87de66bb556a3f91c356cc810389ab431b8ff5aa2966cf94c.js'));
  }
  if (key === '8adc784b414df344d91dc04d5962635238e906fcb797e8c69dc1c9f4a9464a70') {
    pending.push(import('./chunks/chunk-f45ef4cfca45b35284d53d548f8140cc4e361db5cefde32716677426eb722cd0.js'));
  }
  if (key === '3c330b0079b5421a496a32272f9debb11b981d512768c9f008b4e2a47ee0f8a8') {
    pending.push(import('./chunks/chunk-a7eaa8fa446e0ee87de66bb556a3f91c356cc810389ab431b8ff5aa2966cf94c.js'));
  }
  if (key === '9cbcf127860031a32af32413b1cc6d5bed38bdb3074bce8d08387d06c6a1c332') {
    pending.push(import('./chunks/chunk-143ed1e752459763e2c89fd8d3ef7c32dfb3f1f9bc9d9d3c5b3470c7c424d749.js'));
  }
  if (key === 'f61349a561a648d5fbbcf24390139e9d7b640d23d40f122d30187b979c180bfc') {
    pending.push(import('./chunks/chunk-50804ba7ee8153ef056cadff5a323fd4cc1555a9b1c07104e58f5730a66a6956.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}