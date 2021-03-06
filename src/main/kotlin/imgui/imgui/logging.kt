package imgui.imgui

import imgui.ImGui.button
import imgui.Context as g
import imgui.ImGui.currentWindowRead
import imgui.ImGui.popAllowKeyboardFocus
import imgui.ImGui.popId
import imgui.ImGui.popItemWidth
import imgui.ImGui.pushAllowKeyboardFocus
import imgui.ImGui.pushId
import imgui.ImGui.pushItemWidth
import imgui.ImGui.sameLine
import imgui.ImGui.sliderInt

/** Logging: all text output from interface is redirected to tty/file/clipboard. By default, tree nodes are
 *  automatically opened during logging.    */
interface imgui_logging {

//    IMGUI_API void          LogToTTY(int max_depth = -1);                                       // start logging to tty
//    IMGUI_API void          LogToFile(int max_depth = -1, const char* filename = NULL);         // start logging to file

    /** start logging ImGui output to OS clipboard   */
    fun logToClipboard(maxDepth: Int = -1) {

        if (g.logEnabled) return

        val window = g.currentWindow!!

        g.logEnabled = true
        g.logFile = null
        g.logStartDepth = window.dc.treeDepth
        if (maxDepth >= 0)
            g.logAutoExpandMaxDepth = maxDepth
    }


    /** stop logging (close file, etc.) */
    fun logFinish() {

        if (!g.logEnabled) return

        TODO()
//        LogText(IM_NEWLINE);
//        g.LogEnabled = false;
//        if (g.LogFile != NULL)
//        {
//            if (g.LogFile == stdout)
//                fflush(g.LogFile);
//            else
//                fclose(g.LogFile);
//            g.LogFile = NULL;
//        }
//        if (g.LogClipboard->size() > 1)
//        {
//            SetClipboardText(g.LogClipboard->begin());
//            g.LogClipboard->clear();
//        }
    }

    /** Helper to display buttons for logging to tty/file/clipboard */
    fun logButtons() {
        pushId("LogButtons")
        val logToTty = button("Log To TTY"); sameLine()
        val logToFile = button("Log To File"); sameLine()
        val logToClipboard = button("Log To Clipboard"); sameLine()
        pushItemWidth(80f)
        pushAllowKeyboardFocus(false)
        sliderInt("Depth", g::logAutoExpandMaxDepth, 0, 9)
        popAllowKeyboardFocus()
        popItemWidth()
        popId()

        // Start logging at the end of the function so that the buttons don't appear in the log
        if (logToTty) TODO()//LogToTTY(g.LogAutoExpandMaxDepth)
        if (logToFile) TODO()//LogToFile(g.LogAutoExpandMaxDepth, g.IO.LogFilename)
        if (logToClipboard) TODO()//LogToClipboard(g.LogAutoExpandMaxDepth)
    }

    /** pass text data straight to log (without being displayed)    */
    fun logText(fmt: String, vararg args: Any) {
        TODO()
//        ImGuiContext& g = *GImGui;
//        if (!g.LogEnabled)
//            return;
//
//        va_list args;
//        va_start(args, fmt);
//        if (g.LogFile)
//        {
//            vfprintf(g.LogFile, fmt, args);
//        }
//        else
//        {
//            g.LogClipboard->appendv(fmt, args);
//        }
//        va_end(args);
    }
}