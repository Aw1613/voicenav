package com.elderassist.app.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccessibleNodeSummary(
    @SerialName("node_id")
    val nodeId: String,
    val text: String? = null,
    @SerialName("content_description")
    val contentDescription: String? = null,
    @SerialName("class_name")
    val className: String? = null,
    val clickable: Boolean = false,
    val enabled: Boolean = true,
    val visible: Boolean = true,
    val scrollable: Boolean = false,
    val editable: Boolean = false,
    @kotlinx.serialization.Transient
    val isSensitive: Boolean = false
)

@Serializable
data class ScreenSnapshot(
    @SerialName("snapshot_id")
    val snapshotId: String,
    @kotlinx.serialization.Transient
    val timestampMs: Long = 0L,
    @SerialName("package_name")
    val packageName: String,
    @SerialName("window_title")
    val windowTitle: String? = null,
    val nodes: List<AccessibleNodeSummary>
)
