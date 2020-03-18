// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.microsoft.digitalintensity;

interface AnchorCreationListener {
    void onAnchorCreated(AnchorVisual createdAnchor);
    void onAnchorCreationFailed(AnchorVisual placedAnchor, String errorMessage);
}
