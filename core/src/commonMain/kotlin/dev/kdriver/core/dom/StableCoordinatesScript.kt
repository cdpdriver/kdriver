package dev.kdriver.core.dom

/**
 * The function [DefaultElement] applies to an element to read its coordinates once its position is stable across
 * several frames; it resolves null when the element is detached or has no size.
 */
internal val STABLE_COORDINATES_JS = """
    function() {
        if (!this || !this.isConnected) return null;

        return new Promise(resolve => {
            let lastTop = null;
            let lastLeft = null;
            let stableFrames = 0;
            const maxAttempts = 10;
            let attempts = 0;

            const checkStable = () => {
                attempts++;
                const rect = this.getBoundingClientRect();

                if (rect.width === 0 || rect.height === 0) {
                    resolve(null);
                    return;
                }

                if (lastTop !== null &&
                    Math.abs(rect.top - lastTop) < 1 &&
                    Math.abs(rect.left - lastLeft) < 1) {
                    stableFrames++;
                    if (stableFrames >= 2) {
                        resolve({
                            x: rect.left + rect.width / 2,
                            y: rect.top + rect.height / 2
                        });
                        return;
                    }
                } else {
                    stableFrames = 0;
                }

                lastTop = rect.top;
                lastLeft = rect.left;

                if (attempts < maxAttempts) {
                    requestAnimationFrame(checkStable);
                } else {
                    // Timeout: use current position
                    resolve({
                        x: rect.left + rect.width / 2,
                        y: rect.top + rect.height / 2
                    });
                }
            };

            requestAnimationFrame(checkStable);
        });
    }
""".trimIndent()
