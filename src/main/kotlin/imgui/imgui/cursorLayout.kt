package imgui.imgui

import gli_.hasnt
import glm_.glm
import glm_.i
import glm_.vec2.Vec2
import imgui.Col
import imgui.Context.style
import imgui.ImGui.currentWindow
import imgui.ImGui.currentWindowRead
import imgui.ImGui.itemAdd
import imgui.ImGui.itemSize
import imgui.ImGui.popClipRect
import imgui.ImGui.pushColumnClipRect
import imgui.ImGui.verticalSeparator
import imgui.internal.GroupData
import imgui.internal.Rect
import imgui.internal.has
import imgui.internal.isPowerOfTwo
import imgui.internal.or
import imgui.logRenderedText
import imgui.Context as g
import imgui.internal.LayoutType as Lt
import imgui.internal.SeparatorFlags as Sf

interface imgui_cursorLayout {

    /** Separator, generally horizontal. inside a menu bar or in horizontal layout mode, this becomes a vertical separator. */
    fun separator() {

        val window = currentWindow
        if (window.skipItems) return

        var flags = 0
        if (flags hasnt (Sf.Horizontal or Sf.Vertical))
            flags = flags or if (window.dc.layoutType == Lt.Horizontal) Sf.Vertical else Sf.Horizontal
        // Check that only 1 option is selected
        assert((flags and (Sf.Horizontal or Sf.Vertical)).isPowerOfTwo)

        if (flags has Sf.Vertical) {
            verticalSeparator()
            return
        }
        // Horizontal Separator
        if (window.dc.columnsCount > 1)
            popClipRect()

        var x1 = window.pos.x
        val x2 = window.pos.x + window.size.x
        if (window.dc.groupStack.isNotEmpty())
            x1 += window.dc.indentX.i

        val bb = Rect(Vec2(x1, window.dc.cursorPos.y), Vec2(x2, window.dc.cursorPos.y + 1f))
        // NB: we don't provide our width so that it doesn't get feed back into AutoFit, we don't provide height to not alter layout.
        itemSize(Vec2())
        if (!itemAdd(bb)) {
            if (window.dc.columnsCount > 1)
                pushColumnClipRect()
            return
        }

        window.drawList.addLine(bb.min, Vec2(bb.max.x, bb.min.y), Col.Separator.u32)

        if (g.logEnabled)
            logRenderedText(null, "\n--------------------------------")

        if (window.dc.columnsCount > 1)        {
            pushColumnClipRect()
            window.dc.columnsCellMinY = window.dc.cursorPos.y
        }
    }

    /** Call between widgets or groups to layout them horizontally
     *  Gets back to previous line and continue with horizontal layout
     *      posX == 0      : follow right after previous item
     *      posX != 0      : align to specified x position (relative to window/group left)
     *      spacingW < 0   : use default spacing if posX == 0, no spacing if posX != 0
     *      spacingW >= 0  : enforce spacing amount    */
    fun sameLine(posX: Float = 0f, spacingW: Float = -1f) {

        val window = currentWindow
        if (window.skipItems) return

        with(window) {
            dc.cursorPos.put(
                    if (posX != 0f)
                        pos.x - scroll.x + posX + glm.max(0f, spacingW) + dc.groupOffsetX + dc.columnsOffsetX
                    else
                        dc.cursorPosPrevLine.x + if (spacingW < 0f) style.itemSpacing.x else spacingW
                    , dc.cursorPosPrevLine.y)
            dc.currentLineHeight = dc.prevLineHeight
            dc.currentLineTextBaseOffset = dc.prevLineTextBaseOffset
        }
    }

    /** undo a sameLine()   */
    fun newLine() {
        val window = currentWindow
        if (window.skipItems) return

        val backupLayoutType = window.dc.layoutType
        window.dc.layoutType = Lt.Vertical
        // In the event that we are on a line with items that is smaller that FontSize high, we will preserve its height.
        itemSize(Vec2(0f, if (window.dc.currentLineHeight > 0f) 0f else g.fontSize))
        window.dc.layoutType = backupLayoutType
    }

    /** add vertical spacing    */
    fun spacing() {
        if (currentWindow.skipItems) return
        itemSize(Vec2())
    }

    /** add a dummy item of given size  */
    fun dummy(size: Vec2) {

        val window = currentWindow
        if (window.skipItems) return

        val bb = Rect(window.dc.cursorPos, window.dc.cursorPos + size)
        itemSize(bb)
        itemAdd(bb)
    }

    /** move content position toward the right, by style.IndentSpacing or indent_w if >0    */
    fun indent(indentW: Float = 0f) = with(currentWindow) {
        dc.indentX += if (indentW > 0f) indentW else style.indentSpacing
        dc.cursorPos.x = pos.x + dc.indentX + dc.columnsOffsetX
    }

    /** move content position back to the left, by style.IndentSpacing or indent_w if >0    */
    fun unindent(indentW: Float = 0f) = with(currentWindow) {
        dc.indentX -= if (indentW > 0f) indentW else style.indentSpacing
        dc.cursorPos.x = pos.x + dc.indentX + dc.columnsOffsetX
    }


