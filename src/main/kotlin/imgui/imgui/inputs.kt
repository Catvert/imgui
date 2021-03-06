package imgui.imgui

import glm_.glm
import glm_.vec2.Vec2
import imgui.IO
import imgui.ImGui.currentWindowRead
import imgui.Context.style
import imgui.ImGui.calcTypematicPressedRepeatAmount
import imgui.MOUSE_INVALID
import imgui.internal.Rect
import imgui.Context as g


interface imgui_inputs {

    fun getKeyIndex(imguiKey: Int) = IO.keyMap[imguiKey]

    /** is key being held. == io.KeysDown[user_key_index]. note that imgui doesn't know the semantic of each entry of io.KeyDown[].
     *  Use your own indices/enums according to how your backend/engine stored them into KeyDown[]! */
    fun isKeyDown(userKeyIndex: Int) = if (userKeyIndex < 0) false else IO.keysDown[userKeyIndex]

    /** uses user's key indices as stored in the keys_down[] array. if repeat=true.
     *  uses io.KeyRepeatDelay / KeyRepeatRate  */
    fun isKeyPressed(userKeyIndex: Int, repeat: Boolean): Boolean {

        if (userKeyIndex < 0) return false
        val t = IO.keysDownDuration[userKeyIndex]
        return when {
            t == 0f -> true
            repeat && t > IO.keyRepeatDelay -> getKeyPressedAmount(userKeyIndex, IO.keyRepeatDelay, IO.keyRepeatRate) > 0
            else -> false
        }
    }

    /** was key released (went from Down to !Down)..    */
    fun isKeyReleased(userKeyIndex: Int) = if (userKeyIndex < 0) false else IO.keysDownDurationPrev[userKeyIndex] >= 0f && !IO.keysDown[userKeyIndex]

    /** Uses provided repeat rate/delay. return a count, most often 0 or 1 but might be >1 if RepeatRate is small enough
     *  that DeltaTime > RepeatRate */
    fun getKeyPressedAmount(keyIndex: Int, repeatDelay: Float, repeatRate: Float): Int {
        if (keyIndex < 0) return 0
        assert(keyIndex in 0 until IO.keysDown.size)
        val t = IO.keysDownDuration[keyIndex]
        return calcTypematicPressedRepeatAmount(t, t - IO.deltaTime, repeatDelay, repeatRate)
    }

//IMGUI_API bool          IsMouseDown(int button);                                            // is mouse button held

    /** did mouse button clicked (went from !Down to Down)  */
    fun isMouseClicked(button: Int, repeat: Boolean = false): Boolean {

        assert(button >= 0 && button < IO.mouseDown.size)
        val t = IO.mouseDownDuration[button]
        if (t == 0f)
            return true

        if (repeat && t > IO.keyRepeatDelay) {
            val delay = IO.keyRepeatDelay
            val rate = IO.keyRepeatRate
            if ((glm.mod(t - delay, rate) > rate * 0.5f) != (glm.mod(t - delay - IO.deltaTime, rate) > rate * 0.5f))
                return true
        }
        return false
    }

    /** did mouse button double-clicked. a double-click returns false in IsMouseClicked(). uses io.MouseDoubleClickTime.    */
    fun isMouseDoubleClicked(button: Int) = IO.mouseDoubleClicked[button]

//IMGUI_API bool          IsMouseReleased(int button);                                        // did mouse button released (went from Down to !Down)
//IMGUI_API bool          IsMouseDragging(int button = 0, float lock_threshold = -1.0f);      // is mouse dragging. if lock_threshold < -1.0f uses io.MouseDraggingThreshold

    /** Test if mouse cursor is hovering given rectangle
     *  NB- Rectangle is clipped by our current clip setting
     *  NB- Expand the rectangle to be generous on imprecise inputs systems (g.style.TouchExtraPadding)
     *  is mouse hovering given bounding rect (in screen space). clipped by current clipping settings. disregarding of
     *  consideration of focus/window ordering/blocked by a popup.  */
    fun isMouseHoveringRect(r: Rect, clip: Boolean = true) = isMouseHoveringRect(r.min, r.max, clip)

    fun isMouseHoveringRect(rMin: Vec2, rMax: Vec2, clip: Boolean = true): Boolean {

        val window = g.currentWindow!!

        // Clip
        val rectClipped = Rect(rMin, rMax)
        if (clip)
            rectClipped.clipWith(window.clipRect)

        // Expand for touch input
        val rectForTouch = Rect(rectClipped.min - style.touchExtraPadding, rectClipped.max + style.touchExtraPadding)
        return rectForTouch contains IO.mousePos
    }

    /** We typically use ImVec2(-FLT_MAX,-FLT_MAX) to denote an invalid mouse position  */
    fun isMousePosValid(mousePos: Vec2? = null) = (mousePos ?: IO.mousePos) greaterThan MOUSE_INVALID

    /** shortcut to IO.mousePos provided by user, to be consistent with other calls */
    val mousePos get() = IO.mousePos
//IMGUI_API ImVec2        GetMousePosOnOpeningCurrentPopup();                                 // retrieve backup of mouse positioning at the time of opening popup we have BeginPopup() into

    /** dragging amount since clicking. if lockThreshold < -1.0f uses io.MouseDraggingThreshold    */
    fun getMouseDragDelta(button: Int = 0, lockThreshold: Float = -1f): Vec2 {

        assert(button >= 0 && button < IO.mouseDown.size)
        var lockThreshold = lockThreshold
        if (lockThreshold < 0f)
            lockThreshold = IO.mouseDragThreshold
        if (IO.mouseDown[button])
            if (IO.mouseDragMaxDistanceSqr[button] >= lockThreshold * lockThreshold)
                return IO.mousePos - IO.mouseClickedPos[button] // Assume we can only get active with left-mouse button (at the moment).
        return Vec2()
    }
//IMGUI_API void          ResetMouseDragDelta(int button = 0);                                //
//IMGUI_API ImGuiMouseCursor GetMouseCursor();                                                // get desired cursor type, reset in ImGui::NewFrame(), this is updated during the frame. valid before Render(). If you use software rendering by setting io.MouseDrawCursor ImGui will render those for you
//IMGUI_API void          SetMouseCursor(ImGuiMouseCursor type);                              // set desired cursor type
//IMGUI_API void          CaptureKeyboardFromApp(bool capture = true);                        // manually override io.WantCaptureKeyboard flag next frame (said flag is entirely left for your application handle). e.g. force capture keyboard when your widget is being hovered.
//IMGUI_API void          CaptureMouseFromApp(bool capture = true);                           // manually override io.WantCaptureMouse flag next frame (said flag is entirely left for your application handle).

}