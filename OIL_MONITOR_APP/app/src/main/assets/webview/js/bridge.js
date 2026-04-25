/**
 * Oil Watcher — WebView Bridge Script
 *
 * This script is the JavaScript-side counterpart of AndroidBridge.kt.
 * It provides helper functions for HTML screens to communicate with native Android.
 *
 * The native side injects data via: window.receiveData(jsonObject)
 * The HTML side calls native via: AndroidBridge.navigate('screen')
 */

// ══════════════════════════════════════════
//  DATA INJECTION
// ══════════════════════════════════════════

/**
 * Called by native Android to inject data into the WebView page.
 * Override this function in each HTML page to handle screen-specific data.
 */
function receiveData(data) {
    console.log('[Bridge] Data received:', JSON.stringify(data));

    // Dispatch to page-specific handler if defined
    if (typeof onDataReceived === 'function') {
        onDataReceived(data);
    }
}

// ══════════════════════════════════════════
//  NAVIGATION HELPERS
// ══════════════════════════════════════════

function navigateTo(screen) {
    if (typeof AndroidBridge !== 'undefined') {
        AndroidBridge.navigate(screen);
    } else {
        console.warn('[Bridge] AndroidBridge not available — running in browser?');
    }
}

function goBack() {
    navigateTo('back');
}

function openStation(stationId) {
    if (typeof AndroidBridge !== 'undefined') {
        AndroidBridge.navigateToStation(stationId);
    }
}

function navigateToMap(lat, lng, stationName) {
    if (typeof AndroidBridge !== 'undefined') {
        AndroidBridge.navigateExternal(lat, lng, stationName);
    }
}

function switchTab(tab) {
    if (typeof AndroidBridge !== 'undefined') {
        AndroidBridge.switchTab(tab);
    }
}

// ══════════════════════════════════════════
//  SETTINGS HELPERS
// ══════════════════════════════════════════

function toggleSetting(key, value) {
    if (typeof AndroidBridge !== 'undefined') {
        AndroidBridge.toggleSetting(key, value);
    }
}

function signOut() {
    if (typeof AndroidBridge !== 'undefined') {
        AndroidBridge.signOut();
    }
}

// ══════════════════════════════════════════
//  DATA LOADING HELPERS
// ══════════════════════════════════════════

function loadMore(type, offset) {
    if (typeof AndroidBridge !== 'undefined') {
        AndroidBridge.loadMore(type, offset || 0);
    }
}

function refresh() {
    if (typeof AndroidBridge !== 'undefined') {
        AndroidBridge.refresh();
    }
}

function filterPeriod(period) {
    if (typeof AndroidBridge !== 'undefined') {
        AndroidBridge.filterPeriod(period);
    }
}

// ══════════════════════════════════════════
//  UTILITY
// ══════════════════════════════════════════

/**
 * Format a price value to display format.
 */
function formatPrice(value) {
    if (value === null || value === undefined) return '—';
    return '$' + parseFloat(value).toFixed(2);
}

/**
 * Format a timestamp to relative time.
 */
function timeAgo(timestamp) {
    const now = Date.now();
    const diff = now - timestamp;
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'just now';
    if (minutes < 60) return minutes + 'm ago';
    if (hours < 24) return hours + 'h ago';
    return days + 'd ago';
}

// Log that bridge is ready
console.log('[Bridge] Oil Watcher bridge.js loaded');
