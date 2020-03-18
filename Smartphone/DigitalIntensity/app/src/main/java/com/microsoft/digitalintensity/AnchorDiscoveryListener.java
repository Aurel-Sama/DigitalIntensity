// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.microsoft.digitalintensity;

import com.microsoft.azure.spatialanchors.CloudSpatialAnchor;

interface AnchorDiscoveryListener {
    void onAnchorDiscovered(CloudSpatialAnchor cloudAnchor);
}
