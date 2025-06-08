package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import dev.kdriver.cdp.cacheGeneratedDomain
import dev.kdriver.cdp.getGeneratedDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.input: Input
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Input(this))

public class Input(
    private val cdp: CDP,
) : Domain {
    /**
     * Emitted only when `Input.setInterceptDrags` is enabled. Use this data with `Input.dispatchDragEvent` to
     * restore normal drag and drop behavior.
     */
    public val dragIntercepted: Flow<DragInterceptedParameter> = cdp
        .events
        .filter { it.method == "Input.dragIntercepted" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Dispatches a drag event into the page.
     */
    public suspend fun dispatchDragEvent(args: DispatchDragEventParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Input.dispatchDragEvent", parameter)
    }

    /**
     * Dispatches a drag event into the page.
     */
    public suspend fun dispatchDragEvent(
        type: String,
        x: Double,
        y: Double,
        `data`: DragData,
        modifiers: Int? = null,
    ) {
        val parameter = DispatchDragEventParameter(type = type, x = x, y = y, data = data, modifiers = modifiers)
        dispatchDragEvent(parameter)
    }

    /**
     * Dispatches a key event to the page.
     */
    public suspend fun dispatchKeyEvent(args: DispatchKeyEventParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Input.dispatchKeyEvent", parameter)
    }

    /**
     * Dispatches a key event to the page.
     */
    public suspend fun dispatchKeyEvent(
        type: String,
        modifiers: Int? = null,
        timestamp: Double? = null,
        text: String? = null,
        unmodifiedText: String? = null,
        keyIdentifier: String? = null,
        code: String? = null,
        key: String? = null,
        windowsVirtualKeyCode: Int? = null,
        nativeVirtualKeyCode: Int? = null,
        autoRepeat: Boolean? = null,
        isKeypad: Boolean? = null,
        isSystemKey: Boolean? = null,
        location: Int? = null,
        commands: List<String>? = null,
    ) {
        val parameter = DispatchKeyEventParameter(
            type = type,
            modifiers = modifiers,
            timestamp = timestamp,
            text = text,
            unmodifiedText = unmodifiedText,
            keyIdentifier = keyIdentifier,
            code = code,
            key = key,
            windowsVirtualKeyCode = windowsVirtualKeyCode,
            nativeVirtualKeyCode = nativeVirtualKeyCode,
            autoRepeat = autoRepeat,
            isKeypad = isKeypad,
            isSystemKey = isSystemKey,
            location = location,
            commands = commands
        )
        dispatchKeyEvent(parameter)
    }

    /**
     * This method emulates inserting text that doesn't come from a key press,
     * for example an emoji keyboard or an IME.
     */
    public suspend fun insertText(args: InsertTextParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Input.insertText", parameter)
    }

    /**
     * This method emulates inserting text that doesn't come from a key press,
     * for example an emoji keyboard or an IME.
     */
    public suspend fun insertText(text: String) {
        val parameter = InsertTextParameter(text = text)
        insertText(parameter)
    }

    /**
     * This method sets the current candidate text for ime.
     * Use imeCommitComposition to commit the final text.
     * Use imeSetComposition with empty string as text to cancel composition.
     */
    public suspend fun imeSetComposition(args: ImeSetCompositionParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Input.imeSetComposition", parameter)
    }

    /**
     * This method sets the current candidate text for ime.
     * Use imeCommitComposition to commit the final text.
     * Use imeSetComposition with empty string as text to cancel composition.
     */
    public suspend fun imeSetComposition(
        text: String,
        selectionStart: Int,
        selectionEnd: Int,
        replacementStart: Int? = null,
        replacementEnd: Int? = null,
    ) {
        val parameter = ImeSetCompositionParameter(
            text = text,
            selectionStart = selectionStart,
            selectionEnd = selectionEnd,
            replacementStart = replacementStart,
            replacementEnd = replacementEnd
        )
        imeSetComposition(parameter)
    }

    /**
     * Dispatches a mouse event to the page.
     */
    public suspend fun dispatchMouseEvent(args: DispatchMouseEventParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Input.dispatchMouseEvent", parameter)
    }

    /**
     * Dispatches a mouse event to the page.
     */
    public suspend fun dispatchMouseEvent(
        type: String,
        x: Double,
        y: Double,
        modifiers: Int? = null,
        timestamp: Double? = null,
        button: MouseButton? = null,
        buttons: Int? = null,
        clickCount: Int? = null,
        force: Double? = null,
        tangentialPressure: Double? = null,
        tiltX: Double? = null,
        tiltY: Double? = null,
        twist: Int? = null,
        deltaX: Double? = null,
        deltaY: Double? = null,
        pointerType: String? = null,
    ) {
        val parameter = DispatchMouseEventParameter(
            type = type,
            x = x,
            y = y,
            modifiers = modifiers,
            timestamp = timestamp,
            button = button,
            buttons = buttons,
            clickCount = clickCount,
            force = force,
            tangentialPressure = tangentialPressure,
            tiltX = tiltX,
            tiltY = tiltY,
            twist = twist,
            deltaX = deltaX,
            deltaY = deltaY,
            pointerType = pointerType
        )
        dispatchMouseEvent(parameter)
    }

    /**
     * Dispatches a touch event to the page.
     */
    public suspend fun dispatchTouchEvent(args: DispatchTouchEventParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Input.dispatchTouchEvent", parameter)
    }

    /**
     * Dispatches a touch event to the page.
     */
    public suspend fun dispatchTouchEvent(
        type: String,
        touchPoints: List<TouchPoint>,
        modifiers: Int? = null,
        timestamp: Double? = null,
    ) {
        val parameter = DispatchTouchEventParameter(
            type = type,
            touchPoints = touchPoints,
            modifiers = modifiers,
            timestamp = timestamp
        )
        dispatchTouchEvent(parameter)
    }

    /**
     * Cancels any active dragging in the page.
     */
    public suspend fun cancelDragging() {
        val parameter = null
        cdp.callCommand("Input.cancelDragging", parameter)
    }

    /**
     * Emulates touch event from the mouse event parameters.
     */
    public suspend fun emulateTouchFromMouseEvent(args: EmulateTouchFromMouseEventParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Input.emulateTouchFromMouseEvent", parameter)
    }

    /**
     * Emulates touch event from the mouse event parameters.
     */
    public suspend fun emulateTouchFromMouseEvent(
        type: String,
        x: Int,
        y: Int,
        button: MouseButton,
        timestamp: Double? = null,
        deltaX: Double? = null,
        deltaY: Double? = null,
        modifiers: Int? = null,
        clickCount: Int? = null,
    ) {
        val parameter = EmulateTouchFromMouseEventParameter(
            type = type,
            x = x,
            y = y,
            button = button,
            timestamp = timestamp,
            deltaX = deltaX,
            deltaY = deltaY,
            modifiers = modifiers,
            clickCount = clickCount
        )
        emulateTouchFromMouseEvent(parameter)
    }

    /**
     * Ignores input events (useful while auditing page).
     */
    public suspend fun setIgnoreInputEvents(args: SetIgnoreInputEventsParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Input.setIgnoreInputEvents", parameter)
    }

    /**
     * Ignores input events (useful while auditing page).
     */
    public suspend fun setIgnoreInputEvents(ignore: Boolean) {
        val parameter = SetIgnoreInputEventsParameter(ignore = ignore)
        setIgnoreInputEvents(parameter)
    }

    /**
     * Prevents default drag and drop behavior and instead emits `Input.dragIntercepted` events.
     * Drag and drop behavior can be directly controlled via `Input.dispatchDragEvent`.
     */
    public suspend fun setInterceptDrags(args: SetInterceptDragsParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Input.setInterceptDrags", parameter)
    }

    /**
     * Prevents default drag and drop behavior and instead emits `Input.dragIntercepted` events.
     * Drag and drop behavior can be directly controlled via `Input.dispatchDragEvent`.
     */
    public suspend fun setInterceptDrags(enabled: Boolean) {
        val parameter = SetInterceptDragsParameter(enabled = enabled)
        setInterceptDrags(parameter)
    }

    /**
     * Synthesizes a pinch gesture over a time period by issuing appropriate touch events.
     */
    public suspend fun synthesizePinchGesture(args: SynthesizePinchGestureParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Input.synthesizePinchGesture", parameter)
    }

    /**
     * Synthesizes a pinch gesture over a time period by issuing appropriate touch events.
     */
    public suspend fun synthesizePinchGesture(
        x: Double,
        y: Double,
        scaleFactor: Double,
        relativeSpeed: Int? = null,
        gestureSourceType: GestureSourceType? = null,
    ) {
        val parameter = SynthesizePinchGestureParameter(
            x = x,
            y = y,
            scaleFactor = scaleFactor,
            relativeSpeed = relativeSpeed,
            gestureSourceType = gestureSourceType
        )
        synthesizePinchGesture(parameter)
    }

    /**
     * Synthesizes a scroll gesture over a time period by issuing appropriate touch events.
     */
    public suspend fun synthesizeScrollGesture(args: SynthesizeScrollGestureParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Input.synthesizeScrollGesture", parameter)
    }

    /**
     * Synthesizes a scroll gesture over a time period by issuing appropriate touch events.
     */
    public suspend fun synthesizeScrollGesture(
        x: Double,
        y: Double,
        xDistance: Double? = null,
        yDistance: Double? = null,
        xOverscroll: Double? = null,
        yOverscroll: Double? = null,
        preventFling: Boolean? = null,
        speed: Int? = null,
        gestureSourceType: GestureSourceType? = null,
        repeatCount: Int? = null,
        repeatDelayMs: Int? = null,
        interactionMarkerName: String? = null,
    ) {
        val parameter = SynthesizeScrollGestureParameter(
            x = x,
            y = y,
            xDistance = xDistance,
            yDistance = yDistance,
            xOverscroll = xOverscroll,
            yOverscroll = yOverscroll,
            preventFling = preventFling,
            speed = speed,
            gestureSourceType = gestureSourceType,
            repeatCount = repeatCount,
            repeatDelayMs = repeatDelayMs,
            interactionMarkerName = interactionMarkerName
        )
        synthesizeScrollGesture(parameter)
    }

    /**
     * Synthesizes a tap gesture over a time period by issuing appropriate touch events.
     */
    public suspend fun synthesizeTapGesture(args: SynthesizeTapGestureParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Input.synthesizeTapGesture", parameter)
    }

    /**
     * Synthesizes a tap gesture over a time period by issuing appropriate touch events.
     */
    public suspend fun synthesizeTapGesture(
        x: Double,
        y: Double,
        duration: Int? = null,
        tapCount: Int? = null,
        gestureSourceType: GestureSourceType? = null,
    ) {
        val parameter = SynthesizeTapGestureParameter(
            x = x,
            y = y,
            duration = duration,
            tapCount = tapCount,
            gestureSourceType = gestureSourceType
        )
        synthesizeTapGesture(parameter)
    }

    @Serializable
    public data class TouchPoint(
        /**
         * X coordinate of the event relative to the main frame's viewport in CSS pixels.
         */
        public val x: Double,
        /**
         * Y coordinate of the event relative to the main frame's viewport in CSS pixels. 0 refers to
         * the top of the viewport and Y increases as it proceeds towards the bottom of the viewport.
         */
        public val y: Double,
        /**
         * X radius of the touch area (default: 1.0).
         */
        public val radiusX: Double? = null,
        /**
         * Y radius of the touch area (default: 1.0).
         */
        public val radiusY: Double? = null,
        /**
         * Rotation angle (default: 0.0).
         */
        public val rotationAngle: Double? = null,
        /**
         * Force (default: 1.0).
         */
        public val force: Double? = null,
        /**
         * The normalized tangential pressure, which has a range of [-1,1] (default: 0).
         */
        public val tangentialPressure: Double? = null,
        /**
         * The plane angle between the Y-Z plane and the plane containing both the stylus axis and the Y axis, in degrees of the range [-90,90], a positive tiltX is to the right (default: 0)
         */
        public val tiltX: Double? = null,
        /**
         * The plane angle between the X-Z plane and the plane containing both the stylus axis and the X axis, in degrees of the range [-90,90], a positive tiltY is towards the user (default: 0).
         */
        public val tiltY: Double? = null,
        /**
         * The clockwise rotation of a pen stylus around its own major axis, in degrees in the range [0,359] (default: 0).
         */
        public val twist: Int? = null,
        /**
         * Identifier used to track touch sources between events, must be unique within an event.
         */
        public val id: Double? = null,
    )

    @Serializable
    public enum class GestureSourceType {
        @SerialName("default")
        DEFAULT,

        @SerialName("touch")
        TOUCH,

        @SerialName("mouse")
        MOUSE,
    }

    @Serializable
    public enum class MouseButton {
        @SerialName("none")
        NONE,

        @SerialName("left")
        LEFT,

        @SerialName("middle")
        MIDDLE,

        @SerialName("right")
        RIGHT,

        @SerialName("back")
        BACK,

        @SerialName("forward")
        FORWARD,
    }

    @Serializable
    public data class DragDataItem(
        /**
         * Mime type of the dragged data.
         */
        public val mimeType: String,
        /**
         * Depending of the value of `mimeType`, it contains the dragged link,
         * text, HTML markup or any other data.
         */
        public val `data`: String,
        /**
         * Title associated with a link. Only valid when `mimeType` == "text/uri-list".
         */
        public val title: String? = null,
        /**
         * Stores the base URL for the contained markup. Only valid when `mimeType`
         * == "text/html".
         */
        public val baseURL: String? = null,
    )

    @Serializable
    public data class DragData(
        public val items: List<DragDataItem>,
        /**
         * List of filenames that should be included when dropping
         */
        public val files: List<String>? = null,
        /**
         * Bit field representing allowed drag operations. Copy = 1, Link = 2, Move = 16
         */
        public val dragOperationsMask: Int,
    )

    /**
     * Emitted only when `Input.setInterceptDrags` is enabled. Use this data with `Input.dispatchDragEvent` to
     * restore normal drag and drop behavior.
     */
    @Serializable
    public data class DragInterceptedParameter(
        public val `data`: DragData,
    )

    @Serializable
    public data class DispatchDragEventParameter(
        /**
         * Type of the drag event.
         */
        public val type: String,
        /**
         * X coordinate of the event relative to the main frame's viewport in CSS pixels.
         */
        public val x: Double,
        /**
         * Y coordinate of the event relative to the main frame's viewport in CSS pixels. 0 refers to
         * the top of the viewport and Y increases as it proceeds towards the bottom of the viewport.
         */
        public val y: Double,
        public val `data`: DragData,
        /**
         * Bit field representing pressed modifier keys. Alt=1, Ctrl=2, Meta/Command=4, Shift=8
         * (default: 0).
         */
        public val modifiers: Int? = null,
    )

    @Serializable
    public data class DispatchKeyEventParameter(
        /**
         * Type of the key event.
         */
        public val type: String,
        /**
         * Bit field representing pressed modifier keys. Alt=1, Ctrl=2, Meta/Command=4, Shift=8
         * (default: 0).
         */
        public val modifiers: Int? = null,
        /**
         * Time at which the event occurred.
         */
        public val timestamp: Double? = null,
        /**
         * Text as generated by processing a virtual key code with a keyboard layout. Not needed for
         * for `keyUp` and `rawKeyDown` events (default: "")
         */
        public val text: String? = null,
        /**
         * Text that would have been generated by the keyboard if no modifiers were pressed (except for
         * shift). Useful for shortcut (accelerator) key handling (default: "").
         */
        public val unmodifiedText: String? = null,
        /**
         * Unique key identifier (e.g., 'U+0041') (default: "").
         */
        public val keyIdentifier: String? = null,
        /**
         * Unique DOM defined string value for each physical key (e.g., 'KeyA') (default: "").
         */
        public val code: String? = null,
        /**
         * Unique DOM defined string value describing the meaning of the key in the context of active
         * modifiers, keyboard layout, etc (e.g., 'AltGr') (default: "").
         */
        public val key: String? = null,
        /**
         * Windows virtual key code (default: 0).
         */
        public val windowsVirtualKeyCode: Int? = null,
        /**
         * Native virtual key code (default: 0).
         */
        public val nativeVirtualKeyCode: Int? = null,
        /**
         * Whether the event was generated from auto repeat (default: false).
         */
        public val autoRepeat: Boolean? = null,
        /**
         * Whether the event was generated from the keypad (default: false).
         */
        public val isKeypad: Boolean? = null,
        /**
         * Whether the event was a system key event (default: false).
         */
        public val isSystemKey: Boolean? = null,
        /**
         * Whether the event was from the left or right side of the keyboard. 1=Left, 2=Right (default:
         * 0).
         */
        public val location: Int? = null,
        /**
         * Editing commands to send with the key event (e.g., 'selectAll') (default: []).
         * These are related to but not equal the command names used in `document.execCommand` and NSStandardKeyBindingResponding.
         * See https://source.chromium.org/chromium/chromium/src/+/main:third_party/blink/renderer/core/editing/commands/editor_command_names.h for valid command names.
         */
        public val commands: List<String>? = null,
    )

    @Serializable
    public data class InsertTextParameter(
        /**
         * The text to insert.
         */
        public val text: String,
    )

    @Serializable
    public data class ImeSetCompositionParameter(
        /**
         * The text to insert
         */
        public val text: String,
        /**
         * selection start
         */
        public val selectionStart: Int,
        /**
         * selection end
         */
        public val selectionEnd: Int,
        /**
         * replacement start
         */
        public val replacementStart: Int? = null,
        /**
         * replacement end
         */
        public val replacementEnd: Int? = null,
    )

    @Serializable
    public data class DispatchMouseEventParameter(
        /**
         * Type of the mouse event.
         */
        public val type: String,
        /**
         * X coordinate of the event relative to the main frame's viewport in CSS pixels.
         */
        public val x: Double,
        /**
         * Y coordinate of the event relative to the main frame's viewport in CSS pixels. 0 refers to
         * the top of the viewport and Y increases as it proceeds towards the bottom of the viewport.
         */
        public val y: Double,
        /**
         * Bit field representing pressed modifier keys. Alt=1, Ctrl=2, Meta/Command=4, Shift=8
         * (default: 0).
         */
        public val modifiers: Int? = null,
        /**
         * Time at which the event occurred.
         */
        public val timestamp: Double? = null,
        /**
         * Mouse button (default: "none").
         */
        public val button: MouseButton? = null,
        /**
         * A number indicating which buttons are pressed on the mouse when a mouse event is triggered.
         * Left=1, Right=2, Middle=4, Back=8, Forward=16, None=0.
         */
        public val buttons: Int? = null,
        /**
         * Number of times the mouse button was clicked (default: 0).
         */
        public val clickCount: Int? = null,
        /**
         * The normalized pressure, which has a range of [0,1] (default: 0).
         */
        public val force: Double? = null,
        /**
         * The normalized tangential pressure, which has a range of [-1,1] (default: 0).
         */
        public val tangentialPressure: Double? = null,
        /**
         * The plane angle between the Y-Z plane and the plane containing both the stylus axis and the Y axis, in degrees of the range [-90,90], a positive tiltX is to the right (default: 0).
         */
        public val tiltX: Double? = null,
        /**
         * The plane angle between the X-Z plane and the plane containing both the stylus axis and the X axis, in degrees of the range [-90,90], a positive tiltY is towards the user (default: 0).
         */
        public val tiltY: Double? = null,
        /**
         * The clockwise rotation of a pen stylus around its own major axis, in degrees in the range [0,359] (default: 0).
         */
        public val twist: Int? = null,
        /**
         * X delta in CSS pixels for mouse wheel event (default: 0).
         */
        public val deltaX: Double? = null,
        /**
         * Y delta in CSS pixels for mouse wheel event (default: 0).
         */
        public val deltaY: Double? = null,
        /**
         * Pointer type (default: "mouse").
         */
        public val pointerType: String? = null,
    )

    @Serializable
    public data class DispatchTouchEventParameter(
        /**
         * Type of the touch event. TouchEnd and TouchCancel must not contain any touch points, while
         * TouchStart and TouchMove must contains at least one.
         */
        public val type: String,
        /**
         * Active touch points on the touch device. One event per any changed point (compared to
         * previous touch event in a sequence) is generated, emulating pressing/moving/releasing points
         * one by one.
         */
        public val touchPoints: List<TouchPoint>,
        /**
         * Bit field representing pressed modifier keys. Alt=1, Ctrl=2, Meta/Command=4, Shift=8
         * (default: 0).
         */
        public val modifiers: Int? = null,
        /**
         * Time at which the event occurred.
         */
        public val timestamp: Double? = null,
    )

    @Serializable
    public data class EmulateTouchFromMouseEventParameter(
        /**
         * Type of the mouse event.
         */
        public val type: String,
        /**
         * X coordinate of the mouse pointer in DIP.
         */
        public val x: Int,
        /**
         * Y coordinate of the mouse pointer in DIP.
         */
        public val y: Int,
        /**
         * Mouse button. Only "none", "left", "right" are supported.
         */
        public val button: MouseButton,
        /**
         * Time at which the event occurred (default: current time).
         */
        public val timestamp: Double? = null,
        /**
         * X delta in DIP for mouse wheel event (default: 0).
         */
        public val deltaX: Double? = null,
        /**
         * Y delta in DIP for mouse wheel event (default: 0).
         */
        public val deltaY: Double? = null,
        /**
         * Bit field representing pressed modifier keys. Alt=1, Ctrl=2, Meta/Command=4, Shift=8
         * (default: 0).
         */
        public val modifiers: Int? = null,
        /**
         * Number of times the mouse button was clicked (default: 0).
         */
        public val clickCount: Int? = null,
    )

    @Serializable
    public data class SetIgnoreInputEventsParameter(
        /**
         * Ignores input events processing when set to true.
         */
        public val ignore: Boolean,
    )

    @Serializable
    public data class SetInterceptDragsParameter(
        public val enabled: Boolean,
    )

    @Serializable
    public data class SynthesizePinchGestureParameter(
        /**
         * X coordinate of the start of the gesture in CSS pixels.
         */
        public val x: Double,
        /**
         * Y coordinate of the start of the gesture in CSS pixels.
         */
        public val y: Double,
        /**
         * Relative scale factor after zooming (>1.0 zooms in, <1.0 zooms out).
         */
        public val scaleFactor: Double,
        /**
         * Relative pointer speed in pixels per second (default: 800).
         */
        public val relativeSpeed: Int? = null,
        /**
         * Which type of input events to be generated (default: 'default', which queries the platform
         * for the preferred input type).
         */
        public val gestureSourceType: GestureSourceType? = null,
    )

    @Serializable
    public data class SynthesizeScrollGestureParameter(
        /**
         * X coordinate of the start of the gesture in CSS pixels.
         */
        public val x: Double,
        /**
         * Y coordinate of the start of the gesture in CSS pixels.
         */
        public val y: Double,
        /**
         * The distance to scroll along the X axis (positive to scroll left).
         */
        public val xDistance: Double? = null,
        /**
         * The distance to scroll along the Y axis (positive to scroll up).
         */
        public val yDistance: Double? = null,
        /**
         * The number of additional pixels to scroll back along the X axis, in addition to the given
         * distance.
         */
        public val xOverscroll: Double? = null,
        /**
         * The number of additional pixels to scroll back along the Y axis, in addition to the given
         * distance.
         */
        public val yOverscroll: Double? = null,
        /**
         * Prevent fling (default: true).
         */
        public val preventFling: Boolean? = null,
        /**
         * Swipe speed in pixels per second (default: 800).
         */
        public val speed: Int? = null,
        /**
         * Which type of input events to be generated (default: 'default', which queries the platform
         * for the preferred input type).
         */
        public val gestureSourceType: GestureSourceType? = null,
        /**
         * The number of times to repeat the gesture (default: 0).
         */
        public val repeatCount: Int? = null,
        /**
         * The number of milliseconds delay between each repeat. (default: 250).
         */
        public val repeatDelayMs: Int? = null,
        /**
         * The name of the interaction markers to generate, if not empty (default: "").
         */
        public val interactionMarkerName: String? = null,
    )

    @Serializable
    public data class SynthesizeTapGestureParameter(
        /**
         * X coordinate of the start of the gesture in CSS pixels.
         */
        public val x: Double,
        /**
         * Y coordinate of the start of the gesture in CSS pixels.
         */
        public val y: Double,
        /**
         * Duration between touchdown and touchup events in ms (default: 50).
         */
        public val duration: Int? = null,
        /**
         * Number of times to perform the tap (e.g. 2 for double tap, default: 1).
         */
        public val tapCount: Int? = null,
        /**
         * Which type of input events to be generated (default: 'default', which queries the platform
         * for the preferred input type).
         */
        public val gestureSourceType: GestureSourceType? = null,
    )
}
