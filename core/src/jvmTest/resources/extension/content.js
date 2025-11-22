// KDriver test extension content script
// This script adds a marker element to the page to verify the extension was loaded

(function () {
    // Create a marker div that we can detect in tests
    const marker = document.createElement('div');
    marker.id = 'kdriver-extension-marker';
    marker.setAttribute('data-extension-loaded', 'true');
    marker.style.display = 'none';
    marker.textContent = 'KDriver Extension Active';

    // Insert the marker into the document
    if (document.body) {
        document.body.appendChild(marker);
    } else {
        document.addEventListener('DOMContentLoaded', function () {
            document.body.appendChild(marker);
        });
    }
})();
