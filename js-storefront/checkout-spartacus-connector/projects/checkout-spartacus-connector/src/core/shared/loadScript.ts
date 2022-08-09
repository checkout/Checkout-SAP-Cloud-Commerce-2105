import { WindowRef } from '@spartacus/core';

export const loadScript = (windowRef: WindowRef, script: string, onloadCallback?: any, idScript?: string): void => {
  let isFound = false;
  const scripts = windowRef.document.getElementsByTagName('script');
  /* tslint:disable */
  for (let i = 0; i < scripts.length; ++i) {
    if (
      scripts[i].getAttribute('src') != null &&
      scripts[i].getAttribute('src') === script
    ) {
      isFound = true;
    }
  }
  /* tslint:enable */

  if (!isFound) {
    const node = windowRef.document.createElement('script');
    node.src = script;
    node.id = idScript
    node.type = 'text/javascript';
    node.async = true;
    if (onloadCallback) {
      node.onload = onloadCallback;
    }

    windowRef.document.getElementsByTagName('head')[0].appendChild(node);
  }
};
