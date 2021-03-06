package imgui

import glm_.vec2.Vec2
import imgui.ImGui._begin
import imgui.ImGui.beginChild
import imgui.ImGui.beginMainMenuBar
import imgui.ImGui.beginMenu
import imgui.ImGui.beginMenuBar
import imgui.ImGui.beginPopupContextWindow
import imgui.ImGui.beginPopupModal
import imgui.ImGui.beginTooltip
import imgui.ImGui.collapsingHeader
import imgui.ImGui.end
import imgui.ImGui.endChild
import imgui.ImGui.endMainMenuBar
import imgui.ImGui.endMenu
import imgui.ImGui.endMenuBar
import imgui.ImGui.endPopup
import imgui.ImGui.endTooltip
import imgui.ImGui.menuItem
import imgui.ImGui.popId
import imgui.ImGui.popItemWidth
import imgui.ImGui.popStyleVar
import imgui.ImGui.popTextWrapPos
import imgui.ImGui.pushId
import imgui.ImGui.pushItemWidth
import imgui.ImGui.pushStyleVar
import imgui.ImGui.pushTextWrapPos
import imgui.ImGui.treeNode
import imgui.ImGui.treePop
import kotlin.reflect.KMutableProperty0

object functionalProgramming {

    inline fun button(label: String, sizeArg: Vec2 = Vec2(), block: () -> Unit) {
        if (ImGui.buttonEx(label, sizeArg, 0)) block()
    }

    inline fun smallButton(label: String, block: () -> Unit) {
        if (ImGui.smallButton(label)) block()
    }

    inline fun withWindow(name: String, open: KMutableProperty0<Boolean>? = null, flags: Int = 0, block: () -> Unit) {
        _begin(name, open, flags)
        block()
        end()
    }

    inline fun window(name: String, open: KMutableProperty0<Boolean>?, flags: Int = 0, block: () -> Unit) {
        if (_begin(name, open, flags)) {
            block()
            end()
        }
    }

    inline fun menuBar(block: () -> Unit) {
        if (beginMenuBar()) {
            block()
            endMenuBar()
        }
    }

    inline fun mainMenuBar(block: () -> Unit) {
        if (beginMainMenuBar()) {
            block()
            endMainMenuBar()
        }
    }

    inline fun menu(label: String, enabled: Boolean = true, block: () -> Unit) {
        if (beginMenu(label, enabled)) {
            block()
            endMenu()
        }
    }

    inline fun menuItem(label: String, shortcut: String = "", selected: Boolean = false, enabled: Boolean = true, block: () -> Unit) {
        if (menuItem(label, shortcut, selected, enabled)) block()
    }

    inline fun collapsingHeader(label: String, flags: Int = 0, block: () -> Unit) {
        if (collapsingHeader(label, flags))
            block()
    }

    inline fun collapsingHeader(label: String, open: KMutableProperty0<Boolean>, flags: Int = 0, block: () -> Unit) {
        if (collapsingHeader(label, open, flags))
            block()
    }

    inline fun treeNode(label: String, block: () -> Unit) {
        if (treeNode(label)) {
            block()
            treePop()
        }
    }

    inline fun withChild(strId: String, size: Vec2 = Vec2(), border: Boolean = false, extraFlags: Int = 0, block: () -> Unit) {
        beginChild(strId, size, border, extraFlags)
        block()
        endChild()
    }

    inline fun treeNode(strId: String, fmt: String, block: () -> Unit) {
        if (treeNode(strId, fmt)) {
            block()
            treePop()
        }
    }

    inline fun treeNode(ptrId: Any, fmt: String, block: () -> Unit) {
        if (treeNode(ptrId, fmt)) {
            block()
            treePop()
        }
    }

    inline fun popupModal(name: String, pOpen: BooleanArray? = null, extraFlags: Int = 0, block: () -> Unit) {
        if (beginPopupModal(name, pOpen, extraFlags)) {
            block()
            endPopup()
        }
    }

    inline fun withStyleVar(idx: StyleVar, value: Any, block: () -> Unit) {
        pushStyleVar(idx, value)
        block()
        popStyleVar()
    }

    inline fun withItemWidth(itemWidth: Float, block: () -> Unit) {
        pushItemWidth(itemWidth)
        block()
        popItemWidth()
    }

    inline fun withId(id: Int, block: () -> Unit) {
        pushId(id)
        block()
        popId()
    }

    inline fun withTooltip(block: () -> Unit) {
        beginTooltip()
        block()
        endTooltip()
    }

    inline fun withTextWrapPos(wrapPosX: Float = 0f, block: () -> Unit) {
        pushTextWrapPos(wrapPosX)
        block()
        popTextWrapPos()
    }

    inline fun popupContextWindow(block: () -> Unit) {
        if (beginPopupContextWindow()) {
            block()
            endPopup()
        }
    }
}