    /** Lock horizontal starting position + capture group bounding box into one "item" (so you can use IsItemHovered()
     *  or layout primitives such as SameLine() on whole group, etc.)   */
    fun beginGroup() {

        with(currentWindow) {
            dc.groupStack.add(
                    GroupData().apply {
                        backupCursorPos put dc.cursorPos
                        backupCursorMaxPos put dc.cursorMaxPos
                        backupIndentX = dc.indentX
                        backupGroupOffsetX = dc.groupOffsetX
                        backupCurrentLineHeight = dc.currentLineHeight
                        backupCurrentLineTextBaseOffset = dc.currentLineTextBaseOffset
                        backupLogLinePosY = dc.logLinePosY
                        backupActiveIdIsAlive = g.activeIdIsAlive
                        advanceCursor = true
                    })
            dc.groupOffsetX = dc.cursorPos.x - pos.x - dc.columnsOffsetX
            dc.indentX = dc.groupOffsetX
            dc.cursorMaxPos put dc.cursorPos
            dc.currentLineHeight = 0f
            dc.logLinePosY = dc.cursorPos.y - 9999f
        }
    }

    fun endGroup() {

        val window = currentWindow

        assert(window.dc.groupStack.isNotEmpty())    // Mismatched BeginGroup()/EndGroup() calls

        val groupData = window.dc.groupStack.last()

        val groupBb = Rect(groupData.backupCursorPos, window.dc.cursorMaxPos)
        groupBb.max.y -= style.itemSpacing.y      // Cancel out last vertical spacing because we are adding one ourselves.
        groupBb.max = glm.max(groupBb.min, groupBb.max)

        with(window.dc) {
            cursorPos put groupData.backupCursorPos
            cursorMaxPos put glm.max(groupData.backupCursorMaxPos, cursorMaxPos)
            currentLineHeight = groupData.backupCurrentLineHeight
            currentLineTextBaseOffset = groupData.backupCurrentLineTextBaseOffset
            indentX = groupData.backupIndentX
            groupOffsetX = groupData.backupGroupOffsetX
            logLinePosY = cursorPos.y - 9999f
        }

        if (groupData.advanceCursor) {
            window.dc.currentLineTextBaseOffset = glm.max(window.dc.prevLineTextBaseOffset, groupData.backupCurrentLineTextBaseOffset)      // FIXME: Incorrect, we should grab the base offset from the *first line* of the group but it is hard to obtain now.
            itemSize(groupBb.size, groupData.backupCurrentLineTextBaseOffset)
            itemAdd(groupBb)
        }

        /*  If the current ActiveId was declared within the boundary of our group, we copy it to LastItemId so
            IsItemActive() will be functional on the entire group.
            It would be be neater if we replaced window.DC.LastItemId by e.g. 'bool LastItemIsActive', but if you
            search for LastItemId you'll notice it is only used in that context.    */
        val activeIdWithinGroup = !groupData.backupActiveIdIsAlive && g.activeIdIsAlive && g.activeId != 0 &&
                g.activeIdWindow!!.rootWindow === window.rootWindow
        if (activeIdWithinGroup)
            window.dc.lastItemId = g.activeId
        window.dc.lastItemRect put groupBb
        window.dc.groupStack.pop() // TODO last() on top -> pop?

        //window->DrawList->AddRect(groupBb.Min, groupBb.Max, IM_COL32(255,0,255,255));   // [Debug]
    }

    /** cursor position is relative to window position  */
    var cursorPos
        get() = with(currentWindowRead!!) { dc.cursorPos - pos + scroll }
        set(value) = with(currentWindowRead!!) {
            dc.cursorPos put (pos - scroll + value)
            dc.cursorMaxPos = glm.max(dc.cursorMaxPos, dc.cursorPos)
        }

    /** cursor position is relative to window position  */
    var cursorPosX
        get() = with(currentWindowRead!!) { dc.cursorPos.x - pos.x + scroll.x }
        set(value) = with(currentWindowRead!!) {
            dc.cursorPos.x = pos.x - scroll.x + value
            dc.cursorMaxPos.x = glm.max(dc.cursorMaxPos.x, dc.cursorPos.x)
        }

    /** cursor position is relative to window position  */
    var cursorPosY
        get() = with(currentWindowRead!!) { dc.cursorPos.y - pos.y + scroll.y }
        set(value) = with(currentWindowRead!!) {
            dc.cursorPos.y = pos.y - scroll.y + value
            dc.cursorMaxPos.y = glm.max(dc.cursorMaxPos.y, dc.cursorPos.y)
        }

    /** initial cursor position */
    val cursorStartPos get() = with(currentWindowRead!!) { dc.cursorStartPos - pos }

    /** cursor position in absolute screen coordinates [0..io.DisplaySize] (useful to work with ImDrawList API) */
    var cursorScreenPos
        get() = currentWindowRead!!.dc.cursorPos
        set(value) = with(currentWindowRead!!.dc) {
            cursorPos put value
            cursorPos max_ cursorMaxPos
        }

    /** Vertically align/lower upcoming text to framePadding.y so that it will aligns to upcoming widgets
     *  (call if you have text on a line before regular widgets)    */
    fun alignTextToFramePadding() {
        val window = currentWindow
        if (window.skipItems) return
        window.dc.currentLineHeight = glm.max(window.dc.currentLineHeight, g.fontSize + style.framePadding.y * 2)
        window.dc.currentLineTextBaseOffset = glm.max(window.dc.currentLineTextBaseOffset, style.framePadding.y)
    }

    /** height of font == GetWindowFontSize()   */
    val textLineHeight get() = g.fontSize

    /** distance (in pixels) between 2 consecutive lines of text == GetWindowFontSize() + GetStyle().ItemSpacing.y  */
    val textLineHeightWithSpacing get() = g.fontSize + style.itemSpacing.y

    /** distance (in pixels) between 2 consecutive lines of standard height widgets ==
     *  GetWindowFontSize() + GetStyle().FramePadding.y*2 + GetStyle().ItemSpacing.y    */
    val itemsLineHeightWithSpacing get() = g.fontSize + style.framePadding.y * 2f + style.itemSpacing.y

}